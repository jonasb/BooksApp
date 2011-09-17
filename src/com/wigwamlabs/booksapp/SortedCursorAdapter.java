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
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.SectionIndexer;

public abstract class SortedCursorAdapter extends CursorAdapter implements SectionIndexer {
	private final AlphabetIndexer mIndexer;

	public SortedCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor, true);
		mIndexer = null;
	}

	public SortedCursorAdapter(Context context, Cursor cursor, int sortedColumn) {
		super(context, cursor, true);
		mIndexer = new AlphabetIndexer(cursor, sortedColumn, "#ABCDEFGHIJKLMNOPQRSTUVWXYZ");
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (mIndexer != null)
			mIndexer.setCursor(cursor);
	}

	@Override
	public int getPositionForSection(int section) {
		if (mIndexer == null)
			return -1;
		return mIndexer.getPositionForSection(section);
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mIndexer == null)
			return -1;
		return mIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		if (mIndexer == null)
			return null;
		return mIndexer.getSections();
	}
}
