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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.wigwamlabs.booksapp.CheckableAdapter;
import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.LayoutUtilities;
import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.util.CommaStringList;

public class BookListItemViewHolder {
	public static View createOrReuse(Context context, View convertView) {
		if (convertView != null)
			return convertView; // reuse

		final View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, null);
		final BookListItemViewHolder holder = new BookListItemViewHolder(view);
		view.setTag(holder);
		return view;
	}

	public static BookListItemViewHolder from(View view) {
		return (BookListItemViewHolder) view.getTag();
	}

	private static String releaseDateAndPageCount(Context context, Date releaseDate,
			Integer pageCount) {
		String s = null;
		if (releaseDate != null) {
			s = Integer.toString(1900 + releaseDate.getYear());
		}
		if (pageCount != null) {
			s = (s == null ? "" : s + ", ");
			s += String.format(
					context.getResources().getQuantityString(R.plurals.page_count_format,
							pageCount.intValue()), pageCount);
		}
		return s;
	}

	public final TextView creators;
	private final CheckBox mCheckBox;
	public final TextView releasedAndPagecount;
	public final View status;
	public final ImageView thumbnail;
	public final TextView title;

	/* package */BookListItemViewHolder(View view) {
		mCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
		creators = (TextView) view.findViewById(R.id.creators);
		releasedAndPagecount = (TextView) view.findViewById(R.id.released_and_pagecount);
		status = view.findViewById(R.id.book_status);
		thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
		title = (TextView) view.findViewById(R.id.title);
	}

	public void update(Context context, Long bookId, ImageDownloadCollection thumbnails,
			String thumbnailUrl, boolean thumbnailsPaused, String titleText, String creatorsText,
			Integer pageCount, Date releaseDate, int bookStatus, boolean showCheckBox, int checked) {
		final int visibility;
		if (!showCheckBox)
			visibility = View.GONE;
		else
			visibility = (checked == CheckableAdapter.ITEM_NOT_CHECKABLE ? View.INVISIBLE
					: View.VISIBLE);
		mCheckBox.setVisibility(visibility);
		if (visibility == View.VISIBLE)
			mCheckBox.setChecked(checked == CheckableAdapter.ITEM_CHECKED);
		LayoutUtilities.updateStatus(context.getResources(), status, bookStatus);
		LayoutUtilities.updateThumbnail(context, thumbnail, bookId, thumbnails, thumbnailUrl, true,
				thumbnailsPaused);
		title.setText(titleText);
		creators.setText(CommaStringList.prepareStringForDisplay(creatorsText));
		releasedAndPagecount.setText(releaseDateAndPageCount(context, releaseDate, pageCount));
	}
}