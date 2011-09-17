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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;

public final class GoogleApiTestUtilities {
	public static String objectToXml(final Object dataObject,
			final XmlNamespaceDictionary namespaceDictionary) throws IOException {
		final StringWriter writer = new StringWriter();
		final XmlSerializer serializer = Xml.createSerializer();
		serializer.setOutput(writer);
		namespaceDictionary.serialize(serializer, "result", dataObject);
		String result = writer.toString();
		result = result.replaceFirst(".*<(\\w+:)?result [^>]+>", "");
		result = result.replaceFirst("</(\\w+:)?result>", "");
		return result;
	}

	public static <T> List<T> toList(T... items) {
		final List<T> list = new ArrayList<T>(items.length);
		for (final T item : items) {
			list.add(item);
		}
		return list;
	}
}
