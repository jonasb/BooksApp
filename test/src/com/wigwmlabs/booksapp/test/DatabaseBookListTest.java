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

import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.LoanActions;

public class DatabaseBookListTest extends DatabaseTestCase {
	private long addBookWithSeries(String title, String series, Integer volume) {
		final BookEntry be = new BookEntry();
		be.setTitle(title, null);
		be.setSeries(series, volume);
		return be.executeInsertInTransaction(mDb);
	}

	public void testFetchAllFilter() throws Exception {
		final Long ab_mn = addBook("ab mn");
		final Long b_n = addBook("b n");
		final Long a_m = addBook("a m");
		final Long abc_mno = addBook("abc mno");
		BookListCursor c;

		// no filter
		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, null);
		assertListEquals(c, a_m, ab_mn, abc_mno, b_n);
		c.close();

		// single filter
		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "a");
		assertListEquals(c, a_m, ab_mn, abc_mno);
		c.close();

		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "ab");
		assertListEquals(c, ab_mn, abc_mno);
		c.close();

		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "abc");
		assertListEquals(c, abc_mno);
		c.close();

		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "abcd");
		assertListEquals(c);
		c.close();

		// multiple filters
		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "a m");
		assertListEquals(c, a_m, ab_mn, abc_mno);
		c.close();

		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "a mn");
		assertListEquals(c, ab_mn, abc_mno);
		c.close();

		c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "a mno");
		assertListEquals(c, abc_mno);
		c.close();
	}

	public void testFetchAllOrder() throws Exception {
		final Long m = addBook("m");
		final Long _3 = addBook("'3'");
		final Long z = addBook("z");
		final Long Ab = addBook("Ab");
		final Long a = addBook("a");
		final Long Aa = addBook("Aa");
		final Long _2 = addBook("2");

		final BookListCursor c = BookListCursor.fetchAll(mDb,
				BookListCursor.title_normalized_index, null);

		assertListEquals(c, _2, _3, a, Aa, Ab, m, z);

		c.close();
	}

	public void testFetchBySeriesOrdered() throws Exception {
		final long a = addBookWithSeries("a", "Meep", 5);
		final long b = addBookWithSeries("b", "Meep", 1);
		final long c = addBookWithSeries("c", "Meep", null);

		final BookListCursor books = BookListCursor.fetchBySeries(mDb,
				BookListCursor.volume_index, 1, null);
		assertListEquals(books, c, b, a);
		books.close();
	}

	public void testFetchExpired() throws Exception {
		addBook("Not loaned");
		final Long expiredLoan = addBook("Expired loan");
		final Long unexpiredLoan = addBook("Unexpired loan");
		final Date earlier = new Date(10000);
		final Date now = new Date(20000);
		final Date later = new Date(30000);

		LoanActions.startLoan(mDb, expiredLoan.longValue(), "1", "contact", now, earlier);
		LoanActions.startLoan(mDb, unexpiredLoan.longValue(), "1", "contact", now, later);

		BookListCursor c = BookListCursor.fetchExpiredLoans(mDb, now,
				BookListCursor.title_normalized_index, null);
		assertListEquals(c, expiredLoan);
		c.close();

		c = BookListCursor.fetchExpiredLoans(mDb, now, BookListCursor.title_normalized_index,
				"Expired");
		assertListEquals(c, expiredLoan);
		c.close();

		c = BookListCursor
				.fetchExpiredLoans(mDb, now, BookListCursor.title_normalized_index, "xyz");
		assertListEquals(c);
		c.close();
	}

	public void testInvalidFtsQuery() {
		BookListCursor c = null;
		try {
			c = BookListCursor.fetchAll(mDb, BookListCursor.title_normalized_index, "\"\"\"");
		} catch (final Exception e) {
		}
		assertNull(c);
	}
}
