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

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;

import com.wigwamlabs.booksapp.R;
import com.wigwamlabs.booksapp.db.QueryBuilder.CreateQueryBuilder;
import com.wigwamlabs.util.StringUtils;

public final class CollectionsTable {
	public static final String _id = "_id";
	public static final String book_count = "book_count";
	public static final String n = "Collections";
	public static final String name = "name";
	public static final String name_normalized = "name_normalized";

	public static void create(SQLiteDatabase db, Resources res) {
		final CreateQueryBuilder t = QueryBuilder.create(n);
		t.pk(_id);
		t.text(name);
		t.text(name_normalized);
		t.integer(book_count);
		t.execute(db);

		final String[] initialCollections = res.getStringArray(R.array.initial_collections);
		final ContentValues values = new ContentValues(3);
		values.put(CollectionsTable.book_count, Integer.valueOf(0));
		for (final String collection : initialCollections) {
			values.put(CollectionsTable.name, collection);
			values.put(CollectionsTable.name_normalized, StringUtils.normalizeExtreme(collection));
			db.insertOrThrow(CollectionsTable.n, null, values);
		}
	}

	public static void drop(SQLiteDatabase db) {
		db.execSQL(QueryBuilder.drop(n));
	}
}