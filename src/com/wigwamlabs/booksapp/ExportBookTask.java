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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.csvreader.CsvWriter;
import com.wigwamlabs.booksapp.db.BookCollectionCursor;
import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.StorageCompatibility;

public abstract class ExportBookTask extends AsyncBookTask<Long> {
	private static final String FILENAME = "BooksAppExport.csv";

	private static String collections(DatabaseAdapter db, long bookId) {
		final BookCollectionCursor collections = BookCollectionCursor.fetchBookCollections(db,
				bookId, false);
		final StringBuilder sb = new StringBuilder();
		for (collections.moveToFirst(); !collections.isAfterLast(); collections.moveToNext()) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(CommaStringList.escapeItem(collections.name()));
		}
		collections.close();
		return sb.toString();
	}

	private boolean mHeaderWritten = false;
	private final File mOutFile;
	private final CsvWriter mWriter;

	/* package */ExportBookTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener) throws FileNotFoundException {
		super(context, db, listener);

		// TODO delete file eventually
		File dir = StorageCompatibility.getExternalCacheDir(context);
		if (dir == null) { // no external storage
			dir = context.getDir("export", Context.MODE_WORLD_READABLE);
		}
		mOutFile = new File(dir, FILENAME);

		mWriter = new CsvWriter(new PrintWriter(new OutputStreamWriter(new FileOutputStream(
				mOutFile), CsvFormat.CHARSET)), ',');
	}

	@Override
	protected long doInBackground(Context context, DatabaseAdapter db, Long param) throws Exception {
		final long bookId = param.longValue();
		final BookDetailCursor book = BookDetailCursor.fetchBook(db, bookId);
		try {
			if (book.moveToFirst()) {
				if (!mHeaderWritten) {
					// TODO add column Format
					mWriter.writeRecord(new String[] { CsvFormat.TITLE, CsvFormat.SUBTITLE,
							CsvFormat.RATING, CsvFormat.AUTHORS, CsvFormat.SERIES,
							CsvFormat.VOLUME, CsvFormat.PUBLISHER, CsvFormat.RELEASE_DATE,
							CsvFormat.DESCRIPTION, CsvFormat.ISBN10, CsvFormat.ISBN13,
							CsvFormat.GOOGLE_ID, CsvFormat.PAGE_COUNT, CsvFormat.DIMENSIONS,
							CsvFormat.COLLECTIONS, CsvFormat.SUBJECTS, CsvFormat.NOTES,
							CsvFormat.COVER_IMAGE_URL, CsvFormat.INFO_URL });
					mHeaderWritten = true;
				}

				mWriter.write(book.title());
				mWriter.write(book.subtitle());
				mWriter.write(book.rating() == null ? null : book.rating().toString());
				mWriter.write(book.creators());
				mWriter.write(book.series());
				mWriter.write(book.volume() == null ? null : book.volume().toString());
				mWriter.write(book.publisher());
				mWriter.write(DateUtils.format(CsvFormat.DATE_FORMAT, book.releaseDate()));
				mWriter.write(book.description());
				mWriter.write(book.isbn10());
				mWriter.write(book.isbn13());
				mWriter.write(book.googleId());
				mWriter.write(book.pageCount() == null ? null : book.pageCount().toString());
				mWriter.write(book.dimensions());
				mWriter.write(collections(db, bookId));
				mWriter.write(book.subjects());
				mWriter.write(book.notes());
				mWriter.write(book.coverUrl());
				mWriter.write(book.infoUrl());

				mWriter.endRecord();
			}
		} finally {
			book.close();
		}

		return bookId;
	}

	@Override
	protected CharSequence getAbortToastMessage(Resources res) {
		return res.getString(R.string.export_failed_toast);
	}

	@Override
	protected CharSequence getToastMessage(Resources res, int bookCount) {
		return null;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		mWriter.close();
	}

	protected abstract void onFileFinished(File file);

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		mWriter.close();

		onFileFinished(mOutFile);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		Toast.makeText(getContext(), R.string.export_prepare_toast, Toast.LENGTH_LONG).show();
	}
}
