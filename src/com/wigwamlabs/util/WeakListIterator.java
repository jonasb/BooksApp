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
import java.util.Iterator;
import java.util.List;

public class WeakListIterator<T> implements Iterable<T>, Iterator<T> {
	public static <A> WeakListIterator<A> from(List<WeakReference<A>> list) {
		return new WeakListIterator<A>(list);
	}

	private int mIndex = -1;
	private final List<WeakReference<T>> mList;

	public WeakListIterator(List<WeakReference<T>> list) {
		mList = list;
	}

	private T getItem(int index) {
		while (index < mList.size()) {
			final T item = mList.get(index).get();
			if (item != null) {
				return item;
			}
			mList.remove(index);
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		return (getItem(mIndex + 1) != null);
	}

	@Override
	public Iterator<T> iterator() {
		// this instance can only be used once, otherwise we need to return a
		// new iterator here
		assert (mIndex == -1);
		return this;
	}

	@Override
	public T next() {
		mIndex++;
		return getItem(mIndex);
	}

	@Override
	public void remove() {
		if (mIndex > 0 && mIndex < mList.size()) {
			mList.remove(mIndex);
		}
	}
}
