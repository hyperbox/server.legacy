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

import com.google.common.io.Files;
import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.MediumIn;
import io.kamax.hbox.comm.in.ServerIn;
import io.kamax.hbox.comm.out.storage.MediumOut;
import io.kamax.hboxd.comm.io.factory.MediumIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.core.model._Medium;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.tools.logging.KxLog;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public final class MediumCreateAction extends ASingleTaskAction {

    private static final Logger log = KxLog.make(MethodHandles.lookup().lookupClass());

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.VBOX.getId() + HypervisorTasks.MediumCreate.getId());
    }

    @Override
    public boolean isQueueable() {
        return true;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        ServerIn srvIn = request.get(ServerIn.class);
        MediumIn medIn = request.get(MediumIn.class);

        log.debug("Creating a new hard disk at location [" + medIn.getLocation() + "] with format [" + medIn.getFormat() + "] and size ["
                + medIn.getLogicalSize() + "]");
        log.debug("File extension: " + Files.getFileExtension(medIn.getLocation()));
        if (Files.getFileExtension(medIn.getLocation()).isEmpty()) {
            log.debug("Will add extention to filename: " + medIn.getFormat().toLowerCase());
            medIn.setLocation(medIn.getLocation() + "." + medIn.getFormat().toLowerCase());
        } else {
            log.debug("No need to add extension");
        }

        _Medium med = hbox.getServer(srvIn.getId()).createMedium(medIn.getLocation(), medIn.getFormat(), medIn.getLogicalSize());
        MediumOut medOut = MediumIoFactory.get(med);
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, medOut));
    }

}
