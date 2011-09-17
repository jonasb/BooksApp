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

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class FilterEditText extends EditText {
	public interface Callback {
		public void filterList(CharSequence filter);
	}

	private final Callback mCallback;

	public FilterEditText(Context context, Callback callback) {
		super(context);
		mCallback = callback;

		setHint(R.string.filter_books_hint);
		setBackgroundResource(R.drawable.edittext_compact);
		setImeOptions(EditorInfo.IME_ACTION_SEARCH);
	}

	@Override
	public void onTextChanged(CharSequence text, int start, int before, int after) {
		super.onTextChanged(text, start, before, after);
		if (mCallback != null)
			mCallback.filterList(text);
	}
}
