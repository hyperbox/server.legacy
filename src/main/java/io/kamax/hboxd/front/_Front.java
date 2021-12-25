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

import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.exception.HyperboxException;

/**
 * This represent a communication protocol system for the server to be able to connect with clients.<br/>
 * Any protocol implementation (e.g. SOAP, XML-RPC, REST, Java RMI) should implement this interface.<br/>
 * Any class implementing this interface will be automatically loaded.
 *
 * @author max
 */
public interface _Front {

    /**
     * Called by the controller to start listening for clients and send any message to the receiver class.
     *
     * @param receiver Receiver to use
     * @throws HyperboxException If something goes wrong
     */
    void start(_RequestReceiver receiver) throws HyperboxException;

    /**
     * Called by the controller when this front-end needs to stop listening and shutdown.
     */
    void stop();

    /**
     * Called by the controller when a event occurred and needs to be sent to all clients to inform about the state change.
     *
     * @param ev The event to send to all clients
     */
    void broadcast(EventOut ev);

}
