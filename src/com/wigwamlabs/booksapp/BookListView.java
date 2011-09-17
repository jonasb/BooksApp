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
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googleclient.GoogleAccountAction;
import com.wigwamlabs.util.ListViewUtils;
import com.wigwamlabs.util.MenuUtils;
import com.wigwamlabs.util.WeakDataSetObserverProxy;

public class BookListView extends LinearLayout implements OnItemClickListener,
		OnItemLongClickListener, AddBookButton.Callback, FilterEditText.Callback {
	private BookListAdapter mAdapter;
	private ListViewCheckButton mCheckButton;
	private Long mCollectionId;
	private CountView mCountView;
	private final DataSetObserver mDataSetObserver;
	private DatabaseAdapter mDb;
	private EmptyListDrawable mEmptyListBackground;
	private final String mEmptyListString;
	/* package */GoogleAccountAction mGoogleDocsAction;
	private final ListView mList;
	private final PauseThumbnailsOnScrollListener mOnScrollListener = new PauseThumbnailsOnScrollListener();
	private int mPositionToShow = AdapterView.INVALID_POSITION;
	private final AdapterPreviousNextProvider mPreviousNextProvider = new AdapterPreviousNextProvider();
	private SubActivityManager mSubActivityManager;

	public BookListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BookListView);
		mEmptyListString = a.getString(R.styleable.BookListView_emptyListText);
		a.recycle();

		setOrientation(VERTICAL);

		mList = new ListView(context);
		addView(mList, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		mList.setFastScrollEnabled(true);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);
		mList.setOnScrollListener(mOnScrollListener);

		mDataSetObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				onDataSetChanged();
			}
		};
	}

	@Override
	public void filterList(CharSequence filter) {
		mAdapter.getFilter().filter(filter);
	}

	public ListView getList() {
		return mList;
	}

	public void init(DatabaseAdapter db, SubActivityManager manager, ListViewCheckButton checkButton) {
		mDb = db;
		mSubActivityManager = manager;
		mCheckButton = checkButton;
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == R.id.google_account_request_code) {
			if (mGoogleDocsAction != null) {
				mGoogleDocsAction.onActivityResult(requestCode, resultCode, data);
				return true;
			}
		}
		return false;
	}

	public int onCreateOptionsMenu(Menu menu, int itemId) {
		menu.add(Menu.NONE, itemId, Menu.NONE, "").setIcon(R.drawable.menu_remove);
		menu.add(Menu.NONE, itemId + 1, Menu.NONE, "").setIcon(R.drawable.menu_delete);
		menu.add(Menu.NONE, itemId + 2, Menu.NONE, "").setIcon(R.drawable.menu_export);
		return itemId + 3;
	}

	public void onDataSetChanged() {
		updateCount();

		if (mList.getCount() == 0) {
			if (mEmptyListBackground == null) {
				mEmptyListBackground = new EmptyListDrawable(getContext(),
						R.drawable.emptylist_books, mEmptyListString);
			}
			setBackgroundDrawable(mEmptyListBackground);
		} else {
			setBackgroundDrawable(null);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onOpenBookDetails(position, id, ShowDirection.LEFT, null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
			final long id) {
		BookItemContextMenu.showForLocalBook(getContext(), mSubActivityManager, mDb, mCollectionId,
				position, id, parent, view, this);
		return true;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO for some reason this doesn't seem to have any impact
		if (mPositionToShow != AdapterView.INVALID_POSITION) {
			ListViewUtils.ensureVisible(mList, mPositionToShow);
			mPositionToShow = AdapterView.INVALID_POSITION;
		}
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void onNewBookOpened() {
		mPreviousNextProvider.setEnabled(false);
		// this means next/prev won't work after save. the reason is that
		// we don't know where it will end so 'current position' is unknown
	}

	/* package */void onOpenBookDetails(int position, long id, int direction,
			SubActivity activityToReplace) {
		mPreviousNextProvider.setCurrentPosition(position);
		mPreviousNextProvider.setEnabled(true);

		mSubActivityManager.create(BookDetailsSubActivity.class, activityToReplace).prepare(id)
				.show(direction);

		// remember the position to the next layout. for some reason setting
		// position here has no effect (possibly due to data set change)
		mPositionToShow = position;
	}

	public int onOptionsItemSelected(int currentItemId, int itemId) {
		if (currentItemId >= itemId && currentItemId < itemId + 3) {
			final BookListAdapter adapter = mAdapter;
			final Long[] ids = adapter.getActionableItems();

			final ListView listView = mList;
			final ListViewCheckButton checkButton = mCheckButton;
			final AsyncTaskListener<Long, Long, Integer> listener = new AsyncTaskListener<Long, Long, Integer>() {
				@Override
				public void onNextParam(Long param) {
					if (param != null) {
						final int pos = adapter.getPosition(param.longValue());
						if (pos >= 0)
							ListViewUtils.ensureVisible(listView, pos);
					}
				}

				@Override
				public void onPostExecute(Integer bookCount) {
					checkButton.disableCheckable();
				}

				@Override
				public void onProgressUpdate(Long... params) {
					for (final Long param : params) {
						final long id = param.longValue();
						adapter.uncheck(id);
					}
				}
			};

			if (currentItemId == itemId) { // remove from collection
				if (mCollectionId != null)
					new RemoveFromCollectionTask(getContext(), mDb, listener,
							mCollectionId.longValue()).execute(ids);
			} else if (currentItemId == itemId + 1) { // delete
				DeleteBookTask.createAndExecute(getContext(), mDb, listener, ids);
			} else if (currentItemId == itemId + 2) { // export
				ExportBookAction.export(getContext(), mDb, mSubActivityManager, listener, ids,
						new ExportBookAction.Callback() {
							@Override
							public void onGoogleDocsActionCreated(UploadToGoogleDocsAction action) {
								mGoogleDocsAction = action;
							}
						});
			}
		}
		return itemId + 3;
	}

	public int onPrepareOptionsMenu(Menu menu, int itemId) {
		final Resources res = getResources();

		final int actionableItemCount = mAdapter.getActionableItemCount();

		final MenuItem removeFromCollection = menu.getItem(itemId);
		final boolean visible = (mCollectionId != null && actionableItemCount > 0);
		removeFromCollection.setVisible(visible);
		if (visible)
			removeFromCollection.setTitle(res.getString(R.string.remove_from_collection_count,
					Integer.valueOf(actionableItemCount)));

		MenuUtils.updateCountMenuItem(res, menu, itemId + 1, R.plurals.delete_books,
				actionableItemCount);
		MenuUtils.updateCountMenuItem(res, menu, itemId + 2, R.plurals.export_books,
				actionableItemCount);
		return itemId + 3;
	}

	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		final int position = mPreviousNextProvider.previousNextPosition(previous);
		if (position >= 0) {
			final long id = mAdapter.getItemId(position);
			onOpenBookDetails(position, id, previous ? ShowDirection.DOWN : ShowDirection.UP,
					activityToReplace);
		}
	}

	public void prepare(BookListAdapter adapter, Long collectionId) {
		mAdapter = adapter;
		mCollectionId = collectionId;
		mList.setAdapter(adapter);
		mPreviousNextProvider.setAdapter(adapter);
		mOnScrollListener.setAdapter(adapter);

		WeakDataSetObserverProxy.register(adapter, new WeakReference<DataSetObserver>(
				mDataSetObserver));

		// trigger manually the first time
		mDataSetObserver.onChanged();
	}

	public void setCallback(WeakReference<PreviousNextProvider.Callback> callback,
			boolean notifyDirectly) {
		mPreviousNextProvider.setCallback(callback, notifyDirectly);
	}

	public void setCountView(CountView countView) {
		mCountView = countView;
	}

	private void updateCount() {
		if (mCountView == null)
			return;
		mCountView.setCount(mList.getCount());
	}
}
