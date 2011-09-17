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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.ItemCountCursor;
import com.wigwamlabs.util.Pair;
import com.wigwamlabs.util.TextViewUtils;

public class MenuSubActivity extends SubActivity implements OnItemClickListener,
		OnItemLongClickListener {
	private static final int BOOK_COUNT_CURSOR = 0;
	public static final int BOOKS_ITEM = 1;
	private static final int COLLECTION_CURSOR = 1;

	private static Pair<Integer, String> item(Resources res, int drawable, int string) {
		return Pair.create(Integer.valueOf(drawable), res.getString(string));
	}

	private final DatabaseAdapter mDb;
	private final Pair<Integer, String>[] mFixedMenuItems;
	private final ListView mMenuList;

	protected MenuSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db) {
		super(context, manager);
		mDb = db;
		setContentView(R.layout.menu_main);
		mMenuList = (ListView) findViewById(R.id.menu_list);

		final Resources res = getResources();

		@SuppressWarnings({ "unchecked" })
		final Pair<Integer, String>[] fixedItems = new Pair[] {
				item(res, R.drawable.list_scan, R.string.scan_barcode),
				item(res, R.drawable.list_books, R.string.list_books),
				item(res, R.drawable.list_series, R.string.series),
				item(res, R.drawable.list_authors, R.string.authors),
				item(res, R.drawable.list_publishers, R.string.publisher_menu_item),
				item(res, R.drawable.list_contacts, R.string.contacts),
				item(res, R.drawable.list_subjects, R.string.subjects_menu_item),
				item(res, R.drawable.list_search, R.string.search) };

		mFixedMenuItems = fixedItems;

		mMenuList.setOnItemClickListener(this);
		mMenuList.setOnItemLongClickListener(this);
	}

	@Override
	public boolean onBackPressed() {
		// don't close since we're the base
		return false;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_menu, menu);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final SubActivityManager manager = getManager();
		if (position < mFixedMenuItems.length) {
			switch (position) {
			case 0:
				final Intent scan = new Intent(getContext(), BookScanActivity.class);
				manager.getActivity().startActivityForResult(scan,
						BookScanActivity.SCAN_BOOKS_REQUEST);
				break;
			case BOOKS_ITEM:
				manager.create(BookListSubActivity.class).prepare().show();
				break;
			case 2:
				manager.create(BookGroupSubActivity.class).prepare(BookGroup.SERIES).show();
				break;
			case 3:
				manager.create(BookGroupSubActivity.class).prepare(BookGroup.AUTHORS).show();
				break;
			case 4:
				manager.create(BookGroupSubActivity.class).prepare(BookGroup.PUBLISHERS).show();
				break;
			case 5:
				manager.create(BookGroupSubActivity.class).prepare(BookGroup.CONTACTS).show();
				break;
			case 6:
				manager.create(BookGroupSubActivity.class).prepare(BookGroup.SUBJECTS).show();
				break;
			case 7:
				manager.getActivity().onSearchRequested();
				break;
			}
		} else {
			manager.create(BookListSubActivity.class)
					.prepareWithBookGroup(BookGroup.COLLECTIONS, id).show();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		if (position < mFixedMenuItems.length) {
			onItemClick(parent, view, position, id);
			return true;
		}

		final Context context = getContext();
		final DatabaseAdapter db = mDb;
		final String collectionName = ((BookGroupCursor) mMenuList.getItemAtPosition(position))
				.name();
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface d, int which) {
				if (which == 0) {
					onItemClick(parent, view, position, id);
				} else if (which == 1) {
					showRenameCollectionDialog(id, context, collectionName);
				} else if (which == 2) {
					CollectionActions.deleteCollection(db, id);
				}
			}
		};
		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.open_collection);
		menu.add(R.string.rename_collection);
		menu.add(R.string.delete_collection);
		menu.show();
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == R.id.about) {
			getManager().create(AboutSubActivity.class).prepare().show();
		}
		return true;
	}

	public SubActivity prepare() {
		final ItemCountCursor bookCount = ItemCountCursor.fetchBookCount(mDb);
		setCursor(bookCount, BOOK_COUNT_CURSOR);
		final BookGroupCursor collections = BookGroupCursor.fetchAllCollections(mDb, -1, null);
		setCursor(collections, COLLECTION_CURSOR);
		final MenuAdapter menuAdapter = new MenuAdapter(getContext(), mFixedMenuItems, bookCount,
				collections);
		mMenuList.setAdapter(menuAdapter);
		return this;
	}

	/* package */void showRenameCollectionDialog(final long id, final Context context,
			final String collectionName) {
		final EditText rename = new EditText(context);
		rename.setText(collectionName);
		rename.setImeOptions(EditorInfo.IME_ACTION_DONE);
		rename.setSelectAllOnFocus(true);
		rename.setBackgroundResource(R.drawable.edittext);
		rename.setHint(R.string.collection_rename_hint);
		rename.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

		final DatabaseAdapter db = mDb;
		final DialogInterface.OnClickListener renameListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				CollectionActions.renameCollection(db, id, rename.getText().toString().trim());
			}
		};

		final AlertDialog dialog = new AlertDialog.Builder(context)
				.setTitle(R.string.rename_collection).setView(rename)
				.setPositiveButton(R.string.rename_button, renameListener)
				.setNegativeButton(R.string.cancel_button, null).create();
		TextViewUtils
				.disableDialogButtonWhenNoText(rename, dialog, DialogInterface.BUTTON_POSITIVE);
		dialog.show();
	}
}
