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

package io.kamax.hboxd.hypervisor.vbox6_0.factory.event;

import io.kamax.hbox.event._Event;
import io.kamax.hboxd.event.machine.MachineSnapshotDataChangedEvent;
import io.kamax.hboxd.event.snapshot.SnapshotDeletedEvent;
import io.kamax.hboxd.hypervisor.vbox6_0.VBox;
import io.kamax.hboxd.hypervisor.vbox6_0.factory._PreciseEventFactory;
import org.virtualbox_6_0.IEvent;
import org.virtualbox_6_0.ISnapshotDeletedEvent;
import org.virtualbox_6_0.VBoxEventType;

public class SnapshotDeletedEventFactory implements _PreciseEventFactory {

    @Override
    public VBoxEventType getType() {
        return VBoxEventType.OnSnapshotDeleted;
    }

    @Override
    public ISnapshotDeletedEvent getRaw(IEvent vbEvent) {

        return ISnapshotDeletedEvent.queryInterface(vbEvent);
    }

    @Override
    public _Event getEvent(IEvent vbEvent) {

        ISnapshotDeletedEvent snapEv = (ISnapshotDeletedEvent) vbEvent;

        // Generic event might be used due to Webservices bug, depending on revision - See Javadoc of HyperboxEvents.MachineSnapshotDataChange
        // This revision is only valid for 4.2 branch
        if (VBox.get().getRevision() >= 90983) {
            return new SnapshotDeletedEvent(snapEv.getMachineId(), snapEv.getSnapshotId());
        } else {
            return new MachineSnapshotDataChangedEvent(snapEv.getMachineId());
        }
    }

}
