//  ScanAttendee Android
//  Created by Ajay on Sep 6, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * @author laxmanamurthy
 *
 */
public class TextFitTextView extends TextView{
	static final String TAG = "TextFitTextView";
	boolean fit = true;
	
	public TextFitTextView(Context context) {
		super(context);
	}

	public TextFitTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TextFitTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setFitTextToBox( Boolean fit ) {
		this.fit = fit;
	}
	
	protected void onDraw (Canvas canvas) {
		super.onDraw(canvas);
		//if (fit) _shrinkToFit();
	}
	
	public void _shrinkToFit(TextView txt) {
		
		int width = txt.getWidth();
		int height = txt.getHeight();
		int lines = txt.getLineCount();
		Rect r = new Rect();
		int y1 = txt.getLineBounds(0, r);
		int y2 = txt.getLineBounds(lines-1, r);
		
		float size = txt.getTextSize();
		if (y2 > height && size >= height) {
			txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP,(size - 1));
			_shrinkToFit(txt);
		}
		
	}
}
