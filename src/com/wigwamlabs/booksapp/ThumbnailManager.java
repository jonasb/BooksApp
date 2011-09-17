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
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Point;
import android.os.FileObserver;
import android.os.Handler;
import android.view.Display;
import android.view.WindowManager;

import com.wigwamlabs.booksapp.ImageDownloadTask.Callback;
import com.wigwamlabs.util.BitmapUtils;
import com.wigwamlabs.util.WeakListIterator;

public class ThumbnailManager {
	private static class DownloadAndStoreThumbnailTask implements Callback {
		private final long mBookId;
		private final Context mContext;
		private final boolean mSmall;
		private final CharSequence mThumbnailUrl;

		public DownloadAndStoreThumbnailTask(Context context, long bookId,
				CharSequence thumbnailUrl, boolean small) {
			mContext = context;
			mBookId = bookId;
			mThumbnailUrl = thumbnailUrl;
			mSmall = small;

			new ImageDownloadTask(getThumbnailSize(context, mSmall), this).execute(mThumbnailUrl);
		}

		@Override
		public void onImageDownloaded(CharSequence url, Bitmap image) {
			if (image == null)
				return;

			if (mSmall)
				storeThumbnail(mContext, mBookId, image, mSmall);
			else
				storeLargeThumbnail(mContext, mBookId, image, false);
		}

		@Override
		public void onImageDownloadFinished() {
		}
	}
	public interface Observer {
		public void onThumbnailChanged(long bookId, boolean small, File file);
	}

	private static FileObserver mFileObserver;
	private static List<WeakReference<Observer>> mObservers = new ArrayList<WeakReference<Observer>>();
	/* package */static final String SUFFIX_LARGE = "_l.png";
	/* package */static final String SUFFIX_SMALL = "_s.png";
	private static final String THUMBNAIL_DIR = "thumbnails";

	public static void addThumbnailObserver(Context context, WeakReference<Observer> observer) {
		final List<WeakReference<Observer>> observers = mObservers;
		if (mFileObserver == null) {
			final Handler handler = new Handler();
			final File pathToObserve = getThumbnailDir(context);
			final int mask = FileObserver.CLOSE_WRITE | FileObserver.DELETE | FileObserver.MOVED_TO
					| FileObserver.MOVED_FROM;
			mFileObserver = new FileObserver(pathToObserve.getPath(), mask) {
				@Override
				public void onEvent(int event, final String path) {
					final boolean small;
					final String idString;
					if (path.endsWith(SUFFIX_SMALL)) {
						small = true;
						idString = path.subSequence(0, path.length() - SUFFIX_SMALL.length())
								.toString();
					} else if (path.endsWith(SUFFIX_LARGE)) {
						small = false;
						idString = path.subSequence(0, path.length() - SUFFIX_LARGE.length())
								.toString();
					} else
						return;

					long id = -1;
					try {
						id = Long.parseLong(idString);
					} catch (final Exception e) {
						// do nothing
					}
					final long bookId = id;

					// occurs in file observer thread, post to main
					handler.post(new Runnable() {
						@Override
						public void run() {
							for (final Observer o : WeakListIterator.from(observers)) {
								o.onThumbnailChanged(bookId, small, new File(pathToObserve, path));
							}
							if (observers.size() == 0)
								stopWatching();
						}
					});
				}
			};
		}

		observers.add(observer);
		mFileObserver.startWatching();
	}

	public static void deleteAll(Context context) {
		final File dir = getThumbnailDir(context);
		for (final File thumbnail : dir.listFiles()) {
			thumbnail.delete();
		}
		dir.delete();
	}

	public static void deleteThumbnails(Context context, long bookId) {
		final File smallThumbnail = getThumbnail(context, bookId, true);
		if (smallThumbnail.exists()) {
			smallThumbnail.delete();
		}

		final File largeThumbnail = getThumbnail(context, bookId, false);
		if (largeThumbnail.exists()) {
			largeThumbnail.delete();
		}
	}

	public static File getThumbnail(Context context, long bookId, boolean small) {
		return new File(getThumbnailDir(context), bookId + (small ? SUFFIX_SMALL : SUFFIX_LARGE));
	}

	private static File getThumbnailDir(Context context) {
		// getDir() creates dir if not exists
		return context.getDir(THUMBNAIL_DIR, Context.MODE_PRIVATE);
	}

	public static Point getThumbnailSize(Context context, boolean small) {
		if (small) {
			final Resources res = context.getResources();
			return new Point(res.getDimensionPixelSize(R.dimen.small_thumbnail_width),
					res.getDimensionPixelSize(R.dimen.small_thumbnail_height));
		}
		final WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		final Display display = windowManager.getDefaultDisplay();
		return new Point(display.getWidth(), display.getHeight());
	}

	public static void removeThumbnailObserver(Observer observer) {
		final Iterator<WeakReference<Observer>> it = mObservers.iterator();
		while (it.hasNext()) {
			final WeakReference<Observer> o = it.next();
			if (o.get() == observer)
				it.remove();
		}
	}

	public static void save(Context context, long bookId, Bitmap smallThumbnail,
			String thumbnailSmallUrl, Bitmap largeThumbnail, String thumbnailLargeUrl) {
		if (smallThumbnail != null) {
			storeThumbnail(context, bookId, smallThumbnail, true);
		} else if (thumbnailSmallUrl != null) {
			new DownloadAndStoreThumbnailTask(context, bookId, thumbnailSmallUrl, true);
		}

		if (largeThumbnail != null) {
			storeLargeThumbnail(context, bookId, largeThumbnail, false);
		} else if (thumbnailLargeUrl != null) {
			new DownloadAndStoreThumbnailTask(context, bookId, thumbnailLargeUrl, false);
		}
	}

	public static void storeLargeThumbnail(Context context, long bookId, Bitmap thumbnail,
			boolean forceCreateSmall) {
		storeThumbnail(context, bookId, thumbnail, false);

		final boolean createSmall;
		if (forceCreateSmall)
			createSmall = true;
		else {
			final File f = getThumbnail(context, bookId, true);
			createSmall = (!f.exists());
		}

		if (createSmall) {
			final Point size = getThumbnailSize(context, true);
			final Bitmap smallThumbnail = BitmapUtils.createScaledBitmap(thumbnail, size.x, size.y,
					false);
			storeThumbnail(context, bookId, smallThumbnail, true);
		}
	}

	/* package */static void storeThumbnail(Context context, long bookId, Bitmap thumbnail,
			boolean small) {
		try {
			final File f = getThumbnail(context, bookId, small);
			final FileOutputStream out = new FileOutputStream(f);
			thumbnail.compress(CompressFormat.PNG, 100, out);
			out.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
