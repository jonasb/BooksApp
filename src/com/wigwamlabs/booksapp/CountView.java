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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class CountView extends View {
	public static String shortCount(int count) {
		final String text;
		if (count < 1e3)
			text = Integer.toString(count);
		else if (count >= 1e3 && count < 1e6)
			text = Integer.toString((int) (count / 1e3)) + "K";
		else if (count >= 1e6 && count < 1e9)
			text = Integer.toString((int) (count / 1e6)) + "M";
		else if (count >= 1e9)
			text = Integer.toString((int) (count / 1e9)) + "G";
		else
			text = "";
		return text;
	}

	private final Rect mSizeBounds = new Rect();
	String mText = "";
	private float mTextBottom;
	private final Rect mTextBounds = new Rect();
	private float mTextLeft;
	Paint mTextPaint = new Paint();

	public CountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final Resources res = context.getResources();
		mTextPaint.setTextAlign(Align.LEFT);
		mTextPaint.setTextSize(res.getDimension(R.dimen.count_text_size));
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawText(mText, mTextLeft - mTextBounds.left, mTextBottom - mTextBounds.bottom,
				mTextPaint);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = mSizeBounds.width();
		final int height = mSizeBounds.height();
		final int size = width > height ? width : height;

		final int w = getPaddingLeft() + size + getPaddingRight();
		final int h = getPaddingTop() + size + getPaddingBottom();
		setMeasuredDimension(resolveSize(w, widthMeasureSpec), resolveSize(h, heightMeasureSpec));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final int textWidth = mTextBounds.width();
		final int textHeight = mTextBounds.height();
		mTextLeft = (w - textWidth) / 2f;
		mTextBottom = textHeight + (h - textHeight) / 2f;
	}

	public void setCount(int count) {
		final String text = shortCount(count);
		if (text.equals(mText))
			return;

		mText = text;
		mTextPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);

		String measureText = "";
		final int measureTextCount = (count >= 1000 ? 4 : text.length());
		for (int i = 0; i < measureTextCount; i++) {
			measureText += "0";
		}

		mTextPaint.getTextBounds(measureText, 0, measureText.length(), mSizeBounds);

		requestLayout();
		invalidate();
	}
}
