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
import android.widget.ProgressBar;

import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.R;

public final class IsbnSearchItemFullViewHolder extends BookListItemViewHolder {
	public static View createOrReuse(Context context, View convertView) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context)
				.inflate(R.layout.isbn_search_item_full, null);
		final IsbnSearchItemFullViewHolder holder = new IsbnSearchItemFullViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static IsbnSearchItemFullViewHolder from(View view) {
		return (IsbnSearchItemFullViewHolder) view.getTag();
	}

	public final ProgressBar progressBar;

	public IsbnSearchItemFullViewHolder(View view) {
		super(view);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
	}

	public void update(Context context, boolean inProgress, Long bookId,
			ImageDownloadCollection thumbnails, String thumbnailUrl, boolean thumbnailsPaused,
			String titleText, String creatorsText, Integer pageCount, Date releaseDate,
			int bookStatus, boolean showCheckBox, int checked) {
		super.update(context, bookId, thumbnails, thumbnailUrl, thumbnailsPaused, titleText,
				creatorsText, pageCount, releaseDate, bookStatus, showCheckBox, checked);

		progressBar.setVisibility(inProgress ? View.GONE : View.VISIBLE);
	}
}