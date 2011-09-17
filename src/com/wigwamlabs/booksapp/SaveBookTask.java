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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class SaveBookTask extends AsyncBookTask<BookEntry> {
	public static final long NEW_BOOK_ID = -1;
	public static final Bitmap REMOVE_THUMBNAIL = Bitmap.createBitmap(1, 1, Config.ALPHA_8);
	private final Long mCollectionId;
	private final long mId;
	private final Bitmap mThumbnail;

	public SaveBookTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<BookEntry, Long, Integer> listener, long id, Bitmap thumbnail,
			Long collectionId) {
		super(context, db, listener);
		mId = id;
		mThumbnail = thumbnail;
		mCollectionId = collectionId;
	}

	@Override
	protected long doInBackground(Context context, DatabaseAdapter db, BookEntry b) {
		final long id;
		// reset cover url if user has changed the cover
		if (mThumbnail != null)
			b.setCoverUrl(null);

		// insert into/update database
		if (mId == NEW_BOOK_ID) {
			try {
				final int t = db.beginTransaction();
				id = b.executeInsert(db, t);
				if (mCollectionId != null) {
					CollectionActions.addCollection(db, t, id, mCollectionId.longValue());
				}
				db.setTransactionSuccessful(t);
			} finally {
				db.endTransaction();
			}
		} else {
			b.executeUpdateInTransaction(db, mId);
			id = mId;
		}

		// update thumbnails
		if (mThumbnail == REMOVE_THUMBNAIL) {
			ThumbnailManager.deleteThumbnails(context, id);
		} else if (mThumbnail != null) {
			ThumbnailManager.storeLargeThumbnail(context, id, mThumbnail, true);
		}

		return id;
	}

	@Override
	protected CharSequence getToastMessage(Resources res, int bookCount) {
		return res.getQuantityText(R.plurals.saved_books_toast, 1);
	}
}
