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

public final class BookGroup {
	public static final int AUTHORS = 0;
	public static final int COLLECTIONS = 1;
	public static final int CONTACTS = 2;
	public static final int PUBLISHERS = 3;
	public static final int SERIES = 4;
	public static final int SUBJECTS = 5;

	public static void openListActivity(SubActivityManager manager, int bookGroup,
			long bookGroupId, String bookGroupName) {
		if (bookGroup == AUTHORS) {
			manager.create(SearchSubActivity.class).prepareWithAuthor(bookGroupId, bookGroupName)
					.show();
		} else if (bookGroup == CONTACTS) {
			manager.create(LoanListSubActivity.class).prepare(bookGroupId).show();
		} else if (bookGroup == PUBLISHERS) {
			manager.create(SearchSubActivity.class)
					.prepareWithPublisher(bookGroupId, bookGroupName).show();
		} else if (bookGroup == SUBJECTS) {
			manager.create(SearchSubActivity.class).prepareWithSubject(bookGroupId, bookGroupName)
					.show();
		} else {
			manager.create(BookListSubActivity.class).prepareWithBookGroup(bookGroup, bookGroupId)
					.show();
		}
	}

	private BookGroup() {
	}
}
