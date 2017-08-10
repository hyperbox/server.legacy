/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Maxime Dor
 * 
 * http://kamax.io/hbox/
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

package io.kamax.hboxd.core.action.security;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.PermissionIn;
import io.kamax.hbox.comm.in.UserIn;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.security._User;

import java.util.Arrays;
import java.util.List;

public class PermissionSetAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.PermissionSet.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        UserIn usrIn = request.get(UserIn.class);
        PermissionIn permIn = request.get(PermissionIn.class);

        _User usr = hbox.getSecurityManager().getUser(usrIn.getId());
        SecurityItem itemType = SecurityItem.valueOf(permIn.getItemTypeId());
        SecurityAction action = SecurityAction.valueOf(permIn.getActionId());
        hbox.getSecurityManager().set(usr, itemType, action, permIn.getItemId(), permIn.isAllowed());
    }

}
