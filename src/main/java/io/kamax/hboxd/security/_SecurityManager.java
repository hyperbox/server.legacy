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

package io.kamax.hboxd.security;

import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.SecurityAction;
import io.kamax.hbox.comm.SecurityItem;
import io.kamax.hbox.comm.in.UserIn;
import io.kamax.hbox.constant.EntityType;
import io.kamax.hbox.event._Event;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.exception.security.SecurityException;
import io.kamax.hboxd.persistence._SecurityPersistor;

import java.util.List;

public interface _SecurityManager {

    _User init(_SecurityPersistor persistor) throws HyperboxException;

    void start() throws HyperboxException;

    void stop();

    void authenticate(String login, char[] password);

    void authorize(Request req) throws SecurityException;

    boolean isAuthorized(_User u, _Event ev);

    void authorize(SecurityItem item, SecurityAction action);

    boolean isAuthorized(SecurityItem item, SecurityAction action);

    void authorize(SecurityItem item, SecurityAction action, String itemId);

    boolean isAuthorized(SecurityItem item, SecurityAction action, String itemId);

    List<_User> listUsers();

    _User getUser(String usrId);

    _User addUser(UserIn uIn);

    void removeUser(String usrId);

    _User modifyUser(UserIn uIn);

    void setUserPassword(String userId, char[] password);

    void set(_User usr, SecurityItem itemType, SecurityAction action, boolean isAllowed);

    void removePermission(_User usr);

    void remove(_User usr, SecurityItem itemType, SecurityAction action);

    void set(_User usr, SecurityItem itemType, SecurityAction action, String itemId, boolean isAllowed);

    void remove(_User usr, SecurityItem itemType, SecurityAction action, String itemId);

    List<_ActionPermission> listActionPermissions(_User usr);

    List<_ItemPermission> listItemPermissions(_User usr);

    /**
     * List possible permissions for the given entity by using its entity type and ID
     *
     * @param entityTypeId The Entity Type ID to lookup - See {@link EntityType} for default values
     * @param entityId     The Entity ID to lookup
     * @return List of possible Permission IDs
     */
    List<_EntityPermission> listPermission(String entityTypeId, String entityId);

    /**
     * List user permissions for the given entity by using its entity type and ID
     *
     * @param entityTypeId The Entity Type ID - See {@link EntityType} for default values
     * @param entityId     The Entity ID
     * @param usr          The User to lookup the permission for
     * @return List of possible Permission IDs
     */
    List<_UserPermission> listPermission(String entityTypeId, String entityId, _User usr);

}
