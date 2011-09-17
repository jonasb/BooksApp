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
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SecondaryTabView extends LinearLayout {
	interface Callback {
		void onTabSelected(int newIndex, int previousIndex);
	}

	private Callback mCallback;
	private final OnClickListener mTabClickListener;

	public SecondaryTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(HORIZONTAL);
		setBaselineAligned(true);

		final Resources res = getResources();
		setBackgroundColor(res.getColor(R.color.titlebar));

		// create tab click listener
		mTabClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int index = indexOfChild(v);
				setActiveTab(index, true);
			}
		};
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		final LayoutParams params = super.generateDefaultLayoutParams();
		params.width = 0;
		params.height = ViewGroup.LayoutParams.FILL_PARENT;
		params.weight = 1.f;
		return params;
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return generateDefaultLayoutParams();
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	public int getActiveTab() {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.isSelected())
				return i;
		}
		return -1;
	}

	@Override
	protected int getSuggestedMinimumHeight() {
		// make sure the background drawable doesn't add to the height
		return 0;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			child.setOnClickListener(mTabClickListener);

			// final int childBackground;
			// if (i == 0)
			// childBackground = R.drawable.buttonbar_left;
			// else if (i == count - 1)
			// childBackground = R.drawable.buttonbar_right;
			// else
			// childBackground = R.drawable.buttonbar_middle;
			child.setBackgroundResource(R.drawable.buttonbar);
		}
	}

	// TODO use
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		final int id = getId();
		assert (id != NO_ID);
		final Integer previousActiveTab = (savedInstanceState == null ? null : Integer
				.valueOf(savedInstanceState.getInt(Integer.toString(id))));
		final int activeTab = (previousActiveTab == null || previousActiveTab.intValue() < 0 ? 0
				: previousActiveTab.intValue());
		setActiveTab(activeTab, true);
	}

	public void onSaveInstanceState(Bundle outState) {
		final int id = getId();
		assert (id != NO_ID);
		outState.putInt(Integer.toString(id), getActiveTab());
	}

	public void setActiveTab(final int index, final boolean notifyListener) {
		int previousIndex = -1;
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.isSelected())
				previousIndex = i;
			child.setSelected(i == index);
		}

		if (notifyListener && mCallback != null) {
			mCallback.onTabSelected(index, previousIndex);
		}
	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}
}
