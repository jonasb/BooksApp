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

package com.wigwmlabs.booksapp.test;

import com.wigwamlabs.booksapp.db.BookCollectionCursor;
import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.CollectionActions;

public class CollectionTest extends DatabaseTestCase {
	private static final int FAVORITES_ID = 1;

	private String bookCollections(BookCollectionCursor collection) {
		final StringBuilder res = new StringBuilder();
		for (collection.moveToFirst(); !collection.isAfterLast(); collection.moveToNext()) {
			if (res.length() > 0)
				res.append("\n");
			res.append(collection.name()).append("|").append(collection.bookCount()).append("|")
					.append(collection.bookId());
		}
		return res.toString();
	}

	public void testAddAndRemoveExistingCollectionToBook() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);
		collectionsAll.moveToPosition(2);
		final long collectionId = collectionsAll._id();

		CollectionActions.addCollection(mDb, id, collectionId);

		assertEquals("Favorites|0|null\nReading now|0|null\nTo read|1|1\nHave read|0|null",
				bookCollections(collectionsAll));
		assertEquals("To read|1|1", bookCollections(collections));

		CollectionActions.removeCollection(mDb, id, collectionId);

		assertEquals("Favorites|0|null\nReading now|0|null\nTo read|0|null\nHave read|0|null",
				bookCollections(collectionsAll));
		assertEquals("", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}

	public void testAddingBookToCollectionTwice() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);

		// adding twice should result in only one join item
		CollectionActions.addCollection(mDb, id, FAVORITES_ID);
		CollectionActions.addCollection(mDb, id, FAVORITES_ID);

		// doing the same with add new
		CollectionActions.addNewCollectionInTransaction(mDb, id, "New");
		CollectionActions.addNewCollectionInTransaction(mDb, id, "New");

		assertEquals(
				"Favorites|1|1\nReading now|0|null\nTo read|0|null\nHave read|0|null\nNew|1|1",
				bookCollections(collectionsAll));
		assertEquals("Favorites|1|1\nNew|1|1", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}

	public void testAddingNewCollections() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);

		// create new
		CollectionActions.addNewCollectionInTransaction(mDb, id, "Bar");

		// use same name as existing collection (won't create new)
		CollectionActions.addNewCollectionInTransaction(mDb, id, "To read");

		assertEquals(
				"Favorites|0|null\nReading now|0|null\nTo read|1|1\nHave read|0|null\nBar|1|1",
				bookCollections(collectionsAll));
		assertEquals("Bar|1|1\nTo read|1|1", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}

	public void testBookListIsUpdatedWhenChangingCollection() throws Exception {
		final long id = addBook("Foo").longValue();
		final BookListCursor books = BookListCursor.fetchByCollection(mDb,
				BookListCursor.title_normalized_index, 1, null);
		assertEquals(0, books.getCount());

		CollectionActions.addCollection(mDb, id, 1);
		assertEquals(1, books.getCount());

		CollectionActions.removeCollection(mDb, id, 1);
		assertEquals(0, books.getCount());

		books.close();
	}

	public void testBookWithoutCollections() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);

		assertEquals("Favorites|0|null\nReading now|0|null\nTo read|0|null\nHave read|0|null",
				bookCollections(collectionsAll));
		assertEquals("", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}

	public void testDeletingCollection() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);

		CollectionActions.addCollection(mDb, id, FAVORITES_ID);

		CollectionActions.deleteCollection(mDb, FAVORITES_ID);

		assertEquals("Reading now|0|null\nTo read|0|null\nHave read|0|null",
				bookCollections(collectionsAll));
		assertEquals("", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}

	public void testInitialCollections() {
		final BookGroupCursor collections = BookGroupCursor.fetchAllCollections(mDb,
				BookGroupCursor.name_normalized_index, null);

		assertEquals("Favorites|0\nHave read|0\nReading now|0\nTo read|0", bookGroup(collections));
		collections.close();
	}

	public void testInvalidFtsQuery() {
		BookListCursor c = null;
		try {
			c = BookListCursor.fetchByCollection(mDb, BookListCursor.title_normalized_index, 1,
					"\"\"\"");
		} catch (final Exception e) {
		}
		assertNull(c);
	}

	public void testRenamingCollection() {
		final long id = addBook("Foo").longValue();
		final BookCollectionCursor collectionsAll = BookCollectionCursor.fetchBookCollections(mDb,
				id, true);
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(mDb, id,
				false);

		CollectionActions.addCollection(mDb, id, FAVORITES_ID);

		CollectionActions.renameCollection(mDb, FAVORITES_ID, "New name");

		assertEquals("New name|1|1\nReading now|0|null\nTo read|0|null\nHave read|0|null",
				bookCollections(collectionsAll));
		assertEquals("New name|1|1", bookCollections(collections));

		collectionsAll.close();
		collections.close();
	}
}
