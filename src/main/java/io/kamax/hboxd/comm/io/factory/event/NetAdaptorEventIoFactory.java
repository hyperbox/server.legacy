/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Max Dor
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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.ServerOut;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.net.NetAdaptorAddedEventOut;
import io.kamax.hbox.comm.out.event.net.NetAdaptorRemovedEventOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.net.NetAdaptorEvent;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;


public class NetAdaptorEventIoFactory implements _EventIoFactory {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[]{
                HyperboxEvents.NetAdaptorAdded,
                HyperboxEvents.NetAdaptorRemoved
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        if (!(ev instanceof NetAdaptorEvent)) {
            log.warn("Was given event of type " + ev.getClass().getName());
            return null;
        }

        NetAdaptorEvent adaptEv = (NetAdaptorEvent) ev;
        ServerOut srvOut = ServerIoFactory.get();
        switch ((HyperboxEvents) ev.getEventId()) {
            case NetAdaptorAdded:
                return new NetAdaptorAddedEventOut(ev.getTime(), srvOut, adaptEv.getHypervisor().getId(), adaptEv.getMode(), adaptEv.getAdaptor());
            case NetAdaptorRemoved:
                return new NetAdaptorRemovedEventOut(ev.getTime(), srvOut, adaptEv.getHypervisor().getId(), adaptEv.getMode(), adaptEv.getAdaptor());
            default:
                return null;
        }
    }

}
