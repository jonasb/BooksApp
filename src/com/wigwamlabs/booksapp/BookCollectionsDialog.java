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

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.wigwamlabs.booksapp.db.BookCollectionCursor;
import com.wigwamlabs.booksapp.db.CollectionActions;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.DialogUtils;
import com.wigwamlabs.util.ListViewUtils;
import com.wigwamlabs.util.TextViewUtils;
import com.wigwamlabs.util.ViewUtils;

public class BookCollectionsDialog extends Dialog implements OnItemClickListener,
		OnItemLongClickListener {
	private final BookCollectionAdapter mAdapter;
	private final long mBookId;
	private final EditText mCollectionName;
	private final DatabaseAdapter mDb;
	private final ListView mListView;
	private final SubActivityManager mSubActivityManager;

	public BookCollectionsDialog(SubActivityManager manager, Context context, DatabaseAdapter db,
			long bookId) {
		super(context);
		mSubActivityManager = manager;
		mDb = db;
		mBookId = bookId;

		mAdapter = new BookCollectionAdapter(context, db, bookId);
		DialogUtils.closeCursorOnDialogDismiss(this, mAdapter);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(true);
		setContentView(R.layout.book_collection_dialog);

		// set up list
		mListView = (ListView) findViewById(R.id.collection_list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		// set up editor
		mCollectionName = (EditText) findViewById(R.id.new_collection_name);
		mCollectionName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				addNewCollection();
				return true;
			}
		});

		// set up add button
		final View addCollection = findViewById(R.id.add_collection);
		addCollection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				addNewCollection();
			}
		});

		TextViewUtils.disableViewWhenNoText(mCollectionName, addCollection);
	}

	protected void addNewCollection() {
		ViewUtils.hideSoftInput(mListView.getWindowToken(), getContext());

		final String name = mCollectionName.getText().toString().trim();
		if (name.length() > 0) {
			CollectionActions.addNewCollectionInTransaction(mDb, mBookId, name);
		}
		mCollectionName.setText(null);

		ListViewUtils.ensureVisible(mListView, mListView.getCount() - 1);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final BookCollectionCursor collection = (BookCollectionCursor) mAdapter.getItem(position);
		if (collection.bookId() == null) {
			CollectionActions.addCollection(mDb, mBookId, collection._id());
		} else {
			CollectionActions.removeCollection(mDb, mBookId, collection._id());
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSubActivityManager.create(BookListSubActivity.class)
				.prepareWithBookGroup(BookGroup.COLLECTIONS, id).show();
		dismiss();
		return true;
	}
}
