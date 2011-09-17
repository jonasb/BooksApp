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
import com.wigwamlabs.util.DatabaseUtils;

public class BookCollectionCursor extends ExtendedSQLiteCursor {
	private static final int _id_index = 0;
	private static final int book_count_index = 1;
	private static final int book_id_index = 3;
	private static final String[] COLUMNS = { CollectionsTable._id, CollectionsTable.book_count,
			CollectionsTable.name, BookCollectionsTable.book_id };
	private static final CursorFactory FACTORY;
	private static final int name_index = 2;

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new BookCollectionCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static BookCollectionCursor fetchBookCollections(DatabaseAdapter db, long bookId,
			boolean includeAllCollections) {
		if (includeAllCollections) {
			final String sub = "SELECT " + BookCollectionsTable.book_id + " FROM "
					+ BookCollectionsTable.n + " WHERE " + BookCollectionsTable.collection_id
					+ " = " + ContactsTable._id + " AND " + BookCollectionsTable.book_id + " = "
					+ bookId;
			return (BookCollectionCursor) db.queryRaw(FACTORY, "SELECT " + CollectionsTable._id
					+ ", " + CollectionsTable.book_count + ", " + CollectionsTable.name + ", ("
					+ sub + ") FROM " + CollectionsTable.n, null, CursorType.COLLECTION_LIST);
		}
		return (BookCollectionCursor) db.query(FACTORY, CollectionsTable.n + ", "
				+ BookCollectionsTable.n, COLUMNS, CollectionsTable._id + " = "
				+ BookCollectionsTable.collection_id + " AND " + BookCollectionsTable.book_id
				+ " = " + bookId, null, null, null, null, null, CursorType.COLLECTION_LIST);
	}

	public BookCollectionCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public long _id() {
		return getLong(_id_index);
	}

	public int bookCount() {
		return getInt(book_count_index);
	}

	public Long bookId() {
		return DatabaseUtils.getLongOrNull(this, book_id_index);
	}

	public String name() {
		return getString(name_index);
	}
}
