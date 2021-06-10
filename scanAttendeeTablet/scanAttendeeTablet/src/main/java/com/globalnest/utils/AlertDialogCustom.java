package com.globalnest.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globalnest.scanattendee.R;

public class AlertDialogCustom {

	private Intent startclass;
	private Intent endclass;
	private String title="",message="";
	private Activity mActivity;
	private boolean _isfinish;
	public Button btnCancel,btnOK;
	private View v;
	private Dialog dialog;
	private TextView txtTitle,txtMessage;
	private ImageView img_alert;
	private LinearLayout lay_top;

	public AlertDialogCustom(Activity activity)
	{
		mActivity=activity;
		init();
	}

	public void setParamenters(String title,String msg,Intent intent,Intent endintent,int btncount,boolean isfinish){
		this.title = title;
		this.message = msg;
		this.startclass = intent;
		this.endclass=endintent;
		this._isfinish=isfinish;
		if(btncount == 2){
			twoButton();
		}else{
			singleButton();
		}
	}

	public void init()
	{

		dialog=new Dialog(mActivity,R.style.MyCustomTheme);
		LayoutInflater inflater = (mActivity).getLayoutInflater();
		View v=inflater.inflate(R.layout.theme_alert_dialog, null);

		 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		 dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		dialog.setCancelable(false);
		dialog.setContentView(R.layout.theme_alert_dialog);
		    //builder.setIcon(R.drawable.);
			 btnCancel=(Button) dialog.findViewById(R.id.btnCancel);
			 btnOK=(Button) dialog.findViewById(R.id.btnOK);
			 txtTitle=(TextView) dialog.findViewById(R.id.txt_title);
			 txtMessage=(TextView) dialog.findViewById(R.id.txt_message);
			 img_alert=(ImageView) dialog.findViewById(R.id.img_alert);
			 lay_top=(LinearLayout)dialog.findViewById(R.id.lay_top);



	}

	public void show(){
		txtMessage.setText(message);
		if(!title.isEmpty())
		txtTitle.setText(title);
		dialog.show();
	}
	private void singleButton(){
		btnCancel.setVisibility(View.GONE);


		btnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if(startclass !=null){
					mActivity.startActivity(startclass);
				}else{
					if(_isfinish)
					mActivity.finish();
				}
			}
		});

	}



	private void twoButton(){
		//this.setTitle(title);
		//this.setMessage(message);




		btnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if(startclass !=null){
					mActivity.startActivity(startclass);
					if(_isfinish)
						mActivity.finish();
				}else{
					if(_isfinish)
						mActivity.finish();
					else{
						dialog.dismiss();
					}
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if (endclass != null) {
					mActivity.startActivity(endclass);
				if (_isfinish)
						mActivity.finish();
				} else {
					if (_isfinish)
						mActivity.finish();
					else {
						dialog.dismiss();
					}

				}
			}
		});
	}

	/*private void customButtons(){
		btnOK.setText("Now");
		btnCancel.setText("Not Now");
		
		
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					// TODO Auto-generated method stub
					dialog.dismiss();
					if(startclass !=null){
						mActivity.startActivity(startclass);
						//if(AppUtils.NullChecker(startclass.getStringExtra("IS_FROM")).equals("AddTicketActivity"))
						//context.finish();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				if(endclass !=null){
					if(AppUtils.NullChecker(endclass.getStringExtra("IS_FROM")).equals("AddTicketActivity"))
						context.finish();
					{
					//context.startActivity(endclass);
					//}
					//context.finish();
				}
			}
		});
	}*/

	public void setFirstButtonName(String name)
	{
		btnOK.setText(name);
	}


	public void setSecondButtonName(String name)
	{
		btnCancel.setText(name);
	}

	public void setAlertImage(int id,String type)
	{
		img_alert.setImageResource(id);


	}
	public void dismiss(){
		dialog.dismiss();
	}

}

