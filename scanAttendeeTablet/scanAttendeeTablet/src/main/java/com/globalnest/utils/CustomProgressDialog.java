package com.globalnest.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.globalnest.scanattendee.R;


public  class CustomProgressDialog extends ProgressDialog {

private AnimationDrawable animation;

public static ProgressDialog ctor(Context context) {

 CustomProgressDialog dialog = new CustomProgressDialog (context);

 dialog.setIndeterminate(true);

 dialog.setCancelable(false);

  return dialog;
  }
  public CustomProgressDialog (Context context) {

    super(context);
  }

  public CustomProgressDialog (Context context, int theme) {

    super(context, theme);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    
   super.onCreate(savedInstanceState);

    setContentView(R.layout.ustom_progress_dialog);

    ImageView animate = (ImageView) findViewById(R.id.animation);

    animate.setBackgroundResource(R.drawable.custom_animation_drawable);

    animation = (AnimationDrawable) animate.getBackground();
  }

  @Override
  public void show() {

    super.show();

    animation.start();
  }

  @Override
  public void dismiss() {

    super.dismiss();

    animation.stop();

  }
}
