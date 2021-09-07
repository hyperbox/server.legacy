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

package io.kamax.hboxd.controller.action;

import io.kamax.hbox.comm.AnswerType;
import io.kamax.hbox.comm.Request;
import io.kamax.hbox.comm._Client;
import io.kamax.hboxd.core._Hyperbox;
import io.kamax.hboxd.core.action.ASingleTaskAction;
import io.kamax.hboxd.core.action.AbstractHyperboxMultiTaskAction;

import java.util.List;

/**
 * <p>
 * Represent an action that can be done on the hyperbox server following a specific request from a client.<br/>
 * The action is mapped using the task string into a request.<br/>
 * The supported tasks need to be given into the <code>getRegistrations()</code> method.
 * </p>
 * This class will be auto-loaded at startup.
 *
 * @author max
 * @see AbstractHyperboxMultiTaskAction
 * @see ASingleTaskAction
 * @see ActionUtils
 */
// TODO javadoc
// TODO create a skeleton action for custom modules that can handle single object return or list returns
// TODO somehow, check that the required module is loaded.
public interface _Action {

    /**
     * To which task should this action answer to
     *
     * @return a list of task as String
     */
    List<String> getRegistrations();

    AnswerType getStartReturn();

    /**
     * null if none is required
     *
     * @return AnswerType for the return
     */
    AnswerType getFinishReturn();

    AnswerType getFailReturn();

    /**
     * Not implemented
     *
     * @return Not implemented
     */
    Class<?>[] getRequiredClasses();

    /**
     * Not implemented
     *
     * @return Not implemented
     */
    Enum<?>[] getRequiredEnums();

    /**
     * Not implemented
     *
     * @return Not implemented
     */
    String[] getRequiredData();

    void run(_Hyperbox core, Request req, _Client client);

}
