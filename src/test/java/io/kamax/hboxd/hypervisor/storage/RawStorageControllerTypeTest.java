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

import static org.junit.Assert.*;

public class RawStorageControllerTypeTest {

    public static void validate(_RawStorageControllerType rawType) {
        assertFalse(rawType.getId().isEmpty());
        assertFalse(rawType.getMinPort() <= 0);
        assertFalse(rawType.getMaxPort() <= 0);
        assertFalse(rawType.getMinPort() > rawType.getMaxPort());
        assertFalse(rawType.getMaxDevPerPort() <= 0);
    }

    public static void compare(_RawStorageControllerType rawType1, _RawStorageControllerType rawType2) {
        assertTrue(rawType1.getId().contentEquals(rawType2.getId()));
        assertEquals(rawType1.getMinPort(), rawType2.getMinPort());
        assertEquals(rawType1.getMaxPort(), rawType2.getMaxPort());
        assertEquals(rawType1.getMaxDevPerPort(), rawType2.getMaxDevPerPort());
    }

}
