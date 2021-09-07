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

package io.kamax.hboxd.event;

import io.kamax.hbox.event._Event;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.tools.logging.KxLog;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public final class DefaultEventManager implements _EventManager, Runnable {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private BlockingQueue<_Event> eventsQueue;
    private MBassador<_Event> eventBus;
    private boolean running = false;
    private Thread worker;

    @Override
    public void start() throws HyperboxException {

        log.debug("Event Manager Starting");
        eventBus = new MBassador<>(BusConfiguration.Default());
        eventBus.addErrorHandler(new IPublicationErrorHandler() {

            @Override
            public void handleError(PublicationError error) {
                log.error("Failed to dispatch event " + error.getPublishedObject(), error.getCause());
            }

        });
        eventsQueue = new LinkedBlockingQueue<>();
        worker = new Thread(this, "EvMgrWT");
        worker.setDaemon(true);
        SecurityContext.addAdminThread(worker);

        worker.start();
        log.debug("Event Manager Started");
    }

    @Override
    public void stop() {

        log.debug("Event Manager Stopping");
        running = false;
        if (worker != null) {
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                log.warn("Tracing exception", e);
            }
        }
        eventsQueue = null;
        log.debug("Event Manager Stopped");
    }

    @Override
    public void register(Object o) {

        eventBus.subscribe(o);
        log.debug(o + " has registered for all events.");
    }

    @Override
    public void unregister(Object o) {

        eventBus.unsubscribe(o);
        log.debug(o + " has unregistered for all events.");
    }

    @Override
    public void post(_Event ev) {

        log.debug("Received Event ID [" + ev.getEventId() + "] fired @ " + ev.getTime());
        if ((eventsQueue != null) && !eventsQueue.offer(ev)) {
            log.error("Event queue is full (" + eventsQueue.size() + "), cannot add " + ev.getEventId());
        }
    }

    @Override
    public void run() {

        log.debug("Event Manager Worker Started");
        running = true;
        while (running) {
            try {
                _Event event = eventsQueue.take();
                log.debug("Processing Event: " + event);
                eventBus.publish(event);
            } catch (InterruptedException e) {
                log.debug("Got interrupted, halting...");
                running = false;
            } catch (Throwable t) {
                log.error("Error when processing event: " + t.getMessage(), t);
            }
        }
        log.debug("Event Manager Worker halted.");
    }

}
