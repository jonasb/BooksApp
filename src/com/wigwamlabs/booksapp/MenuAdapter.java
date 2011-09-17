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
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.ItemCountCursor;
import com.wigwamlabs.booksapp.ui.BookGroupItemViewHolder;
import com.wigwamlabs.booksapp.ui.IconBookGroupItemViewHolder;
import com.wigwamlabs.booksapp.ui.ListSectionViewHolder;
import com.wigwamlabs.util.Pair;

public class MenuAdapter extends BaseAdapter {
	private static final int COLLECTION_HEADER = 1;
	private int mBookCount;
	private final BookGroupCursor mCollections;
	private final Context mContext;
	/* package */boolean mDataValid = true;
	private final Pair<Integer, String>[] mFixedMenuItems;

	public MenuAdapter(Context context, Pair<Integer, String>[] fixedMenuItems,
			final ItemCountCursor bookCount, BookGroupCursor collections) {
		mContext = context;
		mFixedMenuItems = fixedMenuItems;
		mCollections = collections;
		mCollections.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				mDataValid = true;
			}

			@Override
			public void onInvalidated() {
				mDataValid = false;
			}
		});

		bookCount.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				updateBookCount(bookCount);
			}
		});
		updateBookCount(bookCount);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getCount() {
		if (!mDataValid)
			return mFixedMenuItems.length;
		final int collectionCount = mCollections.getCount();
		if (collectionCount == 0)
			return mFixedMenuItems.length;
		return mFixedMenuItems.length + COLLECTION_HEADER + collectionCount;
	}

	@Override
	public Object getItem(int position) {
		if (position < mFixedMenuItems.length)
			return mFixedMenuItems[position];

		if (position == mFixedMenuItems.length)
			return null;

		if (!mDataValid)
			return null;
		mCollections.moveToPosition(position - mFixedMenuItems.length - COLLECTION_HEADER);
		return mCollections;
	}

	@Override
	public long getItemId(int position) {
		if (position < mFixedMenuItems.length)
			return 0;
		if (position == mFixedMenuItems.length)
			return 0;

		if (!mDataValid)
			return 0;
		mCollections.moveToPosition(position - mFixedMenuItems.length - COLLECTION_HEADER);
		return mCollections._id();
	}

	@Override
	public int getItemViewType(int position) {
		if (position == MenuSubActivity.BOOKS_ITEM)
			return 0;
		if (position < mFixedMenuItems.length)
			return 1;
		if (position == mFixedMenuItems.length)
			return 2;
		return 3;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position == MenuSubActivity.BOOKS_ITEM) {
			final Pair<Integer, String> item = mFixedMenuItems[position];
			final View view = IconBookGroupItemViewHolder.createOrReuse(mContext, convertView);
			IconBookGroupItemViewHolder.from(view).update(item.first.intValue(), item.second,
					mBookCount);
			return view;
		} else if (position < mFixedMenuItems.length) {
			final Pair<Integer, String> item = mFixedMenuItems[position];
			View view = convertView;
			if (convertView == null) {
				view = LayoutInflater.from(mContext).inflate(R.layout.icon_list_item, null);
			}
			final TextView textView = (TextView) view.findViewById(android.R.id.text1);
			textView.setText(item.second);

			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			icon.setImageResource(item.first.intValue());
			return view;
		} else if (position == mFixedMenuItems.length) {
			final View view = ListSectionViewHolder.createOrReuse(mContext, convertView);
			ListSectionViewHolder.from(view).update(R.string.collections_section_header);
			return view;
		} else {
			if (!mDataValid)
				return new View(mContext);

			final View view = BookGroupItemViewHolder.createOrReuse(mContext, convertView, false);
			final BookGroupCursor bookGroup = (BookGroupCursor) getItem(position);
			BookGroupItemViewHolder.from(view).update(bookGroup.name(), bookGroup.bookCount());
			return view;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	@Override
	public boolean isEnabled(int position) {
		// section header not enabled
		return (position != mFixedMenuItems.length);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
		mCollections.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
		mCollections.unregisterDataSetObserver(observer);
	}

	/* package */void updateBookCount(ItemCountCursor bookCount) {
		if (bookCount.moveToFirst()) {
			mBookCount = bookCount.count();
			notifyDataSetChanged();
		}
	}
}
