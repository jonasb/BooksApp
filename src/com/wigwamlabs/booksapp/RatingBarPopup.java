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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

public class RatingBarPopup implements OnRatingBarChangeListener, OnTouchListener,
		OnDismissListener {
	public interface Callback {
		public void onRatingPopupClosed(float oldRating, float newRating);
	}

	private final Callback mCallback;
	private float mNewRating;
	private final float mOldRating;
	private final RatingBar mRatingBar;

	public RatingBarPopup(RatingBar ratingBar, Callback callback) {
		mRatingBar = ratingBar;
		mCallback = callback;
		mOldRating = ratingBar.getRating();
		mNewRating = mOldRating;
		ratingBar.setOnTouchListener(this);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mCallback.onRatingPopupClosed(mOldRating, mNewRating);
	}

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
		mRatingBar.setRating(rating);
		mNewRating = rating;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN)
			return true;

		if (action == MotionEvent.ACTION_UP) {
			final Context context = mRatingBar.getContext();
			final RatingBar popupRatingBar = new RatingBar(context);
			popupRatingBar.setNumStars(mRatingBar.getNumStars());
			popupRatingBar.setStepSize(mRatingBar.getStepSize());
			popupRatingBar.setRating(mRatingBar.getRating());
			popupRatingBar.setOnRatingBarChangeListener(this);

			final AlertDialog dialog = new AlertDialog.Builder(context).setView(popupRatingBar)
					.create();
			popupRatingBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOnDismissListener(this);
			dialog.show();
			return true;
		}
		return false;
	}
}
