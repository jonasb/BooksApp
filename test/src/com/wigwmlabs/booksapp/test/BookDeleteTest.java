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

import static com.wigwamlabs.util.CommaStringList.stringToList;
import static com.wigwamlabs.util.DateUtils.parseDate;

import java.util.Date;
import java.util.List;

import com.wigwamlabs.booksapp.db.AuthorsTable;
import com.wigwamlabs.booksapp.db.BookAuthorsTable;
import com.wigwamlabs.booksapp.db.BookCollectionsTable;
import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.BookFieldsTable;
import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.BookPublishersTable;
import com.wigwamlabs.booksapp.db.BookSubjectsTable;
import com.wigwamlabs.booksapp.db.BooksTable;
import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.CollectionsTable;
import com.wigwamlabs.booksapp.db.ContactsTable;
import com.wigwamlabs.booksapp.db.LoanActions;
import com.wigwamlabs.booksapp.db.LoansTable;
import com.wigwamlabs.booksapp.db.PublishersTable;
import com.wigwamlabs.booksapp.db.SubjectsTable;
import com.wigwamlabs.util.CommaStringList;

public class BookDeleteTest extends DatabaseTestCase {
	private long addLoan(final long id, String contactSystemId, String contactName) {
		final Date now = parseDate("yyyy", "2001");
		final Date returnBy = parseDate("yyyy", "2002");
		return LoanActions.startLoan(mDb, id, contactSystemId, contactName, now, returnBy);
	}

	private long createBook(String title, String authors, String publisher, String subjects,
			String collections) {
		final BookEntry be = new BookEntry();
		be.setTitle(title, null);
		be.setCreators(stringToList(authors));
		be.setPublisher(publisher);
		be.setSubjects(stringToList(subjects));
		final long id = be.executeInsertInTransaction(mDb);

		for (final String collection : CommaStringList.stringToList(collections)) {
			CollectionActions.addNewCollectionInTransaction(mDb, id, collection);
		}

		return id;
	}

	private void returnLoan(long bookId, long loanId) {
		final Date now = parseDate("yyyy", "2001");
		LoanActions.returnBook(mDb, bookId, now, loanId);
	}

	public void testCursorsAreRequired() throws Exception {
		final long deleteBookId = createBook("Delete book", "Author A", "Publisher A", "Subject A",
				"Collection A");
		addLoan(deleteBookId, "1", "John Doe");

		// set up
		final BookGroupCursor authors = BookGroupCursor.fetchAllAuthors(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> authorsChanged = observeCursorChange(authors);

		final BookDetailCursor book = BookDetailCursor.fetchBook(mDb, deleteBookId);
		final List<Boolean> bookChanged = observeCursorChange(book);

		final BookListCursor books = BookListCursor.fetchAll(mDb,
				BookListCursor.title_normalized_index, null);
		final List<Boolean> booksChanged = observeCursorChange(books);

		final BookGroupCursor collections = BookGroupCursor.fetchAllCollections(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> collectionsChanged = observeCursorChange(collections);

		final BookGroupCursor contacts = BookGroupCursor.fetchAllContacts(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> contactsChanged = observeCursorChange(contacts);

		final BookGroupCursor publishers = BookGroupCursor.fetchAllPublishers(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> publishersChanged = observeCursorChange(publishers);

		final BookGroupCursor subjects = BookGroupCursor.fetchAllSubjects(mDb,
				BookGroupCursor.name_normalized_index, null);
		final List<Boolean> subjectsChanged = observeCursorChange(subjects);

		// delete
		BookEntry.delete(mDb, deleteBookId);

		// check
		assertEquals(1, authorsChanged.size());
		authors.close();

		assertEquals(1, bookChanged.size());
		book.close();

		assertEquals(1, booksChanged.size());
		books.close();

		assertEquals(1, collectionsChanged.size());
		collections.close();

		assertEquals(1, contactsChanged.size());
		contacts.close();

		assertEquals(1, publishersChanged.size());
		publishers.close();

		assertEquals(1, subjectsChanged.size());
		subjects.close();

		// TODO could check that cursors aren't changed unless needed (e.g. book
		// without publisher shouldn't invalidate PUBLISHER_LIST
	}

	public void testDeleteBook() {
		final long deleteBookId = createBook("Delete book", "Author A, Author B", "Publisher A",
				"Subject A, Subject B", "Collection A, Collection B");
		final long loanId = addLoan(deleteBookId, "1", "User A");
		returnLoan(deleteBookId, loanId);
		addLoan(deleteBookId, "2", "User B");

		final long keepBookId = createBook("Keep book", "Author B, Author C", "Publisher A",
				"Subject B, Subject C", "Collection B, Collection C");
		addLoan(keepBookId, "2", "User B");

		BookEntry.delete(mDb, deleteBookId);

		assertTableEquals("2|Keep book", BooksTable.n, BooksTable._id + ", " + BooksTable.title);
		assertTableEquals("Keep book|Author B, Author C|null|null", BookFieldsTable.n);

		assertTableEquals("2|2|User B|bb user|1", ContactsTable.n);
		assertTableEquals("3|2|2|null|978303600", LoansTable.n);

		assertTableEquals("2|Author B|bb author|1\n3|Author C|cc author|1", AuthorsTable.n);
		assertTableEquals("2|2\n2|3", BookAuthorsTable.n);

		assertTableEquals(
				"1|Favorites|ffavorites|0\n2|Reading now|rreading now|0\n3|To read|tto read|0\n4|Have read|hhave read|0\n"
						+ "5|Collection A|ccollection a|0\n6|Collection B|ccollection b|1\n7|Collection C|ccollection c|1",
				CollectionsTable.n);
		assertTableEquals("2|6\n2|7", BookCollectionsTable.n);

		assertTableEquals("1|Publisher A|ppublisher a|1", PublishersTable.n);
		assertTableEquals("2|1", BookPublishersTable.n);

		assertTableEquals("2|Subject B|ssubject b|1\n3|Subject C|ssubject c|1", SubjectsTable.n);
		assertTableEquals("2|2\n2|3", BookSubjectsTable.n);
	}

	public void testPublisherIsRemoved() {
		final long deleteBookId = createBook("Delete book", "Author A", "Publisher A", "Subject A",
				"Collection A");
		BookEntry.delete(mDb, deleteBookId);

		assertTableEquals("", PublishersTable.n);
		assertTableEquals("", BookPublishersTable.n);
	}
}
