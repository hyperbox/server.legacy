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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.comm.out.hypervisor.SnapshotOut;
import io.kamax.hboxd.hypervisor.vm.snapshot._RawSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SnapshotIoFactory {

    private SnapshotIoFactory() {
        // Static class, not allowed
    }

    public static SnapshotOut get(_RawSnapshot rawSnap) {
        if (rawSnap == null) {
            return null;
        }
        String parentUuid = null;
        if (rawSnap.hasParent()) {
            parentUuid = rawSnap.getParent().getUuid();
        }

        List<String> childrenUuid = new ArrayList<>();
        for (_RawSnapshot child : rawSnap.getChildren()) {
            childrenUuid.add(child.getUuid());
        }

        SnapshotOut snapOut = new SnapshotOut(rawSnap.getUuid(), SettingIoFactory.getList(rawSnap.listSettings()), parentUuid, childrenUuid);
        return snapOut;
    }

}
