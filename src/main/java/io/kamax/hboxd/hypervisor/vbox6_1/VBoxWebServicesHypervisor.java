/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 - Max Dor
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

package io.kamax.hboxd.hypervisor.vbox6_1;

import io.kamax.hbox.exception.HypervisorException;
import io.kamax.hbox.hypervisor.vbox.VirtualBox;
import io.kamax.hbox.hypervisor.vbox._VBoxWebSrv;
import io.kamax.hboxd.hypervisor.Hypervisor;
import io.kamax.hboxd.hypervisor.vbox.VBoxWSOptions;
import io.kamax.hboxd.hypervisor.vbox.VBoxWebSrv;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;
import org.virtualbox_6_1.VBoxException;
import org.virtualbox_6_1.VirtualBoxManager;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;

@Hypervisor(
        id = VirtualBox.ID.WS_6_1,
        vendor = VirtualBox.VENDOR,
        product = VirtualBox.PRODUCT,
        version = VirtualBox.Version.v6_1,
        typeId = VirtualBox.Type.WEB_SERVICES)
public class VBoxWebServicesHypervisor extends VBoxHypervisor {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private _VBoxWebSrv webSrv;

    @Override
    protected VirtualBoxManager connect(String rawOptions) {
        log.debug("Using Web Services");
        try {
            VBoxWSOptions options = new VBoxWSOptions(rawOptions);
            if (!options.hasOptions()) {
                if (webSrv == null) {
                    webSrv = new VBoxWebSrv(options.getHost(), options.getPort(), "null");
                    webSrv.start();
                } else {
                    log.warn("Got an already running VirtualBox WebServices instance!");
                }
                options.setPort(webSrv.getPort());
            }

            try {
                VirtualBoxManager mgr = VirtualBoxManager.createInstance(null);
                log.debug("Connection info: {}", options.extractServer());
                log.debug("User: {}", options.getUsername());
                log.debug("Password given: {}", options.getPasswd().length > 0);
                mgr.connect(options.extractServer(), options.getUsername(), options.getPasswd().toString());

                return mgr;
            } catch (VBoxException e) {
                disconnect();
                throw new HypervisorException("Unable to connect to the Virtualbox WebServices : " + e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            throw new HypervisorException("Invalid options syntax: " + e.getMessage(), e);
        }
    }

    @Override
    protected void disconnect() {
        try {
            try {
                vbMgr.disconnect();
            } catch (Throwable t) {
                if ((t.getMessage() == null) || !t.getMessage().contains("Connection refused")) {
                    log.warn("Error when disconnecting : " + t.getMessage());
                }
            }
            if (webSrv != null) {
                webSrv.stop();
            }
        } catch (Throwable t) {
            log.warn("Failed to stop WebServices", t);
        } finally {
            webSrv = null;
        }
    }

}
