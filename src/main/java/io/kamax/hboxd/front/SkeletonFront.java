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

package io.kamax.hboxd.front;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.invoke.MethodHandles;

/**
 * This class will provide a thread management for a Front class, but the front class is responsible to set the proper status for its state.
 *
 * @author max
 */
// TODO implement events
public abstract class SkeletonFront implements _Front, Runnable {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    protected static final int STOPPED = 0;
    protected static final int STARTING = 1;
    protected static final int STARTED = 2;
    protected static final int STOPPING = 3;
    protected static final int CRASHED = 4;

    private _RequestReceiver r;

    private volatile int status = STOPPED;
    private Thread thread;

    @Override
    public void start(_RequestReceiver receiver) throws HyperboxException {

        r = receiver;
        thread = new Thread(this, "Front ID " + getClass().getSimpleName());
        thread.setUncaughtExceptionHandler(new FrontExceptionHandler());
        status = STARTING;
        starting();
        thread.start();
        synchronized (this) {
            while (status == STARTING) {
                try {
                    wait(1000L);
                } catch (InterruptedException e) {
                    throw new HyperboxException("Unable to start " + getClass().getSimpleName() + " because it was interrupted.");
                }
            }
        }
        if (status != STARTED) {
            throw new HyperboxException("An error occured while trying to start " + getClass().getSimpleName());
        }
    }

    @Override
    public void stop() {
        if (status == STARTED) {
            status = STOPPING;
            stopping();
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.trace("Interrupted while waiting", e);
            }
        }
        thread = null;
        status = STOPPED;
    }

    protected void setStatus(int status) {
        this.status = status;
        synchronized (this) {
            this.notifyAll();
        }

    }

    protected void starting() {
        // stub - to be implemented if needed
    }

    protected void stopping() {
        // stub - to be implemented if needed
    }

    protected _RequestReceiver getReceiver() {
        return r;
    }

    private class FrontExceptionHandler implements UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread arg0, Throwable arg1) {
            log.debug("The Front-end \"" + getClass().getSimpleName() + " has crashed.");
            log.debug("Exception caught is : " + arg1.getClass().getSimpleName() + " - " + arg1.getMessage());
            log.debug("The Front-end will not be restarted.");
            status = CRASHED;
        }
    }

}
