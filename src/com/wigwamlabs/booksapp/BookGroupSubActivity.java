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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.wigwamlabs.booksapp.db.BookGroupCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class BookGroupSubActivity extends SubActivity implements FilterEditText.Callback,
		OnItemClickListener {
	private static final int MAIN_CURSOR = 0;
	private int mBookGroup;
	private final DatabaseAdapter mDb;
	private EmptyListDrawable.Observer mEmptyListBackground;
	private final ListView mGroupList;

	public BookGroupSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db) {
		super(context, manager);
		mDb = db;

		setContentView(R.layout.book_group_main);
		mGroupList = (ListView) findViewById(R.id.group_list);
		mGroupList.setOnItemClickListener(this);

		getTitleBar().enableFilter(this);
	}

	@Override
	public void filterList(CharSequence filter) {
		final BookGroupAdapter adapter = (BookGroupAdapter) mGroupList.getAdapter();
		adapter.getFilter().filter(filter);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final BookGroupCursor item = (BookGroupCursor) parent.getItemAtPosition(position);
		BookGroup.openListActivity(getManager(), mBookGroup, item._id(), item.name());
	}

	public SubActivity prepare(int bookGroup) {
		mBookGroup = bookGroup;
		final BookGroupAdapter adapter = new BookGroupAdapter(getContext(), mDb, this, MAIN_CURSOR,
				false, mBookGroup);
		mGroupList.setAdapter(adapter);

		if (mEmptyListBackground != null)
			mEmptyListBackground.setAdapter(null);

		final int drawableId;
		final int primaryTextId;
		int secondaryTextId = 0;
		switch (bookGroup) {
		case BookGroup.AUTHORS:
			drawableId = R.drawable.emptylist_authors;
			primaryTextId = R.string.no_authors;
			break;
		case BookGroup.CONTACTS:
			drawableId = R.drawable.emptylist_contacts;
			primaryTextId = R.string.no_contacts;
			secondaryTextId = R.string.no_contacts_description;
			break;
		case BookGroup.PUBLISHERS:
			drawableId = R.drawable.emptylist_publishers;
			primaryTextId = R.string.no_publishers;
			break;
		case BookGroup.SERIES:
			drawableId = R.drawable.emptylist_series;
			primaryTextId = R.string.no_series;
			break;
		case BookGroup.SUBJECTS:
			drawableId = R.drawable.emptylist_subjects;
			primaryTextId = R.string.no_subjects;
			break;
		default:
			drawableId = R.drawable.emptylist_books;
			primaryTextId = R.string.no_books;
		}
		mEmptyListBackground = new EmptyListDrawable.Observer(getContext(), mGroupList, drawableId,
				primaryTextId, secondaryTextId);
		mEmptyListBackground.setAdapter(adapter);

		return this;
	}
}
