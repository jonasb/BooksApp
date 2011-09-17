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

import static com.wigwamlabs.util.DateUtils.format;
import static com.wigwamlabs.util.DateUtils.parseDate;

import java.util.Date;
import java.util.List;

import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.BooksTable;
import com.wigwamlabs.booksapp.db.LoanActions;
import com.wigwamlabs.booksapp.db.LoanHistoryCursor;

public class LoanTest extends DatabaseTestCase {
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

	private String bookList(BookListCursor contactLoansInactive) {
		return dumpCursor(contactLoansInactive, BooksTable._id, BooksTable.title);
	}

	private String bookLoanStatus(BookDetailCursor book) {
		book.moveToFirst();
		final String date = format(DATE_TIME_PATTERN, book.loanReturnBy());
		return book.loanedTo() + "|" + date + "|" + book.loanId();
	}

	private String lhq(LoanHistoryCursor history) {
		final StringBuilder res = new StringBuilder();
		final String dateFormat = DATE_TIME_PATTERN;

		for (history.moveToFirst(); !history.isAfterLast(); history.moveToNext()) {
			if (res.length() > 0)
				res.append("\n");
			res.append(history.name()).append("|").append(format(dateFormat, history.outDate()))
					.append("|").append(format(dateFormat, history.inDate()));
		}
		return res.toString();
	}

	public void testContactBookCountIsIncreasedWhenLoanAdded() {
		final long book1Id = addBook("A book").longValue();
		final long book2Id = addBook("Another book").longValue();
		final Date now = parseDate("yyyy", "2001");
		final Date returnBy = parseDate("yyyy", "2002");
		final BookGroupCursor contacts = BookGroupCursor.fetchAllContacts(mDb,
				BookGroupCursor.name_normalized_index, null);

		LoanActions.startLoan(mDb, book1Id, "123", "John Doe", now, returnBy);
		assertEquals("John Doe|1", bookGroup(contacts));

		LoanActions.startLoan(mDb, book2Id, "123", "John Doe", now, returnBy);
		assertEquals("John Doe|2", bookGroup(contacts));

		contacts.close();
	}

	public void testHistoryForOtherBookIsntRequeried() {
		final long bookId = addBook("A book").longValue();
		final LoanHistoryCursor bookHistory = LoanHistoryCursor.fetchFor(mDb, bookId);
		final long anotherBookId = addBook("Another book").longValue();
		final LoanHistoryCursor anotherBookHistory = LoanHistoryCursor.fetchFor(mDb, anotherBookId);

		final List<Boolean> bookChanged = observeCursorChange(bookHistory);
		final List<Boolean> anotherBookChanged = observeCursorChange(anotherBookHistory);

		// assert starting loan requeries the right cursor
		final long loanId = LoanActions.startLoan(mDb, bookId, "123", "John Doe",
				parseDate("yyyy", "2001"), parseDate("yyyy", "2002"));

		assertFalse(bookChanged.isEmpty());
		assertTrue(anotherBookChanged.isEmpty());

		bookChanged.clear();
		anotherBookChanged.clear();

		// assert returning book requeries the right cursor
		LoanActions.returnBook(mDb, bookId, parseDate("yyyy", "2002"), loanId);

		assertFalse(bookChanged.isEmpty());
		assertTrue(anotherBookChanged.isEmpty());

		bookHistory.close();
		anotherBookHistory.close();
	}

	public void testHistoryIsRequriedWhenLoanCreated() {
		final long id = addBook("Foo").longValue();

		final LoanHistoryCursor history = LoanHistoryCursor.fetchFor(mDb, id);
		assertEquals(0, history.getCount());

		final Date now = parseDate("yyyy", "2001");
		final Date loanReturnBy = parseDate("yyyy", "2002");
		LoanActions.startLoan(mDb, id, "123", "John Doe", now, loanReturnBy);

		assertEquals("John Doe|2001-01-01 00:00|null", lhq(history));

		history.close();
	}

	public void testLoaningBookSomeoneElseHasLoaned() {
		final long id = addBook("Ze Book").longValue();
		final BookListCursor contact1LoansActive = BookListCursor.fetchLoansByContact(mDb, 1, true);
		final BookListCursor contact1LoansInactive = BookListCursor.fetchLoansByContact(mDb, 1,
				false);
		final String dateFormat = "yyyy-MM-dd";
		final Date now = parseDate(dateFormat, "2010-09-25");
		final Date loanReturnBy = parseDate(dateFormat, "2010-10-25");
		final Date returnedAt = parseDate(dateFormat, "2010-09-27");

		final long loan1 = LoanActions.startLoan(mDb, id, "123", "John Doe", now, loanReturnBy);

		assertEquals("1|Ze Book", bookList(contact1LoansActive));
		assertEquals("", bookList(contact1LoansInactive));

		LoanActions.returnBook(mDb, id, returnedAt, loan1);

		assertEquals("", bookList(contact1LoansActive));
		assertEquals("1|Ze Book", bookList(contact1LoansInactive));

		LoanActions.startLoan(mDb, id, "234", "Jane Doe", now, returnedAt);

		assertEquals("", bookList(contact1LoansActive));
		assertEquals("1|Ze Book", bookList(contact1LoansInactive));

		contact1LoansActive.close();
		contact1LoansInactive.close();
	}

	public void testLoanSequence() {
		final long id = addBook("Ze Book").longValue();
		final BookDetailCursor book = BookDetailCursor.fetchBook(mDb, id);
		final LoanHistoryCursor history = LoanHistoryCursor.fetchFor(mDb, id);
		final BookGroupCursor contacts = BookGroupCursor.fetchAllContacts(mDb,
				BookGroupCursor.name_normalized_index, null);
		final BookListCursor contactLoansActive = BookListCursor.fetchLoansByContact(mDb, 1, true);
		final BookListCursor contactLoansInactive = BookListCursor.fetchLoansByContact(mDb, 1,
				false);
		final String dateFormat = "yyyy-MM-dd";
		final Date now = parseDate(dateFormat, "2010-09-25");
		final Date loanReturnBy = parseDate(dateFormat, "2010-10-25");
		final Date returnedAt = parseDate(dateFormat, "2010-09-27");

		final long loanId = LoanActions.startLoan(mDb, id, "123", "John Doe", now, loanReturnBy);

		assertEquals("John Doe|2010-10-25 00:00|1", bookLoanStatus(book));
		assertEquals("John Doe|2010-09-25 00:00|null", lhq(history));
		assertEquals("John Doe|1", bookGroup(contacts));
		assertEquals("1|Ze Book", bookList(contactLoansActive));
		assertEquals("", bookList(contactLoansInactive));

		LoanActions.returnBook(mDb, id, returnedAt, loanId);

		assertEquals("null|null|null", bookLoanStatus(book));
		assertEquals("John Doe|2010-09-25 00:00|2010-09-27 00:00", lhq(history));
		assertEquals("John Doe|1", bookGroup(contacts));
		assertEquals("", bookList(contactLoansActive));
		assertEquals("1|Ze Book", bookList(contactLoansInactive));

		book.close();
		history.close();
		contacts.close();
		contactLoansActive.close();
		contactLoansInactive.close();
	}

	public void testOrder() {
		final long book1 = addBook("Book 1").longValue();
		final long book2 = addBook("Book 2").longValue();

		LoanActions.startLoan(mDb, book2, "123", "John Doe", new Date(100), new Date(120));
		LoanActions.startLoan(mDb, book1, "123", "John Doe", new Date(130), new Date(140));
		LoanActions.startLoan(mDb, book2, "123", "John Doe", new Date(160), new Date(170));

		final BookListCursor c = BookListCursor.fetchLoansByContact(mDb, 1, true);
		assertEquals("2|Book 2\n1|Book 1\n2|Book 2", bookList(c));
		c.close();
	}
}
