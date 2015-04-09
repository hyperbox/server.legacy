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

package io.kamax.hboxd.core.action.machine;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HypervisorTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.data.Machine;
import io.kamax.hboxd.comm.io.factory.MachineIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.session.SessionContext;
import java.util.Arrays;
import java.util.List;

public final class MachineCreateAction extends ASingleTaskAction {

   @Override
   public List<String> getRegistrations() {
      return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.MachineCreate.getId());
   }

   @Override
   public boolean isQueueable() {
      return true;
   }

   @Override
   public void run(Request request, _Hyperbox hbox) {
      MachineIn mIn = request.get(MachineIn.class);
      String serverId = mIn.getServerId();
      if (request.has(ServerIn.class)) {
         serverId = request.get(ServerIn.class).getId();
      }

      String osTypeId = mIn.hasSetting(MachineAttribute.OsType) ? mIn.getSetting(MachineAttribute.OsType).getString() : null;
      Machine settingTemplate = hbox.getHypervisor().getMachineSettings(osTypeId);
      _RawVM newVm = hbox.getHypervisor().createMachine(mIn.getUuid(), mIn.getName(), osTypeId);
      newVm.applyConfiguration(settingTemplate);

      _Machine vm = hbox.getServer(serverId).getMachine(newVm.getUuid());
      SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, MachineIoFactory.get(vm)));
   }

}
