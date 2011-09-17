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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.wigwamlabs.util.ViewUtils;

public class SubActivityManager {
	interface Callback {
		public <T extends SubActivity> Object onCreateSubActivity(Class<T> klass);

		public void onSwitchSubActivities(SubActivity previousTop, SubActivity newTop, int direction);
	}

	public static class ShowDirection {
		public static final int DOWN = 0;
		public static final int LEFT = 1;
		public static final int NONE = 2;
		public static final int RIGHT = 3;
		public static final int SWITCH_CONTENT = 4;
		public static final int UP = 5;
	}

	private final Activity mActivity;
	private final ArrayList<SubActivity> mActivityStack = new ArrayList<SubActivity>();
	private SubActivity mActivityToBeReplaced;
	private final Callback mCallback;
	private boolean mOptionMenuHasBeenCreated = false;
	private boolean mTopHasBeenShown = false;

	public SubActivityManager(Activity activity, Callback callback) {
		mActivity = activity;
		mCallback = callback;
	}

	public void close(SubActivity subActivity) {
		final int index = mActivityStack.indexOf(subActivity);
		final boolean isTop = (index == mActivityStack.size() - 1);
		mActivityStack.remove(index);

		if (isTop && mTopHasBeenShown) {
			ViewUtils.hideSoftInput(subActivity.getRoot().getWindowToken(), mActivity);

			final SubActivity newTop = getTop();
			if (newTop != null)
				newTop.onResume();

			mCallback.onSwitchSubActivities(subActivity, newTop, ShowDirection.RIGHT);

			mOptionMenuHasBeenCreated = false;
		}
		subActivity.onDestroy();
	}

	@SuppressWarnings("unchecked")
	public <T extends SubActivity> T create(Class<T> klass) {
		final T sa = (T) mCallback.onCreateSubActivity(klass);
		assert (sa != null);

		mActivityStack.add(sa);
		mTopHasBeenShown = false;

		return sa;
	}

	public <T extends SubActivity> T create(Class<T> klass, SubActivity old) {
		return old != null ? createReplacement(klass, old) : create(klass);
	}

	@SuppressWarnings("unchecked")
	public <T extends SubActivity> T createReplacement(Class<T> klass, SubActivity old) {
		assert (old != null);
		final T sa = (T) mCallback.onCreateSubActivity(klass);
		assert (sa != null);

		final int index = mActivityStack.indexOf(old);
		mActivityToBeReplaced = mActivityStack.set(index, sa);
		mTopHasBeenShown = false;

		return sa;
	}

	public Activity getActivity() {
		return mActivity;
	}

	public SubActivity getChild(SubActivity parent) {
		final int parentIndex = mActivityStack.indexOf(parent);
		if (parentIndex >= 0 && parentIndex < mActivityStack.size() - 1)
			return mActivityStack.get(parentIndex + 1);
		return null;
	}

	public SubActivity getParent(SubActivity subActivity) {
		for (int i = mActivityStack.size() - 1; i >= 0; i--) {
			if (subActivity == mActivityStack.get(i)) {
				if (i > 0)
					return mActivityStack.get(i - 1);
				return null;
			}
		}
		return null;
	}

	private SubActivity getPreviousTop() {
		if (mActivityToBeReplaced != null)
			return mActivityToBeReplaced;

		final int index = mActivityStack.size() - 2;
		if (index >= 0)
			return mActivityStack.get(index);
		return null;
	}

	private SubActivity getTop() {
		return (mActivityStack.isEmpty() ? null : mActivityStack.get(mActivityStack.size() - 1));
	}

	public void goBackToBottomActivity() {
		if (mActivityStack.size() <= 1)
			return;

		// remove top activity
		final int topIndex = mActivityStack.size() - 1;
		final SubActivity previousTop = mActivityStack.get(topIndex);
		mActivityStack.remove(topIndex);

		// hide soft input
		ViewUtils.hideSoftInput(previousTop.getRoot().getWindowToken(), mActivity);

		// remove and destroy all activities in between
		for (int i = mActivityStack.size() - 1; i >= 1; i--) {
			final SubActivity sa = mActivityStack.remove(i);
			sa.onDestroy();
		}
		final SubActivity newTop = mActivityStack.get(0);

		// prepare new top activity to be shown
		newTop.onResume();

		// animate
		mCallback.onSwitchSubActivities(previousTop, newTop, ShowDirection.RIGHT);

		// destroy previous top
		previousTop.onDestroy();

		mOptionMenuHasBeenCreated = false;
	}

	public boolean isTop(SubActivity subActivity) {
		return getTop() == subActivity;
	}

	void onActivityPause() {
		for (final SubActivity sa : mActivityStack) {
			sa.onHostPause();
		}
	}

	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		final SubActivity top = getTop();
		if (top == null)
			return false;

		return top.onActivityResult(requestCode, resultCode, data);
	}

	void onActivityResume() {
		for (final SubActivity sa : mActivityStack) {
			sa.onHostResume();
		}
	}

	public boolean onBackPressed() {
		final SubActivity top = getTop();
		if (top == null)
			return false;

		return top.onBackPressed();
	}

	public void onDestroy() {
		for (int i = mActivityStack.size() - 1; i >= 0; i--) {
			final SubActivity sa = mActivityStack.get(i);
			sa.onDestroy();
		}
		mActivityStack.clear();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		final SubActivity top = getTop();
		if (top == null)
			return false;
		return top.onOptionsItemSelected(item);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		final SubActivity top = getTop();
		if (top == null)
			return false;
		if (!mOptionMenuHasBeenCreated) {
			menu.clear();
			top.onCreateOptionsMenu(menu);
			mOptionMenuHasBeenCreated = true;
		}
		return top.onPrepareOptionsMenu(menu);
	}

	public void show(SubActivity subActivity, int direction) {
		final SubActivity previousTop = getPreviousTop();
		if (subActivity == getTop()) {
			if (previousTop != null) {
				ViewUtils.hideSoftInput(previousTop.getRoot().getWindowToken(), mActivity);
			}

			subActivity.onResume();
			mCallback.onSwitchSubActivities(previousTop, subActivity, direction);

			mOptionMenuHasBeenCreated = false;
			mTopHasBeenShown = true;
		}

		if (mActivityToBeReplaced != null) {
			mActivityToBeReplaced.onDestroy();
			mActivityToBeReplaced = null;
		} else if (previousTop != null) {
			previousTop.onPause();
		}
	}
}
