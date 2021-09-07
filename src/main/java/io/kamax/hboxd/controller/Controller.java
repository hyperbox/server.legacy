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

package io.kamax.hboxd.controller;

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
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public final class Controller implements _Controller {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private int exitCode = 0;
    private _Hyperbox model;
    private final List<_Front> fronts = new ArrayList<>();

    private Thread shutdownHook;

    static {
        try {
            if (new File(Hyperbox.getConfigFilePath()).exists()) {
                Configuration.init(Hyperbox.getConfigFilePath());
            } else {
                log.debug("Default config file does not exist, skipping: " + Hyperbox.getConfigFilePath());
            }
        } catch (HyperboxException e) {
            log.error("Cannot init from config file", e);
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
            log.error("Cannot start model", e);
            stop();
        }
    }

    private void startFront() throws HyperboxException {
        Set<_Front> subTypes = Hyperbox.loadSubTypes(_Front.class);
        fronts.addAll(subTypes);

        log.info("Starting Front-ends");
        for (final _Front f : fronts) {
            try {
                f.start(model.getReceiver());
                log.info(f.getClass().getSimpleName() + " has started");
            } catch (HyperboxException e1) {
                log.info(f.getClass().getSimpleName() + " failed to start");
                throw new HyperboxException(e1);
            }
        }
        log.info("Done starting Front-ends");

    }

    @Override
    public void start(String[] args) {
        Hyperbox.setArgs(args);
        shutdownHook = new Thread(() -> {
            SecurityContext.setUser(new SystemUser());
            SessionContext.setClient(new Client());
            Controller.this.stop();
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        try {
            Long startTime = System.currentTimeMillis();
            String logLevel = Configuration.getSetting("log.level", "info");
            System.setProperty("org.slf4j.simpleLogger.log", logLevel);
            System.setProperty("org.slf4j.simpleLogger.log.io.kamax", logLevel);
            System.setProperty("org.slf4j.simpleLogger.log." + KxLog.logPrefix, logLevel);

            /*
            String logFilename = Configuration.getSetting("log.file", "log/hboxd.log");
            if (!logFilename.contentEquals("none")) {
                log.log(logFilename, 4);
            }
            */

            log.info(getHeader());
            log.info("Hyperbox Init Sequence started");

            SecurityContext.init();
            SecurityContext.addAdminThread(shutdownHook);

            ShutdownAction.setController(this);

            log.debug("-------- Environment variables -------");
            for (String name : System.getenv().keySet()) {
                log.debug(name + " | " + System.getenv(name));
            }
            log.debug("--------------------------------------");

            startBack();
            startFront();
            Long endTime = System.currentTimeMillis();
            log.debug("Hyperbox started in " + (endTime - startTime) + "ms");
            log.info("-------> Hyperbox is running <-------");
        } catch (Throwable e) {
            log.error("Hyperbox Init Sequence failed!", e);
            exitCode = 1;
            stop();
        }

    }

    @Override
    public void stop() {

        Long startTime = System.currentTimeMillis();
        log.info("-------> Hyperbox is stopping <-------");
        try {
            stopFront();
            stopBack();
        } catch (Throwable e) {
            log.error("Exception when stopping Hyperbox", e);
        } finally {
            Long endTime = System.currentTimeMillis();
            log.debug("Hyperbox Stop Sequence finished in " + (endTime - startTime) + "ms");
            log.info("-------> Hyperbox halt <-------");

            if (!Thread.currentThread().equals(shutdownHook)) {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
                System.exit(exitCode);
            }
        }

    }

    private void stopFront() {

        log.info("Stopping front-ends");
        EventOut evOut = new ServerShutdownEventOut(new Date(), ServerIoFactory.get(model.getServerManager().getServer()));
        for (_Front f : fronts) {
            f.broadcast(evOut);
            f.stop();
            log.info("\t" + f.getClass().getSimpleName() + " stopped");
        }
        log.info("Finished stopping front-ends");
    }

    private void stopBack() {

        log.info("Stopping back-ends");
        if (model != null) {
            model.stop();
        }
        log.info("Finished stopping back-ends");
    }

    private static class Client implements _Client {

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
