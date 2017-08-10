/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
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

package io.kamax.hboxd;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.persistence._Persistor;
import io.kamax.hboxd.server._Server;
import io.kamax.tools.logging.Logger;

public class HBoxServer {

    private static _Persistor persistor;
    private static _Server srv;

    public static void initPersistor(_Persistor persistor) {

        if (HBoxServer.persistor == null) {
            HBoxServer.persistor = persistor;
        }
    }

    public static void initServer(_Server srv) {

        HBoxServer.srv = srv;
    }

    public static _Server get() {
        return srv;
    }

    public static String getSetting(String key, String defaultValue) {
        try {
            if (Configuration.hasSetting(key)) {
                return Configuration.getSetting(key, defaultValue);
            } else {
                return persistor.loadSetting(key);
            }
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public static String getSetting(String key) {
        return getSetting(key, null);
    }

    public static boolean hasSetting(String key) {

        try {
            Logger.debug("Checking key \"" + key + "\" in storage");
            return persistor.loadSetting(key) != null;
        } catch (Throwable e) {
            Logger.debug("Key is not in storage, checking in memory or env - Error: " + e.getMessage());
            return Configuration.hasSetting(key);
        }
    }

    public static String getSettingOrFail(String key) {
        if (!hasSetting(key)) {
            throw new HyperboxException("Setting key not found: " + key);
        } else {
            return getSetting(key);
        }
    }

    public static void setSetting(String key, Object value) {

        persistor.storeSetting(key, value.toString());
        Configuration.setSetting(key, value);
    }
}
