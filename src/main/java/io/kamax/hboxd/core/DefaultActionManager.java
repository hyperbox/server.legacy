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

import io.kamax.hbox.comm.Request;
import io.kamax.hbox.exception.HyperboxCommunicationException;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.Hyperbox;
import io.kamax.hboxd.core.action._ActionManager;
import io.kamax.hboxd.core.action._HyperboxAction;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultActionManager implements _ActionManager {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private final Map<String, _HyperboxAction> actions = new HashMap<>();

    @Override
    public void start() throws HyperboxException {
        Set<_HyperboxAction> actionList = Hyperbox.loadSubTypes(_HyperboxAction.class);
        for (_HyperboxAction action : actionList) {
            List<String> mappings = action.getRegistrations();
            if ((mappings == null) || (mappings.size() == 0)) {
                log.warn("Failed to load " + action.getClass().getSimpleName() + " : No provided mappings");
            } else {
                for (String mapping : mappings) {
                    actions.put(mapping, action);
                    log.debug("Loaded " + action.getClass().getSimpleName() + " and mapped under " + mapping);
                }
            }
        }
    }

    @Override
    public _HyperboxAction get(Request req) {
        return get(req.getCommand() + req.getName());
    }

    @Override
    public _HyperboxAction get(String id) {
        if (actions.containsKey(id)) {
            _HyperboxAction ca = actions.get(id);
            log.debug("Found " + ca.getClass().getSimpleName() + " for " + id);
            return ca;
        } else {
            throw new HyperboxCommunicationException("No matching action for " + id);
        }
    }

    @Override
    public void stop() {
        actions.clear();
    }

}
