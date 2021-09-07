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

package io.kamax.hboxd.hypervisor.vbox6_1.net;

import io.kamax.hbox.comm.io.*;
import io.kamax.hbox.constant.NetServiceType;
import io.kamax.hbox.hypervisor.vbox.VBoxNetMode;
import io.kamax.hbox.hypervisor.vbox.net.VBoxAdaptor;
import io.kamax.hbox.hypervisor.vbox.net._NetService;
import io.kamax.hboxd.hypervisor.vbox6_1.VBox;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;
import org.virtualbox_6_1.INATNetwork;

import java.lang.invoke.MethodHandles;

public class VBoxNatNetworkAdaptor extends VBoxAdaptor {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    public VBoxNatNetworkAdaptor(INATNetwork natNet) {
        super(natNet.getNetworkName(), natNet.getNetworkName(), VBoxNetMode.NATNetwork, natNet.getEnabled());
    }

    @Override
    public void setLabel(String label) {
        INATNetwork natNet = VBox.get().findNATNetworkByName(getId());
        natNet.setNetworkName(label);
    }

    @Override
    protected void process(_NetService svc) {
        NetServiceType svcType = NetServiceType.valueOf(svc.getType());
        INATNetwork natNet = VBox.get().findNATNetworkByName(getId());
        switch (svcType) {
            case IPv4_NetCIDR:
                natNet.setNetwork(((NetService_IP4_CIDR_IO) svc).getCIDR());
                break;
            case DHCP_IPv4:
                natNet.setNeedDhcpServer(svc.isEnabled());
                break;
            case IPv6:
                natNet.setIPv6Enabled(svc.isEnabled());
                break;
            case IPv6_Gateway:
                natNet.setAdvertiseDefaultIPv6RouteEnabled(svc.isEnabled());
                break;
            case NAT_IPv4:
                log.warn(svcType + " is not implemented for " + getMode().getId());
                break;
            case NAT_IPv6:
                log.warn(svcType + " is not implemented for " + getMode().getId());
                break;
            default:
                throw new IllegalArgumentException("Service type " + svc.getType() + " is not supported on " + getMode().getId() + " adaptor");
        }
    }

    @Override
    public _NetService getService(String serviceTypeId) {
        INATNetwork natNet = VBox.get().findNATNetworkByName(getId());

        if (NetServiceType.IPv4_NetCIDR.is(serviceTypeId)) {
            return new NetService_IP4_CIDR_IO(natNet.getNetwork());
        }

        if (NetServiceType.DHCP_IPv4.is(serviceTypeId)) {
            return new NetService_DHCP_IP4_IO(natNet.getNeedDhcpServer());
        }

        if (NetServiceType.IPv6.is(serviceTypeId)) {
            return new NetService_IP6_IO(natNet.getIPv6Enabled());
        }

        if (NetServiceType.IPv6_Gateway.is(serviceTypeId)) {
            return new NetService_IP6_Gateway_IO(natNet.getAdvertiseDefaultIPv6RouteEnabled());
        }

        if (NetServiceType.NAT_IPv4.is(serviceTypeId)) {
            NetService_NAT_IP4_IO svc = new NetService_NAT_IP4_IO(true);
            for (String ruleRaw : natNet.getPortForwardRules4()) {
                String[] ruleRawSplit = ruleRaw.split(":");
                svc.addRule(new NATRuleIO(ruleRawSplit[0], ruleRawSplit[1], ruleRawSplit[2].replace("[", "").replace("]", ""), ruleRawSplit[3], ruleRawSplit[4]
                        .replace("[", "").replace("]", ""), ruleRawSplit[5]));
            }
            return svc;
        }

        if (NetServiceType.NAT_IPv6.is(serviceTypeId)) {
            NetService_NAT_IP6_IO svc = new NetService_NAT_IP6_IO(true);
            for (String ruleRaw : natNet.getPortForwardRules6()) {
                String[] ruleRawSplit = ruleRaw.split(":");
                svc.addRule(new NATRuleIO(ruleRawSplit[0], ruleRawSplit[1], ruleRawSplit[2].replace("[", "").replace("]", ""), ruleRawSplit[3], ruleRawSplit[4]
                        .replace("[", "").replace("]", ""), ruleRawSplit[5]));
            }
            return svc;
        }

        throw new IllegalArgumentException("Service type " + serviceTypeId + " is not supported on " + getMode().getId() + " adaptor");

    }

}
