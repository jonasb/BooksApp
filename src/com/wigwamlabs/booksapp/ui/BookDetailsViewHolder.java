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

package com.wigwamlabs.booksapp.ui;

import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.LayoutUtilities;
import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.booksapp.TitleBar;
import com.wigwamlabs.util.CommaStringList;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.StringUtils;

public class BookDetailsViewHolder {
	private static CharSequence creatorText(String creators, boolean linkifyBookGroups,
			Resources res) {
		final String by = res.getString(R.string.by_author);
		final CharSequence byCreators;
		if (linkifyBookGroups)
			byCreators = LayoutUtilities.turnCsvIntoFakeLinks(by, creators);
		else {
			final String displayCreators = CommaStringList.prepareStringForDisplay(creators);
			byCreators = (displayCreators != null && displayCreators.length() > 0 ? by + " "
					+ creators : null);
		}
		return byCreators;
	}

	private static CharSequence publishedText(boolean linkifyBookGroups, CharSequence publisher,
			Date releaseDate) {
		final SpannableStringBuilder builder = new SpannableStringBuilder();
		if (publisher != null && publisher.length() > 0) {
			if (linkifyBookGroups)
				LayoutUtilities.appendFakeLink(builder, publisher);
			else
				builder.append(publisher);
		}
		final String releasedAt = DateUtils.formatSparse(releaseDate);
		if (releasedAt != null) {
			if (builder.length() > 0)
				builder.append(", ");
			builder.append(releasedAt);
		}
		return builder;
	}

	private static CharSequence seriesText(CharSequence series, Integer volume) {
		final SpannableStringBuilder builder = new SpannableStringBuilder();
		if (series != null && series.length() > 0) {
			LayoutUtilities.appendFakeLink(builder, series);

			if (volume != null)
				builder.append(" #").append(volume.toString());
		}
		return builder;
	}

	private static CharSequence subjectsText(boolean linkifyBookGroups, String subjects) {
		return linkifyBookGroups ? LayoutUtilities.turnCsvIntoFakeLinks(null, subjects)
				: CommaStringList.prepareStringForDisplay(subjects);
	}

	public final TextView creatorsView;
	public final TextView isbnsView;
	public final TextView overviewView;
	public final TextView publishedView;
	public final RatingBar ratingBar;
	public final TextView seriesView;
	public final TextView sizeView;
	public final TextView subjectsView;
	public final TextView subtitleView;
	public final ImageView thumbnailView;
	public final TitleBar titleBarView;
	public final TextView titleView;

	public BookDetailsViewHolder(View parent, TitleBar titleBar) {
		titleBarView = titleBar;
		thumbnailView = (ImageView) parent.findViewById(R.id.thumbnail);
		ratingBar = (RatingBar) parent.findViewById(R.id.rating);
		titleView = (TextView) parent.findViewById(R.id.title);
		subtitleView = (TextView) parent.findViewById(R.id.subtitle);
		seriesView = (TextView) parent.findViewById(R.id.series);
		overviewView = (TextView) parent.findViewById(R.id.overview);
		creatorsView = (TextView) parent.findViewById(R.id.creators);
		publishedView = (TextView) parent.findViewById(R.id.published);
		isbnsView = (TextView) parent.findViewById(R.id.isbns);
		subjectsView = (TextView) parent.findViewById(R.id.subjects);
		sizeView = (TextView) parent.findViewById(R.id.size);
	}

	protected CharSequence getSize(Resources res, Integer pageCount, CharSequence dimensions) {
		String size = null;
		if (pageCount != null) {
			size = String.format(
					res.getQuantityString(R.plurals.page_count_format, pageCount.intValue()),
					pageCount);
		}
		if (dimensions != null) {
			size = (size == null ? "" : size + ", ");
			size += dimensions;
		}
		return size;
	}

	public void update(Context context, Long bookId, ImageDownloadCollection thumbnails,
			CharSequence thumbnailUrl, CharSequence title, CharSequence subtitle, String creators,
			boolean linkifyBookGroups, Float rating, CharSequence series, Integer volume,
			CharSequence description, CharSequence publisher, Date releaseDate,
			CharSequence isbn10, CharSequence isbn13, String subjects, Integer pageCount,
			CharSequence dimensions) {
		final Resources res = context.getResources();

		updateThumbnail(context, bookId, thumbnails, thumbnailUrl);
		updateTextView(titleView, title, false);
		updateTextView(subtitleView, subtitle, false);
		updateTextView(creatorsView, creatorText(creators, linkifyBookGroups, res), false);
		ratingBar.setRating(rating == null ? 0f : rating.floatValue());
		updateTextView(seriesView, seriesText(series, volume), true);
		updateTextView(overviewView, description, true);
		updateTextView(publishedView, publishedText(linkifyBookGroups, publisher, releaseDate),
				true);
		updateTextView(isbnsView, StringUtils.nullableArrayToString(isbn10, isbn13), true);
		updateTextView(subjectsView, subjectsText(linkifyBookGroups, subjects), true);
		updateTextView(sizeView, getSize(res, pageCount, dimensions), true);
	}

	protected void updateTextView(TextView view, CharSequence text, boolean hideParentIfEmpty) {
		view.setText(text);
		final int visibility = (text == null || text.length() == 0 ? View.GONE : View.VISIBLE);
		if (hideParentIfEmpty) {
			final ViewGroup parent = (ViewGroup) view.getParent();
			parent.setVisibility(visibility);
		} else {
			view.setVisibility(visibility);
		}
	}

	public void updateThumbnail(Context context, Long bookId, ImageDownloadCollection thumbnails,
			CharSequence thumbnailUrl) {
		LayoutUtilities.updateThumbnail(context, thumbnailView, bookId, thumbnails, thumbnailUrl,
				true, false);
	}
}