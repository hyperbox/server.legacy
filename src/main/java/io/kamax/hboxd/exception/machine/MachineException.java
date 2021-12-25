/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Max Dor
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

package io.kamax.hboxd.exception.machine;

import io.kamax.hbox.exception.HyperboxException;


public abstract class MachineException extends HyperboxException {

    private static final long serialVersionUID = -4987939845805356290L;

    public MachineException(String s) {
        super(s);
        // TODO Auto-generated constructor stub
    }

    public MachineException(Throwable t) {
        super(t);
        // TODO Auto-generated constructor stub
    }

    public MachineException(String s, Throwable t) {
        super(s, t);
        // TODO Auto-generated constructor stub
    }

}
