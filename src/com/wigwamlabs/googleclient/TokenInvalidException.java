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

package com.wigwamlabs.googleclient;

import com.google.api.client.http.HttpResponseException;

@SuppressWarnings("serial")
public class TokenInvalidException extends Exception {
	public static void checkAndThrow(HttpResponseException e) throws TokenInvalidException,
			HttpResponseException {
		final int statusCode = e.response.statusCode;
		if (statusCode == 401 || statusCode == 403) {
			throw new TokenInvalidException();
		} else
			throw e;
	}
}
