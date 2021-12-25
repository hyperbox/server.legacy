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

package io.kamax.hboxd.core;

import com.google.common.io.Files;
import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.HypervisorIn;
import io.kamax.hbox.comm.out.event.EventOut;
import io.kamax.hbox.constant.ServerAttribute;
import io.kamax.hbox.constant.ServerType;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.exception.HypervisorException;
import io.kamax.hbox.hypervisor.vbox.VirtualBox;
import io.kamax.hbox.states.ServerConnectionState;
import io.kamax.hbox.states.ServerState;
import io.kamax.hboxd.HBoxServer;
import io.kamax.hboxd.Hyperbox;
import io.kamax.hboxd.core.action._ActionManager;
import io.kamax.hboxd.core.model.Medium;
import io.kamax.hboxd.core.model._Machine;
import io.kamax.hboxd.core.model._Medium;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.hypervisor.HypervisorDisconnectedEvent;
import io.kamax.hboxd.event.server.ServerConnectionStateEvent;
import io.kamax.hboxd.event.server.ServerPropertyChangedEvent;
import io.kamax.hboxd.event.system.SystemStateEvent;
import io.kamax.hboxd.exception.hypervisor.HypervisorNotConnectedException;
import io.kamax.hboxd.exception.server.ServerNotFoundException;
import io.kamax.hboxd.factory.MachineFactory;
import io.kamax.hboxd.factory.SecurityManagerFactory;
import io.kamax.hboxd.front._RequestReceiver;
import io.kamax.hboxd.host.Host;
import io.kamax.hboxd.host._Host;
import io.kamax.hboxd.hypervisor.Hypervisor;
import io.kamax.hboxd.hypervisor._Hypervisor;
import io.kamax.hboxd.hypervisor.storage._RawMedium;
import io.kamax.hboxd.hypervisor.vbox.VBoxWebSrv;
import io.kamax.hboxd.hypervisor.vm._RawVM;
import io.kamax.hboxd.persistence._Persistor;
import io.kamax.hboxd.persistence.sql.h2.H2SqlPersistor;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.hboxd.security._SecurityManager;
import io.kamax.hboxd.security._User;
import io.kamax.hboxd.server._Server;
import io.kamax.hboxd.server._ServerManager;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.hboxd.session.SessionManager;
import io.kamax.hboxd.session._SessionManager;
import io.kamax.hboxd.store.StoreManager;
import io.kamax.hboxd.store._StoreManager;
import io.kamax.hboxd.task.TaskManager;
import io.kamax.hboxd.task._TaskManager;
import io.kamax.tools.AxBooleans;
import io.kamax.tools.AxSystems;
import io.kamax.tools.logging.KxLog;
import net.engio.mbassy.listener.Handler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;

public class SingleHostServer implements _Hyperbox, _Server {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private ServerState state;

    private _SessionManager sessMgr;
    private _SecurityManager secMgr;
    private _ActionManager actionMgr;
    private _TaskManager taskMgr;
    private _StoreManager storeMgr;
    private _Persistor persistor;

    private final Map<String, Class<? extends _Hypervisor>> hypervisors = new HashMap<>();
    private _Hypervisor hypervisor;

    private final _Client system = new SystemClient();

    private String id;
    private String name;

    private void setState(ServerState state) {
        if (this.state != state) {
            this.state = state;
            EventManager.post(new SystemStateEvent(state));
        }
    }

    public ServerState getState() {
        return state;
    }

    private void loadPersistors() throws HyperboxException {
        persistor = Hyperbox.loadClass(CFGKEY_CORE_PERSISTOR_CLASS, H2SqlPersistor.class);
        persistor.init();
    }

    @Override
    public void init() throws HyperboxException {
        SessionContext.setClient(system);

        HBoxServer.initServer(this);

        EventManager.start();
        EventManager.register(this);

        loadPersistors();

        secMgr = SecurityManagerFactory.get();
        SecurityContext.setAdminUser(secMgr.init(persistor));

        actionMgr = new DefaultActionManager();
        taskMgr = new TaskManager();

        storeMgr = new StoreManager();
        storeMgr.init(persistor);

        sessMgr = new SessionManager();
    }

    @Override
    public void start() throws HyperboxException {
        setState(ServerState.Starting);

        persistor.start();
        HBoxServer.initPersistor(persistor);

        if (!HBoxServer.hasSetting(CFGKEY_SRV_ID)) {
            log.debug("Generating new Server ID");
            HBoxServer.setSetting(CFGKEY_SRV_ID, UUID.randomUUID());
            log.debug("Generating default Server name");
            HBoxServer.setSetting(CFGKEY_SRV_NAME, AxSystems.getHostname());
        }
        id = HBoxServer.getSettingOrFail(CFGKEY_SRV_ID);
        name = HBoxServer.getSettingOrFail(CFGKEY_SRV_NAME);

        log.info("Server ID: " + id);
        log.info("Server Name: " + name);

        actionMgr.start();
        secMgr.start();
        SecurityContext.initSecurityManager(secMgr);

        //FIXME Changes the admin password
        if (Arrays.asList(Hyperbox.getArgs()).contains("--reset-admin-pass")) {
            boolean next = false;
            for (String item : Hyperbox.getArgs()) {
                if (next) {
                    secMgr.setUserPassword(_User.ADMIN_ID, item.toCharArray());
                    log.info("The password has changed");
                    break;
                }
                if (item.equals("--reset-admin-pass")) {
                    next = true;
                }
            }
            if (!next) {
                log.error("You should give the new password after the '--reset-admin-pass' parameter.");
                System.exit(1);
            }

            System.exit(0);
        }

        taskMgr.start(this);
        sessMgr.start(this);
        storeMgr.start();

        loadHypervisors();

        if (HBoxServer.hasSetting(CFGKEY_CORE_HYP_ID)) {
            log.info("Loading Hypervisor configuration");
            HypervisorIn in = new HypervisorIn(HBoxServer.getSetting(CFGKEY_CORE_HYP_ID));
            in.setConnectionOptions(HBoxServer.getSetting(CFGKEY_CORE_HYP_OPTS));
            in.setAutoConnect(AxBooleans.get(HBoxServer.getSetting(CFGKEY_CORE_HYP_AUTO)));
            log.info("Hypervisor ID: " + in.getId());
            log.info("Hypervisor options: " + in.getConnectOptions());
            log.info("Hypervisor AutoConnect: " + in.getAutoConnect());
            if (in.getAutoConnect()) {
                log.info("Hyperbox will auto-connect to the Hypervisor");
                taskMgr.process(new Request(Command.HBOX, HyperboxTasks.HypervisorConnect, in));
            } else {
                log.info("Hypervisor is not set to auto-connect, skipping");
            }
        } else {
            log.info("No Hypervisor configuration found, skipping");
            VBoxWebSrv vBoxWebSrv = new VBoxWebSrv();
            try {
                String version = vBoxWebSrv.getVersion();
                log.info("Auto-detected VirtualBox version: {}", version);
                String hypVersion = "";
                if (version.startsWith("6.0")) hypVersion = VirtualBox.ID.WS_6_0;
                if (version.startsWith("6.1")) hypVersion = VirtualBox.ID.WS_6_1;

                if (StringUtils.isNotBlank(hypVersion)) {
                    log.info("Auto-connecting to detected VirtualBox installation");
                    HypervisorIn in = new HypervisorIn(hypVersion);
                    in.setAutoConnect(true);
                    taskMgr.process(new Request(Command.HBOX, HyperboxTasks.HypervisorConnect, in));
                }
            } catch (HypervisorException e) {
                log.info("No Virtualbox installation detected: {}", e.getMessage());
                log.debug("Details", e);
            }
        }

        setState(ServerState.Running);
    }

    @Override
    public void stop() {
        secMgr.authorize(SecurityItem.Server, SecurityAction.Stop);

        setState(ServerState.Stopping);

        if (sessMgr != null) {
            sessMgr.stop();
        }
        if (taskMgr != null) {
            taskMgr.stop();
        }

        if (isConnected()) {
            disconnect();
        }
        if (secMgr != null) {
            secMgr.stop();
        }
        if (actionMgr != null) {
            actionMgr.stop();
        }
        if (storeMgr != null) {
            storeMgr.stop();
        }

        if (persistor != null) {
            persistor.stop();
        }

        hypervisors.clear();
        setState(ServerState.Stopped);
        EventManager.stop();
    }

    @Override
    public _RequestReceiver getReceiver() {
        return sessMgr;
    }

    @Override
    public _TaskManager getTaskManager() {
        return taskMgr;
    }

    @Override
    public _SessionManager getSessionManager() {
        return sessMgr;
    }

    @Override
    public _SecurityManager getSecurityManager() {
        return secMgr;
    }

    @Override
    public _ActionManager getActionManager() {
        return actionMgr;
    }

    @Override
    public _StoreManager getStoreManager() {
        return storeMgr;
    }

    @Override
    public _ServerManager getServerManager() {
        return this;
    }

    @Override
    public _Persistor getPersistor() {
        return persistor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        HBoxServer.setSetting(CFGKEY_SRV_NAME, name);
        this.name = name;
        EventManager.post(new ServerPropertyChangedEvent(this, ServerAttribute.Name, name));
    }

    @Override
    public ServerType getType() {
        return ServerType.Host;
    }

    @Override
    public String getVersion() {
        return Hyperbox.getVersion().toString();
    }

    private void loadHypervisors() throws HyperboxException {
        Class<? extends _Hypervisor> vbox6_0 = io.kamax.hboxd.hypervisor.vbox6_0.VBoxWebServicesHypervisor.class;
        hypervisors.put(
                vbox6_0.getAnnotation(Hypervisor.class).id(),
                vbox6_0
        );

        Class<? extends _Hypervisor> vbox6_1 = io.kamax.hboxd.hypervisor.vbox6_1.VBoxWebServicesHypervisor.class;
        hypervisors.put(
                vbox6_1.getAnnotation(Hypervisor.class).id(),
                vbox6_1
        );
    }

    @Override
    public void connect(String hypervisorId, String options) {
        secMgr.authorize(SecurityItem.Hypervisor, SecurityAction.Connect);

        if (!hypervisors.containsKey(hypervisorId)) {
            throw new HyperboxException("No Hypervisor is registered under this ID: " + hypervisorId);
        }

        try {
            Class<? extends _Hypervisor> hypClass = hypervisors.get(hypervisorId);
            log.debug("Loading " + hypClass.getName() + " using " + hypClass.getClassLoader().getClass().getName());
            _Hypervisor hypervisor = hypervisors.get(hypervisorId).newInstance();
            hypervisor.setEventManager(EventManager.get());
            hypervisor.start(options);
            this.hypervisor = hypervisor;
            HBoxServer.setSetting(CFGKEY_CORE_HYP_ID, hypervisorId);
            HBoxServer.setSetting(CFGKEY_CORE_HYP_OPTS, options);
            HBoxServer.setSetting(CFGKEY_CORE_HYP_AUTO, 1);
            EventManager.post(new ServerConnectionStateEvent(this, ServerConnectionState.Connected));
        } catch (IllegalArgumentException | SecurityException | InstantiationException | IllegalAccessException e) {
            throw new HyperboxException("Hypervisor cannot be loaded due to bad format: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        secMgr.authorize(SecurityItem.Hypervisor, SecurityAction.Disconnect);

        if (isConnected()) {
            hypervisor.stop();
            hypervisor = null;
            EventManager.post(new ServerConnectionStateEvent(this, ServerConnectionState.Disconnected));
        }
    }

    @Override
    public boolean isConnected() {
        return ((hypervisor != null) && hypervisor.isRunning());
    }

    @Override
    public _Hypervisor getHypervisor() {
        if (!isConnected()) {
            throw new HypervisorNotConnectedException();
        }

        return hypervisor;
    }

    @Override
    public _Server getServer() {
        return this;
    }

    @Override
    public _Server getServer(String uuid) {
        if (!uuid.contentEquals(getId())) {
            throw new ServerNotFoundException(uuid);
        }
        return this;
    }

    @Override
    public List<_Server> listServer() {
        return Collections.singletonList(this);
    }

    @Override
    public List<Class<? extends _Hypervisor>> listHypervisors() {
        return new ArrayList<>(hypervisors.values());
    }

    private static class SystemClient implements _Client {

        @Override
        public void putAnswer(Answer ans) {
            // we ignore this
        }

        @Override
        public String getId() {
            return "System";
        }

        @Override
        public String getAddress() {
            return "";
        }

        @Override
        public void post(EventOut evOut) {
            log.debug("[ Event ] " + evOut);
        }

    }

    @Override
    public List<_Machine> listMachines() {
        if (!isConnected()) {
            throw new HyperboxException("Server is not connected");
        }

        List<_Machine> vms = new ArrayList<>();
        for (_RawVM rawVm : hypervisor.listMachines()) {
            if (secMgr.isAuthorized(SecurityItem.Machine, SecurityAction.List, rawVm.getUuid())) {
                vms.add(MachineFactory.get(this, hypervisor, rawVm));
            }
        }

        return vms;
    }

    @Override
    public _Machine getMachine(String id) {
        secMgr.authorize(SecurityItem.Machine, SecurityAction.Get, id);
        return MachineFactory.get(this, hypervisor, hypervisor.getMachine(id));
    }

    @Override
    public _Machine findMachine(String idOrName) {
        return getMachine(idOrName);
    }

    @Override
    public void unregisterMachine(String id) {
        hypervisor.unregisterMachine(id);
    }

    @Override
    public void deleteMachine(String id) {
        hypervisor.deleteMachine(id);
    }

    @Override
    public _Medium createMedium(String location, String format, Long logicalSize) {
        log.debug("Creating a new hard disk at location [" + location + "] with format [" + format + "] and size ["
                + logicalSize + "]");
        log.debug("File extension: " + Files.getFileExtension(location));
        if (Files.getFileExtension(location).isEmpty()) {
            log.debug("Will add extension to filename: " + format.toLowerCase());
            location = location + "." + format.toLowerCase();
        } else {
            log.debug("No need to add extension");
        }
        _RawMedium rawMed = hypervisor.createHardDisk(location, format, logicalSize);
        return new Medium(this, hypervisor, rawMed);
    }

    @Override
    public _Medium createMedium(String vmId, String filename, String format, Long logicalSize) {
        if (!new File(filename).isAbsolute()) {
            filename = getMachine(vmId).getLocation() + "/" + filename;
        }
        return createMedium(filename, format, logicalSize);
    }

    @Override
    public _Medium getMedium(String medId) {
        return new Medium(this, hypervisor, hypervisor.getMedium(medId));
    }

    @Override
    public _Host getHost() {
        if (!isConnected()) {
            throw new HyperboxException("Hypervisor is not connected - cannot retrieve host information");
        }

        return new Host(hypervisor.getHost());
    }

    @Handler
    protected void putHypervisorDisconnectEvent(HypervisorDisconnectedEvent ev) {
        if (ev.getHypervisor().equals(hypervisor) && isConnected()) {
            log.debug("Hypervisor disconnected, cleaning up");
            disconnect();
        }
    }

    @Override
    public void save() {
        // stub, nothing to save for now
    }

}
