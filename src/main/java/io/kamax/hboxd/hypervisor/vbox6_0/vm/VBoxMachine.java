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

package io.kamax.hboxd.hypervisor.vbox6_0.vm;

import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.constant.StorageControllerType;
import io.kamax.hbox.data.MachineData;
import io.kamax.hbox.exception.FeatureNotImplementedException;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.exception.MachineException;
import io.kamax.hbox.states.ACPI;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.snapshot.SnapshotRestoredEvent;
import io.kamax.hboxd.hypervisor.perf._RawMetricMachine;
import io.kamax.hboxd.hypervisor.storage._RawStorageController;
import io.kamax.hboxd.hypervisor.vbox6_0.VBox;
import io.kamax.hboxd.hypervisor.vbox6_0.data.Mappings;
import io.kamax.hboxd.hypervisor.vbox6_0.manager.VBoxSessionManager;
import io.kamax.hboxd.hypervisor.vbox6_0.manager.VBoxSettingManager;
import io.kamax.hboxd.hypervisor.vbox6_0.storage.VBoxStorageController;
import io.kamax.hboxd.hypervisor.vbox6_0.vm.guest.VBoxGuest;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.hypervisor.vm.device.*;
import io.kamax.hboxd.hypervisor.vm.guest._RawGuest;
import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;
import io.kamax.tools.logging.KxLog;
import io.kamax.tools.setting._Setting;
import org.slf4j.Logger;
import org.virtualbox_6_0.*;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class VBoxMachine implements _RawVM {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    /**
     * Waiting coefficient to use on ISession::getTimeRemaining() with wait() while waiting for task in progress to finish.<br/>
     * Virtualbox returns a waiting time in seconds, this coefficient allow to turn it into milliseconds and set a 'shorter' waiting time for a more
     * reactive update.<br/>
     * Default value waits half of the estimated time reported by Virtualbox.
     */
    private final long waitingCoef = 500L;

    private final String uuid;

    private final VBoxConsole console;
    private final VBoxCPU cpu;
    private final VBoxDisplay display;
    private final VBoxKeyboard keyboard;
    private final VBoxMemory memory;
    private final VBoxMotherboard motherboard;
    private final VBoxMouse mouse;
    private final VBoxUSB usb;

    private final VBoxGuest guest;

    private ISession session = null;

    public VBoxMachine(String uuid) {
        this.uuid = uuid;

        console = new VBoxConsole(this);
        cpu = new VBoxCPU(this);
        display = new VBoxDisplay(this);
        keyboard = new VBoxKeyboard(this);
        memory = new VBoxMemory(this);
        motherboard = new VBoxMotherboard(this);
        mouse = new VBoxMouse(this);
        usb = new VBoxUSB(this);
        guest = new VBoxGuest(this);
    }

    /**
     * Create a new VirtualboxMachine object with the given UUID and State
     *
     * @param machine The machine to create this object from
     */
    public VBoxMachine(IMachine machine) {
        this(machine.getId());
    }

    @Override
    public void saveChanges() {
        try {
            getRaw().saveSettings();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        }
    }

    @Override
    public void discardChanges() {
        try {
            getRaw().discardSettings();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        }
    }

    private void lock(LockType lockType) {
        session = VBoxSessionManager.get().lock(getUuid(), lockType);
    }

    @Override
    public void lock() {
        if (getRaw().getSessionState().equals(SessionState.Locked)) {
            lock(LockType.Shared);
        } else {
            lock(LockType.Write);
        }
    }

    private void lockAutoWrite() {
        session = VBoxSessionManager.get().lockAuto(getUuid(), LockType.Write);
    }

    private void lockAutoShared() {
        session = VBoxSessionManager.get().lockAuto(getUuid(), LockType.Shared);
    }

    private void lockAuto() {
        session = VBoxSessionManager.get().lockAuto(getUuid());
    }

    @Override
    public void unlock() {
        unlock(true);
    }

    @Override
    public void unlock(boolean saveChanges) {
        VBoxSessionManager.get().unlock(getUuid(), saveChanges);
        session = null;
    }

    private void unlockAuto() {
        unlockAuto(false);
    }

    private void unlockAuto(boolean saveSettings) {
        VBoxSessionManager.get().unlockAuto(getUuid(), saveSettings);
        session = null;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean isAccessible() {
        return getRaw().getAccessible();
    }

    @Override
    public String getName() {
        return getSetting(MachineAttribute.Name).getString();
    }

    @Override
    public MachineStates getState() {
        return Mappings.get(getRaw().getState());
    }

    private IMachine getRaw() {
        session = VBoxSessionManager.get().getLock(uuid);
        if ((session != null) && session.getState().equals(SessionState.Locked)) {
            return session.getMachine();
        } else {
            return VBox.get().findMachine(uuid);
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + getUuid() + ")";
    }

    @Override
    public void powerOn() throws MachineException {

        IMachine rawMachine = getRaw();
        session = VBox.getSession();
        try {
            IProgress p = rawMachine.launchVMProcess(session, "headless", null);
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
            log.debug("VBox API Return code: " + p.getResultCode());
            if (p.getResultCode() != 0) {
                throw new MachineException(p.getErrorInfo().getText());
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            if (session.getState().equals(SessionState.Locked)) {
                session.unlockMachine();
            } else {
                log.debug("PowerOn session is not locked, did powering on failed?");
            }
        }
    }

    @Override
    public void powerOff() throws MachineException {

        try {
            lockAuto();
            IProgress p = session.getConsole().powerDown();
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
            log.debug("VBox API Return code: " + p.getResultCode());
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlock();
        }
    }

    @Override
    public void pause() throws MachineException {

        try {
            lockAuto();
            session.getConsole().pause();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void resume() throws MachineException {

        try {
            lockAuto();
            session.getConsole().resume();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void saveState() throws MachineException {

        try {
            lockAuto();
            IProgress p = session.getMachine().saveState();
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlock();
        }
    }

    @Override
    public void reset() throws MachineException {

        try {
            lockAuto();
            session.getConsole().reset();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void sendAcpi(ACPI acpi) throws MachineException {

        try {
            lockAuto();
            if (acpi.equals(ACPI.PowerButton)) {
                session.getConsole().powerButton();
            } else {
                session.getConsole().sleepButton();
            }
            if (!session.getConsole().getPowerButtonHandled()) {
                log.debug("ACPI Power Button event was not handled by the guest");
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public List<_RawMetricMachine> getMetrics() {

        throw new FeatureNotImplementedException();
    }

    @Override
    public _Setting getSetting(Object getName) {
        return VBoxSettingManager.get(this, getName);
    }

    @Override
    public List<_Setting> listSettings() {
        return VBoxSettingManager.list(this);
    }

    @Override
    public void setSetting(_Setting s) {
        VBoxSettingManager.set(this, Collections.singletonList(s));
    }

    @Override
    public void setSetting(List<_Setting> s) {
        VBoxSettingManager.set(this, s);
    }

    @Override
    public _RawCPU getCpu() {
        return cpu;
    }

    @Override
    public _RawDisplay getDisplay() {
        return display;
    }

    @Override
    public _RawKeyboard getKeyboard() {
        return keyboard;
    }

    @Override
    public _RawMemory getMemory() {
        return memory;
    }

    @Override
    public _RawMotherboard getMotherboard() {
        return motherboard;
    }

    @Override
    public _RawMouse getMouse() {
        return mouse;
    }

    @Override
    public _RawUSB getUsb() {
        return usb;
    }

    @Override
    public Set<_RawNetworkInterface> listNetworkInterfaces() {

        Set<_RawNetworkInterface> nics = new HashSet<>();
        // TODO do this better to avoid endless loop - Check using ISystemProperties maybe?
        long i = 0;

        try {
            while (i < 8) {
                getRaw().getNetworkAdapter(i);
                nics.add(new VBoxNetworkInterface(this, i));
                i++;
            }
        } catch (VBoxException e) {
            throw new HyperboxException("Unable to list NICs", e);
        }

        return nics;

    }

    @Override
    public _RawNetworkInterface getNetworkInterface(long nicId) {
        try {
            // We try to get the interface simply for validation - this class is not "useful" as there are no link back to the VM from it.
            getRaw().getNetworkAdapter(nicId);
            return new VBoxNetworkInterface(this, nicId);
        } catch (VBoxException e) {
            throw new HyperboxException("Couldn't get NIC #" + nicId + " from " + getName() + " because : " + e.getMessage());
        }
    }

    @Override
    public Set<_RawStorageController> listStoroageControllers() {
        Set<_RawStorageController> storageCtrls = new HashSet<>();
        try {
            for (IStorageController vboxStrCtrl : getRaw().getStorageControllers()) {
                storageCtrls.add(new VBoxStorageController(this, vboxStrCtrl));
            }
            return storageCtrls;
        } catch (VBoxException e) {
            throw new HyperboxException(e);
        }
    }

    @Override
    public _RawStorageController getStorageController(String name) {
        return new VBoxStorageController(this, getRaw().getStorageControllerByName(name));
    }

    @Override
    public _RawStorageController addStorageController(String type, String name) {

        StorageBus bus = StorageBus.valueOf(type);

        lockAuto();
        try {
            IStorageController strCtrl = getRaw().addStorageController(name, bus);
            return new VBoxStorageController(this, strCtrl);
        } finally {
            unlockAuto(true);
        }
    }

    @Override
    public _RawStorageController addStorageController(StorageControllerType type, String name) {
        return addStorageController(type.getId(), name);
    }

    @Override
    public void removeStorageController(String name) {
        lockAuto();
        try {
            getRaw().removeStorageController(name);
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public boolean hasSnapshot() {
        return getRaw().getCurrentSnapshot() != null;
    }

    @Override
    public _RawSnapshot getRootSnapshot() {
        if (hasSnapshot()) {
            return getSnapshot(null);
        } else {
            return null;
        }
    }

    @Override
    public _RawSnapshot getSnapshot(String id) {
        try {
            ISnapshot snap = getRaw().findSnapshot(id);
            return new VBoxSnapshot(snap);
        } catch (VBoxException e) {
            throw new HyperboxException(e);
        }
    }

    @Override
    public _RawSnapshot getCurrentSnapshot() {
        try {
            return new VBoxSnapshot(getRaw().getCurrentSnapshot());
        } catch (VBoxException e) {
            throw new HyperboxException(e);
        }
    }

    @Override
    public _RawSnapshot takeSnapshot(String name, String description) {

        lockAuto();
        try {
            Holder<String> snapId = new Holder<>();
            IProgress p = session.getMachine().takeSnapshot(name, description, true, snapId);
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
            log.debug("Return code : " + p.getResultCode());
            if (p.getResultCode() == 0) {
                return getSnapshot(snapId.value);
            } else {
                throw new MachineException(p.getErrorInfo().getText());
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void deleteSnapshot(String id) {

        lockAuto();
        try {
            IProgress p = session.getMachine().deleteSnapshot(id);
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
            log.debug("Return code : " + p.getResultCode());
            if (p.getResultCode() != 0) {
                throw new MachineException(p.getErrorInfo().getText());
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public void restoreSnapshot(String id) {

        lockAuto();
        try {
            ISnapshot snapshot = getRaw().findSnapshot(id);
            IProgress p = session.getMachine().restoreSnapshot(snapshot);
            while (!p.getCompleted() || p.getCanceled()) {
                try {
                    synchronized (this) {
                        wait(Math.abs(Math.min(1, p.getTimeRemaining())) * waitingCoef);
                    }
                } catch (InterruptedException e) {
                    log.warn("Tracing exception", e);
                }
            }
            log.debug("Return code : " + p.getResultCode());
            if (p.getResultCode() == 0) {
                EventManager.post(new SnapshotRestoredEvent(uuid, snapshot.getId()));
            } else {
                throw new MachineException(p.getErrorInfo().getText());
            }
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public _RawConsole getConsole() {
        return console;
    }

    @Override
    public void applyConfiguration(MachineData rawData) {
        lockAuto();
        try {
            VBoxSettingManager.apply(session.getMachine(), rawData);
            saveChanges();
        } catch (VBoxException e) {
            throw new MachineException(e.getMessage(), e);
        } finally {
            unlockAuto();
        }
    }

    @Override
    public String getLocation() {
        File settingFiles = new File(getRaw().getSettingsFilePath());
        return settingFiles.getAbsoluteFile().getParent();
    }

    // TODO provide a screenshot size
    @Override
    public byte[] takeScreenshot() {
        /*
        lockAutoShared();
        try {
           Holder<Long> height = new Holder<Long>();
           Holder<Long> width = new Holder<Long>();
           Holder<Long> bpp = new Holder<Long>();
           Holder<Integer> xOrigin = new Holder<Integer>();
           Holder<Integer> yOrigin = new Holder<Integer>();

           try {
              session.getConsole().getDisplay().getScreenResolution(0l, width, height, bpp, xOrigin, yOrigin);
           } catch (NullPointerException e) {
              throw new MachineDisplayNotAvailableException();
           }
           byte[] screenshot = session.getConsole().getDisplay().takeScreenShotPNGToArray(0l, width.value, height.value);
           return screenshot;
        } catch (VBoxException e) {
           throw new MachineException(e.getMessage(), e);
        } finally {
           unlockAuto();
        }
         */
        throw new FeatureNotImplementedException("Machine screenshot");
    }

    @Override
    public _RawGuest getGuest() {
        return guest;
    }

}
