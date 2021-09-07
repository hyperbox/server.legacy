/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 - Max Dor
 *
 * https://apps.kamax.io/hyperbox
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package io.kamax.hboxd.hypervisor.vbox6_1;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.comm.io.MachineLogFileIO;
import io.kamax.hbox.constant.EntityType;
import io.kamax.hbox.data.MachineData;
import io.kamax.hbox.exception.*;
import io.kamax.hbox.exception.net.InvalidNetworkModeException;
import io.kamax.hbox.exception.net.NetworkAdaptorNotFoundException;
import io.kamax.hbox.hypervisor.vbox.VBoxNetMode;
import io.kamax.hbox.hypervisor.vbox._MachineLogFile;
import io.kamax.hbox.hypervisor.vbox.net.VBoxAdaptor;
import io.kamax.hbox.hypervisor.vbox.net._NetAdaptor;
import io.kamax.hbox.hypervisor.vbox.net._NetMode;
import io.kamax.hbox.states.ServiceState;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event._EventManager;
import io.kamax.hboxd.event.hypervisor.HypervisorConfiguredEvent;
import io.kamax.hboxd.event.hypervisor.HypervisorConnectedEvent;
import io.kamax.hboxd.event.hypervisor.HypervisorDisconnectedEvent;
import io.kamax.hboxd.event.net.NetAdaptorAddedEvent;
import io.kamax.hboxd.event.net.NetAdaptorRemovedEvent;
import io.kamax.hboxd.event.service.ServiceStateEvent;
import io.kamax.hboxd.hypervisor.Hypervisor;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.hypervisor._RawOsType;
import io.kamax.hboxd.hypervisor.host._RawHost;
import io.kamax.hboxd.hypervisor.storage._RawMedium;
import io.kamax.hboxd.hypervisor.storage._RawStorageControllerSubType;
import io.kamax.hboxd.hypervisor.storage._RawStorageControllerType;
import io.kamax.hboxd.hypervisor.vbox6_1.data.Mappings;
import io.kamax.hboxd.hypervisor.vbox6_1.factory.OsTypeFactory;
import io.kamax.hboxd.hypervisor.vbox6_1.host.VBoxHost;
import io.kamax.hboxd.hypervisor.vbox6_1.manager.VBoxSessionManager;
import io.kamax.hboxd.hypervisor.vbox6_1.net.VBoxBridgedAdaptor;
import io.kamax.hboxd.hypervisor.vbox6_1.net.VBoxHostOnlyAdaptor;
import io.kamax.hboxd.hypervisor.vbox6_1.net.VBoxNatNetworkAdaptor;
import io.kamax.hboxd.hypervisor.vbox6_1.service.EventsManagementService;
import io.kamax.hboxd.hypervisor.vbox6_1.storage.VBoxMedium;
import io.kamax.hboxd.hypervisor.vbox6_1.storage.VBoxStorageControllerSubType;
import io.kamax.hboxd.hypervisor.vbox6_1.storage.VBoxStorageControllerType;
import io.kamax.hboxd.hypervisor.vbox6_1.vm.VBoxMachine;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.service._Service;
import io.kamax.tools.AxStrings;
import io.kamax.tools.logging.KxLog;
import io.kamax.tools.setting.BooleanSetting;
import io.kamax.tools.setting.StringSetting;
import io.kamax.tools.setting._Setting;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.virtualbox_6_1.*;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VBoxHypervisor implements _Hypervisor {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    protected VirtualBoxManager vbMgr;

    private VBoxHost host;
    private _EventManager evMgr;
    // TODO keep this register up-to-date
    private Map<String, VBoxMedium> mediumRegister;
    private _Service evMgrSvc;

    private List<_RawOsType> osTypeCache;

    protected abstract VirtualBoxManager connect(String options);

    @Override
    public String getId() {
        return this.getClass().getAnnotation(Hypervisor.class).id();
    }

    @Override
    public String getTypeId() {
        return this.getClass().getAnnotation(Hypervisor.class).typeId();
    }

    @Override
    public String getVendor() {
        return this.getClass().getAnnotation(Hypervisor.class).vendor();
    }

    @Override
    public String getProduct() {
        return this.getClass().getAnnotation(Hypervisor.class).product();
    }

    @Override
    public void setEventManager(_EventManager evMgr) {

        this.evMgr = evMgr;
    }

    @Override
    public void start(String options) throws HypervisorException {
        EventManager.register(this);

        long start = System.currentTimeMillis();

        try {
            vbMgr = connect(options);
            VBox.set(vbMgr);

            log.info("Connected in " + (System.currentTimeMillis() - start) + "ms to " + VBox.get().getHost().getDomainName());
            log.info("VB Version: " + vbMgr.getVBox().getVersion());
            log.info("VB Revision: " + vbMgr.getVBox().getRevision());
            log.info("VB Client API Version: " + vbMgr.getClientAPIVersion());
            log.info("VB Server API Version: " + vbMgr.getVBox().getAPIVersion());

            if (!VBox.get().getAPIVersion().contentEquals(VBox.getManager().getClientAPIVersion())) {
                throw new HypervisorException("Mismatch API Connector: Server is " + VBox.get().getAPIVersion() + " but the connector handles "
                        + VBox.getManager().getClientAPIVersion());
            }

            host = new VBoxHost(VBox.get().getHost());

            log.info("Host OS: " + vbMgr.getVBox().getHost().getOperatingSystem() + " " + vbMgr.getVBox().getHost().getOSVersion());

            Mappings.load();

            mediumRegister = new ConcurrentHashMap<>();
            if (Configuration.getSetting("virtualbox.cache.medium.autoload", "0").contentEquals("1")) {
                log.debug("Loading media registry");
                updateMediumRegistry();
            }

            if (Configuration.getSetting("virtualbox.cache.osType.autoload", "0").contentEquals("1")) {
                log.debug("Loading OS Types");
                buildOsTypeCache();
            }

            try {
                if (evMgr != null) {
                    evMgrSvc = new EventsManagementService(evMgr);
                    evMgrSvc.startAndRun();
                } else {
                    throw new HypervisorException("No Event Manager was set to handle events from Virtualbox");
                }
            } catch (ServiceException e) {
                throw new HypervisorException("Unable to start the Event Manager Service : " + e.getMessage());
            }

            EventManager.post(new HypervisorConnectedEvent(this));
        } catch (HyperboxException e) {
            stop();
            throw e;
        } catch (Throwable t) {
            stop();
            throw new HyperboxException("Failed to connect: " + t.getMessage(), t);
        }
    }

    protected abstract void disconnect();

    @Override
    public void stop() {
        host = null;
        mediumRegister = null;
        osTypeCache = null;

        if (evMgrSvc != null) {
            if (!evMgrSvc.stopAndDie(15000)) {
                log.warn("Error when trying to stop the Event Manager Service");
            }
            evMgrSvc = null;
        }

        VBox.unset();
        if (vbMgr != null) {
            disconnect();
            vbMgr.cleanup();
            vbMgr = null;
        }

        EventManager.post(new HypervisorDisconnectedEvent(this));
        EventManager.unregister(this);
    }

    @Override
    public boolean isRunning() {
        try {
            return !vbMgr.getVBox().getVersion().isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    private void updateMediumRegistry() {
        mediumRegister.clear();
        registerMediums(vbMgr.getVBox().getDVDImages());
        registerMediums(vbMgr.getVBox().getHardDisks());
        registerMediums(vbMgr.getVBox().getFloppyImages());
    }

    private void registerMediums(List<IMedium> mediums) {
        for (IMedium medium : mediums) {
            mediumRegister.put(medium.getId(), new VBoxMedium(medium));
            registerMediums(medium.getChildren());
        }
    }

    private IMedium getRawMedium(String uuid) {
        updateMediumRegistry();
        _RawMedium rawMed = mediumRegister.get(uuid);
        return vbMgr.getVBox().openMedium(rawMed.getLocation(), DeviceType.fromValue(rawMed.getDeviceType()), AccessMode.ReadOnly, false);
    }

    private void buildOsTypeCache() {
        List<_RawOsType> osTypes = new ArrayList<>();
        for (IGuestOSType osType : vbMgr.getVBox().getGuestOSTypes()) {
            osTypes.add(OsTypeFactory.get(osType));
        }
        osTypeCache = osTypes;
    }

    @Override
    public _RawHost getHost() {
        return host;
    }

    @Override
    public _RawVM createMachine(String name, String osTypeId) {
        return createMachine(null, name, osTypeId);
    }

    @Override
    public _RawVM createMachine(String uuid, String name, String osTypeId) {
        if (uuid != null) {
            uuid = "UUID=" + uuid;
        }
        if (osTypeId == null) {
            if (vbMgr.getVBox().getHost().getProcessorFeature(ProcessorFeature.HWVirtEx)) {
                osTypeId = "Other_64";
            } else {
                osTypeId = "Other";
            }
        }

        try {
            IMachine machine = vbMgr.getVBox().createMachine(null, name, null, osTypeId, uuid);
            machine.saveSettings();
            vbMgr.getVBox().registerMachine(machine);
            uuid = machine.getId();
            return new VBoxMachine(uuid);
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public List<_RawVM> listMachines() {
        try {
            List<IMachine> rawMachines = vbMgr.getVBox().getMachines();
            List<_RawVM> machines = new ArrayList<>();
            for (IMachine rawMachine : rawMachines) {
                machines.add(new VBoxMachine(rawMachine));
            }
            return machines;
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public _RawVM getMachine(String uuid) {
        try {
            return new VBoxMachine(vbMgr.getVBox().findMachine(uuid));
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public _RawMedium createHardDisk(String filePath, String format, Long logicalSize) {
        // TODO find a way to know the smallest size for a given format, set to 2MB for now.
        if (logicalSize < 2048000L) {
            logicalSize = 2048000L;
        }

        try {
            // TODO check via ISystemProperties if the format is valid
            IMedium med = VBox.get().createMedium(format, filePath, AccessMode.ReadWrite, DeviceType.HardDisk);
            IProgress p = med.createBaseStorage(logicalSize, Collections.singletonList(MediumVariant.Standard));
            p.waitForCompletion(-1);
            if (p.getResultCode() != 0) {
                throw new HypervisorException("Unable to create hard disk: " + p.getErrorInfo().getResultCode() + " | " + p.getErrorInfo().getText());
            }
            updateMediumRegistry();
            return getMedium(filePath, DeviceType.HardDisk.toString());
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public _RawMedium getMedium(String medId) {
        // TODO use events instead of rescanning the data if data is not found
        if (!mediumRegister.containsKey(medId)) {
            updateMediumRegistry();
        }
        if (mediumRegister.containsKey(medId)) {
            return mediumRegister.get(medId);
        } else {
            // TODO logic should be in the business classes, not in hypervisor classes
            return getMedium(medId, DeviceType.DVD.toString());
        }
    }

    @Override
    public _RawMedium getMedium(String filePath, String mediumType) {
        // TODO check for mediumType validity
        try {
            IMedium medium = vbMgr.getVBox().openMedium(filePath, DeviceType.valueOf(mediumType), AccessMode.ReadOnly, false);
            if (medium.refreshState().equals(MediumState.Inaccessible)) {
                medium.close();
                throw new HypervisorException("Unable to get " + filePath + " : " + medium.getLastAccessError());
            }
            return new VBoxMedium(medium);
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public _RawMedium getMedium(String filePath, EntityType mediumType) {
        return getMedium(filePath, mediumType.toString());
    }

    @Override
    public List<String> listNicAdapterTypes() {
        List<String> listInfo = new ArrayList<>();
        for (NetworkAdapterType adapterType : NetworkAdapterType.values()) {
            if (!adapterType.equals(NetworkAdapterType.Null)) {
                listInfo.add(adapterType.toString());
            }
        }
        return listInfo;
    }

    @Override
    public List<String> listNicAttachModes() {
        List<String> listInfo = new ArrayList<>();
        for (NetworkAttachmentType attachType : NetworkAttachmentType.values()) {
            listInfo.add(attachType.toString());
        }
        return listInfo;
    }

    @Override
    public List<String> listNicAttachNames(String attachMode) {
        List<String> listInfo = new ArrayList<>();
        NetworkAttachmentType type = NetworkAttachmentType.valueOf(attachMode);
        switch (type) {
            case Bridged:
                for (IHostNetworkInterface nic : vbMgr.getVBox().getHost().getNetworkInterfaces()) {
                    if (nic.getInterfaceType().equals(HostNetworkInterfaceType.Bridged)) {
                        listInfo.add(nic.getName());
                    }
                }
                break;
            case Generic:
                listInfo.addAll(vbMgr.getVBox().getGenericNetworkDrivers());
                break;
            case HostOnly:
                for (IHostNetworkInterface nic : vbMgr.getVBox().getHost().getNetworkInterfaces()) {
                    if (nic.getInterfaceType().equals(HostNetworkInterfaceType.HostOnly)) {
                        listInfo.add(nic.getName());
                    }
                }
                break;
            case Internal:
                listInfo.addAll(vbMgr.getVBox().getInternalNetworks());
                break;
            case NAT:
            case Null:
                break;
            case NATNetwork:
                for (INATNetwork net : VBox.get().getNATNetworks()) {
                    listInfo.add(net.getNetworkName());
                }
            default:
                throw new HypervisorException(attachMode + " is not supported as an attachment mode");
        }
        return listInfo;
    }

    @Override
    public _RawStorageControllerType getStorageControllerType(String id) {
        try {
            // We validate that the type exist in Virtualbox
            StorageBus.valueOf(id);

            return VBoxStorageControllerType.valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new HypervisorException(id + " is not a supported Controller Type");
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public List<_RawStorageControllerType> listStorageControllerType() {
        // TODO improve so _Raw... has a concrete implementation class and use the enum to fetch the min/max values
        // valueOf(id) should be used on VbStorageControllerType with the StorageBus.toString() as its ID.
        // This way we ensure that every StorageBus has a corresponding value with data and none is missed.
        // Reminder : must skip StorageBus.Null
        //

        return Arrays.asList(VBoxStorageControllerType.values());
    }

    @Override
    public _RawStorageControllerSubType getStorageControllerSubType(String id) {
        try {
            // We validate that the type exist in Virtualbox
            StorageControllerType.valueOf(id);

            return VBoxStorageControllerSubType.valueOf(id);
        } catch (IllegalArgumentException e) {
            throw new HypervisorException(id + " is not a supported Controller SubType");
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }

    }

    @Override
    public List<_RawStorageControllerSubType> listStorageControllerSubType(String type) {
        try {
            List<_RawStorageControllerSubType> subTypes = new ArrayList<>();
            // TODO Must validate using the same logic than listStorageControllerType - use IVirtualbox::StorageControllerType.toString() as lookup ID, skipping Null
            for (VBoxStorageControllerSubType subType : VBoxStorageControllerSubType.values()) {
                if (subType.getParentType().contentEquals(type)) {
                    subTypes.add(subType);
                }
            }
            return subTypes;
        } catch (IllegalArgumentException e) {
            throw new HypervisorException(type + " is not a supported Controller Type");
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public List<_RawOsType> listOsTypes() {
        if ((osTypeCache == null) || osTypeCache.isEmpty()) {
            buildOsTypeCache();
        }

        return new ArrayList<>(osTypeCache);
    }

    @Override
    public void deleteMachine(String uuid) {
        // TODO improve with multi-step exception handling, as well as a separate method for HDD deletion
        VBoxSessionManager.get().unlock(uuid);
        IMachine machine = vbMgr.getVBox().findMachine(uuid);

        try {
            List<IMedium> hdds = machine.unregister(CleanupMode.DetachAllReturnHardDisksOnly);
            IProgress p = machine.deleteConfig(hdds);
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        /*
                         * Waiting coefficient to use on ISession::getTimeRemaining() with wait() while waiting for task in progress to finish.<br/>
                         * Virtualbox returns a waiting time in seconds, this coefficient allows to turn it into milliseconds and set a 'shorter' waiting time for a more
                         * reactive update.<br/>
                         * Default value waits half of the estimated time reported by Virtualbox.
                         */
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * 500L);
                    }
                } catch (InterruptedException e) {
                    log.warn("Interrupted while waiting", e);
                }
            }
            log.debug("VBox API Return code: " + p.getResultCode());
            if (p.getResultCode() != 0) {
                throw new MachineException(p.getErrorInfo().getText());
            }
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public MachineData getMachineSettings(String osTypeId) {
        IGuestOSType rawOsType = vbMgr.getVBox().getGuestOSType(osTypeId);
        return OsTypeFactory.getSettings(rawOsType);
    }

    @Override
    public _RawOsType getOsType(String id) {
        IGuestOSType rawOsType = vbMgr.getVBox().getGuestOSType(id);
        return OsTypeFactory.get(rawOsType);
    }

    @Override
    public List<String> listDeviceTypes() {
        List<String> listDeviceTypes = new ArrayList<>();
        for (DeviceType dt : DeviceType.values()) {
            listDeviceTypes.add(dt.toString());
        }
        return listDeviceTypes;
    }

    @Override
    public _RawVM registerMachine(String path) {
        IMachine machine = vbMgr.getVBox().openMachine(path);
        vbMgr.getVBox().registerMachine(machine);
        return getMachine(machine.getId());
    }

    @Override
    public void unregisterMachine(String uuid) {
        VBoxSessionManager.get().unlock(uuid);
        IMachine machine = vbMgr.getVBox().findMachine(uuid);
        machine.unregister(CleanupMode.DetachAllReturnNone);
    }

    @Override
    public List<String> listKeyboardModes() {
        List<String> listKeyboardModes = new ArrayList<>();
        for (KeyboardHIDType type : KeyboardHIDType.values()) {
            if (Mappings.get(type) != null) {
                listKeyboardModes.add(Mappings.get(type).toString());
            }
        }
        return listKeyboardModes;
    }

    @Override
    public List<String> listMouseModes() {
        List<String> listMouseModes = new ArrayList<>();
        for (PointingHIDType type : PointingHIDType.values()) {
            if (Mappings.get(type) != null) {
                listMouseModes.add(Mappings.get(type).toString());
            }
        }
        return listMouseModes;
    }

    @Override
    public List<_RawMedium> listMediums() {
        updateMediumRegistry();

        return new ArrayList<>(mediumRegister.values());
    }

    @Override
    public void deleteMedium(String uuid) {
        try {
            IMedium medium = getRawMedium(uuid);
            IProgress p = medium.deleteStorage();
            // TODO add progress handling
            p.waitForCompletion(-1);
            if (p.getResultCode() != 0) {
                throw new HypervisorException(p.getErrorInfo().getText());
            } else {
                mediumRegister.remove(uuid);
            }
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        }
    }

    @Override
    public List<String> listHardDiskFormats() {
        List<String> formats = new ArrayList<>();
        for (IMediumFormat format : vbMgr.getVBox().getSystemProperties().getMediumFormats()) {
            formats.add(format.getId());
        }
        return formats;
    }

    @Override
    public _RawVM createMachine(String uuid, String name, String osTypeId, boolean applyTemplate) {
        throw new FeatureNotImplementedException();
    }

    @Override
    public String getVersion() {
        if (vbMgr != null) {
            return vbMgr.getVBox().getVersion();
        } else {
            return "Not Connected";
        }
    }

    @Override
    public String getRevision() {
        if (vbMgr != null) {
            return vbMgr.getVBox().getRevision().toString();
        } else {
            return "Not Connected";
        }
    }

    @Override
    public _RawVM createMachine(String name) {
        return createMachine(name, null);
    }

    @Override
    public _RawMedium getToolsMedium() {
        String path = vbMgr.getVBox().getSystemProperties().getDefaultAdditionsISO();
        if (AxStrings.isEmpty(path)) {
            return null;
        } else {
            return getMedium(path, EntityType.DVD);
        }
    }

    @Override
    public boolean hasToolsMedium() {
        return !AxStrings.isEmpty(vbMgr.getVBox().getSystemProperties().getDefaultAdditionsISO());
    }

    @Override
    public void configure(List<_Setting> listIo) {
        for (_Setting setting : listIo) {
            log.debug("Handling setting: " + setting.getName());
            if (setting.getName().equalsIgnoreCase("vbox.global.machineFolder")) {
                log.debug("Setting default Machine Folder to :" + setting.getString());
                vbMgr.getVBox().getSystemProperties().setDefaultMachineFolder(setting.getString());
            }
            if (setting.getName().equalsIgnoreCase("vbox.global.consoleModule")) {
                vbMgr.getVBox().getSystemProperties().setDefaultVRDEExtPack(setting.getString());
            }
            if (setting.getName().equalsIgnoreCase("vbox.global.virtEx")) {
                vbMgr.getVBox().getSystemProperties().setExclusiveHwVirt(setting.getBoolean());
            }
        }
        EventManager.post(new HypervisorConfiguredEvent(this));
    }

    @Override
    public List<_Setting> getSettings() {
        List<_Setting> settings = new ArrayList<>();
        if (isRunning()) {
            settings.add(new StringSetting("vbox.global.machineFolder", vbMgr.getVBox().getSystemProperties().getDefaultMachineFolder()));
            settings.add(new StringSetting("vbox.global.consoleModule", vbMgr.getVBox().getSystemProperties().getDefaultVRDEExtPack()));
            settings.add(new BooleanSetting("vbox.global.virtEx", vbMgr.getVBox().getSystemProperties().getExclusiveHwVirt()));
        }
        return settings;
    }

    @Handler
    public void putServiceStatusEvent(ServiceStateEvent ev) {
        if (ev.getService().equals(evMgrSvc) && ev.getState().equals(ServiceState.Stopped)) {
            stop();
        }
    }

    @Override
    public List<_NetMode> listNetworkModes() {
        return new ArrayList<>(Arrays.asList(VBoxNetMode.values()));
    }

    @Override
    public _NetMode getNetworkMode(String id) {
        return VBoxNetMode.getEnum(id);
    }

    @Override
    public List<_NetAdaptor> listAdaptors(String modeId) {
        List<_NetAdaptor> listInfo = new ArrayList<>();
        VBoxNetMode type = VBoxNetMode.getEnum(modeId);
        switch (type) {
            case Bridged:
                for (IHostNetworkInterface nic : vbMgr.getVBox().getHost().getNetworkInterfaces()) {
                    if (nic.getInterfaceType().equals(HostNetworkInterfaceType.Bridged)) {
                        listInfo.add(new VBoxBridgedAdaptor(nic));
                    }
                }
                break;
            case Generic:
                for (String driver : vbMgr.getVBox().getGenericNetworkDrivers()) {
                    listInfo.add(new VBoxAdaptor(driver, driver, type, true));
                }
                break;
            case HostOnly:
                for (IHostNetworkInterface nic : vbMgr.getVBox().getHost().getNetworkInterfaces()) {
                    if (nic.getInterfaceType().equals(HostNetworkInterfaceType.HostOnly)) {
                        listInfo.add(new VBoxHostOnlyAdaptor(nic));
                    }
                }
                break;
            case Internal:
                for (String internalNet : vbMgr.getVBox().getInternalNetworks()) {
                    listInfo.add(new VBoxAdaptor(internalNet, internalNet, type, true));
                }
                break;
            case NAT:
                break;
            case NATNetwork:
                for (INATNetwork net : VBox.get().getNATNetworks()) {
                    listInfo.add(new VBoxNatNetworkAdaptor(net));
                }
                break;
            default:
                log.warn("Got a valid but non supported net mode: " + modeId);
                throw new InvalidNetworkModeException(modeId);
        }
        return listInfo;
    }

    @Override
    public List<_NetAdaptor> listAdaptors() {
        List<_NetAdaptor> listInfo = new ArrayList<>();
        for (VBoxNetMode mode : VBoxNetMode.values()) {
            listInfo.addAll(listAdaptors(mode.getId()));
        }
        return listInfo;
    }

    @Override
    public _NetAdaptor createAdaptor(String modeId, String name) throws InvalidNetworkModeException {
        VBoxNetMode mode = VBoxNetMode.getEnum(modeId);
        switch (mode) {
            case HostOnly:
                return createHostOnlyAdaptor(name);
            case NATNetwork:
                return createNatNetworkAdaptor(name);
            default:
                if (mode.canAddAdaptor()) {
                    log.warn("Got a valid but non supported net mode: " + modeId);
                }
                throw new InvalidNetworkModeException(modeId, modeId + " does not support adaptor creation");
        }
    }

    public _NetAdaptor createHostOnlyAdaptor(String name) {
        Holder<IHostNetworkInterface> holder = new Holder<>();
        IProgress p = VBox.get().getHost().createHostOnlyNetworkInterface(holder);
        p.waitForCompletion(-1);
        if (p.getResultCode() != 0) {
            throw new HyperboxException("Error creating host only interface: " + p.getErrorInfo().getText());
        }
        _NetAdaptor adaptor = getNetAdaptor(VBoxNetMode.HostOnly.getId(), holder.value.getId());
        EventManager.post(new NetAdaptorAddedEvent(this, adaptor.getMode().getId(), adaptor.getId()));
        return adaptor;
    }

    public _NetAdaptor createNatNetworkAdaptor(String name) {
        VBox.get().createNATNetwork(name);
        _NetAdaptor adaptor = getNetAdaptor(VBoxNetMode.NATNetwork.getId(), name);
        EventManager.post(new NetAdaptorAddedEvent(this, adaptor.getMode().getId(), adaptor.getId()));
        return adaptor;
    }

    @Override
    public void removeAdaptor(String modeId, String adaptorId) throws InvalidNetworkModeException {
        VBoxNetMode mode = VBoxNetMode.getEnum(modeId);
        switch (mode) {
            case HostOnly:
                removeHostOnlyAdaptor(adaptorId);
                break;
            case NATNetwork:
                removeNatNetworkAdaptor(adaptorId);
                break;
            default:
                if (mode.canRemoveAdaptor()) {
                    log.warn("Got a valid but non supported net mode: " + modeId);
                }
                throw new InvalidNetworkModeException(modeId, modeId + " does not support adaptor removal");
        }
    }

    public void removeHostOnlyAdaptor(String adaptorId) {
        log.debug("Removing Host-Only adaptor: " + adaptorId);
        IProgress p = VBox.get().getHost().removeHostOnlyNetworkInterface(adaptorId);
        p.waitForCompletion(-1);
        if (p.getResultCode() != 0) {
            throw new HyperboxException("Error removing host only interface: " + p.getErrorInfo().getText());
        } else {
            EventManager.post(new NetAdaptorRemovedEvent(this, VBoxNetMode.HostOnly.getId(), adaptorId));
        }
    }

    public void removeNatNetworkAdaptor(String name) {
        log.debug("Removing NAT Network: " + name);
        INATNetwork natNet = VBox.get().findNATNetworkByName(name);
        VBox.get().removeNATNetwork(natNet);
        EventManager.post(new NetAdaptorRemovedEvent(this, VBoxNetMode.NATNetwork.getId(), name));
    }

    @Override
    public _NetAdaptor getNetAdaptor(String modeId, String adaptorId) throws NetworkAdaptorNotFoundException {
        VBoxNetMode type = VBoxNetMode.getEnum(modeId);
        IHostNetworkInterface nic;
        switch (type) {
            case Bridged:
                nic = vbMgr.getVBox().getHost().findHostNetworkInterfaceById(adaptorId);
                if (!nic.getInterfaceType().equals(HostNetworkInterfaceType.Bridged)) {
                    throw new HyperboxException("Adaptor of type " + type + " with ID " + adaptorId + " was not found");
                }
                return new VBoxBridgedAdaptor(nic);
            case Generic:
                for (String driver : vbMgr.getVBox().getGenericNetworkDrivers()) {
                    if (driver.equalsIgnoreCase(adaptorId)) {
                        return new VBoxAdaptor(driver, driver, type, true);
                    }
                }
                throw new HyperboxException("Adaptor of type " + type + " with ID " + adaptorId + " was not found");
            case HostOnly:
                nic = vbMgr.getVBox().getHost().findHostNetworkInterfaceById(adaptorId);
                if (!nic.getInterfaceType().equals(HostNetworkInterfaceType.HostOnly)) {
                    throw new HyperboxException("Adaptor of type " + type + " with ID " + adaptorId + " was not found");
                }
                return new VBoxHostOnlyAdaptor(nic);
            case Internal:
                for (String internalNet : vbMgr.getVBox().getInternalNetworks()) {
                    if (internalNet.equalsIgnoreCase(adaptorId)) {
                        return new VBoxAdaptor(internalNet, internalNet, type, true);
                    }
                }
                throw new HyperboxException("Adaptor of type " + type + " with ID " + adaptorId + " was not found");
            case NAT:
                throw new InvalidNetworkModeException(modeId, modeId + " does not support network adaptor");
            case NATNetwork:
                INATNetwork natNet = vbMgr.getVBox().findNATNetworkByName(adaptorId);
                return new VBoxNatNetworkAdaptor(natNet);
            default:
                log.warn("Got a valid but non supported net mode: " + modeId);
                throw new InvalidNetworkModeException(modeId);
        }

    }

    @Override
    public List<String> getLogFileList(String vmId) {
        List<String> ret = new ArrayList<>();
        long i = 0;
        while (!vbMgr.getVBox().findMachine(vmId).queryLogFilename(i).isEmpty()) {
            ret.add(vbMgr.getVBox().findMachine(vmId).queryLogFilename(i));
            i++;
        }

        return ret;
    }

    //FIXME check with vbox devs
    @Override
    public _MachineLogFile getLogFile(String vmId, long logId) {
        byte[] ret;
        StringBuilder log = new StringBuilder();
        long i = 0;
        do {
            ret = vbMgr.getVBox().findMachine(vmId).readLog(logId, i, 65536L);
            log.append(new String(ret));
            i = i + 65536;
        } while (ret.length > 0);
        List<String> logList = Arrays.asList(log.toString().split(System.getProperty("line.separator")));
        return new MachineLogFileIO(vmId, vbMgr.getVBox().findMachine(vmId).queryLogFilename(logId), logList);
    }

    @Override
    public void importAppliance(String applianceFile) {
        // TODO Auto-generated method stub

    }

}
