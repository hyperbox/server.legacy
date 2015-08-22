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

package io.kamax.hboxd.hypervisor.vm.device;

import io.kamax.hbox.hypervisor.net._NetService;
import io.kamax.hboxd.hypervisor._RawItem;
import java.util.List;

public interface _RawNetworkInterface extends _RawItem {

    public String getMachineUuid();

    /**
     * From 1, matches the index of Virtualbox
     *
     * @return the index as long
     */
    public long getNicId();

    public boolean isEnabled();

    public void setEnabled(boolean isEnabled);

    public String getMacAddress();

    public void setMacAddress(String macAddress);

    public boolean isCableConnected();

    public void setCableConnected(boolean isConnected);

    public String getAttachMode();

    public void setAttachMode(String attachMode);

    public String getAttachName();

    public void setAttachName(String attachName);

    public String getAdapterType();

    public void setAdapterType(String adapterType);

    public List<_NetService> getServices();

    public void setService(_NetService svc);

    public _NetService getService(String serviceTypeId);

}
