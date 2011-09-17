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

package com.wigwamlabs.googleclient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.wigwamlabs.booksapp.ContextMenu;
import com.wigwamlabs.booksapp.R;

public abstract class GoogleAccountAction {
	private static final String ACCOUNT_TYPE = "com.google";
	public static final String AUTH_TOKEN_TYPE_GOOGLE_DOCS = "writely";
	/* package */static final String TAG = GoogleAccountAction.class.getName();
	/* package */Account mAccount;
	/* package */final Activity mActivity;
	/* package */final String mAuthTokenType;

	public GoogleAccountAction(final Activity activity, final String authTokenType) {
		mActivity = activity;
		mAuthTokenType = authTokenType;
	}

	protected Activity getActivity() {
		return mActivity;
	}

	private void getAuthToken(final AccountManager manager) {
		final Handler handler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					final Bundle bundle = manager.getAuthToken(mAccount, mAuthTokenType, true,
							null, null).getResult();
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								final Intent intent = bundle
										.getParcelable(AccountManager.KEY_INTENT);
								int flags = intent.getFlags();
								flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
								intent.setFlags(flags);
								mActivity.startActivityForResult(intent,
										R.id.google_account_request_code);
							} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								final String authToken = bundle
										.getString(AccountManager.KEY_AUTHTOKEN);
								onAuthenticated(authToken);
							}
						}
					});
				} catch (final Exception e) {
					Log.e(TAG, "Exception", e);
				}
			}
		}.start();
	}

	public void invalidateTokenAndRestart(String authToken) {
		final AccountManager manager = AccountManager.get(mActivity);

		manager.invalidateAuthToken(ACCOUNT_TYPE, authToken);

		getAuthToken(manager);
	}

	/* package */void onAccountSelected(final AccountManager manager, final Account account) {
		mAccount = account;
		getAuthToken(manager);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			getAuthToken(AccountManager.get(mActivity));
		} else {
			selectAccount();
		}
	}

	public abstract void onAuthenticated(String authToken);

	private void selectAccount() {
		final AccountManager manager = AccountManager.get(mActivity);
		final Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
		if (accounts.length == 0) {
			Toast.makeText(mActivity, R.string.export_google_docs_no_accounts_toast,
					Toast.LENGTH_SHORT).show();
			return;
		}

		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int pos) {
				onAccountSelected(manager, accounts[pos]);
			}
		};
		final ContextMenu menu = new ContextMenu(mActivity, listener);
		for (final Account a : accounts) {
			menu.add(a.name);
		}
		menu.show();
	}

	public void start() {
		selectAccount();
	}
}
