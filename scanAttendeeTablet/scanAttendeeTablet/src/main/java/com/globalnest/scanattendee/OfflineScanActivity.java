package com.globalnest.scanattendee;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class OfflineScanActivity extends BaseActivity {

	TextView txt_scans_count;
	ListView list_offline;
	String scannedData = "";
	List<OfflineScansObject> offlineList = new ArrayList<OfflineScansObject>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_offline_scan);
		//Log.i("------------Offline Scan Data---------",":"+getIntent().getCharArrayExtra(Util.SCANDATA).toString());
		
		if (getIntent().getCharArrayExtra(Util.SCANDATA) != null) {
			char[] data = getIntent().getCharArrayExtra(Util.SCANDATA);
			scannedData = new String(data);
			generateCsvFile(scannedData.toString().trim());
			//finish();
		}
		//Log.i("------------Offline Scan Data---------",":"+scannedData.toString().trim());
		offlineList= Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"
				+ checked_in_eventId + "' ORDER BY "+DBFeilds.OFFLINE_SCAN_TIME+" DESC",false);
		txt_scans_count.setText("TOTAL SCANS   :   "+offlineList.size());
        list_offline.setAdapter(new OfflineListAdapter(offlineList));

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mDrawerLayout.isDrawerOpen(left_menu_slider))
					mDrawerLayout.closeDrawer(left_menu_slider);
				else
					mDrawerLayout.openDrawer(left_menu_slider);
			}
		});

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doRequest();
			}
		});

		img_search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exportCSV();
			}
		});
	}

	private void exportCSV() {
		File mediaStorageDir = new File(
				Environment.getExternalStorageDirectory(),
				"ScanAttendee Barcodes");
		File file = new File(mediaStorageDir.getPath() + File.separator
				+ checkedin_event_record.Events.Id+"_"+sfdcddetails.user_id+"_"+user_profile.Profile.Email__c+"_"+checkedin_event_record.Events.Name+" barcodes.csv");
		Uri u1 = Uri.fromFile(file);
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Offline Attendees For The Event : "+checkedin_event_record.Events.Name);
		sendIntent.putExtra(Intent.EXTRA_STREAM, u1);
		sendIntent.setType("text/richtext");
		startActivity(sendIntent);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
	
		offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"
						+ checked_in_eventId + "' ORDER BY "+DBFeilds.OFFLINE_SCAN_TIME+" DESC",false);
		if (list_offline != null && offlineList.size() > 0)
			list_offline.setAdapter(new OfflineListAdapter(offlineList));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		if(isOnline()){
			String urlValuesString = getValues();
			String accessToken = sfdcddetails.token_type + " "
					+ sfdcddetails.access_token;
			postMethod = new HttpPostData("Please Wait...",
					sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL
					+ urlValuesString, getJsonBody().toString(),
					accessToken, OfflineScanActivity.this);

			postMethod.execute();
		}else{
			startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
		}
	}

	@SuppressWarnings("deprecation")
	private String getValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("scannedby", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("eventId",
				BaseActivity.checkedin_event_record.Events.Id));
		values.add(new BasicNameValuePair("source", "Offline"));
		values.add(new BasicNameValuePair("DeviceType", Util.getDeviceNameandAppVersion()));
		values.add(new BasicNameValuePair("checkin_only",String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))));
		return AppUtils.getQuery(values);
	}

	private String getJsonBody() {
		JSONArray ticketarray = new JSONArray();
		for (OfflineScansObject scanObj : offlineList) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("TicketId", scanObj.badge_id);
				obj.put("sTime", Util.getOfflineSyncServerFormat(scanObj.scan_date_time));
				//boolean isFreeSession = Util.db.isItemPoolFreeSession(scanObj.item_pool_id, scanObj.event_id);
				
				obj.put("isCHeckIn", true);
				obj.put("freeSemPoolId", scanObj.item_pool_id);
				obj.put("device", "ANDROID");
				obj.put("scandevicemode",scanObj.scandevicemode);
				ticketarray.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ticketarray.toString();
	}

	@Override
	public void parseJsonResponse(String response) {
		try {
			if(response !=null){
				if(BaseActivity.isValidResponse(response)){
					Gson gson = new Gson();
					OfflineSyncResController offlineResponse = gson.fromJson(response, OfflineSyncResController.class);
					if(offlineResponse !=null){
						
						for(OfflineSyncFailuerObject syncObj:offlineResponse.FailureTickets){
							OfflineScansObject scanObject=new OfflineScansObject();
							scanObject.badge_id=syncObj.tbup.ticketId;
							scanObject.badge_status=DBFeilds.STATUS_INVALID;
							scanObject.event_id=BaseActivity.checkedin_event_record.Events.Id;
							scanObject.item_pool_id=syncObj.tbup.freeSemPoolId;
							scanObject.scan_date_time=Util.getOfflineSyncClientFormat(syncObj.tbup.sTime,checkedin_event_record.Events.Time_Zone__c);
							scanObject.error=syncObj.msg;
							Util.db.InsertAndUpdateOfflineScans(scanObject);
						}
						
						for(OfflineSyncSuccessObject syncObj:offlineResponse.SuccessTickets){
							
							Util.db.deleteOffLineScans("("+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Badge_ID__c
									+"' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Custom_Barcode__c+"') AND "+DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+syncObj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c
									+"' AND "+DBFeilds.OFFLINE_EVENT_ID+" = '"+BaseActivity.checkedin_event_record.Events.Id+"'");
						}
						
						Intent i = new Intent(this,OfflineSyncDialogActivity.class);
						String totalScans=String.valueOf(offlineResponse.FailureTickets.size()+offlineResponse.SuccessTickets.size());
						String successScans=String.valueOf(offlineResponse.SuccessTickets.size());
						String faluerScans=String.valueOf(offlineResponse.FailureTickets.size());
						i.putExtra("SYNCHRESPONSE", totalScans+","+successScans+","+faluerScans);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}
				}else{
					openSessionExpireAlert(errorMessage(response));
				}
			}
		} catch (Exception e) {
			
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		linearview.addView(inflater.inflate(layout, null));
		txt_title.setText("Offline Scans");
		img_search.setImageResource(R.drawable.share);
		img_addticket.setImageResource(R.drawable.img_offline_sync);
		img_addticket.setVisibility(View.VISIBLE);
		img_search.setVisibility(View.VISIBLE);
		img_menu.setImageResource(R.drawable.top_more);
		list_offline = (ListView) linearview.findViewById(R.id.list_offline);
		txt_scans_count = (TextView)linearview.findViewById(R.id.txt_scans_count);

	}

	private class OfflineListAdapter extends BaseAdapter {

		private List<OfflineScansObject> mOfflineList;

		public OfflineListAdapter(List<OfflineScansObject> offlineList) {
			mOfflineList = offlineList;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mOfflineList.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public OfflineScansObject getItem(int position) {
			// TODO Auto-generated method stub
			return mOfflineList.get(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View converterView,
				ViewGroup viewGroup) {

			final HolderView viewHolder;
			if (converterView == null) {
				converterView = inflater.inflate(R.layout.offline_list_item,
						null);
				viewHolder = new HolderView();
				viewHolder.txt_offlineBarcodeName = (TextView) converterView
						.findViewById(R.id.txt_offlinename);
				viewHolder.txt_scanDateTime = (TextView) converterView
						.findViewById(R.id.txt_dateTime);
				viewHolder.txt_status = (TextView) converterView
						.findViewById(R.id.txt_status);
				viewHolder.txt_error = (TextView) converterView
						.findViewById(R.id.txt_offlineerror);
				viewHolder.img_delete = (ImageView) converterView
						.findViewById(R.id.img_delete);
				converterView.setTag(viewHolder);
			} else {
				viewHolder = (HolderView) converterView.getTag();
			}

			OfflineScansObject offlineobj = mOfflineList.get(position);
			viewHolder.txt_offlineBarcodeName.setText(offlineobj.badge_id);
			viewHolder.txt_scanDateTime.setText(offlineobj.scan_date_time);
			
			if (offlineobj.badge_status.equals(DBFeilds.STATUS_INVALID)) {
				viewHolder.txt_status.setBackgroundResource(R.color.orange_bg);
				//Log.i("-----------------Badge Invalid----------",":"+offlineobj.scan_date_time);
			} else {
				viewHolder.txt_status.setBackgroundResource(R.color.red);
				//Log.i("-----------------Badge Valid----------",":"+offlineobj.scan_date_time);
			}
			viewHolder.txt_status.setText(offlineobj.badge_status.toUpperCase());
			viewHolder.img_delete.setTag(position);
			if (!offlineobj.error.isEmpty()) {
				viewHolder.txt_error.setVisibility(View.VISIBLE);
				viewHolder.txt_error.setText(offlineobj.error);
			} else {
				viewHolder.txt_error.setVisibility(View.GONE);
			}
			viewHolder.img_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					
					final AlertDialogCustom dialog=new AlertDialogCustom(OfflineScanActivity.this);
					dialog.setParamenters("Alert", "Do you want to delete this badge?", null, null, 2, false);
					dialog.setFirstButtonName("DELETE");
					dialog.btnOK.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View arg0) {
							dialog.dismiss();
							OfflineScansObject offlineobj = mOfflineList
									.get((Integer) viewHolder.img_delete.getTag());
							Util.db.deleteOffLineScans(DBFeilds.OFFLINE_EVENT_ID
									+ " = '" + offlineobj.event_id + "' AND "
									+ DBFeilds.OFFLINE_BADGE_ID + " = '"
									+ offlineobj.badge_id + "' AND "
									+ DBFeilds.OFFLINE_ITEM_POOL_ID + " = '"
									+ offlineobj.item_pool_id + "'");
							offlineList = Util.db
									.getOfflineScans(" Where "
											+ DBFeilds.OFFLINE_EVENT_ID + " ='"
											+ checked_in_eventId + "' ORDER BY "+DBFeilds.OFFLINE_SCAN_TIME+" DESC",false);
							txt_scans_count.setText("TOTAL SCANS   :   "+offlineList.size());
							list_offline
									.setAdapter(new OfflineListAdapter(offlineList));
						}
					});
					
					dialog.btnCancel.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					
					dialog.show();
				}
			});

			return converterView;
		}
	}

	private class HolderView {
		TextView txt_offlineBarcodeName, txt_scanDateTime, txt_status,
				txt_error;
		ImageView img_delete;

	}
}
