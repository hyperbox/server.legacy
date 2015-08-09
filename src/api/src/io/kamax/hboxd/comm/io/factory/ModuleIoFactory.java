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

import io.kamax.hbox.comm.io.BooleanSettingIO;
import io.kamax.hbox.comm.io.SettingIO;
import io.kamax.hbox.comm.io.StringSettingIO;
import io.kamax.hbox.comm.out.ModuleOut;
import io.kamax.hbox.constant.ModuleAttribute;
import io.kamax.hboxd.module._Module;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModuleIoFactory {

   private ModuleIoFactory() {
      // static class, cannot be instantiated
   }

   public static ModuleOut get(_Module mod) {
      List<SettingIO> settings = new ArrayList<SettingIO>();
      settings.add(new StringSettingIO(ModuleAttribute.DescriptorFile, mod.getDescriptor()));
      settings.add(new StringSettingIO(ModuleAttribute.Name, mod.getName()));
      settings.add(new StringSettingIO(ModuleAttribute.Version, mod.getVersion()));
      settings.add(new BooleanSettingIO(ModuleAttribute.isLoaded, mod.isLoaded()));
      return new ModuleOut(mod.getId(), settings);
   }

   public static List<ModuleOut> get(Collection<_Module> mods) {
      List<ModuleOut> modsOut = new ArrayList<ModuleOut>();
      for (_Module mod : mods) {
         modsOut.add(get(mod));
      }
      return modsOut;
   }

}
