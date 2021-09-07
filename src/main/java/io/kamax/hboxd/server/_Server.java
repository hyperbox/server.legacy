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

package io.kamax.hboxd.server;

import io.kamax.hbox.constant.ServerType;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.core.model._Medium;
import io.kamax.hboxd.host._Host;
import io.kamax.hboxd.hypervisor._Hypervisor;

import java.util.List;

/**
 * Represent a server
 *
 * @author max
 */
public interface _Server {

    String CFGKEY_SRV_ID = "server.id";
    String CFGKEY_SRV_NAME = "server.name";
    String CFGKEY_CORE_HYP_ID = "core.hypervisor.id";
    String CFGKEY_CORE_HYP_OPTS = "core.hypervisor.options";
    String CFGKEY_CORE_HYP_AUTO = "core.hypervisor.autoconnect";
    String CFGKEY_CORE_PERSISTOR_CLASS = "core.persistor.class";

    /**
     * Get this server unique ID, by default a randomly generated UUID.<br>
     * This must be unique in an organisation structure.
     *
     * @return The unique ID as String
     */
    String getId();

    /**
     * Get the server defined name, which is by default the hostname of the machine.<br>
     * This does not have to be unique in an organisation/cluster but should be to avoid user mistakes.
     *
     * @return The name as String
     */
    String getName();

    /**
     * Change the server name to another value.
     *
     * @param newName The new name as String
     */
    void setName(String newName);

    /**
     * Get the type of server this is.
     *
     * @return The Type of server as ServerType
     * @see ServerType
     */
    ServerType getType();

    String getVersion();

    void connect(String hypervisorId, String options);

    void disconnect();

    boolean isConnected();

    _Host getHost();

    _Hypervisor getHypervisor();

    List<Class<? extends _Hypervisor>> listHypervisors();

    List<_Machine> listMachines();

    _Machine getMachine(String id);

    _Machine findMachine(String idOrName);

    void unregisterMachine(String id);

    void deleteMachine(String id);

    _Medium createMedium(String location, String format, Long logicalSize);

    _Medium createMedium(String vmId, String filename, String format, Long logicalSize);

    _Medium getMedium(String medId);

    /**
     * Force save all current settings to the persistence layer.
     */
    void save();

}
