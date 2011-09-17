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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DatabaseUtils;
import com.wigwamlabs.util.StringUtils;

public final class BookEntry {
	private static final List<String> COLLECTIONS_NOT_SET = new ArrayList<String>(0);

	public static void delete(DatabaseAdapter db, long id) {
		try {
			final int t = db.beginTransaction();

			AuthorActions.updateAuthors(db, t, id, null, true);
			PublisherActions.updatePublisher(db, t, id, null, true);
			SubjectActions.updateSubjects(db, t, id, null, true);
			CollectionActions.removeAllCollections(db, t, id);
			LoanActions.removeLoansForBook(db, t, id);

			db.delete(t, BooksTable.n, BooksTable._id + " = " + id, null);
			db.delete(t, BookFieldsTable.n, BookFieldsTable.rowid + " = " + id, null);

			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
		db.requeryCursors(CursorType.bookDetail(id));
		db.requeryCursors(CursorType.BOOK_LIST);
		db.onBookRemoved(id);
	}

	private final ContentValues mBookFieldsValues = new ContentValues();
	private final ContentValues mBooksValues = new ContentValues();
	private List<String> mCollections = COLLECTIONS_NOT_SET;
	private List<String> mCreators;
	private List<String> mSubjects;

	public long executeInsert(DatabaseAdapter db, int t) {
		// TODO handle exception
		final long id = db.insertOrThrow(t, BooksTable.n, mBooksValues);
		if (id >= 0) {
			mBookFieldsValues.put(BookFieldsTable.rowid, Long.valueOf(id));
			final long fieldId = db.insertOrThrow(t, BookFieldsTable.n, mBookFieldsValues);
			assert (fieldId == id);

			updateGroups(db, t, id, false);
		}

		db.requeryCursors(CursorType.BOOK_LIST);
		db.onBookAdded(id, mBooksValues.getAsString(BooksTable.google_id));
		return id;
	}

	public long executeInsertInTransaction(DatabaseAdapter db) {
		try {
			final int t = db.beginTransaction();
			final long id = executeInsert(db, t);
			db.setTransactionSuccessful(t);
			return id;
		} finally {
			db.endTransaction();
		}
	}

	public void executeUpdate(DatabaseAdapter db, int t, long id) {
		boolean detailHasChanged = false;
		if (mBooksValues.size() > 0) {
			final int n = db.update(t, BooksTable.n, mBooksValues, BooksTable._id + "=" + id);
			assert (n == 1);
			detailHasChanged = true;

			updateGroups(db, t, id, true);
		}
		if (mBookFieldsValues.size() > 0) {
			final int n = db.update(t, BookFieldsTable.n, mBookFieldsValues, BookFieldsTable.rowid
					+ "=" + id);
			assert (n == 1);
			detailHasChanged = true;
		}

		boolean listHasChanged = false;
		for (final String key : DatabaseAdapter.BOOK_LIST_COLUMNS) {
			if (mBooksValues.containsKey(key)) {
				listHasChanged = true;
				break;
			}
		}
		if (listHasChanged) {
			db.requeryCursors(CursorType.BOOK_LIST);
		}

		if (detailHasChanged) {
			db.requeryCursors(CursorType.bookDetail(id));
		}
	}

	public void executeUpdateInTransaction(DatabaseAdapter db, long id) {
		try {
			final int t = db.beginTransaction();
			executeUpdate(db, t, id);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
	}

	public void setCollections(List<String> value) {
		mCollections = value;
	}

	public void setCoverUrl(String value) {
		mBooksValues.put(BooksTable.cover_url, value);
	}

	public void setCreators(List<String> list) {
		mCreators = list;
		final String value = CommaStringList.listToString(list);
		mBooksValues.put(BooksTable.creators, value);
		mBookFieldsValues.put(BookFieldsTable.creators, value);
	}

	public void setDescription(String value) {
		mBookFieldsValues.put(BookFieldsTable.description, value);
	}

	public void setDimensions(String value) {
		mBooksValues.put(BooksTable.dimensions, value);
	}

	public void setGoogleId(String value) {
		mBooksValues.put(BooksTable.google_id, value);
	}

	public void setIsbn10(String value) {
		mBooksValues.put(BooksTable.isbn10, value);
	}

	public void setIsbn13(String value) {
		mBooksValues.put(BooksTable.isbn13, value);
	}

	public void setLoanId(Long value) {
		mBooksValues.put(BooksTable.loan_id, value);
	}

	public void setLoanReturnBy(Date value) {
		mBooksValues.put(BooksTable.loan_return_by, DatabaseUtils.dateToLong(value));
	}

	public void setNotes(String notes) {
		mBookFieldsValues.put(BookFieldsTable.notes, notes);
	}

	public void setPageCount(Integer value) {
		mBooksValues.put(BooksTable.page_count, value);
	}

	public void setPublisher(String value) {
		mBooksValues.put(BooksTable.publisher, value);
	}

	public void setRating(Float value) {
		mBooksValues.put(BooksTable.rating, value);
	}

	public void setReleaseDate(Date value) {
		mBooksValues.put(BooksTable.release_date, DatabaseUtils.dateToLong(value));
	}

	public void setSeries(String series, Integer volume) {
		mBooksValues.put(BooksTable.series, series);
		mBooksValues.put(BooksTable.volume, volume);
	}

	public void setSubjects(List<String> list) {
		mSubjects = list;
		final String value = CommaStringList.listToString(list);
		mBooksValues.put(BooksTable.subjects, value);
	}

	public void setTitle(String title, String subtitle) {
		mBooksValues.put(BooksTable.title, title);
		mBooksValues.put(BooksTable.subtitle, subtitle);

		mBooksValues.put(BooksTable.title_normalized, StringUtils.normalizeExtreme(title));

		String fulltitle = title;
		if (subtitle != null)
			fulltitle = (fulltitle != null ? fulltitle + ": " + subtitle : subtitle);
		mBookFieldsValues.put(BookFieldsTable.fulltitle, fulltitle);
	}

	private void updateGroups(DatabaseAdapter db, int t, final long id, boolean isUpdate) {
		if (mBooksValues.containsKey(BooksTable.creators)) {
			AuthorActions.updateAuthors(db, t, id, mCreators, isUpdate);
		}
		if (mBooksValues.containsKey(BooksTable.series)) {
			SeriesActions.updateSeries(db, t, id, mBooksValues.getAsString(BooksTable.series),
					mBooksValues.getAsInteger(BooksTable.volume), isUpdate);
		}
		if (mBooksValues.containsKey(BooksTable.publisher)) {
			PublisherActions.updatePublisher(db, t, id,
					mBooksValues.getAsString(BooksTable.publisher), isUpdate);
		}
		if (mBooksValues.containsKey(BooksTable.subjects)) {
			SubjectActions.updateSubjects(db, t, id, mSubjects, isUpdate);
		}
		if (mCollections != COLLECTIONS_NOT_SET) {
			CollectionActions.updateCollections(db, t, id, mCollections, isUpdate);
		}
	}
}