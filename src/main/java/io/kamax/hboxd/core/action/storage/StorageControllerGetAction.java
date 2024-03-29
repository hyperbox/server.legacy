/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Max Dor
 *
 * https://apps.kamax.io/hyperbox
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

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.in.StorageControllerIn;
import io.kamax.hboxd.comm.io.factory.StorageControllerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.hypervisor.storage._RawStorageController;
import io.kamax.hboxd.session.SessionContext;

import java.util.Arrays;
import java.util.List;

public final class StorageControllerGetAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.StorageControllerGet.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        MachineIn mIn = request.get(MachineIn.class);
        StorageControllerIn scIn = request.get(StorageControllerIn.class);
        _RawStorageController rawSc = hbox.getHypervisor().getMachine(mIn.getUuid()).getStorageController(scIn.getId());
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, StorageControllerIoFactory.get(rawSc)));
    }

}
