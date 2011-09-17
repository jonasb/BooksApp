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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wigwamlabs.booksapp.IsbnSearchService.BookSearchItem;
import com.wigwamlabs.booksapp.IsbnSearchService.LocalBinder;
import com.wigwamlabs.booksapp.IsbnSearchService.Observer;
import com.wigwamlabs.booksapp.db.BookAddRemoveObserver;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.IsbnSearchItemBasicViewHolder;
import com.wigwamlabs.booksapp.ui.IsbnSearchItemFullViewHolder;
import com.wigwamlabs.googlebooks.GoogleBook;

public class IsbnSearchAdapter extends ArrayAdapter<BookSearchItem> implements
		BookAddRemoveObserver, CheckableAdapter, IsbnSearchService.Observer,
		PausableThumbnailAdapter, ThumbnailManager.Observer {
	private LocalBinder mBinder;
	private boolean mCheckable = false;
	private final Set<BookSearchItem> mCheckedLocalItems = new HashSet<BookSearchItem>();
	private final boolean mFull;
	private final LoanStatusProvider mLoanStatusProvider;
	private int mLocalBookCount = 0;
	private final GoogleBook mNullBook = new GoogleBook();
	private boolean mThumbnailsPaused;

	public IsbnSearchAdapter(Context context, DatabaseAdapter db, boolean full) {
		super(context, full ? R.layout.isbn_search_item_full : R.layout.isbn_search_item_basic);
		mFull = full;
		mLoanStatusProvider = new LoanStatusProvider(db);
		db.addBookAddRemoveObserver(new WeakReference<BookAddRemoveObserver>(this));
		ThumbnailManager.addThumbnailObserver(context,
				new WeakReference<ThumbnailManager.Observer>(this));
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mFull;
	}

	public int getActionableItemCount() {
		return (mCheckable ? mCheckedLocalItems.size() : mLocalBookCount);
	}

	public Long[] getActionableItems() {
		final int count = getCount();
		final List<Long> ids = new ArrayList<Long>(mCheckable ? mCheckedLocalItems.size()
				: mLocalBookCount);
		for (int i = 0; i < count; i++) {
			final BookSearchItem item = getItem(i);
			if (!mCheckable || mCheckedLocalItems.contains(item)) {
				final Long id = getDatabaseId(item);
				if (id != null) {
					ids.add(id);
				}
			}
		}

		return ids.toArray(new Long[ids.size()]);
	}

	@Override
	public int getCheckableItemCount() {
		return mLocalBookCount;
	}

	@Override
	public int getCheckedItemCount() {
		return mCheckedLocalItems.size();
	}

	private Long getDatabaseId(BookSearchItem item) {
		final GoogleBook book = item.book;
		if (book == null)
			return null;
		return book.databaseId;
	}

	public int getPosition(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final GoogleBook book = getItem(i).book;
			if (book != null && book.databaseId != null && book.databaseId.longValue() == id)
				return i;
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final BookSearchItem item = getItem(position);
		GoogleBook book = item.book;
		if (book == null) {
			book = mNullBook;
			book.title = item.isbn;
		}

		final ImageDownloadCollection thumbnails = mBinder.getThumbnails();
		final Date loanReturnBy = mLoanStatusProvider.getDateForBookId(book.databaseId);
		final int bookStatus = BookStatus.get(book.databaseId, loanReturnBy);

		if (mFull) {
			final View view = IsbnSearchItemFullViewHolder.createOrReuse(context, convertView);
			final int checked;
			if (book.databaseId == null)
				checked = ITEM_NOT_CHECKABLE;
			else
				checked = (mCheckable && mCheckedLocalItems.contains(item) ? ITEM_CHECKED
						: ITEM_UNCHECKED);

			IsbnSearchItemFullViewHolder.from(view).update(context, item.inProgress(),
					book.databaseId, thumbnails, book.thumbnailSmallUrl, mThumbnailsPaused,
					book.title, book.creatorsText, book.pageCount, book.releaseDate, bookStatus,
					mCheckable, checked);
			return view;
		} else {
			final View view = IsbnSearchItemBasicViewHolder.createOrReuse(context, convertView);
			IsbnSearchItemBasicViewHolder.from(view).update(context, item.inProgress(),
					book.databaseId, thumbnails, book.thumbnailSmallUrl, book.title, bookStatus);
			return view;
		}
	}

	@Override
	public boolean hasCheckedItems() {
		return !mCheckedLocalItems.isEmpty();
	}

	@Override
	public boolean isCheckable() {
		return mCheckable;
	}

	@Override
	public boolean isEnabled(int position) {
		return mFull;
	}

	@Override
	public void onAllItemsRemoved() {
		clear();
		mCheckedLocalItems.clear();
		mLocalBookCount = 0;
	}

	@Override
	public void onBookAdded(long bookId, String googleId) {
		boolean changed = false;
		for (int i = 0; i < getCount(); i++) {
			final BookSearchItem item = getItem(i);
			final GoogleBook book = item.book;
			if (book != null && book.googleId != null && book.googleId.equals(googleId)) {
				book.databaseId = Long.valueOf(bookId);
				mLocalBookCount++;
				changed = true;
			}
		}

		if (changed) {
			notifyDataSetChanged();
		}
	}

	public void onBookCreated(BookSearchItem item) {
		mLocalBookCount++;

		notifyDataSetChanged();
	}

	@Override
	public void onBookRemoved(long bookId) {
		boolean changed = false;
		for (int i = getCount() - 1; i >= 0; i--) {
			final BookSearchItem item = getItem(i);
			final Long id = getDatabaseId(item);
			if (id != null && id.longValue() == bookId) {
				remove(item);
				mLocalBookCount--;
				changed = true;

				mCheckedLocalItems.remove(item);
			}
		}

		if (changed) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void onItemAdded(BookSearchItem item) {
		add(item);

		if (item.book != null && item.book.databaseId != null)
			mLocalBookCount++;
	}

	@Override
	public void onItemsStateChanged() {
		notifyDataSetChanged();
	}

	@Override
	public void onStateChanged(int newState) {
	}

	@Override
	public void onThumbnailChanged(long bookId, boolean small, File file) {
		notifyDataSetChanged();
	}

	@Override
	public void setAllChecked(boolean checked) {
		if (checked) {
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				final BookSearchItem item = getItem(i);
				if (getDatabaseId(item) != null) {
					mCheckedLocalItems.add(item);
				}
			}
		} else {
			mCheckedLocalItems.clear();
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

	public void setService(LocalBinder binder) {
		mBinder = binder;
		binder.addObserver(new WeakReference<Observer>(this));
		clear();
		for (final BookSearchItem item : binder.getItems()) {
			onItemAdded(item);
		}
	}

	@Override
	public void setThumbnailsPaused(boolean paused) {
		mThumbnailsPaused = paused;
	}

	@Override
	public void toggleCheck(int position, long id) {
		final BookSearchItem item = getItem(position);
		if (getDatabaseId(item) == null)
			return;

		if (!mCheckedLocalItems.remove(item))
			mCheckedLocalItems.add(item);

		notifyDataSetChanged();
	}

	public void uncheck(int position) {
		final BookSearchItem item = getItem(position);
		mCheckedLocalItems.remove(item);

		notifyDataSetChanged();
	}
}
