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

package io.kamax.hboxd.hypervisor.vbox6_0.setting.network;

import io.kamax.hbox.constant.NetworkInterfaceAttribute;
import io.kamax.hbox.exception.ConfigurationException;
import io.kamax.hbox.hypervisor.vbox.settings.network.NicCableConnectedSetting;
import io.kamax.hboxd.hypervisor.vbox6_0.setting._NetworkInterfaceSettingAction;
import io.kamax.tools.setting.BooleanSetting;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_0.INetworkAdapter;
import org.virtualbox_6_0.LockType;
import org.virtualbox_6_0.VBoxException;

public class NicCableConnectedSettingAction implements _NetworkInterfaceSettingAction {

    @Override
    public LockType getLockType() {
        return LockType.Shared;
    }

    @Override
    public String getSettingName() {
        return NetworkInterfaceAttribute.CableConnected.getId();
    }

    @Override
    public void set(INetworkAdapter nic, _Setting setting) {
        try {
            nic.setCableConnected(((BooleanSetting) setting).getValue());
        } catch (VBoxException e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @Override
    public _Setting get(INetworkAdapter nic) {
        return new NicCableConnectedSetting(nic.getCableConnected());
    }

}
