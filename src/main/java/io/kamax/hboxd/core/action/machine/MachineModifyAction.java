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

package io.kamax.hboxd.core.action.machine;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.*;
import io.kamax.hbox.comm.io.factory.SettingIoFactory;
import io.kamax.hbox.constant.MachineAttribute;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.core.model.*;
import io.kamax.hboxd.server._Server;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public final class MachineModifyAction extends ASingleTaskAction {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    private void createMedium(_Server srv, _Machine vm, _StorageController sc, StorageDeviceAttachmentIn sdaIn) {

        _Medium med = srv
                .createMedium(vm.getUuid(), sdaIn.getMedium().getLocation(), sdaIn.getMedium().getFormat(), sdaIn.getMedium().getLogicalSize());
        sc.attachMedium(med, sdaIn.getPortId(), sdaIn.getDeviceId());
    }

    private void replaceMedium(_Server srv, _StorageController sc, StorageDeviceAttachmentIn sdaIn) {

        _Medium med = srv.getMedium(sdaIn.getMedium().getUuid());
        sc.attachMedium(med, sdaIn.getPortId(), sdaIn.getDeviceId());
    }

    private void removeMedium(_StorageController sc, StorageDeviceAttachmentIn sdaIn) {

        sc.detachMedium(sdaIn.getPortId(), sdaIn.getDeviceId());
    }

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.MachineModify.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        ServerIn srvIn = request.get(ServerIn.class);
        MachineIn mIn = request.get(MachineIn.class);
        mIn.removeSetting(MachineAttribute.ServerId);

        _Server srv = hbox.getServer(srvIn.getId());
        _Machine vm = srv.getMachine(mIn.getId());

        boolean success = false;

        vm.lock();

        try {
            vm.setSetting(SettingIoFactory.getListIo(mIn.listSettings()));

            for (DeviceIn devIn : mIn.listDevice()) {
                vm.getDevice(devIn.getId()).setSetting(SettingIoFactory.getListIo(devIn.listSettings()));
            }

            for (NetworkInterfaceIn nIn : mIn.listNetworkInterface()) {
                _NetworkInterface nic = vm.getNetworkInterface(nIn.getNicId());
                nic.setSetting(SettingIoFactory.getListIo(nIn.listSettings()));
            }

            for (StorageControllerIn scIn : mIn.listStorageController()) {
                _StorageController sc = null;

                if (scIn.getAction().equals(Action.Delete) || scIn.getAction().equals(Action.Replace)) {
                    vm.removeStorageController(scIn.getId());
                }
                if (scIn.getAction().equals(Action.Create) || scIn.getAction().equals(Action.Replace)) {
                    sc = vm.addStorageController(scIn.getType(), scIn.getId());
                }
                if (scIn.getAction().equals(Action.Create) || scIn.getAction().equals(Action.Modify) || scIn.getAction().equals(Action.Replace)) {
                    sc = vm.getStorageController(scIn.getId());
                    sc.setSetting(SettingIoFactory.getListIo(scIn.listSettings()));

                    for (StorageDeviceAttachmentIn sdaIn : scIn.listAttachments()) {
                        if (sdaIn.getAction().equals(Action.Delete)) {
                            sc.detachDevice(sdaIn.getPortId(), sdaIn.getDeviceId());
                        }

                        if (sdaIn.getAction().equals(Action.Create)) {
                            sc.attachDevice(sdaIn.getDeviceType(), sdaIn.getPortId(), sdaIn.getDeviceId()); // TODO evaluate if still needed
                            if (sdaIn.hasMedium()) {
                                if (sdaIn.getMedium().getAction().equals(Action.Create)) {
                                    createMedium(srv, vm, sc, sdaIn);
                                } else {
                                    replaceMedium(srv, sc, sdaIn);
                                }
                            }
                        }
                        if (sdaIn.getAction().equals(Action.Modify)) {
                            _MediumAttachment medAtt = sc.getMediumAttachment(sdaIn.getPortId(), sdaIn.getDeviceId());
                            if (medAtt == null) {

                                SessionContext.getClient().putAnswer(
                                        new Answer(request, AnswerType.WARNING, "Trying to modify a storage attachment that doesn't exist, skipping"));
                            } else {
                                // We either want to replace or create or change nothing if UUID are the same
                                if (sdaIn.hasMedium() && (sdaIn.getMedium().getAction() == Action.Create)) {
                                    createMedium(srv, vm, sc, sdaIn);
                                }

                                // We attach since there is nothing attached yet
                                else if (sdaIn.hasMedium() && !medAtt.hasMedium()) {
                                    replaceMedium(srv, sc, sdaIn);
                                }

                                // We only want to modify if the UUID is different
                                else if (sdaIn.hasMedium() && (sdaIn.getMedium().getAction() == Action.Modify)
                                        && !sdaIn.getMedium().getUuid().contentEquals(medAtt.getMediumId())) {
                                    replaceMedium(srv, sc, sdaIn);
                                }

                                // We want to remove the current medium if the ACtion is set to Delete or if the input has no medium
                                // TODO need explicit change via Action.Delete or Action.Replace
                                else if ((sdaIn.hasMedium() && (sdaIn.getMedium().getAction() == Action.Delete)) || (medAtt.hasMedium() && !sdaIn.hasMedium())) {
                                    removeMedium(sc, sdaIn);
                                } else {
                                    log.debug("No medium in the current config or the input, skipping");
                                }
                            }
                        }
                    }
                }
            }
            success = true;
        } finally {
            vm.unlock(success);
        }
    }

}
