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

package io.kamax.hboxd.hypervisor.vm.device;

import io.kamax.hboxd.hypervisor._RawItem;

import java.util.Set;

public interface _RawConsole extends _RawItem {

    Boolean isEnabled();

    void setEnabled(Boolean enable);

    Boolean isActive();

    String getAddress();

    Long getPort();

    String getProtocol();

    String getAuthType();

    void setAuthType(String authType);

    String getAuthLibrary();

    void setAuthLibrary(String library);

    Long getAuthTimeout();

    void setAuthTimeout(Long timeout);

    Boolean getAllowMultiConnection();

    void setAllowMultiConnection(Boolean allow);

    Set<String> listProperties();

    boolean hasProperty(String key);

    String getProperty(String key);

    void setProperty(String key, String value);

    void unsetProperty(String key);

}
