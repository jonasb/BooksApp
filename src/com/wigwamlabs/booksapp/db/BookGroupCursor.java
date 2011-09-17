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

public class BookGroupCursor extends ExtendedSQLiteCursor {
	private static final int _id_index = 0;
	private static final String[] AUTHORS_COLUMNS = { AuthorsTable._id, AuthorsTable.book_count,
			AuthorsTable.name, AuthorsTable.name_normalized };
	private static final int book_count_index = 1;
	private static final String[] COLLECTIONS_COLUMNS = { CollectionsTable._id,
			CollectionsTable.book_count, CollectionsTable.name, CollectionsTable.name_normalized };
	private static final String[] CONTACTS_COLUMNS = { ContactsTable._id, ContactsTable.book_count,
			ContactsTable.name, ContactsTable.name_normalized };
	private static final CursorFactory FACTORY;
	private static final int name_index = 2;
	public static final int name_normalized_index = 3;
	private static final String[] PUBLISHERS_COLUMNS = { PublishersTable._id,
			PublishersTable.book_count, PublishersTable.name, PublishersTable.name_normalized };
	private static final String[] SERIES_COLUMNS = { SeriesTable._id, SeriesTable.book_count,
			SeriesTable.name, SeriesTable.name_normalized };
	private static final String[] SUBJECTS_COLUMNS = { SubjectsTable._id, SubjectsTable.book_count,
			SubjectsTable.name, SubjectsTable.name_normalized };

	static {
		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new BookGroupCursor(db, masterQuery, editTable, query);
			}
		};
	}

	private static BookGroupCursor doFetchAll(String table, String[] columns, String matchField,
			DatabaseAdapter db, int orderBy, CharSequence filter, CursorType cursorType) {
		final String filterLike = DatabaseUtils.filterLike(filter);
		final String orderByColumn = (orderBy >= 0 ? columns[orderBy] : null);
		if (filterLike == null) {
			return (BookGroupCursor) db.query(FACTORY, table, columns, null, null, null, null,
					orderByColumn, null, cursorType);
		}
		return (BookGroupCursor) db.query(FACTORY, table, columns, matchField + " LIKE ?",
				new String[] { filterLike }, null, null, orderByColumn, null, cursorType);
	}

	private static BookGroupCursor doFetchBooksBookGroup(String itemTable, String itemIdField,
			String joinTable, String joinMainEntityIdField, String joinItemIdField,
			String[] columns, DatabaseAdapter db, long bookId, int orderBy) {
		return (BookGroupCursor) db.query(FACTORY, itemTable + ", " + joinTable, columns,
				itemIdField + " = " + joinItemIdField + " AND " + joinMainEntityIdField + " = "
						+ bookId, null, null, null, columns[orderBy], null,
				CursorType.bookDetail(bookId));
	}

	public static BookGroupCursor fetchAllAuthors(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		// TODO only filters from start... should filter on last name as well?
		return doFetchAll(AuthorsTable.n, AUTHORS_COLUMNS, AuthorsTable.name, db, orderBy, filter,
				CursorType.AUTHOR_LIST);
	}

	public static BookGroupCursor fetchAllCollections(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		return doFetchAll(CollectionsTable.n, COLLECTIONS_COLUMNS, CollectionsTable.name, db,
				orderBy, filter, CursorType.COLLECTION_LIST);
	}

	public static BookGroupCursor fetchAllContacts(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		return doFetchAll(ContactsTable.n, CONTACTS_COLUMNS, ContactsTable.name, db, orderBy,
				filter, CursorType.CONTACT_LIST);
	}

	public static BookGroupCursor fetchAllPublishers(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		return doFetchAll(PublishersTable.n, PUBLISHERS_COLUMNS, PublishersTable.name, db, orderBy,
				filter, CursorType.PUBLISHER_LIST);
	}

	public static BookGroupCursor fetchAllSeries(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		return doFetchAll(SeriesTable.n, SERIES_COLUMNS, SeriesTable.name, db, orderBy, filter,
				CursorType.SERIES_LIST);
	}

	public static BookGroupCursor fetchAllSubjects(DatabaseAdapter db, int orderBy,
			CharSequence filter) {
		return doFetchAll(SubjectsTable.n, SUBJECTS_COLUMNS, SubjectsTable.name, db, orderBy,
				filter, CursorType.SUBJECT_LIST);
	}

	public static BookGroupCursor fetchBookAuthors(DatabaseAdapter db, long bookId, int orderBy) {
		return doFetchBooksBookGroup(AuthorsTable.n, AuthorsTable._id, BookAuthorsTable.n,
				BookAuthorsTable.book_id, BookAuthorsTable.author_id, AUTHORS_COLUMNS, db, bookId,
				orderBy);
	}

	public static BookGroupCursor fetchBookPublishers(DatabaseAdapter db, long bookId, int orderBy) {
		return doFetchBooksBookGroup(PublishersTable.n, PublishersTable._id, BookPublishersTable.n,
				BookPublishersTable.book_id, BookPublishersTable.publisher_id, PUBLISHERS_COLUMNS,
				db, bookId, orderBy);
	}

	public static Cursor fetchBookSeries(DatabaseAdapter db, long bookId, int orderBy) {
		return doFetchBooksBookGroup(SeriesTable.n, SeriesTable._id, BookSeriesTable.n,
				BookSeriesTable.book_id, BookSeriesTable.series_id, SERIES_COLUMNS, db, bookId,
				orderBy);
	}

	public static BookGroupCursor fetchBookSubjects(DatabaseAdapter db, long bookId, int orderBy) {
		return doFetchBooksBookGroup(SubjectsTable.n, SubjectsTable._id, BookSubjectsTable.n,
				BookSubjectsTable.book_id, BookSubjectsTable.subject_id, SUBJECTS_COLUMNS, db,
				bookId, orderBy);
	}

	public BookGroupCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public long _id() {
		return getLong(_id_index);
	}

	public int bookCount() {
		return getInt(book_count_index);
	}

	public String name() {
		return getString(name_index);
	}
}
