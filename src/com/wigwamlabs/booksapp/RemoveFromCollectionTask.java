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

import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class RemoveFromCollectionTask extends AsyncBookTask<Long> {
	private final long mCollectionId;

	public RemoveFromCollectionTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener, long collectionId) {
		super(context, db, listener);
		mCollectionId = collectionId;
	}

	@Override
	protected long doInBackground(Context context, DatabaseAdapter db, Long param) {
		final long bookId = param.longValue();
		CollectionActions.removeCollection(db, bookId, mCollectionId);
		return bookId;
	}

	@Override
	protected CharSequence getToastMessage(Resources res, int bookCount) {
		return res.getQuantityString(R.plurals.removed_from_collection_toast, bookCount);
	}
}
