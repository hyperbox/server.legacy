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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.system.SystemStateEventOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hbox.states.ServerState;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;

public final class SystemEventIoFactory implements _EventIoFactory {

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[]{
                HyperboxEvents.SystemState
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        if (ev.getEventId() == HyperboxEvents.SystemState) {
            return new SystemStateEventOut(ev.getTime(), ServerIoFactory.get(), ev.get(ServerState.class));
        }
        return null;
    }

}
