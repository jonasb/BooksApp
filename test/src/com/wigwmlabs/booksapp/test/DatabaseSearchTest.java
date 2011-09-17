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

import com.wigwamlabs.booksapp.db.BookListCursor;

public class DatabaseSearchTest extends DatabaseTestCase {
	public void testQueryErrorsThrowException() {
		BookListCursor c = null;
		try {
			c = BookListCursor.searchAny(mDb, "\"\"\"");
		} catch (final Exception e) {
		}
		assertNull(c);
	}

	public void testSearchAnyField() throws Exception {
		addBook(null, null, null, null);
		final Long titleId = addBook("aa keyword aa", null, null, null);
		final Long subtitleId = addBook(null, "aa keyword aa", null, null);
		final Long creatorsId = addBook(null, null, "aa keyword aa", null);
		final Long descriptionId = addBook(null, null, null, "aa keyword aa");

		final BookListCursor c = BookListCursor.searchAny(mDb, "keyword");
		assertListEquals(c, titleId, subtitleId, creatorsId, descriptionId);
		c.close();
	}

	public void testSearchOrderingAllFields() throws Exception {
		final Long descriptionId = addBook(null, null, null, "aa keyword aa");
		final Long creatorsId = addBook(null, null, "aa keyword aa", null);
		final Long titleId = addBook("aa keyword aa", null, null, null);

		final BookListCursor c = BookListCursor.searchAny(mDb, "keyword");
		assertListEquals(c, titleId, creatorsId, descriptionId);
		c.close();
	}

	public void testSearchOrderingMatchesInMultipleColumns() throws Exception {
		final Long subtitle1Id = addBook(null, "aa keyword aa", null, null);
		final Long subtitleCreatorsId = addBook(null, "aa keyword aa", "aa keyword aa", null);
		final Long subtitle2Id = addBook(null, "aa keyword aa", null, null);

		final BookListCursor c = BookListCursor.searchAny(mDb, "keyword");
		assertListEquals(c, subtitleCreatorsId, subtitle1Id, subtitle2Id);
		c.close();
	}

	public void testSearchOrderingMultipleMatchesInOneColumn() throws Exception {
		final Long oneMatch1Id = addBook("aa keyword aa", null, null, null);
		final Long twoMatchesId = addBook("aa keyword bb keyword aa", null, null, null);
		final Long oneMatch2Id = addBook("bb keyword aa", null, null, null);

		final BookListCursor c = BookListCursor.searchAny(mDb, "keyword");
		assertListEquals(c, twoMatchesId, oneMatch1Id, oneMatch2Id);
		c.close();
	}
}
