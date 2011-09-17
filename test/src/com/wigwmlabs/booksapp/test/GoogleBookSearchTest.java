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

import java.io.IOException;

import android.content.Context;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.api.client.apache.ApacheHttpTransport;
import com.wigwamlabs.booksapp.HttpTransportCache;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookFeed;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.googlebooks.GoogleBookSearch.FeedSearch;
import com.wigwmlabs.booksapp.test.ExpectedResult.Books;

public class GoogleBookSearchTest extends InstrumentationTestCase {
	private GoogleBookSearch mSearch;

	private void assertGeneratedDataEquals(GoogleBook expectedBook, GoogleBook actualBook) {
		assertEquals(expectedBook.description, actualBook.description);
		assertEquals(expectedBook.googleId, actualBook.googleId);
		assertEquals(expectedBook.isbn10, actualBook.isbn10);
		assertEquals(expectedBook.isbn13, actualBook.isbn13);
		assertEquals(expectedBook.pageCount, actualBook.pageCount);
		assertEquals(expectedBook.publisher, actualBook.publisher);
		assertEquals(expectedBook.releaseDate, actualBook.releaseDate);
		assertEquals(expectedBook.subtitle, actualBook.subtitle);
		assertEquals(expectedBook.title, actualBook.title);
		assertEquals(expectedBook.thumbnailSmallUrl, actualBook.thumbnailSmallUrl);
		assertEquals(expectedBook.thumbnailLargeUrl, actualBook.thumbnailLargeUrl);
	}

	private void assertResultEquals(GoogleBook expectedBook, GoogleBook actualBook)
			throws IOException {
		final String expected = GoogleApiTestUtilities.objectToXml(expectedBook,
				GoogleBookSearch.Namespace.DICTIONARY);
		String actual = GoogleApiTestUtilities.objectToXml(actualBook,
				GoogleBookSearch.Namespace.DICTIONARY);

		actual = actual.replaceAll("average=\"[0-9.]+\"", "average=\"3.5\"");

		assertEquals(expected, actual);

		assertGeneratedDataEquals(expectedBook, actualBook);
	}

	private void assertResultEquals(GoogleBookFeed result, GoogleBook... expectedBooks)
			throws IOException {
		String expected = "";
		for (final GoogleBook book : expectedBooks) {
			expected += "<entry>"
					+ GoogleApiTestUtilities.objectToXml(book,
							GoogleBookSearch.Namespace.DICTIONARY) + "</entry>";
		}
		String actual = GoogleApiTestUtilities.objectToXml(result,
				GoogleBookSearch.Namespace.DICTIONARY);
		// remove dynamic content from links
		actual = actual.replaceAll("&amp;dq=[^&]+", "&amp;dq=SEARCH_QUERY");
		actual = actual.replaceAll("&amp;cd=\\d+", "&amp;cd=X");
		// remove feed links
		final int finalEntry = actual.lastIndexOf("</entry>");
		actual = actual.substring(0, finalEntry + 8);
		// remove actual rating
		actual = actual.replaceAll("average=\"[0-9.]+\"", "average=\"3.5\"");

		if (!expected.equals(actual))
			Log.d("XXX", "Expected: " + expected + "\n but got: " + actual);
		assertEquals(expected, actual);

		// check isbns as they are not in the generated xml
		for (int i = 0; i < Math.min(result.books.size(), expectedBooks.length); i++) {
			final GoogleBook expectedBook = expectedBooks[i];
			final GoogleBook actualBook = result.books.get(i);
			assertGeneratedDataEquals(expectedBook, actualBook);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		// TODO should be getContext() but it returns a broken context
		final Context context = getInstrumentation().getTargetContext();
		HttpTransportCache.install(ApacheHttpTransport.INSTANCE, context);
		mSearch = new GoogleBookSearch(context);
	}

	public void testDescriptionIsUnescaped() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns("9780805745252").execute();
		assertEquals(1, result.totalResults);
		assertEquals(1, result.books.size());

		final GoogleBook b = result.books.get(0);
		assertFalse(b.description.contains("&"));

		// make sure the original description did indeed contain &
		assertTrue(b.descriptions.get(0).contains("&"));
	}

	public void testMergeWithFullBook() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.DragonTattoo.isbn10).execute();
		final GoogleBook book = result.books.get(0);
		assertFalse(book.isFullBook);

		final GoogleBook fullBook = mSearch.searchByGoogleId(book.googleId).execute();
		assertTrue(fullBook.isFullBook);

		book.mergeWithFullBook(fullBook);
		assertTrue(book.isFullBook);

		assertResultEquals(Books.DragonTattoo_Full, book);
	}

	public void testNoMatches() throws IOException {
		final GoogleBookFeed result = mSearch.searchByTitle("a").execute();

		assertEquals(0, result.totalResults);
		assertNull(result.books);
	}

	public void testSearchByGoogleId() throws IOException {
		final GoogleBook result = mSearch.searchByGoogleId(Books.DragonTattoo.googleId).execute();
		assertResultEquals(Books.DragonTattoo_Full, result);
		assertTrue(result.isFullBook);
	}

	public void testSearchByIsbnsMultiple() throws IOException {
		// test both ISBN 13 and 10
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.Stardust.isbn13,
				Books.DragonTattoo.isbn10).execute();
		// note that the result is ordered differently
		assertResultEquals(result, Books.DragonTattoo, Books.Stardust);
	}

	public void testSearchByIsbnsMultipleCreators() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.UML.isbn10).execute();
		final GoogleBook firstBook = result.books.get(0);
		assertTrue(firstBook.creators.size() > 1);
		assertResultEquals(result, Books.UML);
	}

	public void testSearchByIsbnsMultipleTitles() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.UML.isbn13).execute();
		final GoogleBook firstBook = result.books.get(0);
		assertEquals("The rational unified process made easy", firstBook.title);
		assertEquals("a practitioner's guide to the RUP", firstBook.subtitle);
		assertResultEquals(result, Books.UML);
	}

	public void testSearchByIsbnsSingle() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.Stardust.isbn13).execute();
		assertResultEquals(result, Books.Stardust);
	}

	public void testSearchByTitle() throws IOException {
		// TODO is spaces dealt with properly? and "?
		final GoogleBookFeed result = mSearch.searchByTitle("The Girl with the Dragon Tattoo")
				.execute();
		assertTrue(result.books.size() > 0);
		boolean foundBook = false;
		for (final GoogleBook b : result.books) {
			if (b.title.toLowerCase().contains("tattoo")) {
				foundBook = true;
				break;
			}
		}
		assertTrue(foundBook);
	}

	public void testSearchByTitleForBookWithoutIsbn() throws IOException {
		final GoogleBookFeed result = mSearch.searchByTitle("Pippi Goes on Board").execute();
		assertTrue(result.books.size() > 0);

		boolean foundBookWithoutIsbn = false;
		for (final GoogleBook book : result.books) {
			if (book.isbn10 == null && book.isbn13 == null)
				foundBookWithoutIsbn = true;
			// make sure it's possible to download book (i.e. that google id is
			// correct)
			final GoogleBook fullBook = mSearch.searchByGoogleId(book.googleId).execute();
			assertNotNull(fullBook);
		}
		assertTrue(foundBookWithoutIsbn);
	}

	public void testSearchNext() throws IOException {
		GoogleBookFeed result = mSearch.searchByTitle("wellpapp").execute();
		int numResults = result.books.size();
		assertEquals(10, numResults);

		while (true) {
			final FeedSearch nextSearch = mSearch.searchNext(result);
			if (nextSearch == null)
				break;
			result = nextSearch.execute();
			assertTrue(result.books.size() > 0);
			numResults += result.books.size();
		}

		assertTrue(numResults > 10);
	}

	public void testSearchNoNext() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.DragonTattoo.isbn13).execute();
		assertTrue(result.books.size() > 0);

		assertNull(mSearch.searchNext(result));
	}

	public void testSearchRelated() throws IOException {
		GoogleBookFeed result = mSearch.searchRelatedByIsbn(Books.PippiLongstocking.isbn10)
				.execute();
		assertTrue(result.books.size() > 0);

		result = mSearch.searchRelatedByIsbn(Books.PippiLongstocking.isbn13).execute();
		assertTrue(result.books.size() > 0);

		result = mSearch.searchRelatedByGoogleId(Books.PippiLongstocking.googleId).execute();
		assertTrue(result.books.size() > 0);
	}

	public void testUnicodeIsntXMLEscaped() throws IOException {
		final GoogleBookFeed result = mSearch.searchByIsbns(Books.PippiLongstocking.isbn10)
				.execute();
		final GoogleBook book = result.books.get(0);
		// make sure content isn't escaped
		assertTrue(book.title.contains("\u00E5"));
		assertResultEquals(result, Books.PippiLongstocking);
	}
}
