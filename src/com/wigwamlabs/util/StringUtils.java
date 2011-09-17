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

public final class StringUtils {
	public static String copyJoin(String item, String join, int count) {
		final int capacity = item.length() * count + join.length() * (count - 1);
		final StringBuilder builder = new StringBuilder(capacity);

		for (int i = 0; i < count; i++) {
			if (i > 0)
				builder.append(join);
			builder.append(item);
		}

		return builder.toString();
	}

	public static String normalizeExtreme(String string) {
		if (string == null)
			string = "";
		string = string.toLowerCase().trim();
		final StringBuilder sb = new StringBuilder(string.length());
		final int count = string.length();
		for (int i = 0; i < count; i++) {
			final char c = string.charAt(i);
			if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
				sb.append(c);
			}
		}
		string = sb.toString();
		final char first = UnicodeToAsciiConverter.firstAZHash(string);
		return first + string;
	}

	public static String normalizePersonNameExtreme(String name) {
		if (name != null) {
			final String[] parts = name.split("\\s");
			if (parts.length > 1) {
				final StringBuilder sb = new StringBuilder(name.length());
				// lastname first
				sb.append(parts[parts.length - 1]);
				// then the rest
				for (int i = 0; i < parts.length - 1; i++) {
					final String part = parts[i];
					if (part.length() > 0)
						sb.append(" ").append(part);
				}
				name = sb.toString();
			}
		}
		return normalizeExtreme(name);
	}

	public static CharSequence nullableArrayToString(CharSequence... strings) {
		final StringBuilder builder = new StringBuilder();
		for (final CharSequence s : strings) {
			if (s == null)
				continue;
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(s);
		}
		if (builder.length() == 0)
			return null;
		return builder.toString();
	}

	public static String trimmedStringOrNull(String s) {
		if (s == null)
			return null;
		s = s.trim();
		if (s.length() == 0)
			return null;
		return s;
	}
}