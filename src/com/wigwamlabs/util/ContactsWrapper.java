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

package com.wigwamlabs.util;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.PeopleColumns;
import android.provider.ContactsContract;

@SuppressWarnings("deprecation")
public class ContactsWrapper {
	/* package */static class LazyLoadEclairPlus {
		/* package */static final Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
		/* package */static final String NAME_COLUMN = ContactsContract.Contacts.DISPLAY_NAME;
	}

	/* package */static class LazyLoadPreEclair {
		/* package */static final Uri CONTENT_URI = Contacts.People.CONTENT_URI;
		/* package */static final String NAME_COLUMN = PeopleColumns.NAME;
	}

	public static final Uri CONTENT_URI;

	static {
		if (Compatibility.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR) {
			CONTENT_URI = LazyLoadPreEclair.CONTENT_URI;
		} else {
			CONTENT_URI = LazyLoadEclairPlus.CONTENT_URI;
		}
	}

	public static String getName(ContentResolver contentResolver, Intent data) {
		final String nameColumn;
		if (Compatibility.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR) {
			nameColumn = LazyLoadPreEclair.NAME_COLUMN;
		} else {
			nameColumn = LazyLoadEclairPlus.NAME_COLUMN;
		}

		final Uri contactData = data.getData();
		final Cursor c = contentResolver.query(contactData, null, null, null, null);
		String name = null;
		if (c.moveToFirst()) {
			name = c.getString(c.getColumnIndexOrThrow(nameColumn));
		}
		c.close();
		return name;
	}
}
