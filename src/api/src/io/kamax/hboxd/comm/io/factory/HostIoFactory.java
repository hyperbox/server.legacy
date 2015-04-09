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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.io.SettingIO;
import io.kamax.hbox.comm.out.host.HostOut;
import io.kamax.hbox.constant.HostAttribute;
import io.kamax.hboxd.host._Host;
import java.util.ArrayList;
import java.util.List;

public class HostIoFactory {

   private HostIoFactory() {
      // static class, cannot be instantiated
   }

   public static HostOut get(_Host host) {
      List<SettingIO> sIoList = new ArrayList<SettingIO>();
      sIoList.add(new SettingIO(HostAttribute.Hostname, host.getHostname()));
      sIoList.add(new SettingIO(HostAttribute.OperatingSystemName, host.getOSName()));
      sIoList.add(new SettingIO(HostAttribute.OperatingSystemVersion, host.getOSVersion()));
      sIoList.add(new SettingIO(HostAttribute.MemoryTotal, host.getMemorySize()));
      sIoList.add(new SettingIO(HostAttribute.MemoryAvailable, host.getMemoryAvailable()));
      return new HostOut(sIoList);
   }

}
