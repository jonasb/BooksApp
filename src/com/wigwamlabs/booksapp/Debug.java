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

import org.acra.ErrorReporter;

public class Debug {
	public static final boolean DISABLE_CRASH_UPLOADS = false;
	public static final boolean FORCE_PRO = false;
	private static final boolean LOG = false;
	public static final boolean LOG_CACHE = LOG;
	public static final boolean LOG_CURSOR = LOG;
	public static final boolean LOG_LIFECYCLE = LOG;
	public static final boolean LOG_SQL = LOG;
	public static final String TAG = "BooksApp";

	public static void enableStrictMode() {
		// final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		// if (sdkVersion >= Build.VERSION_CODES.GINGERBREAD) {
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectDiskReads()
		// .detectDiskWrites().detectNetwork().penaltyLog().build());
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		// .penaltyLog().penaltyDeath().build());
		// }
	}

	public static void reportException(String message, Throwable e) {
		if (DISABLE_CRASH_UPLOADS)
			return;

		try {
			throw new RuntimeException(message, e);
		} catch (final Exception messageException) {
			ErrorReporter.getInstance().handleException(messageException);
		}
	}
}
