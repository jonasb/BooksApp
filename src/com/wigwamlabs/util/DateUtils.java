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

import java.text.SimpleDateFormat;
import java.util.Date;

import com.wigwamlabs.booksapp.Debug;

public final class DateUtils {
	private static SimpleDateFormat formatter;

	public static String format(String pattern, Date date) {
		if (date == null)
			return null;
		if (formatter == null)
			formatter = new SimpleDateFormat();
		formatter.applyPattern(pattern);
		return formatter.format(date);
	}

	public static CharSequence formatShort(Date now, Date date) {
		if (date == null)
			return null;
		if (date.getYear() != now.getYear())
			return format("yyyy-MM-dd", date);
		return format("MM-dd", date);
	}

	public static String formatSparse(Date date) {
		if (date == null)
			return null;
		// TODO localize
		if (date.getDate() != 1) {
			return format("yyyy-MM-dd", date);
		}
		if (date.getMonth() != 0) {
			return format("yyyy-MM", date);
		}
		return format("yyyy", date);
	}

	public static Date parseDate(String pattern, String string) {
		if (formatter == null)
			formatter = new SimpleDateFormat();
		formatter.applyPattern(pattern);
		try {
			return formatter.parse(string);
		} catch (final StringIndexOutOfBoundsException e) {
			Debug.reportException("Date: " + string, e);
			return null;
		} catch (final Exception e) {
			return null;
		}
	}

	public static Date parseDateRelaxed(String string) {
		// TODO localize
		// TODO this needs to be much more robust and fully tested
		if (string == null)
			return null;
		final String[] c = string.split("[^0-9]+");
		if (c.length == 0)
			return null;
		try {
			final int y = Integer.parseInt(c[0]);
			int m = 0;
			if (c.length >= 2)
				m = Integer.parseInt(c[1]) - 1;
			int d = 1;
			if (c.length >= 3)
				d = Integer.parseInt(c[2]);

			final Date date = new Date();
			date.setYear(y - 1900);
			date.setMonth(m);
			date.setDate(d);
			return date;
		} catch (final NumberFormatException e) {
			return null;
		}
	}
}
