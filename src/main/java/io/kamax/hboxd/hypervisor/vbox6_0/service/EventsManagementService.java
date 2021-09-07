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

package io.kamax.hboxd.hypervisor.vbox6_0.service;

import io.kamax.hbox.event._Event;
import io.kamax.hbox.hypervisor.vbox.utils.EventBusFactory;
import io.kamax.hboxd.event._EventManager;
import io.kamax.hboxd.hypervisor.vbox6_0.ErrorInterpreter;
import io.kamax.hboxd.hypervisor.vbox6_0.VBox;
import io.kamax.hboxd.hypervisor.vbox6_0.factory.EventFactory;
import io.kamax.hboxd.service.SimpleLoopService;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;
import org.virtualbox_6_0.IEvent;
import org.virtualbox_6_0.IEventListener;
import org.virtualbox_6_0.VBoxEventType;
import org.virtualbox_6_0.VBoxException;

import java.lang.invoke.MethodHandles;
import java.util.Collections;

public final class EventsManagementService extends SimpleLoopService {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private final _EventManager evMgr;
    private IEventListener el;

    public EventsManagementService(_EventManager evMgr) {
        this.evMgr = evMgr;
    }

    @Override
    protected void beforeLooping() {
        setSleepingTime(0);

        el = VBox.get().getEventSource().createListener();
        VBox.get().getEventSource().registerListener(el, Collections.singletonList(VBoxEventType.Any), false);
        log.debug("Virtualbox Event Manager Server started.");
    }

    @Override
    protected void afterLooping() {
        if (el != null) {
            try {
                VBox.get().getEventSource().unregisterListener(el);
            } catch (Throwable t) {
                log.debug("Exception when trying to unregister listener on event source: " + t.getMessage());
            }
            el = null;
        }
        log.debug("Virtualbox Event Manager Server stopped.");
    }

    @Override
    protected void doLoop() {
        try {
            VBox.getManager().waitForEvents(0); // Needed to clear the internal event queue, see https://www.virtualbox.org/ticket/13647
            IEvent rawEvent = VBox.get().getEventSource().getEvent(el, 1000);
            if (rawEvent != null) {
                log.debug("Got an event from Virtualbox: " + rawEvent.getClass().getName() + " - " + rawEvent.getType() + " - " + rawEvent);
                IEvent preciseRawEvent = EventFactory.getRaw(rawEvent);
                if (preciseRawEvent != null) {
                    log.debug("Event was processed to " + preciseRawEvent.getClass().getName() + " - " + preciseRawEvent.getType() + " - "
                            + preciseRawEvent);
                    EventBusFactory.post(preciseRawEvent);
                    _Event ev = EventFactory.get(preciseRawEvent);
                    if (ev != null) {
                        evMgr.post(ev);
                    }
                }
                VBox.get().getEventSource().eventProcessed(el, rawEvent);
            }
        } catch (VBoxException e) {
            throw ErrorInterpreter.transform(e);
        } catch (RuntimeException r) {
            if ((r.getMessage() != null) && r.getMessage().contains("Connection refused")) {
                log.error("Virtualbox broke the connection with us, stopping the service", r);
                stop();
            } else {
                throw r;
            }
        } catch (Throwable t) {
            log.error("Unexpected error occured in the VBox Event Manager", t);
            stop();
        }
    }

    @Override
    public String getId() {
        return "vbox-6.0-EventMgmtSvc";
    }

}
