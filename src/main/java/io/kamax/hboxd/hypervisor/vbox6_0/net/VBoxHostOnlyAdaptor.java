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

import io.kamax.hbox.comm.io.NetService_DHCP_IP4_IO;
import io.kamax.hbox.comm.io.NetService_IP4_IO;
import io.kamax.hbox.comm.io.NetService_IP6_IO;
import io.kamax.hbox.constant.NetServiceType;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.hypervisor.vbox.VBoxNetMode;
import io.kamax.hbox.hypervisor.vbox.net.*;
import io.kamax.hboxd.hypervisor.vbox6_0.VBox;
import org.virtualbox_6_0.HostNetworkInterfaceStatus;
import org.virtualbox_6_0.IDHCPServer;
import org.virtualbox_6_0.IHostNetworkInterface;
import org.virtualbox_6_0.VBoxException;

public class VBoxHostOnlyAdaptor extends VBoxAdaptor {

    public VBoxHostOnlyAdaptor(IHostNetworkInterface nic) {
        super(nic.getId(), nic.getName(), VBoxNetMode.HostOnly, nic.getStatus().equals(HostNetworkInterfaceStatus.Up));
    }

    @Override
    protected void process(_NetService service) {
        IHostNetworkInterface nic = VBox.get().getHost().findHostNetworkInterfaceById(getId());
        if (NetServiceType.IPv4.is(service.getType())) {
            _NetService_IP4 ip4Svc = (_NetService_IP4) service;
            if (ip4Svc.isEnabled()) {
                nic.enableStaticIPConfig(ip4Svc.getAddress(), ip4Svc.getMask());
            } else {
                nic.enableStaticIPConfig("", "");
            }
        } else if (NetServiceType.IPv6.is(service.getType())) {
            _NetService_IP6 ip6Svc = (_NetService_IP6) service;
            if (nic.getIPV6Supported()) {
                nic.enableStaticIPConfigV6(ip6Svc.getAddress(), ip6Svc.getMask());
            }
        } else if (NetServiceType.DHCP_IPv4.is(service.getType())) {
            _NetService_IP4_DHCP dhcpSvc = (_NetService_IP4_DHCP) service;
            IDHCPServer dhcpSrv;
            try {
                dhcpSrv = VBox.get().findDHCPServerByNetworkName(nic.getNetworkName());
            } catch (VBoxException e) {
                if (!dhcpSvc.isEnabled()) {
                    return;
                }

                dhcpSrv = VBox.get().createDHCPServer(nic.getNetworkName());
            }
            dhcpSrv.setEnabled(dhcpSvc.isEnabled());
            if (dhcpSvc.isEnabled()) {
                dhcpSrv.setConfiguration(dhcpSvc.getAddress(), dhcpSvc.getMask(), dhcpSvc.getStartAddress(), dhcpSvc.getEndAddress());
            }
        } else {
            throw new HyperboxException("Service type " + service.getType() + " is not supported on " + getMode().getId() + " adaptor");
        }
    }

    @Override
    public _NetService getService(String serviceTypeId) {
        IHostNetworkInterface nic = VBox.get().getHost().findHostNetworkInterfaceById(getId());
        if (NetServiceType.IPv4.is(serviceTypeId)) {
            return new NetService_IP4_IO(true, nic.getIPAddress(), nic.getNetworkMask());
        } else if (NetServiceType.IPv6.is(serviceTypeId)) {
            return new NetService_IP6_IO(nic.getIPV6Supported(), nic.getIPV6Address(), nic.getIPV6NetworkMaskPrefixLength());
        } else if (NetServiceType.DHCP_IPv4.is(serviceTypeId)) {
            IDHCPServer dhcpSrv;
            try {
                dhcpSrv = VBox.get().findDHCPServerByNetworkName(nic.getNetworkName());
            } catch (VBoxException e) {
                return new NetService_DHCP_IP4_IO(false);
            }
            _NetService_IP4_DHCP svc = new NetService_DHCP_IP4_IO(dhcpSrv.getEnabled());
            svc.setAddress(dhcpSrv.getIPAddress());
            svc.setNetmask(dhcpSrv.getNetworkMask());
            svc.setStartAddress(dhcpSrv.getLowerIP());
            svc.setEndAddress(dhcpSrv.getUpperIP());
            return svc;
        } else {
            throw new HyperboxException("Service type " + serviceTypeId + " is not supported on " + getMode().getId() + " adaptor");
        }
    }

}
