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

package com.wigwmlabs.booksapp.test;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.content.Intent;

import com.google.api.client.apache.ApacheHttpTransport;
import com.wigwamlabs.booksapp.HttpTransportCache;
import com.wigwamlabs.booksapp.IsbnSearchService;
import com.wigwamlabs.booksapp.IsbnSearchService.BookSearchItem;
import com.wigwamlabs.booksapp.IsbnSearchService.LocalBinder;
import com.wigwamlabs.booksapp.IsbnSearchService.Observer;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwmlabs.booksapp.test.ExpectedResult.Books;

public class IsbnSearchServiceTest extends DatabaseTestCase {
	class MockObserver implements Observer {
		private CountDownLatch mLatch;

		@Override
		public void onAllItemsRemoved() {
		}

		@Override
		public void onItemAdded(BookSearchItem item) {
		}

		@Override
		public void onItemsStateChanged() {
		}

		@Override
		public void onStateChanged(int newState) {
			if (newState != IsbnSearchService.STATE_RUNNING && mLatch != null) {
				mLatch.countDown();
				mLatch = null;
			}
		}

		public void waitUntilStoppedRunning() {
			if (mBinder.getState() != IsbnSearchService.STATE_RUNNING) {
				return;
			}
			assert (mLatch == null);
			mLatch = new CountDownLatch(1);
			try {
				mLatch.await();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	LocalBinder mBinder;
	private MockObserver mObserver;
	private IsbnSearchService mService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		final Context context = getInstrumentation().getTargetContext();
		final Intent startIntent = new Intent(context, IsbnSearchService.class);

		mService = new IsbnSearchService();
		mService.initDebug(context, mDb);
		mService.onCreate();
		mBinder = (LocalBinder) mService.onBind(startIntent);

		mObserver = new MockObserver();
		mBinder.addObserver(new WeakReference<Observer>(mObserver));

		HttpTransportCache.install(ApacheHttpTransport.INSTANCE, context);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		mService.onDestroy();
	}

	public void test11Isbns() throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBinder.addIsbn("9780152056490");
				mBinder.addIsbn("9781582618296");
				mBinder.addIsbn("9781589800427");
				mBinder.addIsbn("9780760332481");
				mBinder.addIsbn("invalid");
				mBinder.addIsbn("9780761963394");
				mBinder.addIsbn("9780738539607");
				mBinder.addIsbn("9780767931557");
				mBinder.addIsbn("9781438259406");
				mBinder.addIsbn("9780791097410");
				mBinder.addIsbn("9780761315186");
			}
		});

		mObserver.waitUntilStoppedRunning();
		assertEquals(IsbnSearchService.STATE_FINISHED, mBinder.getState());

		final List<BookSearchItem> items = mBinder.getItems();
		for (int i = 0; i < items.size(); i++) {
			assertEquals("Item: " + i, i != 4 ? BookSearchItem.FULL_SAVED
					: BookSearchItem.NOT_FOUND, items.get(i).state);
		}

		assertEquals("I like it when--", items.get(0).book.title);
		assertEquals("Tales from the Indianapolis Colts Sideline", items.get(1).book.title);
		assertEquals("The Ryder Cup", items.get(2).book.title);
		assertEquals("SEC Football", items.get(3).book.title);
		assertNull(items.get(4).book);
		assertEquals("Social network analysis", items.get(5).book.title);
		assertEquals("Green Bay Packers", items.get(6).book.title);
		assertEquals("The Accidental Billionaires", items.get(7).book.title);
		assertEquals("Scientific Search Engine Marketing", items.get(8).book.title);
		assertEquals("Kim Jong Il", items.get(9).book.title);
		assertEquals("Randy Moss", items.get(10).book.title);
		assertEquals(11, items.size());

		assertEquals(10, bookCount());
	}

	public void testFoundOne() throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBinder.addIsbn(Books.DragonTattoo.isbn13);
			}
		});

		mObserver.waitUntilStoppedRunning();
		assertEquals(IsbnSearchService.STATE_FINISHED, mBinder.getState());

		final List<BookSearchItem> items = mBinder.getItems();
		assertEquals(1, items.size());
		final GoogleBook book = items.get(0).book;
		assertNotNull(book);
		assertNotNull(book.databaseId);
		assertEquals(Books.DragonTattoo.googleId, book.googleId);
		assertEquals(Books.DragonTattoo.title, book.title);
		assertEquals(Books.DragonTattoo.creatorsText, book.creatorsText);

		assertEquals(1, bookCount());
	}

	public void testInvalidIsbn() throws Throwable {
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBinder.addIsbn("111");
			}
		});

		mObserver.waitUntilStoppedRunning();
		assertEquals(IsbnSearchService.STATE_FINISHED, mBinder.getState());

		final List<BookSearchItem> items = mBinder.getItems();
		assertEquals(1, items.size());
		final BookSearchItem item = items.get(0);
		assertNull(item.book);
		assertEquals(BookSearchItem.NOT_FOUND, item.state);

		assertEquals(0, bookCount());
	}

	public void testSearchingForBookInDatabase() throws Throwable {
		final long id = Books.DragonTattoo_Full.save(mDb);
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBinder.addIsbn(Books.DragonTattoo_Full.isbn13);
			}
		});

		mObserver.waitUntilStoppedRunning();
		assertEquals(IsbnSearchService.STATE_FINISHED, mBinder.getState());

		final List<BookSearchItem> items = mBinder.getItems();
		assertEquals(1, items.size());
		final GoogleBook book = items.get(0).book;
		assertNotNull(book);
		assertEquals(id, book.databaseId.longValue());
		assertEquals(Books.DragonTattoo_Full.title, book.title);
		assertEquals(Books.DragonTattoo_Full.creatorsText, book.creatorsText);
		assertEquals(Books.DragonTattoo_Full.pageCount, book.pageCount);
		assertEquals(Books.DragonTattoo_Full.releaseDate, book.releaseDate);
	}

	public void testSearchingForBookOneInDatabaseOneMissing() throws Throwable {
		final long id = Books.DragonTattoo_Full.save(mDb);
		runTestOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBinder.addIsbn(Books.DragonTattoo_Full.isbn13);
				mBinder.addIsbn(Books.Stardust.isbn13);
			}
		});

		mObserver.waitUntilStoppedRunning();
		assertEquals(IsbnSearchService.STATE_FINISHED, mBinder.getState());

		final List<BookSearchItem> items = mBinder.getItems();
		assertEquals(2, items.size());
		final GoogleBook b1 = items.get(0).book;
		assertNotNull(b1);
		assertEquals(id, b1.databaseId.longValue());
		assertEquals(Books.DragonTattoo_Full.title, b1.title);

		final GoogleBook b2 = items.get(1).book;
		assertNotNull(b2);
		assertNotNull(b2.databaseId);
		assertEquals(Books.Stardust.title, b2.title);

		assertEquals(2, bookCount());
	}
}
