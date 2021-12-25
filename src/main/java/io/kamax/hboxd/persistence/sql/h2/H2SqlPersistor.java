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
 *
 * Contributions
 * =============
 * 2014-11-23 - DataSource changes by Adrien Nameche <adrien.nameche@altherian.org>
 */

package io.kamax.hboxd.persistence.sql.h2;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.exception.PersistorException;
import io.kamax.hboxd.persistence.sql.SqlPersistor;
import io.kamax.tools.logging.KxLog;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.SQLException;

public class H2SqlPersistor extends SqlPersistor {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    public static final String CFGKEY_H2_DATA_FOLDER = "core.persistance.h2.data.folder";
    public static final String CFGVAL_H2_DATA_FOLDER = "data";
    public static final String CFGKEY_H2_DATA_FILE = "core.persistance.h2.data.file";
    public static final String CFGVAL_H2_DATA_FILE = "global";
    public static final String CFGKEY_H2_DATA_SEP = "core.persistance.h2.data.sep";
    public static final String CFGVAL_H2_DATA_SEP = "/";
    public static final String CFGKEY_H2_CONNECT_PROTOCOL = "core.persistance.h2.connect.protocol";
    public static final String CFGVAL_H2_CONNECT_PROTOCOL = "jdbc:h2:";
    public static final String CFGKEY_H2_USER = "core.persistance.h2.connect.user";
    public static final String CFGVAL_H2_USER = "hyperbox";
    public static final String CFGKEY_H2_PASS = "core.persistance.h2.connect.pass";
    public static final String CFGVAL_H2_PASS = "hyperbox";

    private Connection conn;

    @Override
    public void init() throws PersistorException {
        // stub
    }

    @Override
    public void destroy() {

        try {
            conn.commit();
        } catch (SQLException e) {
            log.warn("Error when trying to commit the H2 engine connection: " + e.getMessage());
        }

        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error when trying to close the H2 engine connection: " + e.getMessage());
        }

    }

    @Override
    public Connection getConn() {
        try {
            if ((conn == null) || (conn.isClosed()) || (!conn.isValid(3))) {
                String connectString =
                        Configuration.getSetting(CFGKEY_H2_CONNECT_PROTOCOL, CFGVAL_H2_CONNECT_PROTOCOL) +
                                Configuration.getSetting(CFGKEY_H2_DATA_FOLDER, CFGVAL_H2_DATA_FOLDER) +
                                Configuration.getSetting(CFGKEY_H2_DATA_SEP, CFGVAL_H2_DATA_SEP) +
                                Configuration.getSetting(CFGKEY_H2_DATA_FILE, CFGVAL_H2_DATA_FILE);
                String user = Configuration.getSetting(CFGKEY_H2_USER, CFGVAL_H2_USER);
                String pass = Configuration.getSetting(CFGKEY_H2_PASS, CFGVAL_H2_PASS);

                JdbcDataSource ds = new JdbcDataSource();
                ds.setURL(connectString);
                ds.setUser(user);
                ds.setPassword(pass);

                conn = ds.getConnection();
            }
            return conn;
        } catch (SQLException e) {
            throw new HyperboxException("Unable to get H2 connection", e);
        }
    }

}
