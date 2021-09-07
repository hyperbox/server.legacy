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

package io.kamax.hboxd.hypervisor.vbox6_0.host;

import io.kamax.hboxd.hypervisor.host._RawHost;
import org.virtualbox_6_0.IHost;

public class VBoxHost implements _RawHost {

    private final IHost host;

    public VBoxHost(IHost host) {
        this.host = host;
    }

    @Override
    public String getHostname() {
        return host.getDomainName();
    }

    @Override
    public String getOSName() {
        return host.getOperatingSystem();
    }

    @Override
    public String getOSVersion() {
        return host.getOSVersion();
    }

    @Override
    public long getMemorySize() {
        return host.getMemorySize();
    }

    @Override
    public long getMemoryAvailable() {
        return host.getMemoryAvailable();
    }

}
