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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.HyperboxAPI;
import io.kamax.hbox.comm.io.BooleanSettingIO;
import io.kamax.hbox.comm.io.SettingIO;
import io.kamax.hbox.comm.io.StringSettingIO;
import io.kamax.hbox.comm.out.ServerOut;
import io.kamax.hbox.constant.ServerAttribute;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.server._Server;

import java.util.ArrayList;
import java.util.List;

public class ServerIoFactory {

    private ServerIoFactory() {
        // static-only class
    }

    public static ServerOut get(_Server srv) {
        List<SettingIO> settings = new ArrayList<>();
        settings.add(new StringSettingIO(ServerAttribute.Name, srv.getName()));
        settings.add(new StringSettingIO(ServerAttribute.Type, srv.getType().getId()));
        settings.add(new StringSettingIO(ServerAttribute.Version, srv.getVersion()));
        settings.add(new BooleanSettingIO(ServerAttribute.IsHypervisorConnected, srv.isConnected()));
        settings.add(new StringSettingIO(ServerAttribute.NetProtocolVersion, HyperboxAPI.getProtocolVersion().toString()));
        ServerOut srvOut = new ServerOut(srv.getId(), settings);
        return srvOut;
    }

    public static ServerOut get() {
        return get(HBoxServer.get());
    }

}
