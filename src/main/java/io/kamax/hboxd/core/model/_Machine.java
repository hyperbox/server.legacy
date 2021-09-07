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

package io.kamax.hboxd.core.model;

import io.kamax.hbox.constant.StorageControllerType;
import io.kamax.hbox.states.ACPI;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;
import io.kamax.hboxd.server._Server;
import io.kamax.tools.setting._Settable;

import java.util.List;
import java.util.Set;

public interface _Machine extends _Settable {

    _Server getServer();

    String getUuid();

    boolean isAccessible();

    String getName();

    MachineStates getState();

    String getLocation();

    void lock();

    void unlock(boolean success);

    void powerOn();

    void powerOff();

    void pause();

    void resume();

    void saveState();

    void reset();

    void sendAcpi(ACPI acpi);

    List<_MachineMetric> getMetrics();

    _CPU getCpu();

    _Display getDisplay();

    _Keyboard getKeyboard();

    _Memory getMemory();

    _Motherboard getMotherboard();

    _Mouse getMouse();

    Set<_NetworkInterface> listNetworkInterfaces();

    _NetworkInterface getNetworkInterface(long nicId);

    Set<_StorageController> listStorageControllers();

    _StorageController getStorageController(String name);

    _StorageController addStorageController(String type, String name);

    _StorageController addStorageController(StorageControllerType type, String name);

    void removeStorageController(String name);

    _USB getUsb();

    _RawSnapshot getSnapshot(String snapshotId);

    _Device getDevice(String deviceId);

}
