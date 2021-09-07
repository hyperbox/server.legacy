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

package io.kamax.hboxd.hypervisor.storage;

import io.kamax.hboxd.hypervisor._RawItem;

import java.util.Set;

// TODO use _Setting
public interface _RawStorageController extends _RawItem {

    String getMachineUuid();

    String getName();

    void setName(String name);

    /**
     * IDE, SATA, ...
     *
     * @return ID for the type
     */
    String getType();

    /**
     * AHCI, etc
     *
     * @return ID for the precise type
     */
    String getSubType();

    /**
     * AHCI, etc
     *
     * @param subType ID for the precise type
     */
    void setSubType(String subType);

    long getPortCount();

    void setPortCount(long portCount);

    long getMaxPortCount();

    long getMinPortCount();

    long getMaxDeviceCount();

    void attachDevice(String devType, long portNb, long deviceNb);

    void detachDevice(long portNb, long deviceNb);

    Set<_RawMedium> listMedium();

    Set<_RawMediumAttachment> listMediumAttachment();

    _RawMediumAttachment getMediumAttachment(long portNb, long deviceNb);

    void attachMedium(_RawMedium medium);

    void attachMedium(_RawMedium medium, long portNb, long deviceNb);

    void detachMedium(_RawMedium medium);

    /**
     * Will force if the media is locked
     *
     * @param portNb   The port number to use
     * @param deviceNb the device number to use
     */
    void detachMedium(long portNb, long deviceNb);

    boolean isSlotTaken(long portNb, long deviceNb);

}
