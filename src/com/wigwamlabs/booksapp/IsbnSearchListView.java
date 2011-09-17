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

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.wigwamlabs.booksapp.IsbnSearchService.BookSearchItem;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.util.ListViewUtils;
import com.wigwamlabs.util.MenuUtils;

public class IsbnSearchListView extends GoogleBookListView {
	private IsbnSearchAdapter mAdapter;
	private final EmptyListDrawable.Observer mEmptyListBackground;
	/* package */UploadToGoogleDocsAction mGoogleDocsAction;

	public IsbnSearchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mEmptyListBackground = new EmptyListDrawable.Observer(context, this,
				R.drawable.emptylist_books, R.string.no_books);
	}

	/* package */void createIsbnBook(final int position, final SubActivity activityToReplace,
			final int direction, final BookSearchItem item) {
		final IsbnSearchAdapter adapter = mAdapter;
		final EditBookSubActivity.Callback onSave = new EditBookSubActivity.Callback() {
			@Override
			public void onSaved(long bookId, String creators, Integer pageCount, Date releaseDate,
					String title) {
				final GoogleBook b = new GoogleBook();
				b.databaseId = Long.valueOf(bookId);
				b.creatorsText = creators;
				b.pageCount = pageCount;
				b.releaseDate = releaseDate;
				b.title = title;
				item.book = b;

				setCurrentPosition(position);

				adapter.onBookCreated(item);
			}
		};
		getSubActivityManager().create(EditBookSubActivity.class, activityToReplace)
				.prepareNewWithISBN(item.isbn, onSave).show(direction);
	}

	@Override
	protected GoogleBook getBookForDisplay(int position) {
		return mAdapter.getItem(position).book;
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
		menu.add(Menu.NONE, itemId, Menu.NONE, "").setIcon(R.drawable.menu_delete);
		menu.add(Menu.NONE, itemId + 1, Menu.NONE, "").setIcon(R.drawable.menu_export);
		return itemId + 2;
	}

	@Override
	protected void onNotifyDataSetChanged() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	protected boolean onOpenGoogleBookDetail(final int position, GoogleBook book,
			final SubActivity activityToReplace, final int direction) {
		if (book == null) {
			final Context context = getContext();
			final Resources res = context.getResources();
			final BookSearchItem item = mAdapter.getItem(position);
			if (item.state == BookSearchItem.UNKNOWN) {
				Toast.makeText(context, R.string.isbn_search_unknown_toast, Toast.LENGTH_SHORT)
						.show();
			} else if (item.state == BookSearchItem.NOT_FOUND) {
				final String msg = res.getString(R.string.isbn_missing_create_toast, item.isbn);
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
				createIsbnBook(position, activityToReplace, direction, item);
				return true;
			}
			return false;
		} else {
			getSubActivityManager().create(IsbnBookDetailSubActivity.class, activityToReplace)
					.prepare(book, getWebSmallThumbnails()).show(direction);
			return true;
		}
	}

	public int onOptionsItemSelected(int currentItemId, int itemId) {
		if (currentItemId >= itemId && currentItemId < itemId + 2) {
			final IsbnSearchAdapter adapter = mAdapter;
			final AsyncTaskListener<Long, Long, Integer> listener = new AsyncTaskListener<Long, Long, Integer>() {
				@Override
				public void onNextParam(Long param) {
					if (param != null) {
						final int pos = adapter.getPosition(param.longValue());
						if (pos >= 0)
							ListViewUtils.ensureVisible(IsbnSearchListView.this, pos);
					}
				}

				@Override
				public void onPostExecute(Integer bookCount) {
					if (!adapter.hasCheckedItems())
						getCheckButton().disableCheckable();
				}

				@Override
				public void onProgressUpdate(Long... params) {
					for (final Long param : params) {
						final int pos = adapter.getPosition(param.longValue());
						if (pos >= 0)
							adapter.uncheck(pos);
					}
				}
			};

			final Long[] ids = adapter.getActionableItems();
			if (currentItemId == itemId) {
				DeleteBookTask.createAndExecute(getContext(), getDb(), listener, ids);
			} else if (currentItemId == itemId + 1) {
				ExportBookAction.export(getContext(), getDb(), getSubActivityManager(), listener,
						ids, new ExportBookAction.Callback() {
							@Override
							public void onGoogleDocsActionCreated(UploadToGoogleDocsAction action) {
								mGoogleDocsAction = action;
							}
						});
			}
		}
		return itemId + 2;
	}

	public int onPrepareOptionsMenu(Menu menu, int itemId) {
		final Resources res = getResources();
		final int actionableItemCount = mAdapter.getActionableItemCount();
		MenuUtils.updateCountMenuItem(res, menu, itemId, R.plurals.delete_books,
				actionableItemCount);
		MenuUtils.updateCountMenuItem(res, menu, itemId + 1, R.plurals.export_books,
				actionableItemCount);
		return itemId + 2;
	}

	@Override
	protected void onShowContextMenuForGoogleBook(AdapterView<?> parent, View view, int position,
			long id, GoogleBook book) {
		final Context context = getContext();
		if (book == null) {
			final BookSearchItem item = mAdapter.getItem(position);
			if (item.state == BookSearchItem.UNKNOWN) {
				Toast.makeText(context, R.string.isbn_search_unknown_toast, Toast.LENGTH_SHORT)
						.show();
			} else if (item.state == BookSearchItem.NOT_FOUND) {
				BookItemContextMenu.showForIsbn13Book(context, item, position, this);
			}
		} else {
			BookItemContextMenu.showForRemoteBookNoSave(context, position, id, parent, view, this);
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		mAdapter = (IsbnSearchAdapter) adapter;
		mEmptyListBackground.setAdapter(adapter);
	}
}
