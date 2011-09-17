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

import static com.wigwamlabs.util.CollectionUtils.listToArray;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.IsbnSearchCursor;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookFeed;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.util.WeakListIterator;

public class IsbnSearchService extends Service {
	public static class BookSearchItem {
		public static final int FULL_EXISTING = 0;
		public static final int FULL_SAVED = 1;
		public static final int LIST = 2;
		public static final int NOT_FOUND = 3;
		public static final int UNKNOWN = 4;
		public GoogleBook book;
		public final String isbn;
		public int state = UNKNOWN;

		public BookSearchItem(String isbn) {
			this.isbn = isbn;
		}

		public boolean inProgress() {
			return (state == BookSearchItem.FULL_EXISTING || state == BookSearchItem.FULL_SAVED || state == BookSearchItem.NOT_FOUND);
		}
	}

	public class LocalBinder extends Binder {
		public void addIsbn(String isbn) {
			IsbnSearchService.this.addIsbn(isbn);
		}

		public void addObserver(WeakReference<Observer> observer) {
			IsbnSearchService.this.addObserver(observer);
		}

		public List<BookSearchItem> getItems() {
			return IsbnSearchService.this.getItems();
		}

		public int getState() {
			return IsbnSearchService.this.getState();
		}

		public ImageDownloadCollection getThumbnails() {
			return IsbnSearchService.this.getThumbnails();
		}

		public void reset() {
			IsbnSearchService.this.reset();
		}

		public void setCollectionId(Long collectionId) {
			IsbnSearchService.this.mCollectionId = collectionId;
		}
	}

	public interface Observer {
		void onAllItemsRemoved();

		void onItemAdded(BookSearchItem item);

		void onItemsStateChanged();

		void onStateChanged(int newState);
	}

	public class SearchTask extends AsyncTask<List<BookSearchItem>, List<BookSearchItem>, Boolean> {
		private void addToCollection(List<BookSearchItem> localItems) {
			if (mCollectionId == null)
				return;

			try {
				final int t = mDb.beginTransaction();
				for (final BookSearchItem item : localItems) {
					CollectionActions.addCollection(mDb, t, item.book.databaseId.longValue(),
							mCollectionId.longValue());
				}
				mDb.setTransactionSuccessful(t);
			} finally {
				mDb.endTransaction();
			}
		}

		private GoogleBook createFakeBook(final IsbnSearchCursor c) {
			final GoogleBook b = new GoogleBook();
			b.databaseId = Long.valueOf(c._id());
			b.creatorsText = c.creators();
			b.pageCount = c.pageCount();
			b.releaseDate = c.releaseDate();
			b.title = c.title();
			return b;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected Boolean doInBackground(List<BookSearchItem>... arg) {
			final List<BookSearchItem> allItems = arg[0];
			final List<BookSearchItem> items = new ArrayList<BookSearchItem>(MAX_COUNT_PER_SEARCH);
			final List<BookSearchItem> localItems = new ArrayList<BookSearchItem>(
					MAX_COUNT_PER_SEARCH);
			final List<String> isbns = new ArrayList<String>(MAX_COUNT_PER_SEARCH);

			while (true) {
				items.clear();
				localItems.clear();
				isbns.clear();

				// find unknown items
				findUnknownItems(allItems, items, isbns);

				if (!items.isEmpty()) {
					// look for them locally
					searchLocally(items, isbns, localItems);
					if (!localItems.isEmpty()) {
						addToCollection(localItems);
						publishProgress(new ArrayList<BookSearchItem>(localItems));
					}

					if (items.isEmpty())
						continue;

					// look for remaining items on web
					if (searchOnWeb(items, listToArray(isbns))) {
						publishProgress(new ArrayList<BookSearchItem>(items));
						continue;
					} else {
						return Boolean.FALSE;
					}
				}

				// get full information
				final BookSearchItem listItem = findFirstListItem(allItems);
				if (listItem != null) {
					if (saveBook(listItem)) {
						items.add(listItem);
						publishProgress(new ArrayList<BookSearchItem>(items));
						continue;
					} else {
						return Boolean.FALSE;
					}
				}

				return Boolean.TRUE;
			}
		}

		private BookSearchItem findFirstListItem(List<BookSearchItem> allItems) {
			for (final BookSearchItem item : allItems) {
				if (item.state == BookSearchItem.LIST)
					return item;
			}
			return null;
		}

		private void findUnknownItems(final List<BookSearchItem> allItems,
				final List<BookSearchItem> items, final List<String> isbns) {
			for (final BookSearchItem item : allItems) {
				if (item.book == null && item.state != BookSearchItem.NOT_FOUND) {
					items.add(item);
					isbns.add(item.isbn);
					if (items.size() >= MAX_COUNT_PER_SEARCH)
						break;
				}
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			setState(result.booleanValue() ? STATE_FINISHED : STATE_FAILED);
			mSearchTask = null;
		}

		@Override
		protected void onProgressUpdate(List<BookSearchItem>... values) {
			final List<BookSearchItem> items = values[0];
			for (final Observer o : WeakListIterator.from(mObservers)) {
				o.onItemsStateChanged();
			}

			int savedCount = 0;
			for (final BookSearchItem item : items) {
				if (item != null && item.state == BookSearchItem.FULL_SAVED) {
					savedCount++;
				}
			}
			if (savedCount > 0) {
				Toast.makeText(IsbnSearchService.this,
						getResources().getQuantityText(R.plurals.saved_books_toast, savedCount),
						Toast.LENGTH_SHORT).show();
			}
		}

		private boolean saveBook(BookSearchItem listItem) {
			SaveGoogleBookTask.execute(IsbnSearchService.this, mDb, mGoogleBookSearch, mThumbnails,
					null, listItem.book, IsbnSearchService.this.mCollectionId);
			listItem.state = BookSearchItem.FULL_SAVED;
			return true;
		}

		private void searchLocally(List<BookSearchItem> items, List<String> isbns,
				List<BookSearchItem> localItems) {
			final IsbnSearchCursor c = IsbnSearchCursor.searchByIsbns(mDb, listToArray(isbns));
			for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
				for (int i = items.size() - 1; i >= 0; i--) {
					final BookSearchItem item = items.get(i);
					if (item.isbn.equals(c.isbn13())) {
						items.remove(i);
						isbns.remove(i);

						item.state = BookSearchItem.FULL_EXISTING;
						item.book = createFakeBook(c);

						localItems.add(item);
					}
				}
			}
			c.close();
		}

		private boolean searchOnWeb(List<BookSearchItem> items, String[] isbns) {
			final GoogleBookFeed feed;
			try {
				feed = mGoogleBookSearch.searchByIsbns(isbns).execute();
			} catch (final IOException e) {
				e.printStackTrace();
				return false;
			}
			if (feed.books != null) {
				for (final GoogleBook book : feed.books) {
					for (final BookSearchItem item : items) {
						if (book.isbn13 != null && book.isbn13.equals(item.isbn)) {
							item.book = book;
						}
					}
				}
			}

			for (final BookSearchItem item : items) {
				item.state = (item.book == null ? BookSearchItem.NOT_FOUND : BookSearchItem.LIST);
			}
			return true;
		}
	}

	/* package */static final int MAX_COUNT_PER_SEARCH = 10;
	public static final int STATE_FAILED = 0;
	public static final int STATE_FINISHED = 1;
	public static final int STATE_RUNNING = 2;
	private LocalBinder mBinder;
	public Long mCollectionId;
	/* package */DatabaseAdapter mDb;
	/* package */GoogleBookSearch mGoogleBookSearch;
	private List<BookSearchItem> mItems;
	/* package */List<WeakReference<Observer>> mObservers;
	/* package */SearchTask mSearchTask;
	private int mState = STATE_FINISHED;
	/* package */ImageDownloadCollection mThumbnails;

	public void addIsbn(String isbn) {
		final BookSearchItem item = new BookSearchItem(isbn);
		mItems.add(item);

		for (final Observer o : WeakListIterator.from(mObservers)) {
			o.onItemAdded(item);
		}

		startQueryUnlessAlreadyRunning();
	}

	public void addObserver(WeakReference<Observer> observer) {
		mObservers.add(observer);
	}

	public List<BookSearchItem> getItems() {
		return mItems;
	}

	public int getState() {
		return mState;
	}

	public ImageDownloadCollection getThumbnails() {
		return mThumbnails;
	}

	public void initDebug(Context context, DatabaseAdapter db) {
		attachBaseContext(context);
		mDb = db;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Debug.enableStrictMode();

		mBinder = new LocalBinder();
		mItems = new ArrayList<BookSearchItem>();
		mObservers = new ArrayList<WeakReference<Observer>>();
		mGoogleBookSearch = new GoogleBookSearch(this);
		mThumbnails = CacheConfig.createWebThumbnailCacheSmall(this);
		if (mDb == null)
			mDb = ((BooksApp) getApplicationContext()).getDb();
	}

	public void reset() {
		setState(STATE_FINISHED);
		mItems.clear();
		if (mSearchTask != null) {
			mSearchTask.cancel(true);
			mSearchTask = null;
		}
		for (final Observer o : WeakListIterator.from(mObservers)) {
			o.onAllItemsRemoved();
		}
		mThumbnails = CacheConfig.createWebThumbnailCacheSmall(this);
	}

	public void setState(int newState) {
		if (newState == mState)
			return;
		mState = newState;

		for (final Observer o : WeakListIterator.from(mObservers)) {
			o.onStateChanged(newState);
		}
	}

	@SuppressWarnings("unchecked")
	public void startQueryUnlessAlreadyRunning() {
		if (mSearchTask != null)
			return;

		mSearchTask = new SearchTask();
		mSearchTask.execute(mItems);

		setState(STATE_RUNNING);
	}
}
