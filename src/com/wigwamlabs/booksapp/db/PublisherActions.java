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

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;

public class PublisherActions {
	private static final ManyToManyActions ACTIONS = new ManyToManyActions(PublishersTable.n,
			PublishersTable._id, PublishersTable.name, PublishersTable.name_normalized, false,
			PublishersTable.book_count, BookPublishersTable.n, BookPublishersTable.book_id,
			BookPublishersTable.publisher_id);

	public static void updatePublisher(DatabaseAdapter db, int t, long bookId, String publisher,
			boolean checkExistingPublisher) {
		List<String> cl = null;
		if (publisher != null) {
			cl = new ArrayList<String>(1);
			cl.add(publisher);
		}

		ACTIONS.updateItems(db, t, bookId, cl, checkExistingPublisher, true,
				CursorType.PUBLISHER_LIST);
	}
}
