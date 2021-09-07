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

package io.kamax.hboxd.exception.task;

import io.kamax.hbox.exception.HyperboxException;


public class TaskInvalidStateException extends HyperboxException {

    private static final long serialVersionUID = 5406464971643245936L;

    public TaskInvalidStateException() {
        super("Task state is invalid for the requested operation");
    }

    public TaskInvalidStateException(String message) {
        super(message);
    }

    public TaskInvalidStateException(String message, Throwable cause) {
        super(message, cause);
    }

}
