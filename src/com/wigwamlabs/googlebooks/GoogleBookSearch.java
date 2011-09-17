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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.GoogleTransport;
import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Key;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.AtomParser;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.booksapp.db.GoogleIdSearchCursor;
import com.wigwamlabs.googleclient.ClientUtils;

public class GoogleBookSearch {
	public static class BookSearch {
		public interface Callback {
			void onDownloadFinished(GoogleBook book);
		}

		private final HttpRequest mRequest;

		public BookSearch(HttpRequest request) {
			mRequest = request;
		}

		public GoogleBook execute() throws IOException {
			final GoogleBook book = mRequest.execute().parseAs(GoogleBook.class);
			if (book != null) {
				book.isFullBook = true;
				book.scrub();
			}
			return book;
		}

		public void executeInBackground(BookSearch.Callback callback) {
			new BookSearchTask(callback).execute(this);
		}
	}

	private static class BookSearchTask extends AsyncTask<BookSearch, Void, GoogleBook> {
		private final WeakReference<BookSearch.Callback> mCallback;

		public BookSearchTask(BookSearch.Callback callback) {
			mCallback = new WeakReference<BookSearch.Callback>(callback);
		}

		@Override
		protected GoogleBook doInBackground(BookSearch... params) {
			final BookSearch search = params[0];
			try {
				return search.execute();
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(GoogleBook result) {
			final BookSearch.Callback callback = mCallback.get();
			if (callback != null) {
				callback.onDownloadFinished(result);
			}
		}
	}

	private static class BooksUrl extends GoogleUrl {
		@SuppressWarnings("unused")
		@Key("q")
		public String q;

		public BooksUrl(String encodedUrl) {
			super(encodedUrl);
		}
	}

	public static class FeedSearch {
		public interface Callback {
			void onDownloadFinished(GoogleBookFeed feed);
		}

		private DatabaseAdapter mDb;
		private final HttpRequest mRequest;

		public FeedSearch(HttpRequest request) {
			mRequest = request;
		}

		public GoogleBookFeed execute() throws IOException {
			final GoogleBookFeed feed = mRequest.execute().parseAs(GoogleBookFeed.class);
			feed.scrub();

			if (feed.books != null && mDb != null) {
				GoogleIdSearchCursor.updateDatabaseIds(mDb, feed.books);
			}
			return feed;
		}

		public void executeInBackground(FeedSearch.Callback callback) {
			new FeedSearchTask(callback).execute(this);
		}

		public void setDatabase(DatabaseAdapter db) {
			mDb = db;
		}
	}

	private static class FeedSearchTask extends AsyncTask<FeedSearch, Void, GoogleBookFeed> {
		private final WeakReference<FeedSearch.Callback> mCallback;

		public FeedSearchTask(FeedSearch.Callback callback) {
			mCallback = new WeakReference<FeedSearch.Callback>(callback);
		}

		@Override
		protected GoogleBookFeed doInBackground(FeedSearch... params) {
			final FeedSearch search = params[0];
			try {
				return search.execute();
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(GoogleBookFeed result) {
			final FeedSearch.Callback callback = mCallback.get();
			if (callback != null) {
				callback.onDownloadFinished(result);
			}
		}
	}

	public static class Namespace {
		public static final XmlNamespaceDictionary DICTIONARY = new XmlNamespaceDictionary();
		static {
			final Map<String, String> map = DICTIONARY.namespaceAliasToUriMap;
			map.put("", "http://www.w3.org/2005/Atom");
			map.put("atom", "http://www.w3.org/2005/Atom");
			map.put("dc", "http://purl.org/dc/terms");
			map.put("openSearch", "http://a9.com/-/spec/opensearchrss/1.0/");
		}
	}

	private static final String VOLUMES_URL = "http://books.google.com/books/feeds/volumes";
	private final HttpTransport mTransport;

	public GoogleBookSearch(Context context) {
		mTransport = GoogleTransport.create();
		final GoogleHeaders headers = (GoogleHeaders) mTransport.defaultHeaders;
		headers.setApplicationName(ClientUtils.getApplicationName(context));
		headers.gdataVersion = "2";

		final AtomParser parser = new AtomParser();
		parser.namespaceDictionary = Namespace.DICTIONARY;
		mTransport.addParser(parser);
	}

	private String prefixAndJoin(final String[] items, final String prefix, final String join) {
		final StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < items.length; i++) {
			buffer.append(prefix).append(items[i]);
			if (i != items.length - 1)
				buffer.append(join);
		}
		return buffer.toString();
	}

	public FeedSearch searchByAny(String query) {
		return searchFeed(query);
	}

	public FeedSearch searchByAuthor(String name) {
		// TODO escape name
		return searchFeed("inauthor:\"" + name + '"');
	}

	public BookSearch searchByGoogleId(String googleId) {
		final BooksUrl url = new BooksUrl(VOLUMES_URL + "/" + googleId);
		final HttpRequest request = mTransport.buildGetRequest();
		request.url = url;

		return new BookSearch(request);
	}

	public FeedSearch searchByIsbns(String... isbns) {
		return searchFeed(prefixAndJoin(isbns, "isbn:", " OR "));
	}

	public FeedSearch searchByPublisher(String name) {
		// TODO escape name
		return searchFeed("inpublisher:\"" + name + '"');
	}

	public FeedSearch searchBySubject(String name) {
		// TODO escape name
		return searchFeed("subject:\"" + name + '"');
	}

	public FeedSearch searchByTitle(String title) {
		// TODO escape title
		return searchFeed("intitle:" + title);
	}

	private FeedSearch searchFeed(String query) {
		final BooksUrl url = new BooksUrl(VOLUMES_URL);
		url.q = query;
		final HttpRequest request = mTransport.buildGetRequest();
		request.url = url;
		return new FeedSearch(request);
	}

	public FeedSearch searchNext(GoogleBookFeed result) {
		if (result == null || result.nextUrl == null)
			return null;
		final HttpRequest request = mTransport.buildGetRequest();
		request.url = new GenericUrl(result.nextUrl);
		return new FeedSearch(request);
	}

	public FeedSearch searchRelatedByGoogleId(String googleId) {
		return searchFeed("related:" + googleId);
	}

	public FeedSearch searchRelatedByIsbn(String isbn) {
		return searchFeed("related:isbn" + isbn);
	}
}
