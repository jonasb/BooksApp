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

public final class CollectionUtils {
	public static <T> List<T> arrayToList(T[] array) {
		final ArrayList<T> list = new ArrayList<T>(array.length);
		for (final T i : array)
			list.add(i);
		return list;
	}

	public static String[] listToArray(final List<String> list) {
		return list.toArray(new String[list.size()]);
	}

	public static void removeDuplicates(List<String> list) {
		for (int i = 0; i < list.size(); i++) {
			int j = i + 1;
			while (j < list.size()) {
				if (list.get(i).equals(list.get(j))) {
					list.remove(j);
				} else {
					j++;
				}
			}
		}
	}

	private CollectionUtils() {
	}
}
