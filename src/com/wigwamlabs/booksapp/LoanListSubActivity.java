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
import android.database.DataSetObserver;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.SeparatedListAdapter;

public class LoanListSubActivity extends SubActivity implements PreviousNextProvider {
	private static final int CURSOR_ACTIVE = 0;
	private static final int CURSOR_INACTIVE = 1;
	private final DatabaseAdapter mDb;
	private final ListView mList;
	private final AdapterPreviousNextProvider mPreviousNextProvider = new AdapterPreviousNextProvider();
	private final ImageDownloadCollection mThumbnails;

	protected LoanListSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db,
			ImageDownloadCollection thumbnails) {
		super(context, manager);
		mDb = db;
		mThumbnails = thumbnails;
		setContentView(R.layout.book_group_main);
		mList = (ListView) findViewById(R.id.group_list);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onOpenBookDetails(position, id, ShowDirection.LEFT, null);
			}
		});
	}

	/* package */void onOpenBookDetails(int position, long id, int direction,
			SubActivity activityToReplace) {
		mPreviousNextProvider.setCurrentPosition(position);

		getManager().create(BookDetailsSubActivity.class, activityToReplace).prepare(id)
				.show(direction);

		// TODO scrolling to 'active' item not implemented here (see e.g.
		// BookListView)
	}

	@Override
	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		final int position = mPreviousNextProvider.previousNextPosition(previous);
		if (position >= 0) {
			onOpenBookDetails(position, mList.getItemIdAtPosition(position),
					previous ? ShowDirection.DOWN : ShowDirection.UP, activityToReplace);
		}
	}

	public LoanListSubActivity prepare(long contactId) {
		final Context context = getContext();
		final Resources res = context.getResources();

		// active loans
		final BookListCursor activeList = BookListCursor.fetchLoansByContact(mDb, contactId, true);
		setCursor(activeList, CURSOR_ACTIVE);
		final BookListAdapter activeAdapter = new BookListAdapter(context, activeList, mThumbnails);

		// inactive loans
		final BookListCursor inactiveList = BookListCursor.fetchLoansByContact(mDb, contactId,
				false);
		setCursor(activeList, CURSOR_INACTIVE);
		final BookListAdapter inactiveAdapter = new BookListAdapter(context, inactiveList,
				mThumbnails);

		// set up main adapter
		final SeparatedListAdapter adapter = new SeparatedListAdapter(context);
		adapter.addSection(res.getString(R.string.active_loans_section), activeAdapter);
		adapter.addSection(res.getString(R.string.inactive_loans_section), inactiveAdapter);
		mList.setAdapter(adapter);
		mPreviousNextProvider.setAdapter(adapter);

		// close sub activity if all books are removed
		adapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				if (adapter.getCount() == 0) {
					adapter.unregisterDataSetObserver(this);
					LoanListSubActivity.this.close();
				}
			}
		});

		return this;
	}

	@Override
	public void setCallback(WeakReference<PreviousNextProvider.Callback> callback,
			boolean notifyDirectly) {
		mPreviousNextProvider.setCallback(callback, notifyDirectly);
	}
}
