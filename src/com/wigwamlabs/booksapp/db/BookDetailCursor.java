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
import static com.wigwamlabs.util.DatabaseUtils.getFloatOrNull;
import static com.wigwamlabs.util.DatabaseUtils.getIntOrNull;
import static com.wigwamlabs.util.DatabaseUtils.getLongOrNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;
import android.text.TextUtils;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.util.CollectionUtils;
import com.wigwamlabs.util.StringUtils;

public final class BookDetailCursor extends ExtendedSQLiteCursor {
	private static final String[] columns = new String[] { BooksTable.cover_url,
			BooksTable.n + "." + BooksTable.creators, BookFieldsTable.description,
			BooksTable.dimensions, BooksTable.google_id, BooksTable.isbn10, BooksTable.isbn13,
			BooksTable.loan_id, BooksTable.loan_return_by, BookFieldsTable.notes,
			BooksTable.page_count, BooksTable.publisher, BooksTable.rating,
			BooksTable.release_date, BooksTable.series, BooksTable.subjects, BooksTable.subtitle,
			BooksTable.title, BooksTable.volume };

	private static final int cover_url_index = 0;
	private static final int creators_index = 1;
	private static final int description_index = 2;
	private static final int dimensions_index = 3;
	private static CursorFactory FACTORY;
	private static final int google_id_index = 4;
	private static final int isbn10_index = 5;
	private static final int isbn13_index = 6;
	private static final int loan_id_index = 7;
	private static final int loan_return_by_index = 8;
	private static final int loaned_to_index = 19; // in sub query
	private static final int notes_index = 9;
	private static final int page_count_index = 10;
	private static final int publisher_index = 11;
	private static final String QUERY;
	private static final int rating_index = 12;
	private static final int release_date_index = 13;
	private static final int series_index = 14;
	private static final int subjects_index = 15;
	private static final int subtitle_index = 16;
	private static final int title_index = 17;
	private static final int volume_index = 18;

	static {
		String sub = "SELECT " + ContactsTable.name;
		sub += " FROM " + LoansTable.n + ", " + ContactsTable.n;
		sub += " WHERE " + BooksTable.loan_id + " = " + LoansTable.n + "." + LoansTable._id;
		sub += " AND " + LoansTable.contact_id + " = " + ContactsTable.n + "." + ContactsTable._id;

		final StringBuilder q = new StringBuilder();
		q.append("SELECT ");
		q.append(TextUtils.join(", ", columns));
		q.append(", (" + sub + ")");
		q.append(" FROM " + BooksTable.n + ", " + BookFieldsTable.n);
		q.append(" WHERE ");
		q.append(BooksTable._id + " = ?");
		q.append(" AND ");
		q.append(BookFieldsTable.n + "." + BookFieldsTable.rowid + " = " + BooksTable._id);
		QUERY = q.toString();

		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new BookDetailCursor(db, masterQuery, editTable, query);
			}
		};
	}

	public static BookDetailCursor fetchBook(DatabaseAdapter db, long bookId) {
		final BookDetailCursor c = (BookDetailCursor) db.queryRaw(FACTORY, QUERY,
				new String[] { Long.toString(bookId) }, CursorType.bookDetail(bookId));
		return c;
	}

	public static Long findBookByIds(DatabaseAdapter db, String isbn10, String isbn13,
			String googleId) {
		final List<String> ids = new ArrayList<String>(3);
		final StringBuilder where = new StringBuilder();

		if (isbn10 != null) {
			ids.add(isbn10);
			where.append(BooksTable.isbn10).append(" = ?");
		}

		if (isbn13 != null) {
			ids.add(isbn13);
			if (where.length() > 0)
				where.append(" OR ");
			where.append(BooksTable.isbn13).append(" = ?");
		}

		if (googleId != null) {
			ids.add(googleId);
			if (where.length() > 0)
				where.append(" OR ");
			where.append(BooksTable.google_id).append(" = ?");
		}

		if (ids.size() == 0)
			return null;

		final Cursor c = db.query(BooksTable.n, new String[] { BooksTable._id }, where.toString(),
				CollectionUtils.listToArray(ids), null, null, null, "1");
		Long id = null;
		if (c.moveToFirst())
			id = Long.valueOf(c.getLong(0));
		c.close();
		return id;
	}

	public BookDetailCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public String coverUrl() {
		return getString(cover_url_index);
	}

	public String creators() {
		return getString(creators_index);
	}

	public String description() {
		return getString(description_index);
	}

	public String dimensions() {
		return getString(dimensions_index);
	}

	public String googleId() {
		return getString(google_id_index);
	}

	public String infoUrl() {
		// same implementation as GoogleBook.infoUrl()
		final String theGoogleId = StringUtils.trimmedStringOrNull(googleId());
		if (theGoogleId == null)
			return null;
		return "http://books.google.com/books?id=" + theGoogleId;
	}

	public String isbn10() {
		return getString(isbn10_index);
	}

	public String isbn13() {
		return getString(isbn13_index);
	}

	public String loanedTo() {
		return getString(loaned_to_index);
	}

	public Long loanId() {
		return getLongOrNull(this, loan_id_index);
	}

	public Date loanReturnBy() {
		return getDateOrNull(this, loan_return_by_index);
	}

	public String notes() {
		return getString(notes_index);
	}

	public Integer pageCount() {
		return getIntOrNull(this, page_count_index);
	}

	public String publisher() {
		return getString(publisher_index);
	}

	public Float rating() {
		return getFloatOrNull(this, rating_index);
	}

	public Date releaseDate() {
		return getDateOrNull(this, release_date_index);
	}

	public String series() {
		return getString(series_index);
	}

	public String subjects() {
		return getString(subjects_index);
	}

	public String subtitle() {
		return getString(subtitle_index);
	}

	public String title() {
		return getString(title_index);
	}

	public Integer volume() {
		return getIntOrNull(this, volume_index);
	}
}