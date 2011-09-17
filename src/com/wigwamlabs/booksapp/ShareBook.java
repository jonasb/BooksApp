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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;

import com.wigwamlabs.util.StringUtils;

public final class ShareBook {
	public static void shareBook(Activity activity, String title, String authors, String infoUrl) {
		final Resources res = activity.getResources();

		final Intent send = new Intent(Intent.ACTION_SEND);
		send.setType("text/plain");
		send.putExtra(Intent.EXTRA_SUBJECT, res.getString(R.string.share_subject));

		String theTitle = StringUtils.trimmedStringOrNull(title);
		if (theTitle == null)
			theTitle = res.getString(R.string.share_no_title_placeholder);
		String body = res.getString(R.string.share_text, theTitle);

		final String theAuthors = StringUtils.trimmedStringOrNull(authors);
		if (theAuthors != null)
			body += " " + res.getString(R.string.by_author) + " " + theAuthors;

		if (infoUrl != null) {
			body += ". " + infoUrl;
		}

		body += " " + res.getString(R.string.share_text_suffix);

		send.putExtra(Intent.EXTRA_TEXT, body);
		try {
			activity.startActivity(Intent.createChooser(send, res.getString(R.string.share_book)));
		} catch (final ActivityNotFoundException ignored) {
		}
	}

	private ShareBook() {
	}
}
