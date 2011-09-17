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

import static com.wigwmlabs.booksapp.test.GoogleApiTestUtilities.toList;
import junit.framework.TestCase;

import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleLink;
import com.wigwamlabs.googlebooks.GoogleRating;
import com.wigwamlabs.util.DateUtils;

public class GoogleBookTest extends TestCase {
	public void testScrubbingDates() {
		final GoogleBook b = new GoogleBook();
		b.scrub();
		assertNull(b.releaseDate);

		// full date
		b.dates = toList("2010-05-19");
		b.scrub();
		assertEquals("[2010-5-19]", DateUtils.format("[y-M-d]", b.releaseDate));

		// year only
		b.dates = toList("2011");
		b.scrub();
		assertEquals("[2011-1-1]", DateUtils.format("[y-M-d]", b.releaseDate));

		// first date is release date
		b.dates = toList("1980", "2011-01-05");
		b.scrub();
		assertEquals("[1980-1-1]", DateUtils.format("[y-M-d]", b.releaseDate));

		// don't choke on invalid date
		b.dates = toList("(invalid)");
		b.scrub();
		assertNull(b.releaseDate);
	}

	public void testScrubbingDescriptions() {
		final GoogleBook b = new GoogleBook();
		b.scrub();
		assertNull(b.description);

		b.descriptions = toList("First", "Second");
		b.scrub();
		assertEquals("First", b.description);
	}

	public void testScrubbingFormats() {
		final GoogleBook b = new GoogleBook();
		b.scrub();
		assertNull(b.pageCount);
		assertNull(b.dimensions);

		b.formats = toList("123 pages", "Dimensions 10.8x17.4x3.6 cm");
		b.scrub();
		assertEquals(123, b.pageCount.intValue());
		assertEquals("10.8x17.4x3.6 cm", b.dimensions);

		// don't crash on invalid format
		b.formats = toList("1_2_3 pages");
		b.scrub();
	}

	public void testScrubbingIdentifiers() {
		final GoogleBook b = new GoogleBook();
		b.identifiers = toList("tRqOPwAACAAJ", "ISBN:0123456789", "ISBN:0123456789012");
		assertNull(b.googleId);
		assertNull(b.isbn10);
		assertNull(b.isbn13);

		b.scrub();
		assertEquals("tRqOPwAACAAJ", b.googleId);
		assertEquals("0123456789", b.isbn10);
		assertEquals("0123456789012", b.isbn13);
	}

	public void testScrubbingIdentifiersWithoutIsbn() {
		final GoogleBook b = new GoogleBook();
		b.identifiers = toList("CDjYAAAAMAAJ", "PSU:000031649933");
		b.scrub();

		assertEquals("CDjYAAAAMAAJ", b.googleId);
	}

	public void testScrubbingLinks() {
		final GoogleBook b = new GoogleBook();
		final GoogleLink l = new GoogleLink();
		l.rel = "http://schemas.google.com/books/2008/thumbnail";
		l.href = "http://thumbnail?id=1&edge=curl&zoom=5";
		b.links = toList(l);
		b.scrub();

		assertEquals("http://thumbnail?id=1&zoom=5", b.thumbnailSmallUrl);
		assertEquals("http://thumbnail?id=1&zoom=1", b.thumbnailLargeUrl);
	}

	public void testScrubbingPublishers() {
		final GoogleBook b = new GoogleBook();
		b.scrub();
		assertNull(b.publisher);

		b.publishers = toList("First", "Second");
		b.scrub();
		assertEquals("First", b.publisher);
	}

	public void testScrubbingRating() {
		final GoogleBook b = new GoogleBook();
		b.scrub();
		assertNull(b.averageRating);

		b.rating = new GoogleRating();
		b.rating.average = "3.88";
		b.rating.max = "5";
		b.scrub();
		assertEquals(3.88, b.averageRating.floatValue(), 0.01);
	}

	public void testScrubbingSubjects() {
		final GoogleBook b = new GoogleBook();
		b.subjects = toList("subject a", "subject b", "subject a", "subject a");
		b.scrub();

		assertEquals(2, b.subjects.size());
		assertEquals(toList("subject a", "subject b"), b.subjects);
	}

	public void testScrubbingTitles() {
		{
			final GoogleBook b = new GoogleBook();
			b.titles = toList("Title");
			b.scrub();
			assertEquals("Title", b.title);
			assertNull(b.subtitle);
		}
		{
			final GoogleBook b = new GoogleBook();
			b.titles = toList("Title", "Sub title");
			b.scrub();
			assertEquals("Title", b.title);
			assertEquals("Sub title", b.subtitle);
		}
		{
			final GoogleBook b = new GoogleBook();
			b.titles = toList("Title", "<These", "should", "be", "ignored>", "Sub title");
			b.scrub();
			assertEquals("Title", b.title);
			assertEquals("Sub title", b.subtitle);
		}
	}
}
