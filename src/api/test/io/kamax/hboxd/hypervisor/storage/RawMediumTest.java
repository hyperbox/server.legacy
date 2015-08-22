/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
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

package io.kamax.hboxd.hypervisor.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public final class RawMediumTest {

    public static void validate(_RawMedium rawMed) {
        assertFalse(rawMed.getUuid().isEmpty());
        assertFalse(rawMed.getLocation().isEmpty());
        assertNotNull(rawMed.getLogicalSize());
        assertFalse(rawMed.getDeviceType().isEmpty());
        assertFalse(rawMed.getFormat().isEmpty());
        assertFalse(rawMed.getMediumFormat().isEmpty());
        assertFalse(rawMed.getName().isEmpty());
    }

}
