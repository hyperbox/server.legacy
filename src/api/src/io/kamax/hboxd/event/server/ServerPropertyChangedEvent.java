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

package io.kamax.hboxd.event.server;

import io.kamax.hbox.event.HyperboxEvents;
import io.kamax.hboxd.server._Server;

public class ServerPropertyChangedEvent extends ServerEvent {

   private Object property;
   private Object newValue;

   public ServerPropertyChangedEvent(_Server srv, Object property, Object newValue) {
      super(HyperboxEvents.ServerPropertyChanged, srv);
      this.property = property;
      this.newValue = newValue;
   }

   public Object getProperty() {
      return property;
   }

   public Object getValue() {
      return newValue;
   }

}
