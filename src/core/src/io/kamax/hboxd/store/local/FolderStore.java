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

package io.kamax.hboxd.store.local;

import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hboxd.store._Store;
import io.kamax.hboxd.store._StoreItem;
import java.io.File;

public final class FolderStore implements _Store {

    private String id;
    private String name;
    private File location;

    public FolderStore(String id, String name, File path) {
        if (!path.exists()) {
            throw new HyperboxException(location.getAbsolutePath() + " does not exist");
        }
        if (!path.isDirectory()) {
            throw new HyperboxException(location.getAbsolutePath() + " is not a folder");
        }
        if (!path.isAbsolute()) {
            throw new HyperboxException(location.getAbsolutePath() + " must be a full path");
        }

        this.id = id;
        this.name = name;
        location = new File(path.getAbsolutePath());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "localFolder";
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public _StoreItem getContainer() {
        return new FolderStoreItem(this, location);
    }

    @Override
    public String getLocation() {
        return location.getAbsolutePath();
    }

    @Override
    public _StoreItem getItem(String path) {
        File newItemPath = path.startsWith(getLocation()) ? new File(path) : new File(getLocation() + path);
        if (!newItemPath.exists()) {
            throw new HyperboxException(newItemPath.getAbsolutePath() + " is not a valid location");
        }
        if (!newItemPath.canRead()) {
            throw new HyperboxException(newItemPath.getAbsolutePath() + " is not readable");
        }
        if (newItemPath.isDirectory()) {
            return new FolderStoreItem(this, newItemPath);
        } else {
            return new FileStoreItem(this, newItemPath);
        }
    }

}
