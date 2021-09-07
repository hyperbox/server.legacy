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

package io.kamax.hboxd.task;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm._Client;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.states.TaskState;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action._HyperboxAction;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.task.TaskStateEvent;
import io.kamax.hboxd.exception.ActionCanceledException;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.hboxd.security._User;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Date;

public class HyperboxTask implements _Task, _Client {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private final String id;
    private Throwable error;
    private final _HyperboxAction ac;
    private final Request req;
    private final _User user;
    private final _Hyperbox hbox;

    private volatile TaskState state;

    private final Date createTime = new Date();
    private volatile Date queueTime = null;
    private volatile Date startTime = null;
    private volatile Date endTime = null;

    public HyperboxTask(String id, _HyperboxAction ac, Request req, _User user, _Client client, _Hyperbox hbox) {
        this.id = id;
        this.ac = ac;
        this.req = req;
        this.user = user;
        this.hbox = hbox;
        setState(TaskState.Created);
    }

    private void setState(TaskState ts) {
        state = ts;
        EventManager.post(new TaskStateEvent(this, state));
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public _HyperboxAction getAction() {
        return ac;
    }

    @Override
    public Request getRequest() {
        return req;
    }

    @Override
    public _User getUser() {
        return user;
    }

    @Override
    public _ProgressTracker getProgress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isCancelable() {
        return ac.isCancelable();
    }

    @Override
    public void start() {

        if (!state.equals(TaskState.Pending)) {
            throw new IllegalStateException("Task must be Pending to be started.");
        }

        TaskState currentState = getState();
        try {
            startTime = new Date();
            SecurityContext.setUser(user);
            SessionContext.setClient(this);
            setState(TaskState.Running);

            log.debug("Running Task #" + getId() + " for Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]");
            try {
                ac.run(req, hbox);
                log.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " succeeded.");
                currentState = TaskState.Completed;
            } catch (ActionCanceledException e) {
                log.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " was canceled: " + e.getMessage());
                currentState = TaskState.Canceled;
            } catch (HyperboxException e) {
                log.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " failed: " + e.getMessage());
                error = e;
                currentState = TaskState.Failed;
            } catch (Throwable e) {
                error = e;
                log.debug("Request #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + " failed in an unexpected manner");
                log.warn("Tracing exception", e);
                currentState = TaskState.CriticalFailure;
            }
        } catch (Throwable e) {
            error = e;
            log.debug("Hyperbox error when executing #" + req.getExchangeId() + " [" + req.getCommand() + ":" + req.getName() + "]" + ": "
                    + e.getMessage());
            log.warn("Tracing exception", e);
            currentState = TaskState.Failed;
        } finally {
            endTime = new Date();
            setState(currentState);
        }
    }

    @Override
    public void cancel() {
        if (!state.equals(TaskState.Running)) {
            throw new HyperboxException("Task must be running to be canceled");
        }
        ac.cancel();
        setState(TaskState.Canceled);
    }

    @Override
    public void putAnswer(Answer ans) {
        // client.putAnswer(ans);
    }

    @Override
    public String getAddress() {
        return "TaskID #" + getId();
    }

    @Override
    public String toString() {
        return getAddress();
    }

    @Override
    public void post(EventOut evOut) {
        // client.post(evOut);
    }

    @Override
    public void queue() {
        if (!state.equals(TaskState.Created)) {
            throw new IllegalStateException("Task must be Created to be queued.");
        }

        queueTime = new Date();
        setState(TaskState.Pending);
    }

    @Override
    public Date getQueueTime() {
        return queueTime;
    }

    @Override
    public Date getStartTime() {
        return startTime;
    }

    @Override
    public Date getStopTime() {
        return endTime;
    }

    @Override
    public TaskState getState() {
        return state;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public Throwable getError() {
        return error;
    }

}
