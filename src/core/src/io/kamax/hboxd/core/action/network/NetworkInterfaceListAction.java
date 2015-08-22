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

package io.kamax.hboxd.core.action.network;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HypervisorTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.out.network.NetworkInterfaceOut;
import io.kamax.hboxd.comm.io.factory.NetworkInterfaceIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.hypervisor.vm.device._RawNetworkInterface;
import io.kamax.hboxd.session.SessionContext;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class NetworkInterfaceListAction extends AbstractHyperboxMultiTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.NetworkInterfaceList.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        MachineIn mIn = request.get(MachineIn.class);

        _RawVM rawVm = hbox.getHypervisor().getMachine(mIn.getUuid());
        Set<_RawNetworkInterface> rawNics = rawVm.listNetworkInterfaces();

        for (_RawNetworkInterface rawNic : rawNics) {
            NetworkInterfaceOut nicOut = NetworkInterfaceIoFactory.get(rawNic);
            SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, nicOut));
        }
    }

}
