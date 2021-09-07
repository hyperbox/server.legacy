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
import io.kamax.hboxd.Hyperbox;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;


public final class EventManager {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static final _EventManager evMgr;

    static {
        evMgr = Hyperbox.loadClass("core.eventmgr.class", DefaultEventManager.class);
    }

    private EventManager() {
        // not to be used
    }

    public static _EventManager get() {
        return evMgr;
    }

    public static void start() throws HyperboxException {
        get().start();
    }

    public static void stop() {
        get().stop();
    }

    public static void register(Object o) {
        get().register(o);
    }

    public static void unregister(Object o) {
        get().unregister(o);
    }

    public static void post(_Event ev) {
        get().post(ev);
    }

}
