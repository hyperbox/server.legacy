/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Max Dor
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

package io.kamax.hboxd.security;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.exception.security.SecurityException;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.WeakHashMap;

public class SecurityContext {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private static _SecurityManager secMgr;
    private static Map<Thread, Thread> adminThreads;
    private static _User adminUsr;
    private static final ThreadLocal<_User> userHolder = new ThreadLocal<>();

    public static void init() {

        if (adminThreads != null) {
            throw new SecurityException("SecurityContext is already initialized");
        }

        adminThreads = new WeakHashMap<>();
        adminThreads.put(Thread.currentThread(), Thread.currentThread());

        log.debug("Security Context has been initialized");
    }

    public static void addAdminThread(Thread thread) {

        if (isAdminThread()) {
            adminThreads.put(thread, thread);
        } else {
            throw new SecurityException("Cannot promoted thread: Current thread is not admin: #" + Thread.currentThread().getId() + " - "
                    + Thread.currentThread().getName());
        }
    }

    public static boolean isAdminThread() {
        return (adminThreads != null) && (adminThreads.isEmpty() || adminThreads.values().contains(Thread.currentThread()));
    }

    public static void initSecurityManager(_SecurityManager secMgr) {

        if (SecurityContext.secMgr != null) {
            throw new HyperboxException("Security Manager is already defined, cannot be redefined");
        }
        SecurityContext.secMgr = secMgr;
    }

    public static void setAdminUser(_User u) {

        if (!isAdminThread()) {
            throw new SecurityException("Cannot set admin user: Current thread is not admin: #" + Thread.currentThread().getId() + " - "
                    + Thread.currentThread().getName());
        }

        adminUsr = u;
    }

    public static void setUser(_User u) {
        if ((getUser() == null) || isAdminThread()) {
            userHolder.set(u);
        } else {
            throw new SecurityException();
        }
    }

    public static void setUser(_SecurityManager secMgr, _User u) {
        if (SecurityContext.secMgr == null) {
            throw new SecurityException("Security Manager is not initialized!");
        }
        if (!SecurityContext.secMgr.equals(secMgr)) {
            throw new SecurityException("User can only be set by the original security manager");
        }

        userHolder.set(u);
    }

    public static _User getUser() {
        if (isAdminThread()) {
            return adminUsr;
        } else {
            return userHolder.get();
        }
    }

    public static _SecurityManager get() {
        return secMgr;
    }

    public static void clear() {
        userHolder.set(null);
    }

}
