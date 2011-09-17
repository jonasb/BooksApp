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

import com.wigwamlabs.booksapp.R;

public class ListSectionViewHolder {
	public static View createOrReuse(Context context, View convertView) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context).inflate(R.layout.list_section, null);
		final ListSectionViewHolder holder = new ListSectionViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static ListSectionViewHolder from(View view) {
		return (ListSectionViewHolder) view.getTag();
	}

	private final TextView mTitle;

	/* package */ListSectionViewHolder(View view) {
		mTitle = (TextView) view.findViewById(R.id.title);
	}

	public void update(int title) {
		mTitle.setText(title);
	}
}