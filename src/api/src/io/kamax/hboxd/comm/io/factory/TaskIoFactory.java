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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.out.ExceptionOut;
import io.kamax.hbox.comm.out.TaskOut;
import io.kamax.hbox.comm.out.security.UserOut;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.task._Task;

public class TaskIoFactory {

    private TaskIoFactory() {
        // will not be used
    }

    public static TaskOut get(_Task t) {
        UserOut uOut = UserIoFactory.get(t.getUser());

        ExceptionOut eOut = null;
        if (t.getError() != null) {
            eOut = ExceptionIoFactory.get(t.getError());
        }

        TaskOut tOut = new TaskOut(
                HBoxServer.get().getId(),
                t.getId(),
                t.getRequest().getName(),
                t.getRequest().getExchangeId(),
                t.getState(),
                uOut,
                t.getCreateTime(),
                t.getQueueTime(),
                t.getStartTime(),
                t.getStopTime(),
                eOut);

        return tOut;
    }

}
