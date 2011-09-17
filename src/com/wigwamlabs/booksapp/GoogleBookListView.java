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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.wigwamlabs.booksapp.PreviousNextProvider.Callback;
import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.util.ListViewUtils;

public abstract class GoogleBookListView extends ListView implements OnItemClickListener,
		OnItemLongClickListener {
	private ListViewCheckButton mCheckButton;
	private DatabaseAdapter mDb;
	private final PauseThumbnailsOnScrollListener mOnScrollListener = new PauseThumbnailsOnScrollListener();
	private int mPositionToShow = INVALID_POSITION;
	private final AdapterPreviousNextProvider mPreviousNextProvider = new AdapterPreviousNextProvider();
	private SubActivityManager mSubActivityManager;
	private ImageDownloadCollection mWebLargeThumbnails;
	private ImageDownloadCollection mWebSmallThumbnails;

	public GoogleBookListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
		setOnScrollListener(mOnScrollListener);
	}

	protected abstract GoogleBook getBookForDisplay(int position);

	public ListViewCheckButton getCheckButton() {
		return mCheckButton;
	}

	public DatabaseAdapter getDb() {
		return mDb;
	}

	protected SubActivityManager getSubActivityManager() {
		return mSubActivityManager;
	}

	protected ImageDownloadCollection getWebLargeThumbnails() {
		return mWebLargeThumbnails;
	}

	protected ImageDownloadCollection getWebSmallThumbnails() {
		return mWebSmallThumbnails;
	}

	protected void init(DatabaseAdapter db, SubActivityManager manager,
			ListViewCheckButton checkButton) {
		mDb = db;
		mSubActivityManager = manager;
		mCheckButton = checkButton;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onOpenBookDetails(position, null, ShowDirection.LEFT);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
			final long id) {
		final GoogleBook book = getBookForDisplay(position);
		if (book != null && book.databaseId != null) {
			BookItemContextMenu.showForLocalBook(getContext(), mSubActivityManager, mDb, null,
					position, book.databaseId.longValue(), parent, view, this);
		} else {
			onShowContextMenuForGoogleBook(parent, view, position, id, book);
		}
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (mPositionToShow != INVALID_POSITION) {
			ListViewUtils.ensureVisible(this, mPositionToShow);
			mPositionToShow = INVALID_POSITION;
		}
		super.onLayout(changed, l, t, r, b);
	}

	protected abstract void onNotifyDataSetChanged();

	private void onOpenBookDetails(int position, SubActivity activityToReplace, int direction) {
		final GoogleBook book = getBookForDisplay(position);
		final int previousPosition = mPreviousNextProvider.getCurrentPosition();
		mPreviousNextProvider.setCurrentPosition(position);

		if (book == null || book.databaseId == null) {
			if (!onOpenGoogleBookDetail(position, book, activityToReplace, direction))
				mPreviousNextProvider.setCurrentPosition(previousPosition);
		} else {
			mSubActivityManager.create(BookDetailsSubActivity.class, activityToReplace)
					.prepare(book.databaseId.longValue()).show(direction);
		}

		// remember the position to the next layout. for some reason setting
		// position here has no effect (possibly due to data set change)
		mPositionToShow = position;

		// user is likely to change the state of a book so invalidate the list
		// content so we're sure to be up to date when returning
		onNotifyDataSetChanged();
	}

	protected abstract boolean onOpenGoogleBookDetail(int position, GoogleBook book,
			SubActivity activityToReplace, int direction);

	protected abstract void onShowContextMenuForGoogleBook(AdapterView<?> parent, View view,
			final int position, final long id, final GoogleBook book);

	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		final int position = mPreviousNextProvider.previousNextPosition(previous);
		if (position >= 0) {
			onOpenBookDetails(position, activityToReplace, previous ? ShowDirection.DOWN
					: ShowDirection.UP);
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		mPreviousNextProvider.setAdapter(adapter);
		mOnScrollListener.setAdapter((PausableThumbnailAdapter) adapter);
	}

	public void setCallback(WeakReference<Callback> callback, boolean notifyDirectly) {
		mPreviousNextProvider.setCallback(callback, notifyDirectly);
	}

	protected void setCurrentPosition(int position) {
		mPreviousNextProvider.setCurrentPosition(position);
	}

	public void setWebThumbnails(ImageDownloadCollection smallThumbnails,
			ImageDownloadCollection largeThumbnails) {
		mWebSmallThumbnails = smallThumbnails;
		mWebLargeThumbnails = largeThumbnails;
	}
}
