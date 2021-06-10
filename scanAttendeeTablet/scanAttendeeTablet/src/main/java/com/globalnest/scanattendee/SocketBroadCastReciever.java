//  ScanAttendee Android
//  Created by Ajay on Jan 8, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.ebay.redlasersdk.BarcodeScanActivity;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.scanattendee.socketmobile.ScannerSettingsApplication;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;
import com.google.gson.Gson;


/**
 * @author laxmanamurthy
 *
 */
public class SocketBroadCastReciever extends BroadcastReceiver{

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	/* private BaseActivity baseActivity;
	 private ExternalSettings ext_settings;
	 private Class<GlobalScanActivity> list_activity;*/
    /* public SocketBroadCastReciever(BaseActivity baseActivity){
    	this.baseActivity = baseActivity;
    	this.baseActivity.sfdcddetails = Util.db.getSFDCDDETAILS();
    	this.baseActivity.user_profile = Util.db.getAppUserProfile(this.baseActivity.sfdcddetails.user_id);
    	this.baseActivity.checked_in_eventId = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);		
    	Util.external_setting_pref = this.baseActivity.getSharedPreferences(Util.EXTERNAL_PREF,Context.MODE_PRIVATE);
    	if(!Util.external_setting_pref.getString(this.baseActivity.sfdcddetails.user_id+baseActivity.checked_in_eventId, "").isEmpty()){
    	    ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(this.baseActivity.sfdcddetails.user_id+baseActivity.checked_in_eventId, ""), ExternalSettings.class);	
    	}
     }*/


	/*@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void onCaptureDeviceAvailabilityChanged(DeviceAvailabilityEvent event) {
		DeviceClient device = event.getDevice();
		int eventType = event.getType();

		if (eventType == DeviceAvailabilityEvent.TYPE_REMOVAL) {
			// No device
		} else if (eventType == DeviceAvailabilityEvent.TYPE_ARRIVAL || eventType == DeviceAvailabilityEvent.TYPE_CLOSE) {
			// Device is closed
		} else if (eventType == DeviceAvailabilityEvent.TYPE_OPEN || eventType == DeviceAvailabilityEvent.TYPE_OWNERSHIP_LOST) {
			// Device open, but no ownership
		} else if (eventType == DeviceAvailabilityEvent.TYPE_OWNERSHIP_OBTAINED) {
			// Got device ownership
		}
	}*/
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		try {
			if (intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.NOTIFY_SCANNER_ARRIVAL)) {

				Util.socket_device_pref.edit().putBoolean(Util.SOCKET_DEVICE_CONNECTED, true).commit();
				//String scanner_name = intent.getStringExtra(ScannerSettingsApplication.EXTRA_DEVICENAME);
				String scanner_name ="";
						Toast.makeText(context, "Paired Successfully with "+scanner_name, Toast.LENGTH_LONG).show();
				Util.socket_device_pref.edit().putString(Util.SOCKET_DEVICE_NAME, scanner_name).commit();
				if(BaseActivity.softscannerdialog != null){
					BaseActivity.softscannerdialog.dismiss();
				}
				if(ExternalSettingsActivity.img_scanner != null){
					ExternalSettingsActivity.img_scanner.setImageResource(R.drawable.green_circle_1);
				}
				if(ExternalSettingsActivity.txt_on_off_scanner != null){
					ExternalSettingsActivity.txt_on_off_scanner.setText("Connected");
					ExternalSettingsActivity.txt_on_off_scanner.setTextColor(context.getResources().getColor(R.color.orange_bg));
				}

				if(ExternalSettingsActivity.txt_scanner_name != null){
					ExternalSettingsActivity.txt_scanner_name.setText("Connected to "+scanner_name);
				}

				if(BaseActivity.img_scanner_base != null){
					BaseActivity.img_scanner_base.setImageResource(R.drawable.green_circle_1);
				}

					/*MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.scannerconnected);
					mediaPlayer.start();*/
				playSound(context,R.raw.scannerconnected);
				ScannerSettingsApplication.getInstance().getBatteryLevel();
			} else if (intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.NOTIFY_ERROR_MESSAGE)) {
					/*String text = intent.getStringExtra(ScannerSettingsApplication.EXTRA_ERROR_MESSAGE);
					Toast.makeText(context, text+". Please pair again.", Toast.LENGTH_LONG).show();*/
				if(BaseActivity.img_scanner_base != null){
					BaseActivity.img_scanner_base.setImageResource(R.drawable.red_circle_1);
				}

				Util.socket_device_pref.edit().putBoolean(Util.SOCKET_DEVICE_CONNECTED, false).commit();
			}else if (intent.getAction().equalsIgnoreCase(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
				if(state == BluetoothAdapter.STATE_ON){
					//Toast.makeText(context, "Bluetooth Turned ON.", Toast.LENGTH_LONG).show();
					if(ExternalSettingsActivity.btn_scanner_setting != null){
						ExternalSettingsActivity.btn_scanner_setting.setVisibility(View.VISIBLE);
					}
					ScannerSettingsApplication.getInstance().onCreate();
					ScannerSettingsApplication.getInstance().increaseViewCount();
					//unPairSocketDevices();
				}else{
					if(ExternalSettingsActivity.btn_scanner_setting != null){
						ExternalSettingsActivity.btn_scanner_setting.setVisibility(View.GONE);
					}
				}
			}else if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.NOTIFY_SCANNER_REMOVAL)){

				Util.socket_device_pref.edit().putBoolean(Util.SOCKET_DEVICE_CONNECTED, false).commit();
				Util.socket_device_pref.edit().putString(Util.SOCKET_DEVICE_NAME, "").commit();
				Util.socket_device_pref.edit().putString(Util.SOCKET_DEVICE_BATTERY_LEVEL, "100").commit();

				ScannerSettingsApplication.getInstance().decreaseViewCount();
				ScannerSettingsApplication.getInstance().increaseViewCount();


				String scanner_name = intent.getStringExtra(ScannerSettingsApplication.EXTRA_DEVICENAME);
				Toast.makeText(context, "Unpaired Successfully with "+scanner_name, Toast.LENGTH_LONG).show();

				if(BaseActivity.softscannerdialog != null){
					BaseActivity.softscannerdialog.dismiss();
				}
				if(BaseActivity.img_scanner_base != null){
					BaseActivity.img_scanner_base.setImageResource(R.drawable.red_circle_1);
				}

				if(ExternalSettingsActivity.txt_on_off_scanner != null){
					ExternalSettingsActivity.txt_on_off_scanner.setText("Not Connected");
					ExternalSettingsActivity.txt_on_off_scanner.setTextColor(context.getResources().getColor(R.color.gray_color));
				}
				if (ExternalSettingsActivity.txt_scanner_name != null) {
					//String scanner_name = intent.getStringExtra(ScannerSettingsApplication.EXTRA_DEVICENAME);
					ExternalSettingsActivity.txt_scanner_name.setText("Scanners");
				}

				if(ExternalSettingsActivity.txt_battery_level != null){
					ExternalSettingsActivity.txt_battery_level.setVisibility(View.GONE);
				}
				if(BaseActivity.img_scanner_base != null){
					BaseActivity.img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
				}
				    /*MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.scannerdisconnected);
					mediaPlayer.start();*/
				playSound(context,R.raw.scannerdisconnected);
			}else if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.NOTIFY_DATA_ARRIVAL)){
				//ScannerSettingsApplication.getInstance().getBatteryLevel();
				char[] data = intent.getCharArrayExtra(ScannerSettingsApplication.EXTRA_DECODEDDATA);
				//Toast.makeText(context, data.toString(), Toast.LENGTH_LONG).show();
				//Log.i("-------------Scanned Data----------",":"+value.toString().trim());
				ScannerSettingsApplication.getInstance().getBatteryLevel();
				callCheckinTask(data,context);
					/*if(Util.db.getGroupCount(BaseActivity.checkedin_event_record.Events.Id) == 0){
						showScannedTicketsAlert(context,"Please Buy at least one scanattendee ticket to scan session.",false);
					}else if(Util.db.getSwitchedOnScanItem(BaseActivity.checkedin_event_record.Events.Id).isEmpty()){
						showScannedTicketsAlert(context,"Please TurnON at least one session for scanning.",true);
					}else{

					}*/

			}else if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.GET_BATTERYLEVEL_COMPLETE)){
				//Toast.makeText(context, intent.getStringExtra(ScannerSettingsApplication.EXTRA_BATTERYLEVEL)+"", Toast.LENGTH_LONG).show();
				String battery = intent.getStringExtra(ScannerSettingsApplication.EXTRA_BATTERYLEVEL);
				Util.socket_device_pref.edit().putString(Util.SOCKET_DEVICE_BATTERY_LEVEL, battery).commit();
				if(ExternalSettingsActivity.txt_battery_level != null){
					String text = "<font color=#8D8D8D>Battery Level:</font> <font color=#DF6B1E>"+battery+"</font>";
					ExternalSettingsActivity.txt_battery_level.setVisibility(View.VISIBLE);
					ExternalSettingsActivity.txt_battery_level.setText(Html.fromHtml(text));
				}
			}/*else if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.GET_FRIENDLYNAME_COMPLETE)){
					Toast.makeText(context, intent.getStringExtra(ScannerSettingsApplication.EXTRA_DEVICENAME), Toast.LENGTH_LONG).show();
				}else if(intent.getAction().equalsIgnoreCase(ScannerSettingsApplication.GET_SOFTSCAN_STATUS_COMPLETE)){
					Toast.makeText(context, intent.getStringExtra(ScannerSettingsApplication.GET_SOFTSCAN_STATUS_COMPLETE), Toast.LENGTH_LONG).show();
				}*/

		} catch (Exception e) {

			e.printStackTrace();
			//Log.i("--------------------Soft Scanner Exception------------",":"+e.getMessage());
		}
	}


	public static synchronized void callCheckinTask(char[] values,Context context) {
		/*ScannedTicketsCheckinTask task = new ScannedTicketsCheckinTask(values);
		task.execute();*/

		SFDCDetails sfdc_details = Util.db.getSFDCDDETAILS();
		ExternalSettings externalSettings = new ExternalSettings();
		if (!Util.external_setting_pref.getString(sfdc_details.user_id + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
			externalSettings = new Gson().fromJson(
					Util.external_setting_pref.getString(sfdc_details.user_id + BaseActivity.checkedin_event_record.Events.Id, ""),
					ExternalSettings.class);
		}
		String scanned_value = new String(values);
		Intent global_intent = new Intent();
		/*try {
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
				if (!BaseActivity.isOrderScanned(scanned_value.toString().trim())) {
					Toast.makeText(context,"Please Scan Only Order QR Code",Toast.LENGTH_LONG).show();
					//releaseCamera();
					//scanStart();
					return;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}*/
		if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
			if (getAttendeeCursor(scanned_value.toString().trim()).getCount() == 1) {
				global_intent = new Intent(context, TransperantGlobalScanActivity.class);
				global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
				global_intent.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
				global_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			} else {
				global_intent = new Intent(context, GlobalScanActivity.class);
				global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
				global_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
		} else {
			if (externalSettings.quick_checkin) //&& !BaseActivity.isOrderScanned(scanned_value.toString().trim())
			{
				global_intent = new Intent(context, TransperantGlobalScanActivity.class);
				global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
				global_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(global_intent);

			} else {
				//else if (externalSettings.quick_checkin && (getAttendeeCursor(scanned_value.toString().trim()).getCount()) == 1) {
				global_intent = new Intent(context, GlobalScanActivity.class);
				global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
				global_intent.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
				global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				global_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(global_intent);
			}
			/*if (externalSettings.quick_checkin && !BaseActivity.isOrderScanned(scanned_value.toString().trim())) {
				global_intent = new Intent(context, TransperantGlobalScanActivity.class);
			} else if (externalSettings.quick_checkin && (getAttendeeCursor(scanned_value.toString().trim()).getCount()) == 1) {
				global_intent = new Intent(context, TransperantGlobalScanActivity.class);
				global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
				global_intent.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
				global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			} else {
				global_intent = new Intent(context, GlobalScanActivity.class);
			}
		}

		//Intent global_intent = new Intent(context, GlobalScanActivity.class);
		global_intent.putExtra(Util.SCANDATA, values);
		global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(global_intent);*/
		/*if (externalSettings.quick_checkin) //&& !BaseActivity.isOrderScanned(scanned_value.toString().trim())
		{
			global_intent = new Intent(context, TransperantGlobalScanActivity.class);
			global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());

		}
		else  {
			//else if (externalSettings.quick_checkin && (getAttendeeCursor(scanned_value.toString().trim()).getCount()) == 1) {
			global_intent = new Intent(context, GlobalScanActivity.class);
			global_intent.putExtra(Util.SCANDATA, scanned_value.toString().trim().toCharArray());
			global_intent.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
			global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		} *//*else {
				global_intent = new Intent(context, GlobalScanActivity.class);
			}*//*
	}*/
		}
	}

	public  void playSound(Context ctx,int resource_id){
		MediaPlayer p = MediaPlayer.create(ctx, resource_id);
		p.start();
		p.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mp.release();
			}
		});
	}
	public void showScannedTicketsAlert(final Context ctx, String msg, final boolean goto_settings){
		Util.setCustomAlertDialog(ctx);
		Util.setCustomDialogImage(R.drawable.alert);
		Util.txt_okey.setText("OK");
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.alert_dialog.setCancelable(false);
		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Util.alert_dialog.dismiss();
				if(goto_settings){
					Intent intent = new Intent(ctx,ScannedTicketSettings.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					ctx.startActivity(intent);
				}
			}
		});
		if(goto_settings){
			Util.txt_dismiss.setVisibility(View.VISIBLE);
			Util.txt_okey.setText("SETTINGS");
        	/*MediaPlayer player = MediaPlayer.create(BaseActivity.this, R.raw.enable_tickets);
        	player.start();*/
			playSound(ctx,R.raw.enable_tickets);
		}
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Util.alert_dialog.dismiss();
			}
		});
		Util.openCustomDialog("ALERT",msg);
	}

	public static  Cursor getAttendeeCursor(String orderId){
		String whereClause = "";
		Cursor attendee_cursor=null;
		try{
			if (BaseActivity.isOrderScanned(orderId)) {
				whereClause = " where Event_Id='" + BaseActivity.staticCheckedInEventId + "' AND Order_Id='" + orderId.trim() + "'";
				attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
			} else {
				whereClause = " where Event_Id='" + BaseActivity.staticCheckedInEventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
				attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
			}
			attendee_cursor.moveToFirst();

		}catch (Exception e){
			e.printStackTrace();
		}
		return attendee_cursor;
	}

	/*private void checkForSelfCheckinWithSingleAttendee(String orderId) {
		try {
			Cursor attendee_cursor;
			if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {

				if (!BaseActivity.isOrderScanned(orderId)) {
					((BaseActivity)BaseActivity.baseContext).showMessageAlert("Please scan only order qrcodes.",false);
					//releaseCamera();
					//scanStart();
					return;
				}

				String whereClause = "";
				if (BaseActivity.isOrderScanned(orderId)) {
					whereClause = " where Event_Id='" + ((BaseActivity)BaseActivity.baseContext).checked_in_eventId + "' AND Order_Id='" + orderId.trim() + "'";
					attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
				} else {
					whereClause = " where Event_Id='" + ((BaseActivity)BaseActivity.baseContext).checked_in_eventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
					attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
				}

				if (attendee_cursor.getCount() == 1) {
					FrameLayout print_badge = (FrameLayout) ((BaseActivity)BaseActivity.baseContext).linearview.findViewById(R.id.frame_attdetailqrcodebadge);
					FrameLayout frame_transparentbadge = (FrameLayout) ((BaseActivity)BaseActivity.baseContext).linearview.findViewById(R.id.frame_transparentbadge);
					attendee_cursor.moveToFirst();
					String attendeeid = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
					PrintAndCheckin printT = new PrintAndCheckin();
					PrintDetails printDetails = new PrintDetails();
					printDetails.attendeeId = attendeeid;
					printDetails.checked_in_eventId = ((BaseActivity)BaseActivity.baseContext).checked_in_eventId;
					printDetails.frame_transparentbadge = frame_transparentbadge;
					printDetails.order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
					printDetails.print_badge = print_badge;
					printDetails.sfdcddetails = ((BaseActivity)BaseActivity.baseContext).sfdcddetails;
					*//**these params are required when any order/badge will scanned**//*
						*//**//*printDetails.attendeeWhereClause=whereClause;		   	 *//**//*
						*//**//*printDetails.isOrderScaneed=BaseActivity.isOrderScanned(orderId);			  	 *//**//*
						*//**//*printDetails.qrCode=orderId;							 *//**//*
					*//**---------------------------END-----------------------------**//*
					printT.doSaveAndPrint(((BaseActivity)BaseActivity.baseContext), printDetails);
				}else{
					//	isScannedItemNotopened();
					Intent i = new Intent(BaseActivity.baseContext, GlobalScanActivity.class);
					i.putExtra(Util.SCANDATA, orderId.toCharArray());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					((BaseActivity)BaseActivity.baseContext).startActivity(i);
					//finish();
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}*/
}
