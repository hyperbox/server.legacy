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

package io.kamax.hboxd.hypervisor;

import io.kamax.hbox.constant.EntityType;
import io.kamax.hbox.data.MachineData;
import io.kamax.hbox.exception.HypervisorException;
import io.kamax.hbox.exception.net.InvalidNetworkModeException;
import io.kamax.hbox.exception.net.NetworkAdaptorNotFoundException;
import io.kamax.hbox.hypervisor.vbox._MachineLogFile;
import io.kamax.hbox.hypervisor.vbox.net._NetAdaptor;
import io.kamax.hbox.hypervisor.vbox.net._NetMode;
import io.kamax.hboxd.event._EventManager;
import io.kamax.hboxd.hypervisor.host._RawHost;
import io.kamax.hboxd.hypervisor.storage._RawMedium;
import io.kamax.hboxd.hypervisor.storage._RawStorageControllerSubType;
import io.kamax.hboxd.hypervisor.storage._RawStorageControllerType;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.tools.setting._Setting;

import java.util.List;

public interface _Hypervisor {

    String getId();

    String getTypeId();

    String getVendor();

    String getProduct();

    String getVersion();

    String getRevision();

    void start(String options) throws HypervisorException;

    void stop();

    boolean isRunning();

    _RawHost getHost();

    _RawMedium createHardDisk(String filePath, String format, Long logicalSize);

    _RawVM createMachine(String name);

    _RawVM createMachine(String name, String osTypeId);

    _RawVM createMachine(String uuid, String name, String osTypeId);

    _RawVM createMachine(String uuid, String name, String osTypeId, boolean applyTemplate);

    _RawVM getMachine(String id);

    MachineData getMachineSettings(String osTypeId);

    void deleteMachine(String uuid);

    void deleteMedium(String uuid);

    _RawMedium getMedium(String uuid);

    _RawMedium getMedium(String filePath, EntityType mediumType);

    _RawMedium getMedium(String filePath, String mediumType);

    _RawOsType getOsType(String id);

    _RawStorageControllerSubType getStorageControllerSubType(String id);

    _RawStorageControllerType getStorageControllerType(String id);

    List<String> listDeviceTypes();

    List<String> listHardDiskFormats();

    List<String> listKeyboardModes();

    List<_RawVM> listMachines();

    boolean hasToolsMedium();

    /**
     * Get the medium for the hypervisor tools that can be attached to a machine, or null if none exists.
     *
     * @return _RawMedium the medium or null if none exists.
     */
    _RawMedium getToolsMedium();

    List<_RawMedium> listMediums();

    List<String> listMouseModes();

    List<String> listNicAdapterTypes();

    List<String> listNicAttachModes();

    List<String> listNicAttachNames(String attachMode);

    List<_RawOsType> listOsTypes();

    List<_RawStorageControllerSubType> listStorageControllerSubType(String type);

    List<_RawStorageControllerType> listStorageControllerType();

    _RawVM registerMachine(String path);

    void setEventManager(_EventManager evMgr);

    void unregisterMachine(String uuid);

    List<_Setting> getSettings();

    void configure(List<_Setting> listIo);

    /**
     * List all supported network modes for the adaptors
     *
     * @return List of network modes or empty list if none is found
     */
    List<_NetMode> listNetworkModes();

    _NetMode getNetworkMode(String id);

    /**
     * List Network adaptors accessible to the VMs
     *
     * @return List of network adaptors or empty list if none is found
     */
    List<_NetAdaptor> listAdaptors();

    /**
     * List all network adaptors for the given network mode
     *
     * @param modeId Network mode ID to match
     * @return List of network adaptor of the specified network mode, or empty list if none is found
     * @throws InvalidNetworkModeException If the network mode does not exist
     */
    List<_NetAdaptor> listAdaptors(String modeId) throws InvalidNetworkModeException;

    _NetAdaptor createAdaptor(String modeId, String name) throws InvalidNetworkModeException;

    void removeAdaptor(String modeId, String adaptorId) throws InvalidNetworkModeException;

    _NetAdaptor getNetAdaptor(String modId, String adaptorId) throws NetworkAdaptorNotFoundException;

    List<String> getLogFileList(String vmId);

    _MachineLogFile getLogFile(String vmId, long logId);

    void importAppliance(String applianceFile);

}
