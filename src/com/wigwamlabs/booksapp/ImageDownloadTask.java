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

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URISyntaxException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.wigwamlabs.util.BitmapUtils;
import com.wigwamlabs.util.Pair;

public class ImageDownloadTask extends AsyncTask<CharSequence, Pair<CharSequence, Bitmap>, Void> {
	interface Callback {
		void onImageDownloaded(CharSequence url, Bitmap image);

		void onImageDownloadFinished();
	}

	/**
	 * Workaround for Android issue 6066:
	 * http://code.google.com/p/android/issues/detail?id=6066
	 */
	private static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					final int read = read();
					if (read < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}

	private static final String FILE_PROTOCOL = "file";
	private static final String TAG = ImageDownloadTask.class.getSimpleName();
	private final Callback mCallback;
	private final WeakReference<Callback> mCallbackWeak;
	private final Point mTargetSize;
	private final HttpTransport mTransport = new HttpTransport();

	public ImageDownloadTask(Point targetSize, final Callback callback) {
		mTargetSize = targetSize;
		mCallback = callback;
		mCallbackWeak = null;
	}

	public ImageDownloadTask(Point targetSize, final WeakReference<Callback> callback) {
		mTargetSize = targetSize;
		mCallbackWeak = callback;
		mCallback = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(final CharSequence... params) {
		for (final CharSequence param : params) {
			final String url = param.toString();
			Bitmap image;
			try {
				if (url.startsWith(FILE_PROTOCOL))
					image = openImage(url);
				else
					image = downloadImage(url);

				// abort if weak ref has been reset
				if (mCallbackWeak != null && mCallbackWeak.get() == null)
					return null;

				if (image != null && mTargetSize != null) {
					image = BitmapUtils.createScaledBitmap(image, mTargetSize.x, mTargetSize.y,
							true);
				}
			} catch (final Exception e) {
				// Debug.reportException(e);
				image = null;
			}

			publishProgress(Pair.create(param, image));
		}
		return null;
	}

	private Bitmap downloadImage(final String url) {
		Bitmap image = null;
		try {
			// configure request
			final HttpRequest request = mTransport.buildGetRequest();
			request.setUrl(url);
			// TODO set appropriate headers to only download if changed
			Log.i(TAG, "Downloading image: " + url);
			final HttpResponse response = request.execute();
			try {
				final InputStream content = response.getContent();
				// create bitmap
				if (content != null) {
					final FlushedInputStream imageStream = new FlushedInputStream(content);
					try {
						image = BitmapFactory.decodeStream(imageStream);
					} finally {
						imageStream.close();
					}
				}
			} finally {
				response.ignore();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return image;
	}

	@Override
	protected void onPostExecute(Void result) {
		final Callback callback = (mCallback != null ? mCallback : mCallbackWeak.get());
		if (callback != null) {
			callback.onImageDownloadFinished();
		}
	}

	@Override
	protected void onProgressUpdate(Pair<CharSequence, Bitmap>... values) {
		final Callback callback = (mCallback != null ? mCallback : mCallbackWeak.get());
		if (callback != null) {
			for (final Pair<CharSequence, Bitmap> value : values) {
				callback.onImageDownloaded(value.first, value.second);
			}
		}
	}

	private Bitmap openImage(String uri) {
		final URI u;
		try {
			u = new URI(uri);
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			return null;
		}

		final File f = new File(u);
		if (f.exists())
			return BitmapFactory.decodeFile(f.getPath());
		else
			return null;
	}
}
