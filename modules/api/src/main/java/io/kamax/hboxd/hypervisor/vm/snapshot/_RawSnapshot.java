/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * 
 * http://kamax.io/hbox/
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

    public String getUuid();

    public String getMachineId();

    public String getName();

    public void setName(String name);

    public String getDescription();

    public void setDescription(String description);

    public Date getCreationTime();

    public boolean isOnline();

    public boolean hasParent();

    public _RawSnapshot getParent();

    public boolean hasChildren();

    public List<_RawSnapshot> getChildren();

}
