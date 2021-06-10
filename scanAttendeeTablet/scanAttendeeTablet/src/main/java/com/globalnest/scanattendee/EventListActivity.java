package com.globalnest.scanattendee;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.Settings.Secure;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.globalnest.BackgroundReciver.DownloadResultReceiver;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.appsessions.DeviceSessionId;
import com.globalnest.appsessions.DeviceSessionResponse;
import com.globalnest.appsessions.RevokeSessionsService;
import com.globalnest.appsessions.SessionListAdapter;
import com.globalnest.database.DBFeilds;
import com.globalnest.network.HttpGetMethod;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.AllPickListValues;
import com.globalnest.objects.EventObjects;
import com.globalnest.objects.LoginResponse;
import com.globalnest.objects.PaymentGateWaysRes;
import com.globalnest.objects.PaymentType;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListActivity extends BaseActivity implements DownloadResultReceiver.Receiver{
	private static final String CREATE_EVENT = "Create_Event";
	private static final String EDIT_EVENT = "Edit_Event";
	private DownloadResultReceiver mReceiver;
	//private Dialog sessionDialog;
	private int listPosition=-1;


	//GridView event_view;

	// EventListActivity eventlist;


	ArrayList<EventObjects> event_list_data = new ArrayList<EventObjects>();
	public String TAG = "EventList";
	String time = "", date = "", date1 = "", selected_event_id = "",
			selected_event_name = "", requestType = "",selected_event_pic="";
	//Calendar c = Calendar.getInstance();
	String[] namesOfMonth = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
			"Aug", "Sep", "Oct", "Nov", "Dec" };

	// HttpPostData1 postMethod;
	LinearLayout eventloader;
	ProgressDialog progressDialog;

	TextView txt_noevent;
	ViewPager mPager;
	//PagerTitleStrip strip;

	PagerSlidingTabStrip mIndicator;
	TextView txt_noevents;
	HashMap<String,String> _values = new HashMap<String, String>();
	// private UserProfile user_profile_contoller = new UserProfile();
	ArrayList<EventObjects> event_details_controller;
	Bitmap edit_event_logo = null;

	private List<DeviceSessionId> other_session_ids = new ArrayList<DeviceSessionId>();
	private int killingsessionpositions = -1;
	private SessionListAdapter sessionAdapter;

	private DeviceSessionResponse session_response = new DeviceSessionResponse();
	private Dialog tempSessionDialog = null;
	private TextView txt_sessionCount;
	//private ViewPager view_pager;
	List<AllPickListValues> pickListValuesArrayList;
	public class Group {
		String name="";
		int id=0;
		List<EventObjects> eventList;

		public Group(String name,int id,List<EventObjects> upCommingEve){
			this.name=name;
			this.id=id;
			this.eventList=upCommingEve;
		}
	}


	//private List<EventObjects> registeredEve = new ArrayList<EventObjects>();
	//private List<EventObjects> registeredEveTemp = new ArrayList<EventObjects>();
	private List<EventObjects> currentEvents = new ArrayList<EventObjects>();
	private List<EventObjects> pastEve = new ArrayList<EventObjects>();

	//private List<EventObjects> pastEveTemp = new ArrayList<EventObjects>();
	private List<Group> pagerGroup=new ArrayList<EventListActivity.Group>();
	//public List<EventObjects> events_list;
	//public EventsListAdapter t;
	//public ListView list_view;
	//private PagerSlidingTabStrip current;
	private Adapter mAdapter;
	private int pastPagerPosition=0;

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setCustomContentView(R.layout.eventlist_activity);
			//doRequest();

			if (!NullChecker(checked_in_eventId).isEmpty() && NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).isEmpty()) {
			/*if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				Intent i = new Intent(EventListActivity.this, SelfCheckinAttendeeList.class);
				startActivity(i);
			}
			else*/
				if (!Util.NullChecker(getIntent().getStringExtra(Util.EVENT_LIST)).equals(Util.EVENT_LIST)) {
					OFFLINESCANS = Util.db.getOfflinescanscount(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId + "' AND " + DBFeilds.OFFLINE_BADGE_STATUS + " != '" + DBFeilds.STATUS_INVALID + "'");
					Intent i = new Intent(EventListActivity.this, DashboardActivity.class);
					if (Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
						Util.checkin_only_pref.edit().putBoolean(sfdcddetails.user_id + checked_in_eventId, true).commit();
						Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, "Checkin").commit();
					} else {
						Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, "Checkinout").commit();

					}
					i.putExtra("CheckIn Event", checkedin_event_record);
					//i.putExtra("fromEventlist",true);
					startActivity(i);
					finish();
				}
			}
			img_addticket.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (Util.isMyServiceRunning(DownloadService.class, EventListActivity.this)) {
						showServiceRunningAlert(checkedin_event_record.Events.Name);
					} else {
						Intent i = new Intent(EventListActivity.this, AddEventActivity.class);
						i.putExtra(Util.EVENT_ACTION, CREATE_EVENT);
						i.putExtra(Util.EVENT_ID, "");
						startActivity(i);
					}

				}
			});

			img_setting.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent i = new Intent(EventListActivity.this, UserProfileActivity.class);
					i.putExtra(Util.INTENT_KEY_1, EventListActivity.class.getName());
					startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
				}

			});
			search_view.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
						if (motionEvent.getX() > (view.getWidth() - 50)) {
							search_view.setText("");
						}
					}
					return false;
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
					setEventAdapter();

				}
			});

			search_view.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					search_view.setFocusable(true);
					search_view.setHint("Search By Event Name");
					if (search_view.getText().toString().trim().length() > 1)
						searchByEventName(search_view.getText().toString());
				}


				@Override
				public void beforeTextChanged (CharSequence s,int start, int count, int after){

				}

				@Override
				public void afterTextChanged (Editable s){

				}

			});
			search_view.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					// TODO Auto-generated method stub
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						hideKeybord(v);
						return true;
					}
					return false;
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private void checkDeviceSessions(int position){
		requestType = WebServiceUrls.SA_DEVICE_SESSION;
		String _url = sfdcddetails.instance_url + WebServiceUrls.SA_DEVICE_SESSION+getSessionValues(event_list_data.get(position).Events.Id);
		postMethod = new HttpPostData("Please Wait...", _url, null, sfdcddetails.token_type+" "+sfdcddetails.access_token, this);
		postMethod.execute();

	}

	private String getSessionValues(String event_id){
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("appname", "ScanAttendee"));
		values.add(new BasicNameValuePair("DeviceType", Util.getDeviceName()+""));
		String android_id = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		values.add(new BasicNameValuePair("device", android_id));
		values.add(new BasicNameValuePair("token", sfdcddetails.refresh_token));
		values.add(new BasicNameValuePair("push", ""));
		values.add(new BasicNameValuePair("Event_id", event_id));
		return AppUtils.getQuery(values);
	}
	public void openDashBoard(int position) {
		if (!NullChecker(checked_in_eventId).isEmpty()&&checked_in_eventId.equalsIgnoreCase(event_list_data.get(position).Events.Id)) {
			Intent i = new Intent(EventListActivity.this, DashboardActivity.class);
			i.putExtra("CheckIn Event", event_list_data.get(position));
			if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)) {
				Util.checkin_only_pref.edit().putBoolean(sfdcddetails.user_id+checked_in_eventId, true).commit();
				Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id+checked_in_eventId, "Checkin").commit();
			}else {
				Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id+checked_in_eventId, "Checkinout").commit();

			}
			i.putExtra("fromEventlist",true);
			startActivity(i);
		} else {
			checkedin_event_record = Util.db.getSelectedEventRecord(event_list_data.get(position).Events.Id);
			String whereClause = " where EventId='" + event_list_data.get(position).Events.Id + "'";
			//Cursor c_ticket = Util.db.getTicketCursor(whereClause);
			if (Util.db.getItemsCount(whereClause) == 0) {

				/* Starting Download Service */
				mReceiver = new DownloadResultReceiver(new Handler());
				mReceiver.setReceiver(EventListActivity.this);
				Intent intent = new Intent(Intent.ACTION_SYNC, null, EventListActivity.this, DownloadService.class);
				/* Send optional extras to Download IntentService */
				String ticURL = sfdcddetails.instance_url + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id=" + event_list_data.get(position).Events.Id;
				//Cursor att_corsor=Util.db.getAttendeeDataCursor(" where "+DBFeilds.ATTENDEE_EVENT_ID+" = '"+ event_list_data.get(position).Events.Id+"'");
				String offset = Util.offset_pref.getString(event_list_data.get(position).Events.Id, "");
			/*if(att_corsor.getCount() > 0){
			        	att_corsor.moveToLast();
			        	offset = att_corsor.getString(att_corsor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
			        }*/

				String attURL = sfdcddetails.instance_url
						+ WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
						+ event_list_data.get(position).Events.Id + "&User_id=" + sfdcddetails.user_id
						+ "&offset=" + offset + "&limit="
						+ checkedin_event_record.Events.scan_attendee_limit__c;
				String badgeURL = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id=" + event_list_data.get(position).Events.Id;
				intent.putExtra(DownloadService.ATT_URL, attURL);
				intent.putExtra(DownloadService.TIC_URL, ticURL);
				intent.putExtra(DownloadService.BADGE_URL, badgeURL);
				intent.putExtra(DownloadService.ACCESSTOKEN, sfdcddetails.token_type + " " + sfdcddetails.access_token);
				intent.putExtra(DownloadService.EVENTID, event_list_data.get(position).Events.Id);
				intent.putExtra(DownloadService.REQUESTTYPE, "tickets");
				intent.putExtra(DownloadService.RECEIVER, mReceiver);
				intent.putExtra(DownloadService.ACTIVITY_NAME, EventListActivity.class.getName());
				intent.putExtra("requestId", 101);
				startService(intent);
			}
			Util.sessionCountPref.edit().putString(Util.isSessionRefreshed, "false").commit();// To fetch session records from session start time
			Util.sessionCountPref.edit().putString(Util.SessionsToBeRefreshed, "").commit();
			Util.sessionCountPref.edit().putBoolean(Util.isExternalDataDownloaded, false).commit();
			Util._savePreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID, event_list_data.get(position).Events.Id);
			Util.clearSharedPreference(Util.tempselfcheckinpref);
			Util.clearSharedPreference(Util.lastModifideDate);//To clear lastmodify date
			Intent i = new Intent(EventListActivity.this, DashboardActivity.class);
			Util.checkin_only_pref.edit().putBoolean(sfdcddetails.user_id+event_list_data.get(position).Events.Id, true).commit();
			Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id+checked_in_eventId,"Checkin").commit();
			i.putExtra("CheckIn Event", event_list_data.get(position));
			i.putExtra("fromEventlist",true);
			startActivity(i);
			//finish();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Util.slide_menu_id = 0;
	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}


	private void searchByEventName(String search_string){

		if(!NullChecker(search_string).isEmpty()){
			event_list_data = Util.db.getEventDetails("where EventName like '%"+search_string+"%'");
			if (event_list_data.size() > 0) {
				currentEvents.clear();
				pastEve.clear();
				currentEvents = preparePastEvents_NEW(event_list_data, 0);
				pastEve = preparePastEvents_NEW(event_list_data, 2);
				pagerGroup.clear();
				pagerGroup.add(new Group("Live Event", 1, currentEvents));
				pagerGroup.add(new Group("Past Event", 2, pastEve));
				pastPagerPosition = mPager.getCurrentItem();
				/*int pastPagerPosition=0;
				if(mPager.getAdapter()!=null&& mPager!=null)
					pastPagerPosition=((Adapter) mPager.getAdapter()).getPostion();*/

				mAdapter = new Adapter(this, pagerGroup);
				mPager.setAdapter(mAdapter);
				mIndicator.setViewPager(mPager);
				mPager.setCurrentItem(pastPagerPosition);
			}else{
				Toast.makeText(EventListActivity.this, "No Events found with this name!", Toast.LENGTH_LONG).show();
			}
		}else{
			setEventAdapter();
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			activity = null;
			if (!Util.isMyServiceRunning(DownloadService.class, EventListActivity.this)) {
				if (Util.db.isOpen()) {
					Util.db.close();
				}
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return true;

		}
		return super.onKeyDown(keyCode, event);


	}

	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " "+ sfdcddetails.access_token;
		if (requestType.equalsIgnoreCase(Util.EVENT_DETAILS)) {
			String url = sfdcddetails.instance_url+ WebServiceUrls.USER_EVENTS + "User_id="	+ sfdcddetails.user_id + "&appname=";
			postMethod = new HttpPostData("Loading Events...",url, null, access_token,EventListActivity.this);
			postMethod.execute();
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLN_MMS_GETALLPICKLISTVALUES)){
			String url = sfdcddetails.instance_url+ WebServiceUrls.SA_BLN_MMS_GETALLPICKLISTVALUES;
			getMehod = new HttpGetMethod(url, null, access_token,EventListActivity.this);
			getMehod.execute();
		}else {
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_DELETE_EVENT	+ "Event_id=" + selected_event_id;
			postMethod = new HttpPostData("Deleting Event...",url, null, access_token, EventListActivity.this);
			postMethod.execute();
		}
	}
	public void requestForKillSession(int position){
		killingsessionpositions = position;
		String access_token = sfdcddetails.token_type + " "+ sfdcddetails.access_token;
		requestType = WebServiceUrls.SA_DEVICE_SESSION_CANCEL;
		String _url = sfdcddetails.instance_url+WebServiceUrls.SA_DEVICE_SESSION_CANCEL+"SessionId="+other_session_ids.get(position).Id;
		postMethod = new HttpPostData("Removing Session...", _url, null, access_token, EventListActivity.this);
		postMethod.execute();
	}

	@Override
	public void insertDB() {
		try {
			if (listPosition != -1) {
				if (Util.dashboard_data_pref
						.getString(sfdcddetails.user_id + event_list_data.get(listPosition).Events.Id, "").isEmpty()) {

					final AlertDialogCustom dialog = new AlertDialogCustom(EventListActivity.this);
					dialog.setParamenters("Alert",
							"Event Data is not stored in offline, \n Please first download the data in online mode.",
							null, null, 2, false);
					dialog.setFirstButtonName("TURN ON");
					dialog.btnOK.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							BaseActivity.turnOnOffWifi(EventListActivity.this, true);
							dialog.dismiss();

						}
					});
					dialog.show();
					return;
				} else {
					openDashBoard(listPosition);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}

	@Override
	public void parseJsonResponse(String response) {
		//Log.i("---Event delete Response----", ":" + response);

		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}

			else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLN_MMS_GETALLPICKLISTVALUES)){
				Type listType = new TypeToken<List<AllPickListValues>>() {}.getType();
				 pickListValuesArrayList =  (List<AllPickListValues>) gson.fromJson(response, listType);
				Util.db.deleteTable(DBFeilds.TABLE_PICKLISTVALUES);
				Util.db.InsertandUpdatePicklistvalues(pickListValuesArrayList);
			}
			else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_DEVICE_SESSION)){
				session_response = new Gson().fromJson(response, DeviceSessionResponse.class);
				if(!session_response.msg.equalsIgnoreCase("Success")){
					openDashBoard(listPosition);
				}else if(session_response.sessionsTocancel.size()>session_response.SessionsCount){
					other_session_ids.clear();
					openSessionAlertDialog();
				}else{
					openDashBoard(listPosition);
				}
			}else if (requestType.equalsIgnoreCase(Util.EVENT_DETAILS)) {
				gson = new Gson();
				//eventloader.setVisibility(View.VISIBLE);
				LoginResponse login_response = gson.fromJson(response,LoginResponse.class);

				if (isRefresh) {
					//Util.db.deleteTable(DBFeilds.TABLE_EVENT_DETAILS);
					// Util.db.deleteTable(DBFeilds.TABLE_USER);
					//Util.db.deleteTable(DBFeilds.TABLE_ITEM_TYPE);
					Util.db.deleteTable(DBFeilds.TABLE_PAY_GATEWAY_KEYS);
					//Util.db.deleteTable(DBFeilds.TABLE_COUNTRY);
					//Util.db.deleteTable(DBFeilds.TABLE_STATE);
					Util.db.deleteTable(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS);
					Util.db.deleteTable(DBFeilds.TABLE_PAYMENT_GATEWAYS);
					//Util.db.deleteTable(DBFeilds.TABLE_SCANNED_TICKETS);
					isRefresh = false;
					Util.db.InsertCurrency(login_response.CurrencyList);
					Util.db.InsertAndUpdateUser(login_response.Profile);
					for (PaymentGateWaysRes pgateway : login_response.PGatewaytypes) {
						pgateway.BLN_PGateway_Type__r.BLN_Currency__c = pgateway.BLN_Currency__c;
						pgateway.BLN_PGateway_Type__r.BLN_PGateway_Type__c = pgateway.BLN_PGateway_Type__c;
						Util.db.Insert_Update_PGateways(pgateway.BLN_PGateway_Type__r);
					}
					for(PaymentType pay_key : login_response.PayGateways){
						Util.db.InsertAndUpdatePay_GAteway_Key(pay_key);
					}
					Util.db.InsertItemType(login_response.ItemTypes);


					PaymentType payment_keys = new PaymentType();
					payment_keys.Paygateway_name__c = getString(R.string.eventdex_stripe_keys);
					payment_keys.PG_User_Key__c = login_response.StripeEventdexClientID;
					payment_keys.PG_Pass_Secret__c = login_response.StripeEventdexSecretKey;
					Util.db.InsertAndUpdatePay_GAteway_Key(payment_keys);
					deleteEventFromLocal(login_response.Events);
					Util.db.InsertAndUpdateEvent(login_response.Events,false);

				}else{

					AppUtils.user_credentials.edit().putString(AppUtils.USER_EMAIL, login_response.Profile.Profile.Email__c).commit();
					Util.db.InsertCurrency(login_response.CurrencyList);
					Util.db.InsertAndUpdateUser(login_response.Profile);

					for (PaymentGateWaysRes pgateway : login_response.PGatewaytypes) {
						pgateway.BLN_PGateway_Type__r.BLN_Currency__c = pgateway.BLN_Currency__c;
						pgateway.BLN_PGateway_Type__r.BLN_PGateway_Type__c = pgateway.BLN_PGateway_Type__c;
						Util.db.Insert_Update_PGateways(pgateway.BLN_PGateway_Type__r);
					}
					for(PaymentType pay_key : login_response.PayGateways){
						Util.db.InsertAndUpdatePay_GAteway_Key(pay_key);
					}
					Util.db.InsertItemType(login_response.ItemTypes);


					PaymentType payment_keys = new PaymentType();
					payment_keys.Paygateway_name__c = getString(R.string.eventdex_stripe_keys);
					payment_keys.PG_User_Key__c = login_response.StripeEventdexClientID;
					payment_keys.PG_Pass_Secret__c = login_response.StripeEventdexSecretKey;
					Util.db.InsertAndUpdatePay_GAteway_Key(payment_keys);

					Util.db.InsertAndUpdateEvent(login_response.Events,true);
					//String sss=login_response.Events.get(0).lastRefreshDate;
				}

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						//eventloader.setVisibility(View.GONE);

						setEventAdapter();

					}




				});


			} else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_DEVICE_SESSION_CANCEL)){
				if(killingsessionpositions != -1){
					Intent i = new Intent(EventListActivity.this,RevokeSessionsService.class);
					i.putExtra(WebServiceUrls.SA_DEVICE_SESSION, other_session_ids.get(killingsessionpositions));
					startService(i);
					other_session_ids.remove(killingsessionpositions);
					sessionAdapter.notifyDataSetChanged();
					if(txt_sessionCount != null){
						session_response.tempSessionsCount=String.valueOf((other_session_ids.size()+1)- session_response.SessionsCount);
						// Log.i("--------------Cancel Session------------",":"+other_session_ids.size()+" : "+session_response.SessionsCount);
						txt_sessionCount.setText(Html.fromHtml("Your session <Font size='16' color='#2E8B57'> LIMIT </Font> "
								+ "exceeded.Remove <Font color='red'>"+session_response.tempSessionsCount+"</Font> session to continue"));
					}

					if(other_session_ids.size()<=session_response.SessionsCount){
						if(tempSessionDialog != null){
							tempSessionDialog.dismiss();
						}
						openDashBoard(listPosition);
					}/*else{
	                        openSessionAlertDialog();
	                    }*/
				}

			}else {

				JSONObject res_object = new JSONObject(response);
				final String status = res_object.optString("Eventstatus");
				if (status.equalsIgnoreCase("Deleted")) {
					if (Util.db
							.deleteEventData(res_object.optString("Eventid"))) {
						event_list_data.clear();
						event_list_data = Util.db.getEventDetails();

						Util.eventPrefer.edit().clear().commit();
						//   adapter.notifyDataSetChanged();
						setEventAdapter();
					}
				} else {
					/*AlertDialogCustom dialog=new AlertDialogCustom(EventListActivity.this);
	                    dialog.setParamenters("Error",selected_event_name+" "+res_object.optString("SuccessMsg"), null, null, 1, true);
	                    dialog.setAlertImage(R.drawable.alert_error, "error");
	                    dialog.show();*/
					//Util.openCustomDialog(selected_event_name, status);

					openEventDeleteAlert("Error",selected_event_name+" "+res_object.optString("SuccessMsg"));
				}
				//eventloader.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			/*runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

					// TODO Auto-generated method stub
					eventloader.setVisibility(View.GONE);
		        	startErrorAnimation(getResources().getString(R.string.network_error1),txt_error_msg);

				}
			});	*/
		}
	}

	private void openEventDeleteAlert(String alert,String msg) {
		Util.setCustomAlertDialog(EventListActivity.this);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_okey.setText("OK");
		Util.alert_dialog.setCancelable(false);

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
		Util.openCustomDialog(alert,msg);
	}

	/**
	 *
	 */



	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		try {
			View view = inflater.inflate(layout, null);
			linearview.addView(view);
			txt_title.setText("Events");
			img_menu.setVisibility(View.GONE);
			img_socket_scanner.setVisibility(View.GONE);
			img_scanner_base.setVisibility(View.GONE);
			img_search.setVisibility(View.VISIBLE);
			search_view.setHint("Search By Event Name");
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mPager = (ViewPager) linearview.findViewById(R.id.pager);
			mIndicator = (PagerSlidingTabStrip) linearview.findViewById(R.id.tabs);
			mIndicator.setShouldExpand(true);
			mIndicator.setIndicatorHeight(4);
			mIndicator.setIndicatorColor(getResources().getColor(R.color.scanattendee_icons_color));
			mIndicator.setPadding(0, 0, 0, 0);
			event_list_data = Util.db.getEventDetails();
			if (event_list_data.size() == 0) {
				callEventList();
			/*requestType=Util.EVENT_DETAILS;
			doRequest();*/
			} else {
				currentEvents = preparePastEvents_NEW(event_list_data, 0);
				pastEve = preparePastEvents_NEW(event_list_data, 2);
				pagerGroup.add(new Group("Live Event", 1, currentEvents));
				pagerGroup.add(new Group("Past Event", 2, pastEve));
				mAdapter = new Adapter(this, pagerGroup);
				mPager.setAdapter(mAdapter);
				mIndicator.setViewPager(mPager);
			}
			if (NullChecker(Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID)).isEmpty()) {
				event_layout.setVisibility(View.GONE);

			}
			img_setting.setVisibility(View.VISIBLE);
			//event_view = (GridView) linearview.findViewById(R.id.eventgridview);
			//event_view.setVisibility(View.GONE);
			//pull_to_refresh_listview_event=(PullToRefreshListView) linearview.findViewById(R.id.pull_to_refresh_listview_event);
			img_addticket.setVisibility(View.VISIBLE);
			eventloader = (LinearLayout) linearview.findViewById(R.id.loadevents);
			txt_noevent = (TextView) linearview.findViewById(R.id.txt_noevent);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setEventAdapter() {
		dismissProgressDialog();
		event_list_data.clear();
		event_list_data = Util.db.getEventDetails();
		if (event_list_data.size() > 0) {
			currentEvents.clear();
			pastEve.clear();
			currentEvents = preparePastEvents_NEW(event_list_data, 0);
			pastEve = preparePastEvents_NEW(event_list_data, 2);
			pagerGroup.clear();
			pagerGroup.add(new Group("Live Event", 1, currentEvents));
			pagerGroup.add(new Group("Past Event", 2, pastEve));

			/*if(mPager.getAdapter()!=null&& mPager!=null)
				pastPagerPosition=((Adapter) mPager.getAdapter()).getPostion();*/

			mAdapter = new Adapter(this, pagerGroup);
			mPager.setAdapter(mAdapter);
			mIndicator.setViewPager(mPager);
			mPager.setCurrentItem(pastPagerPosition);
		} else {
			//txt_noevent=(TextView) linearview.findViewById(R.id.txt_noevent);
			//txt_noevent.setVisibility(View.VISIBLE);
			Intent i = new Intent(EventListActivity.this, AddEventActivity.class);
			i.putExtra(Util.EVENT_ACTION, "CREATE");
			i.putExtra("EventId", "");
			startActivity(i);

		}
	}

	public static List<EventObjects> preparePastEvents_NEW(List<EventObjects> events_list,int type){
		List<EventObjects> eventsList = new ArrayList<EventObjects>();
		Date date = new Date();
		final SimpleDateFormat actualformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
		try {
			for (EventObjects eve : events_list) {
				if (type == 0) {
					if (actualformat.parse(eve.Events.End_Date__c).after(date)
							|| actualformat.parse(eve.Events.End_Date__c).equals(date)) {
						eventsList.add(eve);
					}
				} else {
					if (actualformat.parse(eve.Events.End_Date__c).before(date)) {
						eventsList.add(eve);
					}
				}

			}
			if(type != 0){
				Collections.sort(eventsList,new Comparator<EventObjects>() {

					@Override
					public int compare(EventObjects lhs, EventObjects rhs) {
						// TODO Auto-generated method stub
						try {
							Date d1 = actualformat.parse(lhs.Events.End_Date__c);
							Date d2 = actualformat.parse(rhs.Events.End_Date__c);
							return d2.compareTo(d1);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return 0;
					}
				});
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return eventsList;
	}
	private void callEventList() {
		final Bundle bundle=new Bundle();
		final boolean blogTitles = false;
		try {
			progressDialog =new ProgressDialog(EventListActivity.this);
			progressDialog.setMessage("Loading Events........");
			progressDialog.setCancelable(false);
			progressDialog.show();
			ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
			//Call<LoginResponse> call = apiService.getAttendeesurl(url[1],accessToken);
			String url = sfdcddetails.instance_url+ WebServiceUrls.USER_EVENTS + "User_id="	+ sfdcddetails.user_id + "&appname=";
			//postMethod = new HttpPostData("Loading Events...",url, null, access_token,EventListActivity.this);
			Call<LoginResponse> call = apiService.getEvents(sfdcddetails.user_id
					, "", sfdcddetails.token_type + " "+ sfdcddetails.access_token);
			if(AppUtils.isLogEnabled) {
				AppUtils.displayLog(call + "------ Url-------", url);
				AppUtils.displayLog(call + "------Access Token-------", sfdcddetails.access_token);
			}
			call.enqueue(new Callback<LoginResponse>() {
				@Override
				public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
					Log.e("-----Event success-----", "------response started-------");
					try {
						LoginResponse login_response = response.body();
						if (!isValidResponse(response.toString())) {
							dismissProgressDialog();
							openSessionExpireAlert(errorMessage(response.toString()));
						} else if (response.code() == 200) {
							if (isRefresh) {
								Util.db.deleteTable(DBFeilds.TABLE_PAY_GATEWAY_KEYS);
								Util.db.deleteTable(DBFeilds.TABLE_EVENT_PAYAMENT_SETTINGS);
								Util.db.deleteTable(DBFeilds.TABLE_PAYMENT_GATEWAYS);
								isRefresh = false;
								Util.db.InsertCurrency(login_response.CurrencyList);
								Util.db.InsertAndUpdateUser(login_response.Profile);
								for (PaymentGateWaysRes pgateway : login_response.PGatewaytypes) {
									pgateway.BLN_PGateway_Type__r.BLN_Currency__c = pgateway.BLN_Currency__c;
									pgateway.BLN_PGateway_Type__r.BLN_PGateway_Type__c = pgateway.BLN_PGateway_Type__c;
									Util.db.Insert_Update_PGateways(pgateway.BLN_PGateway_Type__r);
								}
								for (PaymentType pay_key : login_response.PayGateways) {
									Util.db.InsertAndUpdatePay_GAteway_Key(pay_key);
								}
								Util.db.InsertItemType(login_response.ItemTypes);
								PaymentType payment_keys = new PaymentType();
								payment_keys.Paygateway_name__c = getString(R.string.eventdex_stripe_keys);
								payment_keys.PG_User_Key__c = login_response.StripeEventdexClientID;
								payment_keys.PG_Pass_Secret__c = login_response.StripeEventdexSecretKey;
								Util.db.InsertAndUpdatePay_GAteway_Key(payment_keys);
								deleteEventFromLocal(login_response.Events);
								Util.db.InsertAndUpdateEvent(login_response.Events, false);
							} else {
								AppUtils.user_credentials.edit().putString(AppUtils.USER_EMAIL, login_response.Profile.Profile.Email__c).commit();
								Util.db.InsertCurrency(login_response.CurrencyList);
								Util.db.InsertAndUpdateUser(login_response.Profile);

								for (PaymentGateWaysRes pgateway : login_response.PGatewaytypes) {
									pgateway.BLN_PGateway_Type__r.BLN_Currency__c = pgateway.BLN_Currency__c;
									pgateway.BLN_PGateway_Type__r.BLN_PGateway_Type__c = pgateway.BLN_PGateway_Type__c;
									Util.db.Insert_Update_PGateways(pgateway.BLN_PGateway_Type__r);
								}
								for (PaymentType pay_key : login_response.PayGateways) {
									Util.db.InsertAndUpdatePay_GAteway_Key(pay_key);
								}
								Util.db.InsertItemType(login_response.ItemTypes);
								PaymentType payment_keys = new PaymentType();
								payment_keys.Paygateway_name__c = getString(R.string.eventdex_stripe_keys);
								payment_keys.PG_User_Key__c = login_response.StripeEventdexClientID;
								payment_keys.PG_Pass_Secret__c = login_response.StripeEventdexSecretKey;
								Util.db.InsertAndUpdatePay_GAteway_Key(payment_keys);

								Util.db.InsertAndUpdateEvent(login_response.Events, true);
							}
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub

									setEventAdapter();
									AppUtils.givePhoneStateandStoragepermission(EventListActivity.this);
								}
							});
							requestType=WebServiceUrls.SA_BLN_MMS_GETALLPICKLISTVALUES;
							doRequest();
							Log.e("------success-------", "------inserrtion ended-------");
						}else {
							dismissProgressDialog();
							openSessionExpireAlert(errorMessage(response.errorBody().string()));
						}
					}catch (Exception e){
						dismissProgressDialog();
						requestType = Util.EVENT_DETAILS;//"GET EVENT";
						doRequest();
						e.printStackTrace();
					}
				}
				@Override
				public void onFailure(Call<LoginResponse> call, Throwable t) {
					// Log error here since request failed
					Log.e("------failure-------", t.toString());
					dismissProgressDialog();
				}
			});
		}catch (Exception e){
			dismissProgressDialog();
			e.printStackTrace();
		}
	}


	public static List<EventObjects> preparePastEvents(List<EventObjects> events_list,int type){

		List<EventObjects> eventsList = new ArrayList<EventObjects>();
		Date date = new Date();
		final String currentDate= new SimpleDateFormat("MMM dd, yyyy hh:mm a").format(date);
		try {
			for(EventObjects eve : events_list){
				try {
					String end_date = Util.change_US_ONLY_DateFormat(eve.Events.End_Date__c.substring(0, 19),eve.Events.Time_Zone__c);

					int dateType = compareDates(end_date,currentDate,false);
					if(type == 0 && (dateType == -2 || dateType == 0)){
						eventsList.add(eve);
					}else if(type == 2 && (dateType == 2)){


						eventsList.add(eve);
					}
				} catch (Exception e) {
					eventsList.add(eve);
					e.printStackTrace();
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		Collections.sort(events_list, new Comparator<EventObjects>() {
			@Override
			public int compare(EventObjects obj1, EventObjects obj2){
				String date = Util.change_US_ONLY_DateFormat(obj1.Events.Start_Date__c.substring(0, 19),obj1.Events.Time_Zone__c);
				String second = Util.change_US_ONLY_DateFormat(obj2.Events.Start_Date__c.substring(0, 19),obj2.Events.Time_Zone__c);
				return  currentDate.format(date).compareTo(currentDate.format(second));

			}
		});
		Collections.reverse(events_list);
		return eventsList;
	}
	public static int compareDates(String strDate1,String strDate2,boolean isReverse){
		try{

			DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
			String dateInString = strDate1;

			Date date1 = formatter.parse(dateInString);

			String dateInString1 = strDate2;

			Date date2 = formatter.parse(dateInString1);

			if(isReverse){
				if(date1.after(date2)){
					return 2;
				}

				if(date1.before(date2)){
					return -2;
				}

				if(date1.equals(date2)){
					return 0;
				}
			}else{
				if(date1.after(date2)){
					return -2;
				}

				if(date1.before(date2)){
					return 2;
				}

				if(date1.equals(date2)){
					return 0;
				}
			}


		}catch (Exception e) {
			e.printStackTrace();
		}
		return 99;
	}


	private class Adapter extends PagerAdapter{
		int pagePostion=0;
		private View v;


		private Context context;
		private List<Group> grpupList;

		public Adapter(Context context,List<Group> pagerGroup) {
			this.context=context;
			this.grpupList=pagerGroup;
		}

		@Override
		public int getCount() {

			return grpupList.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
		@Override
		public CharSequence getPageTitle(int position) {

			switch (position){
				case 0:

					return "Live Event";
				case 1:
					return "Past Event";

			}
			return super.getPageTitle(position);
			/*public CharSequence getPageTitle(int position){
			return grpupList.get(position).name;
			}*/
		}
		public int getPostion(){
			return pagePostion;
		}





		@Override
		public Object instantiateItem(View collection, final int position) {

			pagePostion=position;
			// //Log.i("---------Collect Order Info------------",":instantiateItem Position= "+position+" Index= "+index);
			v = ((Activity) context).getLayoutInflater().inflate(R.layout.events_pager_item, null);

			final PullToRefreshListView pull_to_refresh_listview_event=(PullToRefreshListView)v.findViewById(R.id.list_events);
			txt_noevents =(TextView)v.findViewById(R.id.txt_noevents);
			if(position==0){
				event_list_data=(ArrayList<EventObjects>) grpupList.get(position).eventList;
				if(event_list_data.size()==0){
					txt_noevents.setVisibility(View.VISIBLE);
				}
			}else{
				event_list_data=(ArrayList<EventObjects>) grpupList.get(position).eventList;
				txt_noevents.setVisibility(View.GONE);
			}
			pull_to_refresh_listview_event.setAdapter(new ViewPageAdapter(inflater, grpupList.get(position).eventList, context));
			/*}else{
			 *

			}*/

			pull_to_refresh_listview_event.setTag(position);
			pull_to_refresh_listview_event.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					listPosition=position;
					int pagerPosition=(int)pull_to_refresh_listview_event.getTag();
					Group group=grpupList.get(pagerPosition);
					EventObjects event_data=group.eventList.get(position);
					event_list_data=(ArrayList<EventObjects>) group.eventList;
					if(!NullChecker(event_data.Events.Id).isEmpty() && !NullChecker(event_data.Events.Id).equalsIgnoreCase(event_data.Events.Id)
							&& Util.isMyServiceRunning(DownloadService.class, EventListActivity.this)){
						showServiceRunningAlert(event_data.Events.Name);
					}else if(!isOnline()){
						if(Util.dashboard_data_pref.getString(sfdcddetails.user_id+event_data.Events.Id, "").isEmpty()){

							final AlertDialogCustom dialog=new AlertDialogCustom(EventListActivity.this);
							dialog.setParamenters("Alert", "Event Data is not stored in offline, \n Please first download the data in online mode.", null, null, 2, false);
							dialog.setFirstButtonName("TURN ON");
							dialog.btnOK.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									// TODO Auto-generated method stub
									BaseActivity.turnOnOffWifi(EventListActivity.this, true);
									dialog.dismiss();
								}
							});
							dialog.show();
							return;
						}else{
							openDashBoard(position);
						}
					}else if(!Util.eventPrefer.getString(Util.SESSION_EVENT_ID, "").equals(event_data.Events.Id)){
						checkDeviceSessions(position);
					}else{
						openDashBoard(position);
					}
				}
			});





			pull_to_refresh_listview_event.setTag(position);
			pull_to_refresh_listview_event.setOnRefreshListener(new com.globalnest.utils.PullToRefreshListView.OnRefreshListener(){

				@Override
				public void onRefresh() {
					pagePostion=(int) pull_to_refresh_listview_event.getTag();
					if(isOnline())
					{
						isRefresh = true;
						callEventList();
						/*requestType = Util.EVENT_DETAILS;//"GET EVENT";
						doRequest();*/
					}else{

						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}

					// Make sure you call listView.onRefreshComplete()
					// when the loading is done. This can be done from here or any
					// other place, like on a broadcast receive from your loading
					// service or the onPostExecute of your AsyncTask.

					// For the sake of this sample, the code will pause here to
					// force a delay when invoking the refresh
					pull_to_refresh_listview_event.postDelayed(new Runnable() {

						@Override
						public void run() {
							pull_to_refresh_listview_event.onRefreshComplete();
						}
					}, 2000);
				}
			});

			((ViewPager) collection).addView(v, 0);

			return v;
		}



		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);

		}



		@Override
		public boolean isViewFromObject(View view, Object object) {

			return view == ((View) object);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {

			return null;
		}

		/*public int getCurrentPosition(){
			return
		}*/

		@Override
		public void startUpdate(View arg0) {

		}

	}
	public void showEventAlert(String event_name){
		Util.setCustomAlertDialog(EventListActivity.this);
		Util.setCustomDialogImage(R.drawable.alert);
		Util.txt_okey.setText("OK");
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.alert_dialog.setCancelable(false);
		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Util.alert_dialog.dismiss();
			}
		});
		Util.openCustomDialog("Alert","You dont have permission to edit or delete the "+event_name+".");
	}
	/* (non-Javadoc)
	 * @see com.globalnest.BackgroundReciver.DownloadResultReceiver.Receiver#onReceiveResult(int, android.os.Bundle)
	 */
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if (progress_download_data != null) {
			progress_download_data.setMax(Util.dashboardHandler.totalOrders);
			progress_download_data.clearAnimation();
			progress_download_data.getCircularProgressBar().setCircleWidth(10);

		}



		//Log.i("--------------Result Code In event-----------",":"+resultCode+":"+txt_download_attendees_count);
		if (resultCode == DownloadService.STATUS_FINISHED || resultCode == DownloadService.STATUS_ERROR) {
			if(progress_download_data != null){
				progress_download_data.setVisibility(View.GONE);
			}

			if(img_download != null){
				img_download.setVisibility(View.VISIBLE);

			}
			if (resultCode == DownloadService.STATUS_ERROR) {
				if (txt_download_attendees_count != null) {
					new CountDownTimer(5000,1000) {

						@Override
						public void onTick(long millisUntilFinished) {
							// TODO Auto-generated method stub
							txt_download_attendees_count.setText("Error in downloading data, Please check internet connection.");
							txt_download_attendees_count.setTextColor(getResources().getColor(R.color.red));

						}

						@Override
						public void onFinish() {
							// TODO Auto-generated method stub
							txt_download_attendees_count.setText("You have downloaded " + Util.db.totalOrderCount(checked_in_eventId)
									+ " Attendees out of " + Util.dashboardHandler.totalOrders);
							txt_download_attendees_count.setTextColor(getResources().getColor(R.color.gray_color));
						}

					}.start();
				}





			}

		} else {
			if(txt_download_attendees_count != null){
				checked_in_eventId = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);
				txt_download_attendees_count.setText("You have downloaded "
						+ Util.db.totalOrderCount(checked_in_eventId) + " Attendees out of " + Util.dashboardHandler.totalOrders);
			}
			if (progress_download_data != null) {
				progress_download_data.setProgress(Util.db.totalOrderCount(checked_in_eventId));
			}

		}
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + resultCode);
		if(requestCode==Util.DASHBORD_ONACTIVITY_REQ_CODE){
			Intent i = new Intent(EventListActivity.this, SplashActivity.class);
			startActivity(i);
			finish();

		}
	}
	private void dismissProgressDialog() {
		if(progressDialog!=null) {
			if(progressDialog.isShowing())
				progressDialog.dismiss();
		}
	}
	private void openSessionAlertDialog() {
		tempSessionDialog=new Dialog(this);
		tempSessionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		tempSessionDialog.setContentView(R.layout.session_alert_dialog);
		txt_sessionCount = (TextView) tempSessionDialog.findViewById(R.id.txt_sessionCount);
		TextView txt_deviceType = (TextView) tempSessionDialog.findViewById(R.id.txt_deviceType);
		TextView txt_lastAccessed = (TextView) tempSessionDialog.findViewById(R.id.txt_lastAccessed);
		ListView list_session=(ListView) tempSessionDialog.findViewById(R.id.list_session);
		ImageView img_close=(ImageView)tempSessionDialog.findViewById(R.id.img_close);

		/*DeviceSessionResponse response =new DeviceSessionResponse();
        response.msg=session_response.msg;
        response.SessionsCount=session_response.SessionsCount;
        response.tempSessionsCount=String.valueOf(session_response.sessionsTocancel.size()-Integer.parseInt(session_response.SessionsCount));*/
		//Log.i("-----------------Sesssion Ids count----------------",":"+other_session_ids.size()+" : "+other_session_ids.isEmpty());
		if(other_session_ids.isEmpty()){
			for(DeviceSessionId deviceSessionId: session_response.sessionsTocancel){
				if(deviceSessionId.Id.equalsIgnoreCase(session_response.currentSessionId)){
					Util.eventPrefer.edit().putString(Util.EVENT_CURRENT_SESSION_ID,deviceSessionId.Id).commit();
					txt_deviceType.setText(deviceSessionId.DeviceType__c);
					txt_lastAccessed.setText(Util.sessionDateTimeFormat(deviceSessionId.LastModifiedDate));
				}else{
					other_session_ids.add(deviceSessionId);
				}
			}

			sessionAdapter=new SessionListAdapter(other_session_ids,EventListActivity.this,tempSessionDialog);
			list_session.setAdapter(sessionAdapter);
		}

		session_response.tempSessionsCount=String.valueOf((other_session_ids.size()+1)- session_response.SessionsCount);
		//Log.i("--------------Cancel Session------------",":"+other_session_ids.size()+" : "+session_response.SessionsCount);
		txt_sessionCount.setText(Html.fromHtml("Your session <Font size='16' color='#2E8B57'> LIMIT </Font> "
				+ "exceeded.Remove <Font color='red'>"+session_response.tempSessionsCount+"</Font> session to continue"));

		img_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				tempSessionDialog.dismiss();
				cancelSessionCount(EventListActivity.this,true);
			}
		});
		tempSessionDialog.show();
	}

	private void deleteEventFromLocal(List<EventObjects> Events){
		List<String> server_id = new ArrayList<String>();
		for(EventObjects event : Events){
			server_id.add(event.Events.Id);
		}

		List<String> local_ids = Util.db.getEventIdList();
		for(String s : local_ids){
			if(!server_id.contains(s)){
				Util.db.deleteEventData(s);
			}
		}

	}
	/*OnRemoveSessionListener sessionListener=new OnRemoveSessionListener() {

        @Override
        public void removeSession(DeviceSessionResponse mResponse,
                DeviceSessionId currentSessionId) {
            Log.i("-----EventListActivity------", " ---- before count of session list ="+mResponse.sessionsTocancel.size()+" Session Count ="+mResponse.tempSessionsCount);
            for(int i=0;i<mResponse.sessionsTocancel.size();i++){
                if(mResponse.sessionsTocancel.get(i).Id.equals(currentSessionId.Id)){
                    mResponse.sessionsTocancel.remove(i);
                    mResponse.tempSessionsCount=String.valueOf(Integer.parseInt(mResponse.tempSessionsCount)-1);
                }
            }

            Log.i("-----EventListActivity------", " ---- after count of session list ="+mResponse.sessionsTocancel.size()+" Session Count ="+mResponse.tempSessionsCount);
            if((Integer.parseInt(mResponse.tempSessionsCount))>0){
                openSessionAlertDialog(mResponse);
            }else{
                openDashBoard(listPosition);
            }
        }


    };*/

}
