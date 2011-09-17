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

package com.wigwamlabs.booksapp.db;

import android.database.sqlite.SQLiteDatabase;

import com.wigwamlabs.booksapp.db.QueryBuilder.CreateQueryBuilder;

public final class BooksTable {
	public static final String _id = "_id";
	public static final String cover_url = "cover_url";
	public static final String creators = "creators";
	public static final String dimensions = "dimensions";
	public static final String google_id = "google_id";
	public static final String isbn10 = "isbn10";
	public static final String isbn13 = "isbn13";
	public static final String loan_id = "loan_id";
	public static final String loan_return_by = "loan_return_by";
	public static final String n = "Books";
	public static final String page_count = "page_count";
	public static final String publisher = "publisher";
	public static final String rating = "rating";
	public static final String release_date = "release_date";
	public static final String series = "series";
	public static final String subjects = "subjects";
	public static final String subtitle = "subtitle";
	public static final String title = "title";
	public static final String title_normalized = "title_normalized";
	public static final String volume = "volume";

	public static void create(SQLiteDatabase db) {
		final CreateQueryBuilder t = QueryBuilder.create(n);
		// version 1
		t.pk(_id);
		t.text(cover_url, null);
		t.text(creators, null);
		t.text(dimensions, null);
		t.text(google_id, null);
		t.text(isbn10, null);
		t.text(isbn13, null);
		t.integer(loan_id, null);
		t.integer(loan_return_by, null);
		t.integer(page_count, null);
		t.text(publisher, null);
		t.real(rating, null);
		t.integer(release_date, null);
		t.text(subjects, null);
		t.text(subtitle, null);
		t.text(title, null);
		t.text(title_normalized, null);
		// version 2
		t.text(series, null);
		t.integer(volume, null);
		t.execute(db);
	}

	public static void drop(SQLiteDatabase db) {
		db.execSQL(QueryBuilder.drop(n));
	}

	public static void upgrade(SQLiteDatabase db, int oldVersion) {
		switch (oldVersion) {
		case 1:
			QueryBuilder.alterAddColumn(n, series).text(null).execute(db);
			QueryBuilder.alterAddColumn(n, volume).integer(null).execute(db);
			//$FALL-THROUGH$
		case 2:
		}
	}
}