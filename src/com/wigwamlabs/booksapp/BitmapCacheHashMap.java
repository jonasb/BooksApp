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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

public class BitmapCacheHashMap<Key> extends LinkedHashMap<Key, Bitmap> {
	private static final long serialVersionUID = -7495095304391759144L;
	private static final String TAG = "BitmapCacheHashMap";
	private static final int TINY_BITMAP_SIZE = 128;
	private final int mLimitBytes;
	private final int mLimitSize;
	int mSizeBytes;

	public BitmapCacheHashMap(int limitSize, int limitBytes) {
		super(100, 0.75f, true);
		mLimitSize = limitSize;
		mLimitBytes = limitBytes;
	}

	@Override
	public void clear() {
		mSizeBytes = 0;
		super.clear();
	}

	@Override
	public Bitmap put(Key key, Bitmap value) {
		final Bitmap oldValue = super.put(key, value);
		if (oldValue != null) {
			mSizeBytes -= oldValue.getRowBytes() * oldValue.getHeight();
		}
		if (value != null) {
			mSizeBytes += value.getRowBytes() * value.getHeight();
			removeOldEntries();
		}
		return oldValue;
	}

	@Override
	public Bitmap remove(Object key) {
		final Bitmap oldValue = super.remove(key);
		if (oldValue != null) {
			final int bitmapSize = oldValue.getRowBytes() * oldValue.getHeight();
			if (Debug.LOG_CACHE)
				Log.d(TAG, "remove old value: " + mSizeBytes + " - " + bitmapSize + " = "
						+ (mSizeBytes - bitmapSize));
			mSizeBytes -= bitmapSize;
		}
		return oldValue;
	}

	private void removeOldEntries() {
		// remove non-tiny bitmaps
		if (mSizeBytes > mLimitBytes) {
			final Iterator<Map.Entry<Key, Bitmap>> it = entrySet().iterator();
			while (mSizeBytes > mLimitBytes) {
				if (!it.hasNext())
					break;
				final Bitmap bitmap = it.next().getValue();
				final int bitmapSize = (bitmap != null ? bitmap.getRowBytes() * bitmap.getHeight()
						: 0);
				if (bitmapSize > TINY_BITMAP_SIZE)
					it.remove();
			}
		}

		// remove entries no matter which size
		int entriesToRemove = size() - mLimitSize;
		if (entriesToRemove > 0) {
			final Iterator<Map.Entry<Key, Bitmap>> it = entrySet().iterator();
			while (entriesToRemove > 0) {
				if (!it.hasNext())
					break;
				it.next();
				it.remove();
				entriesToRemove--;
			}
		}
	}
}
