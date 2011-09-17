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

import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.util.DateUtils;

public class LoanHistoryItemViewHolder {
	public static View create(Context context) {
		final View view = LayoutInflater.from(context).inflate(R.layout.loan_history_item, null);
		final LoanHistoryItemViewHolder holder = new LoanHistoryItemViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static LoanHistoryItemViewHolder from(View view) {
		return (LoanHistoryItemViewHolder) view.getTag();
	}

	public final TextView inDateView;
	public final TextView nameView;
	public final TextView outDateView;

	public LoanHistoryItemViewHolder(View view) {
		inDateView = (TextView) view.findViewById(R.id.in_date);
		nameView = (TextView) view.findViewById(R.id.name);
		outDateView = (TextView) view.findViewById(R.id.out_date);
	}

	public void update(CharSequence name, Date now, Date inDate, Date outDate) {
		nameView.setText(name);
		inDateView.setText(DateUtils.formatShort(now, inDate));
		outDateView.setText(DateUtils.formatShort(now, outDate));
	}
}