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

package com.wigwamlabs.googlebooks;

import java.util.Date;
import java.util.List;

import android.text.Html;

import com.google.api.client.util.Key;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.CollectionUtils;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.StringUtils;

public final class GoogleBook {
	private static final String FORMAT_DIMENSIONS_PREFIX = "Dimensions ";
	private static final String FORMAT_PAGES_SUFFIX = " pages";
	private static final String ISBN_PREFIX = "ISBN:";
	public Float averageRating;
	@Key("dc:creator")
	public List<String> creators;
	public String creatorsText;
	public Long databaseId;
	@Key("dc:date")
	public List<String> dates;
	public String description;
	@Key("dc:description")
	public List<String> descriptions;
	public String dimensions;
	@Key("dc:format")
	public List<String> formats;
	public String googleId;
	@Key("dc:identifier")
	public List<String> identifiers;
	public String isbn10;
	public String isbn13;
	public boolean isFullBook = false;
	@Key("link")
	public List<GoogleLink> links;
	public Integer pageCount;
	public String publisher;
	@Key("dc:publisher")
	public List<String> publishers;
	@Key("gd:rating")
	public GoogleRating rating;
	public Date releaseDate;
	@Key("dc:subject")
	public List<String> subjects;
	public String subtitle;
	public String thumbnailLargeUrl;
	public String thumbnailSmallUrl;
	public String title;
	@Key("dc:title")
	public List<String> titles;

	private <T> T chooseFirst(List<T> list) {
		return (list == null || list.size() == 0 ? null : list.get(0));
	}

	public String infoUrl() {
		// same implementation as BookDetailCursor.infoUrl()
		final String theGoogleId = StringUtils.trimmedStringOrNull(googleId);
		if (theGoogleId == null)
			return null;
		return "http://books.google.com/books?id=" + theGoogleId;
	}

	public void mergeWithFullBook(GoogleBook fullBook) {
		assert (fullBook.isFullBook);

		creators = fullBook.creators;
		creatorsText = fullBook.creatorsText;
		databaseId = (fullBook.databaseId != null ? fullBook.databaseId : databaseId);
		dates = fullBook.dates;
		description = fullBook.description;
		descriptions = fullBook.descriptions;
		dimensions = fullBook.dimensions;
		formats = fullBook.formats;
		googleId = fullBook.googleId;
		identifiers = fullBook.identifiers;
		isbn10 = fullBook.isbn10;
		isbn13 = fullBook.isbn13;
		isFullBook = fullBook.isFullBook;
		links = fullBook.links;
		pageCount = fullBook.pageCount;
		publisher = fullBook.publisher;
		publishers = fullBook.publishers;
		releaseDate = fullBook.releaseDate;
		subjects = fullBook.subjects;
		subtitle = fullBook.subtitle;
		thumbnailLargeUrl = fullBook.thumbnailLargeUrl;
		thumbnailSmallUrl = fullBook.thumbnailSmallUrl;
		title = fullBook.title;
		titles = fullBook.titles;
	}

	public long save(DatabaseAdapter db) {
		try {
			final int t = db.beginTransaction();
			final long id = save(db, t);
			db.setTransactionSuccessful(t);
			return id;
		} finally {
			db.endTransaction();
		}
	}

	public long save(DatabaseAdapter db, int t) {
		final BookEntry u = new BookEntry();
		u.setCoverUrl(thumbnailLargeUrl);
		u.setCreators(creators);
		u.setDescription(description);
		u.setDimensions(dimensions);
		u.setGoogleId(googleId);
		u.setIsbn10(isbn10);
		u.setIsbn13(isbn13);
		u.setPageCount(pageCount);
		u.setPublisher(publisher);
		u.setReleaseDate(releaseDate);
		u.setSubjects(subjects);
		u.setTitle(title, subtitle);
		// don't copy rating, has to be user's rating

		final long id = u.executeInsert(db, t);
		databaseId = Long.valueOf(id);
		return id;
	}

	public void scrub() {
		// scrub creators
		creatorsText = CommaStringList.listToString(creators);
		// scrub dates
		if (dates != null && dates.size() > 0) {
			final String date = dates.get(0);
			releaseDate = DateUtils.parseDate("y-M-d", date);
			if (releaseDate == null)
				releaseDate = DateUtils.parseDate("y", date);
		}
		// scrub descriptions
		description = chooseFirst(descriptions);
		if (description != null) {
			// for some reason the description isn't always decoded properly
			description = Html.fromHtml(description).toString();
		}
		// scrub formats
		if (formats != null) {
			for (final String format : formats) {
				if (format.endsWith(FORMAT_PAGES_SUFFIX)) {
					final String n = format.substring(0,
							format.length() - FORMAT_PAGES_SUFFIX.length());
					try {
						pageCount = Integer.valueOf(n);
					} catch (final NumberFormatException e) {
					}
				} else if (format.startsWith(FORMAT_DIMENSIONS_PREFIX)) {
					dimensions = format.substring(FORMAT_DIMENSIONS_PREFIX.length());
				}
			}
		}
		// scrub identifiers
		if (identifiers != null) {
			for (final String identifier : identifiers) {
				if (identifier.startsWith(ISBN_PREFIX)) {
					final String isbn = identifier.substring(ISBN_PREFIX.length()).trim();
					if (isbn.length() == 10)
						isbn10 = isbn;
					else if (isbn.length() == 13)
						isbn13 = isbn;
				} else if (!identifier.contains(":")) {
					googleId = identifier;
				}
			}
		}
		// scrub links
		if (links != null) {
			for (final GoogleLink link : links) {
				if (link.rel.equals("http://schemas.google.com/books/2008/thumbnail")) {
					thumbnailSmallUrl = link.href;
					// remove curl effect
					thumbnailSmallUrl = thumbnailSmallUrl.replace("&edge=curl", "");
					// get large thumbnail
					thumbnailLargeUrl = thumbnailSmallUrl.replace("zoom=5", "zoom=1");
				}
			}
		}
		// scrub publishers
		publisher = chooseFirst(publishers);
		// scrub rating
		if (rating != null && rating.average != null && rating.max != null) {
			try {
				final float average = Float.parseFloat(rating.average);
				final int max = Integer.parseInt(rating.max);
				averageRating = Float.valueOf(5.f * average / max);
			} catch (final NumberFormatException e) {
				// do nothing
			}
		}
		// scrub subjects
		if (subjects != null) {
			CollectionUtils.removeDuplicates(subjects);
		}
		// scrub titles
		if (titles != null) {
			final int titleCount = titles.size();
			if (titleCount > 0)
				title = titles.get(0);
			subtitle = (titleCount < 2 ? null : titles.get(titleCount - 1));
		}
	}
}
