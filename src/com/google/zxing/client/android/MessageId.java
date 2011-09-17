package com.google.zxing.client.android;

final class MessageId {
	// CaptureActivityHandler <-> DecodeThread
	public static final int DECODE = 0;
	public static final int QUIT = 1;
	public static final int DECODE_SUCCEEDED = 2;
	public static final int DECODE_FAILED = 3;
	// CaptureActivityHandler <-> CameraManager
	public static final int AUTO_FOCUS = 4;
	// BarcodeScanActivity <-> CaptureActivityHandler
	public static final int RESTART_PREVIEW = 5;
}
