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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookFeed;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.googlebooks.GoogleBookSearch.FeedSearch;
import com.wigwamlabs.util.CommaStringList;
import com.wigwmlabs.booksapp.test.ExpectedResult.Books;

public class GoogleBookToDatabaseTest extends DatabaseTestCase {
	public void testDatabaseIdIsProvidedWhenSearching() throws IOException {
		final long id = Books.DragonTattoo_Full.save(mDb);

		final GoogleBookSearch bookSearch = new GoogleBookSearch(getInstrumentation()
				.getTargetContext());
		final FeedSearch search = bookSearch.searchByIsbns(Books.DragonTattoo_Full.isbn13);
		search.setDatabase(mDb);
		final GoogleBookFeed result = search.execute();
		final GoogleBook book = result.books.get(0);
		assertEquals(id, book.databaseId.longValue());
	}

	public void testInsertingTriggersRequery() throws Exception {
		final BookListCursor all = BookListCursor.fetchAll(mDb,
				BookListCursor.title_normalized_index, null);
		final BookListCursor searchAny = BookListCursor.searchAny(mDb, "whatever");
		final List<Boolean> allHasChanged = observeCursorChange(all);
		final List<Boolean> searchAnyHasChanged = observeCursorChange(searchAny);

		final GoogleBook b = new GoogleBook();
		b.save(mDb);

		assertFalse(allHasChanged.isEmpty());
		assertFalse(searchAnyHasChanged.isEmpty());

		all.close();
		searchAny.close();
	}

	public void testNullBook() {
		final GoogleBook b = new GoogleBook();
		final long id = b.save(mDb);
		assertTrue(id >= 0);

		final BookDetailCursor c = BookDetailCursor.fetchBook(mDb, id);
		c.moveToFirst();
		for (int i = 0; i < c.getColumnCount(); i++) {
			assertTrue(c.isNull(i));
		}
		c.close();
	}

	public void testOldCursorsAreDeleted() throws Exception {
		{
			// test that using the db directly works as expected
			final SQLiteDatabase db = mDb.getDb();
			Cursor c = db.query("Books", null, null, null, null, null, null);
			final WeakReference<Cursor> weakC = new WeakReference<Cursor>(c);
			c.close();
			c = null;
			System.gc();
			assertNull(weakC.get());
		}

		{
			// make sure DatabaseAdapter doesn't leak memory
			BookListCursor c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index,
					null);
			final WeakReference<Cursor> weakC = new WeakReference<Cursor>(c);
			c.close();
			c = null;
			System.gc();
			assertNull(weakC.get());

			// trigger DatabaseAdapter to invalidate all its cursors which will
			// remove the weak reference to the now deleted cursor
			addBook("Any");
		}
	}

	public void testSavingFullBook() {
		final GoogleBook book = ExpectedResult.Books.DragonTattoo_Full;
		final long id = book.save(mDb);
		assertTrue(id >= 0);
		assertEquals(id, book.databaseId.longValue());

		final BookDetailCursor b = BookDetailCursor.fetchBook(mDb, id);
		b.moveToFirst();
		assertEquals(book.googleId, b.googleId());
		assertEquals(book.isbn10, b.isbn10());
		assertEquals(book.isbn13, b.isbn13());
		assertEquals(book.title, b.title());
		assertEquals(book.subtitle, b.subtitle());
		assertEquals("Stieg Larsson", b.creators());
		assertEquals(book.publisher, b.publisher());
		assertEquals(book.releaseDate, b.releaseDate());
		assertEquals(book.description, b.description());
		assertEquals(book.pageCount, b.pageCount());
		assertEquals(book.dimensions, b.dimensions());
		assertEquals(CommaStringList.listToString(book.subjects), b.subjects());
		assertEquals(book.thumbnailLargeUrl, b.coverUrl());
		b.close();
	}
}
