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
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

public class DeleteFileAction {
	private final File mDir;
	private int mFileCount = -1;
	private final FilenameFilter mFilenameFilter;
	private final int mLimit;
	private final int mReduceTo;

	public DeleteFileAction(File dir, int limit, int reduceTo, FilenameFilter filenameFilter) {
		mDir = dir;
		mLimit = limit;
		mReduceTo = reduceTo;
		mFilenameFilter = filenameFilter;
	}

	public boolean execute() {
		if (mFileCount >= 0 && mFileCount <= mLimit)
			return false;

		final File[] files = mDir.listFiles(mFilenameFilter);
		mFileCount = files.length;
		if (mFileCount <= mLimit)
			return false;

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File a, File b) {
				final long aDate = a.lastModified();
				final long bDate = b.lastModified();
				if (aDate == bDate)
					return 0;
				return aDate < bDate ? -1 : 1;
			}
		});

		for (int i = mReduceTo; i < files.length; i++) {
			final File file = files[i];
			file.delete();
		}
		mFileCount = mReduceTo;

		return true;
	}

	public void onFileAdded(String path) {
		if (mFileCount < 0)
			return;

		final String dir = mDir.getAbsolutePath();
		if (path.startsWith(dir)) {
			if (mFilenameFilter.accept(mDir, path.substring(dir.length() + 1))) {
				mFileCount++;
			}
		}
	}
}
