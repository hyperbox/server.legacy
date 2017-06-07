/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Maxime Dor
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

package io.kamax.hboxd.core.action.device;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.DeviceIn;
import io.kamax.hbox.comm.in.MachineIn;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.comm.io.SettingIO;
import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tools.setting._Setting;

import java.util.Arrays;
import java.util.List;

public class DevicePropertyListAction extends AbstractHyperboxMultiTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.DevicePropertyList.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        ServerIn srvIn = request.get(ServerIn.class);
        MachineIn mIn = request.get(MachineIn.class);
        DeviceIn dIn = request.get(DeviceIn.class);

        List<_Setting> settings = hbox.getServer(srvIn.getId()).getMachine(mIn.getUuid()).getDevice(dIn.getId()).getSettings();
        List<SettingIO> settingsIo = SettingIoFactory.getList(settings);
        for (SettingIO settingIo : settingsIo) {
            SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, SettingIO.class, settingIo));
        }
    }

}
