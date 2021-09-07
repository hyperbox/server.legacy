/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2018 Kamax Sarl
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

package io.kamax.hboxd.hypervisor.vbox6_1.vm.guest;

import io.kamax.hboxd.hypervisor.vbox6_1.VBox;
import io.kamax.hboxd.hypervisor.vm.guest._RawGuestNetworkInterface;
import org.virtualbox_6_1.IMachine;

public class VBoxGuestNetworkInterface implements _RawGuestNetworkInterface {

    private final String machineUuid;
    private final int nicId;

    private IMachine getVm() {
        return VBox.get().findMachine(machineUuid);
    }

    private String getProperty(String name) {
        return getVm().getGuestPropertyValue("/VirtualBox/GuestInfo/Net/" + nicId + "/" + name);
    }

    public VBoxGuestNetworkInterface(String machineUuid, int nicId) {
        this.machineUuid = machineUuid;
        this.nicId = nicId;
    }

    @Override
    public int getId() {
        return nicId;
    }

    @Override
    public boolean isUp() {
        return getProperty("Status").equalsIgnoreCase("up");
    }

    @Override
    public String getMacAddress() {
        return getProperty("MAC");
    }

    @Override
    public String getIp4Address() {
        return getProperty("V4/IP");
    }

    @Override
    public String getIp4Subnet() {
        return getProperty("V4/Netmask");
    }

    @Override
    public String getIp6Address() {
        // TODO Auto-generated method stub
        return "";
    }

    @Override
    public String getIp6Subnet() {
        // TODO Auto-generated method stub
        return "";
    }

}
