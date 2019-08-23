package com.pecuyu.customcamera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.pecuyu.customcamera.camera.CameraController;
import com.pecuyu.customcamera.camera.CameraPreview;

import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity {
	private static final String TAG = CameraActivity.class.getSimpleName();
	private SurfaceView mSurfaceView;
	private CameraPreview mCameraPreview;
	private Camera mCamera;
	private CameraController mCameraController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mSurfaceView = findViewById(R.id.id_preview);

		requestPermissionsIfNeeded();
	}

	private void requestPermissionsIfNeeded() {
		boolean cameraPermReq = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
		boolean storagePermReq = ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED;
		if (!cameraPermReq) {
			prepareCamera();
		}

		ArrayList<String> perms = new ArrayList<>();
		if (cameraPermReq) perms.add(Manifest.permission.CAMERA);
		if (storagePermReq){
			perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}

		if (!perms.isEmpty()) {
			ActivityCompat.requestPermissions(this, perms.toArray(new String[perms.size()]), 1);
		}
	}

	private void prepareCamera() {
		if (mCameraController == null) {
			mCameraController = new CameraController(getApplicationContext(), mSurfaceView.getHolder());
			mCameraController.start();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int i = 0; i < permissions.length; i++) {
			String permission = permissions[i];
			int grantResult = grantResults[i];

			switch (permission) {
				case Manifest.permission.CAMERA:
					if (grantResult == PackageManager.PERMISSION_GRANTED) {
						Log.e(TAG, "PERMISSION_GRANTED " + permission);
						prepareCamera();
					}
					break;
				case Manifest.permission.WRITE_EXTERNAL_STORAGE:
					if (grantResult == PackageManager.PERMISSION_GRANTED) {
						Log.e(TAG, "PERMISSION_GRANTED " + permission);

					}
					break;
			}
		}

	}

	public Camera getCameraInstance() {
		Camera camera = null;

		try {
			camera = Camera.open();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return camera;
	}

	public void Capture(View view) {
		mCameraController.takePicture(new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {

			}
		});
	}
}
