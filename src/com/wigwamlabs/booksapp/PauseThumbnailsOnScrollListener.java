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

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

//TODO could pre-catch thumbnails outside current view on idle state
//TODO pause thumbnails on fast scroll?
public class PauseThumbnailsOnScrollListener implements OnScrollListener {
	private PausableThumbnailAdapter mAdapter;
	private boolean mPaused = false;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		setPaused(scrollState == SCROLL_STATE_FLING);
	}

	public void setAdapter(PausableThumbnailAdapter adapter) {
		mAdapter = adapter;
		mAdapter.setThumbnailsPaused(mPaused);
	}

	private void setPaused(boolean paused) {
		if (paused == mPaused)
			return;
		mPaused = paused;
		if (mAdapter != null) {
			mAdapter.setThumbnailsPaused(mPaused);
			if (!mPaused)
				mAdapter.notifyDataSetChanged();
		}
	}
}
