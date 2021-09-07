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

import io.kamax.hbox.hypervisor.vbox.net._NetService;
import io.kamax.hboxd.hypervisor._RawItem;

import java.util.List;

public interface _RawNetworkInterface extends _RawItem {

    String getMachineUuid();

    /**
     * From 1, matches the index of Virtualbox
     *
     * @return the index as long
     */
    long getNicId();

    boolean isEnabled();

    void setEnabled(boolean isEnabled);

    String getMacAddress();

    void setMacAddress(String macAddress);

    boolean isCableConnected();

    void setCableConnected(boolean isConnected);

    String getAttachMode();

    void setAttachMode(String attachMode);

    String getAttachName();

    void setAttachName(String attachName);

    String getAdapterType();

    void setAdapterType(String adapterType);

    List<_NetService> getServices();

    void setService(_NetService svc);

    _NetService getService(String serviceTypeId);

}
