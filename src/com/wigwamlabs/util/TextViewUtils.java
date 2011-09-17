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

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public final class TextViewUtils {
	public static void disableDialogButtonWhenNoText(TextView textView, final AlertDialog dialog,
			final int whichButton) {
		final TextWatcher watcher = new TextWatcher() {
			private Button mButton = null;

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// get button lazily since it's not created until dialog is
				// shown (Android issue #6360)
				if (mButton == null) {
					mButton = dialog.getButton(whichButton);
					if (mButton == null)
						return;
				}
				final boolean empty = s.toString().trim().length() == 0;
				mButton.setEnabled(!empty);
			}
		};
		textView.addTextChangedListener(watcher);
	}

	public static void disableViewWhenNoText(TextView textView, final View view) {
		final TextWatcher watcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				final boolean empty = s.toString().trim().length() == 0;
				view.setEnabled(!empty);
			}
		};
		textView.addTextChangedListener(watcher);
		final CharSequence existingText = textView.getText();
		watcher.onTextChanged(existingText, 0, existingText.length(), existingText.length());
	}

	private TextViewUtils() {
	}
}
