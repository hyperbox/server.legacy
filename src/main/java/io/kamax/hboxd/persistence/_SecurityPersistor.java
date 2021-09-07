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

package io.kamax.hboxd.persistence;

import io.kamax.hbox.comm.SecurityAction;
import io.kamax.hbox.comm.SecurityItem;
import io.kamax.hboxd.security._ActionPermission;
import io.kamax.hboxd.security._ItemPermission;
import io.kamax.hboxd.security._User;
import io.kamax.hboxd.security._UserGroup;

import java.util.List;

public interface _SecurityPersistor {

    /*--- User Management ---*/
    List<_User> listUsers();

    _User getUser(String userId);

    void insertUser(_User user);

    void updateUser(_User user);

    void deleteUser(_User user);

    /*--- Password Management ---*/
    byte[] getUserPassword(String userId);

    void setUserPassword(_User user, byte[] password);

    /*--- Group Management ---*/
    List<_UserGroup> listGroups();

    _UserGroup getGroup(String groupId);

    void insertGroup(_UserGroup group);

    void updateGroup(_UserGroup group);

    void deleteGroup(_UserGroup group);

    /*--- Group Membership ---*/
    List<_User> listUsers(_UserGroup group);

    void link(_User user, _UserGroup group);

    void unlink(_User user, _UserGroup group);

    /*-- Permissions Management ---*/
    void insertPermission(_User usr, SecurityItem item, SecurityAction action, boolean isAllowed);

    void insertPermission(_User usr, SecurityItem item, SecurityAction action, String itemId, boolean isAllowed);

    void deletePermission(_User usr);

    void deletePermission(_User usr, SecurityItem item, SecurityAction action);

    void deletePermission(_User usr, SecurityItem item, SecurityAction action, String itemId);

    List<_ActionPermission> listActionPermissions(_User usr);

    List<_ItemPermission> listItemPermissions(_User usr);

    _ActionPermission getPermission(_User usr, SecurityItem item, SecurityAction action);

    _ItemPermission getPermission(_User usr, SecurityItem item, SecurityAction action, String itemId);
}
