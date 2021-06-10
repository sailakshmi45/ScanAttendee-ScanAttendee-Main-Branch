//  ScanAttendee Android
//  Created by Ajay on Aug 10, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.scanattendee;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;

import com.globalnest.utils.Util;

/**
 * @author laxmanamurthy
 *
 */
public class AlertDialogActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		String msg = getIntent().getStringExtra(Util.INTENT_KEY_1);
		openSessionExpireAlert(msg);
	}
	
	public void openSessionExpireAlert(String msg){
		Util.setCustomAlertDialog(AlertDialogActivity.this);
		final String[] title_msg = msg.split(";");
		//Log.i("------------String Alrt Message----------",":"+title_msg.length+" : "+msg);
		Util.setCustomDialogImage(R.drawable.error);
		if(title_msg[0].equalsIgnoreCase("APEX_ERROR")){
			Util.txt_okey.setText("OK");
			Util.txt_dismiss.setVisibility(View.GONE);
		}else{
			Util.txt_okey.setText("LOGOUT");
			Util.txt_dismiss.setVisibility(View.VISIBLE);

		}
		
		Util.alert_dialog.setCancelable(false);
		
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				if(!title_msg[0].equalsIgnoreCase("APEX_ERROR")){
					boolean isdeleted = Util.db.isDBDeleted();
					if (isdeleted) {
						BaseActivity.DeleteFiles();
						Util.clearSharedPreference(Util.login_prefer);
						Util.clearSharedPreference(Util.eventPrefer);
						Util.clearSharedPreference(Util.first_login_pref);
						Util.clearSharedPreference(Util.selected_session_attedee_pref);
						Util.clearSharedPreference(Util.offset_pref);
						Util.clearSharedPreference(Util.dashboard_data_pref);
						Util.clearSharedPreference(Util.socket_device_pref);
						//Util.clearSharedPreference(Util.external_setting_pref);
						//Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
						Util.clearSharedPreference(Util.external_setting_pref);
						//Util.mApi = null;
						CookieManager.getInstance().removeAllCookie();
						Intent intent = new Intent(AlertDialogActivity.this, SplashActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intent);
						finish();
					}
				}else{
					finish();
				}
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				finish();

			}
		});
		if(!title_msg[0].equalsIgnoreCase("APEX_ERROR")){
			title_msg[1] = title_msg[1]+". Please logout and login again.";
		}
		Util.openCustomDialog("ERROR",title_msg[1]);
	}
}
