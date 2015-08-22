/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
 * hyperbox at altherian dot org
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

package io.kamax.hboxd.core.action.store;

import io.kamax.hbox.comm.Answer;
import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Command;
import io.kamax.hbox.comm.HyperboxTasks;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm.in.StoreIn;
import io.kamax.hbox.comm.out.StoreOut;
import io.kamax.hbox.constant.StoreAttribute;
import io.kamax.hboxd.comm.io.factory.StoreIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.hboxd.store._Store;
import java.util.Arrays;
import java.util.List;

public class StoreRegisterAction extends ASingleTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.StoreRegister.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        StoreIn stoIn = request.get(StoreIn.class);

        String location = stoIn.getSetting(StoreAttribute.Location).getString();
        String label = stoIn.getSetting(StoreAttribute.Label).getString();

        _Store store = hbox.getStoreManager().registerStore(location, label);
        StoreOut stoOut = StoreIoFactory.get(store);
        SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, stoOut));
    }

}
