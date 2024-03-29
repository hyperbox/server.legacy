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

package io.kamax.hboxd.persistence.sql;

import io.kamax.tools.helper.sql.EasyPreparedStatement;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingSQL {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    public static final String TABLE = "settings";

    public static final String NAME = "settingName";
    public static final String VALUE = "settingValue";

    public static void init(SqlPersistor sql) throws SQLException {

        createTables(sql);
    }

    public static void createTables(SqlPersistor sql) throws SQLException {

        sql.getConn()
                .createStatement()
                .executeUpdate(
                        "CREATE TABLE IF NOT EXISTS `" + TABLE + "` (`" + NAME
                                + "` VARCHAR(255) NOT NULL,`" + VALUE + "` VARCHAR(255),PRIMARY KEY (`" + NAME + "`))");
    }

    public static String getSetSettingQuery() {
        return "MERGE INTO " + TABLE + " (" + NAME + "," + VALUE + ") VALUES (?,?)";
    }

    public static void populateSetSettingQuery(EasyPreparedStatement stmt, String name, String value) throws SQLException {
        log.debug("Saving setting " + name + " with value " + value);
        stmt.setString(name);
        stmt.setString(value);
    }

    public static String getLoadSettingQuery() {
        return "SELECT * FROM " + TABLE + " WHERE settingName = ?";
    }

    public static void populateLoadSettingQuery(EasyPreparedStatement stmt, String name) throws SQLException {
        stmt.setString(name);
    }

    public static String extractSetting(ResultSet rSet) throws SQLException {
        return rSet.getString(VALUE);
    }

}
