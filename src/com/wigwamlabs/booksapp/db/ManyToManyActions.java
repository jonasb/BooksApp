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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;
import com.wigwamlabs.util.Pair;
import com.wigwamlabs.util.StringUtils;

public final class ManyToManyActions {
	private static final ContentValues NEW_ITEM_VALUES = new ContentValues(3);
	private static final Integer ONE = Integer.valueOf(1);

	private static boolean removeFromExistingItems(String item,
			List<Pair<Long, String>> existingItems) {
		if (existingItems == null)
			return false;
		final int count = existingItems.size();
		for (int i = 0; i < count; i++) {
			final Pair<Long, String> e = existingItems.get(i);
			if (e.second.equals(item)) {
				existingItems.remove(i);
				return true;
			}
		}
		return false;
	}

	private final String mItemCountField;
	private final String mItemIdField;
	private final String mItemNameField;
	private final String mItemNameNormalizedField;
	private final String mItemTable;
	private final String mJoinItemIdField;
	private final String mJoinMainEntityIdField;
	private final String mJoinTable;
	private final boolean mNormalizeAsPersonName;

	public ManyToManyActions(String itemTable, String itemIdField, String itemNameField,
			String itemNameNormalizedField, boolean normalizeAsPersonName, String itemCountField,
			String joinTable, String joinMainEntityIdField, String joinItemIdField) {
		mItemTable = itemTable;
		mItemIdField = itemIdField;
		mItemNameField = itemNameField;
		mItemNameNormalizedField = itemNameNormalizedField;
		mNormalizeAsPersonName = normalizeAsPersonName;
		mItemCountField = itemCountField;
		mJoinTable = joinTable;
		mJoinMainEntityIdField = joinMainEntityIdField;
		mJoinItemIdField = joinItemIdField;
	}

	public void addItem(DatabaseAdapter db, int t, long mainEntityId, long itemId,
			CursorType cursorType) {
		if (joinItemExists(db, mainEntityId, itemId))
			return;

		incrementItem(db, t, itemId);

		createJoinItem(db, t, mainEntityId, itemId);

		db.requeryCursors(cursorType);
	}

	public void addItem(DatabaseAdapter db, int t, long mainEntityId, String item,
			CursorType cursorType) {
		Long itemId = getItem(db, item);
		if (itemId == null) {
			itemId = Long.valueOf(createItem(db, t, item));
		} else {
			if (joinItemExists(db, mainEntityId, itemId.longValue()))
				return;
			incrementItem(db, t, itemId.longValue());
		}

		createJoinItem(db, t, mainEntityId, itemId.longValue());

		db.requeryCursors(cursorType);
	}

	private long createItem(DatabaseAdapter db, int t, final String item) {
		NEW_ITEM_VALUES.put(mItemNameField, item);
		NEW_ITEM_VALUES.put(mItemNameNormalizedField, normalizeItemName(item));
		NEW_ITEM_VALUES.put(mItemCountField, ONE);
		final long itemId = db.insertOrThrow(t, mItemTable, NEW_ITEM_VALUES);
		NEW_ITEM_VALUES.clear();
		return itemId;
	}

	private void createJoinItem(DatabaseAdapter db, int t, long mainEntityId, long itemId) {
		final ContentValues ba = new ContentValues(2);
		ba.put(mJoinMainEntityIdField, Long.valueOf(mainEntityId));
		ba.put(mJoinItemIdField, Long.valueOf(itemId));
		db.insertOrThrow(t, mJoinTable, ba);
	}

	public void decrementOrRemoveItem(DatabaseAdapter db, int t, long mainEntityId, long itemId,
			boolean deleteItemWhenZero, CursorType cursorType) {
		final String joinWhere = mJoinMainEntityIdField + " = " + mainEntityId + " AND "
				+ mJoinItemIdField + " = " + itemId;
		final String itemsWhere = mItemIdField + " = " + itemId;
		decrementOrRemoveItems(db, t, joinWhere, itemsWhere, deleteItemWhenZero);

		db.requeryCursors(cursorType);
	}

	private void decrementOrRemoveItems(DatabaseAdapter db, int t, long mainEntityId,
			List<Pair<Long, String>> existingItems, boolean deleteItemWhenZero) {
		while (!existingItems.isEmpty()) {
			String joinWhere = "";
			String itemsWhere = "";
			for (int i = 0; i < 10 && !existingItems.isEmpty(); i++) {
				final long itemId = existingItems.remove(0).first.longValue();
				if (i > 0) {
					joinWhere += " OR ";
					itemsWhere += " OR ";
				}
				joinWhere += mJoinItemIdField + " = " + itemId;
				itemsWhere += mItemIdField + " = " + itemId;
			}
			joinWhere = mJoinMainEntityIdField + " = " + mainEntityId + " AND (" + joinWhere + ")";

			decrementOrRemoveItems(db, t, joinWhere, itemsWhere, deleteItemWhenZero);
		}
	}

	private void decrementOrRemoveItems(DatabaseAdapter db, int t, String joinWhere,
			String itemsWhere, boolean deleteItemWhenZero) {
		db.delete(t, mJoinTable, joinWhere, null);

		db.execSQL(t, "UPDATE " + mItemTable + " SET " + mItemCountField + " = " + mItemCountField
				+ " - 1  WHERE " + itemsWhere);

		if (deleteItemWhenZero) {
			db.delete(t, mItemTable, mItemCountField + " <= 0 AND (" + itemsWhere + ")", null);
		}
	}

	public void deleteItem(DatabaseAdapter db, int t, long itemId, CursorType cursorType) {
		db.delete(t, mJoinTable, mJoinItemIdField + " = " + itemId, null);
		db.delete(t, mItemTable, mItemIdField + " = " + itemId, null);

		db.requeryCursors(cursorType);
	}

	private List<Pair<Long, String>> getExistingItems(DatabaseAdapter db, long mainEntityId) {
		final String[] columns = { mItemIdField, mItemNameField };
		final Cursor c = db.query(mItemTable + ", " + mJoinTable, columns, mJoinMainEntityIdField
				+ " = " + mainEntityId + " AND " + mJoinItemIdField + " = " + mItemIdField, null,
				null, null, null, null);
		if (c.getCount() == 0) {
			c.close();
			return null;
		}

		final List<Pair<Long, String>> list = new ArrayList<Pair<Long, String>>(c.getCount());
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			list.add(Pair.create(Long.valueOf(c.getLong(0)), c.getString(1)));
		}
		c.close();

		return list;
	}

	private Long getItem(DatabaseAdapter db, final String item) {
		final String[] columns = { mItemIdField };
		final Cursor c = db.query(mItemTable, columns, mItemNameField + " = ?",
				new String[] { item }, null, null, null, "1");

		Long itemId = null;
		if (c.moveToFirst()) {
			itemId = Long.valueOf(c.getLong(0));
		}
		c.close();
		return itemId;
	}

	private void incrementItem(DatabaseAdapter db, int t, long itemId) {
		final String sql = "UPDATE " + mItemTable + " SET " + mItemCountField + " = "
				+ mItemCountField + " + 1 WHERE " + mItemIdField + " = " + itemId;
		db.execSQL(t, sql);
	}

	private long incrementOrCreateItem(DatabaseAdapter db, int t, final String item) {
		final Long itemId = getItem(db, item);
		if (itemId == null) {
			return createItem(db, t, item);
		}

		incrementItem(db, t, itemId.longValue());
		return itemId.longValue();
	}

	private boolean joinItemExists(DatabaseAdapter db, long mainEntityId, long itemId) {
		final String[] columns = { mJoinItemIdField };
		final Cursor c = db.query(mJoinTable, columns, mJoinMainEntityIdField + " = "
				+ mainEntityId + " AND " + mJoinItemIdField + " = " + itemId, null, null, null,
				null, "1");
		final boolean exists = (c.getCount() > 0);
		c.close();
		return exists;
	}

	private String normalizeItemName(String name) {
		if (mNormalizeAsPersonName)
			return StringUtils.normalizePersonNameExtreme(name);
		return StringUtils.normalizeExtreme(name);
	}

	public void renameItem(DatabaseAdapter db, int t, long itemId, String newName,
			CursorType cursorType) {
		final ContentValues values = new ContentValues();
		values.put(mItemNameField, newName);
		values.put(mItemNameNormalizedField, normalizeItemName(newName));
		db.update(t, mItemTable, values, mItemIdField + " = " + itemId);

		db.requeryCursors(cursorType);
	}

	public void updateItems(DatabaseAdapter db, int t, long mainEntityId, List<String> items,
			boolean checkExistingItems, boolean deleteItemWhenZero, CursorType cursorType) {
		// TODO check for duplicates
		final List<Pair<Long, String>> existingItems = checkExistingItems ? getExistingItems(db,
				mainEntityId) : null;

		if (items != null) {
			final ContentValues ba = new ContentValues(2);
			ba.put(mJoinMainEntityIdField, Long.valueOf(mainEntityId));
			for (final String item : items) {
				if (removeFromExistingItems(item, existingItems))
					continue;
				final long itemId = incrementOrCreateItem(db, t, item);
				ba.put(mJoinItemIdField, Long.valueOf(itemId));
				db.insertOrThrow(t, mJoinTable, ba);
			}
		}

		if (existingItems != null && existingItems.size() > 0) {
			decrementOrRemoveItems(db, t, mainEntityId, existingItems, deleteItemWhenZero);
		}

		db.requeryCursors(cursorType);
	}
}
