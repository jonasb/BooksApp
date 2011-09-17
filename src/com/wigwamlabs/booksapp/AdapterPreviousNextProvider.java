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

import android.widget.ListAdapter;

public class AdapterPreviousNextProvider {
	private ListAdapter mAdapter;
	private WeakReference<PreviousNextProvider.Callback> mCallback;
	private int mCurrentPosition;
	private boolean mEnabled = true;

	public int getCurrentPosition() {
		return mCurrentPosition;
	}

	public int previousNextPosition(boolean previous) {
		return previousNextPosition(mCurrentPosition, previous);
	}

	private int previousNextPosition(int startPos, boolean previous) {
		final int count = mAdapter.getCount();
		final int direction = previous ? -1 : 1;
		int newPos = startPos;

		do {
			newPos += direction;
			if (newPos < 0 || newPos >= count)
				return -1;
			// skip disabled items
		} while (!mAdapter.isEnabled(newPos));

		return newPos;
	}

	public void setAdapter(ListAdapter adapter) {
		mCurrentPosition = -1;
		mAdapter = adapter;
	}

	public void setCallback(WeakReference<PreviousNextProvider.Callback> callback,
			boolean notifyDirectly) {
		mCallback = callback;
		if (notifyDirectly)
			updatePrevNext();
	}

	public void setCurrentPosition(int position) {
		mCurrentPosition = position;
	}

	public void setEnabled(boolean enabled) {
		if (enabled == mEnabled)
			return;

		mEnabled = enabled;
		updatePrevNext();
	}

	private void updatePrevNext() {
		// TODO update when adapter changes
		final PreviousNextProvider.Callback callback = (mCallback != null ? mCallback.get() : null);
		if (callback != null) {
			if (mEnabled) {
				final boolean previous = previousNextPosition(mCurrentPosition, true) >= 0;
				final boolean next = previousNextPosition(mCurrentPosition, false) >= 0;
				callback.setPreviousNextAvailable(previous, next);
			} else {
				callback.disablePreviousNext();
			}
		}
	}
}
