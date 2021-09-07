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

package io.kamax.hboxd.comm.io.factory;

import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.comm.out.storage.MediumOut;
import io.kamax.hboxd.core.model._Medium;
import io.kamax.hboxd.hypervisor.storage._RawMedium;

public class MediumIoFactory {

    private MediumIoFactory() {
        // static class
    }

    public static MediumOut get(_RawMedium m) {
        MediumOut mIo = new MediumOut(m.getUuid(), SettingIoFactory.getList(m.listSettings()));
        return mIo;
    }

    public static MediumOut get(_Medium m) {
        MediumOut mIo = new MediumOut(m.getUuid(), SettingIoFactory.getList(m.getSettings()));
        return mIo;
    }

}
