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

package io.kamax.hboxd.task;

import io.kamax.hbox.comm.Request;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.core._Hyperbox;

import java.util.List;

public interface _TaskManager {

    void start(_Hyperbox hbox) throws HyperboxException;

    void stop();

    void process(Request req);

    List<_Task> list();

    _Task get(String taskId);

    void remove(String taskId);

}
