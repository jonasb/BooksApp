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
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wigwamlabs.booksapp.BookStatus;
import com.wigwamlabs.booksapp.ImageDownloadCollection;
import com.wigwamlabs.booksapp.LayoutUtilities;
import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.booksapp.TitleBar;
import com.wigwamlabs.booksapp.db.BookCollectionCursor;
import com.wigwamlabs.util.BasicTableListView;
import com.wigwamlabs.util.DateUtils;
import com.wigwamlabs.util.ViewUtils;

public class BookDetailsMainViewHolder extends BookDetailsViewHolder {
	public final TextView collectionsView;
	public final ImageView editButton;
	public final View loanBookView;
	public final TextView loanButton;
	public final BasicTableListView loanHistoryList;
	public final ImageView nextButton;
	public final TextView notesView;
	public final ImageView previousButton;
	public final CheckBox showLoanHistoryToggle;

	public BookDetailsMainViewHolder(View parent, TitleBar titleBar) {
		super(parent, titleBar);
		final Context context = parent.getContext();
		final Resources res = context.getResources();
		final LayoutInflater inflater = LayoutInflater.from(context);

		loanBookView = insertLoanBookView(parent, inflater, res);
		collectionsView = insertCollectionsView(parent, inflater);
		notesView = insertNotesView(context, parent);

		loanButton = (TextView) loanBookView.findViewById(R.id.loan_button);
		showLoanHistoryToggle = (CheckBox) loanBookView.findViewById(R.id.show_loan_history_toggle);
		loanHistoryList = (BasicTableListView) loanBookView.findViewById(R.id.loan_history_list);

		editButton = new ImageView(context);
		editButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		editButton.setImageResource(R.drawable.titlebar_create_edit);
		titleBar.addLeftContent(editButton);

		nextButton = new ImageView(context);
		nextButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		nextButton.setImageResource(R.drawable.titlebar_down);
		titleBar.addRightContent(nextButton);

		previousButton = new ImageView(context);
		previousButton.setBackgroundResource(R.drawable.titlebar_icon_background);
		previousButton.setImageResource(R.drawable.titlebar_up);
		titleBar.addRightContent(previousButton);
	}

	private TextView insertCollectionsView(View parent, LayoutInflater inflater) {
		final View row = inflater.inflate(R.layout.collection_view, null);

		final View publishedRow = parent.findViewById(R.id.published_row);
		ViewUtils.addAfterSibling(row, publishedRow, null);

		return (TextView) row.findViewById(R.id.collections);
	}

	private View insertLoanBookView(View parent, LayoutInflater inflater, Resources res) {
		final View view = inflater.inflate(R.layout.loan_book_view, null);

		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final int margin = res.getDimensionPixelOffset(R.dimen.loan_view_vert_margin);
		params.setMargins(0, margin, 0, margin);

		final View bookHeaderLayout = parent.findViewById(R.id.book_header_layout);
		ViewUtils.addAfterSibling(view, bookHeaderLayout, params);

		return view;
	}

	private TextView insertNotesView(Context context, View parent) {
		final TextView notes = new TextView(context);

		final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		final int margin = ViewUtils.dipToPixels(context.getResources().getDisplayMetrics(), 7);
		params.setMargins(margin, margin, margin, margin);

		final View table = parent.findViewById(R.id.detail_table_layout);
		ViewUtils.addAfterSibling(notes, table, params);

		return notes;
	}

	public void update(Context context, Long bookId, ImageDownloadCollection thumbnails,
			CharSequence thumbnailUrl, CharSequence title, CharSequence subtitle, String creators,
			Float rating, CharSequence series, Integer volume, CharSequence description,
			CharSequence publisher, Date releaseDate, CharSequence isbn10, CharSequence isbn13,
			String subjects, Integer pageCount, CharSequence dimensions, CharSequence notes,
			CharSequence loanedTo, int bookStatus, Date loanReturnBy, Date now) {
		super.update(context, bookId, thumbnails, thumbnailUrl, title, subtitle, creators, true,
				rating, series, volume, description, publisher, releaseDate, isbn10, isbn13,
				subjects, pageCount, dimensions);

		final Resources res = context.getResources();
		switch (bookStatus) {
		case BookStatus.LATE:
			loanBookView.setBackgroundResource(R.drawable.loan_view_background_late);
			break;
		case BookStatus.NORMAL:
			loanBookView.setBackgroundResource(R.drawable.loan_view_background_normal);
			break;
		case BookStatus.PENDING:
			loanBookView.setBackgroundResource(R.drawable.loan_view_background_pending);
			break;
		case BookStatus.UNKNOWN:
			// shouldn't get here
			break;
		}

		final String loanText;
		if (loanedTo != null) {
			final CharSequence returnBy = DateUtils.formatShort(now, loanReturnBy);
			loanText = res.getString(R.string.lent_until, loanedTo, returnBy);

		} else {
			loanText = res.getString(R.string.lend_book);
		}
		loanButton.setText(LayoutUtilities.turnTextIntoFakeLinks(loanText));

		notesView.setVisibility(notes == null ? View.GONE : View.VISIBLE);
		notesView.setText(notes);
	}

	public void updateCollections(BookCollectionCursor collections) {
		final SpannableStringBuilder b = new SpannableStringBuilder();

		if (collections.getCount() == 0) {
			final String text = collectionsView.getResources().getString(
					R.string.book_no_collections);
			LayoutUtilities.appendFakeLink(b, text);
			b.setSpan(new StyleSpan(Typeface.ITALIC), 0, b.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		} else {
			for (collections.moveToFirst(); !collections.isAfterLast(); collections.moveToNext()) {
				if (b.length() > 0)
					b.append(", ");
				LayoutUtilities.appendFakeLink(b, collections.name());
			}
		}

		collectionsView.setText(b);
	}
}