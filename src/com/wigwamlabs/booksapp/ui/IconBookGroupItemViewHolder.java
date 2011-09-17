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
import android.widget.ImageView;
import android.widget.TextView;

import com.wigwamlabs.booksapp.CountView;
import com.wigwamlabs.booksapp.R;

public class IconBookGroupItemViewHolder {
	public static View createOrReuse(Context context, View convertView) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context).inflate(R.layout.icon_book_group_item, null);
		final IconBookGroupItemViewHolder holder = new IconBookGroupItemViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static IconBookGroupItemViewHolder from(View view) {
		return (IconBookGroupItemViewHolder) view.getTag();
	}

	private final CountView mBookCount;
	private final ImageView mIcon;
	private final TextView mName;

	public IconBookGroupItemViewHolder(View view) {
		mIcon = (ImageView) view.findViewById(android.R.id.icon);
		mName = (TextView) view.findViewById(R.id.name);
		mBookCount = (CountView) view.findViewById(R.id.book_count);
	}

	public void update(int icon, String name, int bookCount) {
		mIcon.setImageResource(icon);
		mName.setText(name);
		mBookCount.setCount(bookCount);
	}
}
