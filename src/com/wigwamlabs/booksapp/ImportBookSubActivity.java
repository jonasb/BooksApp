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

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

import com.csvreader.CsvReader;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class ImportBookSubActivity extends SubActivity implements PreviousNextProvider {
	private static final int BOOKS_CURSOR = 0;
	private BookListAdapter mAdapter;
	private final BookListView mBookList;
	private final ListViewCheckButton mCheckButton;
	private final DatabaseAdapter mDb;
	private Long mFirstBookId;
	private Long mLastBookId;
	private final ImageDownloadCollection mThumbnails;

	protected ImportBookSubActivity(Context context, SubActivityManager manager,
			DatabaseAdapter db, ImageDownloadCollection thumbnails) {
		super(context, manager);
		mDb = db;
		mThumbnails = thumbnails;
		setContentView(R.layout.book_list_main);

		final TitleBar titleBar = getTitleBar();
		mCheckButton = new ListViewCheckButton(context);
		titleBar.addLeftContent(mCheckButton);

		mBookList = (BookListView) findViewById(R.id.book_list);
		mBookList.init(mDb, manager, mCheckButton);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mBookList.onActivityResult(requestCode, resultCode, data))
			return true;
		return super.onActivityResult(requestCode, resultCode, data);
	}

	/* package */void onBookImported(Long bookId) {
		if (mFirstBookId == null)
			mFirstBookId = bookId;
		mLastBookId = bookId;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		int itemId = 0;
		itemId = mBookList.onCreateOptionsMenu(menu, itemId);
		itemId = mCheckButton.onCreateOptionsMenu(menu, itemId);
	}

	/* package */void onImportFinished() {
		if (mFirstBookId == null || mLastBookId == null) {
			close();
			return;
		}

		final BookListCursor books = BookListCursor.fetchByIdRange(mDb, mFirstBookId.longValue(),
				mLastBookId.longValue());
		// don't close old cursor since the adapter will do that
		setCursor(books, BOOKS_CURSOR, false);
		mAdapter.changeCursor(books);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int currentItemId = item.getItemId();
		int itemId = 0;

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
		itemId = mBookList.onPrepareOptionsMenu(menu, itemId);
		itemId = mCheckButton.onPrepareOptionsMenu(menu, itemId);
		return true;
	}

	@Override
	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		mBookList.openPreviousNext(previous, activityToReplace);
	}

	public ImportBookSubActivity prepare(ContentResolver contentResolver, Uri uri) {
		final AsyncTaskListener<CsvReader, Long, Integer> listener = new AsyncTaskListener<CsvReader, Long, Integer>() {
			@Override
			public void onPostExecute(Integer integer) {
				onImportFinished();
			}

			@Override
			public void onProgressUpdate(Long... params) {
				for (final Long bookId : params) {
					onBookImported(bookId);
				}
			}
		};

		mFirstBookId = mLastBookId = null;
		mAdapter = new BookListAdapter(getContext(), null, mThumbnails);
		mBookList.prepare(mAdapter, null);
		mCheckButton.prepare(mBookList.getList(), mAdapter);

		if (!ImportBookTask.createAndExecute(getContext(), mDb, contentResolver, listener, uri)) {
			close();
		}

		return this;
	}

	@Override
	public void setCallback(WeakReference<PreviousNextProvider.Callback> callback,
			boolean notifyDirectly) {
		mBookList.setCallback(callback, notifyDirectly);
	}
}
