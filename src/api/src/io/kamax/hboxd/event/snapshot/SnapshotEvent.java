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

package io.kamax.hboxd.event.snapshot;

import io.kamax.hboxd.event.machine.MachineEvent;

public abstract class SnapshotEvent extends MachineEvent {

   private String snapUuid;

   public SnapshotEvent(Enum<?> id, String machineUuid, String snapUuid) {
      super(id, machineUuid);
      this.snapUuid = snapUuid;
      snapUuid.isEmpty();
   }

   public String getSnapshotUuid() {
      return snapUuid;
   }

}
