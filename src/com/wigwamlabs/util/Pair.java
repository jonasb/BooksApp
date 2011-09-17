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

public class Pair<F, S> {
	public static <A, B> Pair<A, B> create(A first, B second) {
		return new Pair<A, B>(first, second);
	}

	public final F first;
	public final S second;

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	private boolean componentsEqual(final Object a, final Object b) {
		if (a == null) {
			return b == null;
		} else {
			return a.equals(b);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof Pair<?, ?>))
			return false;

		final Pair<?, ?> other = (Pair<?, ?>) o;
		return componentsEqual(first, other.first) && componentsEqual(second, other.second);
	}

	@Override
	public int hashCode() {
		if (first == null && second == null)
			return super.hashCode();

		int hash = 17;
		if (first != null)
			hash = 31 * hash + first.hashCode();
		if (second != null)
			hash = 31 * hash + second.hashCode();
		return hash;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
}
