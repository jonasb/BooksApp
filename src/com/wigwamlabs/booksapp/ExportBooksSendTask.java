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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;

public class ExportBooksSendTask extends ExportBookTask {
	public static ExportBooksSendTask createOrNull(Activity activity, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener) {
		try {
			return new ExportBooksSendTask(activity, db, listener);
		} catch (final FileNotFoundException e) {
			Toast.makeText(activity, R.string.export_failed_toast, Toast.LENGTH_LONG).show();
			return null;
		}
	}

	private final Activity mActivity;

	private ExportBooksSendTask(Activity activity, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener) throws FileNotFoundException {
		super(activity, db, listener);
		mActivity = activity;
	}

	@Override
	protected void onFileFinished(File file) {
		final Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("text/csv");
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "BooksApp export");
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

		mActivity.startActivity(Intent.createChooser(sendIntent, getContext().getResources()
				.getString(R.string.export_send)));
	}
}
