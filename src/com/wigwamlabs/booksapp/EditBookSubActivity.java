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
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.ImageViewUtils;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;
import com.wigwamlabs.util.StringUtils;

public class EditBookSubActivity extends SubActivity implements ImageViewPopup.Callback {
	public interface Callback {
		public void onSaved(long bookId, String creators, Integer pageCount, Date releaseDate,
				String title);
	}

	private static final int AUTHORS_CURSOR = 0;
	private static final int PICK_IMAGE_REQUEST_CODE = 1;
	private static final int PUBLISHERS_CURSOR = 1;
	private static final int SERIES_CURSOR = 2;
	private static final int SUBJECTS_CURSOR = 3;
	private Callback mCallback;
	private Long mCollectionId;
	private final MultiAutoCompleteTextView mCreators;
	private final DatabaseAdapter mDb;
	private final EditText mDimensions;
	private long mId;
	private final EditText mIsbn10;
	private final EditText mIsbn13;
	private final ImageDownloadCollection mLargeThumbnails;
	private Bitmap mNewThumbnail;
	private final EditText mNotes;
	private final EditText mOverview;
	private final EditText mPageCount;
	private final AutoCompleteTextView mPublisher;
	private final RatingBar mRating;
	private final EditText mReleaseDate;
	private final AutoCompleteTextView mSeries;
	private final ImageDownloadCollection mSmallThumbnails;
	private final MultiAutoCompleteTextView mSubjects;
	private final EditText mSubtitle;
	private final ImageView mThumbnailView;
	private final EditText mTitle;
	private final EditText mVolume;
	private final TextView mVolumeLabel;

	protected EditBookSubActivity(final Context context, SubActivityManager manager,
			DatabaseAdapter db, ImageDownloadCollection smallThumbnails,
			ImageDownloadCollection largeThumbnails) {
		super(context, manager);
		mDb = db;
		mSmallThumbnails = smallThumbnails;
		mLargeThumbnails = largeThumbnails;
		setContentView(R.layout.edit_book_main);

		mThumbnailView = (ImageView) findViewById(R.id.thumbnail);
		mCreators = (MultiAutoCompleteTextView) findViewById(R.id.creators);
		mTitle = (EditText) findViewById(R.id.title);
		mSubtitle = (EditText) findViewById(R.id.subtitle);
		mSeries = (AutoCompleteTextView) findViewById(R.id.series);
		mVolume = (EditText) findViewById(R.id.volume);
		mVolumeLabel = (TextView) findViewById(R.id.volume_label);
		mRating = (RatingBar) findViewById(R.id.rating);
		mOverview = (EditText) findViewById(R.id.overview);
		mPublisher = (AutoCompleteTextView) findViewById(R.id.publisher);
		mReleaseDate = (EditText) findViewById(R.id.release_date);
		mIsbn10 = (EditText) findViewById(R.id.isbn10);
		mIsbn13 = (EditText) findViewById(R.id.isbn13);
		mPageCount = (EditText) findViewById(R.id.page_count);
		mDimensions = (EditText) findViewById(R.id.dimensions);
		mSubjects = (MultiAutoCompleteTextView) findViewById(R.id.subjects);
		mNotes = (EditText) findViewById(R.id.notes);

		// thumbnail
		new ImageViewPopup(mThumbnailView, this);

		final View thumbnailPick = findViewById(R.id.thumbnail_pick);
		thumbnailPick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showThumbnailPicker();
			}
		});

		final View deleteThumbnail = findViewById(R.id.delete_thumbnail);
		deleteThumbnail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				deleteThumbnail();
			}
		});

		//
		final MultiAutoCompleteTextView.Tokenizer tokenizer = new CommaStringListTokenizer();
		final OnFocusChangeListener bookGroupFocusListener = new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				final MultiAutoCompleteTextView tv = (MultiAutoCompleteTextView) v;
				if (hasFocus) {
					if (tv.length() > 0)
						tv.append(", ");
				} else {
					final List<String> creators = CommaStringList.stringToList(tv.getText()
							.toString());
					tv.setText(CommaStringList.listToString(creators));
				}
			}
		};

		mCreators.setTokenizer(tokenizer);
		mCreators.setAdapter(new BookGroupAdapter(context, mDb, this, AUTHORS_CURSOR, true,
				BookGroup.AUTHORS));
		mCreators.setOnFocusChangeListener(bookGroupFocusListener);

		// series / volume editors
		mSeries.setAdapter(new BookGroupAdapter(context, mDb, this, SERIES_CURSOR, true,
				BookGroup.SERIES));

		mSeries.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				onSeriesTextChanged(s);
			}
		});

		//
		mPublisher.setAdapter(new BookGroupAdapter(context, mDb, this, PUBLISHERS_CURSOR, true,
				BookGroup.PUBLISHERS));

		mSubjects.setTokenizer(tokenizer);
		mSubjects.setAdapter(new BookGroupAdapter(context, mDb, this, SUBJECTS_CURSOR, true,
				BookGroup.SUBJECTS));
		mSubjects.setOnFocusChangeListener(bookGroupFocusListener);

		mReleaseDate.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					final EditText editText = (EditText) v;
					updateReleaseDate(getDate(editText));
				}
			}
		});

		final View releaseDatePick = findViewById(R.id.release_date_pick);
		releaseDatePick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePicker();
			}
		});

		final View saveButton = findViewById(R.id.save_button);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});

		final View cancelButton = findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				close();
			}
		});
	}

	/* package */void deleteThumbnail() {
		mNewThumbnail = SaveBookTask.REMOVE_THUMBNAIL;
		updateThumbnail(mThumbnailView, true);
	}

	private void doPrepare(long id, Long collectionId, String creators, String title,
			String subtitle, String series, Integer volume, Float rating, String description,
			String publisher, String releaseDate, String isbn10, String isbn13, Integer pageCount,
			String dimensions, String subjects, String notes, Callback callback) {
		mId = id;
		mCollectionId = collectionId;
		mCallback = callback;
		mNewThumbnail = null;

		updateThumbnail(mThumbnailView, true);
		updateEditText(mCreators, creators);
		updateEditText(mTitle, title);
		updateEditText(mSubtitle, subtitle);
		updateEditText(mSeries, series);
		updateEditText(mVolume, volume);
		updateRating(mRating, rating);
		updateEditText(mOverview, description);
		updateEditText(mPublisher, publisher);
		updateEditText(mIsbn10, isbn10);
		updateEditText(mIsbn13, isbn13);
		updateEditText(mReleaseDate, releaseDate);
		updateEditText(mPageCount, pageCount);
		updateEditText(mDimensions, dimensions);
		updateEditText(mSubjects, subjects);
		updateEditText(mNotes, notes);
	}

	Date getDate(EditText view) {
		final String text = getText(view);
		return DateUtils.parseDateRelaxed(text);
	}

	private Integer getIntOrNull(EditText view) {
		final String text = getText(view);
		if (text == null)
			return null;
		try {
			return Integer.valueOf(text);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private String getText(EditText view) {
		return StringUtils.trimmedStringOrNull(view.getText().toString());
	}

	private List<String> getTextAsList(TextView view) {
		return CommaStringList.stringToList(view.getText().toString());
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICK_IMAGE_REQUEST_CODE) {
			final Uri uri = (data == null ? null : data.getData());
			if (uri != null) {
				final Context context = getContext();
				final Cursor cursor = context.getContentResolver().query(uri,
						new String[] { MediaColumns.DATA }, null, null, null);
				cursor.moveToFirst();
				final String path = cursor.getString(0);
				cursor.close();

				final ImageDownloadTask.Callback onLoaded = new ImageDownloadTask.Callback() {
					@Override
					public void onImageDownloaded(CharSequence url, Bitmap image) {
						onNewThumbnailLoaded(image);
					}

					@Override
					public void onImageDownloadFinished() {
					}
				};

				final File f = new File(path);
				new ImageDownloadTask(ThumbnailManager.getThumbnailSize(context, false), onLoaded)
						.execute(f.toURI().toString());
			}
			return true;
		}
		return false;
	}

	@Override
	public void onAssignHighResolutionImage(ImageView imageView) {
		updateThumbnail(imageView, false);
	}

	/* package */void onNewThumbnailLoaded(Bitmap image) {
		mNewThumbnail = image;
		updateThumbnail(mThumbnailView, true);
	}

	/* package */void onSeriesTextChanged(CharSequence s) {
		final int vis = (s.toString().trim().length() == 0 ? View.GONE : View.VISIBLE);
		mVolumeLabel.setVisibility(vis);
		mVolume.setVisibility(vis);
	}

	public EditBookSubActivity prepare(long id, Callback callback) {
		final BookDetailCursor b = BookDetailCursor.fetchBook(mDb, id);
		b.moveToFirst();

		final String releaseDate = DateUtils.formatSparse(b.releaseDate());
		doPrepare(id, null, b.creators(), b.title(), b.subtitle(), b.series(), b.volume(),
				b.rating(), b.description(), b.publisher(), releaseDate, b.isbn10(), b.isbn13(),
				b.pageCount(), b.dimensions(), b.subjects(), b.notes(), callback);

		b.close();

		return this;
	}

	public EditBookSubActivity prepareNew(Long collectionId) {
		doPrepare(SaveBookTask.NEW_BOOK_ID, collectionId, null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null);
		return this;
	}

	public EditBookSubActivity prepareNewWithISBN(String isbn13, Callback callback) {
		doPrepare(SaveBookTask.NEW_BOOK_ID, null, null, null, null, null, null, null, null, null,
				null, null, isbn13, null, null, null, null, callback);
		return this;
	}

	protected void save() {
		final BookEntry b = new BookEntry();
		final List<String> creators = getTextAsList(mCreators);
		b.setCreators(creators);
		final String title = getText(mTitle);
		b.setTitle(title, getText(mSubtitle));
		final String series = getText(mSeries);
		b.setSeries(series, (series == null ? null : getIntOrNull(mVolume)));
		b.setRating(Float.valueOf(mRating.getRating()));
		b.setDescription(getText(mOverview));
		b.setPublisher(getText(mPublisher));
		final Date releaseDate = getDate(mReleaseDate);
		b.setReleaseDate(releaseDate);
		b.setIsbn10(getText(mIsbn10));
		b.setIsbn13(getText(mIsbn13));
		final Integer pageCount = getIntOrNull(mPageCount);
		b.setPageCount(pageCount);
		b.setDimensions(getText(mDimensions));
		b.setSubjects(getTextAsList(mSubjects));
		b.setNotes(getText(mNotes));

		final boolean showDetailScreen = (mId == SaveBookTask.NEW_BOOK_ID);
		final Callback callback = mCallback;
		final AsyncTaskListener<BookEntry, Long, Integer> listener = new AsyncTaskListener<BookEntry, Long, Integer>() {
			long savedId;

			@Override
			public void onPostExecute(Integer integer) {
				if (callback != null) {
					callback.onSaved(savedId, CommaStringList.listToString(creators), pageCount,
							releaseDate, title);
				}

				if (showDetailScreen) {
					getManager()
							.createReplacement(BookDetailsSubActivity.class,
									EditBookSubActivity.this).prepare(savedId)
							.show(ShowDirection.SWITCH_CONTENT);
				} else {
					close();
				}
			}

			@Override
			public void onProgressUpdate(Long... params) {
				savedId = params[0].longValue();
			}
		};
		new SaveBookTask(getContext(), mDb, listener, mId, mNewThumbnail, mCollectionId).execute(b);
	}

	void showDatePicker() {
		final OnDateChangedListener listener = new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				final Date d = new Date();
				d.setYear(year - 1900);
				d.setMonth(monthOfYear);
				d.setDate(dayOfMonth);
				updateReleaseDate(d);
			}
		};

		SimpleDatePickerDialog.show(getContext(), getDate(mReleaseDate), listener);
	}

	/* package */void showThumbnailPicker() {
		startActivityForResult(new Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.INTERNAL_CONTENT_URI), PICK_IMAGE_REQUEST_CODE);
	}

	private void updateEditText(EditText view, Object text) {
		view.setText(text == null ? null : text.toString());
	}

	private void updateRating(RatingBar view, Float rating) {
		view.setRating(rating == null ? 0f : rating.floatValue());
	}

	void updateReleaseDate(Date date) {
		mReleaseDate.setText(DateUtils.formatSparse(date));
	}

	private void updateThumbnail(ImageView imageView, boolean small) {
		if (mNewThumbnail != null) {
			final Bitmap thumbnail = (mNewThumbnail == SaveBookTask.REMOVE_THUMBNAIL ? null
					: mNewThumbnail);
			ImageViewUtils.updateImageView(imageView, thumbnail,
					WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder));
		} else {
			final ImageDownloadCollection thumbnails = (small ? mSmallThumbnails : mLargeThumbnails);
			LayoutUtilities.updateThumbnail(getContext(), imageView,
					mId == SaveBookTask.NEW_BOOK_ID ? null : Long.valueOf(mId), thumbnails, null,
					small, false);
		}
	}
}