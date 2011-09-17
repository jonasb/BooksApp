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

package com.wigwamlabs.booksapp.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ProgressBar;

public class SimpleProgressDialog {
	public static Dialog show(Context context, OnDismissListener listener) {
		final Dialog dialog = new Dialog(context);
		final ProgressBar progress = new ProgressBar(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setOnDismissListener(listener);
		final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		dialog.addContentView(progress, params);
		dialog.show();
		return dialog;
	}
}
