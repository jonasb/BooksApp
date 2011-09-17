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

import java.lang.ref.WeakReference;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.BookAddRemoveObserver;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.GoogleBookDetailsMainViewHolder;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.googlebooks.GoogleBookSearch.BookSearch;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;

public class GoogleBookDetailsSubActivity extends SubActivity implements BookAddRemoveObserver,
		BookSearch.Callback, ImageViewPopup.Callback {
	private final GoogleBookSearch mBookSearch;
	private GoogleBook mCurrentBook;
	private final DatabaseAdapter mDb;
	private ImageDownloadCollection mLargeThumbnails;
	@SuppressWarnings("unused")
	private PreviousNextClient mPreviousNextClient;
	private ImageDownloadCollection mSmallThumbnails;
	private final GoogleBookDetailsMainViewHolder mViewHolder;

	public GoogleBookDetailsSubActivity(Context context, SubActivityManager manager,
			DatabaseAdapter db, GoogleBookSearch bookSearch) {
		super(context, manager);
		mDb = db;
		mBookSearch = bookSearch;

		setContentView(R.layout.google_book_details_main);
		mViewHolder = new GoogleBookDetailsMainViewHolder(getRoot(), getTitleBar());

		new ImageViewPopup(mViewHolder.thumbnailView, this);

		mViewHolder.saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				saveToDatabase();
			}
		});

		db.addBookAddRemoveObserver(new WeakReference<BookAddRemoveObserver>(this));
	}

	@Override
	public void onAssignHighResolutionImage(ImageView imageView) {
		mLargeThumbnails.attachUrl(mCurrentBook.thumbnailLargeUrl, imageView,
				WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder), false);
	}

	@Override
	public void onBookAdded(long bookId, String googleId) {
		if (mCurrentBook.googleId != null && mCurrentBook.googleId.equals(googleId)) {
			final BookDetailsSubActivity activity = getManager().createReplacement(
					BookDetailsSubActivity.class, this);
			activity.prepare(bookId).show(ShowDirection.SWITCH_CONTENT);
		}
	}

	@Override
	public void onBookRemoved(long bookId) {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.google_book_details_menu, menu);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mDb.removeBookAddRemoveObserver(this);
		// TODO need to add observer when activity is reused in the future
	}

	@Override
	public void onDownloadFinished(GoogleBook book) {
		mViewHolder.progressBar.setVisibility(View.GONE);
		if (book != null) {
			mCurrentBook.mergeWithFullBook(book);
			updateBook(mCurrentBook);
		} else {
			Toast.makeText(getContext(), R.string.book_detail_download_error, Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.save_book) {
			saveToDatabase();
		} else if (itemId == R.id.share_book) {
			ShareBook.shareBook(getManager().getActivity(), mCurrentBook.title,
					mCurrentBook.creatorsText, mCurrentBook.infoUrl());
		} else if (itemId == R.id.explore_related) {
			getManager().create(SearchSubActivity.class)
					.prepareRelatedSearch(false, mCurrentBook.googleId).show();
		} else
			return false;

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO enable refresh if !mCurrentBook.isFullBook
		return true;
	}

	public GoogleBookDetailsSubActivity prepare(GoogleBook book,
			ImageDownloadCollection smallThumbnails, ImageDownloadCollection largeThumbnails) {
		mPreviousNextClient = PreviousNextClient.install(this, mViewHolder.previousButton,
				mViewHolder.nextButton);

		assert (smallThumbnails != null);
		mSmallThumbnails = smallThumbnails;
		mLargeThumbnails = largeThumbnails;
		assert (book != null);

		updateBook(book);
		if (!book.isFullBook) {
			mViewHolder.progressBar.setVisibility(View.VISIBLE);
			mBookSearch.searchByGoogleId(book.googleId).executeInBackground(this);
		}

		return this;
	}

	/* package */void saveToDatabase() {
		new SaveGoogleBookTask(getContext(), mDb, null, mBookSearch, mSmallThumbnails,
				mLargeThumbnails).execute(mCurrentBook);
	}

	private void updateBook(GoogleBook book) {
		mCurrentBook = book;

		mViewHolder.update(getContext(), mSmallThumbnails, book);
	}
}
