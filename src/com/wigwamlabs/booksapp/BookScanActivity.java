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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.client.apache.ApacheHttpTransport;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BarcodeScanActivity;
import com.google.zxing.client.android.ViewfinderView;
import com.wigwamlabs.booksapp.IsbnSearchService.LocalBinder;
import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.Compatibility;

public class BookScanActivity extends BarcodeScanActivity implements ServiceConnection {
	public static final String COLLECTION_KEY = "collection";
	public static final String ISBNS_KEY = "isbns";
	public static final int SCAN_BOOKS_REQUEST = 0;
	private static final float SOUND_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION_MS = 200L;
	private LocalBinder mBinder;
	private final Vector<BarcodeFormat> mDecodeFormats = new Vector<BarcodeFormat>();
	private View mDoneButton;
	private String mLastScannedIsbn;
	private final List<String> mScannedIsbns = new ArrayList<String>();
	private IsbnSearchAdapter mScannedListAdapter;
	private MediaPlayer mSuccessSound;
	private Vibrator mVibrator;
	private ViewfinderView mViewfinder;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		final int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_CAMERA) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				Toast.makeText(this, R.string.scan_button_press_toast, Toast.LENGTH_SHORT).show();
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void displayFrameworkBugMessageAndExit() {
		// TODO Auto-generated method stub
		finish();
	}

	public void done() {
		final Intent data = new Intent();
		// TODO no need to return ISBNs anymore?
		data.putExtra(ISBNS_KEY, mScannedIsbns.toArray(new String[mScannedIsbns.size()]));
		setResult(RESULT_OK, data);
		finish();
	}

	@Override
	public void drawViewfinder() {
		mViewfinder.drawViewfinder();
	}

	@Override
	public void foundPossibleResultPoint(ResultPoint point) {
		mViewfinder.addPossibleResultPoint(point);
	}

	@Override
	protected SurfaceView getCameraPreview() {
		return (SurfaceView) findViewById(R.id.cameraPreview);
	}

	@Override
	protected Vector<BarcodeFormat> getDecodeFormats() {
		return mDecodeFormats;
	}

	@Override
	public void handleDecode(Result obj, Bitmap barcode) {
		super.handleDecode(obj, barcode);

		final String isbn = obj.toString();

		if (!isbn.equals(mLastScannedIsbn)) {
			if (!mScannedIsbns.contains(isbn)) {
				mScannedIsbns.add(isbn);

				if (mBinder != null)
					mBinder.addIsbn(isbn);
			} else {
				Toast.makeText(this, R.string.scanned_duplicate_toast, Toast.LENGTH_SHORT).show();
			}
			mLastScannedIsbn = isbn;

			notifyScanned();
		}

		mDoneButton.setVisibility(View.VISIBLE);

		restart();
	}

	private void notifyScanned() {
		mVibrator.vibrate(VIBRATE_DURATION_MS);
		if (mSuccessSound != null)
			mSuccessSound.start();
	}

	@Override
	public void onBackPressed() {
		done();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Debug.enableStrictMode();

		final DatabaseAdapter db = ((BooksApp) getApplicationContext()).getDb();

		// initialise service
		final Intent service = new Intent(this, IsbnSearchService.class);
		// start service so it won't close when we leave this activity
		startService(service);
		final boolean success = bindService(service, this, BIND_AUTO_CREATE);

		HttpTransportCache.install(ApacheHttpTransport.INSTANCE, this);

		setContentView(R.layout.book_scan_main);

		mViewfinder = (ViewfinderView) findViewById(R.id.viewfinder);

		final ListView scannedList = (ListView) findViewById(R.id.scanned_list);
		scannedList.setDivider(null);
		mScannedListAdapter = new IsbnSearchAdapter(this, db, false);
		scannedList.setAdapter(mScannedListAdapter);

		mDoneButton = findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				done();
			}
		});
		getDecodeFormats().add(BarcodeFormat.EAN_13);

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		setupSounds();

		Toast.makeText(this, R.string.scan_intro_toast, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindService(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Compatibility.SDK_INT < Build.VERSION_CODES.ECLAIR && keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			// workaround for pre-Eclair versions
			onBackPressed();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mBinder = (LocalBinder) service;
		mBinder.reset();
		mScannedListAdapter.setService(mBinder);

		final Intent intent = getIntent();
		final long collectionId = intent.getLongExtra(COLLECTION_KEY, -1);
		mBinder.setCollectionId(collectionId == -1 ? null : Long.valueOf(collectionId));

		// deal with books scanned before this point
		for (final String isbn : mScannedIsbns) {
			mBinder.addIsbn(isbn);
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mBinder = null;
	}

	private void setupSounds() {
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		final OnCompletionListener rewindOnCompletion = new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.seekTo(0);
			}
		};

		mSuccessSound = new MediaPlayer();
		mSuccessSound.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mSuccessSound.setOnCompletionListener(rewindOnCompletion);

		final AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.book_scan_success);
		try {
			mSuccessSound.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
					file.getLength());
			file.close();
			mSuccessSound.setVolume(SOUND_VOLUME, SOUND_VOLUME);
			mSuccessSound.prepare();
		} catch (final IOException e) {
			mSuccessSound = null;
		}
	}
}
