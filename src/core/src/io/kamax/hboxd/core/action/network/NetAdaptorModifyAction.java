/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Maxime Dor
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

package io.kamax.hboxd.core.action.network;

import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HypervisorTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.NetAdaptorIn;
import io.kamax.hbox.comm.io.NetServiceIO;
import io.kamax.hbox.hypervisor.net._NetAdaptor;
import io.kamax.hbox.hypervisor.net._NetService;
import io.kamax.hboxd.comm.io.factory.NetServiceIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ServerAction;
import io.kamax.hboxd.server._Server;
import java.util.Arrays;
import java.util.List;

public class NetAdaptorModifyAction extends ServerAction {

   @Override
   public List<String> getRegistrations() {
      return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.NetAdaptorModify.getId());
   }

   @Override
   public boolean isQueueable() {
      return true;
   }

   @Override
   protected void run(Request request, _Hyperbox hbox, _Server srv) {
      NetAdaptorIn adaptIn = request.get(NetAdaptorIn.class);
      _NetAdaptor adapt = srv.getHypervisor().getNetAdaptor(adaptIn.getModeId(), adaptIn.getId());
      if (adapt.getMode().canRenameAdaptor()) {
         adapt.setLabel(adaptIn.getLabel());
      }
      if (!adaptIn.getServices().isEmpty()) {
         for (NetServiceIO svcIn : adaptIn.getServices()) {
            _NetService svc = NetServiceIoFactory.get(svcIn);
            adapt.setService(svc);
         }
      }
   }

}
