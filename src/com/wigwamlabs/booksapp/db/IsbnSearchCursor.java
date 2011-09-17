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

import static com.wigwamlabs.util.DatabaseUtils.getDateOrNull;
import static com.wigwamlabs.util.DatabaseUtils.getIntOrNull;

import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;

import com.wigwamlabs.util.StringUtils;

public final class IsbnSearchCursor extends ExtendedSQLiteCursor {
	private static final int _id_index = 0;

	private static final String[] columns = new String[] { BooksTable._id, BooksTable.isbn13,
			BooksTable.title, BooksTable.creators, BooksTable.release_date, BooksTable.page_count };
	private static final int creators_index = 3;
	private static final CursorFactory FACTORY;
	private static final int isbn13_index = 1;
	private static final int page_count_index = 5;
	private static final int release_date_index = 4;
	private static final int title_index = 2;

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new IsbnSearchCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static IsbnSearchCursor searchByIsbns(DatabaseAdapter db, String[] isbns) {
		final String selection = StringUtils.copyJoin(BooksTable.isbn13 + "=?", " OR ",
				isbns.length);
		return (IsbnSearchCursor) db.query(FACTORY, BooksTable.n, columns, selection, isbns, null,
				null, null, null, null);
	}

	/* package */IsbnSearchCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public long _id() {
		return getLong(_id_index);
	}

	public String creators() {
		return getString(creators_index);
	}

	public String isbn13() {
		return getString(isbn13_index);
	}

	public Integer pageCount() {
		return getIntOrNull(this, page_count_index);
	}

	public Date releaseDate() {
		return getDateOrNull(this, release_date_index);
	}

	public String title() {
		return getString(title_index);
	}
}