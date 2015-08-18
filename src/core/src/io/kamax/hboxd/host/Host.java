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

package io.kamax.hboxd.host;

import io.kamax.hboxd.hypervisor.host._RawHost;

public class Host implements _Host {

   private _RawHost rawHost;

   public Host(_RawHost rawHost) {
      this.rawHost = rawHost;
   }

   @Override
   public String getHostname() {
      return rawHost.getHostname();
   }

   @Override
   public String getOSName() {
      return rawHost.getOSName();
   }

   @Override
   public String getOSVersion() {
      return rawHost.getOSVersion();
   }

   @Override
   public long getMemorySize() {
      return rawHost.getMemorySize();
   }

   @Override
   public long getMemoryAvailable() {
      return rawHost.getMemoryAvailable();
   }

}
