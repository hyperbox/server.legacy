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

package io.kamax.hboxd.session;

import net.engio.mbassy.listener.Handler;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm._Client;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.event._Event;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.comm.io.factory.EventIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.session._Session;
import io.kamax.hboxd.session._SessionManager;
import io.kamax.tool.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionManager implements _SessionManager {

   private Map<_Client, _Session> sessions = new ConcurrentHashMap<_Client, _Session>();
   private _Hyperbox hbox;

   @Override
   public void start(_Hyperbox hbox) throws HyperboxException {

      this.hbox = hbox;
      EventManager.register(this);
   }

   @Override
   public void stop() {

      for (_Session sess : sessions.values()) {
         sess.close();
         sessions.remove(sess.getId());
      }
   }

   @Override
   public List<_Session> list() {
      return new ArrayList<_Session>(sessions.values());
   }

   @Override
   public void closeSession(_Client c) {

      if (sessions.containsKey(c)) {
         _Session sess = sessions.get(c);
         sess.close();
         sessions.remove(c);
      } else {
         Logger.warning("Asked to close session for Client " + c.toString() + " but no session was found. Skipping...");
      }

   }

   @Override
   public boolean hasSession(_Client c) {
      return sessions.containsKey(c);
   }

   @Override
   public _Session getSession(_Client c) {
      return sessions.get(c);
   }

   @Override
   public void postRequest(_Client client, Request req) {

      Logger.debug("Received Request from " + client.getAddress());
      if (!hasSession(client)) {
         register(client);
      }

      getSession(client).putRequest(req);
   }

   @Override
   public void register(_Client client) {

      if (!hasSession(client)) {
         Logger.debug(client.getAddress() + " connected, registering...");
         _Session sess = new UserSession(client, hbox.getTaskManager());
         Logger.debug("Session #" + sess.getId() + " has been created for " + client.getAddress());
         sessions.put(client, sess);
         Logger.debug("Session #" + sess.getId() + " has been registered for " + client.getAddress());
      }
   }

   @Override
   public void unregister(_Client client) {

      if (hasSession(client)) {
         Logger.warning(client + " did not close its session before disconnecting, cleaning up");
         closeSession(client);
      }
   }

   @Handler
   public void postEvent(_Event event) {

      EventOut evOut = EventIoFactory.get(hbox, event);
      for (_Session sess : sessions.values()) {
         if (hbox.getSecurityManager().isAuthorized(sess.getUser(), event)) {
            sess.post(evOut);
         }
      }
   }

}
