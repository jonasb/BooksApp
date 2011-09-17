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

import java.lang.ref.WeakReference;

import android.database.DataSetObserver;
import android.widget.Adapter;

public class WeakDataSetObserverProxy extends DataSetObserver {
	public static void register(Adapter adapter, WeakReference<DataSetObserver> observer) {
		new WeakDataSetObserverProxy(adapter, observer);
	}

	private final Adapter mAdapter;
	private final WeakReference<DataSetObserver> mObserver;

	private WeakDataSetObserverProxy(Adapter adapter, WeakReference<DataSetObserver> observer) {
		super();
		mAdapter = adapter;
		mObserver = observer;
		mAdapter.registerDataSetObserver(this);
	}

	@Override
	public void onChanged() {
		final DataSetObserver o = mObserver.get();
		if (o != null) {
			o.onChanged();
		} else {
			mAdapter.unregisterDataSetObserver(this);
		}
	}

	@Override
	public void onInvalidated() {
		final DataSetObserver o = mObserver.get();
		if (o != null) {
			o.onInvalidated();
		} else {
			mAdapter.unregisterDataSetObserver(this);
		}
	}
}
