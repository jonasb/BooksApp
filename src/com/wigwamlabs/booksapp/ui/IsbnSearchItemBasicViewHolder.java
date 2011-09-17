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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.LayoutUtilities;
import com.wigwamlabs.booksapp.R;

public final class IsbnSearchItemBasicViewHolder {
	public static View createOrReuse(Context context, View convertView) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context).inflate(R.layout.isbn_search_item_basic,
				null);
		final IsbnSearchItemBasicViewHolder holder = new IsbnSearchItemBasicViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static IsbnSearchItemBasicViewHolder from(View view) {
		return (IsbnSearchItemBasicViewHolder) view.getTag();
	}

	public final ProgressBar progressBar;
	private final View status;
	public final ImageView thumbnail;
	public final TextView title;

	public IsbnSearchItemBasicViewHolder(View view) {
		status = view.findViewById(R.id.book_status);
		progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
		thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
		title = (TextView) view.findViewById(R.id.title);
	}

	public void update(Context context, boolean inProgress, Long bookId,
			ImageDownloadCollection thumbnails, String thumbnailUrl, String titleText,
			int bookStatus) {
		LayoutUtilities.updateStatus(context.getResources(), status, bookStatus);
		progressBar.setVisibility(inProgress ? View.GONE : View.VISIBLE);
		LayoutUtilities.updateThumbnail(context, thumbnail, bookId, thumbnails, thumbnailUrl, true,
				false);
		title.setText(titleText);
	}
}