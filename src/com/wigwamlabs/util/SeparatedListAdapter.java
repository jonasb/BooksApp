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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;

import com.wigwamlabs.booksapp.R;

public class SeparatedListAdapter extends BaseAdapter {
	private final static int TYPE_SECTION_HEADER = 0;
	private final ArrayAdapter<String> mHeaders;
	private final DataSetObserver mSectionAdapterObserver;
	private final List<Adapter> mSections = new ArrayList<Adapter>();

	public SeparatedListAdapter(Context context) {
		mHeaders = new ArrayAdapter<String>(context, R.layout.list_section);
		mSectionAdapterObserver = new DataSetObserver() {
			@Override
			public void onChanged() {
				SeparatedListAdapter.this.notifyDataSetChanged();
			}

			@Override
			public void onInvalidated() {
				SeparatedListAdapter.this.notifyDataSetInvalidated();
			}
		};
	}

	public void addSection(String section, Adapter adapter) {
		mHeaders.add(section);
		mSections.add(adapter);
		adapter.registerDataSetObserver(mSectionAdapterObserver);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getCount() {
		// total together all sections, plus one for each section header
		int total = 0;
		for (final Adapter adapter : mSections) {
			final int count = adapter.getCount();
			if (count > 0)
				total += count + 1;
		}
		return total;
	}

	@Override
	public Object getItem(int position) {
		final int sectionCount = mSections.size();
		for (int section = 0; section < sectionCount; section++) {
			final Adapter adapter = mSections.get(section);
			final int count = adapter.getCount() + 1;
			if (count == 1)
				continue;

			// check if position inside this section
			if (position == 0)
				return mHeaders.getItem(section);
			if (position < count)
				return adapter.getItem(position - 1);

			// otherwise jump into next section
			position -= count;
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		for (final Adapter adapter : mSections) {
			final int count = adapter.getCount() + 1;
			if (count == 1)
				continue;

			// check if position inside this section
			if (position == 0)
				return -1;
			if (position < count)
				return adapter.getItemId(position - 1);

			// otherwise jump into next section
			position -= count;
		}
		return -1;
	}

	@Override
	public int getItemViewType(int position) {
		int type = 1;
		for (final Adapter adapter : mSections) {
			final int count = adapter.getCount() + 1;
			if (count > 1) {
				// check if position inside this section
				if (position == 0)
					return TYPE_SECTION_HEADER;
				if (position < count)
					return type + adapter.getItemViewType(position - 1);

				// otherwise jump into next section
				position -= count;
			}
			type += adapter.getViewTypeCount();
		}
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int sectionCount = mSections.size();
		for (int section = 0; section < sectionCount; section++) {
			final Adapter adapter = mSections.get(section);
			final int count = adapter.getCount() + 1;
			if (count == 1)
				continue;

			// check if position inside this section
			if (position == 0)
				return mHeaders.getView(section, convertView, parent);
			if (position < count)
				return adapter.getView(position - 1, convertView, parent);

			// otherwise jump into next section
			position -= count;
		}
		return null;
	}

	@Override
	public int getViewTypeCount() {
		// assume that headers count as one, then total all sections
		int total = 1;
		for (final Adapter adapter : mSections) {
			total += adapter.getViewTypeCount();
		}
		return total;
	}

	@Override
	public boolean isEnabled(int position) {
		return (getItemViewType(position) != TYPE_SECTION_HEADER);
	}
}
