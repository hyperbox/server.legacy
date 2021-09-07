/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Max Dor
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

package io.kamax.hboxd.hypervisor.vm;

import io.kamax.hbox.constant.StorageControllerType;
import io.kamax.hbox.data.MachineData;
import io.kamax.hbox.states.ACPI;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.hypervisor._RawItem;
import io.kamax.hboxd.hypervisor.perf._RawMetricMachine;
import io.kamax.hboxd.hypervisor.storage._RawStorageController;
import io.kamax.hboxd.hypervisor.vm.device.*;
import io.kamax.hboxd.hypervisor.vm.guest._RawGuest;
import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;

import java.util.List;
import java.util.Set;

public interface _RawVM extends _RawItem {

    String getUuid();

    boolean isAccessible();

    String getLocation();

    MachineStates getState();

    String getName();

    /**
     * Action must be completed when this call returns
     */
    void powerOn();

    /**
     * Action must be completed when this call returns
     */
    void powerOff();

    /**
     * Action must be completed when this call returns
     */
    void pause();

    /**
     * Action must be completed when this call returns
     */
    void resume();

    /**
     * Action must be completed when this call returns
     */
    void saveState();

    /**
     * Action must be completed when this call returns
     */
    void reset();

    // TODO move to _Motherboard
    void sendAcpi(ACPI acpi);

    List<_RawMetricMachine> getMetrics();

    _RawConsole getConsole();

    _RawCPU getCpu();

    _RawDisplay getDisplay();

    _RawKeyboard getKeyboard();

    _RawMemory getMemory();

    _RawMotherboard getMotherboard();

    _RawMouse getMouse();

    Set<_RawNetworkInterface> listNetworkInterfaces();

    _RawNetworkInterface getNetworkInterface(long nicId);

    Set<_RawStorageController> listStoroageControllers();

    _RawStorageController getStorageController(String name);

    _RawStorageController addStorageController(String type, String name);

    _RawStorageController addStorageController(StorageControllerType type, String name);

    void removeStorageController(String name);

    _RawUSB getUsb();

    boolean hasSnapshot();

    _RawSnapshot getRootSnapshot();

    _RawSnapshot getCurrentSnapshot();

    _RawSnapshot getSnapshot(String id);

    _RawSnapshot takeSnapshot(String name, String description);

    void deleteSnapshot(String id);

    void restoreSnapshot(String id);

    void lock();

    void applyConfiguration(MachineData rawData);

    void saveChanges();

    void discardChanges();

    void unlock();

    void unlock(boolean saveChanges);

    byte[] takeScreenshot();

    _RawGuest getGuest();

}
