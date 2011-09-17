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

package com.wigwamlabs.booksapp.db;

import java.util.Date;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;

public class LoanActions {
	private static final ManyToManyActions ACTIONS = new ManyToManyActions(ContactsTable.n,
			ContactsTable.n + "." + ContactsTable._id, ContactsTable.name,
			ContactsTable.name_normalized, true, ContactsTable.book_count, LoansTable.n,
			LoansTable.book_id, LoansTable.contact_id);
	private static final String INCREMENT_BOOK_COUNT = "UPDATE " + ContactsTable.n + " SET "
			+ ContactsTable.book_count + " = " + ContactsTable.book_count + " + 1 WHERE "
			+ ContactsTable._id + " = ";

	private static void incrementContactBookCount(DatabaseAdapter db, int t, long contactId) {
		db.execSQL(t, INCREMENT_BOOK_COUNT + contactId);
	}

	public static void removeLoansForBook(DatabaseAdapter db, int t, long bookId) {
		ACTIONS.updateItems(db, t, bookId, null, true, true, CursorType.CONTACT_LIST);
	}

	public static void returnBook(DatabaseAdapter db, long bookId, Date now, long loanId) {
		try {
			final int t = db.beginTransaction();

			final BookEntry be = new BookEntry();
			be.setLoanId(null);
			be.setLoanReturnBy(null);
			be.executeUpdate(db, t, bookId); // updates book detail cursors

			final LoanEntry le = new LoanEntry();
			le.setInDate(now);
			le.executeUpdate(db, t, loanId);

			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}

		db.requeryCursors(CursorType.loanHistory(bookId));
	}

	public static long startLoan(DatabaseAdapter db, long bookId, String systemContactId,
			String contactName, final Date now, final Date loanReturnBy) {

		long loanId;
		try {
			final int t = db.beginTransaction();

			final long contactId = ContactEntry.findOrCreate(db, t, systemContactId, contactName);

			final LoanEntry le = new LoanEntry();
			le.setBookId(bookId);
			le.setContactId(contactId);
			le.setOutDate(now);
			loanId = le.executeInsert(db, t);

			final BookEntry be = new BookEntry();
			be.setLoanId(Long.valueOf(loanId));
			be.setLoanReturnBy(loanReturnBy);
			be.executeUpdate(db, t, bookId);

			incrementContactBookCount(db, t, contactId);

			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}

		db.requeryCursors(CursorType.loanHistory(bookId));
		db.requeryCursors(CursorType.CONTACT_LIST);

		return loanId;
	}
}
