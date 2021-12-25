/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2021 Max Dor
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

package io.kamax.test.vbox;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.exception.HypervisorException;
import io.kamax.hbox.hypervisor.vbox._VBoxWebSrv;
import io.kamax.hboxd.hypervisor.vbox.VBoxWebSrv;
import org.junit.Test;

import static io.kamax.hboxd.hypervisor.vbox.VBoxWebSrv.CFG_EXEC_PATH;
import static org.junit.Assert.*;

public class VBoxWebSrvTest {

    @Test
    public void ok() {
        Configuration.setSetting(CFG_EXEC_PATH, "src/test/script/vboxwebsrv-ok");
        VBoxWebSrv srv = new VBoxWebSrv();
        srv.start();
        assertEquals(_VBoxWebSrv.State.Started, srv.getState());
        assertTrue(srv.isRunning());
        srv.stop();
        assertEquals(_VBoxWebSrv.State.Stopped, srv.getState());
    }

    @Test(expected = HypervisorException.class)
    public void failedExec() {
        Configuration.setSetting(CFG_EXEC_PATH, "src/test/script/vboxwebsrv-failedExec");
        VBoxWebSrv srv = new VBoxWebSrv();
        srv.start();
        fail();
    }

    @Test(expected = HypervisorException.class)
    public void portInUse() {
        Configuration.setSetting(CFG_EXEC_PATH, "src/test/script/vboxwebsrv-portAlreadyInUse");
        VBoxWebSrv srv = new VBoxWebSrv();
        srv.start();
        fail();
    }

    public void version() {
        Configuration.setSetting(CFG_EXEC_PATH, "src/test/script/vboxwebsrv-version-ok");
        VBoxWebSrv srv = new VBoxWebSrv();
        String version = srv.getVersion();
        assertEquals("1.2.3r4", version);
    }

}
