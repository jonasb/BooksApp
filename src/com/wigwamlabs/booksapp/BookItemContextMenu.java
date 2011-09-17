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

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;

import com.wigwamlabs.booksapp.IsbnSearchService.BookSearchItem;
import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBook;
import com.wigwamlabs.googlebooks.GoogleBookSearch;

public final class BookItemContextMenu {
	public static void showForIsbn13Book(Context context, final BookSearchItem item,
			final int position, final IsbnSearchListView listView) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int menuIndex) {
				if (menuIndex == 0) {
					listView.createIsbnBook(position, null, ShowDirection.LEFT, item);
				}
			}
		};

		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.create_book);
		menu.show();
	}

	public static void showForLocalBook(final Context context, final SubActivityManager sam,
			final DatabaseAdapter db, final Long collectionId, final int position, final long id,
			final AdapterView<?> parent, final View view,
			final OnItemClickListener itemClickListener) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int menuIndex) {
				final int afterRemoveFromCollectionDelta = (collectionId != null ? 0 : -1);

				if (menuIndex == 0) {
					itemClickListener.onItemClick(parent, view, position, id);
				} else if (menuIndex == 1) {
					sam.create(EditBookSubActivity.class).prepare(id, null).show();
				} else if (collectionId != null && menuIndex == 2) {
					new RemoveFromCollectionTask(context, db, null, collectionId.longValue())
							.execute(Long.valueOf(id));
				} else if (menuIndex == 3 + afterRemoveFromCollectionDelta) {
					DeleteBookTask.createAndExecute(context, db, null, Long.valueOf(id));
				}
			}
		};

		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.open_book);
		menu.add(R.string.edit_book);
		if (collectionId != null)
			menu.add(R.string.remove_from_collection);
		menu.add(R.string.delete_book);
		menu.show();
	}

	public static void showForRemoteBook(final Context context, final DatabaseAdapter db,
			final GoogleBookSearch bookSearch, final ImageDownloadCollection smallThumbnails,
			final ImageDownloadCollection largeThumbnails, final int position,
			final GoogleBook book, final long id, final AdapterView<?> parent, final View view,
			final OnItemClickListener itemClickListener, final ArrayAdapter<?> adapter) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int menuIndex) {
				if (menuIndex == 0) {
					itemClickListener.onItemClick(parent, view, position, id);
				} else if (menuIndex == 1) {
					// the adapter is sometimes notified before the thumbnails
					// are written to disk, hence we need call
					// notifyDataSetChanged once more to be sure the thumbnails
					// are updated
					final AsyncTaskListener<GoogleBook, Long, Integer> saveListener = new AsyncTaskListener<GoogleBook, Long, Integer>() {
						@Override
						public void onPostExecute(Integer integer) {
							adapter.notifyDataSetChanged();
						}
					};
					new SaveGoogleBookTask(context, db, saveListener, bookSearch, smallThumbnails,
							largeThumbnails).execute(book);
				}
			}
		};

		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.open_book);
		menu.add(R.string.save_book);
		menu.show();
	}

	public static void showForRemoteBookNoSave(final Context context, final int position,
			final long id, final AdapterView<?> parent, final View view,
			final OnItemClickListener itemClickListener) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int menuIndex) {
				if (menuIndex == 0) {
					itemClickListener.onItemClick(parent, view, position, id);
				}
			}
		};

		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.open_book);
		menu.show();
	}

	private BookItemContextMenu() {
	}
}
