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

package io.kamax.hboxd.store;

import io.kamax.hbox.comm.SecurityAction;
import io.kamax.hbox.comm.SecurityItem;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.factory.StoreFactory;
import io.kamax.hboxd.persistence._StorePersistor;
import io.kamax.hboxd.security.SecurityContext;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StoreManager implements _StoreManager {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private _StorePersistor persistor;

    private final StoreIdGenerator IdGen = new StoreIdGenerator();
    private final Map<String, _Store> stores = new HashMap<>();

    private void saveStores() {

        log.debug("Saving stores config");
        for (_Store s : stores.values()) {
            persistor.updateStore(s);
        }
    }

    @Override
    public void init(_StorePersistor persistor) throws HyperboxException {
        this.persistor = persistor;
    }

    @Override
    public void start() throws HyperboxException {

        log.debug("Filesystem Store Manager is starting...");
        List<_Store> storeList = persistor.listStores();
        for (_Store store : storeList) {
            stores.put(store.getId(), store);
        }

        // We assume this is the first launch
        // TODO check in a more effective way that this is the first launch
        if (stores.isEmpty()) {
            for (File rootFs : File.listRoots()) {
                if (rootFs.getAbsolutePath().contentEquals("/")) {
                    // We are on a UNIX-type of OS
                    registerStore(rootFs.getAbsolutePath(), "Root");
                } else {
                    // We are on Windows
                    if (rootFs.isDirectory()) {
                        // Empty drives (CD/DVD drives) are skipped, they are not considered as directories
                        registerStore(rootFs.getAbsolutePath(), rootFs.getAbsolutePath().split(":", 2)[0]);
                    }
                }

            }
        }
        log.debug("Filesystem Store Manager started");
    }

    @Override
    public void stop() {

        log.debug("Filesystem Store Manager is stopping...");
        saveStores();
        log.debug("Filesystem Store Manager stopped");
    }

    @Override
    public List<_Store> listStores() {
        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.List);

        return new ArrayList<>(stores.values());
    }

    @Override
    public _Store getStore(String id) {
        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.Get, id);

        if (!stores.containsKey(id)) {
            throw new HyperboxException("Cannot find a Store with \"" + id + "\" as ID");
        }

        return stores.get(id);
    }

    @Override
    public _Store createStore(String location, String label) {

        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.Create);

        File path = new File(location);

        if (path.exists()) {
            throw new HyperboxException("This path already exist, cannot create the store");
        }
        if (!path.mkdirs()) {
            throw new HyperboxException("Unable to create the store, folder creation failed");
        }

        return registerStore(location, label);
    }

    @Override
    public _Store registerStore(String location, String label) {

        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.Add);

        File path = new File(location);

        log.debug("Trying to register " + path.getAbsolutePath() + " under " + label);
        if (!path.exists()) {
            throw new HyperboxException("Store location must exist");
        }
        if (!path.isDirectory()) {
            throw new HyperboxException("Store location must represent a directory");
        }
        if (!path.isAbsolute()) {
            throw new HyperboxException("Store location must be an absolute path");
        }

        // TODO implement registration event
        _Store s = StoreFactory.get("localFolder", IdGen.get(), label, path);
        persistor.insertStore(s);
        stores.put(s.getId(), s);
        persistor.updateStore(s);
        return s;
    }

    @Override
    public void unregisterStore(String id) {

        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.Delete, id);

        _Store s = getStore(id);
        persistor.deleteStore(s);
        stores.remove(s.getId());
        // TODO implement registration event
    }

    @Override
    public void deleteStore(String id) {

        SecurityContext.get().authorize(SecurityItem.Store, SecurityAction.Delete, id);

        if (!new File(getStore(id).getLocation()).delete()) {
            throw new HyperboxException("Unable to delete the store");
        }
        unregisterStore(id);
    }

    private class StoreIdGenerator {

        private Integer nextId = 1;

        public String get() {

            while (stores.containsKey(nextId.toString())) {
                nextId++;
            }
            return nextId.toString();
        }
    }

}
