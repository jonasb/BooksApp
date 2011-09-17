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

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;

@ReportsCrashes(formKey = "dHk0R0lmWllNNmlUWlZwTGpUN3hmdFE6MQ")
public class BooksApp extends Application {
	private DatabaseAdapter mDb;

	public DatabaseAdapter getDb() {
		return mDb;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Debug.enableStrictMode();
		if (!Debug.DISABLE_CRASH_UPLOADS)
			ACRA.init(this);

		mDb = new DatabaseAdapter();
		mDb.open(this);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		mDb.close();
		mDb = null;
	}
}
