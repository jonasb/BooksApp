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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.StringUtils;

public class StringUtilsTest extends TestCase {
	public static <T> List<T> toList(T... items) {
		final List<T> list = new ArrayList<T>(items.length);
		for (final T item : items) {
			list.add(item);
		}
		return list;
	}

	public void testNormalizeExtreme() {
		assertEquals("#", StringUtils.normalizeExtreme(null));
		assertEquals("#", StringUtils.normalizeExtreme("  \t "));

		// keeps [0-9a-zA-Z]
		final String ch09 = "0123456789";
		final String chaz = "abcdefghijklmnopqrstuvwxyz";
		final String chAZ = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		assertEquals("#" + ch09 + chaz + chaz, StringUtils.normalizeExtreme(ch09 + chaz + chAZ));

		// remove non [0-9a-z]
		assertEquals("aabcd", StringUtils.normalizeExtreme("//\"abc..'d.."));

		// lower case
		assertEquals("aabcd", StringUtils.normalizeExtreme("AbCd"));

		// fold accents
		final String folded = "aaaeiuoaaaeiuo";
		final String accents = "Â‰‡ÎÔ˚ˆ≈ƒ¿Àœ‹÷";
		for (int i = 0; i < accents.length(); i++) {
			assertEquals(folded.charAt(i),
					StringUtils.normalizeExtreme(Character.toString(accents.charAt(i))).charAt(0));
		}

		// deal with other unicode characters
		final String ascii = "ddddhhikllllnnnoosttttsaaiioo";
		final String unicode = "\u00D0\u0110\u00F0\u0111\u0126\u0127"
				+ "\u0131\u0138\u013F\u0141\u0140\u0142" + "\u014A\u0149\u014B\u00D8\u00F8\u017F"
				+ "\u00DE\u0166\u00FE\u0167" + "\u00DF\u00C6\u00E6\u0132\u0133\u0152\u0153";
		for (int i = 0; i < unicode.length(); i++) {
			assertEquals(ascii.charAt(i),
					StringUtils.normalizeExtreme(Character.toString(unicode.charAt(i))).charAt(0));
		}
	}

	public void testNormalizePersonNameExtreme() {
		assertEquals("#", StringUtils.normalizePersonNameExtreme(null));
		assertEquals("#", StringUtils.normalizePersonNameExtreme("  \t "));

		assertEquals("jjane", StringUtils.normalizePersonNameExtreme(" Jane "));

		assertEquals("ddoe jane", StringUtils.normalizePersonNameExtreme(" Jane Doe "));

		assertEquals("ttolkien jrr", StringUtils.normalizePersonNameExtreme("J.R.R Tolkien"));

		assertEquals("ssmith john a", StringUtils.normalizePersonNameExtreme("John A Smith"));

		assertEquals("ddoublespace name with",
				StringUtils.normalizePersonNameExtreme("Name  With  Doublespace"));
	}

	public void testStringToList() {
		assertEquals(null, CommaStringList.stringToList(null));
		assertEquals(null, CommaStringList.stringToList("  "));

		assertEquals(null, CommaStringList.listToString(null));
		assertEquals(null, CommaStringList.listToString(toList(" ")));

		assertEquals(toList("a"), CommaStringList.stringToList("a"));

		final List<String> items = toList("a", "b", "c, d");
		final String itemsStr = CommaStringList.listToString(items);
		assertEquals("a, b, c,, d", itemsStr);
		assertEquals(items, CommaStringList.stringToList(itemsStr));
	}
}
