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

package io.kamax.hboxd.hypervisor.vbox6_0.manager;

import io.kamax.hbox.hypervisor.vbox.utils.EventBusFactory;
import io.kamax.hboxd.exception.machine.MachineLockingException;
import io.kamax.hboxd.hypervisor.vbox6_0.VBox;
import io.kamax.tools.logging.KxLog;
import net.engio.mbassy.listener.Handler;
import org.slf4j.Logger;
import org.virtualbox_6_0.*;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

public class VBoxSessionManager implements _RawSessionManager {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static final ThreadLocal<VBoxSessionManager> instance = new ThreadLocal<>();
    private final ThreadLocal<Map<String, ISession>> sessions;
    private final ThreadLocal<Map<String, Boolean>> userLocking;

    public static _RawSessionManager get() {
        if (instance.get() == null) {
            instance.set(new VBoxSessionManager());
        }
        return instance.get();
    }

    public VBoxSessionManager() {
        sessions = new ThreadLocal<>();
        sessions.set(new HashMap<>());

        userLocking = new ThreadLocal<>();
        userLocking.set(new HashMap<>());

        EventBusFactory.subscribe(this);
    }

    @Handler
    public void getMachineStateEvent(IMachineStateChangedEvent ev) {

        if (ev.getState().equals(MachineState.Stopping)) {
            unlock(ev.getMachineId(), false);
        }
    }

    @Handler
    public void getMachineSessionStateEvent(ISessionStateChangedEvent ev) {

        if (ev.getState().equals(SessionState.Unlocked) || ev.getState().equals(SessionState.Unlocking)) {
            unlock(ev.getMachineId(), false);
        }
    }

    @Override
    public ISession lock(String uuid, LockType lockType) throws MachineLockingException {
        return lock(uuid, lockType, true);
    }

    private ISession lock(String uuid, LockType lockType, boolean userRequest) throws MachineLockingException {

        try {
            if (sessions.get().containsKey(uuid) && (lockType.equals(LockType.Shared) ||
                    (sessions.get().get(uuid).getState().equals(SessionState.Locked) && sessions.get().get(uuid).getType().equals(SessionType.WriteLock)))) {
                log.debug("Found a session for [" + uuid + "] that fits the locktype [" + lockType + "]");
            } else {
                log.debug("Creating new " + lockType + " lock on " + uuid);
                ISession session = VBox.getSession();
                IMachine roMachine = VBox.get().findMachine(uuid);
                roMachine.lockMachine(session, lockType);
                sessions.get().put(uuid, session);
            }

            if (!userLocking.get().containsKey(uuid) || !userLocking.get().get(uuid)) {
                userLocking.get().put(uuid, userRequest);
            }

            return sessions.get().get(uuid);
        } catch (VBoxException e) {
            throw new MachineLockingException(e.getMessage(), e);
        }
    }

    @Override
    public void unlock(String uuid, boolean saveSettings) {
        unlock(uuid, true, saveSettings);
    }

    @Override
    public void unlock(String uuid) {
        unlock(uuid, true);
    }

    private void unlock(String uuid, boolean userRequest, boolean saveSettings) {

        if (sessions.get().containsKey(uuid) && (userRequest || (userLocking.get().containsKey(uuid) && !userLocking.get().get(uuid)))) {
            log.debug("Found a session for VM #" + uuid + " and the unlock is authorized.");
            try {
                if (sessions.get().get(uuid).getState().equals(SessionState.Locked)) {
                    log.debug("SaveSettings: " + saveSettings);
                    if (saveSettings) {
                        try {
                            sessions.get().get(uuid).getMachine().saveSettings();
                        } catch (VBoxException e) {
                            /*
                             * In case of PowerDown or SaveState, the session lock could be removed between the time we check for the session state
                             * and the save settings, so ignoring this possible error.
                             * In other case, throwing the exception again.
                             */
                            if (!e.getMessage().contains("80070005")) {
                                throw e;
                            }
                        }
                    }
                    sessions.get().get(uuid).unlockMachine();
                    log.debug("VM #" + uuid + " was unlocked");
                } else {
                    log.debug("VM #" + uuid + " wasn't locked, will clean up");
                }
            } catch (VBoxException e) {
                /*
                 * In case of PowerDown or SaveState, the session lock could be removed between the time we check for the session state and the unlock,
                 * so ignoring this possible error.
                 * In other case, throwing the exception again.
                 */
                if (!e.getMessage().toUpperCase().contains("8000FFFF")) {
                    throw e;
                }
            }
            sessions.get().remove(uuid);
            userLocking.get().remove(uuid);
        } else {
            log.debug("Not unlocking " + uuid + ".");
        }
    }

    @Override
    public void unlockAuto(String uuid) {
        unlock(uuid, false, false);
    }

    @Override
    public void unlockAuto(String uuid, boolean saveSettings) {
        unlock(uuid, false, saveSettings);
    }

    @Override
    public ISession getLock(String uuid) {

        return sessions.get().get(uuid);
    }

    @Override
    public IMachine getCurrent(String uuid) {
        if (sessions.get().containsKey(uuid)) {
            ISession session = getLock(uuid);
            if (session.getState().equals(SessionState.Unlocking) || session.getState().equals(SessionState.Unlocked)) {
                unlock(uuid, false);
            } else {
                return session.getMachine();
            }
        }
        return VBox.get().findMachine(uuid);
    }

    @Override
    public ISession lockAuto(String uuid) throws MachineLockingException {
        try {
            return lockAuto(uuid, LockType.Write);
        } catch (MachineLockingException e) {
            log.debug("Failed to create Write lock: " + e.getLocalizedMessage());
            log.debug("Trying Shared lock instead");
            return lockAuto(uuid, LockType.Shared);
        }
    }

    @Override
    public ISession lockAuto(String uuid, LockType lockType) throws MachineLockingException {
        return lock(uuid, lockType, false);
    }

}
