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

package com.wigwamlabs.util;

import java.io.File;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

public final class StorageCompatibility {
	/* package */static final class LazyLoadFroyoAndBeyond {
		/* package */static File getExternalCacheDir(Context context) {
			return context.getExternalCacheDir();
		}
	}

	/* package */static final class LazyLoadPreFroyo {
		/* package */static File getExternalCacheDir(Context context) {
			try {
				final File root = Environment.getExternalStorageDirectory();
				if (root == null || !root.exists())
					return null;
				final File cacheDir = new File(root, "Android/data/" + context.getPackageName()
						+ "/cache/");
				if (!cacheDir.exists() && !cacheDir.mkdirs())
					return null;
				if (cacheDir.isDirectory())
					return cacheDir;
			} catch (final Exception e) {
				// do nothing
			}
			return null;
		}
	}

	public static File getExternalCacheDir(Context context) {
		if (Compatibility.SDK_INT >= Build.VERSION_CODES.FROYO)
			return LazyLoadFroyoAndBeyond.getExternalCacheDir(context);
		else
			return LazyLoadPreFroyo.getExternalCacheDir(context);
	}

	private StorageCompatibility() {
	}
}
