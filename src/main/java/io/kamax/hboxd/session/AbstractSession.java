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

package io.kamax.hboxd.session;

import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm._Client;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.states.SessionStates;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.session.SessionStateEvent;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.hboxd.security._User;
import io.kamax.hboxd.task._TaskManager;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractSession implements _Session {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private final String id;
    private final Date createTime;
    private SessionStates state;

    private final _Client client;
    protected _User user;

    private Thread worker;
    private volatile boolean running;
    private final BlockingQueue<Request> msgQueue;

    private final _TaskManager taskMgr;

    public AbstractSession(String id, _Client client, _User user, _TaskManager taskMgr) {

        this.id = id;
        createTime = new Date();

        msgQueue = new LinkedBlockingQueue<>();
        this.client = client;
        this.user = user;
        this.taskMgr = taskMgr;

        init();
        setState(SessionStates.Created);
    }

    private void init() {

        running = true;
        worker = new Thread(this, "SessWT - Session #" + id + " - Connection #" + client.getId());
        worker.start();
    }

    protected void setState(SessionStates state) {
        this.state = state;
        EventManager.post(new SessionStateEvent(this));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public SessionStates getState() {
        return state;
    }

    @Override
    public _User getUser() {
        return user;
    }

    @Override
    public void login() {
        user = SecurityContext.getUser();
    }

    @Override
    public void logout() {
        close();
    }

    @Override
    public void close() {

        running = false;

        if (!Thread.currentThread().equals(worker)) {
            worker.interrupt();
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                log.debug("Session worker thread ID: " + worker.getId());
                log.debug("Thread ID #" + Thread.currentThread().getId() + " [" + Thread.currentThread().getName()
                        + "] forcefully destroyed for Session ID #"
                        + getId());
            }
        }

    }

    @Override
    public void putRequest(Request req) {

        if (!msgQueue.offer(req)) {
            throw new HyperboxException("Message queue is full, try again later.");
        }
    }

    protected void process(Request req) {
        taskMgr.process(req);
    }

    @Override
    public void post(EventOut ev) {

        client.post(ev);
    }

    @Override
    public void run() {

        SecurityContext.setUser(user);
        SessionContext.setClient(client);
        running = true;

        log.debug("Session ID #" + getId() + " | " + getUser().getDomainLogonName() + " | Message Queue Runner started");

        while (running) {
            try {
                Request r = msgQueue.take();
                taskMgr.process(r);
            } catch (InterruptedException e) {
                log.debug("Session ID #" + getId() + " | " + getUser().getDomainLogonName() + " | Message Queue Runner interrupted, halting...");
                running = false;
            } catch (Throwable e) {
                log.error("Fatal error while trying to process a client request", e);
            }
        }
        log.debug("Session ID #" + getId() + " | " + getUser().getDomainLogonName() + " | Message Queue Runner halted");
        setState(SessionStates.Destroyed);
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    protected _Client getClient() {
        return client;
    }

}
