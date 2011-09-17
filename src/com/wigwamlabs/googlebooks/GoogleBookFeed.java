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

package com.wigwamlabs.googlebooks;

import java.util.List;

import com.google.api.client.util.Key;

public final class GoogleBookFeed {
	@Key("entry")
	public List<GoogleBook> books;
	@Key("link")
	public List<GoogleLink> links;
	public String nextUrl;
	@Key("openSearch:totalResults")
	public int totalResults;

	public void scrub() {
		// scrub books
		if (books != null) {
			for (final GoogleBook book : books) {
				book.scrub();
			}
		}

		// scrub links
		if (links != null) {
			for (final GoogleLink l : links) {
				if (l.rel != null && l.rel.equals("next")) {
					nextUrl = l.href;
				}
			}
		}
	}
}