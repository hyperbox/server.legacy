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

package io.kamax.hboxd.controller.action;

import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.model._Machine;

/**
 * Utils to extract & manipulate well-known information from Request objects.
 *
 * @author max
 */
// TODO implement all normal & IO objects
public class ActionUtils {

    /**
     * Get the machine referenced from the request message contained into the ClientRequest object.<br/>
     * This method does not validate the presence of the MachineIO.class object)
     *
     * @param core The core to extract the machine from
     * @param req  The Request object where the MachineIO object is referenced
     * @return a _Machine object usable for task
     */
    public static _Machine extractMachine(_Hyperbox core, Request req) {
        return null;
    }

    public static MachineIn extractMachineInput(_Hyperbox core, Request req) {
        return req.get(MachineIn.class);
    }

}
