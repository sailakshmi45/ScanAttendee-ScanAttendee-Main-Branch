//  ScanAttendee Android
//  Created by Ajay
//  This class is used for moving splash background image
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MovingBackGround extends SurfaceView implements
		SurfaceHolder.Callback {
	private Bitmap backGround;
	int width=0, height =0;
	public MovingBackGround(Context context) {
		super(context);
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		 width = displayMetrics.widthPixels;
		 height = displayMetrics.heightPixels;
		
		backGround = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.splash); 
		setWillNotDraw(false);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		doDrawRunning(canvas);
		invalidate();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	/**
	 * Draws current state of the game Canvas.
	 */

	private int mBGFarMoveX = 0;
	private int mBGNearMoveX = 0;

	private void doDrawRunning(Canvas canvas) {

	
		
		try {
			// decrement the far background
			mBGFarMoveX = mBGFarMoveX - 1;

			// decrement the near background
			mBGNearMoveX = mBGNearMoveX - 4;

			// calculate the wrap factor for matching image draw
			int newFarX = backGround.getWidth() - (-mBGFarMoveX);

			// if we have scrolled all the way, reset to start
			if (newFarX <= 0) {
				mBGFarMoveX = 0;
				
				Bitmap scaled = Bitmap.createScaledBitmap(backGround, backGround.getWidth(), height, false);
				canvas.drawBitmap(scaled, mBGFarMoveX, 0, null);
				//canvas.drawBitmap(backGround, mBGFarMoveX, 0, null);

			} else {
				Bitmap scaled = Bitmap.createScaledBitmap(backGround, backGround.getWidth(), height, false);
				canvas.drawBitmap(scaled, mBGFarMoveX, 0, null);
				canvas.drawBitmap(scaled, newFarX, 0, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
