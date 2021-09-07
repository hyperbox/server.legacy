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

package io.kamax.hboxd.hypervisor.vbox6_0.factory;

import io.kamax.hbox.event._Event;
import io.kamax.hboxd.Hyperbox;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;
import org.virtualbox_6_0.IEvent;
import org.virtualbox_6_0.VBoxEventType;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EventFactory {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static final Map<VBoxEventType, _PreciseEventFactory> factories;

    static {
        factories = new HashMap<>();

        Set<_PreciseEventFactory> factoriesSet = Hyperbox.loadSubTypes(_PreciseEventFactory.class);
        for (_PreciseEventFactory factory : factoriesSet) {
            factories.put(factory.getType(), factory);
        }
    }

    public static IEvent getRaw(IEvent rawEvent) {
        if (factories.containsKey(rawEvent.getType())) {
            try {
                return factories.get(rawEvent.getType()).getRaw(rawEvent);
            } catch (Throwable t) {
                log.error("Unable to process event: " + t.getMessage());
                return null;
            }
        } else {
            log.debug("Unknown event : " + rawEvent.getType());
            return null;
        }
    }

    public static _Event get(IEvent rawEvent) {
        if (factories.containsKey(rawEvent.getType())) {
            try {
                return factories.get(rawEvent.getType()).getEvent(rawEvent);
            } catch (Throwable t) {
                log.error("Unable to process event: " + t.getMessage());
                return null;
            }
        } else {
            log.debug("Unknown event : " + rawEvent.getType());
            return null;
        }
    }

}
