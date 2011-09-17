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

import android.content.Context;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;

public final class ViewUtils {
	public static void addAfterSibling(View newView, View sibling, LayoutParams params) {
		final ViewGroup parent = (ViewGroup) sibling.getParent();
		final int index = parent.indexOfChild(sibling);
		if (params == null)
			parent.addView(newView, index + 1);
		else
			parent.addView(newView, index + 1, params);
	}

	public static void addBeforeSibling(View newView, View sibling, LayoutParams params) {
		final ViewGroup parent = (ViewGroup) sibling.getParent();
		final int index = parent.indexOfChild(sibling);
		parent.addView(newView, index, params);
	}

	/**
	 * DisplayMetrics is normally retrieved using
	 * <code>getResources().getDisplayMetrics()</code>.
	 */
	public static int dipToPixels(final DisplayMetrics displayMetrics, int dip) {
		return (int) (dip * displayMetrics.density + 0.5f);
	}

	public static <T extends View> T findChildByClass(Class<T> klass, ViewGroup parent) {
		final int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = parent.getChildAt(i);
			if (klass.isInstance(child)) {
				@SuppressWarnings("unchecked")
				final T c = (T) child;
				return c;
			}
		}
		return null;
	}

	public static <T extends View> T findDescendantByClass(Class<T> klass, View parent) {
		if (parent instanceof ViewGroup)
			return findDescendantByClass(klass, (ViewGroup) parent);
		return null;
	}

	public static <T extends View> T findDescendantByClass(Class<T> klass, ViewGroup parent) {
		final int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = parent.getChildAt(i);
			if (klass.isInstance(child)) {
				@SuppressWarnings("unchecked")
				final T c = (T) child;
				return c;
			}
			if (child instanceof ViewGroup) {
				final T descendant = findDescendantByClass(klass, (ViewGroup) child);
				if (descendant != null) {
					return descendant;
				}
			}
		}
		return null;
	}

	public static void hideSoftInput(IBinder windowToken, Context context) {
		final InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static void makeSingleChildVisible(final ViewGroup parent, final View visibleChild) {
		final int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = parent.getChildAt(i);
			child.setVisibility(child == visibleChild ? View.VISIBLE : View.GONE);
		}
	}

	public static void replaceView(View oldChild, View newChild, boolean transferLayoutParams) {
		final ViewGroup parent = oldChild.getParent() instanceof ViewGroup ? (ViewGroup) oldChild
				.getParent() : null;
		final int index = parent.indexOfChild(oldChild);

		parent.removeViewInLayout(oldChild);

		final LayoutParams layoutParams = (transferLayoutParams ? oldChild.getLayoutParams() : null);
		if (layoutParams != null)
			parent.addView(newChild, index, layoutParams);
		else
			parent.addView(newChild, index);
	}
}
