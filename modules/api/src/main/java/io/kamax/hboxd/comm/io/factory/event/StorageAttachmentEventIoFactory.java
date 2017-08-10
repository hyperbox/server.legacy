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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.ServerOut;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.storage.StorageControllerAttachmentDataModifiedEventOut;
import io.kamax.hbox.comm.out.hypervisor.MachineOut;
import io.kamax.hbox.comm.out.storage.StorageControllerOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hboxd.comm.io.factory.MachineIoFactory;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.comm.io.factory.StorageControllerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.event.storage.StorageAttachmentEvent;

public class StorageAttachmentEventIoFactory implements _EventIoFactory {

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[]{
                HyperboxEvents.StorageAttachmentAdded,
                HyperboxEvents.StorageAttachmentModified,
                HyperboxEvents.StorageAttachmentRemoved
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        StorageAttachmentEvent event = (StorageAttachmentEvent) ev;

        _Machine vm = hbox.getServer().getMachine(event.getMachineId());

        ServerOut srvOut = ServerIoFactory.get();
        MachineOut vmOut = MachineIoFactory.getSimple(vm);
        StorageControllerOut stoOut = StorageControllerIoFactory.get(vm.getStorageController(event.getControllerId()));

        switch ((HyperboxEvents) event.getEventId()) {
            case StorageAttachmentAdded:
                return new StorageControllerAttachmentDataModifiedEventOut(ev.getTime(), srvOut, vmOut, stoOut);
            case StorageAttachmentModified:
                return new StorageControllerAttachmentDataModifiedEventOut(ev.getTime(), srvOut, vmOut, stoOut);
            case StorageAttachmentRemoved:
                return new StorageControllerAttachmentDataModifiedEventOut(ev.getTime(), srvOut, vmOut, stoOut);
            default:
                return null;
        }
    }

}
