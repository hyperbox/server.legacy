/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Maxime Dor
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

package io.kamax.hboxd.core.model;

import io.kamax.hboxd.core.model._Motherboard;
import io.kamax.hboxd.hypervisor.vm.device._RawMotherboard;
import io.kamax.setting._Setting;
import java.util.List;

public class Motherboard implements _Motherboard {

   public Motherboard(_RawMotherboard motherboard) {
      // TODO Auto-generated constructor stub
   }

   @Override
   public List<_Setting> listSettings() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public _Setting getSetting(Object id) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setSetting(_Setting s) {
      // TODO Auto-generated method stub

   }

   @Override
   public void setSetting(List<_Setting> s) {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean isAcpiEnabled() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void setAcpi(boolean isEnabled) {
      // TODO Auto-generated method stub

   }

   @Override
   public boolean isIoApicEnabled() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void setIoApic(boolean isEnabled) {
      // TODO Auto-generated method stub

   }

   @Override
   public String getHardwareUuid() {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public void setHardwareUuid(String uuid) {
      // TODO Auto-generated method stub

   }

}
