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

public class SeriesTable {
	public static final String _id = "_id";
	public static final String book_count = "book_count";
	public static final String n = "Series";
	public static final String name = "name";
	public static final String name_normalized = "name_normalized";

	public static void create(SQLiteDatabase db) {
		final QueryBuilder.CreateQueryBuilder t = QueryBuilder.create(n);
		t.pk(_id);
		t.text(name);
		t.text(name_normalized);
		t.integer(book_count);
		t.execute(db);
	}
}
