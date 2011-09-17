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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.wigwamlabs.booksapp.Debug;

public final class QueryBuilder {
	public static class AlterQueryBuilder {
		private final StringBuilder mQuery = new StringBuilder();

		public AlterQueryBuilder(String tableName, String columnName) {
			mQuery.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN ")
					.append(columnName);
		}

		public void execute(SQLiteDatabase db) {
			if (Debug.LOG_SQL)
				Log.d(TAG, mQuery.toString());
			db.execSQL(mQuery.toString());
		}

		public AlterQueryBuilder integer(Boolean nullable) {
			type(" INTEGER", nullable);
			return this;
		}

		public AlterQueryBuilder text(Boolean nullable) {
			type(" TEXT", nullable);
			return this;
		}

		private void type(String t, Boolean nullable) {
			mQuery.append(t);
			if (nullable != null)
				mQuery.append(" NOT NULL");
		}
	}

	public static class CreateQueryBuilder {
		private boolean mHasAddedColumns;
		private final StringBuilder mQuery = new StringBuilder();

		public CreateQueryBuilder(String tableName, boolean fts3) {
			if (fts3) {
				mQuery.append("CREATE VIRTUAL TABLE ");
				mQuery.append(tableName);
				mQuery.append(" USING fts3 (");
			} else {
				mQuery.append("CREATE TABLE ");
				mQuery.append(tableName);
				mQuery.append(" (");
			}
		}

		public void execute(SQLiteDatabase db) {
			mQuery.append(")");
			if (Debug.LOG_SQL)
				Log.d(TAG, mQuery.toString());
			db.execSQL(mQuery.toString());
		}

		private void field(String columnName, String type, Boolean nullable) {
			prepareForColumn();
			mQuery.append(columnName);
			mQuery.append(type);
			if (nullable != null)
				mQuery.append(" NOT NULL");
		}

		public void integer(String columnName) {
			field(columnName, " INTEGER", Boolean.FALSE);
		}

		public void integer(String columnName, Boolean nullable) {
			field(columnName, " INTEGER", nullable);
		}

		public void pk(String columnName) {
			prepareForColumn();
			mQuery.append(columnName);
			mQuery.append(" INTEGER PRIMARY KEY AUTOINCREMENT");
		}

		private void prepareForColumn() {
			if (mHasAddedColumns)
				mQuery.append(", ");
			mHasAddedColumns = true;
		}

		public void real(String columnName) {
			field(columnName, " REAL", Boolean.FALSE);
		}

		public void real(String columnName, Boolean nullable) {
			field(columnName, " REAL", nullable);
		}

		public void text(String columnName) {
			field(columnName, " TEXT", Boolean.FALSE);
		}

		public void text(String columnName, Boolean nullable) {
			field(columnName, " TEXT", nullable);
		}
	}

	public static class ValuesBuilder {
		private final ContentValues mValues = new ContentValues();

		public ContentValues end() {
			return mValues;
		}

		public ValuesBuilder put(String key, Integer value) {
			mValues.put(key, value);
			return this;
		}

		public ValuesBuilder put(String key, Long value) {
			mValues.put(key, value);
			return this;
		}

		public ValuesBuilder put(String key, String value) {
			mValues.put(key, value);
			return this;
		}
	}

	private static final String TAG = "SQL";

	public static AlterQueryBuilder alterAddColumn(String tableName, String columnName) {
		return new AlterQueryBuilder(tableName, columnName);
	}

	public static CreateQueryBuilder create(String tableName) {
		return new CreateQueryBuilder(tableName, false);
	}

	public static CreateQueryBuilder createFts3(String tableName) {
		return new CreateQueryBuilder(tableName, true);
	}

	public static String drop(String tableName) {
		return "DROP TABLE IF EXISTS " + tableName;
	}

	public static ValuesBuilder values() {
		return new ValuesBuilder();
	}

	private QueryBuilder() {
	}
}
