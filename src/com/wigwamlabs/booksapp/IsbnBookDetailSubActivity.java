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

import java.lang.ref.WeakReference;

import android.content.Context;
import android.view.View;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.BookAddRemoveObserver;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.GoogleBookDetailsMainViewHolder;
import com.wigwamlabs.googlebooks.GoogleBook;

public class IsbnBookDetailSubActivity extends SubActivity implements BookAddRemoveObserver {
	private GoogleBook mBook;
	@SuppressWarnings("unused")
	private PreviousNextClient mPreviousNextClient;
	private final GoogleBookDetailsMainViewHolder mViewHolder;

	public IsbnBookDetailSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db) {
		super(context, manager);

		setContentView(R.layout.google_book_details_main);
		mViewHolder = new GoogleBookDetailsMainViewHolder(getRoot(), getTitleBar());

		mViewHolder.progressBar.setVisibility(View.VISIBLE);
		mViewHolder.saveButton.setVisibility(View.GONE);

		db.addBookAddRemoveObserver(new WeakReference<BookAddRemoveObserver>(this));
	}

	@Override
	public void onBookAdded(long bookId, String googleId) {
		if (mBook.googleId != null && mBook.googleId.equals(googleId)) {
			getManager().createReplacement(BookDetailsSubActivity.class, this).prepare(bookId)
					.show(ShowDirection.SWITCH_CONTENT);
		}
	}

	@Override
	public void onBookRemoved(long bookId) {
	}

	public IsbnBookDetailSubActivity prepare(GoogleBook book, ImageDownloadCollection thumbnails) {
		mBook = book;
		mPreviousNextClient = PreviousNextClient.install(this, mViewHolder.previousButton,
				mViewHolder.nextButton);

		mViewHolder.update(getContext(), thumbnails, book);

		return this;
	}
}
