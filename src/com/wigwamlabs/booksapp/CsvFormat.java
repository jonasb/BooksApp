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

import java.nio.charset.Charset;

public final class CsvFormat {
	public static final String AUTHORS = "Authors";
	public static final String[] AUTHORS_ALTERNATIVES = { AUTHORS, "Author", "Creators", "Creator" };
	public static final Charset CHARSET = Charset.forName("UTF-8");
	public static final String COLLECTIONS = "Collections";
	public static final String[] COLLECTIONS_ALTERNATIVES = { COLLECTIONS };
	public static final String COVER_IMAGE_URL = "Cover Image URL";
	public static final String[] COVER_IMAGE_URL_ALTERNATIVES = { COVER_IMAGE_URL };
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String DESCRIPTION = "Description";
	public static final String[] DESCRIPTION_ALTERNATIVES = { DESCRIPTION, "Overview", "Summary" };
	public static final String DIMENSIONS = "Dimensions";
	public static final String[] DIMENSIONS_ALTERNATIVES = { DIMENSIONS };
	public static final String GOOGLE_ID = "Google ID";
	public static final String[] GOOGLE_ID_ALTERNATIVES = { GOOGLE_ID };
	public static final String INFO_URL = "Info URL";
	public static final String ISBN10 = "ISBN-10";
	public static final String[] ISBN10_ALTERNATIVES = { ISBN10, "i.s.b.n." };
	public static final String ISBN13 = "ISBN-13";
	public static final String[] ISBN13_ALTERNATIVES = { ISBN13, "e.a.n." };
	public static final String NOTES = "Notes";
	public static final String[] NOTES_ALTERNATIVES = { NOTES };
	public static final String PAGE_COUNT = "No. Of Pages";
	public static final String[] PAGE_COUNT_ALTERNATIVES = { PAGE_COUNT };
	public static final String PUBLISHER = "Publisher";
	public static final String[] PUBLISHER_ALTERNATIVES = { PUBLISHER, "Label" };
	public static final String RATING = "Rating";
	public static final String[] RATING_ALTERNATIVES = { RATING };
	public static final String RELEASE_DATE = "Release Date";
	public static final String[] RELEASE_DATE_ALTERNATIVES = { RELEASE_DATE, "Year" };
	public static final String SERIES = "Series";
	public static final String[] SERIES_ALTERNATIVES = { SERIES };
	public static final String SUBJECTS = "Subjects";
	public static final String[] SUBJECTS_ALTERNATIVES = { SUBJECTS, "Genres" };
	public static final String SUBTITLE = "Subtitle";
	public static final String[] SUBTITLE_ALTERNATIVES = { SUBTITLE };
	public static final String TITLE = "Title";
	public static final String[] TITLE_ALTERNATIVES = { TITLE };
	public static final String VOLUME = "Volume";
	public static final String[] VOLUME_ALTERNATIVES = { VOLUME, "No. in series" };

	private CsvFormat() {
	}
}
