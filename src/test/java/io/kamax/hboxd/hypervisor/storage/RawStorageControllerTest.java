/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Max Dor
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

package io.kamax.hboxd.hypervisor.storage;

import io.kamax.hbox.constant.StorageControllerAttribute;

import static org.junit.Assert.*;

public class RawStorageControllerTest {

    public static void validateSimple(_RawStorageController rawSc) {
        assertFalse(rawSc.getMachineUuid().isEmpty());
        assertFalse(rawSc.getName().isEmpty());
    }

    public static void validateFull(_RawStorageController rawSc) {
        validateSimple(rawSc);
        assertNotNull(rawSc.getSetting(StorageControllerAttribute.MinPortCount).getValue());
        assertNotNull(rawSc.getSetting(StorageControllerAttribute.MaxPortCount).getValue());
        assertNotNull(rawSc.getSetting(StorageControllerAttribute.MaxDeviceCount).getValue());
        assertTrue(rawSc.getMinPortCount() >= 0);
        assertTrue(rawSc.getMaxPortCount() >= 0);
        assertTrue(rawSc.getMaxPortCount() >= rawSc.getMinPortCount());
        assertTrue(rawSc.getMaxDeviceCount() >= 0);
    }

}
