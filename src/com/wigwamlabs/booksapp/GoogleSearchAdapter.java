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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.wigwamlabs.booksapp.db.BookAddRemoveObserver;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.BookListItemViewHolder;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookFeed;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.googlebooks.GoogleBookSearch.FeedSearch;
import com.wigwamlabs.util.ListViewUtils.State;
import com.wigwamlabs.util.ListViewUtils.StateListener;

public class GoogleSearchAdapter extends ArrayAdapter<GoogleBook> implements FeedSearch.Callback,
		BookAddRemoveObserver, CheckableAdapter, PausableThumbnailAdapter {
	private final GoogleBookSearch mBookSearch;
	private boolean mCheckable = false;
	private FeedSearch mCurrentSearch;
	private final DatabaseAdapter mDb;
	private final LoanStatusProvider mLoanStatusProvider;
	private final ImageDownloadCollection mLocalThumbnails;
	private FeedSearch mNextSearch;
	private int mRemoteBookCount = 0;
	private final SparseBooleanArray mRemoteCheckedPositions = new SparseBooleanArray();
	private State mState;
	private WeakReference<StateListener> mStateListener;
	private boolean mThumbnailsPaused;
	private int mTotalResults = -1;
	private final ImageDownloadCollection mWebThumbnails;

	public GoogleSearchAdapter(Context context, GoogleBookSearch bookSearch, DatabaseAdapter db,
			FeedSearch search, ImageDownloadCollection localThumbnails,
			ImageDownloadCollection webThumbnails) {
		super(context, R.layout.book_list_item);
		mBookSearch = bookSearch;
		mDb = db;
		mCurrentSearch = search;
		mLocalThumbnails = localThumbnails;
		mWebThumbnails = webThumbnails;
		search.setDatabase(mDb);
		search.executeInBackground(this);
		mState = State.Loading;
		mLoanStatusProvider = new LoanStatusProvider(mDb);
		mDb.addBookAddRemoveObserver(new WeakReference<BookAddRemoveObserver>(this));
	}

	public int getActionableItemCount() {
		return getCheckedItemCount();
	}

	public GoogleBook[] getActionableItems() {
		if (!mCheckable)
			return null;

		final int count = mRemoteCheckedPositions.size();
		final List<GoogleBook> books = new ArrayList<GoogleBook>(count);
		for (int i = 0; i < count; i++) {
			if (mRemoteCheckedPositions.valueAt(i)) {
				final int pos = mRemoteCheckedPositions.keyAt(i);
				final GoogleBook book = getItem(pos);
				books.add(book);
			}
		}
		return books.toArray(new GoogleBook[books.size()]);
	}

	@Override
	public int getCheckableItemCount() {
		return mRemoteBookCount;
	}

	@Override
	public int getCheckedItemCount() {
		return (mCheckable ? mRemoteCheckedPositions.size() : 0);
	}

	public State getState() {
		return mState;
	}

	private StateListener getStateListener() {
		return (mStateListener != null ? mStateListener.get() : null);
	}

	public int getTotalResults() {
		return mTotalResults;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final View view = BookListItemViewHolder.createOrReuse(context, convertView);
		final GoogleBook book = getItem(position);
		final ImageDownloadCollection thumbnails = (book.databaseId == null ? mWebThumbnails
				: mLocalThumbnails);
		final Date loanReturnBy = mLoanStatusProvider.getDateForBookId(book.databaseId);
		final int bookStatus = BookStatus.get(book.databaseId, loanReturnBy);
		final int checked;
		if (book.databaseId != null) // only remote books are checkable
			checked = ITEM_NOT_CHECKABLE;
		else
			checked = (mCheckable && mRemoteCheckedPositions.get(position) ? ITEM_CHECKED
					: ITEM_UNCHECKED);

		BookListItemViewHolder.from(view).update(context, book.databaseId, thumbnails,
				book.thumbnailSmallUrl, mThumbnailsPaused, book.title, book.creatorsText,
				book.pageCount, book.releaseDate, bookStatus, mCheckable, checked);

		return view;
	}

	@Override
	public boolean hasCheckedItems() {
		return mRemoteCheckedPositions.size() > 0;
	}

	@Override
	public boolean isCheckable() {
		return mCheckable;
	}

	public boolean loadMore() {
		if (mState == State.Loading)
			return false;
		final FeedSearch search = (mState == State.ErrorRetry ? mCurrentSearch : mNextSearch);
		if (search == null)
			return false;

		mCurrentSearch = search;
		mCurrentSearch.setDatabase(mDb);
		mCurrentSearch.executeInBackground(this);
		setState(State.Loading);
		return true;
	}

	@Override
	public void onBookAdded(long bookId, String googleId) {
		boolean changed = false;
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final GoogleBook book = getItem(i);
			if (book.googleId.equals(googleId)) {
				book.databaseId = Long.valueOf(bookId);
				mRemoteBookCount--;
				changed = true;

				if (mRemoteCheckedPositions.get(i)) {
					mRemoteCheckedPositions.delete(i);
				}
			}
		}

		if (changed) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void onBookRemoved(long bookId) {
		boolean changed = false;
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final GoogleBook book = getItem(i);
			if (book.databaseId != null && book.databaseId.longValue() == bookId) {
				book.databaseId = null;
				mRemoteBookCount++;
				changed = true;
			}
		}

		if (changed) {
			notifyDataSetChanged();
		}
	}

	@Override
	public void onDownloadFinished(GoogleBookFeed feed) {
		if (feed != null) {
			mTotalResults = feed.totalResults;
			if (feed.books != null) {
				for (final GoogleBook b : feed.books) {
					add(b);

					if (b.databaseId == null) {
						mRemoteBookCount++;

						// eagerly cache thumbnails
						mWebThumbnails.prefetchUrl(b.thumbnailSmallUrl);
					}
				}
			}
			mNextSearch = mBookSearch.searchNext(feed);

			setState(mNextSearch != null ? State.CanLoadMore : State.Finished);
		} else {
			setState(State.ErrorRetry);
		}
	}

	private void reportState() {
		final StateListener listener = getStateListener();
		if (listener != null)
			listener.onStateChanged(mState);
	}

	@Override
	public void setAllChecked(boolean checked) {
		if (checked) {
			final int count = getCount();
			for (int i = 0; i < count; i++) {
				if (getItem(i).databaseId == null) {
					mRemoteCheckedPositions.put(i, checked);
				}
			}
		} else {
			mRemoteCheckedPositions.clear();
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

	private void setState(State state) {
		if (mState == state)
			return;

		mState = state;
		reportState();
	}

	public void setStateListener(WeakReference<StateListener> listener, boolean forceInitialCallback) {
		mStateListener = listener;
		if (forceInitialCallback)
			reportState();
	}

	@Override
	public void setThumbnailsPaused(boolean paused) {
		mThumbnailsPaused = paused;
	}

	@Override
	public void toggleCheck(int position, long id) {
		if (position >= getCount()) // occurs when checking loading footer
			return;
		if (getItem(position).databaseId != null)
			return;

		final boolean newValue = !mRemoteCheckedPositions.get(position);
		mRemoteCheckedPositions.put(position, newValue);

		notifyDataSetChanged();
	}
}
