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

package io.kamax.hboxd.core.action.snapshot;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.in.SnapshotIn;
import io.kamax.hbox.comm.out.hypervisor.SnapshotOut;
import io.kamax.hboxd.comm.io.factory.SnapshotIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;
import io.kamax.hboxd.session.SessionContext;

import java.util.Arrays;
import java.util.List;

public class SnapshotTakeAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.SnapshotTake.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        MachineIn mIn = request.get(MachineIn.class);
        SnapshotIn snapIn = request.get(SnapshotIn.class);
        _RawSnapshot rawSnap = hbox.getHypervisor().getMachine(mIn.getUuid()).takeSnapshot(snapIn.getName(), snapIn.getDescription());
        SnapshotOut snapOut = SnapshotIoFactory.get(rawSnap);
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, snapOut));
    }

}
