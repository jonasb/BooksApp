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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.wigwamlabs.util.ViewUtils;

public class TitleBar extends LinearLayout {
	private View mCenter;
	private FilterEditText mFilter;
	private final View mHome;
	private final View mSearch;
	private SubActivityManager mSubActivityManager;

	public TitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		final Resources res = getResources();
		final int padding = ViewUtils.dipToPixels(res.getDisplayMetrics(), 3);
		setPadding(padding, padding, padding, padding);
		LayoutInflater.from(context).inflate(R.layout.titlebar, this);

		if (getBackground() == null) {
			setBackgroundColor(res.getColor(R.color.titlebar));
		}

		mHome = findViewById(R.id.home);
		mHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onHome();
			}
		});

		mCenter = findViewById(R.id.center);

		mSearch = findViewById(R.id.search);
		mSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onSearch();
			}
		});
	}

	public void addLeftContent(View view) {
		final LayoutParams homeParams = (LayoutParams) mHome.getLayoutParams();
		final LayoutParams params = new LayoutParams(homeParams);

		params.width = params.height = getResources().getDimensionPixelSize(R.dimen.titlebar_size);

		ViewUtils.addBeforeSibling(view, mCenter, params);
	}

	public void addRightContent(View view) {
		final LayoutParams homeParams = (LayoutParams) mHome.getLayoutParams();
		final LayoutParams params = new LayoutParams(homeParams);

		params.width = params.height = getResources().getDimensionPixelSize(R.dimen.titlebar_size);

		ViewUtils.addAfterSibling(view, mCenter, params);
	}

	public void enableFilter(FilterEditText.Callback filterCallback) {
		mFilter = new FilterEditText(getContext(), filterCallback);
		mFilter.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				onSearch();
				return true;
			}
		});
		setCenterContent(mFilter);
	}

	protected void onHome() {
		mSubActivityManager.goBackToBottomActivity();
	}

	protected void onSearch() {
		String query = null;
		if (mFilter != null) {
			query = mFilter.getText().toString().trim();
		}

		final Activity activity = mSubActivityManager.getActivity();
		if (query == null || query.length() == 0) {
			activity.onSearchRequested();
		} else {
			final Intent intent = new Intent(Intent.ACTION_SEARCH);
			intent.putExtra(SearchManager.QUERY, query);
			intent.setClass(getContext(), activity.getClass());
			activity.startActivity(intent);
		}
	}

	private void setCenterContent(View view) {
		ViewUtils.replaceView(mCenter, view, true);
		mCenter = view;
	}

	/* package */void setSubActivityManager(SubActivityManager manager) {
		mSubActivityManager = manager;
	}
}
