/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2018 Kamax Sarl
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

package io.kamax.hboxd.hypervisor.vbox6_1.setting.snapshot;

import io.kamax.hbox.constant.SnapshotAttribute;
import io.kamax.hbox.exception.ConfigurationException;
import io.kamax.hbox.hypervisor.vbox.settings.snapshot.SnapshotIsOnlineSetting;
import io.kamax.hboxd.hypervisor.vbox6_1.setting._SnapshotSettingAction;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_1.ISnapshot;
import org.virtualbox_6_1.LockType;

public class SnapshotIsOnlineSettingAction implements _SnapshotSettingAction {

    @Override
    public LockType getLockType() {
        return LockType.Shared;
    }

    @Override
    public String getSettingName() {
        return SnapshotAttribute.IsOnline.getId();
    }

    @Override
    public void set(ISnapshot snap, _Setting setting) {
        throw new ConfigurationException("Read-only setting [" + setting.getName() + "]");
    }

    @Override
    public _Setting get(ISnapshot snap) {
        return new SnapshotIsOnlineSetting(snap.getOnline());
    }

}
