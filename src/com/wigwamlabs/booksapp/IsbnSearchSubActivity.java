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

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;

import com.wigwamlabs.booksapp.IsbnSearchService.LocalBinder;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class IsbnSearchSubActivity extends SubActivity implements PreviousNextProvider,
		ServiceConnection {
	private LocalBinder mBinder;
	private final ListViewCheckButton mCheckButton;
	private final IsbnSearchListView mList;
	private final IsbnSearchAdapter mListAdapter;

	public IsbnSearchSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db) {
		super(context, manager);

		setContentView(R.layout.isbn_search_main);

		final TitleBar titleBar = getTitleBar();
		mCheckButton = new ListViewCheckButton(context);
		titleBar.addLeftContent(mCheckButton);

		mList = (IsbnSearchListView) findViewById(R.id.search_result_list);
		mList.init(db, manager, mCheckButton);
		mListAdapter = new IsbnSearchAdapter(context, db, true);
		mList.setAdapter(mListAdapter);
		mCheckButton.prepare(mList, mListAdapter);

		// TODO set service from prepare
		context.bindService(new Intent(context, IsbnSearchService.class), this,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mList.onActivityResult(requestCode, resultCode, data))
			return true;
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		int itemId = 0;
		itemId = mList.onCreateOptionsMenu(menu, itemId);
		itemId = mCheckButton.onCreateOptionsMenu(menu, itemId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mBinder.reset();
		getContext().unbindService(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int currentItemId = item.getItemId();

		int itemId = 0;

		itemId = mList.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		itemId = mCheckButton.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		int itemId = 0;
		itemId = mList.onPrepareOptionsMenu(menu, itemId);
		itemId = mCheckButton.onPrepareOptionsMenu(menu, itemId);
		return true;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mBinder = (LocalBinder) service;
		mListAdapter.setService(mBinder);
		final ImageDownloadCollection webLargeThumbnails = CacheConfig
				.createWebThumbnailCacheLarge(getContext());
		mList.setWebThumbnails(mBinder.getThumbnails(), webLargeThumbnails);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mBinder = null;
	}

	@Override
	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		mList.openPreviousNext(previous, activityToReplace);
	}

	public IsbnSearchSubActivity prepare() {
		return this;
	}

	@Override
	public void setCallback(WeakReference<Callback> callback, boolean notifyDirectly) {
		mList.setCallback(callback, notifyDirectly);
	}
}
