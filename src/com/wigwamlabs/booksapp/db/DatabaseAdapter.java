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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.wigwamlabs.booksapp.Debug;
import com.wigwamlabs.util.Pair;
import com.wigwamlabs.util.WeakListIterator;

public class DatabaseAdapter {
	public static class CursorType {
		public static final CursorType AUTHOR_LIST = new CursorType(CursorType.TYPE_AUTHOR_LIST, 0);
		public static final CursorType BOOK_LIST = new CursorType(CursorType.TYPE_BOOK_LIST, 0);
		public static final CursorType COLLECTION_LIST = new CursorType(
				CursorType.TYPE_COLLECTION_LIST, 0);
		public static final CursorType CONTACT_LIST = new CursorType(CursorType.TYPE_CONTACT_LIST,
				0);
		public static final CursorType PUBLISHER_LIST = new CursorType(
				CursorType.TYPE_PUBLISHER_LIST, 0);
		public static final CursorType SERIES_LIST = new CursorType(CursorType.TYPE_SERIES_LIST, 0);
		public static final CursorType SUBJECT_LIST = new CursorType(CursorType.TYPE_SUBJECT_LIST,
				0);
		private static final int TYPE_AUTHOR_LIST = 0;
		private static final int TYPE_BOOK_DETAIL = 1;
		private static final int TYPE_BOOK_LIST = 2;
		private static final int TYPE_COLLECTION_LIST = 3;
		private static final int TYPE_CONTACT_LIST = 4;
		private static final int TYPE_LOAN_HISTORY = 5;
		private static final int TYPE_PUBLISHER_LIST = 6;
		private static final int TYPE_SERIES_LIST = 7;
		private static final int TYPE_SUBJECT_LIST = 8;

		public static CursorType bookDetail(long bookId) {
			return new CursorType(TYPE_BOOK_DETAIL, bookId);
		}

		public static CursorType loanHistory(long bookId) {
			return new CursorType(TYPE_LOAN_HISTORY, bookId);
		}

		public final long id;
		public final int type;

		private CursorType(int type, long id) {
			this.type = type;
			this.id = id;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;
			if (!(o instanceof CursorType))
				return false;
			final CursorType ct = (CursorType) o;
			return type == ct.type && id == ct.id;
		}

		@Override
		public int hashCode() {
			return type * 100000 + (int) id;
		}
	}

	private static class Helper extends SQLiteOpenHelper {
		private final Context mContext;

		public Helper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			final Resources res = mContext.getResources();
			try {
				db.beginTransaction();

				// version 1
				BooksTable.create(db);
				BookFieldsTable.create(db);
				ContactsTable.create(db);
				LoansTable.create(db);
				AuthorsTable.create(db);
				BookAuthorsTable.create(db);
				CollectionsTable.create(db, res);
				BookCollectionsTable.create(db);
				PublishersTable.create(db);
				BookPublishersTable.create(db);
				SubjectsTable.create(db);
				BookSubjectsTable.create(db);
				// version 2
				SeriesTable.create(db);
				BookSeriesTable.create(db);

				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				db.beginTransaction();

				switch (oldVersion) {
				case 1:
					BooksTable.upgrade(db, oldVersion);
					SeriesTable.create(db);
					BookSeriesTable.create(db);
					//$FALL-THROUGH$
				case 2:
				}

				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
	}

	public static final String[] BOOK_LIST_COLUMNS = { BooksTable.creators, BooksTable.title,
			BooksTable.page_count, BooksTable.release_date, BooksTable.loan_return_by };
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 2;
	private static final int MESSAGE_BOOK_ADDED = 0;
	private static final int MESSAGE_BOOK_REMOVED = 1;
	protected static final int MESSAGE_REQUERY_CURSOR = 2;
	private static final String TAG = "SQL";
	private final List<WeakReference<BookAddRemoveObserver>> mBookAddRemoveObservers = new ArrayList<WeakReference<BookAddRemoveObserver>>();
	private final List<Pair<CursorType, WeakReference<ExtendedCursor>>> mCursors = new ArrayList<Pair<CursorType, WeakReference<ExtendedCursor>>>();
	private SQLiteDatabase mDb;
	private boolean mDebugNotifySynchronously = false;
	private Handler mMainThreadHandler;

	public void addBookAddRemoveObserver(WeakReference<BookAddRemoveObserver> observer) {
		mBookAddRemoveObservers.add(observer);
	}

	public void addCursor(CursorType cursorType, ExtendedCursor cursor) {
		if (cursorType == null)
			return;

		mCursors.add(Pair.create(cursorType, new WeakReference<ExtendedCursor>(cursor)));
	}

	public int beginTransaction() {
		if (Debug.LOG_SQL)
			Log.d(TAG, "<transaction>");
		mDb.beginTransaction();
		return 123;
	}

	public void close() {
		mDb.close();
	}

	private void createMainThreadHandler() {
		mMainThreadHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_REQUERY_CURSOR) {
					requeryCursors_((CursorType) msg.obj);
				} else if (msg.what == MESSAGE_BOOK_ADDED) {
					@SuppressWarnings("unchecked")
					final Pair<Long, String> p = (Pair<Long, String>) msg.obj;
					onBookAdded_(p.first.longValue(), p.second);
				} else if (msg.what == MESSAGE_BOOK_REMOVED) {
					final long bookId = ((Long) msg.obj).longValue();
					onBookRemoved_(bookId);
				}
			}
		};
	}

	/**
	 * @param t
	 *            Transaction
	 */
	public void delete(int t, String table, String whereClause, String[] whereArgs) {
		if (Debug.LOG_SQL)
			Log.d(TAG, "~DELETE "
					+ table
					+ " WHERE "
					+ whereClause
					+ (whereArgs == null ? "" : " -- with args (" + TextUtils.join(", ", whereArgs)
							+ ")"));
		mDb.delete(table, whereClause, whereArgs);
	}

	public void endTransaction() {
		if (Debug.LOG_SQL)
			Log.d(TAG, "</transaction>");
		mDb.endTransaction();
	}

	/**
	 * @param t
	 *            Transaction
	 */
	public void execSQL(int t, String sql) {
		if (Debug.LOG_SQL)
			Log.d(TAG, sql);
		mDb.execSQL(sql);
	}

	public SQLiteDatabase getDb() {
		return mDb;
	}

	/**
	 * @param t
	 *            Transaction
	 */
	public long insertOrThrow(int t, String table, ContentValues values) {
		final long id = mDb.insertOrThrow(table, null, values);
		if (Debug.LOG_SQL)
			Log.d(TAG, "~INSERT INTO " + table + " VALUES(" + values + ") ==> " + id);
		return id;
	}

	public void onBookAdded(long bookId, String googleId) {
		sendMessageToMainThread(MESSAGE_BOOK_ADDED, Pair.create(Long.valueOf(bookId), googleId));
	}

	/* package */void onBookAdded_(long bookId, String googleId) {
		for (final BookAddRemoveObserver o : WeakListIterator.from(mBookAddRemoveObservers)) {
			o.onBookAdded(bookId, googleId);
		}
	}

	public void onBookRemoved(long bookId) {
		sendMessageToMainThread(MESSAGE_BOOK_REMOVED, Long.valueOf(bookId));
	}

	/* package */void onBookRemoved_(long bookId) {
		for (final BookAddRemoveObserver o : WeakListIterator.from(mBookAddRemoveObservers)) {
			o.onBookRemoved(bookId);
		}
	}

	public void open(Context context) {
		final Helper helper = new Helper(context);
		mDb = helper.getWritableDatabase();
		createMainThreadHandler();
	}

	public void openInMemory(Context context, boolean debugNotifySynchronously) {
		final Helper helper = new Helper(context);
		mDb = SQLiteDatabase.create(null);
		helper.onCreate(mDb);
		createMainThreadHandler();
		mDebugNotifySynchronously = debugNotifySynchronously;
	}

	public Cursor query(CursorFactory cursorFactory, String tables, String[] columns, String where,
			String[] selectionArgs, String groupBy, String having, String orderBy, String limit,
			CursorType cursorType) {
		final String query = SQLiteQueryBuilder.buildQueryString(false, tables, columns, where,
				groupBy, having, orderBy, limit);
		return queryRaw(cursorFactory, query, selectionArgs, cursorType);
	}

	public Cursor query(String tables, String[] columns, String where, String[] selectionArgs,
			String groupBy, String having, String orderBy, String limit) {
		final String query = SQLiteQueryBuilder.buildQueryString(false, tables, columns, where,
				groupBy, having, orderBy, limit);
		return queryRaw(query, selectionArgs);
	}

	public Cursor queryRaw(CursorFactory cursorFactory, String sql, String[] selectionArgs,
			CursorType cursorType) {
		if (Debug.LOG_SQL)
			Log.d(TAG,
					sql
							+ (selectionArgs == null ? "" : " -- with args ("
									+ TextUtils.join(", ", selectionArgs) + ")"));
		final ExtendedCursor c = (ExtendedCursor) mDb.rawQueryWithFactory(cursorFactory, sql,
				selectionArgs, null);
		addCursor(cursorType, c);
		return c;
	}

	public Cursor queryRaw(String sql, String[] selectionArgs) {
		if (Debug.LOG_SQL)
			Log.d(TAG,
					sql
							+ (selectionArgs == null ? "" : " -- with args ("
									+ TextUtils.join(", ", selectionArgs) + ")"));
		return mDb.rawQuery(sql, selectionArgs);
	}

	public void removeBookAddRemoveObserver(BookAddRemoveObserver observer) {
		for (int i = mBookAddRemoveObservers.size() - 1; i >= 0; i--) {
			if (mBookAddRemoveObservers.get(i).get() == observer)
				mBookAddRemoveObservers.remove(i);
		}
	}

	public void requeryCursors(CursorType cursorType) {
		sendMessageToMainThread(MESSAGE_REQUERY_CURSOR, cursorType);
	}

	/* package */void requeryCursors_(CursorType cursorType) {
		for (int i = mCursors.size() - 1; i >= 0; i--) {
			final Pair<CursorType, WeakReference<ExtendedCursor>> p = mCursors.get(i);
			final ExtendedCursor c = p.second.get();
			if (c == null) {
				mCursors.remove(i);
				continue;
			}
			final CursorType ct = p.first;
			if (ct.equals(cursorType)) {
				final int state = c.getActiveState();
				if (state == ExtendedCursor.STATE_ACTIVE)
					c.requery();
				else if (state == ExtendedCursor.STATE_SOFT_DEACTIVATED)
					c.deactivate();
			}
		}
	}

	private void sendMessageToMainThread(int what, Object obj) {
		final Message message = mMainThreadHandler.obtainMessage(what, obj);
		if (mDebugNotifySynchronously) {
			mMainThreadHandler.handleMessage(message);
			message.recycle();
			return;
		}

		mMainThreadHandler.sendMessage(message);
	}

	/**
	 * @param t
	 *            Transaction
	 */
	public void setTransactionSuccessful(int t) {
		if (Debug.LOG_SQL)
			Log.d(TAG, "transaction :-)");
		mDb.setTransactionSuccessful();
	}

	/**
	 * @param t
	 *            Transaction
	 */
	public int update(int t, String table, ContentValues values, String whereClause) {
		if (Debug.LOG_SQL)
			Log.d(TAG, "~UPDATE " + table + " VALUES(" + values + ") WHERE " + whereClause);
		return mDb.update(table, values, whereClause, null);
	}
}