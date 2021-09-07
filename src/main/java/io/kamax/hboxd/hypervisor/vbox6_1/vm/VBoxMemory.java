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

package io.kamax.hboxd.hypervisor.vbox6_1.vm;

import io.kamax.hbox.constant.EntityType;
import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hbox.hypervisor.vbox.settings.memory.*;
import io.kamax.hboxd.hypervisor.vbox6_1.manager.VBoxSettingManager;
import io.kamax.hboxd.hypervisor.vm.device._RawMemory;
import io.kamax.tools.setting.BooleanSetting;
import io.kamax.tools.setting.PositiveNumberSetting;
import io.kamax.tools.setting._Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VBoxMemory implements _RawMemory {

    private final VBoxMachine machine;

    public VBoxMemory(VBoxMachine machine) {
        this.machine = machine;
    }

    @Override
    public long getAmount() {
        return ((PositiveNumberSetting) machine.getSetting(MachineAttribute.Memory)).getValue();
    }

    @Override
    public void setAmount(long amount) {
        setSetting(new MemorySetting(amount));
    }

    @Override
    public boolean isLargePageEnabled() {
        return ((BooleanSetting) machine.getSetting(MachineAttribute.LargePages)).getValue();
    }

    @Override
    public void setLargePage(boolean isEnabled) {
        setSetting(new LargePagesSetting(isEnabled));
    }

    @Override
    public boolean isPageFusionEnabled() {
        return ((BooleanSetting) machine.getSetting(MachineAttribute.PageFusion)).getValue();
    }

    @Override
    public void setPageFusion(boolean isEnabled) {
        setSetting(new PagefusionSetting(isEnabled));
    }

    @Override
    public boolean isNestedPagingEnabled() {
        return ((BooleanSetting) machine.getSetting(MachineAttribute.NestedPaging)).getValue();
    }

    @Override
    public void setNestedPaging(boolean isEnabled) {
        setSetting(new NestedPagingSetting(isEnabled));
    }

    @Override
    public boolean isVTxvpidEnabled() {
        return ((BooleanSetting) machine.getSetting(MachineAttribute.Vtxvpid)).getValue();
    }

    @Override
    public void setVtxvpid(boolean isEnabled) {
        setSetting(new VtxvpidSetting(isEnabled));
    }

    @Override
    public List<_Setting> listSettings() {
        List<_Setting> settings = new ArrayList<>();
        for (MachineAttribute setting : MachineAttribute.values()) {
            if (setting.getDeviceType().equals(EntityType.Memory)) {
                getSetting(setting);
            }
        }
        return settings;
    }

    @Override
    public _Setting getSetting(Object getName) {
        return VBoxSettingManager.get(machine, getName);
    }

    @Override
    public void setSetting(_Setting s) {
        machine.setSetting(Arrays.asList(s));
    }

    @Override
    public void setSetting(List<_Setting> s) {
        machine.setSetting(s);
    }

}
