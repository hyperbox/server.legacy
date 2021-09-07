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

package io.kamax.hboxd.hypervisor.vbox6_0.setting.audio;

import io.kamax.hbox.constant.AudioController;
import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.hypervisor.vbox.settings.audio.AudioControllerSetting;
import io.kamax.hboxd.hypervisor.vbox6_0.data.Mappings;
import io.kamax.hboxd.hypervisor.vbox6_0.setting._MachineSettingAction;
import io.kamax.tools.setting.StringSetting;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_0.AudioControllerType;
import org.virtualbox_6_0.IMachine;
import org.virtualbox_6_0.LockType;

public final class AudioControllerSettingAction implements _MachineSettingAction {

    @Override
    public LockType getLockType() {
        return LockType.Write;
    }

    @Override
    public String getSettingName() {
        return MachineAttribute.AudioController.toString();
    }

    @Override
    public void set(IMachine machine, _Setting setting) {
        String rawAudioCtrl = ((StringSetting) setting).getValue();
        AudioController audioCtrl = AudioController.valueOf(rawAudioCtrl);
        AudioControllerType audioCtrlType = Mappings.get(audioCtrl);
        machine.getAudioAdapter().setAudioController(audioCtrlType);
    }

    @Override
    public _Setting get(IMachine machine) {
        return new AudioControllerSetting(Mappings.get(machine.getAudioAdapter().getAudioController()));
    }

}
