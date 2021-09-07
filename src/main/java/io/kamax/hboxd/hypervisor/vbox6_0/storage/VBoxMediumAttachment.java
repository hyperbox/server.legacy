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

package io.kamax.hboxd.hypervisor.vbox6_0.storage;

import io.kamax.hboxd.hypervisor.storage._RawMedium;
import io.kamax.hboxd.hypervisor.storage._RawMediumAttachment;
import io.kamax.hboxd.hypervisor.storage._RawStorageController;
import io.kamax.hboxd.hypervisor.vbox6_0.vm.VBoxMachine;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import org.virtualbox_6_0.IMediumAttachment;

public final class VBoxMediumAttachment implements _RawMediumAttachment {

    private final VBoxMachine machine;
    private VBoxMedium medium;
    private final VBoxStorageController controller;
    private final long portId;
    private final long deviceId;
    private final String deviceType;
    private final boolean passThrough;

    public VBoxMediumAttachment(String machineUuid, IMediumAttachment medAttach) {
        machine = new VBoxMachine(machineUuid);
        if (medAttach.getMedium() != null) { // can be null for removable devices, see IMediumAttachment::medium
            medium = new VBoxMedium(medAttach.getMedium());
        }
        controller = new VBoxStorageController(machine, medAttach.getController());
        portId = medAttach.getPort();
        deviceId = medAttach.getDevice();
        deviceType = medAttach.getType().toString();
        passThrough = medAttach.getPassthrough();
    }

    @Override
    public _RawVM getMachine() {
        return machine;
    }

    @Override
    public _RawMedium getMedium() {
        return medium;
    }

    @Override
    public _RawStorageController getController() {
        return controller;
    }

    @Override
    public long getPortId() {
        return portId;
    }

    @Override
    public long getDeviceId() {
        return deviceId;
    }

    @Override
    public String getDeviceType() {
        return deviceType;
    }

    @Override
    public boolean isPassThrough() {
        return passThrough;
    }

    @Override
    public boolean hasMedium() {
        return medium != null;
    }

}
