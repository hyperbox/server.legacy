/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Maxime Dor
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

package io.kamax.hboxd.factory;

import io.kamax.hboxd.core.model.Machine;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.server._Server;

public class MachineFactory {

    private MachineFactory() {
        throw new RuntimeException("Not allowed");

    }

    public static _Machine get(_Server server, _Hypervisor hypervisor, _RawVM rawVm) {
        return new Machine(server, hypervisor, rawVm);
    }

}
