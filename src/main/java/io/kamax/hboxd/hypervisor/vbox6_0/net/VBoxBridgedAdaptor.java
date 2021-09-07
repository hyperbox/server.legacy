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

package io.kamax.hboxd.hypervisor.vbox6_0.net;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.hypervisor.vbox.VBoxNetMode;
import io.kamax.hbox.hypervisor.vbox.net.VBoxAdaptor;
import io.kamax.hbox.hypervisor.vbox.net._NetService;
import org.virtualbox_6_0.HostNetworkInterfaceStatus;
import org.virtualbox_6_0.IHostNetworkInterface;

public class VBoxBridgedAdaptor extends VBoxAdaptor {

    public VBoxBridgedAdaptor(IHostNetworkInterface nic) {
        super(nic.getId(), nic.getName(), VBoxNetMode.Bridged, nic.getStatus().equals(HostNetworkInterfaceStatus.Up));
    }

    @Override
    protected void process(_NetService service) {
        throw new HyperboxException(service.getType() + " is not supported by Bridged adaptor");
    }

}
