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

package io.kamax.hboxd.hypervisor.vm.snapshot;

import io.kamax.hboxd.hypervisor._RawItem;

import java.util.Date;
import java.util.List;

public interface _RawSnapshot extends _RawItem {

    String getUuid();

    String getMachineId();

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Date getCreationTime();

    boolean isOnline();

    boolean hasParent();

    _RawSnapshot getParent();

    boolean hasChildren();

    List<_RawSnapshot> getChildren();

}
