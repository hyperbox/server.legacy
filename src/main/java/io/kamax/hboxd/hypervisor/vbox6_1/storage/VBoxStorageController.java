/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2018 Kamax Sarl
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

package io.kamax.hboxd.hypervisor.vbox6_1.storage;

import io.kamax.hbox.constant.StorageControllerAttribute;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.hypervisor.vbox.settings.storage.ControllerPortCountSetting;
import io.kamax.hbox.hypervisor.vbox.settings.storage.ControllerSubTypeSetting;
import io.kamax.hbox.hypervisor.vbox.utils.InconsistencyUtils;
import io.kamax.hboxd.hypervisor.storage._RawMedium;
import io.kamax.hboxd.hypervisor.storage._RawMediumAttachment;
import io.kamax.hboxd.hypervisor.storage._RawStorageController;
import io.kamax.hboxd.hypervisor.vbox6_1.VBox;
import io.kamax.hboxd.hypervisor.vbox6_1.manager.VBoxSessionManager;
import io.kamax.hboxd.hypervisor.vbox6_1.manager.VBoxSettingManager;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.tools.logging.KxLog;
import io.kamax.tools.setting.PositiveNumberSetting;
import io.kamax.tools.setting.StringSetting;
import io.kamax.tools.setting._Setting;
import io.kamax.vbox.settings.storage.ControllerNameSetting;
import org.slf4j.Logger;
import org.virtualbox_6_1.*;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VBoxStorageController implements _RawStorageController {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private final String machineUuid;
    private String strCtrName;
    private ISession session;

    private void lockAuto() {
        session = VBoxSessionManager.get().lockAuto(machineUuid);
    }

    private void unlockAuto() {
        VBoxSessionManager.get().unlockAuto(machineUuid);
        session = null;
    }

    private IMachine getVm() {
        session = VBoxSessionManager.get().getLock(machineUuid);
        if (session != null) {
            return session.getMachine();
        } else {
            return VBox.get().findMachine(machineUuid);
        }
    }

    public VBoxStorageController(String machineUuid, String controllerName) {
        this.machineUuid = machineUuid;
        strCtrName = controllerName;
    }

    public VBoxStorageController(String machineUuid, IStorageController vStorageController) {
        this(machineUuid, vStorageController.getName());
    }

    public VBoxStorageController(_RawVM machine, IStorageController vStorageController) {
        this(machine.getUuid(), vStorageController.getName());
    }

    public VBoxStorageController(_RawVM machine, String strCtrName) {
        this(machine.getUuid(), strCtrName);
    }

    @Override
    public String getMachineUuid() {
        return machineUuid;
    }

    @Override
    public String getName() {
        return strCtrName;
    }

    @Override
    public void setName(String name) {
        setSetting(new ControllerNameSetting(name));
        strCtrName = name;
    }

    @Override
    public String getType() {
        return getVm().getStorageControllerByName(strCtrName).getBus().toString();
    }

    @Override
    public String getSubType() {
        return ((StringSetting) VBoxSettingManager.get(this, StorageControllerAttribute.SubType)).getValue();
    }

    @Override
    public void setSubType(String subType) {
        setSetting(new ControllerSubTypeSetting(subType));
    }

    @Override
    public long getPortCount() {
        return ((PositiveNumberSetting) VBoxSettingManager.get(this, StorageControllerAttribute.PortCount)).getValue();
    }

    @Override
    public void setPortCount(long portCount) {
        setSetting(new ControllerPortCountSetting(portCount));
    }

    @Override
    public long getMaxPortCount() {
        return getVm().getStorageControllerByName(strCtrName).getMaxPortCount();
    }

    @Override
    public long getMinPortCount() {
        return getVm().getStorageControllerByName(strCtrName).getMinPortCount();
    }

    @Override
    public long getMaxDeviceCount() {
        return getVm().getStorageControllerByName(strCtrName).getMaxDevicesPerPortCount();
    }

    @Override
    public void attachDevice(String devType, long portNb, long deviceNb) {
        // TODO use DeviceTypeMapping
        DeviceType dt = DeviceType.valueOf(devType);

        if (!dt.equals(DeviceType.HardDisk)) {
            lockAuto();
            try {
                getVm().attachDeviceWithoutMedium(getName(), (int) portNb, (int) deviceNb, dt);
            } finally {
                unlockAuto();
            }
        } else {
            log.debug("Not possible to attach an empty HDD drive in Virtualbox, silently ignoring");
        }
    }

    @Override
    public void detachDevice(long portNb, long deviceNb) {
        lockAuto();
        try {
            if (getMediumAttachment(portNb, deviceNb).hasMedium()) {
                detachMedium(portNb, deviceNb);
            }
            getVm().detachDevice(getName(), (int) portNb, (int) deviceNb);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public Set<_RawMedium> listMedium() {
        Set<_RawMedium> setMedium = new HashSet<>();
        for (IMediumAttachment medAttach : getVm().getMediumAttachmentsOfController(strCtrName)) {
            setMedium.add(new VBoxMedium(medAttach.getMedium()));
        }
        return setMedium;
    }

    @Override
    public Set<_RawMediumAttachment> listMediumAttachment() {
        Set<_RawMediumAttachment> setMedium = new HashSet<>();
        for (IMediumAttachment medAttach : getVm().getMediumAttachmentsOfController(strCtrName)) {
            setMedium.add(new VBoxMediumAttachment(machineUuid, medAttach));
        }
        return setMedium;
    }

    // TODO fully implement
    @Override
    public void attachMedium(_RawMedium medium) {
        DeviceType dt = DeviceType.valueOf(medium.getDeviceType());
        IMedium vbMedium = VBox.get().openMedium(medium.getLocation(), dt, AccessMode.ReadOnly, false);
        long maxPortCount = getMaxPortCount();
        long maxDeviceCount = getMaxDeviceCount();

        lockAuto();
        try {
            boolean hasBeenAttached = false;
            for (long portNb = 0; !hasBeenAttached && (portNb <= maxPortCount); portNb++) {
                for (long deviceNb = 0; !hasBeenAttached && (deviceNb <= maxDeviceCount); deviceNb++) {
                    if (!isSlotTaken(portNb, deviceNb)) {
                        getVm().attachDevice(getName(), InconsistencyUtils.getAndTruncate(portNb), InconsistencyUtils.getAndTruncate(deviceNb), dt, vbMedium);
                        hasBeenAttached = true;
                        break;
                    }
                }
            }
            if (!hasBeenAttached) {
                throw new HyperboxException("Could not find a free slot to attach this medium to.");
            }
        } finally {
            unlockAuto();
        }

    }

    @Override
    public void attachMedium(_RawMedium medium, long portNb, long deviceNb) {
        // TODO use DeviceTypeMapping
        DeviceType dt = DeviceType.valueOf(medium.getDeviceType());
        IMedium vbMedium = VBox.get().openMedium(medium.getLocation(), dt, AccessMode.ReadOnly, false);
        Integer portNbInt = InconsistencyUtils.getAndTruncate(portNb);
        Integer deviceNbInt = InconsistencyUtils.getAndTruncate(deviceNb);

        try {
            lockAuto();
            if (isSlotTaken(portNb, deviceNb)) {
                if (dt.equals(DeviceType.DVD) || dt.equals(DeviceType.Floppy)) {
                    getVm().mountMedium(strCtrName, portNbInt, deviceNbInt, vbMedium, true);
                } else {
                    getVm().detachDevice(getName(), portNbInt, deviceNbInt);
                    getVm().attachDevice(getName(), portNbInt, deviceNbInt, dt, vbMedium);
                }
            } else {
                getVm().attachDevice(getName(), portNbInt, deviceNbInt, dt, vbMedium);
            }
            getVm().saveSettings();
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void detachMedium(_RawMedium medium) {
        boolean hasBeenFound = false;

        lockAuto();
        try {
            for (IMediumAttachment medAttach : getVm().getMediumAttachmentsOfController(strCtrName)) {
                if (medium.getUuid().contentEquals(medAttach.getMedium().getId())) {
                    detachMedium(medAttach.getPort(), medAttach.getDevice());
                    hasBeenFound = true;
                    break;
                }
            }
            if (!hasBeenFound) {
                throw new HyperboxException("No such media is attached");
            }
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void detachMedium(long portNb, long deviceNb) {
        if (!isSlotTaken(portNb, deviceNb)) {
            throw new HyperboxException("No media attached to " + portNb + ":" + deviceNb + " on " + getName());
        }
        // TODO handle locked and unlocked media - ask confirmation from the user

        try {
            lockAuto();
            getVm().unmountMedium(getName(), InconsistencyUtils.getAndTruncate(portNb), InconsistencyUtils.getAndTruncate(deviceNb), true);
            getVm().saveSettings();
        } finally {
            unlockAuto();
        }
    }

    @Override
    public boolean isSlotTaken(long portNb, long deviceNb) {
        try {
            for (IMediumAttachment medAttach : getVm().getMediumAttachmentsOfController(strCtrName)) {
                if ((medAttach.getPort() == portNb) && (medAttach.getDevice() == deviceNb)) {
                    return true;
                }
            }
            return false;
        } catch (VBoxException e) {
            throw new HyperboxException(e);
        }
    }

    @Override
    public List<_Setting> listSettings() {
        return VBoxSettingManager.list(this);
    }

    @Override
    public _Setting getSetting(Object getName) {
        return VBoxSettingManager.get(this, getName);
    }

    @Override
    public void setSetting(_Setting s) {
        VBoxSettingManager.set(this, Arrays.asList(s));
    }

    @Override
    public void setSetting(List<_Setting> s) {
        VBoxSettingManager.set(this, s);
    }

    @Override
    public _RawMediumAttachment getMediumAttachment(long portNb, long deviceNb) {
        if (!isSlotTaken(portNb, deviceNb)) {
            return null;
        }

        for (IMediumAttachment medAttach : getVm().getMediumAttachmentsOfController(strCtrName)) {
            if ((medAttach.getPort() == portNb) && (medAttach.getDevice() == deviceNb)) {
                return new VBoxMediumAttachment(machineUuid, medAttach);
            }
        }

        return null;
    }

}
