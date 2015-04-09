/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Maxime Dor
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

package io.kamax.hboxd.event.net;

import io.kamax.hbox.event.Event;
import io.kamax.hboxd.hypervisor._Hypervisor;

public abstract class NetAdaptorEvent extends Event {

   private _Hypervisor hyp;
   private String modeId;
   private String adaptorId;

   public NetAdaptorEvent(Enum<?> eventId, _Hypervisor hyp, String modeId, String adaptorId) {
      super(eventId);
      this.hyp = hyp;
      this.modeId = modeId;
      this.adaptorId = adaptorId;
   }

   public _Hypervisor getHypervisor() {
      return hyp;
   }

   public String getMode() {
      return modeId;
   }

   public String getAdaptor() {
      return adaptorId;
   }

}
