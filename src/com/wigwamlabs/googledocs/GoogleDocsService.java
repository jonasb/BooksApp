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

package com.wigwamlabs.googledocs;

import java.io.File;
import java.io.IOException;

import android.content.Context;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.wigwamlabs.googleclient.ClientUtils;
import com.wigwamlabs.googleclient.TokenInvalidException;

public class GoogleDocsService {
	private static HttpTransport getTransport(Context context) {
		final HttpTransport transport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) transport.defaultHeaders;
		headers.setApplicationName(ClientUtils.getApplicationName(context));
		headers.gdataVersion = "3.0";
		return transport;
	}

	public static void uploadFile(Context context, File file, String mimeType, String authToken)
			throws IOException, TokenInvalidException {
		final HttpTransport transport = getTransport(context);

		final HttpRequest request = transport.buildPostRequest();
		request.url = new GoogleUrl("https://docs.google.com/feeds/default/private/full");
		final GoogleHeaders headers = (GoogleHeaders) request.headers;
		headers.setGoogleLogin(authToken);
		headers.setSlugFromFileName(file.getName());

		final InputStreamContent content = new InputStreamContent();
		content.type = mimeType;
		content.setFileInput(file);
		request.content = content;

		try {
			request.execute().ignore();
		} catch (final HttpResponseException e) {
			TokenInvalidException.checkAndThrow(e);
		}
	}
}
