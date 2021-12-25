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

package io.kamax.hboxd.persistence.sql;

import io.kamax.hbox.comm.SecurityAction;
import io.kamax.hbox.comm.SecurityItem;
import io.kamax.hboxd.factory.ActionPermissionFactory;
import io.kamax.hboxd.factory.ItemPermissionFactory;
import io.kamax.hboxd.security._ActionPermission;
import io.kamax.hboxd.security._ItemPermission;
import io.kamax.hboxd.security._User;
import io.kamax.tools.helper.sql.EasyPreparedStatement;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermissionSQL {

    public static final String TABLE = "permissions";

    public static final String ITEM_TYPE_ID = "itemTypeId";
    public static final String ACTION_ID = "actionId";
    public static final String ITEM_ID = "itemId";
    public static final String ALLOWED = "allowed";

    public static void init(SqlPersistor sql) throws SQLException {

        createTables(sql);
    }

    public static void createTables(SqlPersistor sql) throws SQLException {

        sql.getConn()
                .createStatement()
                .executeUpdate(
                        "CREATE TABLE IF NOT EXISTS `" + TABLE + "` (`"
                                + UserSQL.ID + "` VARCHAR(255) NULL DEFAULT NULL, `"
                                + ITEM_TYPE_ID + "` VARCHAR(255) NOT NULL,`"
                                + ACTION_ID + "` VARCHAR(255) NOT NULL,`"
                                + ITEM_ID + "` VARCHAR(255) NULL DEFAULT NULL,`"
                                + ALLOWED + "` INT(1) NOT NULL DEFAULT 0,PRIMARY KEY (`" + UserSQL.ID + "`))");
    }

    public static String getActionListQuery() {
        return "SELECT * FROM " + TABLE + " WHERE " + UserSQL.ID + " = ? AND " + ITEM_ID + " IS NULL";
    }

    public static void populateActionListQuery(EasyPreparedStatement stmt, _User usr) throws SQLException {
        stmt.setString(usr.getId());
    }

    public static _ActionPermission extractActionPermission(ResultSet rSet) throws SQLException {
        String userId = rSet.getString(UserSQL.ID);
        String itemTypeId = rSet.getString(ITEM_TYPE_ID);
        String actionId = rSet.getString(ACTION_ID);
        boolean isAllowed = rSet.getBoolean(ALLOWED);

        return ActionPermissionFactory.get(userId, itemTypeId, actionId, isAllowed);
    }

    public static List<_ActionPermission> extractActionPermissions(ResultSet rSet) throws SQLException {
        List<_ActionPermission> permList = new ArrayList<>();
        while (rSet.next()) {
            permList.add(extractActionPermission(rSet));
        }
        return permList;
    }

    public static String getActionSelectQuery() {
        return "SELECT * FROM " + TABLE + " WHERE " +
                UserSQL.ID + " = ? AND " +
                "(" + ITEM_TYPE_ID + " = '" + SecurityItem.Any + "' OR " + ITEM_TYPE_ID + " = ?) AND " +
                "(" + ACTION_ID + "= '" + SecurityItem.Any + "' OR " + SecurityAction.Any + " = ?) AND " + ITEM_ID + " = NULL";
    }

    public static void populateSetQuery(EasyPreparedStatement stmt, _User usr, SecurityItem item, SecurityAction action, Boolean isAllowed)
            throws SQLException {
        populateSetQuery(stmt, usr, item, action, null, isAllowed);
    }

    public static String getItemListQuery() {
        return "SELECT * FROM " + TABLE + " WHERE " + UserSQL.ID + " = ? AND " + ITEM_ID + " IS NOT NULL";
    }

    public static void populateItemListQuery(EasyPreparedStatement stmt, _User usr) throws SQLException {
        stmt.setString(usr.getId());
    }

    public static _ItemPermission extractItemPermission(ResultSet rSet) throws SQLException {
        String userId = rSet.getString(UserSQL.ID);
        String itemTypeId = rSet.getString(ITEM_TYPE_ID);
        String actionId = rSet.getString(ACTION_ID);
        String itemId = rSet.getString(ITEM_ID);
        boolean isAllowed = rSet.getBoolean(ALLOWED);

        return ItemPermissionFactory.get(userId, itemTypeId, actionId, itemId, isAllowed);
    }

    public static List<_ItemPermission> extractItemPermissions(ResultSet rSet) throws SQLException {
        List<_ItemPermission> permList = new ArrayList<>();
        while (rSet.next()) {
            permList.add(extractItemPermission(rSet));
        }
        return permList;
    }

    public static String getItemSelectQuery() {
        return "SELECT * FROM " + TABLE + " WHERE " +
                UserSQL.ID + " = ? AND " +
                "(" + ITEM_TYPE_ID + " = '" + SecurityItem.Any + "' OR " + ITEM_TYPE_ID + " = ?) AND " +
                "(" + ACTION_ID + "= '" + SecurityItem.Any + "' OR " + SecurityAction.Any + " = ?) AND " + ITEM_ID + " = ?";
    }

    public static String getSetQuery() {
        return "MERGE INTO " + TABLE + " (" + UserSQL.ID + ", " + ITEM_TYPE_ID + ", " + ACTION_ID + ", " + ITEM_ID + ", " + ALLOWED
                + ") VALUES (?,?,?,?,?)";
    }

    public static void populateSetQuery(EasyPreparedStatement stmt, _User usr, SecurityItem item, SecurityAction action, String itemId,
                                        Boolean isAllowed)
            throws SQLException {
        stmt.setString(usr.getId());
        stmt.setString(item.toString());
        stmt.setString(action.toString());
        stmt.setString(itemId);
        stmt.setBoolean(isAllowed);
    }

    public static String getUserDeleteQuery() {
        return "DELETE FROM " + TABLE + " WHERE " + UserSQL.ID + " = ?";
    }

    public static void populateUserDeleteQuery(EasyPreparedStatement stmt, _User usr) throws SQLException {
        stmt.setString(usr.getId());
    }

    public static String getActionDeleteQuery() {
        return getUserDeleteQuery() + " AND " + ITEM_TYPE_ID + " = ? AND " + ACTION_ID + " = ? AND " + ITEM_ID + " IS NULL";
    }

    public static void populateActionDeleteQuery(EasyPreparedStatement stmt, _User usr, SecurityItem item, SecurityAction action) throws SQLException {
        populateUserDeleteQuery(stmt, usr);
        stmt.setString(item);
        stmt.setString(action);
    }

    public static String getItemDeleteQuery() {
        return getUserDeleteQuery() + " AND " + ITEM_TYPE_ID + " = ? AND " + ACTION_ID + " = ? AND " + ITEM_ID + " = ?";
    }

    public static void populateItemDeleteQuery(EasyPreparedStatement stmt, _User usr, SecurityItem item, SecurityAction action, String itemId)
            throws SQLException {
        populateUserDeleteQuery(stmt, usr);
        stmt.setString(item);
        stmt.setString(action);
        stmt.setString(itemId);
    }

}
