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

package com.wigwmlabs.booksapp.test;

import static com.wigwmlabs.booksapp.test.GoogleApiTestUtilities.toList;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.test.InstrumentationTestCase;
import android.text.TextUtils;

import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.ItemCountCursor;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.util.CommaStringList;

public abstract class DatabaseTestCase extends InstrumentationTestCase {
	protected DatabaseAdapter mDb;

	public DatabaseTestCase() {
		super();
	}

	protected Long addBook(String title) {
		return addBook(title, null, null, null);
	}

	protected Long addBook(String title, String subtitle, String creator, String description) {
		final GoogleBook b = new GoogleBook();
		b.title = title;
		b.subtitle = subtitle;
		b.creators = CommaStringList.stringToList(creator);
		b.descriptions = toList(description);
		b.scrub();
		return Long.valueOf(b.save(mDb));
	}

	protected void assertListEquals(BookListCursor c, Long... expectedIds) {
		final int count = c.getCount();
		final Long[] actualIds = new Long[count];
		for (int i = 0; i < count; i++) {
			c.moveToPosition(i);
			actualIds[i] = Long.valueOf(c._id());
		}
		assertEquals(TextUtils.join(", ", expectedIds), TextUtils.join(", ", actualIds));
	}

	protected void assertTableEquals(String expectedContent, String tableName) {
		assertTableEquals(expectedContent, tableName, "*");
	}

	protected void assertTableEquals(String expectedContent, String tableName, String columns) {
		final Cursor c = mDb.queryRaw("SELECT " + columns + " FROM " + tableName, null);
		final String s = dumpCursor(c);
		c.close();
		assertEquals(expectedContent, s);
	}

	protected int bookCount() {
		final ItemCountCursor bookCount = ItemCountCursor.fetchBookCount(mDb);
		bookCount.moveToFirst();
		final int count = bookCount.count();
		bookCount.close();
		return count;
	}

	protected String bookGroup(BookGroupCursor bookGroup) {
		final StringBuilder res = new StringBuilder();
		for (bookGroup.moveToFirst(); !bookGroup.isAfterLast(); bookGroup.moveToNext()) {
			if (res.length() > 0)
				res.append("\n");
			res.append(bookGroup.name()).append("|").append(bookGroup.bookCount());
		}
		return res.toString();
	}

	protected String dumpCursor(final Cursor c, String... columns) {
		String s = "";
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			if (s.length() > 0)
				s += '\n';
			for (int i = 0; i < c.getColumnCount(); i++) {
				if (columns.length > 0) {
					boolean found = false;
					final String columnName = c.getColumnName(i);
					for (final String column : columns) {
						if (columnName.equals(column))
							found = true;
					}
					if (!found)
						continue;
				}

				if (i > 0)
					s += '|';
				s += c.getString(i);
			}
		}
		return s;
	}

	protected List<Boolean> observeCursorChange(Cursor c) {
		final List<Boolean> hasChanged = new ArrayList<Boolean>();
		c.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				hasChanged.add(Boolean.TRUE);
			}
		});
		return hasChanged;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mDb = new DatabaseAdapter();
		mDb.openInMemory(getInstrumentation().getTargetContext(), true);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mDb.close();
	}

}