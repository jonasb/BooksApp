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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.wigwamlabs.util.ImageViewUtils;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;
import com.wigwamlabs.util.Triple;

public class ImageDownloadCollection implements ImageDownloadTask.Callback,
		ThumbnailManager.Observer {
	private static final Bitmap ERROR_IMAGE = Bitmap.createBitmap(1, 1, Config.ALPHA_8);
	private ImageDownloadTask mDownloadTask;
	private final HashMap<CharSequence, Bitmap> mImages;
	private final ArrayList<Triple<CharSequence, ImageView, WhenNoImage>> mImageViews = new ArrayList<Triple<CharSequence, ImageView, WhenNoImage>>();
	private final Point mTargetSize;

	public ImageDownloadCollection(Point targetSize, int maxImages, int maxBytes) {
		mTargetSize = targetSize;
		mImages = new BitmapCacheHashMap<CharSequence>(maxImages, maxBytes);
	}

	public void attachFile(File file, ImageView imageView, WhenNoImage whenNoImage,
			boolean skipIfNotInCache) {
		attachImageView(file == null ? null : file.toURI().toString(), imageView, whenNoImage,
				skipIfNotInCache);
	}

	private void attachImageView(CharSequence url, ImageView imageView, WhenNoImage whenNoImage,
			boolean skipIfNotInCache) {
		if (imageView == null)
			return;

		removeImageView(imageView);

		if (url == null) {
			ImageViewUtils.updateImageView(imageView, (Drawable) null, whenNoImage);
			return;
		}

		final Bitmap image;
		synchronized (mImages) {
			image = mImages.get(url);
		}
		if (image == null && !skipIfNotInCache) { // download not completed
			mImageViews.add(Triple.create(url, imageView, whenNoImage));
			prefetchUrl(url);
		}

		ImageViewUtils.updateImageView(imageView, (image == ERROR_IMAGE ? null : image),
				whenNoImage);
	}

	public void attachUrl(CharSequence url, ImageView imageView, WhenNoImage whenNoImage,
			boolean skipIfNotInCache) {
		attachImageView(url, imageView, whenNoImage, skipIfNotInCache);
	}

	public Bitmap getImage(String url) {
		final Bitmap image;
		synchronized (mImages) {
			image = mImages.get(url);
		}
		if (image == ERROR_IMAGE)
			return null;
		return image;
	}

	@Override
	public void onImageDownloaded(CharSequence url, Bitmap image) {
		// ensure we don't try to download again, use place holder if image is
		// null
		synchronized (mImages) {
			mImages.put(url, image == null ? ERROR_IMAGE : image);
		}

		// attach image to interested image views
		for (int i = mImageViews.size() - 1; i >= 0; i--) {
			final Triple<CharSequence, ImageView, WhenNoImage> iv = mImageViews.get(i);
			final ImageView imageView = iv.second;
			if (url.equals(iv.first)) {
				ImageViewUtils.updateImageView(imageView, image, iv.third);
				mImageViews.remove(i);
			}
		}
	}

	@Override
	public void onImageDownloadFinished() {
		mDownloadTask = null;
		startDownload();
	}

	@Override
	public void onThumbnailChanged(long bookId, boolean small, File file) {
		final CharSequence url = file.toURI().toString();
		synchronized (mImages) {
			mImages.remove(url);
		}
	}

	public void prefetchUrl(CharSequence url) {
		synchronized (mImages) {
			if (url == null || mImages.containsKey(url))
				return;

			mImages.put(url, null);
		}

		if (mDownloadTask == null) {
			startDownload();
		}
	}

	private void removeImageView(ImageView imageView) {
		final int count = mImageViews.size();
		for (int i = 0; i < count; i++) {
			if (mImageViews.get(i).second == imageView) {
				mImageViews.remove(i);
				return; // there are never duplicates
			}
		}
	}

	private void startDownload() {
		assert (mDownloadTask == null);
		final ArrayList<CharSequence> urls = new ArrayList<CharSequence>();
		synchronized (mImages) {
			for (final Entry<CharSequence, Bitmap> e : mImages.entrySet()) {
				if (e.getValue() == null) {
					urls.add(e.getKey());
				}
			}
		}

		if (!urls.isEmpty()) {
			mDownloadTask = new ImageDownloadTask(mTargetSize,
					new WeakReference<ImageDownloadTask.Callback>(this));
			mDownloadTask.execute(urls.toArray(new CharSequence[urls.size()]));
		}
	}
}
