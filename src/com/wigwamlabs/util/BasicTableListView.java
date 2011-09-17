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

package com.wigwamlabs.util;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.TableLayout;

public class BasicTableListView extends TableLayout {
	private ListAdapter mAdapter;
	private int mEmptyViewCount;
	private final DataSetObserver mObserver;

	public BasicTableListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				updateItems();
			}
		};
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mEmptyViewCount = getChildCount();
	}

	public void setAdapter(ListAdapter adapter) {
		if (mAdapter != null) {
			mAdapter.unregisterDataSetObserver(mObserver);
		}

		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mObserver);

		updateItems();
	}

	/* package */void updateItems() {
		final int itemCount = mAdapter.getCount();
		final int existingViewCount = getChildCount();

		// remove redundant views
		final int viewsToRemove = existingViewCount - mEmptyViewCount - itemCount;
		if (viewsToRemove > 0) {
			removeViews(mEmptyViewCount + itemCount, viewsToRemove);
		}

		// update/create views
		for (int i = 0; i < itemCount; i++) {
			final int viewIndex = i + mEmptyViewCount;
			final View convertView = viewIndex < existingViewCount ? getChildAt(viewIndex) : null;
			final View itemView = mAdapter.getView(i, convertView, this);
			if (convertView != itemView) {
				if (viewIndex < existingViewCount) {
					removeViewAt(viewIndex);
				}
				addView(itemView, viewIndex);
			}
		}

		// show/hide 'empty views'
		for (int i = 0; i < mEmptyViewCount; i++) {
			getChildAt(i).setVisibility(itemCount == 0 ? VISIBLE : GONE);
		}
	}
}
