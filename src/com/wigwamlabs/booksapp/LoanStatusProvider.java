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

import java.util.Date;
import java.util.HashMap;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.booksapp.db.ExtendedCursor;
import com.wigwamlabs.booksapp.db.LoanStatusCursor;
import com.wigwamlabs.util.StubExtendedCursor;

public class LoanStatusProvider {
	private final DatabaseAdapter mDb;
	private final ExtendedCursor mFakeCursor;
	private boolean mHasRegisteredCursor = false;
	private final HashMap<Long, Date> mLoanReturnBy = new HashMap<Long, Date>();

	public LoanStatusProvider(DatabaseAdapter db) {
		mDb = db;

		mFakeCursor = new StubExtendedCursor() {
			@Override
			public boolean requery() {
				onInvalidated();
				return true;
			}
		};
	}

	public Date getDateForBookId(Long bookId) {
		if (bookId == null)
			return null;

		// check cache
		if (mLoanReturnBy.containsKey(bookId)) {
			return mLoanReturnBy.get(bookId);
		}

		// check database
		final LoanStatusCursor c = LoanStatusCursor.fetchForBook(mDb, bookId.longValue());
		final Date d;
		if (!c.moveToFirst()) {
			d = null;
		} else {
			d = c.loanReturnBy();
		}
		c.close();
		mLoanReturnBy.put(bookId, d);

		// register with database to be notified of invalidation
		if (!mHasRegisteredCursor) {
			mDb.addCursor(CursorType.BOOK_LIST, mFakeCursor);
			mHasRegisteredCursor = true;
		}

		return d;
	}

	protected void onInvalidated() {
		mLoanReturnBy.clear();
	}
}
