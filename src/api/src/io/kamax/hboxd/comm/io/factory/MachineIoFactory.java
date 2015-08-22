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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.comm.out.hypervisor.MachineOut;
import io.kamax.hbox.comm.out.network.NetworkInterfaceOut;
import io.kamax.hbox.comm.out.storage.StorageControllerOut;
import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.core.model._NetworkInterface;
import io.kamax.hboxd.core.model._StorageController;
import io.kamax.setting._Setting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class MachineIoFactory {

    private MachineIoFactory() {
        // static class, cannot be instantiated
    }

    public static MachineOut get(String uuid, String state) {
        return get(uuid, state, true);
    }

    public static MachineOut get(String uuid, String state, boolean isAvailable) {
        return new MachineOut(HBoxServer.get().getId(), uuid, state, isAvailable);
    }

    public static MachineOut getSimple(String id) {
        return new MachineOut(HBoxServer.get().getId(), id);
    }

    public static MachineOut getSimple(String uuid, String state, List<_Setting> settings) {
        return new MachineOut(HBoxServer.get().getId(), uuid, state, SettingIoFactory.getList(settings));
    }

    public static MachineOut getSimple(_Machine m) {
        List<_Setting> settings = new ArrayList<_Setting>();
        if (m.isAccessible()) {
            settings.addAll(Arrays.asList(
                    m.getSetting(MachineAttribute.Name.getId()),
                    m.getSetting(MachineAttribute.HasSnapshot.getId()),
                    m.getSetting(MachineAttribute.CurrentSnapshotUuid.getId())
                    ));
            return getSimple(m.getUuid(), m.getState().getId(), settings);
        } else {
            return get(m.getUuid(), MachineStates.Inaccessible.getId(), false);
        }

    }

    public static MachineOut get(_Machine m) {
        if (m.isAccessible()) {
            String serverId = HBoxServer.get().getId();
            List<StorageControllerOut> scOutList = new ArrayList<StorageControllerOut>();
            for (_StorageController sc : m.listStorageControllers()) {
                scOutList.add(StorageControllerIoFactory.get(sc));
            }

            List<NetworkInterfaceOut> nicOutList = new ArrayList<NetworkInterfaceOut>();
            for (_NetworkInterface nic : m.listNetworkInterfaces()) {
                nicOutList.add(NetworkInterfaceIoFactory.get(nic));
            }

            MachineOut mOut = new MachineOut(serverId, m.getUuid(), m.getState(), SettingIoFactory.getList(m.getSettings()), scOutList, nicOutList);
            return mOut;
        } else {
            return get(m.getUuid(), MachineStates.Inaccessible.getId(), false);
        }
    }

}
