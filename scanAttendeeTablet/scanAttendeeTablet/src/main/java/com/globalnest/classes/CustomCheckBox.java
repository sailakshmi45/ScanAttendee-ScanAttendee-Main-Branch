/*
 * Created by Hanks
 * Copyright (c) 2015 NaShangBan. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.globalnest.classes;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by Administrator on 2015/5/14.
 */
public class CustomCheckBox extends View {

    private Paint mCirclePaint;
    private Paint mCorrectPaint;
    private int radius;                    
    private int width, height;             
    private int cx, cy;                    
    private float[] points = new float[6]; 
    private float correctProgress;
    private float downY;
    private boolean isChecked;
    private boolean toggle;
    private boolean isAnim;
    private int animDurtion = 150;

    private OnCheckedChangeListener listener;
    private int unCheckColor = Color.GRAY;
    private int circleColor = Color.GRAY;

    public CustomCheckBox(Context context) {
        this(context, null);
    }

    public CustomCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public CustomCheckBox(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * åˆ�å§‹åŒ–
     * @param context
     */
    private void init(Context context) {

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.RED);
        mCirclePaint.setStyle(Paint.Style.FILL);

        mCorrectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCorrectPaint.setColor(Color.WHITE);
        mCorrectPaint.setStyle(Paint.Style.FILL);
        mCorrectPaint.setStrokeWidth(dip2px(context, 2));
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChecked) {
                    hideCorrect();
                } else {
                    showCheck();
                }
            }
        });
    }

    /**
     * è®¾ç½®å½“å‰�é€‰ä¸­çŠ¶æ€�
     * @param checked
     */
    public void setChecked(boolean checked){
        if (isChecked && !checked) {
            hideCorrect();
        } else if(!isChecked && checked) {
            showCheck();
        }
    }

    /**
     * è¿”å›žå½“å‰�é€‰ä¸­çŠ¶æ€�
     * @return
     */
    public boolean isChecked(){
        return isChecked;
    }

    /**
     * ç¡®å®šå°ºå¯¸å��æ ‡
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = width = Math.min(w - getPaddingLeft() - getPaddingRight(),h - getPaddingBottom() - getPaddingTop());
        cx = w / 2;
        cy = h / 2;

        float r = height / 2f;
        points[0] = r / 2f + getPaddingLeft();
        points[1] = r + getPaddingTop();

        points[2] = r * 5f / 6f + getPaddingLeft();
        points[3] = r + r / 3f + getPaddingTop();

        points[4] = r * 1.5f +getPaddingLeft();
        points[5] = r - r / 3f + getPaddingTop();
        radius = (int) (height * 0.125f);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        float f = (radius -height * 0.125f) / (height * 0.5f); //å½“å‰�è¿›åº¦
        mCirclePaint.setColor(evaluate(f,unCheckColor,circleColor));
        canvas.drawCircle(cx, cy, radius, mCirclePaint); //ç”»åœ†

        //ç”»å¯¹å�·
        if(correctProgress>0) {
            if(correctProgress<1/3f) {
                float x = points[0] + (points[2] - points[0]) * correctProgress;
                float y = points[1] + (points[3] - points[1]) * correctProgress;
                canvas.drawLine(points[0], points[1], x, y, mCorrectPaint);
            }else {
                float x = points[2] + (points[4] - points[2]) * correctProgress;
                float y = points[3] + (points[5] - points[3]) * correctProgress;
                canvas.drawLine(points[0], points[1], points[2], points[3], mCorrectPaint);
                canvas.drawLine(points[2], points[3], x,y, mCorrectPaint);
            }
        }
    }


    /**
     * è®¾ç½®åœ†çš„é¢œè‰²
     * @param color
     */
    public void setCircleColor(int color){
        circleColor = color;
    }

    /**
     * è®¾ç½®å¯¹å�·çš„é¢œè‰²
     * @param color
     */
    public void setCorrectColor(int color){
        mCorrectPaint.setColor(color);
    }

    /**
     * è®¾ç½®æœªé€‰ä¸­æ—¶çš„é¢œè‰²
     * @param color
     */
    public void setUnCheckColor(int color){
        unCheckColor = color;
    }

    private int evaluate(float fraction, int startValue, int endValue) {
        int startInt = startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return ((startA + (int) (fraction * (endA - startA))) << 24)
                | ((startR + (int) (fraction * (endR - startR))) << 16)
                | ((startG + (int) (fraction * (endG - startG))) << 8)
                | ((startB + (int) (fraction * (endB - startB))));
    }

    /**
     * å¤„ç�†è§¦æ‘¸äº‹ä»¶è§¦å�‘åŠ¨ç”»
     */
    /*private class OnChangeStatusListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            //Log.i("Touch","Touch");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dy = event.getRawY() - downY;
                    if (Math.abs(dy) >= 0) { //æ»‘è¿‡ä¸€å�Šè§¦å�‘
                        toggle = true;
                    } else {
                        toggle = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (toggle) {
                        if (isChecked) {
                            hideCorrect();
                        } else {
                            showCheck();
                        }
                    }
                    break;
            }
            return true;
        }
    }*/

    private void showUnChecked() {
        if (isAnim) {
            return;
        }

        isAnim = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(animDurtion);
        va.setInterpolator(new LinearInterpolator());
        va.start();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value =  (Float) animation.getAnimatedValue(); // 0f ~ 1f
                radius = (int) ((1 - value) * height * 0.375f + height * 0.125f);
                if (value >= 1) {
                    isChecked = false;
                    isAnim = false;
                    if(listener!=null){
                        listener.onCheckedChanged(CustomCheckBox.this,false);
                    }
                }
                invalidate();
            }
        });
    }

    private void showCheck() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(animDurtion);
        va.setInterpolator(new LinearInterpolator());
        va.start();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue(); // 0f ~ 1f
                radius = (int) (value * height * 0.37f + height * 0.125f);
                if (value >= 1) {
                    isChecked = true;
                    isAnim = false;
                    if(listener!=null){
                        listener.onCheckedChanged(CustomCheckBox.this,true);
                    }
                    showCorrect();
                }
                invalidate();
            }
        });
    }

    private void showCorrect() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(animDurtion);
        va.setInterpolator(new LinearInterpolator());
        va.start();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue(); // 0f ~ 1f
                correctProgress = value;
                invalidate();
                if(value>=1){
                    isAnim = false;
                }
            }
        });
    }
    private void hideCorrect() {
        if (isAnim) {
            return;
        }
        isAnim = true;
        ValueAnimator va = ValueAnimator.ofFloat(0, 1).setDuration(animDurtion);
        va.setInterpolator(new LinearInterpolator());
        va.start();
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue(); // 0f ~ 1f
                correctProgress = 1-value;
                invalidate();
                if(value>=1){
                    isAnim = false;
                    showUnChecked();
                }
            }
        });
    }
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener){
        this.listener = listener;
    }
    public interface OnCheckedChangeListener{
        void onCheckedChanged(View buttonView, boolean isChecked);
    }

    /**
     * æ ¹æ�®æ‰‹æœºçš„åˆ†è¾¨çŽ‡ä»Ž dp çš„å�•ä½� è½¬æˆ�ä¸º px(åƒ�ç´ )
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * æ ¹æ�®æ‰‹æœºçš„åˆ†è¾¨çŽ‡ä»Ž px(åƒ�ç´ ) çš„å�•ä½� è½¬æˆ�ä¸º dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
