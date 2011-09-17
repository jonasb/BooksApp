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

import java.util.Date;

import com.wigwamlabs.booksapp.LoanStatusProvider;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.util.DateUtils;

public class LoanStatusProviderTest extends DatabaseTestCase {
	public void testCacheIsInvalidated() {
		final Long id = addBook("foo");
		final LoanStatusProvider provider = new LoanStatusProvider(mDb);

		assertNull(provider.getDateForBookId(id));

		final BookEntry u = new BookEntry();
		u.setLoanReturnBy(DateUtils.parseDate("yyyy", "1925"));
		u.executeUpdateInTransaction(mDb, id.longValue());

		final Date d = provider.getDateForBookId(id);
		assertEquals("1925", DateUtils.formatSparse(d));
	}
}
