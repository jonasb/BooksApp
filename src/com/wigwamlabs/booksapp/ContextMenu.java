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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

public class ContextMenu {
	private final ArrayAdapter<String> mAdapter;
	private final Context mContext;
	private final OnClickListener mListener;
	private final Resources mResources;

	public ContextMenu(Context context, OnClickListener listener) {
		mContext = context;
		mListener = listener;
		mResources = context.getResources();
		mAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_item);
	}

	public void add(int resourceId) {
		mAdapter.add(mResources.getString(resourceId));
	}

	public void add(String text) {
		mAdapter.add(text);
	}

	public void show() {
		final AlertDialog dialog = new AlertDialog.Builder(mContext)
				.setAdapter(mAdapter, mListener).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
}