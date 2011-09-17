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

import java.util.Date;

import android.database.Cursor;
import android.util.Log;

public final class DatabaseUtils {
	private static final String TAG = "SQL";

	public static Cursor checkFts(Cursor c) throws Exception {
		try {
			// force error now if there's any
			c.getCount();
			return c;
		} catch (final Exception e) {
			c.close();
			throw e;
		}
	}

	public static Long dateToLong(Date date) {
		if (date == null)
			return null;
		// sqlite stores dates as seconds since epoch
		return Long.valueOf(date.getTime() / 1000);
	}

	public static void debugLogCursor(Cursor cursor) {
		final int pos = cursor.getPosition();
		Log.d(TAG, "Number of items: " + cursor.getCount());
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String item = "";
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				if (i > 0)
					item += "; ";
				item += cursor.getColumnName(i) + ": " + cursor.getString(i);
			}
			Log.d(TAG, item);
		}
		cursor.moveToPosition(pos);
	}

	public static String filterLike(CharSequence filter) {
		String fieldMatch = trimFilterOrNull(filter);
		if (fieldMatch == null)
			return null;
		fieldMatch = fieldMatch + "%";
		return fieldMatch;
	}

	public static String fts3FilterMatch(CharSequence filter) {
		final String fieldMatches = trimFilterOrNull(filter);
		if (fieldMatches == null)
			return null;

		final StringBuilder sb = new StringBuilder();
		for (String fm : fieldMatches.split("\\s+")) {
			if (!(fm.endsWith("*") || fm.endsWith("\"") || fm.endsWith("'")))
				fm = fm + "*";

			if (sb.length() > 0)
				sb.append(" ");
			sb.append(fm);
		}
		return sb.toString();
	}

	public static Date getDateOrNull(Cursor cursor, int columnIndex) {
		if (cursor.isNull(columnIndex))
			return null;
		return longToDate(cursor.getLong(columnIndex));
	}

	public static Float getFloatOrNull(Cursor cursor, int columnIndex) {
		if (cursor.isNull(columnIndex))
			return null;
		return Float.valueOf(cursor.getFloat(columnIndex));
	}

	public static Integer getIntOrNull(Cursor cursor, int columnIndex) {
		if (cursor.isNull(columnIndex))
			return null;
		return Integer.valueOf(cursor.getInt(columnIndex));
	}

	public static Long getLongOrNull(Cursor cursor, int columnIndex) {
		if (cursor.isNull(columnIndex))
			return null;
		return Long.valueOf(cursor.getLong(columnIndex));
	}

	public static Date longToDate(long secondsSinceEpoch) {
		final Date d = new Date();
		// convert back to millisecs
		d.setTime(secondsSinceEpoch * 1000);
		return d;
	}

	private static String trimFilterOrNull(CharSequence filter) {
		if (filter == null)
			return null;
		final String f = filter.toString().trim();
		if (f.length() == 0)
			return null;
		return f;
	}
}
