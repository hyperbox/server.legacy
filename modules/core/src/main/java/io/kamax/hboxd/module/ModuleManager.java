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

package io.kamax.hboxd.module;

import io.kamax.hbox.Configuration;
import io.kamax.hbox.exception.ModuleAlreadyRegisteredException;
import io.kamax.hbox.exception.ModuleException;
import io.kamax.hboxd.event.EventManager;
import io.kamax.hboxd.event.module.ModuleRegisteredEvent;
import io.kamax.hboxd.factory.ModuleFactory;
import io.kamax.tools.AxBooleans;
import io.kamax.tools.logging.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleManager implements _ModuleManager {

    private boolean isStarted = false;
    private String[] baseDirs = new String[0];
    private Map<String, _Module> modules = new HashMap<String, _Module>();

    @Override
    public void start() {
        baseDirs = Configuration.getSetting(CFGKEY_MODULE_BASEPATH, CFGVAL_MODULE_BASEPATH).split(File.pathSeparator);
        refreshModules();
        isStarted = true;
        EventManager.register(this);
        Logger.verbose("Module Manager has started");
    }

    @Override
    public void stop() {
        EventManager.unregister(this);
        isStarted = false;
        baseDirs = null;
        modules.clear();
        Logger.verbose("Module manager has stopped");
    }

    @Override
    public void refreshModules() {
        Logger.info("Refreshing modules...");

        Logger.debug("Number of base module directories: " + baseDirs.length);
        for (String baseDir : baseDirs) {
            File baseDirFile = new File(baseDir).getAbsoluteFile();
            Logger.info("Searching in " + baseDirFile.getAbsolutePath() + " for modules...");
            if (baseDirFile.isDirectory() && baseDirFile.canRead()) {
                Logger.debug(baseDirFile.getAbsolutePath() + " is a readable directory, processing...");
                for (File file : baseDirFile.listFiles()) {
                    if (isRegistered(file.getAbsolutePath())) {
                        Logger.verbose(file.getAbsolutePath() + " is already registered for " + modules.get(file.getAbsolutePath()).getName());
                        continue;
                    }
                    if (!file.isFile()) {
                        continue;
                    }
                    if (!file.canRead()) {
                        Logger.verbose(file.getAbsolutePath() + " is not readable, skipping.");
                        continue;
                    }
                    if (!file.getPath().endsWith("." + Configuration.getSetting(CFGKEY_MODULE_EXTENSION, CFGVAL_MODULE_EXTENSION))) {
                        Logger.verbose(file.getAbsolutePath() + " does not have the module extention, skipping.");
                        continue;
                    }
                    if (modules.containsKey(file.getAbsolutePath())) {
                        Logger.verbose(file.getAbsolutePath() + " is already registered as " + modules.get(file.getAbsolutePath()));
                        continue;
                    }

                    Logger.verbose("Usable module descriptor file detected: " + file.getAbsolutePath());
                    try {
                        registerModule(file.getAbsolutePath());
                    } catch (ModuleAlreadyRegisteredException e) {
                        Logger.verbose("Module is already registered, skipping.");
                    }
                }
            } else {
                Logger.error("Unable to refresh modules for Base Directory " + baseDirFile + ": either not a directory or cannot be read");
            }
        }
        Logger.info("Finished refreshing modules.");
    }

    @Override
    public void setModuleBasedir(String... basedir) {

        baseDirs = basedir;
        Logger.verbose("Module Base Dirs has been set to:");
        for (String s : basedir) {
            Logger.verbose("\t" + new File(s).getAbsolutePath());
        }
        if (isStarted) {
            refreshModules();
        }
    }

    @Override
    public Set<_Module> listModules() {
        return new HashSet<_Module>(modules.values());
    }

    @Override
    public Set<_Module> listModules(Class<?> type) {
        Set<_Module> mods = new HashSet<_Module>();
        for (_Module mod : modules.values()) {
            if (mod.isLoaded() && mod.getTypes().contains(type)) {
                mods.add(mod);
            }
        }
        return mods;
    }

    @Override
    public _Module getModule(String moduleId) {
        return modules.get(moduleId);
    }

    @Override
    public boolean isRegistered(String idOrDescriptorPath) {
        return modules.containsKey(idOrDescriptorPath) || modules.containsKey(new File(idOrDescriptorPath).getAbsolutePath());
    }

    protected boolean isRegistered(_Module mod) {
        return isRegistered(mod.getId()) || isRegistered(mod.getDescriptor());
    }

    @Override
    public _Module registerModule(String moduleDescFile) {

        Logger.info("Attempting to add module with descriptor file: " + moduleDescFile);
        File xmlFile = new File(moduleDescFile);
        _Module mod = ModuleFactory.get(xmlFile);

        if (isRegistered(mod.getId()) || isRegistered(mod.getDescriptor())) {
            throw new ModuleAlreadyRegisteredException(mod.getId());
        }

        modules.put(mod.getId(), mod);
        modules.put(mod.getDescriptor(), mod);

        Logger.info("Module ID " + mod.getId() + " (" + mod.getName() + ") was successfully registered");
        EventManager.post(new ModuleRegisteredEvent(mod));

        if (AxBooleans.get(Configuration.getSetting(CFGKEY_MODULE_AUTOLOAD, CFGVAL_MODULE_AUTOLOAD))) {
            try {
                mod.load();
                Logger.info("Module ID " + mod.getId() + " (" + mod.getName() + ") was autoloaded");
            } catch (ModuleException e) {
                Logger.warning("Module ID " + mod.getId() + " (" + mod.getName() + ") failed to autoload: " + e.getMessage());
            }
        }

        return mod;
    }

}
