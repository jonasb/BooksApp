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

import android.database.AbstractCursor;

import com.wigwamlabs.booksapp.db.ExtendedCursor;

public class StubExtendedCursor extends AbstractCursor implements ExtendedCursor {
	private int mActiveState = ExtendedCursor.STATE_ACTIVE;

	public StubExtendedCursor() {
	}

	@Override
	public void close() {
		super.close();

		mActiveState = STATE_CLOSED;
	}

	@Override
	public void deactivate() {
		super.deactivate();

		mActiveState = ExtendedCursor.STATE_DEACTIVATED;
	}

	@Override
	public int getActiveState() {
		return mActiveState;
	}

	@Override
	public String[] getColumnNames() {
		return null;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public double getDouble(int column) {
		return 0;
	}

	@Override
	public float getFloat(int column) {
		return 0;
	}

	@Override
	public int getInt(int column) {
		return 0;
	}

	@Override
	public long getLong(int column) {
		return 0;
	}

	@Override
	public short getShort(int column) {
		return 0;
	}

	@Override
	public String getString(int column) {
		return null;
	}

	@Override
	public boolean isNull(int column) {
		return true;
	}

	@Override
	public boolean requery() {
		mActiveState = ExtendedCursor.STATE_ACTIVE;

		return super.requery();
	}

	@Override
	public void setSoftDeactivated(boolean softDeactivated) {
		if (mActiveState == STATE_ACTIVE && softDeactivated) {
			mActiveState = ExtendedCursor.STATE_SOFT_DEACTIVATED;
		}
		if (mActiveState == STATE_SOFT_DEACTIVATED && !softDeactivated) {
			mActiveState = ExtendedCursor.STATE_ACTIVE;
		}
	}
}