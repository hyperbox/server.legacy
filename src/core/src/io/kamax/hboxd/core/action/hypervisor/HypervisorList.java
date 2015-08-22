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

package io.kamax.hboxd.core.action.hypervisor;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.comm.out.hypervisor.HypervisorLoaderOut;
import io.kamax.hboxd.comm.io.factory.HypervisorIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.session.SessionContext;
import java.util.Arrays;
import java.util.List;

public class HypervisorList extends AbstractHyperboxMultiTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.HypervisorList.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        List<Class<? extends _Hypervisor>> loaders = null;

        if (request.has(ServerIn.class)) {
            ServerIn srvIn = request.get(ServerIn.class);
            loaders = hbox.getServerManager().getServer(srvIn.getId()).listHypervisors();
        } else {
            loaders = hbox.getServerManager().getServer().listHypervisors();
        }

        for (Class<? extends _Hypervisor> loader : loaders) {
            for (HypervisorLoaderOut loaderOut : HypervisorIoFactory.getOut(loader)) {
                SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, loaderOut));
            }
        }
    }

}
