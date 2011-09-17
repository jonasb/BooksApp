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

import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;

import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookSearch;

public class SaveGoogleBookTask extends AsyncBookTask<GoogleBook> {
	public static long execute(Context context, DatabaseAdapter db, GoogleBookSearch bookSearch,
			ImageDownloadCollection smallThumbnails, ImageDownloadCollection largeThumbnails,
			final GoogleBook book, Long collectionId) {
		if (!book.isFullBook && bookSearch != null) {
			try {
				final GoogleBook fullBook = bookSearch.searchByGoogleId(book.googleId).execute();
				book.mergeWithFullBook(fullBook);
			} catch (final IOException e) {
				// TODO should the user be notified? given the option to try
				// again?
				e.printStackTrace();
			}
		}

		long id;
		try {
			final int t = db.beginTransaction();
			id = book.save(db, t);
			if (collectionId != null) {
				CollectionActions.addCollection(db, t, id, collectionId.longValue());
			}
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}

		if (id >= 0) {
			final Bitmap smallThumbnail = smallThumbnails.getImage(book.thumbnailSmallUrl);
			final Bitmap largeThumbnail = (largeThumbnails == null ? null : largeThumbnails
					.getImage(book.thumbnailLargeUrl));
			ThumbnailManager.save(context, id, smallThumbnail, book.thumbnailSmallUrl,
					largeThumbnail, book.thumbnailLargeUrl);
		}
		// TODO handle error
		return id;
	}

	private final GoogleBookSearch mBookSearch;
	private final ImageDownloadCollection mLargeThumbnails;
	private final ImageDownloadCollection mSmallThumbnails;

	public SaveGoogleBookTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<GoogleBook, Long, Integer> listener, GoogleBookSearch bookSearch,
			ImageDownloadCollection smallThumbnails, ImageDownloadCollection largeThumbnails) {
		super(context, db, listener);
		mBookSearch = bookSearch;
		mSmallThumbnails = smallThumbnails;
		mLargeThumbnails = largeThumbnails;
	}

	@Override
	protected long doInBackground(Context context, DatabaseAdapter db, GoogleBook googleBook) {
		return execute(context, db, mBookSearch, mSmallThumbnails, mLargeThumbnails, googleBook,
				null);
	}

	@Override
	protected CharSequence getToastMessage(Resources res, int bookCount) {
		return res.getQuantityString(R.plurals.saved_books_toast, bookCount);
	}
}