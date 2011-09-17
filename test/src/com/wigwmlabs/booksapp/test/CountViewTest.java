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

package com.wigwmlabs.booksapp.test;

import junit.framework.TestCase;

import com.wigwamlabs.booksapp.CountView;

public class CountViewTest extends TestCase {
	public void testShortCount() {
		assertEquals("0", CountView.shortCount(0));
		assertEquals("1", CountView.shortCount(1));
		assertEquals("999", CountView.shortCount(999));
		// K
		assertEquals("1K", CountView.shortCount(1000));
		assertEquals("1K", CountView.shortCount(1500));
		assertEquals("999K", CountView.shortCount(999999));
		// M
		assertEquals("1M", CountView.shortCount(1000000));
		assertEquals("1M", CountView.shortCount(1500000));
		assertEquals("999M", CountView.shortCount(999999999));
		// G
		assertEquals("1G", CountView.shortCount(1000000000));
		assertEquals("1G", CountView.shortCount(1500000000));
	}
}
