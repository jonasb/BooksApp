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

package com.wigwamlabs.booksapp;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FilterQueryProvider;

import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.BookGroupItemViewHolder;
import com.wigwamlabs.util.CommaStringList;

public class BookGroupAdapter extends SortedCursorAdapter implements FilterQueryProvider {
	private final int mCursorIndex;
	private final CursorManager mCursorManager;
	private final DatabaseAdapter mDb;
	private final boolean mInDialog;
	private final int mType;

	public BookGroupAdapter(Context context, DatabaseAdapter db, CursorManager cursorManager,
			int cursorIndex, boolean inDialog, int type) {
		super(context, null, BookGroupCursor.name_normalized_index);
		mDb = db;
		mCursorManager = cursorManager;
		mCursorIndex = cursorIndex;
		mInDialog = inDialog;
		mType = type;
		setFilterQueryProvider(this);
		changeCursor(runQuery(null));
	}

	public BookGroupAdapter(Context context, DatabaseAdapter db, int type, long bookId,
			boolean inDialog) {
		super(context, null, BookGroupCursor.name_normalized_index);
		mDb = null;
		mCursorManager = null;
		mCursorIndex = -1;
		mInDialog = inDialog;
		mType = type;

		if (type == BookGroup.AUTHORS)
			changeCursor(BookGroupCursor.fetchBookAuthors(db, bookId,
					BookGroupCursor.name_normalized_index));
		else if (type == BookGroup.PUBLISHERS)
			changeCursor(BookGroupCursor.fetchBookPublishers(db, bookId,
					BookGroupCursor.name_normalized_index));
		else if (type == BookGroup.SERIES)
			changeCursor(BookGroupCursor.fetchBookSeries(db, bookId,
					BookGroupCursor.name_normalized_index));
		else if (type == BookGroup.SUBJECTS)
			changeCursor(BookGroupCursor.fetchBookSubjects(db, bookId,
					BookGroupCursor.name_normalized_index));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final BookGroupCursor group = (BookGroupCursor) cursor;
		BookGroupItemViewHolder.from(view).update(group.name(), group.bookCount());
	}

	@Override
	public CharSequence convertToString(Cursor cursor) {
		final BookGroupCursor group = (BookGroupCursor) cursor;
		return CommaStringList.escapeItem(group.name());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return BookGroupItemViewHolder.createOrReuse(context, null, mInDialog);
	}

	@Override
	public Cursor runQuery(CharSequence constraint) {
		if (constraint != null) {
			// turn ,, into , for the sake of MultiAutoCompleteTextViews
			constraint = CommaStringList.prepareStringForDisplay(constraint.toString());
		}
		BookGroupCursor cursor = null;
		if (mType == BookGroup.AUTHORS)
			cursor = BookGroupCursor.fetchAllAuthors(mDb, BookGroupCursor.name_normalized_index,
					constraint);
		else if (mType == BookGroup.COLLECTIONS)
			cursor = BookGroupCursor.fetchAllCollections(mDb,
					BookGroupCursor.name_normalized_index, constraint);
		else if (mType == BookGroup.CONTACTS)
			cursor = BookGroupCursor.fetchAllContacts(mDb, BookGroupCursor.name_normalized_index,
					constraint);
		else if (mType == BookGroup.PUBLISHERS)
			cursor = BookGroupCursor.fetchAllPublishers(mDb, BookGroupCursor.name_normalized_index,
					constraint);
		else if (mType == BookGroup.SERIES)
			cursor = BookGroupCursor.fetchAllSeries(mDb, BookGroupCursor.name_normalized_index,
					constraint);
		else if (mType == BookGroup.SUBJECTS)
			cursor = BookGroupCursor.fetchAllSubjects(mDb, BookGroupCursor.name_normalized_index,
					constraint);
		// don't close old cursor since the adapter will do that
		mCursorManager.setCursor(cursor, mCursorIndex, false);
		return cursor;
	}
}
