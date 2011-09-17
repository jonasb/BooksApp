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

import java.util.List;

import android.database.Cursor;

import com.wigwamlabs.booksapp.db.AuthorsTable;
import com.wigwamlabs.booksapp.db.BookAuthorsTable;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.util.CommaStringList;

public class AuthorUpdateTest extends DatabaseTestCase {
	private String dumpAuthors() {
		final String[] columns = { AuthorsTable._id, AuthorsTable.name, AuthorsTable.book_count };
		final Cursor a = mDb.query(AuthorsTable.n, columns, null, null, null, null, null, null);

		String dump = "";
		for (a.moveToFirst(); !a.isAfterLast(); a.moveToNext()) {
			if (dump.length() > 0)
				dump += "\n";
			dump += a.getString(0) + "|" + a.getString(1) + "|" + a.getString(2);
		}
		a.close();
		return dump;
	}

	private String dumpBookAuthors() {
		final String[] columns = { BookAuthorsTable.book_id, BookAuthorsTable.author_id };
		final Cursor a = mDb.query(BookAuthorsTable.n, columns, null, null, null, null, null, null);

		String dump = "";
		for (a.moveToFirst(); !a.isAfterLast(); a.moveToNext()) {
			if (dump.length() > 0)
				dump += "\n";
			dump += a.getString(0) + "|" + a.getString(1);
		}
		a.close();
		return dump;
	}

	public void testAddMultipleAuthors() {
		addBook("Foo", null, "Author One, Author Two", null);

		assertEquals("1|Author One|1\n2|Author Two|1", dumpAuthors());
		assertEquals("1|1\n1|2", dumpBookAuthors());
	}

	public void testAddTwoBooksWithSameAuthor() {
		addBook("Foo", null, "Author One", null);
		addBook("Bar", null, "Author Two, Author One", null);

		assertEquals("1|Author One|2\n2|Author Two|1", dumpAuthors());
		assertEquals("1|1\n2|2\n2|1", dumpBookAuthors());
	}

	public void testChangingAuthorsRequeriesCursor() {
		final BookGroupCursor c = BookGroupCursor.fetchAllAuthors(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> hasChanged = observeCursorChange(c);

		addBook("Foo", null, "John Doe", null);

		assertFalse(hasChanged.isEmpty());
		assertEquals(1, c.getCount());
		c.moveToFirst();
		assertEquals("John Doe", c.name());
		assertEquals(1, c.bookCount());
		c.close();
	}

	public void testRemoving12Authors() {
		final long id = addBook("Foo", null, "A, B, C, D, E, F, G, H, I, J, K, L, M", null)
				.longValue();

		updateCreators(id, "C");

		assertEquals("3|C|1", dumpAuthors());
		assertEquals("1|3", dumpBookAuthors());
	}

	public void testRemovingAuthorDecrementsCount() {
		final long id = addBook("Foo", null, "Author One", null).longValue();
		addBook("Bar", null, "Author One", null);

		updateCreators(id, null);

		assertEquals("1|Author One|1", dumpAuthors());
		assertEquals("2|1", dumpBookAuthors());
	}

	public void testRemovingAuthorRemovesAuthor() {
		final long id = addBook("Foo", null, "Author One", null).longValue();

		updateCreators(id, null);

		assertEquals("", dumpAuthors());
		assertEquals("", dumpBookAuthors());
	}

	public void testRenamingAuthor() {
		final long id = addBook("Foo", null, "Author One", null).longValue();

		updateCreators(id, "Author Two");

		assertEquals("2|Author Two|1", dumpAuthors());
		assertEquals("1|2", dumpBookAuthors());
	}

	public void testSloppyCommas() {
		final long id = addBook("Foo", null, "Author One", null).longValue();

		updateCreators(id, " Author One ,,  , Author Two, ");

		assertEquals("1|Author One|1\n2|Author Two|1", dumpAuthors());
		assertEquals("1|1\n1|2", dumpBookAuthors());
	}

	public void testUpdateAddAuthor() {
		final long id = addBook("Foo", null, "Author One", null).longValue();

		updateCreators(id, "Author Two, Author One");

		assertEquals("1|Author One|1\n2|Author Two|1", dumpAuthors());
		assertEquals("1|1\n1|2", dumpBookAuthors());
	}

	private void updateCreators(final long id, String creators) {
		final BookEntry u = new BookEntry();
		u.setCreators(CommaStringList.stringToList(creators));
		u.executeUpdateInTransaction(mDb, id);
	}
}
