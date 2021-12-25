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

package io.kamax.hboxd.event.task;

import io.kamax.hbox.comm.out.TaskOut;
import io.kamax.hbox.event.Event;
import io.kamax.hboxd.comm.io.factory.TaskIoFactory;
import io.kamax.hboxd.task._Task;

public abstract class TaskEvent extends Event {

    public TaskEvent(Enum<?> id, _Task t) {
        super(id);
        set(TaskOut.class, TaskIoFactory.get(t));
    }

    public String getTaskId() {
        return getTask().getId();
    }

    public TaskOut getTask() {
        return get(TaskOut.class);
    }

}
