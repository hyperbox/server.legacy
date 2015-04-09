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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.comm.out.network.NetworkInterfaceOut;
import io.kamax.hboxd.core.model._NetworkInterface;
import io.kamax.hboxd.hypervisor.vm.device._RawNetworkInterface;

public final class NetworkInterfaceIoFactory {

   private NetworkInterfaceIoFactory() {
      // static class - cannot be instantiated
   }

   public static NetworkInterfaceOut get(_NetworkInterface nic) {
      NetworkInterfaceOut nicIo = new NetworkInterfaceOut(nic.getNicId(), SettingIoFactory.getList(nic.getSettings()));
      return nicIo;
   }

   public static NetworkInterfaceOut get(_RawNetworkInterface nic) {
      NetworkInterfaceOut nicIo = new NetworkInterfaceOut(nic.getNicId(), SettingIoFactory.getList(nic.listSettings()));
      return nicIo;
   }

}
