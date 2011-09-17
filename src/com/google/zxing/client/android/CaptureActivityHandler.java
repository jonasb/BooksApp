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
 * Changes:
 *  - Replaced CaptureActivity with new Callback interface
 *  - Removed return_scan_result and R.id.launch_product_query functionality
 *  - Added viewfinderView 
 *  - Changed from R.id.* to MessageId.*
 *  - Temporarily removed support for R.id.restart_preview
 */

package com.google.zxing.client.android;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback; // JB: added
import com.google.zxing.client.android.camera.CameraManager;

/* JB: removed
import android.app.Activity;
import android.content.Intent;
*/
import android.graphics.Bitmap;
// JB: removed:  import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.Vector;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

  private static final String TAG = CaptureActivityHandler.class.getSimpleName();
  // JB <added>
  public interface Callback {
	void handleDecode(Result obj, Bitmap barcode);

	void drawViewfinder();
  }
  private final Callback callback;
  // JB </added>
  // JB: removed:  private final CaptureActivity activity;
  private final DecodeThread decodeThread;
  private State state;

  private enum State {
    PREVIEW,
    SUCCESS,
    DONE
  }

  // JB: removed:  CaptureActivityHandler(CaptureActivity activity, Vector<BarcodeFormat> decodeFormats,
  CaptureActivityHandler(Callback callback, ResultPointCallback resultPointCallback, Vector<BarcodeFormat> decodeFormats, // JB: added
      String characterSet) {
    /* JB: removed
    this.activity = activity;
    decodeThread = new DecodeThread(activity, decodeFormats, characterSet,
        new ViewfinderResultPointCallback(activity.getViewfinderView()));
    */
    // JB <added>
	this.callback = callback;
    decodeThread = new DecodeThread(this, decodeFormats, characterSet, resultPointCallback);
    // JB </added>
    decodeThread.start();
    state = State.SUCCESS;

    // Start ourselves capturing previews and decoding.
    CameraManager.get().startPreview();
    restartPreviewAndDecode();
  }

  @Override
  public void handleMessage(Message message) {
    switch (message.what) {
      // JB: removed:  case R.id.auto_focus:
      case MessageId.AUTO_FOCUS: // JB: added
        //Log.d(TAG, "Got auto-focus message");
        // When one auto focus pass finishes, start another. This is the closest thing to
        // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
        if (state == State.PREVIEW) {
        	// JB: removed:  CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
          CameraManager.get().requestAutoFocus(this, MessageId.AUTO_FOCUS); // JB: added
        }
        break;
      // JB: removed:  case R.id.restart_preview:
      case MessageId.RESTART_PREVIEW: // JB: added
        Log.d(TAG, "Got restart preview message");
        restartPreviewAndDecode();
        break;
      // JB: removed:  case R.id.decode_succeeded:
      case MessageId.DECODE_SUCCEEDED: // JB: added
        Log.d(TAG, "Got decode succeeded message");
        state = State.SUCCESS;
        Bundle bundle = message.getData();
        Bitmap barcode = bundle == null ? null :
            (Bitmap) bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
        // JB: removed:  activity.handleDecode((Result) message.obj, barcode);
        callback.handleDecode((Result) message.obj, barcode); // JB: added
        break;
      // JB: removed:  case R.id.decode_failed:
      case MessageId.DECODE_FAILED: // JB: added
        // We're decoding as fast as possible, so when one decode fails, start another.
        state = State.PREVIEW;
        // JB: removed:  CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageId.DECODE); // JB: added
        break;
      /* JB: remove functionality
      case R.id.return_scan_result:
        Log.d(TAG, "Got return scan result message");
        activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
        activity.finish();
        break;
      case R.id.launch_product_query:
        Log.d(TAG, "Got product query message");
        String url = (String) message.obj;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        activity.startActivity(intent);
        break;
      */
    }
  }

  public void quitSynchronously() {
    state = State.DONE;
    CameraManager.get().stopPreview();
    // JB: removed: Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
    Message quit = Message.obtain(decodeThread.getHandler(), MessageId.QUIT); // JB: added
    quit.sendToTarget();
    try {
      decodeThread.join();
    } catch (InterruptedException e) {
      // continue
    }

    // Be absolutely sure we don't send any queued up messages
    /* JB: removed
    removeMessages(R.id.decode_succeeded);
    removeMessages(R.id.decode_failed);
    */
    // JB <added>
    removeMessages(MessageId.DECODE_SUCCEEDED);
    removeMessages(MessageId.DECODE_FAILED);
    // JB </added>
  }

  private void restartPreviewAndDecode() {
    if (state == State.SUCCESS) {
      state = State.PREVIEW;
      /* JB: removed
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
      CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
      activity.drawViewfinder();
      */
      // JB <added>
      CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), MessageId.DECODE);
      CameraManager.get().requestAutoFocus(this, MessageId.AUTO_FOCUS);
      callback.drawViewfinder();
      // JB </added>
    }
  }

}
