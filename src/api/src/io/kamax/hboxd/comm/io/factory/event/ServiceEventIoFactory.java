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
import io.kamax.hbox.comm.out.ServiceOut;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.service.ServiceStateEventOut;
import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hbox.event._Event;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.comm.io.factory.ServiceIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.service.ServiceEvent;

public class ServiceEventIoFactory implements _EventIoFactory {

   @Override
   public Enum<?>[] getHandles() {
      return new Enum<?>[] {
            HyperboxEvents.ServiceState
      };
   }

   @Override
   public EventOut get(_Hyperbox hbox, _Event ev) {
      if (ev instanceof ServiceEvent) {
         ServiceEvent svcEv = (ServiceEvent) ev;
         ServerOut srvOut = ServerIoFactory.get();
         ServiceOut svcOut = ServiceIoFactory.get(svcEv.getService());
         switch ((HyperboxEvents) svcEv.getEventId()) {
            case ServiceState:
               return new ServiceStateEventOut(ev.getTime(), ev.getEventId(), srvOut, svcOut);
            default:
               return null;
         }
      } else {
         return null;
      }
   }

}
