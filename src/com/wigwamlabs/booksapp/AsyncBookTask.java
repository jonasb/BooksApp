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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.ui.SimpleProgressDialog;

public abstract class AsyncBookTask<Param> extends AsyncTask<Param, Long, Integer> {
	private static final String TAG = AsyncBookTask.class.getName();
	private boolean mAborted = false;
	private final Context mContext;
	private final DatabaseAdapter mDb;
	private final AsyncTaskListener<Param, Long, Integer> mListener;
	private Param mNextParam;
	private Dialog mProgressDialog;

	public AsyncBookTask(Context context, DatabaseAdapter db,
			AsyncTaskListener<Param, Long, Integer> listener) {
		mContext = context;
		mDb = db;
		mListener = listener;
	}

	protected void abort() {
		cancel(false);
		mAborted = true;
	}

	abstract protected long doInBackground(Context context, DatabaseAdapter db, Param param)
			throws Exception;

	@Override
	protected Integer doInBackground(Param... params) {
		int result = 0;
		for (int i = 0; i < params.length; i++) {
			final Param param = params[i];
			if (param == null)
				break;
			final Param nextParam = (i >= params.length - 1 ? null : params[i + 1]);

			// TODO share transaction for all?
			try {
				final long id = doInBackground(mContext, mDb, param);
				mNextParam = nextParam;
				publishProgress(Long.valueOf(id));
				result++;
			} catch (final Exception e) {
				Log.e(TAG, "Exception", e);
				abort();
			}

			if (isCancelled())
				break;
		}

		return Integer.valueOf(result);
	}

	protected CharSequence getAbortToastMessage(Resources res) {
		return res.getString(R.string.book_task_abort_toast);
	}

	protected Context getContext() {
		return mContext;
	}

	protected DatabaseAdapter getDb() {
		return mDb;
	}

	protected abstract CharSequence getToastMessage(Resources res, int bookCount);

	@Override
	protected void onCancelled() {
		mProgressDialog.dismiss();
		final Resources res = mContext.getResources();
		final CharSequence msg = mAborted ? getAbortToastMessage(res) : res
				.getString(R.string.book_task_cancel_toast);
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onPostExecute(Integer result) {
		mProgressDialog.dismiss();

		final CharSequence msg = getToastMessage(mContext.getResources(), result.intValue());
		if (msg != null) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}

		if (mListener != null)
			mListener.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		mProgressDialog = SimpleProgressDialog.show(mContext,
				new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						AsyncBookTask.this.cancel(false);
					}
				});
	}

	@Override
	protected void onProgressUpdate(Long... values) {
		if (mListener != null) {
			mListener.onProgressUpdate(values);

			// mNextParam is not thread-safe
			final Param next = mNextParam;
			if (next != null)
				mListener.onNextParam(next);
		}
	}
}