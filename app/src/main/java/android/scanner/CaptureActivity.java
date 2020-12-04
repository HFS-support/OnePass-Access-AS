package android.scanner;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.scanner.camera.CameraManager;
import android.scanner.common.BitmapUtils;
import android.scanner.decode.BitmapDecoder;
import android.scanner.decode.CaptureActivityHandler;
import android.scanner.view.ViewfinderView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.fgtit.access.R;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.result.ResultParser;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback, View.OnClickListener {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final int REQUEST_CODE = 100;

	private static final int PARSE_BARCODE_FAIL = 300;
	private static final int PARSE_BARCODE_SUC = 200;


	private boolean hasSurface;


	private InactivityTimer inactivityTimer;


	private BeepManager beepManager;

	private AmbientLightManager ambientLightManager;

	private CameraManager cameraManager;

	private ViewfinderView viewfinderView;

	private CaptureActivityHandler handler;

	private Result lastResult;

	private boolean isFlashlightOpen;

	private Collection<BarcodeFormat> decodeFormats;

	private Map<DecodeHintType, ?> decodeHints;

	private String characterSet;

	private Result savedResultToShow;

	private IntentSource source;

	private String photoPath;

	private Handler mHandler = new MyHandler(this);

	static class MyHandler extends Handler {

		private WeakReference<Activity> activityReference;

		public MyHandler(Activity activity) {
			activityReference = new WeakReference<Activity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
				case PARSE_BARCODE_SUC:
					break;

				case PARSE_BARCODE_FAIL:

					break;

				default:
					break;
			}

			super.handleMessage(msg);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_brcapture);

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);

		findViewById(R.id.capture_scan_photo).setOnClickListener(this);

		findViewById(R.id.capture_flashlight).setOnClickListener(this);

		setResult(Activity.RESULT_CANCELED);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.

		// �����ʼ���Ķ�����Ҫ���������������Ļ��С����Щ����
		// ������ŵ�onCreate�У���Ϊ�����onCreate�м����״�����չʾ������Ϣ�Ĵ���� ����
		// �ᵼ��ɨ�贰�ڵĳߴ���������bug
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.capture_viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		lastResult = null;

		// ����ͷԤ�����ܱ������SurfaceView�����Ҳ��Ҫ��һ��ʼ������г�ʼ��
		// �����Ҫ�˽�SurfaceView��ԭ��
		// �ο�:http://blog.csdn.net/luoshengyang/article/details/8661317
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview_view); // Ԥ��
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);

		}
		else {
			// ��ֹsdk8���豸��ʼ��Ԥ���쳣
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
		}

		// �����������ã���ʵ��BeemManager�Ĺ�������Ҳ����ø÷���������onCreate��ʱ������һ��
		beepManager.updatePrefs();

		// ��������Ƶ�����
		ambientLightManager.start(cameraManager);

		// �ָ�������
		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		beepManager.close();

		// �ر�����ͷ
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if ((source == IntentSource.NONE) && lastResult != null) { // ���½���ɨ��
					restartPreviewAfterDelay(0L);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_FOCUS:
			case KeyEvent.KEYCODE_CAMERA:
				// Handle these events so they don't launch the Camera app
				return true;

			case KeyEvent.KEYCODE_VOLUME_UP:
				cameraManager.zoomIn();
				return true;

			case KeyEvent.KEYCODE_VOLUME_DOWN:
				cameraManager.zoomOut();
				return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (resultCode == RESULT_OK) {
			final ProgressDialog progressDialog;
			switch (requestCode) {
				case REQUEST_CODE:

					// ��ȡѡ��ͼƬ��·��
					Cursor cursor = getContentResolver().query(
							intent.getData(), null, null, null, null);
					if (cursor.moveToFirst()) {
						photoPath = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));
					}
					cursor.close();

					progressDialog = new ProgressDialog(this);
					progressDialog.setMessage("����ɨ��...");
					progressDialog.setCancelable(false);
					progressDialog.show();

					new Thread(new Runnable() {

						@Override
						public void run() {

							Bitmap img = BitmapUtils
									.getCompressedBitmap(photoPath);

							BitmapDecoder decoder = new BitmapDecoder(
									CaptureActivity.this);
							Result result = decoder.getRawResult(img);

							if (result != null) {
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_SUC;
								m.obj = ResultParser.parseResult(result)
										.toString();
								mHandler.sendMessage(m);
							}
							else {
								Message m = mHandler.obtainMessage();
								m.what = PARSE_BARCODE_FAIL;
								mHandler.sendMessage(m);
							}

							progressDialog.dismiss();

						}
					}).start();

					break;

			}
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG,
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		hasSurface = false;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {

		// ���¼�ʱ
		inactivityTimer.onActivity();

		lastResult = rawResult;

		// ��ͼƬ����ɨ���
		viewfinderView.drawResultBitmap(barcode);

		beepManager.playBeepSoundAndVibrate();

		Toast.makeText(this,
				"ʶ����:" + ResultParser.parseResult(rawResult).toString(),
				Toast.LENGTH_SHORT).show();

		Intent resultIntent = new Intent();  
		resultIntent.putExtra("barcode",ResultParser.parseResult(rawResult).toString());  
		setResult(1, resultIntent);  
		finish();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}

		if (cameraManager.isOpen()) {
			Log.w(TAG,
					"initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);
		}
		catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		}
		catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	/**
	 * ��CaptureActivityHandler�з�����Ϣ����չʾɨ�赽��ͼ��
	 * 
	 * @param bitmap
	 * @param result
	 */
	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		}
		else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler,
						R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.capture_scan_photo: // ͼƬʶ��
				// ���ֻ��е����
				Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
				innerIntent.setType("image/*");
				Intent wrapperIntent = Intent.createChooser(innerIntent,
						"ѡ���ά��ͼƬ");
				this.startActivityForResult(wrapperIntent, REQUEST_CODE);
				break;

			case R.id.capture_flashlight:
				if (isFlashlightOpen) {
					cameraManager.setTorch(false); // �ر������
					isFlashlightOpen = false;
				}
				else {
					cameraManager.setTorch(true); // �������
					isFlashlightOpen = true;
				}
				break;
			default:
				break;
		}

	}

}
