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

package io.kamax.apps.hbox.vbox6_0;

import io.kamax.hbox.Configuration;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.hypervisor.vbox6_0.VBoxWebServicesHypervisor;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assume.assumeTrue;

public class VBoxWSTest {

    @BeforeClass
    public static void beforeClass() {
        assumeTrue(StringUtils.equals("1", System.getenv("VBOX_WITH_INTEGRATION_TESTS")));
    }

    @Test
    public void connect() {
        Configuration.setSetting("core.eventmgr.class", DummyEventManager.class.getName());
        _Hypervisor hyp = new VBoxWebServicesHypervisor();
        hyp.setEventManager(new DummyEventManager());
        hyp.start("");
        System.out.println(hyp.getVendor());
        System.out.println(hyp.getVersion());
        hyp.stop();
    }

}
