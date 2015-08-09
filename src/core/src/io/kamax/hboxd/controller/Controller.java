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

package io.kamax.hboxd.controller;

import io.kamax.hbox.ClassManager;
import io.kamax.hbox.Configuration;
import io.kamax.hbox.HyperboxAPI;
import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm._Client;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.comm.out.event.server.ServerShutdownEventOut;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.Hyperbox;
import io.kamax.hboxd.comm.io.factory.ServerIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ShutdownAction;
import io.kamax.hboxd.factory.ModelFactory;
import io.kamax.hboxd.front._Front;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.hboxd.security.SystemUser;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tool.logging.LogLevel;
import io.kamax.tool.logging.Logger;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public final class Controller implements _Controller {

   private int exitCode = 0;
   private _Hyperbox model;
   private List<_Front> fronts = new ArrayList<_Front>();

   private Thread shutdownHook;

   static {
      Logger.raw(getHeader());
      try {
         if (new File(Hyperbox.getConfigFilePath()).exists()) {
            Configuration.init(Hyperbox.getConfigFilePath());
         } else {
            Logger.debug("Default config file does not exist, skipping: " + Hyperbox.getConfigFilePath());
         }
      } catch (HyperboxException e) {
         Logger.error(e);
         System.exit(1);
      }
   }

   public static String getHeader() {
      return HyperboxAPI.getLogHeader(Hyperbox.getVersion().toString());
   }

   private void startBack() {

      try {
         model = ModelFactory.get();
         model.init();
         model.start();
      } catch (HyperboxException e) {
         Logger.error(e);
         Logger.exception(e);
         stop();
      }
   }

   private void startFront() throws HyperboxException {

      Set<_Front> subTypes = ClassManager.getAtLeastOneOrFail(_Front.class);
      for (_Front test : subTypes) {
         fronts.add(test);
      }

      Logger.info("Starting Front-ends");
      for (final _Front f : fronts) {
         try {
            f.start(model.getReceiver());
            Logger.info(f.getClass().getSimpleName() + " has started");
         } catch (HyperboxException e1) {
            Logger.info(f.getClass().getSimpleName() + " failed to start");
            throw new HyperboxException(e1);
         }
      }
      Logger.info("Done starting Front-ends");

   }

   @Override
   public void start(String[] args) {

      Hyperbox.setArgs(args);
      shutdownHook = new Thread() {

         @Override
         public void run() {
            SecurityContext.setUser(new SystemUser());
            SessionContext.setClient(new Client());
            Controller.this.stop();
         }
      };
      Runtime.getRuntime().addShutdownHook(shutdownHook);

      try {
         Long startTime = System.currentTimeMillis();

         Logger.setLevel(LogLevel.valueOf(Configuration.getSetting("log.level", LogLevel.Info.toString())));

         String logFilename = Configuration.getSetting("log.file", "log/hboxd.log");
         if (!logFilename.contentEquals("none")) {
            Logger.log(logFilename, 4);
         }

         Logger.info("Hyperbox Init Sequence started");

         SecurityContext.init();
         SecurityContext.addAdminThread(shutdownHook);

         ShutdownAction.setController(this);

         Logger.verbose("-------- Environment variables -------");
         for (String name : System.getenv().keySet()) {
            if (name.startsWith(Configuration.CFG_ENV_PREFIX + Configuration.CFG_ENV_SEPERATOR)) {
               Logger.verbose(name + " | " + System.getenv(name));
            } else {
               Logger.debug(name + " | " + System.getenv(name));
            }
         }
         Logger.verbose("--------------------------------------");

         Logger.verbose("-------- Classpath entries -----------");
         for (URL classPathEntry : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
            Logger.verbose(classPathEntry);
         }
         Logger.verbose("--------------------------------------");

         startBack();
         startFront();
         Long endTime = System.currentTimeMillis();
         Logger.verbose("Hyperbox started in " + (endTime - startTime) + "ms");
         Logger.info("-------> Hyperbox is running <-------");
      } catch (Throwable e) {
         Logger.error("Hyperbox Init Sequence failed!");
         exitCode = 1;
         Logger.exception(e);
         stop();
      }

   }

   @Override
   public void stop() {

      Long startTime = System.currentTimeMillis();
      Logger.info("-------> Hyperbox is stopping <-------");
      try {
         stopFront();
         stopBack();
      } catch (Throwable e) {
         Logger.error("Exception when stopping Hyperbox: " + e.getMessage());
         Logger.exception(e);
      } finally {
         Long endTime = System.currentTimeMillis();
         Logger.verbose("Hyperbox Stop Sequence finished in " + (endTime - startTime) + "ms");
         Logger.info("-------> Hyperbox halt <-------");

         if (!Thread.currentThread().equals(shutdownHook)) {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            System.exit(exitCode);
         }
      }

   }

   private void stopFront() {

      Logger.info("Stopping front-ends");
      EventOut evOut = new ServerShutdownEventOut(new Date(), ServerIoFactory.get(model.getServerManager().getServer()));
      for (_Front f : fronts) {
         f.broadcast(evOut);
         f.stop();
         Logger.info("\t" + f.getClass().getSimpleName() + " stopped");
      }
      Logger.info("Finished stopping front-ends");
   }

   private void stopBack() {

      Logger.info("Stopping back-ends");
      if (model != null) {
         model.stop();
      }
      Logger.info("Finished stopping back-ends");
   }

   private class Client implements _Client {

      @Override
      public void putAnswer(Answer ans) {
         // stub
      }

      @Override
      public String getId() {
         return "System";
      }

      @Override
      public String getAddress() {
         return "System";
      }

      @Override
      public void post(EventOut evOut) {
         // stub
      }

   }

}
