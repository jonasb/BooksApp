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

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;

public final class LoanNotificationManager {
	private static final String EXTRA_BOOK_TITLE = "book_title";
	private static AlarmManager mAlarmManager;

	public static void createAlarm(Context context, long bookId, String bookTitle, Date date) {
		final Date now = Calendar.getInstance().getTime();
		if (date.before(now)) {
			removeAlarm(context, bookId);
			return;
		}

		final PendingIntent pendingIntent = createIntent(context, bookId, bookTitle);
		getAlarmManager(context).set(AlarmManager.RTC, date.getTime(), pendingIntent);
	}

	private static PendingIntent createIntent(Context context, long bookId, String bookTitle) {
		final Intent intent = new Intent(MainReceiver.LOAN_ALARM, Uri.parse("loan:" + bookId));
		intent.setClass(context, MainReceiver.class);
		intent.putExtra(EXTRA_BOOK_TITLE, bookTitle);
		return PendingIntent.getBroadcast(context, R.id.loan_alarm_request_code, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	private static void displayNotification(Context context, String bookTitle) {
		final Resources res = context.getResources();
		final NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		final Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(HomeActivity.ACTION_EXPIRED_LOANS);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		final PendingIntent pendingIntent = PendingIntent.getActivity(context,
				R.id.notification_request_code, intent, 0);

		final Notification notification = new Notification();
		notification.icon = R.drawable.notification_loan;
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.tickerText = res.getString(R.string.loan_notification_ticker, bookTitle);
		notification.setLatestEventInfo(context, res.getString(R.string.loan_notification_title),
				res.getString(R.string.loan_notification_text), pendingIntent);

		notificationManager.notify(R.id.loan_notification, notification);
	}

	private static AlarmManager getAlarmManager(Context context) {
		if (mAlarmManager == null)
			mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		return mAlarmManager;
	}

	public static void onAlarmFired(Context context, Intent intent) {
		// final long bookId =
		// Long.parseLong(intent.getData().getSchemeSpecificPart());
		final String bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE);

		displayNotification(context, bookTitle);
	}

	public static void removeAlarm(Context context, long bookId) {
		final PendingIntent intent = createIntent(context, bookId, "");
		getAlarmManager(context).cancel(intent);
	}

	public static void updateAlarm(Context context, long bookId, String bookTitle, Date date) {
		createAlarm(context, bookId, bookTitle, date);
	}

	private LoanNotificationManager() {
	}
}
