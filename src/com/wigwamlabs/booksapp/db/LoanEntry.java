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

import java.util.Date;

import com.wigwamlabs.util.DatabaseUtils;

public class LoanEntry extends DatabaseEntry {
	public LoanEntry() {
		super(LoansTable.n);
	}

	public void setBookId(long value) {
		mValues.put(LoansTable.book_id, Long.valueOf(value));
	}

	public void setContactId(long value) {
		mValues.put(LoansTable.contact_id, Long.valueOf(value));
	}

	public void setInDate(Date value) {
		mValues.put(LoansTable.in_date, DatabaseUtils.dateToLong(value));
	}

	public void setOutDate(Date value) {
		mValues.put(LoansTable.out_date, DatabaseUtils.dateToLong(value));
	}
}
