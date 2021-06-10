//  ScanAttendee Android
//  Created by Ajay on May 10, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.SeminaAgenda;
import com.globalnest.mvc.SessionListResponseController;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.scroll.PoppyViewHelper;
import com.globalnest.scroll.PoppyViewHelper.PoppyViewPosition;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author mayank
 *
 */
public class SessionListActivity extends BaseActivity{
	//private DashboardTicketNameObject ticketObject;
	private String item_pool_id=ITransaction.EMPTY_STRING;
	private ListView list_session;
	private LinearLayout linear_dates,linear_rooms;
	private Cursor attendee_cursor;
	private SessionAttendeesAdapter adapter;
	private TextView txt_session_name,txt_startdate,txt_enddate,txt_room,txt_roomnumber,txt_checkin,txt_checkout,txtnoattendee;
	private String where_clause_all_Attendees = ITransaction.EMPTY_STRING ;
	private String where_clause_all_Attendees_2 = ITransaction.EMPTY_STRING;
	private String request_type= ITransaction.EMPTY_STRING;
	private String attendee_id = ITransaction.EMPTY_STRING;
	private PoppyViewHelper mPoppyViewHelper;
	private LinearLayout sort_layout, filter_layout, sortfilterlayout;
	private TextView txt_sort, txt_filter;
	private boolean isFilter;
	private String filter_by=ITransaction.EMPTY_STRING;
	private String session_id = ITransaction.EMPTY_STRING;
	private String session_name = ITransaction.EMPTY_STRING;
	private int total_scans = 0;
	private boolean isFromDetails = false;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.sessionlist_activity);
		item_pool_id= getIntent().getStringExtra(WebServiceUrls.SA_GETSESSIONCHECKINS);
		session_id = getIntent().getStringExtra(Util.INTENT_KEY_1);
		session_name = getIntent().getStringExtra(Util.INTENT_KEY_3);
		//String item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);

		if(NullChecker(item_pool_id).isEmpty()){
			List<ScannedItems> scan_items = Util.db.getScannedItemsGroup(checked_in_eventId, session_id);
			if(scan_items.size() > 0){
				item_pool_id = scan_items.get(0).BLN_Item_Pool__c;
			}
		}
		//Log.i("-----------------Item Pool Name---------",":"+item_pool_name);
		//where_clause_all_Attendees = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"'"+" ORDER BY "+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC";
		//where_clause_all_Attendees = "where (attendees."+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' OR offline."+DBFeilds.OFFLINE_ITEM_POOL_ID+"='"+item_pool_id+"')"+" AND (attendees."+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' OR offline."+DBFeilds.OFFLINE_EVENT_ID+"='"+checked_in_eventId+"')"+" ORDER BY attendees."+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC, offline."+DBFeilds.OFFLINE_SCAN_TIME+" DESC";

		//where_clause_all_Attendees = "select SessionAttendees.session_attendee_first_name,SessionAttendees.session_attendee_last_name,SessionAttendees.session_item_pool_id, SessionAttendees.session_attendee_ticket_id, SessionAttendees.session_attendee_email_id, SessionAttendees.session_attendee_company, SessionAttendees.session_attendee_checkin_status, SessionAttendees.session_attendee_scantime FROM SessionAttendees where SessionAttendees.session_item_pool_id='"+item_pool_id+"' UNION select offline_scan.badge_id,offline_scan.error,offline_scan.item_pool_id, offline_scan.badge_id, offline_scan.badge_status,offline_scan.user_id,offline_scan.checkin_status,offline_scan.scan_time FROM offline_scan where offline_scan.item_pool_id='"+item_pool_id+"'";
		where_clause_all_Attendees= "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_GROUP_ID+"= '"+session_id+"'";
		where_clause_all_Attendees_2 = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_GROUP_ID+"= '"+session_id+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC";
		txt_session_name.setText(session_name);

		SeminaAgenda agenda = Util.db.getSeminarAgenda("where "+DBFeilds.SEMINAR_ITEM_POOL_ID+"='"+item_pool_id+"'");

		if(agenda != null){
			String start_date = Util.change_US_ONLY_DateFormat(agenda.startTime,
					checkedin_event_record.Events.Time_Zone__c);
			String end_date = Util.change_US_ONLY_DateFormat(agenda.endtime,
					checkedin_event_record.Events.Time_Zone__c);
			txt_startdate.setText(Html.fromHtml("<font color=#DF6B1E> Start Date: </font><font color=#000000>" + start_date+"</font>"));
			txt_enddate.setText(Html.fromHtml("  <font color=#DF6B1E> End Date: </font><font color=#000000>" + end_date+"</font>"));
			txt_room.setText(Html.fromHtml("<font color=#DF6B1E> Room: </font><font color=#000000>" + agenda.room+"</font>"));
			txt_roomnumber.setText(Html.fromHtml("<font color=#DF6B1E> Room Number: </font><font color=#000000>" + agenda.roomNo+"</font>"));
		}else{
			linear_dates.setVisibility(View.GONE);
			linear_rooms.setVisibility(View.GONE);
		}

		Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
		externalSettings = new ExternalSettings();
		if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
			externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
		}

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				request_type = WebServiceUrls.SA_GETSESSIONCHECKINS;
				doRequest();
			}
		});
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
		img_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				back_layout.setVisibility(View.VISIBLE);
				search_layout.setVisibility(View.GONE);
				search_view.setText("");
				// search_view.setFocusable(false);
				hideKeybord(v);
				searchFucntion();
			}
		});

		img_setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/*request_type = DBFeilds.STATUS_OFFLINE;
				doRequest();*/
				Intent i = new Intent(SessionListActivity.this, OfflineDataSyncActivty.class);
				i.putExtra(Util.INTENT_KEY_1, SessionListActivity.class.getName());
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			}
		});
		search_view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_UP){
					if (motionEvent.getX()>(view.getWidth()-50)){
						search_view.setText("");
						txtnoattendee.setVisibility(View.GONE);
					}
				}
				return false;
			}
		});
		search_view.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				searchFucntion();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		list_session.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				Cursor cursor = adapter.getCursor();
				cursor.moveToPosition(position);
				/*Intent i = new Intent(SessionListActivity.this,	CheckinHistory.class);
				i.putExtra(Util.TICKET_ID, c1
						.getString(c1
								.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID)));
				i.putExtra(
						Util.ATTENDEE_NAME,
						c1.getString(c1
								.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME))
								+ " "
								+ c1.getString(c1
										.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)));
				i.putExtra(Util.INTENT_KEY_1, c1.getString(c1.getColumnIndex(DBFeilds.SESSION_ITEM_POOL_ID)));
				startActivity(i);*/
				if(!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)
						&& !NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_INVALID)){
					Intent i = new Intent(SessionListActivity.this, SessionAttendeeDetailActivity.class);
					i.putExtra(DBFeilds.SESSION_ATTENDEE_TICKET_ID, cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID)));
					i.putExtra(DBFeilds.SESSION_GROUP_ID, cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_GROUP_ID)));
					startActivityForResult(i,2018);
				}


			}
		});

		sort_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openSortDialog(SessionListActivity.this);
			}
		});
		sortfilterlayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openFilterDialog(SessionListActivity.this);
			}
		});


	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if(attendee_cursor != null){
			attendee_cursor.close();
		}
		attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause_all_Attendees,where_clause_all_Attendees_2);
		txt_checkin.setText("My Check-ins: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_GROUP_ID+"='"+session_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='true'"));
		txt_checkout.setText("My Check-outs: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_GROUP_ID+"='"+session_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='false'"));

		//Log.i("--------Session Attendees-----------",":"+attendee_cursor.getCount());
		if(Util.db.getALLOfflineScanCount(checked_in_eventId) > 0){
			img_setting.setVisibility(View.VISIBLE);
		}else{
			img_setting.setVisibility(View.GONE);
		}
		if (attendee_cursor.moveToFirst()) {
			txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
			if(isFromDetails){
				isFromDetails = false;
				adapter.changeCursor(attendee_cursor);
				adapter.notifyDataSetChanged();
			}else{
				adapter = new SessionAttendeesAdapter(this, attendee_cursor);
				list_session.setAdapter(adapter);
			}

		}else if(isOnline()){
			request_type = WebServiceUrls.SA_GETSESSIONCHECKINS;
			doRequest();
		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {

		if (!isOnline()) {
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		} else if (request_type.equalsIgnoreCase(WebServiceUrls.SA_GETSESSIONCHECKINS)) {
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_GETSESSIONCHECKINS + getValues();
			getMehod = new HttpGetMethod(url, sfdcddetails.token_type, sfdcddetails.access_token,
					SessionListActivity.this);
			getMehod.execute();
		} else if (request_type.equalsIgnoreCase(WebServiceUrls.SA_TICKETS_SCAN_URL)) {
			String url = sfdcddetails.instance_url+WebServiceUrls.SA_TICKETS_SCAN_URL+"scannedby=" + sfdcddetails.user_id+"&eventId="+checked_in_eventId+"&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion().replaceAll(" ", "%20")
					+"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
			String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
			postMethod = new HttpPostData("Attendee Checking In/Out...", url, getCheckinString(), access_token, SessionListActivity.this);
			postMethod.execute();
		}else if(request_type.equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)){
			String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
			postMethod = new HttpPostData("Please Wait Syncing Offline Data...",
					sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL+ getOfflineValues(), getOfflineJsonBody().toString(), access_token, SessionListActivity.this);

			postMethod.execute();
		}

	}

	private String getCheckinString(){
		JSONArray array = new JSONArray();
		try {

			JSONObject obj = new JSONObject();
			obj.put("TicketId", attendee_id);
			obj.put("device", "ANDROID");
			obj.put("freeSemPoolId",item_pool_id);
			boolean ischeckin = Util.db.SessionCheckInStatus(attendee_id,Util.db.getSwitchedONGroupId(checked_in_eventId));
			if (ischeckin){
				obj.put("isCHeckIn", false);
			}else{
				obj.put("isCHeckIn", true);
			}
			obj.put("sTime", Util.getCurrentDateTimeInGMT());
			obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
			obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
			array.put(obj);
		} catch (Exception e) {
			// TODO: handle exception
		}
		return array.toString();
	}
	private String getValues(){
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("scannedby", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("freeSemPoolId", item_pool_id));
		values.add(new BasicNameValuePair("sesId", session_id));
		values.add(new BasicNameValuePair("lsttStatusId", Util.offset_pref.getString(session_id+"_"+item_pool_id+"_"+checked_in_eventId, "")));
		return AppUtils.getQuery(values);
	}


	private String getOfflineValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("scannedby", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("eventId",
				BaseActivity.checkedin_event_record.Events.Id));
		values.add(new BasicNameValuePair("source", "Offline"));
		values.add(new BasicNameValuePair("DeviceType", Util.getDeviceNameandAppVersion()));
		values.add(new BasicNameValuePair("checkin_only",String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))));
		return AppUtils.getQuery(values);
	}

	private String getOfflineJsonBody() {
		JSONArray ticketarray = new JSONArray();
		List<OfflineScansObject> offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"
				+ checked_in_eventId + "' AND "+DBFeilds.OFFLINE_ITEM_POOL_ID+"='"+item_pool_id+"'",false );
		total_scans = offlineList.size();
		for (OfflineScansObject scanObj : offlineList) {
			JSONObject obj = new JSONObject();
			try {
				obj.put("TicketId", scanObj.badge_id);
				obj.put("sTime", Util.getOfflineSyncServerFormat(scanObj.scan_date_time));
				//boolean isFreeSession = Util.db.isItemPoolFreeSession(scanObj.item_pool_id, scanObj.event_id);

				obj.put("isCHeckIn", true);
				obj.put("freeSemPoolId", scanObj.item_pool_id);
				obj.put("device", "ANDROID");
				obj.put("sessionid", scanObj.scan_group_id);
				obj.put("scandevicemode",scanObj.scandevicemode);
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
		//Log.i("----Session List Activity-----", "response"+response);

		try {

			if (!isValidResponse(response)) {
				openSessionExpireAlert(errorMessage(response));
			} else {

				if (request_type.equalsIgnoreCase(WebServiceUrls.SA_GETSESSIONCHECKINS)) {
					Gson gson = new Gson();
					Type listType = new TypeToken<TStatus[]>() {
					}.getType();
					TStatus responseHandler[] = gson.fromJson(response, listType);
					List<TStatus> session_attendees_list = new ArrayList<TStatus>(
							Arrays.asList(responseHandler));
					//Log.i("-----------SessionAttendees List Attendees List----------", ":"+session_attendees_list.size());
					if (session_attendees_list.size() > 0) {
						Util._savePreference(Util.offset_pref, session_id+"_"+item_pool_id+"_"+checked_in_eventId, session_attendees_list.get(session_attendees_list.size()-1).Id);
						Util.db.InsertAndUpdateSessionAttendees(session_attendees_list, checked_in_eventId);
					}
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							updateUI();
						}
					});

				} else if (request_type.equalsIgnoreCase(WebServiceUrls.SA_TICKETS_SCAN_URL)) {
					OfflineSyncResController t_status_res = new Gson().fromJson(response, OfflineSyncResController.class);

					String dialogtime = ITransaction.EMPTY_STRING;
					JSONObject obj = new JSONObject(response);

					JSONArray success = obj.optJSONArray("SuccessTickets");
					JSONArray failure = obj.optJSONArray("FailureTickets");
					if (success == null) {
						success = new JSONArray();
					}
					if (failure == null) {
						failure = new JSONArray();
					}

					if (success.length() > 0) {
						boolean status = success.optJSONObject(0).optBoolean("Status");
						String time = success.optJSONObject(0).optString("TimeStamp");
						String attendee_Id = success.optJSONObject(0).optJSONObject("STicketId").optString("Ticket__c");
						dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time,checkedin_event_record.Events.Time_Zone__c);
						//Util.db.updateCheckedInStatus(attendee_Id, checked_in_eventId, time, status);
						List<TStatus> checkins = new ArrayList<TStatus>();
						checkins.add(t_status_res.SuccessTickets.get(0).STicketId);
						Util.db.InsertAndUpdateSessionAttendees(checkins,checked_in_eventId);
						searchFucntion();
					} else if (failure.length() > 0) {
						boolean status = failure.optJSONObject(0).optBoolean("Status");
						String time = failure.optJSONObject(0).optString("TimeStamp");
						attendee_id = failure.optJSONObject(0).optString("STicketId");
						dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time,checkedin_event_record.Events.Time_Zone__c);
						//Util.db.updateCheckedInStatus(attendee_Id, checked_in_eventId, time, status);
						List<TStatus> checkins = new ArrayList<TStatus>();
						checkins.add(t_status_res.FailureTickets.get(0).tStaus);
						Util.db.InsertAndUpdateSessionAttendees(checkins,checked_in_eventId);
						searchFucntion();
						/*AlertDialogCustom dialog = new AlertDialogCustom(SessionListActivity.this);
						if(status){
							dialog.setParamenters("Check-in Failed","This Ticket has been already Checked In" + "\nat " + dialogtime, null, null, 1, false);
						}else{
							dialog.setParamenters("Check-Out Failed","This Ticket has been already Checked Out" + "\nat " + dialogtime, null, null, 1, false);
						}
						dialog.show();*/

						Util.setCustomAlertDialog(SessionListActivity.this);
						Util.txt_dismiss.setVisibility(View.VISIBLE);
						Util.txt_okey.setText("DENIED");
						if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
							Util.txt_dismiss.setVisibility(View.GONE);
							Util.txt_okey.setText("OK");
						}else if(status){
							Util.txt_dismiss.setText("CHECK OUT");
						}else{
							Util.txt_dismiss.setText("CHECK IN");
						}

						Util.txt_okey.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								// ticket_dialog.dismiss();
								Util.alert_dialog.dismiss();
								//finish();
							}
						});
						Util.txt_dismiss.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								// ticket_dialog.dismiss();
								Util.alert_dialog.dismiss();
								request_type = WebServiceUrls.SA_TICKETS_SCAN_URL;
								doRequest();
							}
						});
						if (status) {
							if (Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
								Util.openCustomDialog("Check-in Failed",
										"This Ticket has been already Checked In" + "\nat "
												+ dialogtime + ". Check out is disabled.");
							} else {
								Util.openCustomDialog("Check-in Failed",
										"This Ticket has been already Checked In" + "\nat "
												+ dialogtime + ". Do you want to Check Out ?");
							}

						} else {
							Util.openCustomDialog("Check-Out Failed",
									"This Ticket has been already Checked Out" + "\nat "
											+ dialogtime + ". Do you want to Check In ?");
						}
					} else {
						startErrorAnimation(obj.optString("ErrorMsg"), txt_error_msg);
					}
				}else if(request_type.equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)){
					Gson gson = new Gson();
					OfflineSyncResController offlineResponse = gson.fromJson(response, OfflineSyncResController.class);
					if(offlineResponse !=null){
						int invalid_scans = 0;
						HashMap<String, String> duplicate_barcodes = new HashMap<>();
						for (OfflineSyncFailuerObject syncObj : offlineResponse.FailureTickets) {

							if (isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets)) {
								continue;
							} else if(duplicate_barcodes.containsKey(syncObj.tbup.ticketId)){
								if(duplicate_barcodes.get(syncObj.tbup.ticketId).equalsIgnoreCase(syncObj.tbup.sessionid)){
									continue;
								}else{
									duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
									invalid_scans++;
								}
							}else{
								duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
								invalid_scans++;
							}
							OfflineScansObject scanObject=new OfflineScansObject();
							scanObject.badge_id=syncObj.tbup.ticketId;
							scanObject.badge_status=DBFeilds.STATUS_INVALID;
							scanObject.event_id=BaseActivity.checkedin_event_record.Events.Id;
							scanObject.item_pool_id=syncObj.tbup.freeSemPoolId;
							scanObject.scan_date_time=Util.getOfflineSyncClientFormat(syncObj.tbup.sTime,checkedin_event_record.Events.Time_Zone__c);
							scanObject.error=syncObj.msg;
							scanObject.scan_group_id = syncObj.tbup.sessionid;
							Util.db.UpdateOfflineInvalidScans(scanObject);
						}

						for(OfflineSyncSuccessObject syncObj:offlineResponse.SuccessTickets){
							if(Util.db.isItemPoolFreeSession(syncObj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c, BaseActivity.checkedin_event_record.Events.Id)){
								List<TStatus> session_attendees = new ArrayList<TStatus>();
								session_attendees.add(syncObj.STicketId);
								Util.db.InsertAndUpdateSessionAttendees(session_attendees, BaseActivity.checkedin_event_record.Events.Id);
							}else{
								Util.db.updateCheckedInStatus(syncObj.STicketId,BaseActivity.checkedin_event_record.Events.Id);
							}
							Util.db.deleteOffLineScans("("+ DBFeilds.OFFLINE_BADGE_ID + " = '"
									+ syncObj.STicketId.Ticket__r.Id + "' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Badge_ID__c
									+"' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+syncObj.STicketId.Ticket__r.Custom_Barcode__c+"') AND "
									+ DBFeilds.OFFLINE_GROUP_ID + " = '"
									+ syncObj.STicketId.BLN_Session_user__r.BLN_Group__c + "' AND "
									+ DBFeilds.OFFLINE_EVENT_ID + " = '"
									+ checked_in_eventId + "'");
						}

						Intent i = new Intent(this,OfflineSyncDialogActivity.class);
						String totalScans=String.valueOf(total_scans);
						String successScans=String.valueOf(offlineResponse.SuccessTickets.size());
						String faluerScans=String.valueOf(invalid_scans);
						i.putExtra("SYNCHRESPONSE", totalScans+","+successScans+","+faluerScans);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(i);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void updateUI(){

		if (attendee_cursor != null) {
			attendee_cursor.close();
		}
		attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause_all_Attendees,where_clause_all_Attendees_2);
		//Log.i("-----------SessionAttendees List Before If----------", ":"+attendee_cursor.moveToFirst());
		if (attendee_cursor.moveToFirst()) {
			txtnoattendee.setVisibility(View.GONE);
			txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
			txt_checkin.setText("My Check-ins: " + Util.db.getSessionAttendeeCount(" where " + DBFeilds.SESSION_ITEM_POOL_ID
					+ "='" + item_pool_id + "' AND " + DBFeilds.SESSION_EVENT_ID + "='" + checked_in_eventId + "' AND "
					+ DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS + "='true'"));
			txt_checkout.setText("My Check-outs: " + Util.db.getSessionAttendeeCount(" where "
					+ DBFeilds.SESSION_ITEM_POOL_ID + "='" + item_pool_id + "' AND " + DBFeilds.SESSION_EVENT_ID + "='"
					+ checked_in_eventId + "' AND " + DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS + "='false'"));

			//Log.i("-----------SessionAttendees List----------", ":" + attendee_cursor.moveToFirst());
			adapter = new SessionAttendeesAdapter(SessionListActivity.this, attendee_cursor);
			list_session.setAdapter(adapter);
			/*
			 * if (adapter != null) { adapter.swapCursor(attendee_cursor);
			 * adapter.notifyDataSetChanged(); } else { adapter = new
			 * SessionAttendeesAdapter(SessionListActivity.this,
			 * attendee_cursor); list_session.setAdapter(adapter); }
			 */

		} else {
			/*AlertDialogCustom dialog = new AlertDialogCustom(SessionListActivity.this);
			dialog.setParamenters("Alert!", "Sorry...! No Attendees Found.", null, null, 1, true);
			dialog.show();*/
			txtnoattendee.setVisibility(View.VISIBLE);
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
	public void setCustomContentView(int layout) {
		activity = this;
		View view=inflater.inflate(layout, null);
		linearview.addView(view);
		txt_title.setText("Attendees");
		img_menu.setImageResource(R.drawable.top_more);
		img_search.setVisibility(View.VISIBLE);
		img_addticket.setImageResource(R.drawable.dashboardrefresh);
		img_addticket.setVisibility(View.VISIBLE);
		img_setting.setVisibility(View.VISIBLE);
		img_setting.setImageResource(R.drawable.img_offline_sync);
		img_setting.setVisibility(View.GONE);

		list_session=(ListView) linearview.findViewById(R.id.list_sessionList);
		txt_session_name = (TextView)linearview.findViewById(R.id.txt_session_name);
		txt_startdate = (TextView)linearview.findViewById(R.id.txt_session_startdate);
		txt_enddate = (TextView) linearview.findViewById(R.id.txt_session_enddate);
		txt_room = (TextView)linearview.findViewById(R.id.txt_room);
		txt_roomnumber = (TextView)linearview.findViewById(R.id.txt_room_number);
		txt_checkin = (TextView)linearview.findViewById(R.id.txt_session_checkins);
		txt_checkout = (TextView)linearview.findViewById(R.id.txt_session_checkout);
		txtnoattendee = (TextView)linearview.findViewById(R.id.txtnoattendee);
		txtnoattendee.setVisibility(View.GONE);
		linear_dates = (LinearLayout)linearview.findViewById(R.id.linear_dates);
		linear_rooms = (LinearLayout)linearview.findViewById(R.id.linear_rooms);
		//list_session.setAdapter(new SessionListAdapter(SessionListActivity.this,mDashboardHandler));
		search_view.setHint("First Name, Last Name, Email And Company");

		mPoppyViewHelper = new PoppyViewHelper(this, PoppyViewPosition.BOTTOM);
		View poppyView = mPoppyViewHelper.createPoppyViewOnListView(R.id.list_sessionList, R.layout.header_layout);
		sortfilterlayout = (LinearLayout) poppyView.findViewById(R.id.sortfilterlayout);
		sort_layout = (LinearLayout) poppyView.findViewById(R.id.ordersortlayout);
		filter_layout = (LinearLayout) poppyView.findViewById(R.id.orderfilterlayout);
		txt_sort = (TextView) poppyView.findViewById(R.id.txtsortorder);
		txt_filter = (TextView) poppyView.findViewById(R.id.txtfilterorder);

		txt_sort.setTypeface(Util.roboto_regular);
		txt_filter.setTypeface(Util.roboto_regular);
	}

	private void searchFucntion(){
		String search = search_view.getText().toString().trim();
		if(!search.isEmpty()){
			String where_clause = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND ("
					+DBFeilds.SESSION_ATTENDEE_FIRST_NAME+" like '%"+search+"%' OR "
					+DBFeilds.SESSION_ATTENDEE_LAST_NAME+" like '%" +search+"%' OR "
					+DBFeilds.SESSION_ATTENDEE_EMAIL_ID+" like '%" +search+"%' OR "
					+DBFeilds.SESSION_ATTENDEE_COMPANY+" like '%" +search+"%')";
			if(attendee_cursor != null){
				attendee_cursor.close();
			}
			attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause,where_clause);
			txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
			adapter = new SessionAttendeesAdapter(this, attendee_cursor);
			list_session.setAdapter(adapter);
			if(attendee_cursor != null){
				if(attendee_cursor.getCount() == 0){
					showNoAttendeesAlert();
				}
			}
		}else{
			if(attendee_cursor != null){
				attendee_cursor.close();
			}
			attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause_all_Attendees,where_clause_all_Attendees_2);
			txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
			txt_checkin.setText("My Check-ins: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='true'"));
			txt_checkout.setText("My Check-outs: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='false'"));

			adapter = new SessionAttendeesAdapter(this, attendee_cursor);
			list_session.setAdapter(adapter);
		}
	}

	public class SessionAttendeesAdapter extends CursorAdapter{

		/**
		 * @param context
		 * @param c
		 */
		public SessionAttendeesAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			return LayoutInflater.from(context).inflate(R.layout.attendee_item_layout, parent, false);
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			// TODO Auto-generated method stub
			TextView txt_session_attendee_Name = (TextView) view.findViewById(R.id.txtattname);
			TextView txt_sessionCompany = (TextView) view.findViewById(R.id.txtattcomp);
			//TextView txt_sessionEmail = (TextView) view.findViewById(R.id.txt_email);
			TextView txt_date = (TextView) view.findViewById(R.id.txtcheckindate);
			Button img_ok = (Button) view.findViewById(R.id.btncheckin);
			TextView txt_image = (TextView) view.findViewById(R.id.txt_image);
			final ImageView attendee_img = (ImageView) view.findViewById(R.id.attendee_img);
			TextView txt_seat_no = (TextView)view.findViewById(R.id.txtseatnum);
			txt_seat_no.setVisibility(View.GONE);
			TextView txt_session_name = (TextView)view.findViewById(R.id.txttickettype);
			//LinearLayout frame_tickettype = (LinearLayout)view.findViewById(R.id.frame_tickettype);
			//frame_tickettype.setVisibility(View.GONE);
			ImageView image_attendee_delete = (ImageView) view.findViewById(R.id.img_attendee_delete);
			FrameLayout frame_line = (FrameLayout)view.findViewById(R.id.frame_vertical_line);
			image_attendee_delete.setVisibility(View.GONE);
			frame_line.setVisibility(View.GONE);
			//SessionProfileObject profile=getItem(position).Ticket__r.tkt_profile__r;


			txt_session_attendee_Name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)));
			/*if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.OFFLINE_BADGE_ID))).isEmpty()){
				txt_session_attendee_Name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)));
			}else{
				txt_session_attendee_Name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.OFFLINE_BADGE_ID)));
			}*/

			txt_session_attendee_Name.setTypeface(null, Typeface.BOLD);
			if(!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).isEmpty()){
				txt_sessionCompany.setVisibility(View.VISIBLE);
				txt_sessionCompany.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY)));
			}else{
				txt_sessionCompany.setVisibility(View.GONE);
			}

			// txt_session_name.setText(Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId));
			txt_session_name.setText(NullChecker(session_name));
			// txt_sessionEmail.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_EMAIL_ID)));
			String checkin_time = Util.changeGMTtoEventTimeZone(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_SCANTIME)), checkedin_event_record.Events.Time_Zone__c);
			txt_date.setText(checkin_time);

			if (!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME))).isEmpty()
					&& !NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME))).isEmpty()) {
				attendee_img.setVisibility(View.GONE);
				txt_image
						.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME)).substring(0, 1).toUpperCase()
								+ cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)).substring(0, 1).toUpperCase());
			}else{
				attendee_img.setVisibility(View.GONE);
				txt_image.setText("NO");}

			// Log.i("---------------Record Id-------------",":"+cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS)));
			boolean isCheckin = Boolean.valueOf(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS)));
			img_ok.setTag(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID)));
			if(isCheckin){
				img_ok.setText("1/1");
				img_ok.setBackgroundResource(R.drawable.green_price_bg);
			}else{
				img_ok.setText("0/1");
				img_ok.setBackgroundResource(R.drawable.red_price_bg);
			}
			txt_sessionCompany.setTextColor(getResources().getColor(R.color.gray_color));
			if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)
					|| NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_INVALID)){
				image_attendee_delete.setVisibility(View.VISIBLE);
				frame_line.setVisibility(View.VISIBLE);
				img_ok.setBackgroundResource(R.drawable.gray_price_bg);

				if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)){
					txt_sessionCompany.setTextColor(getResources().getColor(R.color.red));
					txt_sessionCompany.setText(DBFeilds.STATUS_OFFLINE.toUpperCase());
				}else{
					txt_sessionCompany.setText(DBFeilds.STATUS_INVALID.toUpperCase());
					txt_sessionCompany.setTextColor(getResources().getColor(R.color.orange_bg));
					if (!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_ID))).isEmpty()) {
						txt_seat_no.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_ID)));
						txt_seat_no.setVisibility(View.VISIBLE);
						txt_seat_no.setTextColor(getResources().getColor(R.color.gray_color));
					}
				}


				if(!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME))).isEmpty()){
					txt_session_attendee_Name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)));
					txt_image.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)).split(" ")[0].substring(0, 1).toUpperCase()
							+cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)).split(" ")[1].substring(0, 1).toUpperCase());
				}else{
					cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME));
					txt_image.setText("NO");
				}
			}

			img_ok.setTag(cursor.getPosition());
			image_attendee_delete.setTag(cursor.getPosition());
			img_ok.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)) {
						openScanSettingsAlert(SessionListActivity.this,item_pool_id, SessionListActivity.class.getName());
					} else {
						cursor.moveToPosition((int) v.getTag());
						if (!NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)
								&& !NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_COMPANY))).equalsIgnoreCase(DBFeilds.STATUS_INVALID)) {
							attendee_id = cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID));
							String orderstatus=Util.db.getOrderStatuswithAttendeeID(attendee_id,checked_in_eventId);
							boolean ischeckin = Util.db.SessionCheckInStatus(attendee_id,Util.db.getSwitchedONGroupId(checked_in_eventId));
							if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
								showMessageAlert(getString(R.string.checkin_only_msg),false);
							}
							//commented for non ticketed session
							/*else if(!externalSettings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")){
								final String att_id=attendee_id;
								Util.setCustomAlertDialog(SessionListActivity.this);
								Util.txt_okey.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										Util.alert_dialog.dismiss();
										request_type = WebServiceUrls.SA_TICKETS_SCAN_URL;
										doRequest();
									}
								});
								Util.txt_dismiss.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										Util.alert_dialog.dismiss();
									}
								});
								Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");

							}*/else{
								request_type = WebServiceUrls.SA_TICKETS_SCAN_URL;
								doRequest();
							}

						}
					}
				}
			});
			image_attendee_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					cursor.moveToPosition((int) v.getTag());
					showDeleteDialog(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME)), cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ITEM_POOL_ID)),cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_SCANTIME)));
				}
			});
		}

	}

	private void showDeleteDialog(final String badge_id, final String item_pool_id,final String scan_time){
		final AlertDialogCustom dialog=new AlertDialogCustom(SessionListActivity.this);
		dialog.setParamenters("Alert", "Do you want to delete this badge?", null, null, 2, false);
		dialog.setFirstButtonName("DELETE");
		dialog.btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();

				Util.db.deleteOffLineScans(DBFeilds.OFFLINE_EVENT_ID
						+ " = '" + checked_in_eventId + "' AND "
						+ DBFeilds.OFFLINE_BADGE_ID + " = '"
						+ badge_id + "' AND "
						+ DBFeilds.OFFLINE_ITEM_POOL_ID + " = '"
						+ item_pool_id + "' AND "+DBFeilds.OFFLINE_SCAN_TIME+" = '"+scan_time+"'");

				attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause_all_Attendees,where_clause_all_Attendees_2);
				if(attendee_cursor.moveToFirst()){
					txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
					txt_checkin.setText("My Check-ins: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='true'"));
					txt_checkout.setText("My Check-outs: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='false'"));

					if(adapter != null){
						adapter.changeCursor(attendee_cursor);
						adapter.notifyDataSetChanged();
					}else{
						adapter = new SessionAttendeesAdapter(SessionListActivity.this, attendee_cursor);
						list_session.setAdapter(adapter);
					}
				}

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
	private void showNoAttendeesAlert(){
		Util.setCustomAlertDialog(SessionListActivity.this);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				search_view.setText("");
				searchFucntion();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();

			}
		});
		Util.openCustomDialog("Alert", "The search attende is not found in the list, Please refresh the list.");
	}

	public void openFilterDialog(Context ctx) {
		try {
			final Dialog sort_dialog = new Dialog(ctx);
			sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			sort_dialog.setContentView(R.layout.filter_dialog_layout);

			// Grab the window of the dialog, and change the width
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			Window window = sort_dialog.getWindow();
			lp.copyFrom(window.getAttributes());
			// This makes the dialog take up the full width
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			sort_dialog.getWindow().setAttributes(lp);


			TextView txt_sortby = (TextView) sort_dialog.findViewById(R.id.txtsortby);
			TextView txt_sort1 = (TextView) sort_dialog.findViewById(R.id.txtsort1);
			TextView txt_sort2 = (TextView) sort_dialog.findViewById(R.id.txtsort2);
			TextView txt_sort3 = (TextView) sort_dialog.findViewById(R.id.txtsort3);
			TextView txt_sort4 = (TextView)sort_dialog.findViewById(R.id.txtsort4);
			FrameLayout line4 = (FrameLayout)sort_dialog.findViewById(R.id.line4);
			line4.setVisibility(View.VISIBLE);
			TextView txt_sort_cancel = (TextView) sort_dialog.findViewById(R.id.txtsortcancel);

			txt_sortby.setTypeface(Util.roboto_regular);
			txt_sort1.setTypeface(Util.roboto_regular);
			txt_sort2.setTypeface(Util.roboto_regular);
			txt_sort3.setTypeface(Util.roboto_regular);

			txt_sort_cancel.setTypeface(Util.roboto_regular);
			sort_dialog.show();

			txt_sortby.setText("FILTER BY");
			txt_sort1.setText("ALL");
			txt_sort2.setText("CHECKED IN");
			txt_sort3.setText("CHECKED OUT");
			txt_sort4.setText(DBFeilds.STATUS_INVALID.toUpperCase());
			txt_sort4.setVisibility(View.VISIBLE);

			txt_sort_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					sort_dialog.dismiss();
				}
			});

			txt_sort1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					filter_by = "ALL";
					isFilter = true;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort1);

				}
			});
			txt_sort2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					filter_by = "CHECKED IN";
					isFilter = true;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort2);
				}
			});
			txt_sort3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					filter_by = "CHECKED OUT";
					isFilter = true;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort3);
				}
			});

			txt_sort4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					filter_by = "INVALID";
					isFilter = true;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort4);
				}
			});

		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	public void openSortDialog(Context ctx) {

		try {
			final Dialog sort_dialog = new Dialog(ctx);
			sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			sort_dialog.setContentView(R.layout.filter_dialog_layout);

			// Grab the window of the dialog, and change the width
			WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
			Window window = sort_dialog.getWindow();
			lp.copyFrom(window.getAttributes());
			// This makes the dialog take up the full width
			lp.width = WindowManager.LayoutParams.MATCH_PARENT;
			lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
			sort_dialog.getWindow().setAttributes(lp);
			FrameLayout line1 = (FrameLayout) sort_dialog.findViewById(R.id.line4);

			TextView txt_sortby = (TextView) sort_dialog.findViewById(R.id.txtsortby);
			TextView txt_sort1 = (TextView) sort_dialog.findViewById(R.id.txtsort1);
			TextView txt_sort2 = (TextView) sort_dialog.findViewById(R.id.txtsort2);
			TextView txt_sort3 = (TextView) sort_dialog.findViewById(R.id.txtsort3);
			TextView txt_sort4 = (TextView) sort_dialog.findViewById(R.id.txtsort4);

			TextView txt_sort_cancel = (TextView) sort_dialog.findViewById(R.id.txtsortcancel);

			txt_sortby.setText("SORT BY");
			txt_sort1.setText("FIRST NAME");
			txt_sort2.setText("LAST NAME");
			txt_sort3.setText("DATE");
			txt_sort4.setText("COMPANY");

			txt_sortby.setTypeface(Util.roboto_regular);
			txt_sort1.setTypeface(Util.roboto_regular);
			txt_sort2.setTypeface(Util.roboto_regular);
			txt_sort3.setTypeface(Util.roboto_regular);
			txt_sort4.setTypeface(Util.roboto_regular);
			// txt_sort5.setTypeface(Util.roboto_regular);
			// txt_sort6.setTypeface(Util.roboto_regular);
			txt_sort_cancel.setTypeface(Util.roboto_regular);

			line1.setVisibility(View.VISIBLE);
			txt_sort4.setVisibility(View.VISIBLE);

			sort_dialog.show();

			txt_sort1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					isFilter = false;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort1);
				}
			});
			txt_sort2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					isFilter = false;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort2);
				}
			});
			txt_sort3.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					isFilter = false;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort3);
				}
			});
			txt_sort4.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					isFilter = false;
					sort_dialog.dismiss();
					SortFunction(R.id.txtsort4);
				}
			});
			txt_sort_cancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					sort_dialog.dismiss();

				}
			});
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	private void SortFunction(int id) {

		String whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC";
		if (id == R.id.txtsort1) {
			if (!isFilter) {
				whereClause_name = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_FIRST_NAME+"  COLLATE NOCASE";
			} else {
				whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"'";
			}

		} else if (id == R.id.txtsort2) {
			if (!isFilter) {
				whereClause_name = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_LAST_NAME+"  COLLATE NOCASE";
			} else {
				whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+" = 'true'";
			}
		}  else if (id == R.id.txtsort3) {
			//Log.i("Attendee List", "R.id.txtsort3 clicked" + " isfilter= " + isFilter);
			if (!isFilter) {
				whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC";
			} else {

				whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+" = 'false'";
			}

		}else if (id == R.id.txtsort4) {
			if (!isFilter) {
				whereClause_name = "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' ORDER BY "+DBFeilds.SESSION_ATTENDEE_COMPANY+"  COLLATE NOCASE";
			}else{
				whereClause_name =  "where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_COMPANY+" = 'invalid'";
			}
		}

		if (attendee_cursor != null) {
			attendee_cursor.close();
		}

		if(isFilter){
			attendee_cursor = Util.db.getSessionAttendeeCursor(whereClause_name,whereClause_name+" ORDER BY "+DBFeilds.SESSION_ATTENDEE_SCANTIME+" DESC");
		}else{
			attendee_cursor = Util.db.getSessionAttendeeCursor(where_clause_all_Attendees,whereClause_name);
		}
		//attendee_cursor = Util.db.getSessionAttendeeCursor(whereClause_name,item_pool_id);

		txt_title.setText("Attendees (" + attendee_cursor.getCount() + ")");
		txt_checkin.setText("My Check-ins: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='true'"));
		txt_checkout.setText("My Check-outs: "+Util.db.getSessionAttendeeCount(" where "+DBFeilds.SESSION_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SESSION_EVENT_ID+"='"+checked_in_eventId+"' AND "+DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS+"='false'"));

		adapter = new SessionAttendeesAdapter(this, attendee_cursor);
		list_session.setAdapter(adapter);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// Log.i("---------------onActivity Result------------", ":" +
		// requestCode + " : " + resultCode);
		if(requestCode == 2018 && resultCode == 2018){
			isFromDetails = true;
		}
		//Log.i("------------On Resume isFromAttendDetails--------",":"+isFromAttendDetails+" : "+resultCode);
	}
}
