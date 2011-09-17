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

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class SimpleDatePickerDialog {
	public static void show(final Context context, Date date, final OnDateChangedListener listener) {
		// init date picker
		final DatePicker datePicker = (DatePicker) LayoutInflater.from(context).inflate(
				R.layout.date_picker, null, false);

		if (date == null) {
			date = Calendar.getInstance().getTime();
		}
		final Resources res = context.getResources();
		final int year = date.getYear() + 1900;
		final int cappedYear = Math.max(res.getInteger(R.integer.datepicker_startyear),
				Math.min(year, res.getInteger(R.integer.datepicker_endyear)));
		datePicker.init(cappedYear, date.getMonth(), date.getDate(), listener);

		// init dialog
		final AlertDialog dialog = new AlertDialog.Builder(context).setView(datePicker).create();
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	private SimpleDatePickerDialog() {
	}
}
