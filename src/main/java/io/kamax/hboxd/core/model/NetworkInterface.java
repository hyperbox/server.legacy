/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2014 Max Dor
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

package io.kamax.hboxd.core.model;

import io.kamax.hbox.constant.EntityType;
import io.kamax.hbox.hypervisor.vbox.net._NetService;
import io.kamax.hboxd.hypervisor.vm.device._RawNetworkInterface;
import io.kamax.tools.setting._Setting;

import java.util.List;

public class NetworkInterface implements _NetworkInterface {

    private final _Machine vm;
    private final _RawNetworkInterface rawNic;

    public NetworkInterface(_Machine vm, _RawNetworkInterface rawNic) {
        this.vm = vm;
        this.rawNic = rawNic;
    }

    @Override
    public EntityType getType() {
        return EntityType.NetworkInterace;
    }

    @Override
    public List<_Setting> getSettings() {
        // TODO add security check
        return rawNic.listSettings();
    }

    @Override
    public _Setting getSetting(String settingId) {
        // TODO add security check
        return rawNic.getSetting(settingId);
    }

    @Override
    public void setSetting(_Setting setting) {
        // TODO add security check
        rawNic.setSetting(setting);
    }

    @Override
    public void setSetting(List<_Setting> settings) {
        // TODO add security check
        rawNic.setSetting(settings);
    }

    @Override
    public boolean hasSetting(String settingId) {
        // TODO add security check
        try {
            rawNic.getSetting(settingId);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public String getId() {
        // TODO add security check
        return vm.getUuid() + rawNic.getNicId();
    }

    @Override
    public _Machine getMachine() {
        // TODO add security check
        return vm;
    }

    @Override
    public long getNicId() {
        // TODO add security check
        return rawNic.getNicId();
    }

    @Override
    public boolean isEnabled() {
        // TODO add security check
        return rawNic.isEnabled();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        // TODO add security check
        rawNic.setEnabled(isEnabled);
    }

    @Override
    public String getMacAddress() {
        // TODO add security check
        return rawNic.getMacAddress();
    }

    @Override
    public void setMacAddress(String macAddress) {
        // TODO add security check
        rawNic.setMacAddress(macAddress);
    }

    @Override
    public boolean isCableConnected() {
        // TODO add security check
        return rawNic.isCableConnected();
    }

    @Override
    public void setCableConnected(boolean isConnected) {
        // TODO add security check
        rawNic.setCableConnected(isConnected);
    }

    @Override
    public String getAttachMode() {
        // TODO add security check
        return rawNic.getAttachMode();
    }

    @Override
    public void setAttachMode(String attachMode) {
        // TODO add security check
        rawNic.setAttachMode(attachMode);
    }

    @Override
    public String getAttachName() {
        // TODO add security check
        return rawNic.getAttachName();
    }

    @Override
    public void setAttachName(String attachName) {
        // TODO add security check
        rawNic.setAttachName(attachName);
    }

    @Override
    public String getAdapterType() {
        // TODO add security check
        return rawNic.getAdapterType();
    }

    @Override
    public void setAdapterType(String adapterType) {
        // TODO add security check
        rawNic.setAdapterType(adapterType);
    }

    @Override
    public List<_NetService> getServices() {
        return rawNic.getServices();
    }

    @Override
    public void setService(_NetService svc) {
        rawNic.setService(svc);
    }

    @Override
    public _NetService getService(String serviceTypeId) {
        return rawNic.getService(serviceTypeId);
    }

}
