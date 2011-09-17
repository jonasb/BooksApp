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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.TextView;

public class AboutSubActivity extends SubActivity {
	protected AboutSubActivity(Context context, SubActivityManager manager) {
		super(context, manager);

		setContentView(R.layout.about_main);

		final MovementMethod movementMethod = LinkMovementMethod.getInstance();

		try {
			final TextView aboutName = (TextView) findViewById(R.id.about_name);
			final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			final String versionName = packageInfo.versionName;
			aboutName.append(" " + versionName);
		} catch (final PackageManager.NameNotFoundException e) {
			// do nothing
		}

		final TextView aboutUsage = (TextView) findViewById(R.id.about_usage);
		final String using = context.getString(R.string.about_using);
		aboutUsage
				.setText(Html
						.fromHtml("<b>"
								+ using
								+ ":</b><br />"
								+ " &bull; <a href=\"http://books.google.com\">Google Books</a><br />"
								+ " &bull; <a href=\"http://code.google.com/p/zxing/\">ZXing</a><br />"
								+ " &bull; <a href=\"http://code.google.com/p/google-api-java-client/\">Google API Client Library for Java</a><br />"
								+ " &bull; <a href=\"http://sourceforge.net/projects/javacsv/\">Java CSV</a><br />"));
		aboutUsage.setMovementMethod(movementMethod);

		final TextView homePage = (TextView) findViewById(R.id.home_page);
		homePage.setText(Html.fromHtml("<a href=\"http://books-app.com\">http://books-app.com</a>"));
		homePage.setMovementMethod(movementMethod);
	}

	public AboutSubActivity prepare() {
		return this;
	}
}
