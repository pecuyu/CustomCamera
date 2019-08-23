package com.pecuyu.customcamera.camera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraController {
	private static final String TAG = CameraController.class.getSimpleName();

	private Context mContext;
	private SurfaceHolder mHolder;
	private final CameraPreview mPreview;
	private Camera mCamera;

	public CameraController(Context context, SurfaceHolder holder) {
		mContext = context;
		mHolder = holder;
		mPreview = new CameraPreview(context,this,holder);
	}

	protected void setCamera(Camera camera) {
		mCamera = camera;
	}

	public void start() {
		if (mPreview != null && !mPreview.isPreviewed()) {
			mPreview.startPreview();
		}
	}

	public void takePicture(final Camera.PictureCallback callback) {
		if (mPreview.isPreviewed()) {
			mPreview.onPictureTaken();
			mCamera.autoFocus(new Camera.AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					mCamera.takePicture(new Camera.ShutterCallback() {
						@Override
						public void onShutter() {}},
							null, new Camera.PictureCallback() {
						@Override
						public void onPictureTaken(byte[] data, Camera camera) {
							String path = handlePictureTaken(data, camera);
							if (callback != null) callback.onPictureTaken(data, camera);
							if (path != null) {
								broadcastNewPicture(mContext,path);
							}
						}
					});
				}
			});
		}
	}

	private void broadcastNewPicture(final Context context, String filePath) {
		try {
			File file = new File(filePath);
			MediaScannerConnection.scanFile(context,			//insert picture data into database
				new String[] { file.getAbsolutePath() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {
						Log.v(TAG, "onScanCompleted() file " + path
								+ " was scanned seccessfully\n uri: " + uri);
						if (uri != null) {
							broadcastNewPicture(context, uri);  /// 广播通知有新图片
						}
					}
				});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void broadcastNewPicture(Context context, Uri uri) {
		context.sendBroadcast(new Intent(
				android.hardware.Camera.ACTION_NEW_PICTURE, uri));
		context.sendBroadcast(new Intent("com.android.camera.NEW_PICTURE", uri));
	}


	private String handlePictureTaken(byte[] data, Camera camera) {
		Log.e(TAG, "onPictureTaken");
		String path = null;
		try {
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
					File.separator + Environment.DIRECTORY_DCIM, System.currentTimeMillis() + ".jpeg");
			path = file.getAbsolutePath();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			path = null;
			e.printStackTrace();
		} catch (IOException e) {
			path = null;
			e.printStackTrace();
		}finally {
			mPreview.afterPictureTaken();
		}

		return path;
	}

	public void stopCamera() {
		if (mPreview != null) {
			mPreview.releaseCamera();
		}
	}
}
