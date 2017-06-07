/*
 * Hyperbox - Virtual Infrastructure Manager
 * Copyright (C) 2013 Maxime Dor
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

import io.kamax.hbox.comm.*;
import io.kamax.hbox.comm.in.StoreIn;
import io.kamax.hbox.comm.in.StoreItemIn;
import io.kamax.hbox.comm.out.StoreItemOut;
import io.kamax.hboxd.comm.io.factory.StoreItemIoFactory;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;
import io.kamax.hboxd.session.SessionContext;
import io.kamax.hboxd.store._StoreItem;

import java.util.Arrays;
import java.util.List;

public final class StoreItemListAction extends AbstractHyperboxMultiTaskAction {

    @Override
    public List<String> getRegistrations() {
        return Arrays.asList(Command.HBOX.getId() + HyperboxTasks.StoreItemList.getId());
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public void run(Request request, _Hyperbox hbox) {
        StoreIn sIn = request.get(StoreIn.class);
        StoreItemIn siIn;
        if (request.has(StoreItemIn.class)) {
            siIn = request.get(StoreItemIn.class);
        } else {
            siIn = new StoreItemIn();
        }
        List<_StoreItem> siList = hbox.getStoreManager().getStore(sIn.getId()).getItem(siIn.getPath()).listItems();
        List<StoreItemOut> siOutList = StoreItemIoFactory.get(siList);
        for (StoreItemOut siOut : siOutList) {
            SessionContext.getClient().putAnswer(new Answer(request, AnswerType.DATA, siOut));
        }
    }

}
