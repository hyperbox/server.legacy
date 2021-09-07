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

package io.kamax.hboxd.core.action.guest;

import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;

import java.util.Arrays;
import java.util.List;

public final class GuestRestart extends AbstractHyperboxMultiTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.GuestRestart.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        throw new UnsupportedOperationException();
        // modules.getActionManager().get(Command.HBOX.toString() + HyperboxTasks.GuestShutdown.toString()).run(request, modules);
        // modules.getActionManager().get(Command.VBOX.toString() + VirtualboxTasks.MachinePowerOn.toString()).run(request, modules);
    }

}
