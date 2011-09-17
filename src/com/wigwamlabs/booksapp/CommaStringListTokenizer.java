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

package com.wigwamlabs.booksapp;

import android.widget.MultiAutoCompleteTextView;

import com.wigwamlabs.util.CommaStringList;

public class CommaStringListTokenizer extends MultiAutoCompleteTextView.CommaTokenizer {
	@Override
	public int findTokenEnd(CharSequence text, int cursor) {
		final String escaped = CommaStringList.escapeStringForProcessing(text.toString());
		return super.findTokenEnd(escaped, cursor);
	}

	@Override
	public int findTokenStart(CharSequence text, int cursor) {
		final String escaped = CommaStringList.escapeStringForProcessing(text.toString());
		return super.findTokenStart(escaped, cursor);
	}

	@Override
	public CharSequence terminateToken(CharSequence text) {
		final String escaped = CommaStringList.escapeStringForProcessing(text.toString());
		final CharSequence terminated = super.terminateToken(escaped);
		return CommaStringList.unscapeStringAfterProcessing(terminated.toString());
	}
}
