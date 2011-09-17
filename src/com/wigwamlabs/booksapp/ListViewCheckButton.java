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

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ListViewCheckButton extends ImageView implements OnClickListener, OnLongClickListener,
		OnItemClickListener {
	private CheckableAdapter mAdapter;
	private ListView mList;
	private OnItemClickListener mNormalOnItemClickListener;

	public ListViewCheckButton(Context context) {
		super(context);

		setBackgroundResource(R.drawable.titlebar_icon_background);
		setImageResource(R.drawable.titlebar_check);

		setOnClickListener(this);
		setOnLongClickListener(this);
	}

	public void disableCheckable() {
		if (mAdapter.isCheckable())
			toggleCheckable();
	}

	@Override
	public void onClick(View v) {
		if (mAdapter == null)
			return;

		toggleCheckable();
	}

	public int onCreateOptionsMenu(Menu menu, int itemId) {
		menu.add(Menu.NONE, itemId, Menu.NONE, R.string.check_all).setIcon(
				R.drawable.menu_select_all);
		menu.add(Menu.NONE, itemId + 1, Menu.NONE, R.string.uncheck_all).setIcon(
				R.drawable.menu_deselect_all);
		menu.add(Menu.NONE, itemId + 2, Menu.NONE, "").setIcon(R.drawable.menu_toggle_selection);

		return itemId + 3;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mAdapter.toggleCheck(position, id);
	}

	@Override
	public boolean onLongClick(View v) {
		if (mAdapter == null)
			return true;

		if (mAdapter.isCheckable()) {
			if (mAdapter.getCheckableItemCount() > 0) {
				final boolean hasChecked = mAdapter.hasCheckedItems();
				setAllChecked(!hasChecked);
			}
		}
		return true;
	}

	public int onOptionsItemSelected(int currentItemId, int itemId) {
		if (currentItemId == itemId)
			setAllChecked(true);
		else if (currentItemId == itemId + 1)
			setAllChecked(false);
		else if (currentItemId == itemId + 2)
			toggleCheckable();

		return itemId + 3;
	}

	public int onPrepareOptionsMenu(Menu menu, int itemId) {
		final boolean checkable = mAdapter.isCheckable();
		boolean checkAll = false;
		boolean uncheckAll = false;
		if (checkable) {
			final int checkedItemCount = mAdapter.getCheckedItemCount();
			final int checkableItemCount = mAdapter.getCheckableItemCount();
			checkAll = (checkedItemCount < checkableItemCount);
			uncheckAll = (checkedItemCount > 0);
		}

		menu.getItem(itemId).setVisible(checkAll);
		menu.getItem(itemId + 1).setVisible(uncheckAll);

		final MenuItem toggleCheckable = menu.getItem(itemId + 2);
		final boolean showToggle = mAdapter.getCount() > 0;
		toggleCheckable.setVisible(showToggle);
		if (showToggle)
			toggleCheckable.setTitle(checkable ? R.string.hide_selection : R.string.show_selection);

		return itemId + 3;
	}

	public void prepare(ListView list, CheckableAdapter adapter) {
		if (mList != null) {
			mList.setOnItemClickListener(mNormalOnItemClickListener);
		}
		mList = list;
		mNormalOnItemClickListener = list.getOnItemClickListener();
		mAdapter = adapter;

		mList.setOnItemClickListener(mAdapter != null && mAdapter.isCheckable() ? this
				: mNormalOnItemClickListener);
	}

	private void setAllChecked(boolean checked) {
		mAdapter.setAllChecked(checked);

		Toast.makeText(getContext(),
				checked ? R.string.checked_all_books_toast : R.string.unchecked_all_books_toast,
				Toast.LENGTH_SHORT).show();
	}

	private void toggleCheckable() {
		final boolean checkable = !mAdapter.isCheckable();
		mAdapter.setCheckable(checkable);
		mList.setOnItemClickListener(checkable ? this : mNormalOnItemClickListener);
	}
}
