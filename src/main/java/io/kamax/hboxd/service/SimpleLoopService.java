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

package io.kamax.hboxd.service;

import io.kamax.hbox.states.ServiceState;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * <p>
 * Basic implementation of a looping type of service. Default sleep time between loop is 1000ms. Can be changed with setSleepTime().
 * </p>
 * <p>
 * {@link #beforeRunning()}, {@link #beforeLooping()}, {@link #afterLooping()} and {@link #afterRunning()} can be overridden if any init and/or
 * destruct code is needed.
 * </p>
 * TODO javadoc
 *
 * @author max
 */
public abstract class SimpleLoopService extends SkeletonService {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private volatile boolean running;
    private volatile long sleepingTime = 1000;

    @Override
    public final void run() {
        try {
            beforeLooping();
            setState(ServiceState.Running);
            while (running) {
                doLoop();
                if (sleepingTime > 0) {
                    synchronized (this) {
                        try {
                            wait(sleepingTime);
                        } catch (InterruptedException e) {
                            // nothing to do here
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            log.warn(getClass().getSimpleName() + " got shutdown signal, stopping...");
            running = false;
        } finally {
            afterLooping();
            setState(ServiceState.Stopped);
        }
    }

    @Override
    protected final void starting() {
        running = true;
        beforeRunning();
    }

    @Override
    protected final void stopping() {
        afterRunning();
        running = false;
    }

    /**
     * Set the sleep time for the next before the next iteration. Any negative value will set a sleep time of 0.
     *
     * @param sleepingTime a
     */
    protected final void setSleepingTime(long sleepingTime) {
        this.sleepingTime = Math.max(0, sleepingTime);
    }

    /**
     * If initialisation is required OUTSIDE the service thread.<br>
     * This code will run on the main thread.
     */
    protected void beforeRunning() {
        // stub method - left to be implemented if required by subclasses
    }

    /**
     * If destruction is required OUTSIDE the service thread.<br>
     * This code will run on the main thread.
     */
    protected void afterRunning() {
        // stub method - left to be implemented if required by subclasses
    }

    /**
     * If initialisation is required INSIDE the service thread.<br>
     * This code will run on the service thread.
     */
    protected void beforeLooping() {
        // stub method - left to be implemented if required by subclasses
    }

    /**
     * If destruction is required INSIDE the service thread.<br>
     * This code will run on the service thread.
     */
    protected void afterLooping() {
        // stub method - left to be implemented if required by subclasses
    }

    /**
     * The main looping code.
     *
     * @throws InterruptedException When the service is required to stop, {@link Thread#interrupt()} is called on the service thread.<br/>
     *                              This is handled by SimpleLoopService.
     */
    protected abstract void doLoop() throws InterruptedException;

}
