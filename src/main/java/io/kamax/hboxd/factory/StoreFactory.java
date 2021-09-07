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

package io.kamax.hboxd.factory;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.store._Store;
import io.kamax.hboxd.store.local.FolderStore;

import java.io.File;

public class StoreFactory {

    private StoreFactory() {
        throw new RuntimeException("Not allowed");
    }

    public static _Store get(String moduleId, String storeId, String storeName, File storePath) {
        if (moduleId.equalsIgnoreCase("localFolder")) {
            return new FolderStore(storeId, storeName, storePath);
        } else {
            throw new HyperboxException("Unsupported Store Type : " + moduleId);
        }
    }

    public static _Store get(String moduleId, String storeId, String storeName, String storePath) {
        return get(moduleId, storeId, storeName, new File(storePath));
    }

}
