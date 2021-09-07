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

package io.kamax.hboxd.core.model;

import io.kamax.hboxd.hypervisor._RawItem;

public interface _Memory extends _RawItem {

    long getAmount();

    void setAmount(long amount);

    boolean isLargePageEnabled();

    void setLargePage(boolean isEnabled);

    boolean isPageFusionEnabled();

    void setPageFusion(boolean isEnabled);

    boolean isNestedPagingEnabled();

    void setNestedPaging(boolean isEnabled);

    boolean isVTxvpidEnabled();

    void setVtxvpid(boolean isEnabled);

}
