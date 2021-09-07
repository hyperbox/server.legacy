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

package io.kamax.hboxd.core.action;

import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hboxd.controller._Controller;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class ShutdownAction extends ASingleTaskAction {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static _Controller c;

    public static void setController(_Controller c) {

        if (ShutdownAction.c == null) {
            ShutdownAction.c = c;
        }
    }

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.ServerShutdown.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {

        log.info("Server shutdown requested by " + SecurityContext.getUser().getDomainLogonName());
        c.stop();
    }

}
