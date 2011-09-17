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

import com.wigwamlabs.util.DatabaseUtils;

public final class LoanStatusCursor extends ExtendedSQLiteCursor {
	private static final String[] columns = new String[] { BooksTable.loan_return_by };

	private static CursorFactory FACTORY;
	private static final int loan_return_by_index = 0;

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new LoanStatusCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static LoanStatusCursor fetchForBook(DatabaseAdapter db, long id) {
		return (LoanStatusCursor) db.query(FACTORY, BooksTable.n, columns, BooksTable._id + "="
				+ id, null, null, null, null, null, null);
	}

	/* package */LoanStatusCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public Date loanReturnBy() {
		return DatabaseUtils.getDateOrNull(this, loan_return_by_index);
	}
}
