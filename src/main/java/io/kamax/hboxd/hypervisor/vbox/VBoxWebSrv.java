/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2015 Max Dor
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

package io.kamax.hboxd.hypervisor.vbox;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.exception.HypervisorException;
import io.kamax.hbox.hypervisor.vbox.VBoxPlatformUtil;
import io.kamax.hbox.hypervisor.vbox._VBoxWebSrv;
import io.kamax.tools.logging.KxLog;
import io.kamax.tools.net.NetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.listener.ProcessListener;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VBoxWebSrv implements _VBoxWebSrv {

    public static final String CFG_EXEC_PATH = "vbox.exec.web.path";

    private static class OutputReader extends LogOutputStream {

        private final Consumer<String> consumer;

        public OutputReader(Consumer<String> consumer) {
            this.consumer = consumer;
        }

        @Override
        protected void processLine(String line) {
            consumer.accept(line);
        }

    }

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private String host = "localhost";
    private int port = 0;
    private String authMethod = "null";

    private final List<String> defaultExecPaths = new ArrayList<>();

    private ProcessExecutor processExec;
    private StartedProcess processRun;

    private State runState = State.Stopped;
    private String error;

    public VBoxWebSrv() {
        String execPath = Configuration.getSetting(CFG_EXEC_PATH);
        if (StringUtils.isNotBlank(execPath)) {
            defaultExecPaths.add(execPath);
        } else {
            defaultExecPaths.add("/usr/bin/vboxwebsrv");
            defaultExecPaths.add("/usr/lib/virtualbox/vboxwebsrv");
            defaultExecPaths.add("/usr/local/bin/vboxwebsrv");
            defaultExecPaths.add(VBoxPlatformUtil.getInstallPathWin() + "/VBoxWebSrv.exe");
        }
    }

    public VBoxWebSrv(String host, int port, String authMethod) {
        this();

        this.host = host;
        this.port = port;
        this.authMethod = authMethod;
    }

    private void validateExecutable(String path) {
        File exec = new File(path).getAbsoluteFile();
        if (!exec.exists()) {
            throw new HypervisorException(path + " does not exist");
        }

        if (!exec.isFile()) {
            throw new HypervisorException(path + " is not a file");
        }

        if (!exec.canExecute()) {
            throw new HypervisorException(path + " is not executable");
        }
    }

    public String locateExecutable() {
        for (String path : defaultExecPaths) {
            try {
                validateExecutable(path);
                log.info("{} is a valid VirtualBox WebService executable", path);
                return path;
            } catch (HyperboxException e) {
                log.debug("Not a valid web exec [{}]: {}", path, e.getMessage());
            }
        }

        throw new HypervisorException("Could not locate a valid VirtualBox WebService executable");
    }

    public String getVersion() {
        Pattern p = Pattern.compile("(\\d+.\\d+.\\d+)(r(\\d+))?");
        List<String> output = new ArrayList<>();

        try {
            List<String> args = new ArrayList<>();
            args.add(locateExecutable());
            args.add("--version");
            ProcessExecutor exec = new ProcessExecutor().command(args).destroyOnExit()
                    .redirectOutput(new OutputReader(output::add));
            StartedProcess sp = exec.start();
            ProcessResult result = sp.getFuture().get(15, TimeUnit.SECONDS);
            if (result.getExitValue() != 0) {
                log.warn("vboxwebsrv output:\n{}", output);
                throw new HypervisorException("Unexpected return code: " + result.getExitValue());
            }

            String versionLine = output.get(output.size() - 1);
            log.debug("VboxWebSrv version: {}", versionLine);
            Matcher m = p.matcher(versionLine);
            if (!m.matches()) {
                return "";
            }
            String version = m.group(1);
            if (StringUtils.isBlank(version)) {
                throw new HypervisorException("Invalid version: " + versionLine);
            }
            return version;
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) {
            log.warn("vboxwebsrv output:\n{}", output);
            throw new HypervisorException("Unable to get vboxwebsrv version", e);
        }
    }

    @Override
    public synchronized void start() {
        if (isRunning()) {
            log.warn("VBox WebSrv Server is already running, ignoring call to start again");
        }

        log.info("VBox WebSrv Server Start: Start");
        runState = State.Starting;
        try {
            if (port == 0) {
                this.port = NetUtil.getRandomAvailablePort(host, port, 100);
                log.info("Using autoDetected port: {}", this.port);
            }

            List<String> args = new ArrayList<>();
            args.add(locateExecutable());
            // Network settings
            args.add("-H"); // host
            args.add(host);
            args.add("-p"); // port
            args.add(Integer.toString(this.port));
            // Authentication settings
            args.add("-A"); // auth method
            args.add(authMethod);

            if (!NetUtil.isPortAvailable(host, port)) {
                throw new HypervisorException("Cannot start the WebService process: a process is already listening on " + host + ":" + port);
            }

            StringBuffer execOutput = new StringBuffer();
            processExec = new ProcessExecutor().command(args).destroyOnExit()
                    .redirectOutput(new OutputReader(line -> {
                        execOutput.append(line).append(System.lineSeparator());
                        if (StringUtils.contains(line, "Socket connection successful: ")) {
                            log.debug("VBox Web Srv: listening for new connections, marking as started");
                            runState = State.Started;
                        }

                        if (StringUtils.contains(line, "#### SOAP FAULT: Address already in use [detected]")) {
                            error = "WebService port " + port + " is already in use";
                            stop();
                        }
                    }))
                    .addListener(new ProcessListener() {
                        @Override
                        public void afterStop(Process process) {
                            log.info("VirtualBox Web Service exec has exited with rc {}", process.exitValue());
                            runState = State.Stopped;
                            synchronized (this) {
                                notifyAll();
                            }
                        }
                    });
            processRun = processExec.start();

            Instant waitLimitTs = Instant.now().plusSeconds(5);
            while (Instant.now().isBefore(waitLimitTs)) {
                if (State.Started.equals(runState)) {
                    log.info("Started VBox WS Process");
                    log.debug("vboxwebsrv output:\n{}", execOutput);
                    return;
                }

                if (State.Stopped.equals(runState)) {
                    log.warn("vboxwebsrv output:\n{}", execOutput);
                    throw new HypervisorException(error);
                }

                if (State.Starting.equals(runState) && !isRunning()) {
                    throw new HypervisorException("Unexpected exit of the VirtualBox Web Service: " + getExitCode());
                }

                try {
                    wait(100L);
                } catch (InterruptedException e) {
                    // we don't care
                }
            }

            if (!NetUtil.isPortAvailable(host, port)) {
                log.warn("VirtualBox Web Services port is in use, but service was not detected as started. Assuming started");
                runState = State.Started;
                return;
            }

            stop();
            throw new HypervisorException("VirtualBox Web Services did not start within wait time");
        } catch (IOException e) {
            stop();
            throw new HypervisorException(e);
        } finally {
            log.debug("VBox WebSrv Server Start: End");
        }
    }

    @Override
    public synchronized void stop() {
        if (State.Stopped.equals(runState)) {
            return;
        }

        if (!isRunning()) {
            return;
        }

        runState = State.Stopping;
        Process process = processRun.getProcess();

        log.debug("VBox WebServices Server shutdown: Start");
        try {
            log.info("Stopping VBox WS Process");
            try {
                process.destroy();

                if (isRunning()) {
                    int j = 40;
                    for (int i = 1; i <= j && isRunning(); i++) {
                        log.debug("Waiting for VBox WebServices process to stop ({}/{})", i, j);
                        try {
                            processRun.getFuture().get(250L, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            log.warn("Interrupted while waiting for VBox WS process");
                        } catch (ExecutionException e) {
                            log.warn("Error while waiting for VBox WS process to end", e);
                        } catch (TimeoutException e) {
                            // as expected
                        }
                    }
                }

                if (isRunning()) {
                    process.destroyForcibly();
                }
                log.debug("Is VBox WS process stopped? {}", !isRunning());
                log.debug("VBox WS return code: {}", process.exitValue());
                log.info("Stopped VBox WS process");
            } catch (Throwable t) {
                if (isRunning()) {
                    processExec.destroyOnExit();
                    log.warn("Unable to stop VBox WS process, marked to destroy on exit", t);
                } else {
                    log.warn("VBox WS process stop was not clean", t);
                }
            }
        } finally {
            runState = State.Stopped;
            log.debug("VBox WebServices Server shutdown: End");
        }
    }

    @Override
    public synchronized boolean isRunning() {
        if (Objects.isNull(processRun)) {
            return false;
        }

        try {
            processRun.getProcess().exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    @Override
    public int getPort() {
        if (!isRunning()) {
            throw new IllegalStateException("VBox Web Server is not running");
        }

        return port;
    }

    @Override
    public void kill() {
        if (!isRunning()) {
            throw new IllegalStateException("VBox Web Server is not running");
        }

        processRun.getProcess().destroy();
    }

    @Override
    public int getExitCode() {
        if (processRun == null) {
            throw new IllegalStateException("VBox Web Server has not been started");
        }

        if (isRunning()) {
            throw new IllegalStateException("VBox Web Server has not been started");
        }

        return processRun.getProcess().exitValue();
    }

    @Override
    public State getState() {
        return runState;
    }

}
