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
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.states.ACPI;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;
import io.kamax.hboxd.exception.ActionCanceledException;
import io.kamax.hboxd.exception.task.TaskInvalidStateException;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class GuestShutdownAction extends AbstractHyperboxMultiTaskAction {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private Thread runningThread;
    private boolean canceled = false;

    private void init() {
        runningThread = Thread.currentThread();
        canceled = false;
    }

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.GuestShutdown.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @Override
    public void cancel() {
        if (runningThread == null) {
            throw new TaskInvalidStateException("Cannot cancel a non-running task");
        }

        canceled = true;
        runningThread.interrupt();
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        init();
        try {
            MachineIn mIn = request.get(MachineIn.class);
            _RawVM m = hbox.getHypervisor().getMachine(mIn.getUuid());
            while (!m.getState().equals(MachineStates.PoweredOff)) {
                if (canceled) {
                    log.debug("Action has been canceled, interupting");
                    throw new ActionCanceledException();
                }
                if (m.getState().equals(MachineStates.Running)) {
                    m.sendAcpi(ACPI.PowerButton);
                }
                try {
                    synchronized (this) {
                        wait(1000L);
                    }
                } catch (InterruptedException e) {
                    throw new ActionCanceledException();
                }
            }
        } finally {
            runningThread = null;
        }
    }

}
