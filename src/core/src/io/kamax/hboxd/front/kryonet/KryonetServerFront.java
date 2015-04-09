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

package io.kamax.hboxd.front.kryonet;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm._Client;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.kryonet.KryoRegister;
import io.kamax.hbox.kryonet.KryoUncaughtExceptionHandler;
import io.kamax.hbox.kryonet.KryonetDefaultSettings;
import io.kamax.hboxd.front._Front;
import io.kamax.hboxd.front._RequestReceiver;
import io.kamax.tool.logging.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class KryonetServerFront implements _Front {

   private _RequestReceiver r;
   private Server server;
   private Integer port;

   private Map<Integer, _Client> clients;

   @Override
   public void start(_RequestReceiver r) throws HyperboxException {

      this.r = r;
      clients = new HashMap<Integer, _Client>();

      loadConfig();

      try {
         int netBufferWriteSize = Integer.parseInt(Configuration.getSetting(KryonetDefaultSettings.CFGKEY_KRYO_NET_WRITE_BUFFER_SIZE,
               KryonetDefaultSettings.CFGVAL_KRYO_NET_WRITE_BUFFER_SIZE));
         int netBufferObjectSize = Integer.parseInt(Configuration.getSetting(KryonetDefaultSettings.CFGVAL_KRYO_NET_OBJECT_BUFFER_SIZE,
               KryonetDefaultSettings.CFGVAL_KRYO_NET_OBJECT_BUFFER_SIZE));
         server = new Server(netBufferWriteSize, netBufferObjectSize);
         server.start();
         KryoRegister.register(server.getKryo());

         server.bind(port);
         server.getUpdateThread().setUncaughtExceptionHandler(new KryoUncaughtExceptionHandler());
         server.addListener(new MainListener());
         Logger.info("Kryonet connector is listening on port " + port);
      } catch (NumberFormatException e) {
         stop();
         throw new HyperboxException("Unable to start the Kryonet server : " + e.getLocalizedMessage());
      } catch (IOException e) {
         stop();
         throw new HyperboxException("Unable to start the Kryonet server : " + e.getLocalizedMessage());
      }
   }

   private void loadConfig() throws HyperboxException {

      try {
         port = Integer.parseInt(Configuration.getSetting(KryonetDefaultSettings.CFGKEY_KRYO_NET_TCP_PORT,
               KryonetDefaultSettings.CFGVAL_KRYO_NET_TCP_PORT.toString()));
         Logger.debug("Found valid value for config key [" + KryonetDefaultSettings.CFGKEY_KRYO_NET_TCP_PORT + "]: " + port);
      } catch (NumberFormatException e) {
         throw new HyperboxException("Invalid value for config key [" + KryonetDefaultSettings.CFGKEY_KRYO_NET_TCP_PORT + "]: "
               + Configuration.getSetting(KryonetDefaultSettings.CFGKEY_KRYO_NET_TCP_PORT));
      }
   }

   @Override
   public void stop() {

      server.stop();
   }

   public void stop(EventOut ev) {
      broadcast(ev);
      stop();
   }

   @Override
   public void broadcast(EventOut ev) {

      if (server != null) {
         server.sendToAllTCP(ev);
      }
   }

   private class MainListener extends Listener {

      @Override
      public void connected(Connection connection) {

         Logger.info("Conn #" + connection.getID() + " " + connection.getRemoteAddressTCP().getAddress().getHostAddress() + " connected.");
         _Client client = new KryonetClient(connection);
         r.register(client);
         clients.put(connection.getID(), client);
      }

      @Override
      public void received(Connection connection, Object object) {
         if (object.getClass().equals(Request.class)) {

            Request req = (Request) object;
            _Client client = clients.get(connection.getID());
            Logger.debug("Received request from " + client.getId() + " (" + client.getAddress() + ") : " + req.getExchangeId() + " - " + req.getCommand()
                  + " - " + req.getName());
            r.postRequest(client, req);
         }
      }

      @Override
      public void disconnected(Connection connection) {

         Logger.info("Conn #" + connection.getID() + " has disconnected.");
         r.unregister(clients.get(connection.getID()));
         clients.remove(connection.getID());
      }
   }

}
