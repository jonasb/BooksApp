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

import android.database.Cursor;

public interface ExtendedCursor extends Cursor {
	public static final int STATE_ACTIVE = 0;
	public static final int STATE_CLOSED = 1;
	public static final int STATE_DEACTIVATED = 2;
	public static final int STATE_SOFT_DEACTIVATED = 3;

	public int getActiveState();

	public void setSoftDeactivated(boolean softDeactivate);
}
