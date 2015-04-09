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

package io.kamax.hboxd.core.action.storage;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HypervisorTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.StorageControllerTypeIn;
import io.kamax.hbox.comm.out.storage.StorageControllerTypeOut;
import io.kamax.hbox.exception.HyperboxCommunicationException;
import io.kamax.hboxd.comm.io.factory.StorageControllerTypeIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.hypervisor.storage._RawStorageControllerType;
import io.kamax.hboxd.session.SessionContext;
import java.util.Arrays;
import java.util.List;

public final class StorageControllerTypeGetAction extends ASingleTaskAction {

   @Override
   public List<String> getRegistrations() {
      return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.StorageControllerTypeGet.getId());
   }

   @Override
   public boolean isQueueable() {
      return false;
   }

   @Override
   public void run(Request request, _Hyperbox hbox) {
      if (!request.has(StorageControllerTypeIn.class)) {
         throw new HyperboxCommunicationException(StorageControllerTypeIn.class.getSimpleName() + " is missing");
      }

      StorageControllerTypeIn sctIn = request.get(StorageControllerTypeIn.class);
      _RawStorageControllerType sctType = hbox.getHypervisor().getStorageControllerType(sctIn.getId());
      StorageControllerTypeOut sctOut = StorageControllerTypeIoFactory.get(sctType);
      SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, sctOut));
   }

}
