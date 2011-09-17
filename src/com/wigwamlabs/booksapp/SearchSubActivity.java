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

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.wigwamlabs.booksapp.db.BookListCursor;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.googlebooks.GoogleBookSearch.FeedSearch;
import com.wigwamlabs.util.ViewUtils;

public class SearchSubActivity extends SubActivity implements PreviousNextProvider,
		SecondaryTabView.Callback {
	private static final int LIST_CURSOR = 0;
	private static final int TAB_LOCAL = 0;
	private static final int TAB_WEB = 1;
	private final GoogleBookSearch mBookSearch;
	private final ListViewCheckButton mCheckButton;
	private final DatabaseAdapter mDb;
	private BookListAdapter mLocalAdapter;
	private final BookListView mLocalList;
	private final ImageDownloadCollection mLocalThumbnails;
	private int mOptionsMenuCreatedForTab = -1;
	private final Animation mSwitchContentInAnimation;
	private final Animation mSwitchContentOutAnimation;
	private final SecondaryTabView mTabView;
	private GoogleSearchAdapter mWebAdapter;
	private final GoogleSearchListView mWebList;

	public SearchSubActivity(Context context, SubActivityManager manager, DatabaseAdapter db,
			GoogleBookSearch bookSearch, ImageDownloadCollection localThumbnails) {
		super(context, manager);
		mDb = db;
		mBookSearch = bookSearch;
		mLocalThumbnails = localThumbnails;

		setContentView(R.layout.search_main);

		final TitleBar titleBar = getTitleBar();
		mCheckButton = new ListViewCheckButton(context);
		titleBar.addLeftContent(mCheckButton);

		final ProgressBar webLoading = new ProgressBar(context, null,
				android.R.attr.progressBarStyleLargeInverse);
		titleBar.addRightContent(webLoading);

		mLocalList = (BookListView) findViewById(R.id.local_list);
		mLocalList.init(mDb, manager, mCheckButton);

		final CountView localCount = (CountView) findViewById(R.id.local_book_count);
		mLocalList.setCountView(localCount);

		mWebList = (GoogleSearchListView) findViewById(R.id.web_list);
		mWebList.init(mDb, manager, mCheckButton, mBookSearch);

		final CountView webCount = (CountView) findViewById(R.id.web_book_count);
		mWebList.setCountView(webCount);

		mTabView = (SecondaryTabView) findViewById(R.id.tab_view);
		setupTabs(webLoading);

		mSwitchContentOutAnimation = AnimationUtils.loadAnimation(context,
				R.anim.switch_content_out);
		mSwitchContentInAnimation = AnimationUtils.loadAnimation(context, R.anim.switch_content_in);
	}

	private void doPrepare(BookListCursor bookList, FeedSearch webSearch) {
		final Context context = getContext();
		setCursor(bookList, LIST_CURSOR);
		mLocalAdapter = (bookList == null ? null : new BookListAdapter(context, bookList,
				mLocalThumbnails));

		final ImageDownloadCollection webSmallThumbnails = CacheConfig
				.createWebThumbnailCacheSmall(context);
		final ImageDownloadCollection webLargeThumbnails = CacheConfig
				.createWebThumbnailCacheLarge(context);
		mWebList.setWebThumbnails(webSmallThumbnails, webLargeThumbnails);
		mWebAdapter = new GoogleSearchAdapter(context, mBookSearch, mDb, webSearch,
				mLocalThumbnails, webSmallThumbnails);

		if (mLocalAdapter != null)
			mLocalList.prepare(mLocalAdapter, null);
		mWebList.setAdapter(mWebAdapter);

		// show web tab if there're no local matches
		final int tab = (mLocalAdapter != null && mLocalAdapter.getCount() > 0 ? TAB_LOCAL
				: TAB_WEB);
		mTabView.setActiveTab(tab, true);
		mTabView.setVisibility(mLocalAdapter != null ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mLocalList.onActivityResult(requestCode, resultCode, data))
			return true;
		return super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		int itemId = 0;
		final int activeTab = mTabView.getActiveTab();
		if (activeTab == TAB_LOCAL) {
			itemId = mLocalList.onCreateOptionsMenu(menu, itemId);
		} else {
			itemId = mWebList.onCreateOptionsMenu(menu, itemId);
		}
		itemId = mCheckButton.onCreateOptionsMenu(menu, itemId);

		mOptionsMenuCreatedForTab = activeTab;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int currentItemId = item.getItemId();

		int itemId = 0;

		if (mTabView.getActiveTab() == TAB_LOCAL) {
			itemId = mLocalList.onOptionsItemSelected(currentItemId, itemId);
			if (itemId > currentItemId)
				return true;
		} else {
			itemId = mWebList.onOptionsItemSelected(currentItemId, itemId);
			if (itemId > currentItemId)
				return true;
		}

		itemId = mCheckButton.onOptionsItemSelected(currentItemId, itemId);
		if (itemId > currentItemId)
			return true;

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final int activeTab = mTabView.getActiveTab();
		if (mOptionsMenuCreatedForTab != activeTab) {
			menu.clear();
			onCreateOptionsMenu(menu);
		}

		if (activeTab == TAB_LOCAL) {
			int itemId = 0;
			itemId = mLocalList.onPrepareOptionsMenu(menu, itemId);
			itemId = mCheckButton.onPrepareOptionsMenu(menu, itemId);
		} else {
			int itemId = 0;
			itemId = mWebList.onPrepareOptionsMenu(menu, itemId);
			itemId = mCheckButton.onPrepareOptionsMenu(menu, itemId);
		}

		return true;
	}

	@Override
	public void onTabSelected(int newIndex, int previousIndex) {
		if (newIndex == previousIndex)
			return;

		final View newContent = (newIndex == TAB_LOCAL ? mLocalList : mWebList);

		if (previousIndex >= 0) {
			final View previousContent = (previousIndex == TAB_LOCAL ? mLocalList : mWebList);
			previousContent.startAnimation(mSwitchContentOutAnimation);
			newContent.startAnimation(mSwitchContentInAnimation);
		}

		final ViewGroup parent = (ViewGroup) newContent.getParent();
		ViewUtils.makeSingleChildVisible(parent, newContent);

		// update list check button
		if (newIndex == TAB_LOCAL)
			mCheckButton.prepare(mLocalList.getList(), mLocalAdapter);
		else
			mCheckButton.prepare(mWebList, mWebAdapter);
	}

	@Override
	public void openPreviousNext(boolean previous, SubActivity activityToReplace) {
		if (mTabView.getActiveTab() == TAB_LOCAL)
			mLocalList.openPreviousNext(previous, activityToReplace);
		else
			mWebList.openPreviousNext(previous, activityToReplace);
	}

	public SubActivity prepareRelatedSearch(boolean isIsbn, String id) {
		doPrepare(
				null,
				isIsbn ? mBookSearch.searchRelatedByIsbn(id) : mBookSearch
						.searchRelatedByGoogleId(id));
		return this;
	}

	public SearchSubActivity prepareSearch(String query) {
		BookListCursor bookList;
		try {
			bookList = BookListCursor.searchAny(mDb, query);
		} catch (final Exception e) {
			bookList = null;
		}
		doPrepare(bookList, mBookSearch.searchByAny(query));
		return this;
	}

	public SearchSubActivity prepareWithAuthor(long authorId, String name) {
		doPrepare(
				BookListCursor.fetchByAuthor(mDb, BookListCursor.title_normalized_index, authorId),
				mBookSearch.searchByAuthor(name));
		return this;
	}

	public SearchSubActivity prepareWithPublisher(long publisherId, String name) {
		doPrepare(BookListCursor.fetchByPublisher(mDb, BookListCursor.title_normalized_index,
				publisherId), mBookSearch.searchByPublisher(name));
		return this;
	}

	public SearchSubActivity prepareWithSubject(long subjectId, String name) {
		doPrepare(BookListCursor.fetchBySubject(mDb, BookListCursor.title_normalized_index,
				subjectId), mBookSearch.searchBySubject(name));
		return this;
	}

	@Override
	public void setCallback(WeakReference<Callback> callback, boolean notifyDirectly) {
		if (mTabView.getActiveTab() == TAB_LOCAL)
			mLocalList.setCallback(callback, notifyDirectly);
		else
			mWebList.setCallback(callback, notifyDirectly);
	}

	private void setupTabs(ProgressBar webLoading) {
		mWebList.setProgressBar(webLoading);

		mTabView.setCallback(this);
	}
}