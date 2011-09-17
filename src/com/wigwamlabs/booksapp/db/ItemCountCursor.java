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

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;

public class ItemCountCursor extends ExtendedSQLiteCursor {
	private static CursorFactory FACTORY;

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new ItemCountCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static ItemCountCursor fetchBookCount(DatabaseAdapter db) {
		return (ItemCountCursor) db.queryRaw(FACTORY, "SELECT count(" + BooksTable._id + ") FROM "
				+ BooksTable.n, null, CursorType.BOOK_LIST);
	}

	public ItemCountCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public int count() {
		return getInt(0);
	}
}
