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
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.csvreader.CsvReader;
import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.StringUtils;

public class ImportBookTask extends AsyncBookTask<CsvReader> {
	private static final int HEADER_AUTHORS = 0;
	private static final int HEADER_COLLECTIONS = 1;
	private static final int HEADER_COVER_IMAGE_URL = 2;
	private static final int HEADER_DESCRIPTION = 3;
	private static final int HEADER_DIMENSIONS = 4;
	private static final int HEADER_GOOGLE_ID = 5;
	private static final int HEADER_ISBN10 = 6;
	private static final int HEADER_ISBN13 = 7;
	private static final int HEADER_NOTES = 8;
	private static final int HEADER_PAGE_COUNT = 9;
	private static final int HEADER_PUBLISHER = 10;
	private static final int HEADER_RATING = 11;
	private static final int HEADER_RELEASE_DATE = 12;
	private static final int HEADER_SERIES = 13;
	private static final int HEADER_SUBJECTS = 14;
	private static final int HEADER_SUBTITLE = 15;
	private static final int HEADER_TITLE = 16;
	private static final int HEADER_VOLUME = 17;
	private static final int HEADERS_COUNT = 18;
	private static final int OPTION_COLLECTIONS = 2;
	private static final int OPTION_COVERS = 4;
	private static final int OPTION_NOTES = 3;
	private static final int OPTION_RATINGS = 1;
	private static final int OPTION_SKIP_DUPLICATES = 0;
	private static final String TAG = ImportBookTask.class.getName();

	private static Integer[] checkHeaders(CsvReader reader) throws IOException {
		final String[] headers = reader.getHeaders();
		for (int i = 0; i < headers.length; i++)
			headers[i] = headers[i].toLowerCase();

		final Integer[] h = new Integer[HEADERS_COUNT];
		h[HEADER_TITLE] = findHeader(headers, CsvFormat.TITLE_ALTERNATIVES);
		h[HEADER_SUBTITLE] = findHeader(headers, CsvFormat.SUBTITLE_ALTERNATIVES);
		h[HEADER_RATING] = findHeader(headers, CsvFormat.RATING_ALTERNATIVES);
		h[HEADER_AUTHORS] = findHeader(headers, CsvFormat.AUTHORS_ALTERNATIVES);
		h[HEADER_SERIES] = findHeader(headers, CsvFormat.SERIES_ALTERNATIVES);
		h[HEADER_VOLUME] = findHeader(headers, CsvFormat.VOLUME_ALTERNATIVES);
		h[HEADER_PUBLISHER] = findHeader(headers, CsvFormat.PUBLISHER_ALTERNATIVES);
		h[HEADER_RELEASE_DATE] = findHeader(headers, CsvFormat.RELEASE_DATE_ALTERNATIVES);
		h[HEADER_DESCRIPTION] = findHeader(headers, CsvFormat.DESCRIPTION_ALTERNATIVES);
		h[HEADER_ISBN10] = findHeader(headers, CsvFormat.ISBN10_ALTERNATIVES);
		h[HEADER_ISBN13] = findHeader(headers, CsvFormat.ISBN13_ALTERNATIVES);
		h[HEADER_GOOGLE_ID] = findHeader(headers, CsvFormat.GOOGLE_ID_ALTERNATIVES);
		h[HEADER_PAGE_COUNT] = findHeader(headers, CsvFormat.PAGE_COUNT_ALTERNATIVES);
		h[HEADER_DIMENSIONS] = findHeader(headers, CsvFormat.DIMENSIONS_ALTERNATIVES);
		h[HEADER_COLLECTIONS] = findHeader(headers, CsvFormat.COLLECTIONS_ALTERNATIVES);
		h[HEADER_SUBJECTS] = findHeader(headers, CsvFormat.SUBJECTS_ALTERNATIVES);
		h[HEADER_NOTES] = findHeader(headers, CsvFormat.NOTES_ALTERNATIVES);
		h[HEADER_COVER_IMAGE_URL] = findHeader(headers, CsvFormat.COVER_IMAGE_URL_ALTERNATIVES);
		return h;
	}

	private static boolean containsHeader(Integer[] headers) {
		for (final Integer header : headers) {
			if (header != null)
				return true;
		}
		return false;
	}

	public static boolean createAndExecute(Context context, DatabaseAdapter db,
			ContentResolver contentResolver, AsyncTaskListener<CsvReader, Long, Integer> listener,
			Uri uri) {
		Integer errorMsg = null;
		try {
			final CsvReader reader = new CsvReader(new InputStreamReader(
					contentResolver.openInputStream(uri), CsvFormat.CHARSET));

			reader.readHeaders();

			final Integer[] headers = checkHeaders(reader);
			if (containsHeader(headers)) {
				showDialogAndExecute(context, db, listener, reader, headers);
			} else {
				errorMsg = Integer.valueOf(R.string.import_error_wrong_format_toast);
			}
		} catch (final IOException e) {
			Log.e(TAG, "Exception", e);
			errorMsg = Integer.valueOf(R.string.import_error_cant_read_toast);
		}

		if (errorMsg != null) {
			Toast.makeText(context, errorMsg.intValue(), Toast.LENGTH_LONG).show();
		}

		return (errorMsg == null);
	}

	private static Integer findHeader(String[] headers, String[] headerAlternatives) {
		for (final String alt : headerAlternatives) {
			for (int i = 0; i < headers.length; i++) {
				if (alt.toLowerCase().equals(headers[i]))
					return Integer.valueOf(i);
			}
		}
		return null;
	}

	private static void showDialogAndExecute(final Context context, final DatabaseAdapter db,
			final AsyncTaskListener<CsvReader, Long, Integer> listener, final CsvReader reader,
			final Integer[] headers) {
		final boolean[] options = { true, false, false, false, true };
		final DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					new ImportBookTask(context, db, listener, options, headers).execute(reader);
				} else if (which == DialogInterface.BUTTON_NEGATIVE) {
					listener.onPostExecute(Integer.valueOf(0));
				}
			}
		};

		final DialogInterface.OnMultiChoiceClickListener optionsListener = new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i, boolean b) {
				options[i] = b;
			}
		};

		final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialogInterface) {
				listener.onPostExecute(Integer.valueOf(0));
			}
		};

		new AlertDialog.Builder(context)
				.setMultiChoiceItems(R.array.import_options, options, optionsListener)
				.setOnCancelListener(cancelListener)
				.setPositiveButton(R.string.import_button, buttonListener)
				.setNegativeButton(R.string.cancel_button, buttonListener).show();
	}

	private final Integer[] mHeaders;
	private final boolean[] mOptions;

	public ImportBookTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<CsvReader, Long, Integer> listener, boolean[] options,
			Integer[] headers) {
		super(context, db, listener);
		mOptions = options;
		mHeaders = headers;
	}

	@Override
	protected long doInBackground(Context context, DatabaseAdapter db, CsvReader csvReader)
			throws Exception {
		// won't be called
		return 0;
	}

	@Override
	protected Integer doInBackground(CsvReader... csvReaders) {
		final CsvReader reader = csvReaders[0];
		final DatabaseAdapter db = getDb();
		final Context context = getContext();

		int imported = 0;
		try {
			final int t = db.beginTransaction();
			while (reader.readRecord()) {
				final Long bookId = importBook(context, db, reader, t);
				if (bookId != null) {
					imported++;
					publishProgress(bookId);
				}
			}
			reader.close();
			db.setTransactionSuccessful(t);
		} catch (final Exception e) {
			Log.e(TAG, "Exception", e);
			abort();
		} finally {
			db.endTransaction();
		}
		return Integer.valueOf(imported);
	}

	private Date getDateOrNull(CsvReader reader, int header) throws IOException {
		return DateUtils.parseDateRelaxed(getStringOrNull(reader, header));
	}

	private Float getFloatOrNull(CsvReader reader, int header) throws IOException {
		final String f = getStringOrNull(reader, header);
		try {
			return Float.valueOf(Float.parseFloat(f));
		} catch (final Exception e) {
			return null;
		}
	}

	private Integer getIntegerOrNull(CsvReader reader, int header) throws IOException {
		final String i = getStringOrNull(reader, header);
		try {
			return Integer.valueOf(Integer.parseInt(i));
		} catch (final Exception e) {
			return null;
		}
	}

	private List<String> getStringListOrNull(CsvReader reader, int header) throws IOException {
		return CommaStringList.stringToList(getStringOrNull(reader, header));
	}

	private String getStringOrNull(CsvReader reader, int header) throws IOException {
		final Integer h = mHeaders[header];
		if (h == null)
			return null;
		return StringUtils.trimmedStringOrNull(reader.get(h.intValue()));
	}

	@Override
	protected CharSequence getToastMessage(Resources res, int bookCount) {
		if (bookCount == 0)
			return res.getString(R.string.imported_no_books_toast);
		return res.getQuantityString(R.plurals.imported_books_toast, bookCount,
				Integer.valueOf(bookCount));
	}

	private Long importBook(Context context, DatabaseAdapter db, CsvReader reader, int t)
			throws IOException {
		final BookEntry be = new BookEntry();

		// read all fields
		final String title = getStringOrNull(reader, HEADER_TITLE);
		final String subtitle = getStringOrNull(reader, HEADER_SUBTITLE);
		final Float rating = (mOptions[OPTION_RATINGS] ? getFloatOrNull(reader, HEADER_RATING)
				: null);
		final List<String> authors = getStringListOrNull(reader, HEADER_AUTHORS);
		final String series = getStringOrNull(reader, HEADER_SERIES);
		final Integer volume = getIntegerOrNull(reader, HEADER_VOLUME);
		final String publisher = getStringOrNull(reader, HEADER_PUBLISHER);
		final Date releaseDate = getDateOrNull(reader, HEADER_RELEASE_DATE);
		final String isbn10 = getStringOrNull(reader, HEADER_ISBN10);
		final String isbn13 = getStringOrNull(reader, HEADER_ISBN13);
		final String googleId = getStringOrNull(reader, HEADER_GOOGLE_ID);
		final String description = getStringOrNull(reader, HEADER_DESCRIPTION);
		final Integer pageCount = getIntegerOrNull(reader, HEADER_PAGE_COUNT);
		final String dimensions = getStringOrNull(reader, HEADER_DIMENSIONS);
		final List<String> subjects = getStringListOrNull(reader, HEADER_SUBJECTS);
		final String notes = (mOptions[OPTION_NOTES] ? getStringOrNull(reader, HEADER_NOTES) : null);
		final String coverUrl = (mOptions[OPTION_COVERS] ? getStringOrNull(reader,
				HEADER_COVER_IMAGE_URL) : null);
		final List<String> collections = (mOptions[OPTION_COLLECTIONS] ? getStringListOrNull(
				reader, HEADER_COLLECTIONS) : null);

		// look for existing book
		if (mOptions[OPTION_SKIP_DUPLICATES]) {
			final Long existingBookId = BookDetailCursor
					.findBookByIds(db, isbn10, isbn13, googleId);
			if (existingBookId != null) {
				return null;
			}
		}

		be.setTitle(title, subtitle);
		be.setRating(rating);
		be.setCreators(authors);
		be.setSeries(series, volume);
		be.setPublisher(publisher);
		be.setReleaseDate(releaseDate);
		be.setDescription(description);
		be.setIsbn10(isbn10);
		be.setIsbn13(isbn13);
		be.setGoogleId(googleId);
		be.setPageCount(pageCount);
		be.setDimensions(dimensions);
		be.setSubjects(subjects);
		be.setNotes(notes);
		be.setCoverUrl(coverUrl);
		be.setCollections(collections);
		final long bookId = be.executeInsert(db, t);

		if (coverUrl != null) {
			ThumbnailManager.save(context, bookId, null, null, null, coverUrl);
		}

		return Long.valueOf(bookId);
	}
}
