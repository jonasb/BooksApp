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

package com.wigwamlabs.booksapp.db;

import java.util.List;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;

public class CollectionActions {
	private static final ManyToManyActions ACTIONS = new ManyToManyActions(CollectionsTable.n,
			CollectionsTable._id, CollectionsTable.name, CollectionsTable.name_normalized, false,
			CollectionsTable.book_count, BookCollectionsTable.n, BookCollectionsTable.book_id,
			BookCollectionsTable.collection_id);

	public static void addCollection(DatabaseAdapter db, int t, long bookId, long collectionId) {
		ACTIONS.addItem(db, t, bookId, collectionId, CursorType.COLLECTION_LIST);
		db.requeryCursors(CursorType.BOOK_LIST);
	}

	public static void addCollection(DatabaseAdapter db, long bookId, long collectionId) {
		try {
			final int t = db.beginTransaction();
			addCollection(db, t, bookId, collectionId);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
		db.requeryCursors(CursorType.BOOK_LIST);
	}

	public static void addNewCollection(DatabaseAdapter db, int t, long bookId, String name) {
		ACTIONS.addItem(db, t, bookId, name, CursorType.COLLECTION_LIST);
	}

	public static void addNewCollectionInTransaction(DatabaseAdapter db, long bookId, String name) {
		try {
			final int t = db.beginTransaction();
			addNewCollection(db, t, bookId, name);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
	}

	public static void deleteCollection(DatabaseAdapter db, long collectionId) {
		try {
			final int t = db.beginTransaction();
			ACTIONS.deleteItem(db, t, collectionId, CursorType.COLLECTION_LIST);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
	}

	public static void removeAllCollections(DatabaseAdapter db, int t, long bookId) {
		ACTIONS.updateItems(db, t, bookId, null, true, false, CursorType.COLLECTION_LIST);
	}

	public static void removeCollection(DatabaseAdapter db, long bookId, long collectionId) {
		try {
			final int t = db.beginTransaction();
			ACTIONS.decrementOrRemoveItem(db, t, bookId, collectionId, false,
					CursorType.COLLECTION_LIST);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
		db.requeryCursors(CursorType.BOOK_LIST);
	}

	public static void renameCollection(DatabaseAdapter db, long collectionId, String newName) {
		try {
			final int t = db.beginTransaction();
			ACTIONS.renameItem(db, t, collectionId, newName, CursorType.COLLECTION_LIST);
			db.setTransactionSuccessful(t);
		} finally {
			db.endTransaction();
		}
	}

	public static void updateCollections(DatabaseAdapter db, int t, long bookId,
			List<String> collections, boolean checkExistingCollections) {
		ACTIONS.updateItems(db, t, bookId, collections, checkExistingCollections, true,
				CursorType.COLLECTION_LIST);
	}
}
