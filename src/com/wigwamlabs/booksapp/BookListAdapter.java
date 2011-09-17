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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.ui.BookListItemViewHolder;

public class BookListAdapter extends SortedCursorAdapter implements CheckableAdapter,
		PausableThumbnailAdapter, ThumbnailManager.Observer {
	private boolean mCheckable = false;
	private final SparseBooleanArray mCheckedIds = new SparseBooleanArray();
	private final ImageDownloadCollection mThumbnails;
	private boolean mThumbnailsPaused;

	public BookListAdapter(Context context, BookListCursor list, ImageDownloadCollection thumbnails) {
		super(context, list);
		mThumbnails = thumbnails;
		ThumbnailManager.addThumbnailObserver(context,
				new WeakReference<ThumbnailManager.Observer>(this));
	}

	public BookListAdapter(Context context, BookListCursor list, int sortedColumn,
			ImageDownloadCollection thumbnails) {
		super(context, list, sortedColumn);
		mThumbnails = thumbnails;
		ThumbnailManager.addThumbnailObserver(context,
				new WeakReference<ThumbnailManager.Observer>(this));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final BookListCursor book = (BookListCursor) cursor;
		final Long bookId = Long.valueOf(book._id());
		final int bookStatus = BookStatus.get(bookId, book.loanReturnBy());
		final int checked = (mCheckable && mCheckedIds.get(bookId.intValue()) ? ITEM_CHECKED
				: ITEM_UNCHECKED);

		BookListItemViewHolder.from(view).update(context, bookId, mThumbnails, null,
				mThumbnailsPaused, book.title(), book.creators(), book.pageCount(),
				book.releaseDate(), bookStatus, mCheckable, checked);
	}

	public int getActionableItemCount() {
		return (mCheckable ? getCheckedItemCount() : getCount());
	}

	public Long[] getActionableItems() {
		final BookListCursor b = (BookListCursor) getCursor();
		final List<Long> ids = new ArrayList<Long>();
		for (b.moveToFirst(); !b.isAfterLast(); b.moveToNext()) {
			final long id = b._id();
			if (!mCheckable || mCheckedIds.get((int) id)) {
				ids.add(Long.valueOf(id));
			}
		}
		return ids.toArray(new Long[ids.size()]);
	}

	@Override
	public int getCheckableItemCount() {
		return getCount();
	}

	@Override
	public int getCheckedItemCount() {
		if (!mCheckable)
			return 0;

		// since the list might be filtered we must always iterate the items
		int count = 0;
		final BookListCursor b = (BookListCursor) getCursor();
		for (b.moveToFirst(); !b.isAfterLast(); b.moveToNext()) {
			if (mCheckedIds.get((int) b._id()))
				count++;
		}
		return count;
	}

	public int getPosition(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public boolean hasCheckedItems() {
		// since the list might be filtered we must always iterate the items
		final BookListCursor b = (BookListCursor) getCursor();
		for (b.moveToFirst(); !b.isAfterLast(); b.moveToNext()) {
			if (mCheckedIds.get((int) b._id()))
				return true;
		}
		return false;
	}

	@Override
	public boolean isCheckable() {
		return mCheckable;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return BookListItemViewHolder.createOrReuse(context, null);
	}

	@Override
	public void onThumbnailChanged(long bookId, boolean small, File file) {
		if (small)
			notifyDataSetChanged();
	}

	@Override
	public void setAllChecked(boolean checked) {
		// since the list might be filtered we must always iterate the items
		final BookListCursor b = (BookListCursor) getCursor();
		for (b.moveToFirst(); !b.isAfterLast(); b.moveToNext()) {
			mCheckedIds.put((int) b._id(), checked);
		}
		notifyDataSetChanged();
	}

	@Override
	public void setCheckable(boolean checkable) {
		if (checkable == mCheckable)
			return;

		mCheckable = checkable;
		notifyDataSetChanged();
	}

	@Override
	public void setThumbnailsPaused(boolean paused) {
		mThumbnailsPaused = paused;
	}

	@Override
	public void toggleCheck(int position, long id) {
		final int key = (int) id;
		final boolean newValue = !mCheckedIds.get(key);
		mCheckedIds.put(key, newValue);
		notifyDataSetChanged();
	}

	public void uncheck(long id) {
		final int key = (int) id;
		mCheckedIds.delete(key);
		notifyDataSetChanged();
	}
}
