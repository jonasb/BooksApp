/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2010 Jonas Bengtsson
 * This file is based on CaptureActivity, but made more generic.
 */

package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.Vector;



public abstract class BarcodeScanActivity extends Activity implements
    CaptureActivityHandler.Callback, ResultPointCallback, SurfaceHolder.Callback {
  private static final String TAG = BarcodeScanActivity.class.getSimpleName();
  private CaptureActivityHandler handler;
  private boolean hasSurface;
  private InactivityTimer inactivityTimer;

  abstract protected SurfaceView getCameraPreview();

  protected String getCharacterSet() {
    return null;
  }

  abstract protected Vector<BarcodeFormat> getDecodeFormats();
  
  abstract protected void displayFrameworkBugMessageAndExit();

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    Window window = getWindow();
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    CameraManager.init(getApplication());
    handler = null;
    hasSurface = false;
    inactivityTimer = new InactivityTimer(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    SurfaceView surfaceView = getCameraPreview();
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
      surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    CameraManager.get().closeDriver();
  }

  @Override
  protected void onDestroy() {
    inactivityTimer.shutdown();
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
      // Handle these events so they don't launch the Camera app
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  /**
   * A valid barcode has been found.
   *
   * @param rawResult The contents of the barcode.
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  public void handleDecode(Result rawResult, Bitmap barcode) {
    inactivityTimer.onActivity();
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    try {
      CameraManager.get().openDriver(surfaceHolder, true);
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
      return;
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializating camera", e);
      displayFrameworkBugMessageAndExit();
      return;
    }
    if (handler == null) {
      handler = new CaptureActivityHandler(this, this, getDecodeFormats(), getCharacterSet());
    }
  }
  
  protected void restart() {
    handler.sendEmptyMessage(MessageId.RESTART_PREVIEW);
  }
}
