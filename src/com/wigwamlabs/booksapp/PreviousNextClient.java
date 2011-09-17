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

import android.view.View;
import android.view.View.OnClickListener;

public class PreviousNextClient implements PreviousNextProvider.Callback, OnClickListener {
	public static PreviousNextClient install(SubActivity subActivity, View previousView,
			View nextView) {
		return new PreviousNextClient(subActivity, previousView, nextView);
	}

	private final View mNextView;
	private final PreviousNextProvider mPreviousNextProvider;
	private final View mPreviousView;
	private final SubActivity mSubActivity;

	private PreviousNextClient(SubActivity subActivity, View previousView, View nextView) {
		mSubActivity = subActivity;
		mPreviousView = previousView;
		mNextView = nextView;

		disablePreviousNext(); // until we get update

		// get provider from parent activity and init
		final SubActivity parent = subActivity.getParentSubActivity();
		mPreviousNextProvider = (parent instanceof PreviousNextProvider ? (PreviousNextProvider) parent
				: null);
		if (mPreviousNextProvider != null) {
			mPreviousNextProvider.setCallback(
					new WeakReference<PreviousNextProvider.Callback>(this), true);
		}

		// init prev/next views
		mPreviousView.setOnClickListener(this);
		mNextView.setOnClickListener(this);
	}

	@Override
	public void disablePreviousNext() {
		mPreviousView.setVisibility(View.GONE);
		mNextView.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		mPreviousNextProvider.openPreviousNext(v == mPreviousView, mSubActivity);
	}

	@Override
	public void setPreviousNextAvailable(boolean previous, boolean next) {
		mPreviousView.setEnabled(previous);
		mPreviousView.setVisibility(View.VISIBLE);
		mNextView.setEnabled(next);
		mNextView.setVisibility(View.VISIBLE);
	}
}
