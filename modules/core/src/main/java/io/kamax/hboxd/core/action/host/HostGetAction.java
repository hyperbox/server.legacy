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

package io.kamax.hboxd.core.action.host;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.comm.out.host.HostOut;
import io.kamax.hboxd.comm.io.factory.HostIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.host._Host;
import io.kamax.hboxd.session.SessionContext;

import java.util.Arrays;
import java.util.List;

public class HostGetAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.HostGet.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        ServerIn srvIn = request.get(ServerIn.class);
        _Host host = hbox.getServer(srvIn.getId()).getHost();
        HostOut hostOut = HostIoFactory.get(host);

        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, hostOut));
    }

}
