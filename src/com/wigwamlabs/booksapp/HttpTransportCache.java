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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import android.content.Context;

import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.LowLevelHttpTransport;
import com.wigwamlabs.util.CacheList;

public class HttpTransportCache extends LowLevelHttpTransport {
	private enum Method {
		GET
	}

	private static class RecordRequest extends LowLevelHttpRequest {
		private final HttpTransportCache mCache;
		private final Method mMethod;
		private LowLevelHttpRequest mProperRequest;
		private final String mUrl;

		public RecordRequest(HttpTransportCache cache, LowLevelHttpTransport transport, String url,
				Method method) throws IOException {
			mCache = cache;
			mUrl = url;
			mMethod = method;
			switch (method) {
			case GET:
				mProperRequest = transport.buildGetRequest(url);
				break;
			}
		}

		@Override
		public void addHeader(String name, String value) {
			mProperRequest.addHeader(name, value);
		}

		@Override
		public LowLevelHttpResponse execute() throws IOException {
			final Response response = new Response(mProperRequest.execute(), mUrl, mMethod);
			mCache.cacheResponse(response);
			return response;
		}

		@Override
		public void setContent(HttpContent content) throws IOException {
			mProperRequest.setContent(content);
		}
	}

	private static class ReplayRequest extends LowLevelHttpRequest {
		private final Response mResponse;

		public ReplayRequest(Response response) {
			mResponse = response;
		}

		@Override
		public void addHeader(String name, String value) {
		}

		@Override
		public LowLevelHttpResponse execute() throws IOException {
			return mResponse;
		}

		@Override
		public void setContent(HttpContent content) throws IOException {
		}
	}

	private static class Response extends LowLevelHttpResponse implements Serializable {
		private static final long serialVersionUID = -8312347393328339356L;
		private final byte[] mContent;
		private final String mContentEncoding;
		private final long mContentLength;
		private final String mContentType;
		private final String[] mHeaderNames;
		private final String[] mHeaderValues;
		private final String mReasonPhrase;
		private final Method mRequestMethod;
		private final String mRequestUrl;
		private final int mStatusCode;
		private final String mStatusLine;

		public Response(LowLevelHttpResponse response, String requestUrl, Method requestMethod)
				throws IOException {
			mRequestUrl = requestUrl;
			mRequestMethod = requestMethod;
			mContentEncoding = response.getContentEncoding();
			mContentLength = response.getContentLength();
			mContentType = response.getContentType();
			mReasonPhrase = response.getReasonPhrase();
			mStatusCode = response.getStatusCode();
			mStatusLine = response.getStatusLine();

			// content
			final BufferedInputStream in = new BufferedInputStream(response.getContent());
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];

			int length;
			while ((length = in.read(buffer)) != -1) {
				out.write(buffer, 0, length);
			}
			mContent = out.toByteArray();

			// headers
			final int headerCount = response.getHeaderCount();
			mHeaderNames = new String[headerCount];
			mHeaderValues = new String[headerCount];
			for (int i = 0; i < headerCount; i++) {
				mHeaderNames[i] = response.getHeaderName(i);
				mHeaderValues[i] = response.getHeaderValue(i);
			}
		}

		@Override
		public InputStream getContent() throws IOException {
			return new ByteArrayInputStream(mContent);
		}

		@Override
		public String getContentEncoding() {
			return mContentEncoding;
		}

		@Override
		public long getContentLength() {
			return mContentLength;
		}

		@Override
		public String getContentType() {
			return mContentType;
		}

		@Override
		public int getHeaderCount() {
			return mHeaderNames.length;
		}

		@Override
		public String getHeaderName(int index) {
			return mHeaderNames[index];
		}

		@Override
		public String getHeaderValue(int index) {
			return mHeaderValues[index];
		}

		public Integer getKey() {
			return responseKey(mRequestUrl, mRequestMethod);
		}

		@Override
		public String getReasonPhrase() {
			return mReasonPhrase;
		}

		public Method getRequestMethod() {
			return mRequestMethod;
		}

		public String getRequestUrl() {
			return mRequestUrl;
		}

		@Override
		public int getStatusCode() {
			return mStatusCode;
		}

		@Override
		public String getStatusLine() {
			return mStatusLine;
		}
	}

	/* package */static final String CACHEFILE_PREFIX = "response";
	/* package */static final String CACHEFILE_SUFFIX = ".bin";
	private static final int FILE_CACE_LOWER_LIMIT = 150;
	private static final int FILE_CACHE_UPPER_LIMIT = 200;
	private static final int INMEMORY_CACHE_LIMIT = 28;
	private static HttpTransportCache INSTANCE;

	public static void install(LowLevelHttpTransport properTransport, Context context) {
		if (INSTANCE != null) {
			assert (properTransport == INSTANCE.mProperTransport && context == INSTANCE.mContext);
			return;
		}
		INSTANCE = new HttpTransportCache(properTransport, context);
		HttpTransport.setLowLevelHttpTransport(INSTANCE);
	}

	static Integer responseKey(String url, Method method) {
		return Integer.valueOf((method + url).hashCode());
	}

	private final BackgroundFileOperations mBackgroundFileOperations;
	private final File mCacheDir;
	private final Context mContext;
	private final LowLevelHttpTransport mProperTransport;
	private final CacheList<Integer, Response> mResponses = new CacheList<Integer, Response>(
			INMEMORY_CACHE_LIMIT);

	private HttpTransportCache(LowLevelHttpTransport properTransport, Context context) {
		mProperTransport = properTransport;
		mContext = context;
		mCacheDir = context.getCacheDir();

		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(CACHEFILE_PREFIX) && name.endsWith(CACHEFILE_SUFFIX);
			}
		};
		final DeleteFileAction action = new DeleteFileAction(mCacheDir, FILE_CACHE_UPPER_LIMIT,
				FILE_CACE_LOWER_LIMIT, filter);
		mBackgroundFileOperations = new BackgroundFileOperations(action);
	}

	@Override
	public LowLevelHttpRequest buildDeleteRequest(String url) throws IOException {
		return mProperTransport.buildDeleteRequest(url);
	}

	@Override
	public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
		return buildRequest(url, Method.GET);
	}

	@Override
	public LowLevelHttpRequest buildPostRequest(String url) throws IOException {
		return mProperTransport.buildPostRequest(url);
	}

	@Override
	public LowLevelHttpRequest buildPutRequest(String url) throws IOException {
		return mProperTransport.buildPutRequest(url);
	}

	private LowLevelHttpRequest buildRequest(String url, Method method) throws IOException {
		final Response response = getCachedResponse(url, method);
		if (response != null)
			return new ReplayRequest(response);
		else
			return new RecordRequest(this, mProperTransport, url, method);
	}

	/* package */synchronized void cacheResponse(Response response) {
		final Integer key = response.getKey();
		mBackgroundFileOperations.addObjectSerialization(mCacheDir + File.separator
				+ CACHEFILE_PREFIX + key + CACHEFILE_SUFFIX, response);
		mResponses.put(key, response);
	}

	private synchronized Response getCachedResponse(String url, Method method) {
		final Integer key = responseKey(url, method);

		// check in memory cache
		Response cached = mResponses.get(key);
		if (cached != null) {
			if (cached.getRequestUrl().equals(url) && cached.getRequestMethod().equals(method)) {
				return cached;
			} else {
				return null;
			}
		}

		// check on disk cache
		final File file = new File(mCacheDir, CACHEFILE_PREFIX + key + CACHEFILE_SUFFIX);
		if (!file.exists())
			return null;

		try {
			final FileInputStream fileStream = new FileInputStream(file);
			final ObjectInputStream in = new ObjectInputStream(fileStream);
			try {
				cached = (Response) in.readObject();
				// TODO update modified date on file?
			} finally {
				in.close();
			}
			mResponses.put(key, cached);

			if (cached.getRequestUrl().equals(url) && cached.getRequestMethod().equals(method)) {
				return cached;
			} else {
				return null;
			}
		} catch (final Exception e) {
			e.printStackTrace();
			file.delete();
		}

		return null;
	}
}
