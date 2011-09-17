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

import java.util.List;

import com.wigwamlabs.booksapp.db.DatabaseAdapter.CursorType;

public class SubjectActions {
	private static final ManyToManyActions ACTIONS = new ManyToManyActions(SubjectsTable.n,
			SubjectsTable._id, SubjectsTable.name, SubjectsTable.name_normalized, false,
			SubjectsTable.book_count, BookSubjectsTable.n, BookSubjectsTable.book_id,
			BookSubjectsTable.subject_id);

	public static void updateSubjects(DatabaseAdapter db, int t, long bookId,
			List<String> subjects, boolean checkExistingSubjects) {
		ACTIONS.updateItems(db, t, bookId, subjects, checkExistingSubjects, true,
				CursorType.SUBJECT_LIST);
	}
}
