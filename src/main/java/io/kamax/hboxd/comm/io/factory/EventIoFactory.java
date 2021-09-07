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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.UnknownEventOut;
import io.kamax.hbox.event._Event;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.Hyperbox;
import io.kamax.hboxd.comm.io.factory.event._EventIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class EventIoFactory {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static final Map<Enum<?>, _EventIoFactory> factories;

    static {
        factories = new HashMap<>();
        Set<_EventIoFactory> preciseFactories = Hyperbox.loadSubTypes(_EventIoFactory.class);
        for (_EventIoFactory preciseFactory : preciseFactories) {
            for (Enum<?> id : preciseFactory.getHandles()) {
                factories.put(id, preciseFactory);
            }
        }
    }

    private EventIoFactory() {
        // static class, cannot be instantiated
    }

    private static EventOut getUnknown(_Event ev) {
        log.debug("Creating Unknown Event for ID " + ev.getEventId() + " @ " + ev.getTime() + ": " + ev);
        return new UnknownEventOut(ev.getTime(), ev.getEventId(), ServerIoFactory.get(HBoxServer.get()));
    }

    public static EventOut get(_Hyperbox hbox, _Event ev) {
        try {
            if (factories.containsKey(ev.getEventId())) {
                log.debug("Using " + factories.get(ev.getEventId()).getClass().getName() + " for " + ev.getEventId());
                EventOut evOut = factories.get(ev.getEventId()).get(hbox, ev);
                if (evOut != null) {
                    return evOut;
                }
            }
        } catch (Throwable t) {
            log.error("Error while trying to Get EventOutput : " + t.getMessage(), t);
        }

        log.warn("No factory for Event ID " + ev.getEventId() + ", sending " + UnknownEventOut.class.getName() + " instead");
        return getUnknown(ev);
    }
}
