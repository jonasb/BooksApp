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

// http://stackoverflow.com/questions/3211974/transforming-some-special-caracters-e-e-into-e
// http://www.ahinea.com/en/tech/accented-translate.html
public final class UnicodeToAsciiConverter {
	private static final String PLAIN_ASCII = "AaEeIiOoUu" // grave
			+ "AaEeIiOoUuYy" // acute
			+ "AaEeIiOoUuYy" // circumflex
			+ "AaOoNn" // tilde
			+ "AaEeIiOoUuYy" // umlaut
			+ "Aa" // ring
			+ "Cc" // cedilla
			+ "OoUu" // double acute
			+ "DDddHh"//
			+ "ikLLll"//
			+ "NnnOos"//
			+ "TTtt" //
			+ "sAaIiOo" // should be ss,Ae,ae,IJ,ij,Oe,oe
	;

	private static final String UNICODE = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9" // grave
			+ "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" // acute
			+ "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" // circumflex
			+ "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1" // tilde
			+ "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" // umlaut
			+ "\u00C5\u00E5" // ring
			+ "\u00C7\u00E7" // cedilla
			+ "\u0150\u0151\u0170\u0171" // double acute
			+ "\u00D0\u0110\u00F0\u0111\u0126\u0127" //
			+ "\u0131\u0138\u013F\u0141\u0140\u0142" //
			+ "\u014A\u0149\u014B\u00D8\u00F8\u017F" //
			+ "\u00DE\u0166\u00FE\u0167" //
			// these are not entirely correct, should expand to two characters
			+ "\u00DF\u00C6\u00E6\u0132\u0133\u0152\u0153" //
	;

	public static String convertKnownUnicodeCharacters(CharSequence src) {
		if (src == null)
			return null;
		final int count = src.length();
		final StringBuilder sb = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			char c = src.charAt(i);
			final int pos = (c <= 126) ? -1 : UNICODE.indexOf(c);
			if (pos > -1) {
				c = PLAIN_ASCII.charAt(pos);
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static char firstAZHash(CharSequence src) {
		final int count = src.length();
		for (int i = 0; i < count; i++) {
			char c = src.charAt(i);
			final int pos = (c <= 126) ? -1 : UNICODE.indexOf(c);
			if (pos > -1) {
				c = PLAIN_ASCII.charAt(pos);
			}
			c = Character.toLowerCase(c);
			if (c >= '0' && c <= '9')
				return '#';
			if (c >= 'a' && c <= 'z')
				return c;
		}
		return '#';
	}

	private UnicodeToAsciiConverter() {
	}
}
