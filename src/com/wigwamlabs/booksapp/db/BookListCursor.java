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

import static com.wigwamlabs.util.DatabaseUtils.checkFts;
import static com.wigwamlabs.util.DatabaseUtils.getDateOrNull;
import static com.wigwamlabs.util.DatabaseUtils.getIntOrNull;

import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;
import android.os.Build;
import android.text.TextUtils;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.util.Compatibility;
import com.wigwamlabs.util.DatabaseUtils;

public final class BookListCursor extends ExtendedSQLiteCursor {
	private static final int _id_index = 0;
	private static final String[] COLUMNS_STANDARD = new String[] {
			BooksTable.n + "." + BooksTable._id, BooksTable.title, BooksTable.title_normalized,
			BooksTable.n + "." + BooksTable.creators, BooksTable.release_date,
			BooksTable.page_count, BooksTable.loan_return_by };
	private static final String[] COLUMNS_WITH_LOAN_DATE;
	private static final String[] COLUMNS_WITH_VOLUME;
	private static final int creators_index = 3;
	private static final CursorFactory FACTORY;
	private static final String FETCH_ALL_FILTER_QUERY;
	private static final String FETCH_BY_BOOK_GROUP_FILTER_QUERY_STANDARD;
	private static final String FETCH_BY_BOOK_GROUP_FILTER_QUERY_VOLUME;
	private static final int loan_return_by_index = 6;
	private static final int page_count_index = 5;
	private static final int release_date_index = 4;
	private static final String SEARCH_ANY_QUERY;
	private static final int title_index = 1;
	public static final int title_normalized_index = 2;
	public static final int volume_index = 7; // COLUMNS_WITH_VOLUME

	static {
		// init columns
		COLUMNS_WITH_LOAN_DATE = new String[COLUMNS_STANDARD.length + 1];
		COLUMNS_WITH_VOLUME = new String[COLUMNS_STANDARD.length + 1];
		for (int i = 0; i < COLUMNS_STANDARD.length; i++) {
			COLUMNS_WITH_LOAN_DATE[i] = COLUMNS_STANDARD[i];
			COLUMNS_WITH_VOLUME[i] = COLUMNS_STANDARD[i];
		}
		COLUMNS_WITH_LOAN_DATE[COLUMNS_WITH_LOAN_DATE.length - 1] = LoansTable.out_date;
		COLUMNS_WITH_VOLUME[COLUMNS_WITH_VOLUME.length - 1] = BooksTable.volume;

		//
		final String allStandardColumns = TextUtils.join(", ", COLUMNS_STANDARD);
		final String allVolumeColumns = TextUtils.join(", ", COLUMNS_WITH_VOLUME);

		final StringBuilder q = new StringBuilder();
		q.append("SELECT ");
		q.append(allStandardColumns);
		q.append(" FROM " + BooksTable.n + ", " + BookFieldsTable.n);
		q.append(" WHERE " + BookFieldsTable.n + " MATCH ?");
		q.append(" AND " + BooksTable._id + " = " + BookFieldsTable.n + "." + BookFieldsTable.rowid);
		// TODO matchinfo() doens't seem to exist before Froyo,
		// workaround for earlier versions?
		if (Compatibility.SDK_INT >= Build.VERSION_CODES.FROYO) {
			q.append(" ORDER BY matchinfo(" + BookFieldsTable.n + ") DESC");
		}
		SEARCH_ANY_QUERY = q.toString();

		q.setLength(0);
		q.append("SELECT ");
		q.append(allStandardColumns);
		q.append(" FROM " + BooksTable.n + ", " + BookFieldsTable.n);
		q.append(" WHERE " + BookFieldsTable.n + " MATCH ?");
		q.append(" AND " + BooksTable._id + " = " + BookFieldsTable.n + "." + BookFieldsTable.rowid);
		FETCH_ALL_FILTER_QUERY = q.toString();

		q.setLength(0);
		q.append(" FROM " + BooksTable.n + ", " + BookFieldsTable.n + ", %s");
		q.append(" WHERE " + BookFieldsTable.n + " MATCH ?");
		q.append(" AND " + BooksTable.n + "." + BooksTable._id + " = " + BookFieldsTable.n + "."
				+ BookFieldsTable.rowid);
		final String fetchByBookGroupFilterQuery = q.toString();
		FETCH_BY_BOOK_GROUP_FILTER_QUERY_STANDARD = "SELECT " + allStandardColumns
				+ fetchByBookGroupFilterQuery;
		FETCH_BY_BOOK_GROUP_FILTER_QUERY_VOLUME = "SELECT " + allVolumeColumns
				+ fetchByBookGroupFilterQuery;

		FACTORY = new CursorFactory() {
			@Override
			public Cursor newCursor(SQLiteDatabase db, SQLiteCursorDriver masterQuery,
					String editTable, SQLiteQuery query) {
				return new BookListCursor(db, masterQuery, editTable, query);
			}
		};
	}

	private static BookListCursor doFetch(DatabaseAdapter db, String where, int orderBy,
			CharSequence filter) throws Exception {
		final String matchWith = DatabaseUtils.fts3FilterMatch(filter);
		if (matchWith == null) {
			return (BookListCursor) db.query(FACTORY, BooksTable.n, COLUMNS_STANDARD, where, null,
					null, null, COLUMNS_STANDARD[orderBy], null, CursorType.BOOK_LIST);
		}
		final String andWhere = (where != null ? " AND " + where : "");
		return (BookListCursor) checkFts(db.queryRaw(FACTORY, FETCH_ALL_FILTER_QUERY + andWhere
				+ " ORDER BY " + COLUMNS_STANDARD[orderBy], new String[] { matchWith },
				CursorType.BOOK_LIST));
	}

	private static BookListCursor doFetchGroup(DatabaseAdapter db, int orderBy,
			CharSequence filter, String additionalTables, String where) throws Exception {
		final String matchWith = DatabaseUtils.fts3FilterMatch(filter);
		if (matchWith == null) {
			return doFetchGroupNoFilter(db, orderBy, additionalTables, where);
		}
		final boolean useVolume = orderBy == volume_index;
		final String[] columns = (useVolume ? COLUMNS_WITH_VOLUME : COLUMNS_STANDARD);
		return (BookListCursor) checkFts(db.queryRaw(
				FACTORY,
				String.format(useVolume ? FETCH_BY_BOOK_GROUP_FILTER_QUERY_VOLUME
						: FETCH_BY_BOOK_GROUP_FILTER_QUERY_STANDARD, additionalTables)
						+ " AND " + where + " ORDER BY " + columns[orderBy],
				new String[] { matchWith }, CursorType.BOOK_LIST));
	}

	private static BookListCursor doFetchGroupNoFilter(DatabaseAdapter db, int orderBy,
			String additionalTables, String where) {
		final boolean useVolume = orderBy == volume_index;
		final String[] columns = (useVolume ? COLUMNS_WITH_VOLUME : COLUMNS_STANDARD);
		return (BookListCursor) db.query(FACTORY, BooksTable.n + ", " + additionalTables, columns,
				where, null, null, null, columns[orderBy], null, CursorType.BOOK_LIST);
	}

	public static BookListCursor fetchAll(DatabaseAdapter db, int orderBy, CharSequence filter)
			throws Exception {
		return doFetch(db, null, orderBy, filter);
	}

	public static BookListCursor fetchByAuthor(DatabaseAdapter db, int orderBy, long authorId) {
		final String whereAuthor = BooksTable._id + " = " + BookAuthorsTable.book_id + " AND "
				+ BookAuthorsTable.author_id + " = " + authorId;
		return doFetchGroupNoFilter(db, orderBy, BookAuthorsTable.n, whereAuthor);
	}

	public static BookListCursor fetchByCollection(DatabaseAdapter db, int orderBy,
			long collectionId, CharSequence filter) throws Exception {
		final String whereCollection = BooksTable.n + "." + BooksTable._id + " = "
				+ BookCollectionsTable.book_id + " AND " + BookCollectionsTable.collection_id
				+ " = " + collectionId;
		return doFetchGroup(db, orderBy, filter, BookCollectionsTable.n, whereCollection);
	}

	public static BookListCursor fetchByContact(DatabaseAdapter db, int orderBy, long contactId,
			CharSequence filter) throws Exception {
		final String whereContact = BooksTable.n + "." + BooksTable._id + " = "
				+ LoansTable.book_id + " AND " + LoansTable.contact_id + " = " + contactId;
		return doFetchGroup(db, orderBy, filter, LoansTable.n, whereContact);
	}

	public static BookListCursor fetchByIdRange(DatabaseAdapter db, long firstId, long lastId) {
		final String where = BooksTable._id + " >= " + firstId + " AND " + BooksTable._id + " <= "
				+ lastId;
		return (BookListCursor) db.query(FACTORY, BooksTable.n, COLUMNS_STANDARD, where, null,
				null, null, null, null, CursorType.BOOK_LIST);
	}

	public static BookListCursor fetchByPublisher(DatabaseAdapter db, int orderBy, long publisherId) {
		final String wherePublisher = BooksTable.n + "." + BooksTable._id + " = "
				+ BookPublishersTable.book_id + " AND " + BookPublishersTable.publisher_id + " = "
				+ publisherId;
		return doFetchGroupNoFilter(db, orderBy, BookPublishersTable.n, wherePublisher);
	}

	public static BookListCursor fetchBySeries(DatabaseAdapter db, int orderBy, long seriesId,
			CharSequence filter) throws Exception {
		final String where = BooksTable.n + "." + BooksTable._id + " = " + BookSeriesTable.book_id
				+ " AND " + BookSeriesTable.series_id + " = " + seriesId;
		return doFetchGroup(db, orderBy, filter, BookSeriesTable.n, where);
	}

	public static BookListCursor fetchBySubject(DatabaseAdapter db, int orderBy, long subjectId) {
		final String whereSubject = BooksTable.n + "." + BooksTable._id + " = "
				+ BookSubjectsTable.book_id + " AND " + BookSubjectsTable.subject_id + " = "
				+ subjectId;
		return doFetchGroupNoFilter(db, orderBy, BookSubjectsTable.n, whereSubject);
	}

	public static BookListCursor fetchExpiredLoans(DatabaseAdapter db, Date now, int orderBy,
			CharSequence filter) throws Exception {
		return doFetch(db, BooksTable.loan_return_by + " <= " + DatabaseUtils.dateToLong(now),
				orderBy, filter);
	}

	public static BookListCursor fetchLoansByContact(DatabaseAdapter db, long contactId,
			boolean activeLoans) {
		final String inDateEq = (activeLoans ? "IS" : "IS NOT");
		final String whereContact = BooksTable.n + "." + BooksTable._id + " = "
				+ LoansTable.book_id + " AND " + LoansTable.contact_id + " = " + contactId + " "
				+ "AND " + LoansTable.in_date + " " + inDateEq + " NULL";

		return (BookListCursor) db.query(FACTORY, BooksTable.n + ", " + LoansTable.n,
				COLUMNS_WITH_LOAN_DATE, whereContact, null, null, null,
				COLUMNS_WITH_LOAN_DATE[COLUMNS_WITH_LOAN_DATE.length - 1], null,
				CursorType.BOOK_LIST);
	}

	public static BookListCursor searchAny(DatabaseAdapter db, String keywords) throws Exception {
		return (BookListCursor) checkFts(db.queryRaw(FACTORY, SEARCH_ANY_QUERY,
				new String[] { keywords }, CursorType.BOOK_LIST));
	}

	/* package */BookListCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public long _id() {
		return getLong(_id_index);
	}

	public String creators() {
		return getString(creators_index);
	}

	public Date loanReturnBy() {
		return getDateOrNull(this, loan_return_by_index);
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