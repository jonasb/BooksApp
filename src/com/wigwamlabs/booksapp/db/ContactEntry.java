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

import android.database.Cursor;

import com.wigwamlabs.util.StringUtils;

public class ContactEntry extends DatabaseEntry {
	// TODO unit test
	public static long findOrCreate(DatabaseAdapter db, int t, String systemContactId, String name) {
		final Cursor c = db.query(ContactsTable.n, new String[] { ContactsTable._id },
				ContactsTable.system_contact_id + "= ?", new String[] { systemContactId }, null,
				null, null, "1");

		if (c.moveToFirst()) {
			final long id = c.getLong(0);
			c.close();
			return id;
		}
		c.close();

		final ContactEntry ce = new ContactEntry();
		ce.setSystemContactId(systemContactId);
		ce.setName(name);
		ce.setBookCount(0);
		return ce.executeInsert(db, t);
	}

	protected ContactEntry() {
		super(ContactsTable.n);
	}

	private void setBookCount(int value) {
		mValues.put(ContactsTable.book_count, Integer.valueOf(value));
	}

	private void setName(String value) {
		mValues.put(ContactsTable.name, value);
		mValues.put(ContactsTable.name_normalized, StringUtils.normalizePersonNameExtreme(value));
	}

	private void setSystemContactId(String value) {
		mValues.put(ContactsTable.system_contact_id, value);
	}
}
