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

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.wigwamlabs.booksapp.db.LoanHistoryCursor;
import com.wigwamlabs.booksapp.ui.LoanHistoryItemViewHolder;

public class LoanHistoryAdapter extends CursorAdapter {
	private final Date mNow;

	public LoanHistoryAdapter(Context context, LoanHistoryCursor cursor, Date now) {
		super(context, cursor);
		mNow = now;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final LoanHistoryCursor lh = (LoanHistoryCursor) cursor;
		LoanHistoryItemViewHolder.from(view).update(lh.name(), mNow, lh.inDate(), lh.outDate());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return LoanHistoryItemViewHolder.create(context);
	}
}
