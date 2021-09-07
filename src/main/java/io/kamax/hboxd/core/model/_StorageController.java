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

import java.util.Set;

public interface _StorageController extends _Device {

    @Override
    String getId();

    String getMachineUuid();

    String getName();

    void setName(String name);

    /**
     * IDE, SATA, ...
     *
     * @return ID for the type
     */
    String getControllerType();

    /**
     * AHCI, etc
     *
     * @return ID for the precise type
     */
    String getControllerSubType();

    /**
     * AHCI, etc
     *
     * @param subType ID for the precise type
     */
    void setSubType(String subType);

    long getPortCount();

    void setPortCount(long portCount);

    long getMaxPortCount();

    long getMaxDeviceCount();

    void attachDevice(String deviceId, long portNb, long deviceNb);

    void detachDevice(long portNb, long deviceNb);

    Set<_Medium> listMedium();

    Set<_MediumAttachment> listMediumAttachment();

    void attachMedium(_Medium medium);

    void attachMedium(_Medium medium, long portNb, long deviceNb);

    void detachMedium(_Medium medium);

    /**
     * Will force if the media is locked
     *
     * @param portNb   The port number to use
     * @param deviceNb the device number to use
     */
    void detachMedium(long portNb, long deviceNb);

    boolean isSlotTaken(long portNb, long deviceNb);

    _MediumAttachment getMediumAttachment(long portNb, long deviceNb);

}
