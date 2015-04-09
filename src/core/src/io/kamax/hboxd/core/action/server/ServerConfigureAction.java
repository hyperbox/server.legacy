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

package io.kamax.hboxd.core.action.server;

import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.comm.io.SettingIO;
import io.kamax.hbox.constant.ServerAttribute;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ServerAction;
import io.kamax.hboxd.server._Server;
import io.kamax.tool.logging.Logger;
import java.util.Arrays;
import java.util.List;

public class ServerConfigureAction extends ServerAction {

   @Override
   public List<String> getRegistrations() {
      return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.ServerConfigure.getId());
   }

   @Override
   public boolean isQueueable() {
      return true;
   }

   @Override
   protected void run(Request request, _Hyperbox hbox, _Server srv) {
      ServerIn srvIn = request.get(ServerIn.class);

      Logger.debug("Available settings :");
      for (SettingIO set : srvIn.listSettings()) {
         Logger.debug(set.getName());
      }
      Logger.debug("--------------------");

      if (srvIn.hasSetting(ServerAttribute.Name)) {
         Logger.debug("Setting new name: " + srvIn.getSetting(ServerAttribute.LogLevel).getString());
         srv.setName(srvIn.getSetting(ServerAttribute.Name).getString());
      }
      if (srvIn.hasSetting(ServerAttribute.LogLevel)) {
         Logger.debug("Setting new log level: " + srvIn.getSetting(ServerAttribute.LogLevel).getString());
         srv.setLogLevel(srvIn.getSetting(ServerAttribute.LogLevel).getString());
      }
      srv.save();
   }

}
