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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public final class ImageViewUtils {
	/* package */static final class LazyLoadDonutAndBeyond {
		public static Drawable createBitmapDrawable(Resources res, Bitmap image) {
			return new BitmapDrawable(res, image);
		}
	}

	/* package */static final class LazyLoadPreDonut {
		public static Drawable createBitmapDrawable(Bitmap image) {
			return new BitmapDrawable(image);
		}
	}

	public static class WhenNoImage {
		public static final WhenNoImage DO_NOTHING = new WhenNoImage(/* STRATEGY_DO_NOTHING */0, 0);
		public static final WhenNoImage HIDE = new WhenNoImage(/* STRATEGY_HIDE */1, 0);
		public static final int STRATEGY_DO_NOTHING = 0;
		public static final int STRATEGY_HIDE = 1;
		public static final int STRATEGY_USE_PLACEHOLDER = 2;

		public static WhenNoImage usePlaceholder(int placeholderId) {
			return new WhenNoImage(STRATEGY_USE_PLACEHOLDER, placeholderId);
		}

		public final int placeholderId;
		public final int strategy;

		private WhenNoImage(int strategy, int placeholderId) {
			this.strategy = strategy;
			this.placeholderId = placeholderId;
		}
	}

	public static void updateImageView(ImageView imageView, Bitmap image, WhenNoImage whenNoImage) {
		final Drawable d;
		if (image == null)
			d = null;
		else {
			if (Compatibility.SDK_INT >= Build.VERSION_CODES.DONUT)
				d = LazyLoadDonutAndBeyond.createBitmapDrawable(imageView.getResources(), image);
			else
				d = LazyLoadPreDonut.createBitmapDrawable(image);
		}

		updateImageView(imageView, d, whenNoImage);
	}

	public static void updateImageView(ImageView imageView, Drawable drawable,
			WhenNoImage whenNoImage) {
		switch (whenNoImage.strategy) {
		case ImageViewUtils.WhenNoImage.STRATEGY_DO_NOTHING:
			if (drawable != null) {
				imageView.setImageDrawable(drawable);
			}
			break;
		case ImageViewUtils.WhenNoImage.STRATEGY_HIDE:
			imageView.setImageDrawable(drawable);
			imageView.setVisibility(drawable != null ? View.VISIBLE : View.GONE);
			break;
		case ImageViewUtils.WhenNoImage.STRATEGY_USE_PLACEHOLDER:
			if (drawable != null) {
				imageView.setImageDrawable(drawable);
				imageView.setScaleType(ScaleType.FIT_CENTER);
			} else {
				imageView.setImageResource(whenNoImage.placeholderId);
				imageView.setScaleType(ScaleType.FIT_XY);
			}
			break;
		}
	}

	private ImageViewUtils() {
	}
}
