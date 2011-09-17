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

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ListAdapter;
import android.widget.ListView;

class EmptyListDrawable extends Drawable {

	public static class Observer extends DataSetObserver {
		private ListAdapter mAdapter;
		private final Context mContext;
		private EmptyListDrawable mDrawable;
		private final int mDrawableId;
		private final ListView mListView;
		private final int mPrimaryTextId;
		private final int mSecondaryTextId;

		public Observer(Context context, ListView listView, int drawableId, int primaryTextId) {
			this(context, listView, drawableId, primaryTextId, 0);
		}

		public Observer(Context context, ListView listView, int drawableId, int primaryTextId,
				int secondaryTextId) {
			mContext = context;
			mListView = listView;
			mDrawableId = drawableId;
			mPrimaryTextId = primaryTextId;
			mSecondaryTextId = secondaryTextId;
		}

		@Override
		public void onChanged() {
			if (mListView.getCount() == 0) {
				if (mDrawable == null)
					mDrawable = new EmptyListDrawable(mContext, mDrawableId, mPrimaryTextId,
							mSecondaryTextId);
				mListView.setBackgroundDrawable(mDrawable);
			} else {
				mListView.setBackgroundDrawable(null);
			}
		}

		public void setAdapter(ListAdapter adapter) {
			if (mAdapter != null) {
				mAdapter.unregisterDataSetObserver(this);
			}
			mAdapter = adapter;
			if (mAdapter != null) {
				mAdapter.registerDataSetObserver(this);
			}
			onChanged();
		}
	}

	private final Drawable mDrawable;
	private final String mPrimaryText;
	private final Paint mPrimaryTextPaint = new Paint();
	private final String mSecondaryText;
	private final Paint mSecondaryTextPaint;
	private final PointF mTextOrigin = new PointF();

	public EmptyListDrawable(Context context, int drawableId, int primaryTextId) {
		this(context.getResources(), drawableId, context.getResources().getString(primaryTextId),
				null);
	}

	public EmptyListDrawable(Context context, int drawableId, int primaryTextId, int secondaryTextId) {
		this(context.getResources(), drawableId, context.getResources().getString(primaryTextId),
				secondaryTextId != 0 ? context.getResources().getString(secondaryTextId) : null);
	}

	public EmptyListDrawable(Context context, int drawableId, String primaryText) {
		this(context.getResources(), drawableId, primaryText, null);
	}

	private EmptyListDrawable(Resources res, int drawableId, String primaryText,
			String secondaryText) {
		mPrimaryText = primaryText;
		mSecondaryText = secondaryText;
		mDrawable = res.getDrawable(drawableId);

		mPrimaryTextPaint.setTextAlign(Align.CENTER);
		mPrimaryTextPaint.setTextSize(res.getDimensionPixelSize(R.dimen.empty_list_text_size));
		mPrimaryTextPaint.setColor(Color.WHITE);
		mPrimaryTextPaint.setAntiAlias(true);

		if (secondaryText != null) {
			mSecondaryTextPaint = new Paint(mPrimaryTextPaint);
			mSecondaryTextPaint.setTextSize(res
					.getDimensionPixelSize(R.dimen.empty_list_text_size_secondary));
		} else {
			mSecondaryTextPaint = null;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		mDrawable.draw(canvas);
		canvas.drawText(mPrimaryText, mTextOrigin.x, mTextOrigin.y, mPrimaryTextPaint);
		if (mSecondaryTextPaint != null)
			canvas.drawText(mSecondaryText, mTextOrigin.x,
					mTextOrigin.y + mPrimaryTextPaint.getTextSize() * 2, mSecondaryTextPaint);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		final Rect rect = LayoutUtilities.getCenteredRect(mDrawable.getIntrinsicWidth(),
				mDrawable.getIntrinsicHeight(), bounds);
		mDrawable.setBounds(rect);

		mTextOrigin.x = bounds.centerX();
		mTextOrigin.y = bounds.centerY() - mPrimaryTextPaint.getFontMetrics().ascent / 2;
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}
}