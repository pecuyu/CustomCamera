package com.pecuyu.customcamera.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraPreview implements SurfaceHolder.Callback {
	private static final String TAG = CameraPreview.class.getSimpleName();

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private Context mContext;
	private CameraController mCameraController;
	private volatile boolean isPreviewed;
	private final int mScreenWidth;
	private final int mScreenHeight;

	public CameraPreview(Context context, CameraController cameraController, SurfaceHolder holder) {
		mContext = context;
		mCameraController = cameraController;
		mHolder = holder;
		holder.addCallback(this);
		initCamera();

		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		mScreenWidth = displayMetrics.widthPixels;
		mScreenHeight = displayMetrics.heightPixels;
	}

	protected void onPictureTaken() {
		isPreviewed = false;
	}

	protected void afterPictureTaken() {
		startPreview();
	}

	protected void startPreview() {
		if (mCamera == null) {
			initCamera();
		}

		if (mCamera != null) {
			mCamera.stopPreview();

			try {
				mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
					@Override
					public void onPreviewFrame(byte[] data, Camera camera) {
						isPreviewed = true;
						Log.e(TAG, "onPreviewFrame isPreviewed=" + isPreviewed);
					}
				});
				mCamera.setPreviewDisplay(mHolder);
				mCamera.setDisplayOrientation(90); // 设置预览方向
				mCamera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void initCamera() {
		mCamera = getCameraInstance();
		initParams();
	}

	private void initParams() {
		if (mCamera != null) {
			Camera.Parameters parameters = mCamera.getParameters();
			if (parameters != null) {
				try {
					int previewWidth = mScreenWidth;
					int previewHeight = mScreenHeight;
					// 选择合适的预览尺寸
					List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
					for (Camera.Size cur : sizeList) {
						if (cur.width >= previewWidth
								&& cur.height >= previewHeight) {
							previewWidth = cur.width;
							previewHeight = cur.height;
							break;
						}
					}

					parameters.setPreviewSize(previewWidth, previewHeight); //获得摄像区域的大小
					parameters.setPictureSize(previewWidth, previewHeight);//设置拍出来的屏幕大小
					parameters.setPictureFormat(PixelFormat.JPEG);//设置照片输出的格式
					parameters.setJpegQuality(100);//设置照片质量
					parameters.setRotation(90); // 输出图片的方向
					Log.e(TAG, "previewWidth=" + previewWidth + " ,previewHeight=" + previewHeight);
					Log.e(TAG, "mScreenWidth=" + mScreenWidth + " ,mScreenHeight=" + mScreenHeight);
					mCamera.setParameters(parameters);// params 赋给摄像头


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void releaseCamera() {
		if (mCamera != null) {
			isPreviewed = false;
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	public boolean isPreviewed() {
		return isPreviewed;
	}

	public Camera getCamera() {
		return mCamera;
	}

	private Camera getCameraInstance() {
		Camera camera = null;

		try {
			camera = Camera.open();
			Log.e(TAG, "camera=" + camera);
			mCameraController.setCamera(camera);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return camera;
	}

	/**
	 * This is called immediately after the surface is first created.
	 * Implementations of this should start up whatever rendering code
	 * they desire.  Note that only one thread can ever draw into
	 * a {@link Surface}, so you should not draw into the Surface here
	 * if your normal rendering will be in another thread.
	 *
	 * @param holder The SurfaceHolder whose surface is being created.
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e(TAG, "surfaceCreated");
		startPreview();
	}

	/**
	 * This is called immediately after any structural changes (format or
	 * size) have been made to the surface.  You should at this point update
	 * the imagery in the surface.  This method is always called at least
	 * once, after {@link #surfaceCreated}.
	 *
	 * @param holder The SurfaceHolder whose surface has changed.
	 * @param format The new PixelFormat of the surface.
	 * @param width  The new width of the surface.
	 * @param height The new height of the surface.
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.e(TAG, "surfaceChanged");
		startPreview();
	}

	/**
	 * This is called immediately before a surface is being destroyed. After
	 * returning from this call, you should no longer try to access this
	 * surface.  If you have a rendering thread that directly accesses
	 * the surface, you must ensure that thread is no longer touching the
	 * Surface before returning from this function.
	 *
	 * @param holder The SurfaceHolder whose surface is being destroyed.
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.e(TAG, "surfaceDestroyed");
		releaseCamera();
	}
}
