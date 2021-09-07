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

package io.kamax.hboxd.hypervisor.vbox6_0.setting.machine;

import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.exception.ConfigurationException;
import io.kamax.hbox.hypervisor.vbox.settings.general.CurrentSnapshotSetting;
import io.kamax.hboxd.hypervisor.vbox6_0.setting._MachineSettingAction;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_0.IMachine;
import org.virtualbox_6_0.LockType;

public class CurrentSnapshotSettingAction implements _MachineSettingAction {

    @Override
    public LockType getLockType() {
        return LockType.Shared;
    }

    @Override
    public String getSettingName() {
        return MachineAttribute.CurrentSnapshotUuid.getId();
    }

    @Override
    public void set(IMachine machine, _Setting setting) {
        throw new ConfigurationException("Read-only attribute: " + setting.getName());
    }

    @Override
    public _Setting get(IMachine machine) {
        if (machine.getSnapshotCount() != 0) {
            return new CurrentSnapshotSetting(machine.getCurrentSnapshot().getId());
        } else {
            return new CurrentSnapshotSetting("");
        }
    }

}
