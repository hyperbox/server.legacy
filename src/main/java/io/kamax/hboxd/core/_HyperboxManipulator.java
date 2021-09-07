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

package io.kamax.hboxd.core;

import io.kamax.hboxd.core.action._ActionManager;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.persistence._Persistor;
import io.kamax.hboxd.security._SecurityManager;
import io.kamax.hboxd.server._ServerManager;
import io.kamax.hboxd.session._SessionManager;
import io.kamax.hboxd.store._StoreManager;
import io.kamax.hboxd.task._TaskManager;

public interface _HyperboxManipulator {

    _ServerManager getServerManager();

    _TaskManager getTaskManager();

    _SessionManager getSessionManager();

    _SecurityManager getSecurityManager();

    _ActionManager getActionManager();

    _StoreManager getStoreManager();

    _Persistor getPersistor();

    _Hypervisor getHypervisor();

}
