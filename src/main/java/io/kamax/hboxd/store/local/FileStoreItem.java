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

package io.kamax.hboxd.store.local;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.exception.store.StoreItemIsNotContainerException;
import io.kamax.hboxd.store._Store;
import io.kamax.hboxd.store._StoreItem;

import java.io.File;
import java.util.List;

public final class FileStoreItem implements _StoreItem {

    private final _Store store;
    private final File location;

    public FileStoreItem(_Store store, File path) {
        if (!path.exists()) {
            throw new HyperboxException(path + " does not exist");
        }
        if (path.isDirectory()) {
            throw new HyperboxException(path + " is a folder");
        }
        if (!path.isAbsolute()) {
            throw new HyperboxException(path + " must be a full path");
        }

        this.store = store;
        location = new File(path.getAbsolutePath());

    }

    @Override
    public String getName() {
        return location.getName();
    }

    @Override
    public boolean isContainer() {
        return false;
    }

    @Override
    public String getPath() {
        return location.getAbsolutePath();
    }

    @Override
    public long getSize() {
        return location.length();
    }

    @Override
    public List<_StoreItem> listItems() {
        throw new StoreItemIsNotContainerException(location);
    }

    @Override
    public _Store getStore() {
        return store;
    }

}
