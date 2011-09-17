/*
 * Copyright 2011 Jonas Bengtsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wigwamlabs.booksapp;

public interface CheckableAdapter {
	public static int ITEM_CHECKED = 0;
	public static int ITEM_NOT_CHECKABLE = 1;
	public static int ITEM_UNCHECKED = 2;

	public int getCheckableItemCount();

	public int getCheckedItemCount();

	public int getCount();

	public boolean hasCheckedItems();

	public boolean isCheckable();

	public void setAllChecked(boolean checked);

	public void setCheckable(boolean checkable);

	public void toggleCheck(int position, long id);
}
