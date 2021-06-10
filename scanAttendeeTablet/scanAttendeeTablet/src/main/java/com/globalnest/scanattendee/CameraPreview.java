/*
 * Barebones implementation of displaying camera preview.
 * 
 * Created by lisah0 on 2012-02-24
 */
package com.globalnest.scanattendee;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private PreviewCallback previewCallback;
    private AutoFocusCallback autoFocusCallback;

    @SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera,
                         PreviewCallback previewCb,
                         AutoFocusCallback autoFocusCb) {
        super(context);
        try {
			mCamera = camera;
			previewCallback = previewCb;
			autoFocusCallback = autoFocusCb;
			mHolder = getHolder();
     
			mHolder.addCallback(this);

			// deprecated setting, but required on Android versions prior to 3.0
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }



    public void surfaceCreated(SurfaceHolder holder) {
        try {
			// The Surface has been created, now tell the camera where to draw the preview.
			try {
				if(mCamera != null && holder != null)
			    mCamera.setPreviewDisplay(holder);
			} catch (Exception e) {
			    Log.d("DBG", "Error setting camera preview: " + e.getMessage());
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Camera preview released in activity
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        /*
         * If your preview can change or rotate, take care of those events here.
         * Make sure to stop the preview before resizing or  reformatting it.
         */
        if (mHolder.getSurface() == null){
          // preview surface does not exist
          return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        try {
            // Hard code camera surface rotation 90 degs to match Activity view in portrait
            
            mCamera.setDisplayOrientation(90);

            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(previewCallback);
            mCamera.startPreview();
            mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e){
            Log.d("DBG", "Error starting camera preview: " + e.getMessage());
        }
    }
}
