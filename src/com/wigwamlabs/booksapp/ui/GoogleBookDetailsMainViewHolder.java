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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.booksapp.TitleBar;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.util.CommaStringList;

public class GoogleBookDetailsMainViewHolder extends BookDetailsViewHolder {
	public final ImageView nextButton;
	public final ImageView previousButton;
	public final ProgressBar progressBar;
	public final ImageView saveButton;

	public GoogleBookDetailsMainViewHolder(View root, TitleBar titleBar) {
		super(root, titleBar);
		final Context context = root.getContext();

		progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);

		saveButton = new ImageView(context);
		saveButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		saveButton.setImageResource(R.drawable.titlebar_import);
		titleBar.addLeftContent(saveButton);

		nextButton = new ImageView(context);
		nextButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		nextButton.setImageResource(R.drawable.titlebar_down);
		titleBar.addRightContent(nextButton);

		previousButton = new ImageView(context);
		previousButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		previousButton.setImageResource(R.drawable.titlebar_up);
		titleBar.addRightContent(previousButton);
	}

	public void update(Context context, ImageDownloadCollection thumbnails, GoogleBook book) {
		update(context, book.databaseId, thumbnails, book.thumbnailSmallUrl, book.title,
				book.subtitle, book.creatorsText, false, book.averageRating, null, null,
				book.description, book.publisher, book.releaseDate, book.isbn10, book.isbn13,
				CommaStringList.listToString(book.subjects), book.pageCount, book.dimensions);
	}
}
