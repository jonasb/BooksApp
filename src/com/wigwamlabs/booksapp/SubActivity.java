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

import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.ExtendedCursor;
import com.wigwamlabs.util.ViewUtils;

public abstract class SubActivity implements CursorManager {
	private final Context mContext;
	private final Vector<ExtendedCursor> mCursors = new Vector<ExtendedCursor>(1);
	private final SubActivityManager mManager;
	private final Resources mResources;
	private View mRootView;
	private TitleBar mTitleBar;

	protected SubActivity(Context context, SubActivityManager manager) {
		mContext = context;
		mResources = context.getResources();
		mManager = manager;
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + "()");
	}

	protected void close() {
		mManager.close(this);
	}

	protected View findViewById(int id) {
		return mRootView.findViewById(id);
	}

	protected SubActivity getChildSubActivity() {
		return mManager.getChild(this);
	}

	protected Context getContext() {
		return mContext;
	}

	@Override
	public ExtendedCursor getCursor(int cursorIndex) {
		if (cursorIndex >= mCursors.size())
			return null;
		return mCursors.get(cursorIndex);
	}

	public SubActivityManager getManager() {
		return mManager;
	}

	public MenuInflater getMenuInflater() {
		return mManager.getActivity().getMenuInflater();
	}

	protected SubActivity getParentSubActivity() {
		return mManager.getParent(this);
	}

	public Resources getResources() {
		return mResources;
	}

	public View getRoot() {
		return mRootView;
	}

	public TitleBar getTitleBar() {
		return mTitleBar;
	}

	/**
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 * @return <code>true</code> if handled
	 */
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		return false;
	}

	public boolean onBackPressed() {
		close();
		return true;
	}

	/**
	 * @param menu
	 */
	public void onCreateOptionsMenu(Menu menu) {
	}

	public void onDestroy() {
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + ".onDestroy()");
		for (final ExtendedCursor c : mCursors) {
			if (c == null)
				continue;

			c.close();
		}
		mCursors.clear();
	}

	public void onHostPause() {
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + ".onHostPause()");
		for (final ExtendedCursor c : mCursors) {
			if (c == null)
				continue;

			final int state = c.getActiveState();
			if (state == ExtendedCursor.STATE_ACTIVE
					|| state == ExtendedCursor.STATE_SOFT_DEACTIVATED)
				c.deactivate();
		}
	}

	public void onHostResume() {
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + ".onHostResume()");

		if (!mManager.isTop(this))
			return;

		for (final ExtendedCursor c : mCursors) {
			if (c == null)
				continue;

			final int state = c.getActiveState();
			if (state == ExtendedCursor.STATE_DEACTIVATED)
				c.requery();
			else if (state == ExtendedCursor.STATE_SOFT_DEACTIVATED)
				c.setSoftDeactivated(false);
		}
	}

	/**
	 * @param item
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

	public void onPause() {
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + ".onPause()");
		for (final ExtendedCursor c : mCursors) {
			if (c == null)
				continue;

			if (c.getActiveState() == ExtendedCursor.STATE_ACTIVE)
				c.setSoftDeactivated(true);
		}
	}

	/**
	 * @param menu
	 */
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	public void onResume() {
		if (Debug.LOG_LIFECYCLE)
			Log.d(Debug.TAG, getClass().getSimpleName() + ".onResume()");
		for (final ExtendedCursor c : mCursors) {
			if (c == null)
				continue;

			final int activeState = c.getActiveState();
			if (activeState == ExtendedCursor.STATE_DEACTIVATED)
				c.requery();
			else if (activeState == ExtendedCursor.STATE_SOFT_DEACTIVATED)
				c.setSoftDeactivated(false);
		}
	}

	protected void setContentView(int resource) {
		mRootView = LayoutInflater.from(mContext).inflate(resource, null);

		mTitleBar = ViewUtils.findDescendantByClass(TitleBar.class, mRootView);
		if (mTitleBar != null)
			mTitleBar.setSubActivityManager(mManager);
	}

	@Override
	public void setCursor(ExtendedCursor cursor, int cursorIndex) {
		setCursor(cursor, cursorIndex, true);
	}

	@Override
	public void setCursor(ExtendedCursor cursor, int cursorIndex, boolean closeOldCursor) {
		if (cursorIndex >= mCursors.size()) {
			mCursors.setSize(cursorIndex + 1);
		}
		final ExtendedCursor oldCursor = mCursors.get(cursorIndex);
		if (oldCursor != null && closeOldCursor) {
			oldCursor.close();
		}

		mCursors.set(cursorIndex, cursor);
	}

	protected void show() {
		mManager.show(this, ShowDirection.LEFT);
	}

	public void show(int direction) {
		mManager.show(this, direction);
	}

	public void startActivityForResult(Intent intent, int requestCode) {
		mManager.getActivity().startActivityForResult(intent, requestCode);
	}
}
