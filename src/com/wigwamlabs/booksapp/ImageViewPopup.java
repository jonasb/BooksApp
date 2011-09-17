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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;

import com.wigwamlabs.util.ImageViewUtils;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;

public class ImageViewPopup implements OnClickListener {
	public interface Callback {
		void onAssignHighResolutionImage(ImageView imageView);
	}

	private final Callback mCallback;
	private final ImageView mImageView;

	public ImageViewPopup(ImageView imageView, Callback callback) {
		mImageView = imageView;
		mCallback = callback;

		mImageView.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		final Context context = mImageView.getContext();

		// prepare image view
		final ImageView popup = new ImageView(context);

		mCallback.onAssignHighResolutionImage(popup);

		Drawable drawable = popup.getDrawable();
		if (drawable == null || drawable instanceof NinePatchDrawable) {
			Drawable smallImage = mImageView.getDrawable();
			if (smallImage instanceof NinePatchDrawable) // is place holder?
				smallImage = null;

			ImageViewUtils.updateImageView(popup, smallImage,
					WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder));
			drawable = popup.getDrawable();
		}

		// prepare dialog
		final Dialog dialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		final int contentWidth;
		final int contentHeight;
		if (drawable instanceof NinePatchDrawable) { // is place holder?
			contentWidth = mImageView.getWidth();
			contentHeight = mImageView.getHeight();
		} else {
			contentWidth = drawable.getIntrinsicWidth();
			contentHeight = drawable.getIntrinsicHeight();
		}

		final Display display = dialog.getWindow().getWindowManager().getDefaultDisplay();
		final float scale = LayoutUtilities.getScaleToFit(contentWidth, contentHeight,
				display.getWidth(), display.getHeight());
		final LayoutParams params = new LayoutParams((int) (scale * contentWidth),
				(int) (scale * contentHeight));

		dialog.addContentView(popup, params);
		dialog.setCanceledOnTouchOutside(true);

		// show dialog
		dialog.show();
	}
}
