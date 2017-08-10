/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * hyperbox at altherian dot org
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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.ServerOut;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.hypervisor.HypervisorConfiguredEventOut;
import io.kamax.hbox.comm.out.event.hypervisor.HypervisorConnectedEventOut;
import io.kamax.hbox.comm.out.event.hypervisor.HypervisorDisconnectedEventOut;
import io.kamax.hbox.comm.out.hypervisor.HypervisorOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hboxd.comm.io.factory.HypervisorIoFactory;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.hypervisor.HypervisorEvent;

public class HypervisorIoEvent implements _EventIoFactory {

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[]{
                HyperboxEvents.HypervisorConfigured,
                HyperboxEvents.HypervisorConnected,
                HyperboxEvents.HypervisorDisconnected
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        if (ev instanceof HypervisorEvent) {
            HypervisorEvent hypEv = (HypervisorEvent) ev;
            ServerOut srvOut = ServerIoFactory.get();
            HypervisorOut hypOut = HypervisorIoFactory.getOut(hypEv.getHypervisor());
            switch ((HyperboxEvents) hypEv.getEventId()) {
                case HypervisorConfigured:
                    return new HypervisorConfiguredEventOut(hypEv.getTime(), srvOut, hypOut);
                case HypervisorConnected:
                    return new HypervisorConnectedEventOut(hypEv.getTime(), srvOut, hypOut);
                case HypervisorDisconnected:
                    return new HypervisorDisconnectedEventOut(hypEv.getTime(), srvOut, hypOut);
                default:
                    return null;
            }
        } else {
            return null;
        }
    }

}
