package com.globalnest.classes;

import com.globalnest.classes.CircularProgressBar.OnProgressChangeListener;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 *
 * @author kalidoss rajendran
 */

public class RateTextCircularProgressBar extends FrameLayout implements OnProgressChangeListener {

	private CircularProgressBar mCircularProgressBar;
	private TextView mRateText;

	public RateTextCircularProgressBar(Context context) {
		super(context);
		init();
	}

	public RateTextCircularProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mCircularProgressBar = new CircularProgressBar(getContext());
		this.addView(mCircularProgressBar);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.gravity = Gravity.CENTER;
		mCircularProgressBar.setLayoutParams(lp);
		mRateText = new TextView(getContext());
		this.addView(mRateText);
		mRateText.setLayoutParams(lp);
		mRateText.setGravity(Gravity.CENTER);
		mRateText.setTextColor(Color.BLACK);
		mRateText.setTextSize(10);
		mCircularProgressBar.setOnProgressChangeListener(this);
	}


	public void setMax(int max) {
		mCircularProgressBar.setMax(max);
	}


	public void setProgress(int progress) {
		mCircularProgressBar.setProgress(progress);
	}

	public CircularProgressBar getCircularProgressBar() {
		return mCircularProgressBar;
	}

	public void setTextSize(float size) {
		mRateText.setTextSize(size);
	}


	public void setTextColor(int color) {
		mRateText.setTextColor(color);
	}

	@Override
	public void onChange(int duration, int progress, float rate) {
		mRateText.setText(String.valueOf((int) (rate * 100) + "%"));
	}
}