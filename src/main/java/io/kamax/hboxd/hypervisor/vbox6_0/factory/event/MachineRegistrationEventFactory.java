/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2018 Kamax Sarl
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

package io.kamax.hboxd.hypervisor.vbox6_0.factory.event;

import io.kamax.hbox.event._Event;
import io.kamax.hboxd.event.machine.MachineRegistrationEvent;
import io.kamax.hboxd.hypervisor.vbox6_0.factory._PreciseEventFactory;
import org.virtualbox_6_0.IEvent;
import org.virtualbox_6_0.IMachineRegisteredEvent;
import org.virtualbox_6_0.VBoxEventType;

public class MachineRegistrationEventFactory implements _PreciseEventFactory {

    @Override
    public VBoxEventType getType() {
        return VBoxEventType.OnMachineRegistered;
    }

    @Override
    public IMachineRegisteredEvent getRaw(IEvent vbEvent) {

        return IMachineRegisteredEvent.queryInterface(vbEvent);
    }

    @Override
    public _Event getEvent(IEvent vbEvent) {

        IMachineRegisteredEvent rawEvent = getRaw(vbEvent);
        _Event event = new MachineRegistrationEvent(rawEvent.getMachineId(), rawEvent.getRegistered());
        return event;
    }

}
