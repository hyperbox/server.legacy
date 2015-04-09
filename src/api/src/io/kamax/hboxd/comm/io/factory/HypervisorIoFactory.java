/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * hyperbox at altherian dot org
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
import io.kamax.hbox.comm.io.StringSettingIO;
import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.comm.out.hypervisor.HypervisorLoaderOut;
import io.kamax.hbox.comm.out.hypervisor.HypervisorOut;
import io.kamax.hbox.constant.HypervisorAttribute;
import io.kamax.hboxd.hypervisor.Hypervisor;
import io.kamax.hboxd.hypervisor._Hypervisor;
import java.util.ArrayList;
import java.util.List;

public class HypervisorIoFactory {

   private HypervisorIoFactory() {
      // static only
   }

   public static HypervisorOut getOut(_Hypervisor hyp) {
      List<SettingIO> settings = new ArrayList<SettingIO>();
      if (hyp.isRunning()) {
         settings.add(new StringSettingIO(HypervisorAttribute.Type, hyp.getTypeId()));
         settings.add(new StringSettingIO(HypervisorAttribute.Vendor, hyp.getVendor()));
         settings.add(new StringSettingIO(HypervisorAttribute.Product, hyp.getProduct()));
         if (hyp.isRunning()) {
            settings.add(new StringSettingIO(HypervisorAttribute.Version, hyp.getVersion()));
            settings.add(new StringSettingIO(HypervisorAttribute.Revision, hyp.getRevision()));
         }
         settings.addAll(SettingIoFactory.getList(hyp.getSettings()));
      }
      return new HypervisorOut(hyp.getId(), settings);
   }

   public static List<HypervisorLoaderOut> getOut(Class<? extends _Hypervisor> loader) {
      List<HypervisorLoaderOut> listOut = new ArrayList<HypervisorLoaderOut>();
      Hypervisor metadata = loader.getAnnotation(Hypervisor.class);
      if (metadata != null) {
         for (String id : metadata.schemes()) {
            listOut.add(new HypervisorLoaderOut(metadata.vendor(), metadata.product(), metadata.typeId(), id));
         }
      }
      return listOut;
   }

}
