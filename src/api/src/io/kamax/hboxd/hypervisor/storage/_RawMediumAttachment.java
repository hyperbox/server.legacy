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

package io.kamax.hboxd.hypervisor.storage;

import io.kamax.hboxd.hypervisor.vm._RawVM;

public interface _RawMediumAttachment {

   public _RawVM getMachine();

   public boolean hasMedium();

   public _RawMedium getMedium();

   public _RawStorageController getController();

   public long getPortId();

   public long getDeviceId();

   public String getDeviceType();

   public boolean isPassThrough();

}
