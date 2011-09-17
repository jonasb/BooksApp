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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.api.client.apache.ApacheHttpTransport;
import com.wigwamlabs.booksapp.SubActivityManager.ShowDirection;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.googlebooks.GoogleBookSearch;
import com.wigwamlabs.util.Compatibility;

public class HomeActivity extends Activity implements SubActivityManager.Callback {
	public static final String ACTION_EXPIRED_LOANS = "expired_loans";
	private GoogleBookSearch mBookSearch;
	private DatabaseAdapter mDb;
	private ViewGroup mFrameLayout;
	private ImageDownloadCollection mLargeThumbnails;
	private Animation mMoveInDownAnimation;
	private Animation mMoveInLeftAnimation;
	private Animation mMoveInRightAnimation;
	private Animation mMoveInUpAnimation;
	private Animation mMoveOutDownAnimation;
	private Animation mMoveOutLeftAnimation;
	private Animation mMoveOutRightAnimation;
	private Animation mMoveOutUpAnimation;
	private ImageDownloadCollection mSmallThumbnails;
	private SubActivityManager mSubActivityManager;
	private Animation mSwitchContentInAnimation;
	private Animation mSwitchContentOutAnimation;

	private void handleIntent(Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			SearchSuggestionsProvider.saveQuery(this, query);

			mSubActivityManager.create(SearchSubActivity.class).prepareSearch(query).show();
		} else if (ACTION_EXPIRED_LOANS.equals(action)) {
			mSubActivityManager.create(BookListSubActivity.class).prepareWithExpiredLoans().show();
		} else if (Intent.ACTION_VIEW.equals(action)) {
			final Uri uri = intent.getData();
			if (uri != null) {
				mSubActivityManager.create(ImportBookSubActivity.class)
						.prepare(getContentResolver(), uri).show();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mSubActivityManager.onActivityResult(requestCode, resultCode, data))
			return;

		if (requestCode == BookScanActivity.SCAN_BOOKS_REQUEST) {
			if (resultCode == RESULT_OK) {
				final Bundle extras = data.getExtras();
				if (extras != null) {
					final String[] isbns = extras.getStringArray(BookScanActivity.ISBNS_KEY);
					if (isbns != null && isbns.length > 0) {
						mSubActivityManager.create(IsbnSearchSubActivity.class).prepare().show();
					}
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (mSubActivityManager.onBackPressed())
			return;

		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.enableStrictMode();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home_main);

		// revert to standard background since mMenuList has the same
		// background. app_background is only in the theme to get it displayed
		// as soon as possible
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));

		mDb = ((BooksApp) getApplicationContext()).getDb();
		mSmallThumbnails = CacheConfig.createLocalThumbnailCacheSmall(this);
		mLargeThumbnails = CacheConfig.createLocalThumbnailCacheLarge(this);

		mSubActivityManager = new SubActivityManager(this, this);

		mFrameLayout = (ViewGroup) findViewById(R.id.frame_layout);
		mMoveOutLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.move_out_left);
		mMoveInLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.move_in_left);
		mMoveOutRightAnimation = AnimationUtils.loadAnimation(this, R.anim.move_out_right);
		mMoveInRightAnimation = AnimationUtils.loadAnimation(this, R.anim.move_in_right);
		mMoveOutUpAnimation = AnimationUtils.loadAnimation(this, R.anim.move_out_up);
		mMoveInUpAnimation = AnimationUtils.loadAnimation(this, R.anim.move_in_up);
		mMoveOutDownAnimation = AnimationUtils.loadAnimation(this, R.anim.move_out_down);
		mMoveInDownAnimation = AnimationUtils.loadAnimation(this, R.anim.move_in_down);
		mSwitchContentOutAnimation = AnimationUtils.loadAnimation(this, R.anim.switch_content_out);
		mSwitchContentInAnimation = AnimationUtils.loadAnimation(this, R.anim.switch_content_in);

		mSubActivityManager.create(MenuSubActivity.class).prepare().show(ShowDirection.NONE);

		mBookSearch = new GoogleBookSearch(this);
		HttpTransportCache.install(ApacheHttpTransport.INSTANCE, this);

		handleIntent(getIntent());
	}

	@Override
	public <T extends SubActivity> Object onCreateSubActivity(Class<T> klass) {
		if (klass == AboutSubActivity.class) {
			return new AboutSubActivity(this, mSubActivityManager);
		}
		if (klass == BookListSubActivity.class) {
			return new BookListSubActivity(this, mSubActivityManager, mDb, mSmallThumbnails);
		}
		if (klass == BookDetailsSubActivity.class) {
			return new BookDetailsSubActivity(this, mSubActivityManager, mDb, mSmallThumbnails,
					mLargeThumbnails);
		}
		if (klass == BookGroupSubActivity.class) {
			return new BookGroupSubActivity(this, mSubActivityManager, mDb);
		}
		if (klass == EditBookSubActivity.class) {
			return new EditBookSubActivity(this, mSubActivityManager, mDb, mSmallThumbnails,
					mLargeThumbnails);
		}
		if (klass == GoogleBookDetailsSubActivity.class) {
			return new GoogleBookDetailsSubActivity(this, mSubActivityManager, mDb, mBookSearch);
		}
		if (klass == ImportBookSubActivity.class) {
			return new ImportBookSubActivity(this, mSubActivityManager, mDb, mSmallThumbnails);
		}
		if (klass == IsbnBookDetailSubActivity.class) {
			return new IsbnBookDetailSubActivity(this, mSubActivityManager, mDb);
		}
		if (klass == IsbnSearchSubActivity.class) {
			return new IsbnSearchSubActivity(this, mSubActivityManager, mDb);
		}
		if (klass == LoanListSubActivity.class) {
			return new LoanListSubActivity(this, mSubActivityManager, mDb, mSmallThumbnails);
		}
		if (klass == MenuSubActivity.class) {
			return new MenuSubActivity(this, mSubActivityManager, mDb);
		}
		if (klass == SearchSubActivity.class) {
			return new SearchSubActivity(this, mSubActivityManager, mDb, mBookSearch,
					mSmallThumbnails);
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		mSubActivityManager.onDestroy();

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Compatibility.SDK_INT < Build.VERSION_CODES.ECLAIR && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			// workaround for pre-Eclair versions
			onBackPressed();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mSubActivityManager.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSubActivityManager.onActivityPause();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return mSubActivityManager.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mSubActivityManager.onActivityResume();
	}

	@Override
	public void onSwitchSubActivities(SubActivity previousTop, SubActivity newTop, int direction) {
		final View previousView = (previousTop != null ? previousTop.getRoot() : null);
		if (previousView != null) {
			if (direction == ShowDirection.LEFT)
				previousView.startAnimation(mMoveOutLeftAnimation);
			else if (direction == ShowDirection.RIGHT)
				previousView.startAnimation(mMoveOutRightAnimation);
			else if (direction == ShowDirection.UP)
				previousView.startAnimation(mMoveOutUpAnimation);
			else if (direction == ShowDirection.DOWN)
				previousView.startAnimation(mMoveOutDownAnimation);
			else if (direction == ShowDirection.SWITCH_CONTENT)
				previousView.startAnimation(mSwitchContentOutAnimation);
			mFrameLayout.removeView(previousView);
		}
		final View newView = (newTop != null ? newTop.getRoot() : null);
		if (newView != null) {
			if (direction == ShowDirection.LEFT)
				newView.startAnimation(mMoveInLeftAnimation);
			else if (direction == ShowDirection.RIGHT)
				newView.startAnimation(mMoveInRightAnimation);
			else if (direction == ShowDirection.UP)
				newView.startAnimation(mMoveInUpAnimation);
			else if (direction == ShowDirection.DOWN)
				newView.startAnimation(mMoveInDownAnimation);
			else if (direction == ShowDirection.SWITCH_CONTENT)
				newView.startAnimation(mSwitchContentInAnimation);
			mFrameLayout.addView(newView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
	}
}