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

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Point;

public final class CacheConfig {
	private static ImageDownloadCollection create(Context context, Point targetSize, int maxImages,
			int maxBytes) {
		final ImageDownloadCollection thumbnails = new ImageDownloadCollection(targetSize,
				maxImages, maxBytes);
		ThumbnailManager.addThumbnailObserver(context,
				new WeakReference<ThumbnailManager.Observer>(thumbnails));
		return thumbnails;

	}

	public static ImageDownloadCollection createLocalThumbnailCacheLarge(Context context) {
		return create(context, null, 10, 1024 * 1024);
	}

	public static ImageDownloadCollection createLocalThumbnailCacheSmall(Context context) {
		return create(context, null, 5000, 2 * 1024 * 1024);
	}

	public static ImageDownloadCollection createWebThumbnailCacheLarge(Context context) {
		return create(context, ThumbnailManager.getThumbnailSize(context, false), 10, 1024 * 1024);
	}

	public static ImageDownloadCollection createWebThumbnailCacheSmall(Context context) {
		return create(context, ThumbnailManager.getThumbnailSize(context, true), 5000,
				2 * 1024 * 1024);
	}

	private CacheConfig() {
	}
}
