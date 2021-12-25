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

package io.kamax.hboxd.core.action.hypervisor;

import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.HypervisorIn;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;

import java.util.Arrays;
import java.util.List;

public class HypervisorConnectAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.HypervisorConnect.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        HypervisorIn hypIn = request.get(HypervisorIn.class);
        hbox.getServerManager().getServer().connect(hypIn.getId(), hypIn.getConnectOptions());
    }

}
