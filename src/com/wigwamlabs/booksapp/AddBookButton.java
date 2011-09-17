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
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class AddBookButton extends ImageView implements OnClickListener {
	public interface Callback {
		public void onNewBookOpened();
	}

	private final Callback mCallback;
	private Long mCollectionId;
	private final SubActivityManager mSubActivityManager;

	public AddBookButton(Context context, SubActivityManager manager, Callback callback) {
		super(context);
		mSubActivityManager = manager;
		mCallback = callback;

		setBackgroundResource(R.drawable.titlebar_icon_background);
		setImageResource(R.drawable.titlebar_add);

		setOnClickListener(this);
	}

	private void addBook() {
		final Context context = getContext();

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					addBookManually();
				} else if (which == 1) {
					addBookByScan(context);
				}
			}
		};

		final ContextMenu menu = new ContextMenu(context, listener);
		menu.add(R.string.create_book);
		menu.add(R.string.scan_barcode);
		menu.show();
	}

	/* package */void addBookByScan(final Context context) {
		final Intent scan = new Intent(context, BookScanActivity.class);
		if (mCollectionId != null) {
			scan.putExtra(BookScanActivity.COLLECTION_KEY, mCollectionId);
		}
		mSubActivityManager.getActivity().startActivityForResult(scan,
				BookScanActivity.SCAN_BOOKS_REQUEST);
	}

	/* package */void addBookManually() {
		mCallback.onNewBookOpened();
		mSubActivityManager.create(EditBookSubActivity.class).prepareNew(mCollectionId).show();
	}

	@Override
	public void onClick(View v) {
		addBook();
	}

	public int onCreateOptionsMenu(Menu menu, int itemId) {
		menu.add(Menu.NONE, itemId, Menu.NONE, R.string.create_book).setIcon(
				R.drawable.menu_create_edit);
		menu.add(Menu.NONE, itemId + 1, Menu.NONE, R.string.scan_barcode).setIcon(
				R.drawable.menu_scan);

		return itemId + 2;
	}

	public int onOptionsItemSelected(int currentItemId, int itemId) {
		if (currentItemId == itemId) {
			addBookManually();
		} else if (currentItemId == itemId + 1) {
			addBookByScan(getContext());
		}
		return itemId + 2;
	}

	public int onPrepareOptionsMenu(Menu menu, int itemId) {
		return itemId + 2;
	}

	public void setCollectionId(Long collectionId) {
		mCollectionId = collectionId;
	}
}
