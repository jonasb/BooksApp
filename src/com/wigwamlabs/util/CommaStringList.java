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

import java.util.ArrayList;
import java.util.List;

public final class CommaStringList {
	public static String escapeItem(String item) {
		return item.replace(",", ",,");
	}

	public static String escapeStringForProcessing(String csl) {
		return csl.replace(",,", "\u001B\u001B");
	}

	public static String listToString(List<String> list) {
		if (list == null)
			return null;
		final StringBuilder sb = new StringBuilder();
		for (final String item : list) {
			if (item == null)
				continue;
			final String escaped = escapeItem(item.trim());
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(escaped);
		}
		if (sb.length() == 0)
			return null;
		return sb.toString();
	}

	public static String prepareStringForDisplay(String csl) {
		if (csl == null)
			return null;
		return csl.replace(",,", ",");
	}

	public static List<String> stringToList(String csl) {
		if (csl == null)
			return null;

		// TODO count no of , for capacity and reduce no of new strings
		csl = csl.replace(",,", "\u001B");
		final List<String> list = new ArrayList<String>();
		while (csl != null) {
			final int index = csl.indexOf(',');
			String item;
			if (index >= 0) {
				item = csl.substring(0, index);
				csl = csl.substring(index + 1);
			} else {
				item = csl;
				csl = null;
			}

			item = item.trim().replace('\u001B', ',');
			if (item.length() > 0)
				list.add(item);
		}
		if (list.size() == 0)
			return null;
		return list;
	}

	public static String unscapeStringAfterProcessing(String csl) {
		return csl.replace("\u001B\u001B", ",,");
	}

	private CommaStringList() {
	}
}
