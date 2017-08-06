/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Maxime Dor
 * 
 * http://kamax.io/hbox/
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

package io.kamax.hboxd.core.model;

import io.kamax.hbox.constant.MachineAttribute;

import static org.junit.Assert.*;

public class MachineTest {

    public static void validateSimple(_Machine vm) {
        assertNotNull(vm);
        assertNotNull(vm.getUuid());
        assertFalse(vm.getUuid().isEmpty());
        assertNotNull(vm.getName());
        assertFalse(vm.getName().isEmpty());
        assertNotNull(vm.getState());
    }

    public static void validateFull(_Machine vm) {
        validateSimple(vm);
        assertFalse(vm.getSetting(MachineAttribute.OsType.toString()).getString().isEmpty());
        for (_StorageController sto : vm.listStorageControllers()) {
            StorageControllerTest.validateFull(sto);
        }
        for (_NetworkInterface nic : vm.listNetworkInterfaces()) {
            NetworkInterfaceTest.validateFull(nic);
        }
    }

    public static void compareSimple(_Machine vm1, _Machine vm2) {
        assertTrue(vm1.getUuid().contentEquals(vm2.getUuid()));
        assertTrue(vm1.getName().contentEquals(vm2.getName()));
        assertTrue(vm1.getState().equals(vm2.getState()));
    }

}
