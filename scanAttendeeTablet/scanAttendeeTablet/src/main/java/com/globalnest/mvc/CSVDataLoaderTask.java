//  ScanAttendee Android
//  Created by Ajay on Jan 5, 2017
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.util.ArrayList;
import java.util.List;

import com.globalnest.database.DBFeilds;
import com.globalnest.objects.ScannedItems;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * @author laxmanamurthy
 *
 */
public class CSVDataLoaderTask extends AsyncTask<String, Integer, Boolean>{
	
	private List<String[]> csv_data = new ArrayList<>();
	private Context ctx;
	private ProgressDialog _dialog;
	private SFDCDetails sfdcddetails;
	public CSVDataLoaderTask(Context ctx,List<String[]> csv_data) {
		// TODO Auto-generated constructor stub
		this.csv_data = csv_data;
		this.ctx = ctx;
		this._dialog = new ProgressDialog(ctx);
		this._dialog.setMessage("Loading CSV Data...");
		sfdcddetails = Util.db.getSFDCDDETAILS();
	}
	
	protected void onPreExecute(){
		super.onPreExecute();
		_dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		_dialog.setCancelable(false);
		_dialog.setMax(csv_data.size()-1);
		_dialog.setProgress(0);
		_dialog.show();
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected Boolean doInBackground(String... params) {
		// TODO Auto-generated method stub
		return importCSVDate(csv_data);
	}
	public void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		_dialog.setProgress(values[0]);
	}
	
	protected void onPostExecute(Boolean result){
		super.onPostExecute(result);
		_dialog.dismiss();
		showMessageAlert("Data Imported Successfully, Please check offline tab in Attendees page.");
	}

	private boolean importCSVDate(List<String[]> csv_data){
		List<ScannedItems> scannedItem=Util.db.getSwitchedOnScanItem(BaseActivity.checkedin_event_record.Events.Id);
		String Item_pool_ids = ITransaction.EMPTY_STRING;
		for(int i=0;i<scannedItem.size();i++){
			Item_pool_ids = Item_pool_ids+scannedItem.get(i).BLN_Item_Pool__c;
			if (i != (scannedItem.size() - 1)) {
				Item_pool_ids = Item_pool_ids+",";
			}
		}

		int i = 0,j=0;
		for(String[] row : csv_data){
			AppUtils.displayLog("------------CSV Data Column 1----------",":"+row[0]);
			AppUtils.displayLog("------------CSV Data Column 2----------",":"+row[1]);
			if(i == 0){
				i++;
				continue;
			}
			
			OfflineScansObject offlineObj=new OfflineScansObject();
			offlineObj.badge_id=row[0].replace("\"", "");
			offlineObj.badge_status=DBFeilds.STATUS_OFFLINE;
			offlineObj.checkin_status="";
			offlineObj.item_pool_id=Item_pool_ids;
			offlineObj.event_id=BaseActivity.checkedin_event_record.Events.Id;
			offlineObj.scan_date_time=Util.getCSVDateFormat(row[1].trim().replace("\"", "")+" "+row[2].trim().replace("\"", ""));
			offlineObj.user_id=sfdcddetails.user_id;
			offlineObj.scan_group_id = Util.db.getSwitchedONGroupId(BaseActivity.checkedin_event_record.Events.Id);
			offlineObj.name = Util.db.getAttendeeName(row[0]);
			offlineObj.scandevicemode =Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+BaseActivity.checkedin_event_record.Events.Id,"");
			Util.db.InsertAndUpdateOfflineScans(offlineObj);
			onProgressUpdate(j++);
		}
		return true;
	}
	
	public void showMessageAlert(String msg) {
		
		Util.setCustomAlertDialog(this.ctx);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.alert);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();

			}
		});
		Util.openCustomDialog("Alert", msg);
	}
}
