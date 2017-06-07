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

package io.kamax.hboxd.hypervisor.snapshot;

import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;

import static org.junit.Assert.*;

public class RawSnapshotTest {

    private RawSnapshotTest() {
        // not in use
    }

    public static void validateSimple(_RawSnapshot snap) {
        assertFalse(snap.getUuid().isEmpty());
        assertFalse(snap.getMachineId().isEmpty());
    }

    public static void validateFull(_RawSnapshot snap) {
        validateSimple(snap);
        assertFalse(snap.getName().isEmpty());
        assertNotNull(snap.getDescription());
        assertNotNull(snap.getChildren());
        if (snap.hasChildren()) {
            assertFalse(snap.getChildren().isEmpty());
        } else {
            assertTrue(snap.getChildren().isEmpty());
        }
        if (snap.hasParent()) {
            assertNotNull(snap.getParent());
        } else {
            assertNull(snap.getParent());
        }
        assertNotNull(snap.isOnline());
    }

}
