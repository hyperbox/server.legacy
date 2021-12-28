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

package io.kamax.hboxd.controller.action;

import io.kamax.hbox.comm.AnswerType;

import javax.swing.*;

/**
 * Skeleton structure for a multi-task action.<br/>
 * This class will handle the start, finish & failed return type.</br> The following are used :
 * <ul>
 * <li>Start : AnswerType.STARTED</li>
 * <li>Finish : AnswerType.COMPLETED</li>
 * <li>Failed : AnswerType.FAILED</li>
 * </ul>
 *
 * @author max
 * @see AnswerType
 * @see _Action
 * @see AbstractAction
 */
public abstract class AbstractHyperboxMultiTaskAction extends AbstractHyperboxAction {

    @Override
    public AnswerType getStartReturn() {
        return AnswerType.STARTED;
    }

    @Override
    public AnswerType getFinishReturn() {
        return AnswerType.COMPLETED;
    }

    @Override
    public AnswerType getFailReturn() {
        return AnswerType.FAILED;
    }

}