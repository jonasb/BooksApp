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
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;

import com.wigwamlabs.booksapp.db.BookCollectionCursor;
import com.wigwamlabs.booksapp.db.BookDetailCursor;
import com.wigwamlabs.booksapp.db.BookEntry;
import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.LoanActions;
import com.wigwamlabs.booksapp.db.LoanHistoryCursor;
import com.wigwamlabs.booksapp.ui.BookDetailsMainViewHolder;
import com.wigwamlabs.util.ContactsWrapper;
import com.wigwamlabs.util.DialogUtils;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;
import com.wigwamlabs.util.StringUtils;

public class BookDetailsSubActivity extends SubActivity implements ImageViewPopup.Callback,
		RatingBarPopup.Callback, ThumbnailManager.Observer {
	private static final int BOOK_CURSOR = 0;
	private static final int COLLECTIONS_CURSOR = 1;
	private static final int CONTACT_PICKER_RESULT = 1001;
	private static final int DEFAULT_LOAN_PERIOD_MONTHS = 1;
	private static final int LOAN_HISTORY_CURSOR = 2;
	private BookDetailCursor mBook;
	private BookCollectionCursor mCollections;
	private final DataSetObserver mCollectionsObserver;
	private final DatabaseAdapter mDb;
	private long mId;
	private final ImageDownloadCollection mLargeThumbnails;
	private LoanHistoryAdapter mLoanHistoryAdapter;
	private final DataSetObserver mObserver;
	@SuppressWarnings("unused")
	private PreviousNextClient mPreviousNextClient;
	private final ImageDownloadCollection mSmallThumbnails;
	private final BookDetailsMainViewHolder mViewHolder;

	public BookDetailsSubActivity(final Context context, SubActivityManager manager,
			DatabaseAdapter db, ImageDownloadCollection smallThumbnails,
			ImageDownloadCollection largeThumbnails) {
		super(context, manager);
		mDb = db;
		mSmallThumbnails = smallThumbnails;
		mLargeThumbnails = largeThumbnails;
		setContentView(R.layout.book_details_main);
		final TitleBar titleBar = getTitleBar();
		mViewHolder = new BookDetailsMainViewHolder(getRoot(), titleBar);

		// edit button
		mViewHolder.editButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onEdit();
			}
		});

		//
		final OnClickListener bookGroupListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				showBookGroupDialog(v);
			}
		};
		mViewHolder.creatorsView.setOnClickListener(bookGroupListener);
		mViewHolder.seriesView.setOnClickListener(bookGroupListener);
		mViewHolder.publishedView.setOnClickListener(bookGroupListener);
		mViewHolder.subjectsView.setOnClickListener(bookGroupListener);

		mViewHolder.collectionsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showCollectionsDialog();
			}
		});

		new ImageViewPopup(mViewHolder.thumbnailView, this);

		new RatingBarPopup(mViewHolder.ratingBar, this);

		mViewHolder.loanButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onLoanButtonClicked();
			}
		});

		mViewHolder.showLoanHistoryToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				showLoanHistory(isChecked);
			}
		});

		mObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				updateBook();
			}
		};

		mCollectionsObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				updateCollections();
			}
		};
	}

	private Date now() {
		final Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CONTACT_PICKER_RESULT) {
				final String id = data.getData().getLastPathSegment();
				String name = ContactsWrapper.getName(getManager().getActivity()
						.getContentResolver(), data);
				if (name == null)
					name = "";
				onLoanerPicked(id, name);
				return true;
			}
		}
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onAssignHighResolutionImage(ImageView imageView) {
		mLargeThumbnails.attachFile(ThumbnailManager.getThumbnail(getContext(), mId, false),
				imageView, WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder), false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.book_details_menu, menu);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ThumbnailManager.removeThumbnailObserver(this);
	}

	/* package */void onEdit() {
		getManager().create(EditBookSubActivity.class).prepare(mId, null).show();
	}

	protected void onLoanButtonClicked() {
		if (mBook.loanId() == null) {
			final Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
					ContactsWrapper.CONTENT_URI);
			getManager().getActivity().startActivityForResult(contactPickerIntent,
					CONTACT_PICKER_RESULT);
		} else {
			final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which == 0) {
						showReturnDateDialog();
					} else if (which == 1) {
						returnBook();
					}
				}
			};

			final ContextMenu menu = new ContextMenu(getContext(), listener);
			menu.add(R.string.change_return_date);
			menu.add(R.string.return_book);
			menu.show();
		}
	}

	private void onLoanerPicked(String systemContactId, String name) {
		// stale cursor since we get this call from onActivityResult() before
		// onResume() has been called
		mBook.requery();

		final Calendar cal = Calendar.getInstance();
		final Date now = cal.getTime();
		cal.add(Calendar.MONTH, DEFAULT_LOAN_PERIOD_MONTHS);
		final Date loanReturnBy = cal.getTime();

		LoanActions.startLoan(mDb, mId, systemContactId, name, now, loanReturnBy);
		LoanNotificationManager.createAlarm(getContext(), mId, mBook.title(), loanReturnBy);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.edit_book) {
			onEdit();
		} else if (itemId == R.id.delete_book) {
			DeleteBookTask.createAndExecute(getContext(), mDb, null, Long.valueOf(mId));
			// gets closed in updateBook()
		} else if (itemId == R.id.share_book) {
			ShareBook.shareBook(getManager().getActivity(), mBook.title(), mBook.creators(),
					mBook.infoUrl());
		} else if (itemId == R.id.explore_related) {
			String id = StringUtils.trimmedStringOrNull(mBook.googleId());
			final boolean isIsbn = (id != null);
			if (isIsbn) {
				id = StringUtils.trimmedStringOrNull(mBook.isbn13());
				if (id == null)
					id = StringUtils.trimmedStringOrNull(mBook.isbn10());
				if (id == null)
					return true;
			}
			getManager().create(SearchSubActivity.class).prepareRelatedSearch(isIsbn, id).show();
		} else
			return false;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final boolean relatable = StringUtils.trimmedStringOrNull(mBook.googleId()) != null
				|| StringUtils.trimmedStringOrNull(mBook.isbn10()) != null
				|| StringUtils.trimmedStringOrNull(mBook.isbn13()) != null;
		menu.findItem(R.id.explore_related).setVisible(relatable);
		return true;
	}

	@Override
	public void onRatingPopupClosed(float oldRating, float newRating) {
		if (newRating != oldRating) {
			final BookEntry u = new BookEntry();
			u.setRating(Float.valueOf(newRating));
			u.executeUpdateInTransaction(mDb, mId);
		}
	}

	@Override
	public void onThumbnailChanged(long bookId, boolean small, File file) {
		if (bookId == mId && small) {
			mViewHolder.updateThumbnail(getContext(), Long.valueOf(mId), mSmallThumbnails, null);
		}
	}

	public SubActivity prepare(long id) {
		mPreviousNextClient = PreviousNextClient.install(this, mViewHolder.previousButton,
				mViewHolder.nextButton);

		mId = id;
		mBook = BookDetailCursor.fetchBook(mDb, id);
		setCursor(mBook, BOOK_CURSOR);
		mBook.registerDataSetObserver(mObserver);
		updateBook();

		mCollections = BookCollectionCursor.fetchBookCollections(mDb, mId, false);
		setCursor(mCollections, COLLECTIONS_CURSOR);
		mCollections.registerDataSetObserver(mCollectionsObserver);
		updateCollections();

		mViewHolder.showLoanHistoryToggle.setChecked(false);
		showLoanHistory(false);
		if (mLoanHistoryAdapter != null) {
			mLoanHistoryAdapter.changeCursor(null);
			setCursor(null, LOAN_HISTORY_CURSOR);
		}

		ThumbnailManager.addThumbnailObserver(getContext(),
				new WeakReference<ThumbnailManager.Observer>(this));

		return this;
	}

	protected void returnBook() {
		LoanActions.returnBook(mDb, mId, now(), mBook.loanId().longValue());
		LoanNotificationManager.removeAlarm(getContext(), mId);
	}

	protected void showBookGroupDialog(View view) {
		final Context context = getContext();
		int bg = -1;
		if (view == mViewHolder.creatorsView)
			bg = BookGroup.AUTHORS;
		else if (view == mViewHolder.seriesView)
			bg = BookGroup.SERIES;
		else if (view == mViewHolder.publishedView)
			bg = BookGroup.PUBLISHERS;
		else if (view == mViewHolder.subjectsView)
			bg = BookGroup.SUBJECTS;
		final int bookGroup = bg;
		final BookGroupAdapter adapter = new BookGroupAdapter(context, mDb, bookGroup, mId, true);

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final BookGroupCursor item = (BookGroupCursor) adapter.getItem(which);
				BookGroup.openListActivity(getManager(), bookGroup, item._id(), item.name());
			}
		};

		final AlertDialog dialog = new AlertDialog.Builder(context).setAdapter(adapter, listener)
				.create();
		dialog.setCanceledOnTouchOutside(true);
		DialogUtils.closeCursorOnDialogDismiss(dialog, adapter);
		dialog.show();
	}

	protected void showCollectionsDialog() {
		new BookCollectionsDialog(getManager(), getContext(), mDb, mId).show();
	}

	protected void showLoanHistory(boolean show) {
		mViewHolder.loanHistoryList.setVisibility(show ? View.VISIBLE : View.GONE);

		if (show) {
			if (getCursor(LOAN_HISTORY_CURSOR) == null) {
				final LoanHistoryCursor loanHistory = LoanHistoryCursor.fetchFor(mDb, mId);
				// old cursor is closed by the adapter
				setCursor(loanHistory, LOAN_HISTORY_CURSOR, false);

				if (mLoanHistoryAdapter == null) {
					mLoanHistoryAdapter = new LoanHistoryAdapter(getContext(), loanHistory, now());
					mViewHolder.loanHistoryList.setAdapter(mLoanHistoryAdapter);
				} else {
					mLoanHistoryAdapter.changeCursor(loanHistory);
				}
			}
		}
	}

	protected void showReturnDateDialog() {
		final Date returnBy = mBook.loanReturnBy();
		final OnDateChangedListener listener = new OnDateChangedListener() {
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				returnBy.setYear(year - 1900);
				returnBy.setMonth(monthOfYear);
				returnBy.setDate(dayOfMonth);

				updateReturnByDate(returnBy);
			}
		};

		SimpleDatePickerDialog.show(getContext(), returnBy, listener);
	}

	void updateBook() {
		if (!mBook.moveToFirst()) {
			close();
			return;
		}

		final Long id = Long.valueOf(mId);
		final Date loanReturnBy = mBook.loanReturnBy();
		final int bookStatus = BookStatus.get(id, loanReturnBy);

		mViewHolder.update(getContext(), id, mSmallThumbnails, null, mBook.title(),
				mBook.subtitle(), mBook.creators(), mBook.rating(), mBook.series(), mBook.volume(),
				mBook.description(), mBook.publisher(), mBook.releaseDate(), mBook.isbn10(),
				mBook.isbn13(), mBook.subjects(), mBook.pageCount(), mBook.dimensions(),
				mBook.notes(), mBook.loanedTo(), bookStatus, loanReturnBy, now());
	}

	protected void updateCollections() {
		mViewHolder.updateCollections(mCollections);
	}

	protected void updateReturnByDate(Date returnAt) {
		final BookEntry be = new BookEntry();
		be.setLoanReturnBy(returnAt);
		be.executeUpdateInTransaction(mDb, mId);

		LoanNotificationManager.updateAlarm(getContext(), mId, mBook.title(), returnAt);
	}
}
