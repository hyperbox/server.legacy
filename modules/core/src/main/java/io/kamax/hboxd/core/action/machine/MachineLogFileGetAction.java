/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Maxime Dor
 * <max@altherian.org>
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

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.io.MachineLogFileIO;
import io.kamax.hbox.comm.io.factory.MachineLogFileIoFactory;
import io.kamax.hbox.hypervisor._MachineLogFile;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ServerAction;
import io.kamax.hboxd.server._Server;
import io.kamax.hboxd.session.SessionContext;

import java.util.Arrays;
import java.util.List;

public class MachineLogFileGetAction extends ServerAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.MachineLogFileGet.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    protected void run(Request request, _Hyperbox hbox, _Server srv) {
        MachineLogFileIO logfile = request.get(MachineLogFileIO.class);
        MachineIn machine = request.get(MachineIn.class);

        _MachineLogFile log = srv.getHypervisor().getLogFile(machine.getId(), Long.parseLong(logfile.getId()));
        MachineLogFileIO logIo = MachineLogFileIoFactory.get(log);

        // FIXME send(MachineLogFileIO.class, logIo);
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, MachineLogFileIO.class, logIo));
    }

}
