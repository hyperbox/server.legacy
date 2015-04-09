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

package io.kamax.hboxd.hypervisor.vm.device;

import io.kamax.hboxd.hypervisor._RawItem;

public interface _RawMemory extends _RawItem {

   public long getAmount();

   public void setAmount(long amount);

   public boolean isLargePageEnabled();

   public void setLargePage(boolean isEnabled);

   public boolean isPageFusionEnabled();

   public void setPageFusion(boolean isEnabled);

   public boolean isNestedPagingEnabled();

   public void setNestedPaging(boolean isEnabled);

   public boolean isVTxvpidEnabled();

   public void setVtxvpid(boolean isEnabled);

}
