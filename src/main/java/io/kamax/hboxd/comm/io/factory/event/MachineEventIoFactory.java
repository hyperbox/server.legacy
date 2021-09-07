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

package io.kamax.hboxd.comm.io.factory.event;

import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.machine.MachineDataChangeEventOut;
import io.kamax.hbox.comm.out.event.machine.MachineRegistrationEventOut;
import io.kamax.hbox.comm.out.event.machine.MachineSnapshotDataChangedEventOut;
import io.kamax.hbox.comm.out.event.machine.MachineStateEventOut;
import io.kamax.hbox.comm.out.hypervisor.MachineOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.states.MachineStates;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.comm.io.factory.MachineIoFactory;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.machine.MachineEvent;
import io.kamax.hboxd.event.machine.MachineRegistrationEvent;
import io.kamax.hboxd.event.machine.MachineStateEvent;

public final class MachineEventIoFactory implements _EventIoFactory {

    private MachineOut getObjOut(String id) {
        try {
            return MachineIoFactory.get(HBoxServer.get().getMachine(id));
        } catch (HyperboxException e) {
            return MachineIoFactory.get(id, MachineStates.Unknown.getId());
        }
    }

    @Override
    public Enum<?>[] getHandles() {
        return new Enum<?>[]{
                HyperboxEvents.MachineState,
                HyperboxEvents.MachineRegistration,
                HyperboxEvents.MachineDataChange,
                HyperboxEvents.MachineSnapshotDataChange
        };
    }

    @Override
    public EventOut get(_Hyperbox hbox, _Event ev) {
        MachineEvent mEv = (MachineEvent) ev;
        MachineOut mOut = getObjOut(mEv.getMachineId());

        switch ((HyperboxEvents) ev.getEventId()) {
            case MachineState:
                return new MachineStateEventOut(mEv.getTime(), ServerIoFactory.get(), mOut, ((MachineStateEvent) ev).getState());
            case MachineRegistration:
                return new MachineRegistrationEventOut(mEv.getTime(), ServerIoFactory.get(), mOut,
                        ((MachineRegistrationEvent) mEv).isRegistrated());
            case MachineDataChange:
                return new MachineDataChangeEventOut(mEv.getTime(), ServerIoFactory.get(), mOut);
            case MachineSnapshotDataChange:
                return new MachineSnapshotDataChangedEventOut(mEv.getTime(), ServerIoFactory.get(), mOut);
            default:
                return null;
        }
    }

}
