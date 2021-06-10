//  ScanAttendee Android
//  Created by Ajay on Nov 30, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.scanattendee;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class OfflineDataSyncActivty extends BaseActivity{
	
	private ListView list_view;
	private List<OfflineScansObject> offlineScans = new ArrayList<OfflineScansObject>();
	private int total_count = 0,batch_count = 0,MAX_RECORDS = 500;
	private ArrayList<OfflineStatus> offline_status_list = new ArrayList<>();
	private SyncingAdapter adapter = new SyncingAdapter();
	private String manual_sync = ITransaction.EMPTY_STRING;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.offline_batchsync_layout);
		manual_sync = this.getIntent().getStringExtra(Util.INTENT_KEY_1);
		checked_in_eventId = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);
		
		if(!NullChecker(manual_sync).equalsIgnoreCase(SessionListActivity.class.getName())){
			total_count = Util.db.getTotalOfflineScanCount(checked_in_eventId);
		}else{
			total_count = Util.db.getTotalOfflineScanCount(checked_in_eventId,true);
		}
				
		
		txt_title.setText("Offline Syncing ("+total_count+")");
		batch_count = (int)Math.ceil(total_count * 1.0/MAX_RECORDS);
		AppUtils.displayLog("--------------Batch Sync----------",":"+batch_count);

		for(int i =0 ;i<batch_count;i++){
			OfflineStatus status = new OfflineStatus();
			
			if(i == 0){
				status.showProgress = true;
			}
			if(((i+1) * MAX_RECORDS) > total_count){
				status.total = total_count - ((i*MAX_RECORDS));
				status.progress_name = ((i*MAX_RECORDS)+1)+" to "+total_count+" is Syncing...";
			}else{
				status.progress_name = ((i*MAX_RECORDS)+1)+" to "+((i+1) * MAX_RECORDS)+" is Syncing...";
				status.total = MAX_RECORDS;
			}
			status.invalid = 0;
			status.valid = 0;
			offline_status_list.add(status);
		}
		list_view.setAdapter(adapter);

		showCustomToast(this, "Batch Syncing is started please wait...", R.drawable.img_like, R.drawable.toast_greenroundededge, false);
		batchSync();
		
       back_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(batch_count > 0){
					showSyncAlert("Processing your offline records, please wait...!");
				}else{
					Intent i = new Intent();
					setResult(2017, i);
					finish();

				}
				
			} 
		});
	}

	private void batchSync(){
		offlineScans.clear();
		String where_clause = ITransaction.EMPTY_STRING;
		
		if(!NullChecker(manual_sync).equalsIgnoreCase(SessionListActivity.class.getName())){
			where_clause = " Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"+ checked_in_eventId + "' AND "+DBFeilds.OFFLINE_BADGE_STATUS+" != '"+DBFeilds.STATUS_INVALID+"' LIMIT "+MAX_RECORDS;
		}else{
			where_clause = " Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"+ checked_in_eventId +"' LIMIT "+MAX_RECORDS;
		}
		offlineScans = Util.db.getOfflineScans(where_clause, false);
		AppUtils.displayLog("-------------Event Id Offline Scans-----------",":"+isOnline()+ " "+offlineScans.size());

		if (isOnline() && offlineScans.size() > 0) {
			//Log.i("---OfflineSyncBrosdCast----", " ---in the Service---");
			doRequest();
		}
	
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		String _url = sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL + getValues();
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
		//String message =  offlineScans.size() + " records are syncing out of "+total_count  + " \n Please wait..."; 
		postMethod = new HttpPostData("Hide", _url, getJsonBody(), access_token, OfflineDataSyncActivty.this);
		postMethod.execute();
		/*ServiceCall postdataMethod = new ServiceCall(_url, getJsonBody().toString(), access_token);
		postdataMethod.execute();*/
	}
	
	private String getValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("scannedby", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("eventId", checked_in_eventId));
		values.add(new BasicNameValuePair("source", "Offline"));
		values.add(new BasicNameValuePair("DeviceType", Util.getDeviceNameandAppVersion()));
		values.add(new BasicNameValuePair("checkin_only",String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))));
		return AppUtils.getQuery(values);
	}
	
	private String getJsonBody() {
		JSONArray ticketarray = new JSONArray();
		for (OfflineScansObject scanObj : offlineScans) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("TicketId", scanObj.badge_id);
				obj.put("sTime", Util.getOfflineSyncServerFormat(scanObj.scan_date_time));
				obj.put("isCHeckIn", true);
				obj.put("freeSemPoolId", scanObj.item_pool_id);
				obj.put("device", "ANDROID");
				obj.put("sessionid", scanObj.scan_group_id);
				obj.put("scandevicemode",scanObj.scandevicemode);
				//obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
				ticketarray.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ticketarray.toString();
	}


	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
	try {
			if (!isValidResponse(response)) {
				openSessionExpireAlert(errorMessage(response));
			} else {
				// toast.cancel();
				Gson gson = new Gson();
				OfflineSyncResController offlineResponse = gson.fromJson(response, OfflineSyncResController.class);
				/*JsonReader reader = new JsonReader(new StringReader(response));
				reader.setLenient(true);
				OfflineSyncResController offlineResponse = gson.fromJson(String.valueOf(reader), OfflineSyncResController.class);
*/
				if (offlineResponse != null) {
					int invalid_scans = 0;
					HashMap<String, String> duplicate_barcodes = new HashMap<>();
					for (OfflineSyncFailuerObject syncObj : offlineResponse.FailureTickets) {
						AppUtils.displayLog("-------------Offline Invalid-----------------", ":" + syncObj.tbup.ticketId + " : "
								+ isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets));
						if (isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets)) {
							continue;
						} else if (duplicate_barcodes.containsKey(syncObj.tbup.ticketId)) {
							if (duplicate_barcodes.get(syncObj.tbup.ticketId)
									.equalsIgnoreCase(syncObj.tbup.sessionid)) {
								continue;
							} else {
								duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
								invalid_scans++;
							}
						} else {
							duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
							invalid_scans++;
						}

						OfflineScansObject scanObject = new OfflineScansObject();
						scanObject.badge_id = syncObj.tbup.ticketId;
						scanObject.badge_status = DBFeilds.STATUS_INVALID;
						scanObject.event_id = checked_in_eventId;
						scanObject.item_pool_id = syncObj.tbup.freeSemPoolId;
						scanObject.scan_group_id = syncObj.tbup.sessionid;
						scanObject.scan_date_time = Util.getOfflineSyncClientFormat(syncObj.tbup.sTime,
								BaseActivity.checkedin_event_record.Events.Time_Zone__c);
						scanObject.error = syncObj.msg;
						Util.db.UpdateOfflineInvalidScans(scanObject);
					}

					for (OfflineSyncSuccessObject syncObj : offlineResponse.SuccessTickets) {
						if (Util.db.isItemPoolFreeSession(syncObj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c,	BaseActivity.checkedin_event_record.Events.Id)) {
							List<TStatus> session_attendees = new ArrayList<TStatus>();
							session_attendees.add(syncObj.STicketId);
							Util.db.InsertAndUpdateSessionAttendees(session_attendees, checked_in_eventId);
						} else {
							Util.db.updateCheckedInStatus(syncObj.STicketId, checked_in_eventId);
						}
						Util.db.deleteOffLineScans("(" + DBFeilds.OFFLINE_BADGE_ID + " = '"
								+ syncObj.STicketId.Ticket__r.Id + "' OR " + DBFeilds.OFFLINE_BADGE_ID + " = '"
								+ syncObj.STicketId.Ticket__r.Badge_ID__c + "' OR " + DBFeilds.OFFLINE_BADGE_ID + " = '"
								+ syncObj.STicketId.Ticket__r.Custom_Barcode__c + "') AND " + DBFeilds.OFFLINE_GROUP_ID
								+ " = '" + syncObj.STicketId.BLN_Session_user__r.BLN_Group__c + "' AND "
								+ DBFeilds.OFFLINE_EVENT_ID + " = '" + checked_in_eventId + "' AND "
								+ DBFeilds.OFFLINE_SCAN_TIME+ " = '"+syncObj.TimeStamp.split("\\.")[0].trim()+"'");
					}

					/*Intent i = new Intent(OfflineDataSyncActivty.this, OfflineSyncDialogActivity.class);
					String totalScans = String.valueOf(offlineScans.size());/// offlineResponse.FailureTickets.size()
																			/// +
																			/// offlineResponse.SuccessTickets.size());
					String successScans = String.valueOf(offlineResponse.SuccessTickets.size());
					String faluerScans = String.valueOf(invalid_scans);
					i.putExtra("SYNCHRESPONSE", totalScans + "," + successScans + "," + faluerScans);
					i.putExtra(Util.INTENT_KEY_1, checked_in_eventId);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					finish();*/
					
					offline_status_list.get(offline_status_list.size() - batch_count).progress_name = offline_status_list.get(offline_status_list.size() - batch_count).progress_name.replace("is Syncing...", "Records Status");
					offline_status_list.get(offline_status_list.size() - batch_count).invalid=invalid_scans;
					offline_status_list.get(offline_status_list.size() - batch_count).total=offlineScans.size();
					offline_status_list.get(offline_status_list.size() - batch_count).valid=offlineResponse.SuccessTickets.size();
					offline_status_list.get(offline_status_list.size() - batch_count).progress = 100;
					offline_status_list.get(offline_status_list.size() - batch_count).showProgress = false;
					if(batch_count > 1){
						offline_status_list.get((offline_status_list.size() - batch_count)+1).showProgress = true;	
					}
					adapter.notifyDataSetChanged();
					batch_count--;

					AppUtils.displayLog("--------------Batch Sync In Parse Json----------",":"+batch_count);

					if(batch_count > 0){
						batchSync();
					}else{
						showCustomToast(this, "Batch Syncing is completed successfully.", R.drawable.img_like, R.drawable.toast_greenroundededge, false);

					}
					
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		 if (batch_count > 1) {
			offline_status_list.get((offline_status_list.size() - batch_count) + 1).showProgress = true;
		 }
		 adapter.notifyDataSetChanged();
		 batch_count--;
		AppUtils.displayLog("--------------Batch Sync In Exception----------", ":" + batch_count);
        if (batch_count > 0) {
			batchSync();
		} else {
			showCustomToast(this, "Batch Syncing is completed successfully.", R.drawable.img_like, R.drawable.toast_greenroundededge, false);
        }
		}
		
 }

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		isOfflineSyncRunning=false;
	}

	@Override
	public void setCustomContentView(int layout) {
		// TODO Auto-generated method stub
		isOfflineSyncRunning=true;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);

		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		list_view = (ListView)linearview.findViewById(R.id.list_progress);
	}
	
	public void showSyncAlert(String msg){
		Util.setCustomAlertDialog(this);
		Util.txt_dismiss.setVisibility(View.VISIBLE);
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
	
	private class SyncingAdapter extends BaseAdapter{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return offline_status_list.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public OfflineStatus getItem(int position) {
			// TODO Auto-generated method stub
			return offline_status_list.get(position);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = inflater.inflate(R.layout.offline_progress_item, null);
			TextView txt_total = (TextView)v.findViewById(R.id.txt_total_scans);
			TextView txt_valid = (TextView)v.findViewById(R.id.txt_valid_scans);
			TextView txt_invalid = (TextView)v.findViewById(R.id.txt_invalid_scans);
			TextView txt_sync = (TextView)v.findViewById(R.id.txt_sync);
			ProgressBar progress_bar = (ProgressBar) v.findViewById(R.id.progress_bar);
			ProgressBar progress_dialog = (ProgressBar)v.findViewById(R.id.progress_dialog);
			progress_dialog.setVisibility(View.GONE);
			if(getItem(position).showProgress){
				progress_dialog.setVisibility(View.VISIBLE);
			}
			
			//progress_bar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));	
			txt_sync.setText(getItem(position).progress_name);
			progress_bar.setProgress(getItem(position).progress);
			//progress_bar.setMax(100);
			txt_total.setText(getItem(position).total+"");
			txt_valid.setText(getItem(position).valid+"");
			txt_invalid.setText(getItem(position).invalid+"");
			return v;
		}
		
	}
	
	public class OfflineStatus{
		public int total = 0, valid = 0,invalid = 0,progress = 2;
		public String progress_name = "1 to 100 is Syncing...";
		public boolean showProgress = false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			if(batch_count > 0){
				showSyncAlert("Processing your offline records, please wait...!");
			}else{
				finish();	
			}
		}
		return super.onKeyDown(keyCode, event);


	}
}
