package com.pecuyu.customcamera.camera;

import android.content.Context;
import android.hardware.Camera;
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
							handlePictureTaken(data, camera);
							callback.onPictureTaken(data, camera);
						}
					});
				}
			});
		}
	}

	private void handlePictureTaken(byte[] data, Camera camera) {
		Log.e(TAG, "onPictureTaken");
		try {
			FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
					"/" + Environment.DIRECTORY_DCIM, System.currentTimeMillis() + ".jpeg"));
			fos.write(data);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			mPreview.afterPictureTaken();
		}
	}

	public void stopCamera() {
		if (mPreview != null) {
			mPreview.releaseCamera();
		}
	}
}
