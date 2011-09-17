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

import java.util.Date;
import java.util.List;

import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;

public class DatabaseBookUpdateTest extends DatabaseTestCase {
	public void testUpdateInvalidatesDetailCursor() {
		final long id = addBook("Foo").longValue();
		final long anotherBookId = addBook("Bar").longValue();
		final BookDetailCursor book = BookDetailCursor.fetchBook(mDb, id);
		final List<Boolean> allHasChanged = observeCursorChange(book);
		final BookDetailCursor anotherBook = BookDetailCursor.fetchBook(mDb, anotherBookId);
		final List<Boolean> anotherBookHasChanged = observeCursorChange(anotherBook);

		int i = 0;
		list: while (true) {
			final BookEntry u = new BookEntry();
			// all fields except googleId
			switch (i) {
			case 0:
				u.setCreators(CommaStringList.stringToList("John Doe"));
				break;
			case 1:
				u.setDescription("...");
				break;
			case 2:
				u.setDimensions("...");
				break;
			case 3:
				u.setIsbn10("0123456789");
				break;
			case 4:
				u.setIsbn13("0123456789012");
				break;
			case 5:
				u.setLoanReturnBy(DateUtils.parseDate("yyyy", "1980"));
				break;
			case 6:
				u.setPageCount(Integer.valueOf(123));
				break;
			case 7:
				u.setPublisher("...");
				break;
			case 8:
				u.setRating(Float.valueOf(3.3f));
				break;
			case 9:
				u.setReleaseDate(DateUtils.parseDate("yyyy", "1980"));
				break;
			case 10:
				u.setSubjects(CommaStringList.stringToList("..."));
				break;
			case 11:
				u.setTitle("Bar", "Baz");
				break;
			case 12:
				u.setNotes("notes");
				break;
			default:
				break list;
			}
			u.executeUpdateInTransaction(mDb, id);
			assertFalse("i=" + i, allHasChanged.isEmpty());
			allHasChanged.clear();

			i++;
		}

		assertTrue("other cursors shouldn't be invalidated", anotherBookHasChanged.isEmpty());

		book.close();
		anotherBook.close();
	}

	public void testUpdatesDontInvalidateListCursor() throws Exception {
		final long id = addBook("Foo").longValue();
		final BookListCursor all = BookListCursor.fetchAll(mDb,
				BookListCursor.title_normalized_index, null);
		final List<Boolean> allHasChanged = observeCursorChange(all);

		final BookEntry u = new BookEntry();
		u.setDescription("foo");
		u.setDimensions("foo");
		u.setLoanId(Long.valueOf(1));
		u.setNotes("foo");
		u.setPublisher("foo");
		u.setRating(Float.valueOf(1.0f));
		u.setSubjects(CommaStringList.stringToList("foo"));
		u.executeUpdateInTransaction(mDb, id);

		assertTrue(allHasChanged.isEmpty());

		all.close();
	}

	public void testUpdatesInvalidateListCursor() throws Exception {
		final long id = addBook("Foo").longValue();
		final BookListCursor all = BookListCursor.fetchAll(mDb,
				BookListCursor.title_normalized_index, null);
		final List<Boolean> allHasChanged = observeCursorChange(all);

		{
			final BookEntry u = new BookEntry();
			u.setTitle("Bar", "Baz");
			u.executeUpdateInTransaction(mDb, id);

			assertFalse(allHasChanged.isEmpty());
			allHasChanged.clear();
		}
		{
			final BookEntry u = new BookEntry();
			u.setCreators(CommaStringList.stringToList("John Doe"));
			u.executeUpdateInTransaction(mDb, id);

			assertFalse(allHasChanged.isEmpty());
			allHasChanged.clear();
		}
		{
			final BookEntry u = new BookEntry();
			u.setLoanReturnBy(DateUtils.parseDate("yyyy", "1980"));
			u.executeUpdateInTransaction(mDb, id);

			assertFalse(allHasChanged.isEmpty());
			allHasChanged.clear();
		}
		{
			final BookEntry u = new BookEntry();
			u.setPageCount(Integer.valueOf(123));
			u.executeUpdateInTransaction(mDb, id);

			assertFalse(allHasChanged.isEmpty());
			allHasChanged.clear();
		}
		{
			final BookEntry u = new BookEntry();
			final Date d = new Date();
			d.setYear(1980);
			d.setMonth(8);
			d.setDate(4);
			u.setReleaseDate(d);
			u.executeUpdateInTransaction(mDb, id);

			assertFalse(allHasChanged.isEmpty());
			allHasChanged.clear();
		}

		all.close();
	}
}
