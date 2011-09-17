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

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.view.View;
import android.widget.ImageView;

import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.ImageViewUtils.WhenNoImage;

public final class LayoutUtilities {
	/* package */static class FakeLinkStyle extends CharacterStyle implements UpdateAppearance {
		@Override
		public void updateDrawState(TextPaint tp) {
			tp.setColor(tp.linkColor);
			tp.setUnderlineText(true);
		}
	}

	public static void appendFakeLink(SpannableStringBuilder b, CharSequence text) {
		final int posBefore = b.length();
		b.append(text);
		b.setSpan(new FakeLinkStyle(), posBefore, b.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	public static Rect getCenteredRect(int contentWidth, int contentHeight, Rect bounds) {
		int width = contentWidth;
		int height = contentHeight;
		final int boundsWidth = bounds.width();
		final int boundsHeight = bounds.height();
		if (contentWidth > boundsWidth || contentHeight > boundsHeight) {
			final float scale = getScaleToFit(contentWidth, contentHeight, boundsWidth,
					boundsHeight);
			width = (int) (contentWidth * scale);
			height = (int) (contentHeight * scale);
		}

		final int left = (boundsWidth - width) / 2;
		final int top = (boundsHeight - height) / 2;
		return new Rect(left, top, left + width, top + height);
	}

	public static float getScaleToFit(int contentWidth, int contentHeight, final int boundsWidth,
			final int boundsHeight) {
		final float contentAspectRatio = (float) contentWidth / contentHeight;
		final float boundsAspectRatio = (float) boundsWidth / boundsHeight;
		final boolean fillHeight = boundsAspectRatio > contentAspectRatio;
		if (fillHeight)
			return (float) boundsHeight / contentHeight;
		else
			return (float) boundsWidth / contentWidth;
	}

	public static CharSequence turnCsvIntoFakeLinks(String prefix, String itemsString) {
		if (itemsString == null || itemsString.length() == 0)
			return null;

		final SpannableStringBuilder b = new SpannableStringBuilder();

		final List<String> authors = CommaStringList.stringToList(itemsString);
		for (final String author : authors) {
			if (b.length() > 0)
				b.append(", ");

			appendFakeLink(b, author);
		}
		if (prefix != null)
			b.insert(0, prefix + " ");

		return b;
	}

	public static CharSequence turnTextIntoFakeLinks(CharSequence text) {
		if (text == null || text.length() == 0)
			return null;

		final SpannableStringBuilder b = new SpannableStringBuilder();
		appendFakeLink(b, text);
		return b;
	}

	public static void updateStatus(Resources res, View view, int bookStatus) {
		switch (bookStatus) {
		case BookStatus.LATE:
			view.setBackgroundColor(res.getColor(R.color.book_late));
			break;
		case BookStatus.NORMAL:
			view.setBackgroundColor(res.getColor(R.color.book_normal));
			break;
		case BookStatus.PENDING:
			view.setBackgroundColor(res.getColor(R.color.book_pending));
			break;
		case BookStatus.UNKNOWN:
			view.setBackgroundColor(res.getColor(R.color.book_unknown));
			break;
		}
	}

	public static void updateThumbnail(Context context, ImageView imageView, Long bookId,
			ImageDownloadCollection thumbnails, CharSequence thumbnailUrl, boolean small,
			boolean thumbnailsPaused) {
		if (bookId == null) {
			thumbnails.attachUrl(thumbnailUrl, imageView,
					WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder), thumbnailsPaused);
		} else {
			thumbnails.attachFile(
					ThumbnailManager.getThumbnail(context, bookId.longValue(), small), imageView,
					WhenNoImage.usePlaceholder(R.drawable.thumbnail_placeholder), thumbnailsPaused);
		}
	}

	private LayoutUtilities() {
	}
}
