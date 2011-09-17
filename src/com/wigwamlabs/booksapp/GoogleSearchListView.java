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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.util.ListViewUtils;
import com.wigwamlabs.util.ListViewUtils.State;
import com.wigwamlabs.util.ListViewUtils.StateListener;
import com.wigwamlabs.util.MenuUtils;

public class GoogleSearchListView extends GoogleBookListView implements StateListener,
		ListFooterView.Callback {
	private GoogleSearchAdapter mAdapter;
	private GoogleBookSearch mBookSearch;
	private CountView mCountView;
	private EmptyListDrawable mEmptyListBackground;
	private final ListFooterView mFooter;
	private View mProgressBar;

	public GoogleSearchListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mFooter = new ListFooterView(context, this);
		addFooterView(mFooter);
	}

	@Override
	protected GoogleBook getBookForDisplay(int position) {
		return mAdapter.getItem(position);
	}

	public void init(DatabaseAdapter db, SubActivityManager manager,
			ListViewCheckButton checkButton, GoogleBookSearch bookSearch) {
		init(db, manager, checkButton);
		mBookSearch = bookSearch;
	}

	public int onCreateOptionsMenu(Menu menu, int itemId) {
		menu.add(Menu.NONE, itemId, Menu.NONE, "").setIcon(R.drawable.menu_import);
		return itemId + 1;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final int count = mAdapter.getCount();
		if (position < count) {
			super.onItemClick(parent, view, position, id);
		} else {
			mAdapter.loadMore();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position,
			final long id) {
		final int count = mAdapter.getCount();
		if (position < count) {
			return super.onItemLongClick(parent, view, position, id);
		} else {
			mAdapter.loadMore();
			return false;
		}
	}

	@Override
	public void onLoadMore() {
		// ensure footer isn't selected after loading next batch of books to
		// prevent endless loading of more books
		final int count = getCount();
		if (getSelectedItemPosition() == count - 1)
			setSelection(count - 2);

		mAdapter.loadMore();
	}

	@Override
	protected void onNotifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected boolean onOpenGoogleBookDetail(int position, GoogleBook book,
			SubActivity activityToReplace, int direction) {
		getSubActivityManager().create(GoogleBookDetailsSubActivity.class, activityToReplace)
				.prepare(book, getWebSmallThumbnails(), getWebLargeThumbnails()).show(direction);
		return true;
	}

	public int onOptionsItemSelected(int currentItemId, int itemId) {
		if (currentItemId == itemId) {
			final GoogleSearchAdapter adapter = mAdapter;
			final AsyncTaskListener<GoogleBook, Long, Integer> listener = new AsyncTaskListener<GoogleBook, Long, Integer>() {
				@Override
				public void onNextParam(GoogleBook param) {
					if (param != null) {
						final int pos = adapter.getPosition(param);
						if (pos >= 0)
							ListViewUtils.ensureVisible(GoogleSearchListView.this, pos);
					}
				}

				@Override
				public void onPostExecute(Integer bookCount) {
					if (!adapter.hasCheckedItems())
						getCheckButton().disableCheckable();
				}

				@Override
				public void onProgressUpdate(Long... params) {
					// the adapter is sometimes notified before the thumbnails
					// are written to disk, hence we need call
					// notifyDataSetChanged once more to be sure the thumbnails
					// are updated
					adapter.notifyDataSetChanged();
				}
			};

			final GoogleBook[] books = mAdapter.getActionableItems();
			new SaveGoogleBookTask(getContext(), getDb(), listener, mBookSearch,
					getWebSmallThumbnails(), getWebLargeThumbnails()).execute(books);
		}
		return itemId + 1;
	}

	public int onPrepareOptionsMenu(Menu menu, int itemId) {
		final int actionableItemCount = mAdapter.getActionableItemCount();

		final Resources res = getResources();
		MenuUtils.updateCountMenuItem(res, menu, itemId, R.plurals.save_books, actionableItemCount);

		return itemId + 1;
	}

	@Override
	protected void onShowContextMenuForGoogleBook(AdapterView<?> parent, View view, int position,
			long id, GoogleBook book) {
		BookItemContextMenu.showForRemoteBook(getContext(), getDb(), mBookSearch,
				getWebSmallThumbnails(), getWebLargeThumbnails(), position, book, id, parent, view,
				this, mAdapter);
	}

	@Override
	public void onStateChanged(State state) {
		mFooter.onStateChanged(state);

		if (mProgressBar != null) {
			mProgressBar.setVisibility(state == State.Loading ? View.VISIBLE : View.GONE);
		}

		if (mCountView != null) {
			final int totalResults = mAdapter.getTotalResults();
			if (totalResults >= 0) {
				mCountView.setCount(totalResults);
			}
			mCountView.setVisibility(totalResults >= 0 ? View.VISIBLE : View.GONE);
		}

		if (state == State.Finished && mAdapter.getCount() == 0) {
			if (mEmptyListBackground == null) {
				mEmptyListBackground = new EmptyListDrawable(getContext(),
						R.drawable.emptylist_books, R.string.no_matches);
			}
			setBackgroundDrawable(mEmptyListBackground);
		} else {
			setBackgroundDrawable(null);
		}
	}

	public void setAdapter(GoogleSearchAdapter adapter) {
		mAdapter = adapter;
		super.setAdapter(adapter);

		mAdapter.setStateListener(new WeakReference<StateListener>(this), true);
	}

	public void setCountView(CountView countView) {
		mCountView = countView;
	}

	public void setProgressBar(View progressBar) {
		mProgressBar = progressBar;
	}
}
