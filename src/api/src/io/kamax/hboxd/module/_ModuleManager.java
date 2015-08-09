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

import java.util.Set;

/**
 * A Module Manager is in charge of searching, registering, unregistering and related actions for modules.<br/>
 * The Module manager use a base directory where it will look for modules using their descriptor files.<br/>
 * <p>
 * A descriptor file is an XML file that give all required info about a module including, but not limited to:
 * <ul>
 * <li>Module ID which must be unique</li>
 * <li>Module Name</li>
 * <li>Base directory of the module</li>
 * <li>Providers included in the module</li>
 * </ul>
 * </p>
 * <p>
 * The file can have any name but must use the extension configured as the setting key {@value #CFGKEY_MODULE_EXTENSION} and must to be located in the
 * directory configured as the setting key {@value #CFGKEY_MODULE_BASEPATH} to be automatically picked up.
 * </p>
 * <p>
 * If supported by the Module Manager, newly detected modules can be auto-loaded at startup using the following config key:
 * {@value #CFGKEY_MODULE_AUTOLOAD}
 * </p>
 * 
 * @author max
 * 
 */
public interface _ModuleManager {

   /**
    * Configuration Key for Module Auto-loading.
    */
   String CFGKEY_MODULE_AUTOLOAD = "server.module.autoload";
   /**
    * Default value for {@value #CFGKEY_MODULE_AUTOLOAD}
    */
   String CFGVAL_MODULE_AUTOLOAD = "1";

   /**
    * Configuration Key for Module Descriptor file extension
    */
   String CFGKEY_MODULE_EXTENSION = "server.module.extension";
   /**
    * Default value for {@value #CFGKEY_MODULE_EXTENSION}
    */
   String CFGVAL_MODULE_EXTENSION = "xml";

   /**
    * <p>
    * Configuration Key for Module search directory.
    * </p>
    * If a relative path is given, it will be from the installation directory of Hyperbox
    */
   String CFGKEY_MODULE_BASEPATH = "server.module.dir";
   /**
    * Default value for {@value #CFGKEY_MODULE_BASEPATH}
    */
   String CFGVAL_MODULE_BASEPATH = "modules";

   /**
    * Start the module manager.
    * <p>
    * If supported, the Module Manager will also refresh the modules in the base directories or fail silently. If Auto-load is supported, modules
    * found during the refresh will be loaded.
    * </p>
    */
   public void start();

   /**
    * Stop the module manager.
    * <p>
    * All loaded modules will be unloaded and any references to them cleared.
    * </p>
    */
   public void stop();

   /**
    * Scan the base directories for new modules. Does not affected already loaded modules.
    */
   public void refreshModules();

   /**
    * Change the base directories for modules.
    * <p>
    * Modules will be refreshed using {{@link #refreshModules()} if the manager is started.
    * </p>
    * 
    * @param basedir The new base directories for module search
    */
   public void setModuleBasedir(String... basedir);

   /**
    * List all loaded modules.
    * 
    * @return a Set of loaded modules
    */
   public Set<_Module> listModules();

   /**
    * List all loaded modules being of a specific kind.
    * 
    * @param type The kind this module should be of, as a Class
    * @return the matching list of loaded modules
    */
   public Set<_Module> listModules(Class<?> type);

   /**
    * Get the module information given its ID.
    * 
    * @param moduleId The Module ID
    * @return the Module object or null if no such module is loaded
    */
   public _Module getModule(String moduleId);

   /**
    * Load a module given its descriptor file path.
    * 
    * @param path Relative path to the base directories or an absolute path.
    * @return The loaded module or null if no module was found at the given path.
    */
   public _Module registerModule(String path);

   /**
    * Checks if the module ID or Descriptor File path is already registered.
    * 
    * @param idOrDescriptorPath Module ID or absolute Descriptor File path.
    * @return true if this ID/path represents a registered module, false if not.
    */
   public boolean isRegistered(String idOrDescriptorPath);

}
