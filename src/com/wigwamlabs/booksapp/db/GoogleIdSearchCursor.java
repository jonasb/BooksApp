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

import static com.wigwamlabs.util.CollectionUtils.listToArray;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.util.StringUtils;

public final class GoogleIdSearchCursor {
	private static final int _id_index = 0;
	private static final String[] columns = new String[] { BooksTable._id, BooksTable.google_id };
	private static final int google_id_index = 1;

	private static Cursor searchByGoogleId(DatabaseAdapter db, String[] googleIds) {
		final String selection = StringUtils.copyJoin(BooksTable.google_id + "=?", " OR ",
				googleIds.length);
		return db.query(BooksTable.n, columns, selection, googleIds, null, null, null, null);
	}

	public static void updateDatabaseIds(DatabaseAdapter db, List<GoogleBook> books) {
		final ArrayList<String> selectionArgsList = new ArrayList<String>(books.size());
		for (final GoogleBook book : books) {
			if (book.googleId != null) {
				selectionArgsList.add(book.googleId);
			}
		}

		final Cursor c = searchByGoogleId(db, listToArray(selectionArgsList));
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			final String googleId = c.getString(google_id_index);

			for (final GoogleBook book : books) {
				if (googleId.equals(book.googleId)) {
					book.databaseId = Long.valueOf(c.getLong(_id_index));
					break;
				}
			}
		}

		c.close();
	}
}