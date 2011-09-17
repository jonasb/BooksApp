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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.wigwamlabs.booksapp.CountView;
import com.wigwamlabs.booksapp.R;

public class BookGroupItemViewHolder {
	public static View createOrReuse(Context context, View convertView, boolean inDialog) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context).inflate(R.layout.book_group_item, null);
		final BookGroupItemViewHolder holder = new BookGroupItemViewHolder(context, view, inDialog);
		view.setTag(holder);
		return view;
	}

	public static BookGroupItemViewHolder from(View view) {
		return (BookGroupItemViewHolder) view.getTag();
	}

	private final CountView mBookCount;
	private final TextView mName;

	public BookGroupItemViewHolder(Context context, View view, boolean inDialog) {
		mName = (TextView) view.findViewById(R.id.name);
		mBookCount = (CountView) view.findViewById(R.id.book_count);

		if (inDialog)
			mName.setTextAppearance(context, android.R.style.TextAppearance_Large_Inverse);
	}

	public void update(String name, int bookCount) {
		mName.setText(name);
		mBookCount.setCount(bookCount);
	}
}
