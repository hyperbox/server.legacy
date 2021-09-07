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
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.task._ProgressTracker;

import java.util.Set;

public interface _RawMedium extends _RawItem {

    String getUuid();

    void setUuid(String newUuid);

    void generateUuid();

    String getDescription();

    void setDescription(String desc);

    String getState();

    String getVariant();

    String getLocation();

    void setLocation(String path);

    String getName();

    String getDeviceType();

    /**
     * Get size In bytes
     *
     * @return size in bytes
     */
    long getSize();

    String getFormat();

    String getMediumFormat();

    String getType();

    void setType(String type);

    boolean hasParent();

    _RawMedium getParent();

    boolean hasChild();

    Set<_RawMedium> getChild();

    _RawMedium getBase();

    boolean isReadOnly();

    long getLogicalSize();

    boolean isAutoReset();

    String lastAccessError();

    Set<_RawVM> getLinkedMachines();

    void close();

    void refresh();

    _ProgressTracker clone(String path);

    _ProgressTracker clone(_RawMedium toMedium);

    _ProgressTracker clone(String path, String variantType);

    _ProgressTracker clone(_RawMedium toMedium, String variantType);

    _ProgressTracker compact();

    /**
     * @param size in bytes
     * @return progress tracking object
     */
    _ProgressTracker create(long size);

    /**
     * @param size        in bytes
     * @param variantType see Virtualbox MediumVariant
     * @return progress tracking object
     */
    _ProgressTracker create(long size, String variantType);

    /**
     * @param size in bytes
     * @return progress tracking object
     */
    _ProgressTracker resize(long size);

    /**
     * Only for diff storage
     */
    void reset();

}
