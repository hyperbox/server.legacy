/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Max Dor
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

import io.kamax.hbox.comm.io.NetServiceIO;
import io.kamax.hbox.constant.NetServiceType;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.hypervisor.vbox.net._NetService;

public class NetServiceIoFactory {

    private NetServiceIoFactory() {
        // only static
    }

    public static NetServiceIO get(_NetService svc) {
        return svc.getIO();
    }

    public static _NetService get(NetServiceIO svcIn) {
        if (NetServiceType.IPv4.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.IPv4_NetCIDR.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.IPv6.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.IPv6_Gateway.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.DHCP_IPv4.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.NAT_IPv4.is(svcIn.getType())) {
            return svcIn;
        }

        if (NetServiceType.NAT_IPv6.is(svcIn.getType())) {
            return svcIn;
        }

        throw new HyperboxException(svcIn.getType() + " is not supported for network operations");
    }

}
