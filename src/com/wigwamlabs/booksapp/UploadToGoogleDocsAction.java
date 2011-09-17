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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.SimpleProgressDialog;
import com.wigwamlabs.googleclient.GoogleAccountAction;
import com.wigwamlabs.googleclient.TokenInvalidException;
import com.wigwamlabs.googledocs.GoogleDocsService;

public class UploadToGoogleDocsAction extends GoogleAccountAction {
	public class UploadToGoogleDocsTask extends ExportBookTask {
		private final String mAuthToken;

		/* package */UploadToGoogleDocsTask(Activity activity, DatabaseAdapter db,
				AsyncTaskListener<Long, Long, Integer> listener, String authToken)
				throws FileNotFoundException {
			super(activity, db, listener);
			mAuthToken = authToken;
		}

		@Override
		protected void onFileFinished(File file) {
			uploadFile(getContext(), file, mAuthToken);
		}
	}

	public static final String TAG = UploadToGoogleDocsAction.class.getName();
	private final DatabaseAdapter mDb;
	/* package */File mFile;
	private final Long[] mIds;
	private final AsyncTaskListener<Long, Long, Integer> mListener;

	public UploadToGoogleDocsAction(final Activity activity, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener, Long[] ids) {
		super(activity, AUTH_TOKEN_TYPE_GOOGLE_DOCS);
		mDb = db;
		mListener = listener;
		mIds = ids;
	}

	@Override
	public void onAuthenticated(String authToken) {
		final Activity activity = getActivity();
		if (mFile != null) {
			uploadFile(activity, mFile, authToken);
		} else {
			try {
				new UploadToGoogleDocsTask(activity, mDb, mListener, authToken).execute(mIds);
			} catch (final FileNotFoundException e) {
				Toast.makeText(activity, R.string.export_failed_toast, Toast.LENGTH_LONG).show();
			}
		}
	}

	/* package */void uploadFile(final Context context, final File file, final String authToken) {
		final Handler handler = new Handler();
		final Dialog dialog = SimpleProgressDialog.show(context, null);
		Toast.makeText(context, R.string.upload_starting_toast, Toast.LENGTH_LONG).show();

		new Thread() {
			@Override
			public void run() {
				int toastMessage;
				try {
					// upload file
					GoogleDocsService.uploadFile(context, file, "text/csv", authToken);

					toastMessage = R.string.upload_finished_toast;
				} catch (final IOException e) {
					Log.e(TAG, "Exception", e);
					toastMessage = R.string.upload_failed_toast;
				} catch (final TokenInvalidException e) {
					if (mFile == null) { // first time
						// try again with new token
						mFile = file;
						handler.post(new Runnable() {
							@Override
							public void run() {
								dialog.hide();
								invalidateTokenAndRestart(authToken);
							}
						});
						return;
					}
					// give up if token is invalid twice in a row
					Log.e(TAG, "Exception", e);
					toastMessage = R.string.upload_failed_toast;
				}
				// clean up and notify user
				file.delete();
				final int toastMsg = toastMessage;
				handler.post(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
						dialog.hide();
					}
				});
			}
		}.start();
	}
}
