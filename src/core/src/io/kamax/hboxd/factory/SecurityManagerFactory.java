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

package io.kamax.hboxd.factory;

import io.kamax.hboxd.security.SecurityManager;
import io.kamax.hboxd.security._SecurityManager;

public class SecurityManagerFactory {

   private static _SecurityManager secMgr;

   private SecurityManagerFactory() {
      throw new RuntimeException("Not allowed");
   }

   public static _SecurityManager get() {

      if (secMgr == null) {
         secMgr = new SecurityManager();
      }
      return secMgr;
   }

}
