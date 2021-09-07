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

import io.kamax.hbox.comm.io.NATRuleIO;
import io.kamax.hbox.comm.io.NetService_NAT_IO;
import io.kamax.hbox.comm.io.NetService_NAT_IP4_IO;
import io.kamax.hbox.constant.NetServiceType;
import io.kamax.hbox.constant.NetworkInterfaceAttribute;
import io.kamax.hbox.hypervisor.vbox.net._NATRule;
import io.kamax.hbox.hypervisor.vbox.net._NetService;
import io.kamax.hbox.hypervisor.vbox.settings.network.*;
import io.kamax.hboxd.hypervisor.vbox6_1.VBox;
import io.kamax.hboxd.hypervisor.vbox6_1.manager.VBoxSettingManager;
import io.kamax.hboxd.hypervisor.vm.device._RawNetworkInterface;
import io.kamax.tools.setting.BooleanSetting;
import io.kamax.tools.setting.StringSetting;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_1.INetworkAdapter;
import org.virtualbox_6_1.NATProtocol;
import org.virtualbox_6_1.NetworkAttachmentType;

import java.util.Collections;
import java.util.List;

public class VBoxNetworkInterface implements _RawNetworkInterface {

    private final String machineUuid;
    private final long nicIndex;

    public VBoxNetworkInterface(VBoxMachine machine, long index) {
        this(machine.getUuid(), index);
    }

    public VBoxNetworkInterface(String machineUuid, long nicIndex) {
        this.machineUuid = machineUuid;
        this.nicIndex = nicIndex;
    }

    public INetworkAdapter getRaw() {
        return VBox.get().findMachine(machineUuid).getNetworkAdapter(nicIndex);
    }

    @Override
    public String getMachineUuid() {
        return machineUuid;
    }

    @Override
    public long getNicId() {
        return nicIndex;
    }

    @Override
    public boolean isEnabled() {
        return ((BooleanSetting) getSetting(NetworkInterfaceAttribute.Enabled)).getValue();
    }

    @Override
    public void setEnabled(boolean isEnabled) {
        setSetting(new NicEnabledSetting(isEnabled));
    }

    @Override
    public String getMacAddress() {
        return ((StringSetting) getSetting(NetworkInterfaceAttribute.MacAddress)).getValue();
    }

    @Override
    public void setMacAddress(String macAddress) {
        setSetting(new NicMacAddressSetting(macAddress));
    }

    @Override
    public boolean isCableConnected() {
        return ((BooleanSetting) getSetting(NetworkInterfaceAttribute.CableConnected)).getValue();
    }

    @Override
    public void setCableConnected(boolean isConnected) {
        setSetting(new NicCableConnectedSetting(isConnected));
    }

    @Override
    public String getAttachMode() {
        return ((StringSetting) getSetting(NetworkInterfaceAttribute.AttachMode)).getValue();
    }

    @Override
    public void setAttachMode(String attachMode) {
        setSetting(new NicAttachModeSetting(attachMode));
    }

    @Override
    public String getAttachName() {
        return ((StringSetting) getSetting(NetworkInterfaceAttribute.AttachName)).getValue();
    }

    @Override
    public void setAttachName(String attachName) {
        setSetting(new NicAttachNameSetting(attachName));
    }

    @Override
    public String getAdapterType() {
        return ((StringSetting) getSetting(NetworkInterfaceAttribute.AdapterType)).getValue();
    }

    @Override
    public void setAdapterType(String adapterType) {
        setSetting(new NicAdapterTypeSetting(adapterType));
    }

    @Override
    public List<_Setting> listSettings() {
        return VBoxSettingManager.list(this);
    }

    @Override
    public _Setting getSetting(Object getName) {
        return VBoxSettingManager.get(this, getName);
    }

    @Override
    public void setSetting(_Setting s) {
        setSetting(Collections.singletonList(s));
    }

    @Override
    public void setSetting(List<_Setting> s) {
        VBoxSettingManager.set(this, s);
    }

    @Override
    public List<_NetService> getServices() {
        if (NetworkAttachmentType.NAT.equals(getRaw().getAttachmentType())) {
            return Collections.singletonList(getService(NetServiceType.NAT_IPv4.getId()));
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public void setService(_NetService svc) {
        INetworkAdapter nicRaw = getRaw();

        if (NetServiceType.NAT_IPv4.equals(svc.getType())) {
            NetService_NAT_IO svcNatIp4 = (NetService_NAT_IO) svc;
            for (String ruleRaw : nicRaw.getNATEngine().getRedirects()) {
                nicRaw.getNATEngine().removeRedirect(ruleRaw.split(",")[0]);
            }
            for (_NATRule rule : svcNatIp4.getRules()) {
                nicRaw.getNATEngine().addRedirect(rule.getName(), NATProtocol.valueOf(rule.getProtocol().toUpperCase()), rule.getPublicIp(),
                        Integer.parseInt(rule.getPublicPort()), rule.getPrivateIp(), Integer.parseInt(rule.getPrivatePort()));
            }
        } else {
            throw new IllegalArgumentException("Service type " + svc.getType() + " is not supported on " + nicRaw.getAdapterType() + " adaptor type");
        }
    }

    @Override
    public _NetService getService(String serviceTypeId) {
        INetworkAdapter nicRaw = getRaw();

        if (NetworkAttachmentType.NAT.equals(nicRaw.getAdapterType()) && NetServiceType.NAT_IPv4.equals(serviceTypeId)) {
            NetService_NAT_IP4_IO svc = new NetService_NAT_IP4_IO(true);
            for (String ruleRaw : nicRaw.getNATEngine().getRedirects()) {
                String[] ruleRawSplit = ruleRaw.split(",");
                svc.addRule(new NATRuleIO(ruleRawSplit[0], ruleRawSplit[1], ruleRawSplit[2], ruleRawSplit[3], ruleRawSplit[4], ruleRawSplit[5]));
            }
            return svc;
        } else {
            throw new IllegalArgumentException("Service type " + serviceTypeId + " is not supported on " + nicRaw.getAdapterType() + " adaptor type");
        }
    }

}
