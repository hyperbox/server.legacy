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

package io.kamax.hboxd.core.action.medium;

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.MediumIn;
import io.kamax.hbox.comm.out.storage.MediumOut;
import io.kamax.hbox.constant.MediumAttribute;
import io.kamax.hboxd.comm.io.factory.MediumIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tools.AxStrings;

import java.util.Arrays;
import java.util.List;

public final class MediumGetAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.MediumGet.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {

        MediumIn medIn = request.get(MediumIn.class);

        MediumOut medOut = null;
        if (!AxStrings.isEmpty(medIn.getUuid())) {
            medOut = MediumIoFactory.get(hbox.getHypervisor().getMedium(medIn.getUuid()));
        } else {
            medOut = MediumIoFactory.get(hbox.getHypervisor().getMedium(medIn.getLocation(), medIn.getSetting(MediumAttribute.Type).getString()));
        }
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, medOut));
    }

}
