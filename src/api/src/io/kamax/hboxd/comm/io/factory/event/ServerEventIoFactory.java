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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.ServerOut;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.server.ServerConnectionStateEventOut;
import io.kamax.hbox.comm.out.event.server.ServerPropertyChangedEventOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hbox.states.ServerConnectionState;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.server.ServerConnectionStateEvent;
import io.kamax.hboxd.event.server.ServerEvent;
import io.kamax.hboxd.event.server.ServerPropertyChangedEvent;

public class ServerEventIoFactory implements _EventIoFactory {

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[] {
                HyperboxEvents.ServerConnectionState,
                HyperboxEvents.ServerPropertyChanged
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        if (!(ev instanceof ServerEvent)) {
            return null;
        }

        ServerOut srvOut = ServerIoFactory.get();
        switch ((HyperboxEvents) ev.getEventId()) {
            case ServerConnectionState:
                ServerConnectionState state = ((ServerConnectionStateEvent) ev).getState();
                return new ServerConnectionStateEventOut(ev.getTime(), srvOut, state);
            case ServerPropertyChanged:
                Object property = ((ServerPropertyChangedEvent) ev).getProperty();
                Object value = ((ServerPropertyChangedEvent) ev).getValue();
                return new ServerPropertyChangedEventOut(ev.getTime(), srvOut, property, value.toString());
            default:
                return null;
        }
    }

}
