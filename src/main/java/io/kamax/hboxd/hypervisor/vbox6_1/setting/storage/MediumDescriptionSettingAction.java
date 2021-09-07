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

package io.kamax.hboxd.hypervisor.vbox6_1.setting.storage;

import io.kamax.hbox.constant.MediumAttribute;
import io.kamax.hbox.exception.HyperboxException;
import io.kamax.hbox.hypervisor.vbox.settings.medium.MediumDescriptionSetting;
import io.kamax.hboxd.hypervisor.vbox6_1.setting._MediumSettingAction;
import io.kamax.tools.setting._Setting;
import org.virtualbox_6_1.IMedium;

public class MediumDescriptionSettingAction implements _MediumSettingAction {

    @Override
    public String getSettingName() {
        return MediumAttribute.Description.toString();
    }

    @Override
    public void set(IMedium medium, _Setting setting) {
        throw new HyperboxException("Read-only setting");
    }

    @Override
    public _Setting get(IMedium medium) {
        return new MediumDescriptionSetting(medium.getDescription());
    }

}
