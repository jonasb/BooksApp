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

package com.wigwamlabs.booksapp.db;

import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.wigwamlabs.booksapp.Debug;

public class ExtendedSQLiteCursor extends SQLiteCursor implements ExtendedCursor {
	private int mActiveState = ExtendedCursor.STATE_ACTIVE;
	private int mDebugId;

	public ExtendedSQLiteCursor(SQLiteDatabase db, SQLiteCursorDriver driver, String editTable,
			SQLiteQuery query) {
		super(db, driver, editTable, query);
		if (Debug.LOG_CURSOR) {
			mDebugId = (int) (Math.random() * 1000);
			Log.d(Debug.TAG, "cursor (" + mDebugId + ") constructed: " + query.toString());
		} else {
			mDebugId = 0;
		}
	}

	@Override
	public void close() {
		super.close();

		mActiveState = STATE_CLOSED;
		if (Debug.LOG_CURSOR)
			Log.d(Debug.TAG, "cursor (" + mDebugId + ") closed");
	}

	@Override
	public void deactivate() {
		super.deactivate();

		mActiveState = ExtendedCursor.STATE_DEACTIVATED;
		if (Debug.LOG_CURSOR)
			Log.d(Debug.TAG, "cursor (" + mDebugId + ") deactivated");
	}

	@Override
	public int getActiveState() {
		return mActiveState;
	}

	@Override
	public boolean requery() {
		final boolean res = super.requery();

		if (Debug.LOG_CURSOR) {
			Log.d(Debug.TAG, "cursor (" + mDebugId + ") requeried"
					+ (mActiveState != STATE_ACTIVE ? " -> active" : ""));
		}
		mActiveState = ExtendedCursor.STATE_ACTIVE;

		return res;
	}

	@Override
	public void setSoftDeactivated(boolean softDeactivated) {
		if (mActiveState == STATE_ACTIVE && softDeactivated) {
			mActiveState = ExtendedCursor.STATE_SOFT_DEACTIVATED;
			if (Debug.LOG_CURSOR)
				Log.d(Debug.TAG, "cursor (" + mDebugId + ") soft deactivated");
		}
		if (mActiveState == STATE_SOFT_DEACTIVATED && !softDeactivated) {
			mActiveState = ExtendedCursor.STATE_ACTIVE;
			if (Debug.LOG_CURSOR)
				Log.d(Debug.TAG, "cursor (" + mDebugId + ") active");
		}
	}
}
