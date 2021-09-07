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

package io.kamax.hboxd.factory;

import io.kamax.hbox.comm.SecurityAction;
import io.kamax.hbox.comm.SecurityItem;
import io.kamax.hboxd.security.ItemPermission;
import io.kamax.hboxd.security._ItemPermission;

public class ItemPermissionFactory {

    private ItemPermissionFactory() {
        throw new RuntimeException("Not allowed");
    }

    public static _ItemPermission get(String userId, String itemTypeId, String actionId, String itemId, boolean isAllowed) {
        return new ItemPermission(userId, SecurityItem.valueOf(itemTypeId), SecurityAction.valueOf(actionId), itemId, isAllowed);
    }

}
