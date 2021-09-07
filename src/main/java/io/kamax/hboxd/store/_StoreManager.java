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

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.persistence._StorePersistor;

import java.util.List;

public interface _StoreManager {

    void init(_StorePersistor persistor) throws HyperboxException;

    void start() throws HyperboxException;

    void stop();

    List<_Store> listStores();

    _Store getStore(String id);

    /**
     * Will create the store using the location given, register it under the given label and open the store
     *
     * @param location Location of the store
     * @param label    The label to apply
     * @return The newly created & registered Store
     */
    _Store createStore(String location, String label);

    /**
     * Will register the store using the path given under the given label and open the store
     *
     * @param location Full path for the store - must be a directory
     * @param label    The label to apply
     * @return The newly registered Store
     */
    _Store registerStore(String location, String label);

    /**
     * <p>
     * Will close the store and unregister it from the store list.<br/>
     * This operation will not delete the actual implementation.
     * </p>
     *
     * @param id The Store ID to unregister
     */
    void unregisterStore(String id);

    /**
     * <p>
     * Will close the store, unregister it and try to delete the implementation.<br/>
     * This call could fail if the store is not empty and the implementation doesn't allow the removal of non-empty containers.<br/>
     * </p>
     *
     * @param id The Store ID to unregister & delete
     */
    void deleteStore(String id);

}
