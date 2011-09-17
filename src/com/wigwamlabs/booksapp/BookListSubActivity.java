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
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FilterQueryProvider;

import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class BookListSubActivity extends SubActivity implements FilterQueryProvider,
		PreviousNextProvider {
	private static final int BOOKS_CURSOR = 0;
	private final AddBookButton mAddButton;
	private Integer mBookGroup;
	private Long mBookGroupId;
	private final BookListView mBookList;
	private final ListViewCheckButton mCheckButton;
	private final DatabaseAdapter mDb;
	private boolean mExpiredLoans;
	private final ImageDownloadCollection mThumbnails;

	public BookListSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db,
			ImageDownloadCollection thumbnails) {
		super(context, manager);
		mDb = db;
		mThumbnails = thumbnails;
		setContentView(R.layout.book_list_main);

		final TitleBar titleBar = getTitleBar();
		mCheckButton = new ListViewCheckButton(context);
		titleBar.addLeftContent(mCheckButton);

		mBookList = (BookListView) findViewById(R.id.book_list);
		mBookList.init(mDb, manager, mCheckButton);

		mAddButton = new AddBookButton(context, manager, mBookList);
		titleBar.addLeftContent(mAddButton);

		titleBar.enableFilter(mBookList);
	}

	private void doPrepare(Integer bookGroup, Long bookGroupId, boolean expiredLoans) {
		mBookGroup = bookGroup;
		mBookGroupId = bookGroupId;
		mExpiredLoans = expiredLoans;
		final Long collectionId = bookGroup != null
				&& bookGroup.intValue() == BookGroup.COLLECTIONS ? bookGroupId : null;

		final BookListCursor books = runQuery(null);
		final BookListAdapter adapter = new BookListAdapter(getContext(), books,
				BookListCursor.title_normalized_index, mThumbnails);
		adapter.setFilterQueryProvider(this);
		mBookList.prepare(adapter, collectionId);
		mCheckButton.prepare(mBookList.getList(), adapter);

		mAddButton.setVisibility(bookGroup == null || collectionId != null ? View.VISIBLE
				: View.GONE);
		mAddButton.setCollectionId(collectionId);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mBookList.onActivityResult(requestCode, resultCode, data))
			return true;
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		int itemId = 0;
		itemId = mAddButton.onCreateOptionsMenu(menu, itemId);
		itemId = mBookList.onCreateOptionsMenu(menu, itemId);
		itemId = mCheckButton.onCreateOptionsMenu(menu, itemId);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int currentItemId = item.getItemId();
		int itemId = 0;

		itemId = mAddButton.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		itemId = mBookList.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		itemId = mCheckButton.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int itemId = 0;
		itemId = mAddButton.onPrepareOptionsMenu(menu, itemId);
		itemId = mBookList.onPrepareOptionsMenu(menu, itemId);
		itemId = mCheckButton.onPrepareOptionsMenu(menu, itemId);
		return true;
	}

	@Override
	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		mBookList.openPreviousNext(previous, activityToReplace);
	}

	public BookListSubActivity prepare() {
		doPrepare(null, null, false);
		return this;
	}

	public BookListSubActivity prepareWithBookGroup(int bookGroup, long bookGroupId) {
		doPrepare(Integer.valueOf(bookGroup), Long.valueOf(bookGroupId), false);
		return this;
	}

	public BookListSubActivity prepareWithExpiredLoans() {
		doPrepare(null, null, true);
		return this;
	}

	@Override
	public BookListCursor runQuery(CharSequence constraint) {
		BookListCursor c;
		try {
			if (mBookGroup == null) {
				if (mExpiredLoans) {
					final Date now = Calendar.getInstance().getTime();
					c = BookListCursor.fetchExpiredLoans(mDb, now,
							BookListCursor.title_normalized_index, constraint);
				} else
					c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index,
							constraint);
			} else {
				switch (mBookGroup.intValue()) {
				case BookGroup.COLLECTIONS:
					c = BookListCursor.fetchByCollection(mDb,
							BookListCursor.title_normalized_index, mBookGroupId.longValue(),
							constraint);
					break;
				case BookGroup.CONTACTS:
					c = BookListCursor.fetchByContact(mDb, BookListCursor.title_normalized_index,
							mBookGroupId.longValue(), constraint);
					break;
				case BookGroup.SERIES:
					c = BookListCursor.fetchBySeries(mDb, BookListCursor.volume_index,
							mBookGroupId.longValue(), constraint);
					break;
				default:
					throw new IllegalArgumentException("BookGroup unknown: " + mBookGroup);
				}
			}
		} catch (final Exception e) {
			c = null;
		}
		// don't close old cursor since the adapter will do that
		setCursor(c, BOOKS_CURSOR, false);
		return c;
	}

	@Override
	public void setCallback(WeakReference<PreviousNextProvider.Callback> callback,
			boolean notifyDirectly) {
		mBookList.setCallback(callback, notifyDirectly);
	}
}
