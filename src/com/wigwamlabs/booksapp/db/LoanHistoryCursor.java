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

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.util.DatabaseUtils;

public class LoanHistoryCursor extends ExtendedSQLiteCursor {
	private static final String[] columns = new String[] { LoansTable.n + "." + LoansTable._id,
			LoansTable.in_date, ContactsTable.name, LoansTable.out_date };

	// private static final int _id_index = 0;
	private static final CursorFactory FACTORY;
	private static final int in_date_index = 1;
	private static final int name_index = 2;
	private static final int out_date_index = 3;

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new LoanHistoryCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static LoanHistoryCursor fetchFor(DatabaseAdapter db, long bookId) {
		final LoanHistoryCursor c = (LoanHistoryCursor) db.query(FACTORY, LoansTable.n + ", "
				+ ContactsTable.n, columns, LoansTable.book_id + "=" + bookId + " AND "
				+ LoansTable.contact_id + " = " + ContactsTable.n + "." + ContactsTable._id, null,
				null, null, LoansTable.out_date + " DESC", null, CursorType.loanHistory(bookId));
		return c;
	}

	/* package */LoanHistoryCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public Date inDate() {
		return DatabaseUtils.getDateOrNull(this, in_date_index);
	}

	public String name() {
		return getString(name_index);
	}

	public Date outDate() {
		return DatabaseUtils.getDateOrNull(this, out_date_index);
	}
}
