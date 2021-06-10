//  ScanAttendee Android
//  Created by Ajay on Nov 2, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.globalnest.BackgroundReciver.DownloadResultReceiver;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.Barchart.BarAttr;
import com.globalnest.Barchart.BarChartAdapter;
import com.globalnest.Barchart.ColorTemplates;
import com.globalnest.Barchart.ExpandableVerticalBarChartAdapter;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.DashboradHandler;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.scanattendee.ExpandablePanel.OnExpandListener;
import com.globalnest.stripe.android.compat.AsyncTask;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import tech.gusavila92.websocketclient.WebSocketClient;

import static com.globalnest.classes.MultiDirectionSlidingDrawer.LOG_TAG;

/**
 * @author laxmanamurthy
 */
public class DashboardActivity extends BaseActivity implements OnChartValueSelectedListener, DownloadResultReceiver.Receiver  {

	ExpandablePanel panel_statistics, panel_ticket_sales, panel_payments, panel_checkedin_out;
	ImageView img_statistics, img_ticket_sale, img_payments, img_checkin_out;
	private EventObjects event_data;
	private DashboradHandler dashboard_data = new DashboradHandler();
	private String request_type, todaydatestring;
	//private ArrayList<String> GroupNames=new ArrayList<String>();
	private ExpandableListView exp_dashboard;
	private LinearLayout emptyDashboard;
	private Typeface mTf;
	private FrameLayout layout_event_statistics, layout_checkedin_out;
	private boolean isPanelOpen = false,isStaticsExpandOpen=false,isFromEventlist=false;
	LinearLayout onsiterevenue_layout,totalrevenue_layout;
	WebSocketClient httpGet;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_dashboard);
		//createWebSocketClient();
		if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
			startActivity(new Intent(DashboardActivity.this, SelfCheckinAttendeeList.class));
			/*Intent i = new Intent(DashboardActivity.this, SelfCheckinAttendeeList.class);
			startActivity(i);*/
		}
		if (Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
			Util.checkin_only_pref.edit().putBoolean(sfdcddetails.user_id + checked_in_eventId, true).commit();
			Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, "Checkin").commit();
		} else {
			Util.scanmode_checkin_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, "Checkinout").commit();

		}
		isFromEventlist = getIntent().getBooleanExtra("fromEventlist", true);
		if (NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(Util.ADDEVENT)) {
			event_data = Util.db.getSelectedEventRecord(getIntent().getStringExtra(Util.ADDED_EVENT_ID));
		} else {
			event_data = (EventObjects) getIntent().getSerializableExtra("CheckIn Event");
		}

		if (event_data.Events.Id.isEmpty()) {
			Util.clearSharedPreference(Util.eventPrefer);
			Intent i = new Intent(DashboardActivity.this, EventListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}

		Util.slide_menu_id = R.id.homelayout;

		//mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		/*if (!isEventAdmin()) {
			//GroupNames.add("CheckIn-CheckOut Reports");

			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_statistics.setVisibility(View.GONE);
			panel_ticket_sales.setVisibility(View.GONE);
			panel_payments.setVisibility(View.GONE);
		} else if(event_data.Events.Revenue_visibility__c.equalsIgnoreCase("false")){
			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_statistics.setVisibility(View.GONE);
			panel_ticket_sales.setVisibility(View.GONE);
			panel_payments.setVisibility(View.GONE);
		}else {
			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_statistics.setVisibility(View.VISIBLE);
			panel_ticket_sales.setVisibility(View.VISIBLE);
			panel_payments.setVisibility(View.VISIBLE);
			*//*GroupNames.add("Event Statistics");
			GroupNames.add("Ticket Sales");
			GroupNames.add("Payment Reports");
			GroupNames.add("CheckIn-CheckOut Reports");*//*

		}*/
		setDashboardvisibility();
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mDrawerLayout.isDrawerOpen(left_menu_slider))
					mDrawerLayout.closeDrawer(left_menu_slider);
				else
					mDrawerLayout.openDrawer(left_menu_slider);
			}
		});

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {


                    if (isOnline()) {
                        request_type = Util.EVENT_REFRESH;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
					/*HttpClient _httpclient = HttpClientClass.getHttpClient(3000);
					HttpGet httpGet = new HttpGet("https://cs17.salesforce.com/services/data/v46.0/sobjects/PL_BLN_Event__e");
					httpGet.addHeader("Authorization", "Bearer " + "00Dg0000006Nl2Q!AQgAQJtg.TEFIbqqZFgZ_uTT4E3fmmKO3ohYyih1JNShbHrLNiFQc9BwQCS6y8Zu2I7O436zx55H2i6Mz1iBRTLjk0qAREFt");
					httpGet.addHeader("Content-type", "application/json");
					httpGet.addHeader("consumer_key", "3MVG9ahGHqp.k2_zXQS1Hefh0f6yrn7Dj6PFAty.rJKNEGtjF6ihS1d6juoLNdtF_Dark7lGqrQprTl9tBDhZ");
					httpGet.addHeader("consumer_secret", "F29E51CAFEAA808BD831653DC102178E4BAC65613ADE28AB8EFD807283F797CB");
					httpGet.addHeader("username", "durga@boothleads.com.uditgaurav");
					httpGet.addHeader("password", "Gaurav@UditQFFEiVaeUeeT5GfC0LmDF9PKw");
					httpGet.addHeader("sandbox", "True");
					HttpResponse _response = _httpclient.execute(httpGet);
					int _responsecode =_response.getStatusLine().getStatusCode();*/
				} catch (Exception e) {
					e.printStackTrace();
				}



				/*if (isOnline()) {
					request_type = Util.EVENT_REFRESH;
					doRequest();
				} else {
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				}*/

			}
		});



		panel_statistics.setOnExpandListener(new OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				img_statistics.setImageResource(R.drawable.minus_green);
				isStaticsExpandOpen=false;
				//panel_statistics.Expanded(true);

			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				img_statistics.setImageResource(R.drawable.plus_green);
				isStaticsExpandOpen=true;
				//panel_statistics.Expanded(false);
				//handle.performClick();
			}
		});

		panel_ticket_sales.setOnExpandListener(new OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				img_ticket_sale.setImageResource(R.drawable.minus_green);
			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				img_ticket_sale.setImageResource(R.drawable.plus_green);
			}
		});
		panel_payments.setOnExpandListener(new OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				img_payments.setImageResource(R.drawable.minus_green);
			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				img_payments.setImageResource(R.drawable.plus_green);
			}
		});

		panel_checkedin_out.setOnExpandListener(new OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				img_checkin_out.setImageResource(R.drawable.minus_green);
				isPanelOpen = true;
			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				img_checkin_out.setImageResource(R.drawable.plus_green);
				isPanelOpen = false;
			}
		});
		/*ApiInterface apiService = ApiClient.getNetworkConnectivity("https://www.google.com").create(ApiInterface.class);
		// Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
		Call<String> call = apiService.getIsNetAvailable();

		call.enqueue(new Callback<String>() {
			@Override
			public void onResponse(Call<String> call, Response<String> response) {
				Log.e(call+"------success-------", "------response started-------");
				if(AppUtils.isLogEnabled){AppUtils.displayLog(call+"------JSON Response-------", response.toString());}
				try {
					 if (response.code() == 200) {
						 Log.e("------200-------", "");
					}else {
						 Log.e("------no 200-------", "");
					 }
				}catch (Exception e) {
					e.printStackTrace();
					startErrorAnimation(
							getResources().getString(R.string.network_error1),
							txt_error_msg);
				}
			}

			@Override
			public void onFailure(Call<String> call, Throwable t) {
				// Log error here since request failed
				Log.e("------failure-------", t.toString());
			}
		});*/
//if (!isNetAvailable()) {
		//if (!isNetAvailable()) {
		if(!isOnline()){
			if (!Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim().isEmpty()) {
				//request_type=Util.DASHBORD;
				//parseJsonResponse(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""));
				dashboard_data = new Gson().fromJson(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim(), DashboradHandler.class);
				Util.dashboardHandler = dashboard_data;
				setDashboardDetails();
			} else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}
		} else {
			/*String url = sfdcddetails.instance_url + WebServiceUrls.SA_EVENT_DASHBOARD + "Event_id=" + checked_in_eventId;
			postMethod = new HttpPostData("Loading Dashboard...", url, null, access_token, DashboardActivity.this);
			postMethod.execute();*/
			/*DownloadResultReceiver mReceiver = new DownloadResultReceiver(new Handler());
			mReceiver.setReceiver(DashboardActivity.this);
			Intent intent = new Intent(Intent.ACTION_SYNC, null, DashboardActivity.this, DownloadService.class);
			String ticURL = sfdcddetails.instance_url + WebServiceUrls.SA_EVENT_DASHBOARD + "Event_id=" + checked_in_eventId;
			String sfdcurl = sfdcddetails.instance_url;
			intent.,putExtra(DownloadService.ATT_URL, sfdcurl);
			intent.putExtra(DownloadService.TIC_URL, ticURL);
			intent.putExtra(DownloadService.ACCESSTOKEN, sfdcddetails.token_type + " " + sfdcddetails.access_token);
			intent.putExtra(DownloadService.EVENTID, checked_in_eventId);
			intent.putExtra(DownloadService.REQUESTTYPE, "Statistics");
			intent.putExtra(DownloadService.RECEIVER, mReceiver);
			intent.putExtra(DownloadService.ACTIVITY_NAME, DashboardActivity.class.getName());
			intent.putExtra("requestId", 101);
			startService(intent);*/
			request_type = Util.DASHBORD;
			doRequest();
		}

		try {
			hideSoftKeyboard(DashboardActivity.this);

		} catch (Exception e) {
			e.printStackTrace();
		}
		ordersuccess_popupok_clicked=false;
		//Util.clearSharedPreference(Util.selfcheckinpref);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			hideSoftKeyboard(DashboardActivity.this);
			if(isEventAdmin()){
				panel_statistics.Expanded(true);
			}
			//setDashboardvisibility();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (checked_in_eventId.isEmpty()) {
			Intent i = new Intent(DashboardActivity.this, EventListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			finish();
		}/*else{
			setDashboardDetails();
		}*/
	}

	protected void onStart() {
		super.onStart();
	}

	protected void onPause() {
		super.onPause();
		//Log.i("------------------On Pause Dash Board Activity-----------",":");

	}

	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Util.slide_menu_id = 0;
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		if (isOnline()) {
			if (!NullChecker(checkedin_event_record.Events.last_modified_date).isEmpty()) {
				todaydatestring = checkedin_event_record.Events.last_modified_date.trim();
			} else {
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
				todaydatestring = format.format(date);
			}

			String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
			//Log.i("Dashbord Activity", "Request type====>"+request_type);
			if (request_type.equals(Util.EVENT_REFRESH)) {
				//loading.setVisibility(View.VISIBLE);
				String url = sfdcddetails.instance_url + WebServiceUrls.SA_REFRESH_URL + getValues(request_type);
				postMethod = new HttpPostData("Refreshing Event...", url, null, access_token, DashboardActivity.this);
				postMethod.execute();
			} else if (request_type.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)) {
				String url = sfdcddetails.instance_url + WebServiceUrls.SA_REFRESH_URL + getValues(request_type);
				postMethod = new HttpPostData("Refreshing Items And Orders...", url, null, access_token, DashboardActivity.this);
				postMethod.execute();
			} else if (request_type.equalsIgnoreCase(Util.LOAD_BADGE)) {
				String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id=" + checked_in_eventId;
				postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, DashboardActivity.this);
				postMethod.execute();
			} else if (request_type.equalsIgnoreCase(Util.DEVICE_SESSION)) {
				String _url = sfdcddetails.instance_url + WebServiceUrls.SA_DEVICE_SESSION + getSessionValues();
				postMethod = new HttpPostData("Session Validating...", _url, null, access_token, DashboardActivity.this);
				postMethod.execute();

			} else if (request_type.equalsIgnoreCase(WebServiceUrls.SA_GET_TICKET_LIST)) {
				String url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id=" + checked_in_eventId;
				postMethod = new HttpPostData("Loading Scanned Tickets...", url, null, access_token, DashboardActivity.this);
				postMethod.execute();
			} else {
				//loading.setVisibility(View.VISIBLE);
				String url = sfdcddetails.instance_url + WebServiceUrls.SA_EVENT_DASHBOARD + "Event_id=" + checked_in_eventId;
				postMethod = new HttpPostData("Loading Dashboard...", url, null, access_token, DashboardActivity.this);
				postMethod.execute();
			}
		} else {
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}
	}

	private String getValues(String request_type) {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("appname", ""));
		values.add(new BasicNameValuePair("ResetSett", "false"));
		if (request_type.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)) {
			//values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
			values.add(new BasicNameValuePair("Request_Flag", "Itemandattendees"));
			values.add(new BasicNameValuePair("LastModifiedDate", Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"")));
		} else {
			if(NullChecker(Util.lastModifideDate.getString(Util.EVENTREFRESHLASTMODITIFEDATE,"")).trim().isEmpty()){
				values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
			}else {
				values.add(new BasicNameValuePair("LastModifiedDate", Util.lastModifideDate.getString(Util.EVENTREFRESHLASTMODITIFEDATE,"")));
			}
			values.add(new BasicNameValuePair("Request_Flag", "Event"));
		}

		return AppUtils.getQuery(values);
	}

	private String getSessionValues() {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("appname", "ScanAttendee"));
		String android_id = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
		values.add(new BasicNameValuePair("device", android_id));
		values.add(new BasicNameValuePair("token", sfdcddetails.refresh_token));
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		return AppUtils.getQuery(values);
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
			}
			gson = new Gson();
			if (request_type.equals(Util.EVENT_REFRESH)) {
				RefreshResponse refresh = gson.fromJson(response, RefreshResponse.class);
				Util.db.InsertAndUpdateRefresh(refresh, checked_in_eventId, false);
				Util.lastModifideDate.edit().putString(Util.EVENTREFRESHLASTMODITIFEDATE,refresh.LastRefreshedDate).commit();
				setDashboardvisibility();
				hideAttendeeTab();
				request_type = WebServiceUrls.SA_GET_TICKET_LIST;
				doRequest();
			} else if (request_type.equals(Util.DASHBORD)) {
				if (isValidResponse(response)) {
					Util.dashboard_data_pref.edit().putString(sfdcddetails.user_id + checked_in_eventId, response).commit();
					dashboard_data = gson.fromJson(response, DashboradHandler.class);
					Util.dashboardHandler = dashboard_data;
					setDashboardDetails();
				} else {
					insertDB();
				}
				////Log.i(dashboard_data.dashboard_payment_type.size()+"----Ticket Size---",""+ dashboard_data.dashboard_tickets.size());
			} else if (request_type.equalsIgnoreCase(Util.LOAD_BADGE)) {
				Type listType = new TypeToken<List<BadgeResponseNew>>() {
				}.getType();
				List<BadgeResponseNew> badges = (List<BadgeResponseNew>) gson.fromJson(response, listType);
				//Log.i("---------------- parseJsonResponse Badge Size----------", ":" + badges.size());
				Util.db.deleteBadges(checked_in_eventId);
				sharedPreferences.edit().clear().commit();
				for (BadgeResponseNew badge : badges) {
					badge.badge.event_id = checked_in_eventId;
					Util.db.InsertAndUpdateBadgeTemplateNew(badge);
				}
				request_type = Util.DASHBORD;
				doRequest();
			} else if (request_type.equalsIgnoreCase(WebServiceUrls.SA_GET_TICKET_LIST)) {
				ItemsListResponse item_response = gson.fromJson(response, ItemsListResponse.class);
				Util.db.upadteItemListRecordInDB(item_response.Itemscls_infoList, checked_in_eventId);
				Util.db.InsertAndUpdateSEMINAR_AGENDA(item_response.agndaInfo);

				List<SessionGroup> group_list = Util.db.getGroupList(checked_in_eventId);
				Util.db.deleteEventScannedTicketsGroup(checked_in_eventId);
				Util.db.deleteEventScannedTickets(checked_in_eventId);

				if (!group_list.isEmpty()) {
					for (SessionGroup group : group_list) {
						for (SessionGroup server_group : item_response.userSessions) {
							if (group.Id.equalsIgnoreCase(server_group.Id)) {
								if (server_group.BLN_Session_users__r.records.size() > 0) {
									server_group.BLN_Session_users__r.records.get(0).DefaultValue__c = group.Scan_Switch;
								}

							}
						}
					}
				}
				Util.db.InsertAndUpdateSESSION_GROUP(item_response.userSessions);
				request_type = Util.DASHBORD;
				doRequest();
			}

		} catch (Exception e) {

			e.printStackTrace();
			//emptyDashboard.setVisibility(View.VISIBLE);

			startErrorAnimation(getResources().getString(R.string.connection_error),
					txt_error_msg);
		}

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub
		try {
			//Log.i("-------------Insert Db In Dash Board----------------",":"+Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim());

			if (!Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim().isEmpty()) {
				dashboard_data = new Gson().fromJson(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim(), DashboradHandler.class);
				Util.dashboardHandler = dashboard_data;
				//setEventStatistics();
				setDashboardDetails();
				//parseJsonResponse(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""));
			} else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}

	public void setDashboardDetails(){
		new LongRunningTask().execute();
	} /*{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				emptyDashboard.setVisibility(View.GONE);

				setCheckedin_out();
				setEventStatistics();
				setTicketSale();
				setPayments();

				if (isEventAdmin()) {
					//layout_checkedin_out.performClick();

					if (dashboard_data.sessionsReport.size() == 0) {
						panel_checkedin_out.setVisibility(View.GONE);
					} else {
						panel_checkedin_out.setVisibility(View.VISIBLE);
					}
					layout_event_statistics.performClick();
				}

				*//*DashboardListAdapter dashboardAdapter = new DashboardListAdapter(DashboardActivity.this, GroupNames,
						dashboard_data, checkedin_event_record, exp_dashboard);
				exp_dashboard.setAdapter(dashboardAdapter);
				exp_dashboard.expandGroup(0);*//*
	 *//*if(GroupNames.size()==1){
					Util.setListViewHeight(exp_dashboard,0);
				}else{
					Util.setListViewHeight(exp_dashboard,3);
				}*//*

			}
		});
	}*/

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(layout, null);
		linearview.addView(view);
		txt_title.setText("Dashboard");
		img_menu.setImageResource(R.drawable.top_more);
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		img_setting.setVisibility(View.VISIBLE);
		img_addticket.setImageResource(R.drawable.dashboardrefresh);
		img_addticket.setVisibility(View.VISIBLE);
		Util.slide_menu_id = R.id.homelayout;
		home_line.setVisibility(View.VISIBLE);
		panel_statistics = (ExpandablePanel) linearview.findViewById(R.id.expand_event_statistics);
		panel_ticket_sales = (ExpandablePanel) linearview.findViewById(R.id.expand_ticket_sales);
		panel_payments = (ExpandablePanel) linearview.findViewById(R.id.expand_payments);
		panel_checkedin_out = (ExpandablePanel) linearview.findViewById(R.id.expand_checkin_out);

		img_statistics = (ImageView) linearview.findViewById(R.id.img_statistics);
		img_ticket_sale = (ImageView) linearview.findViewById( R.id.img_ticket_sales);
		img_payments = (ImageView) linearview.findViewById(R.id.img_payments);
		img_checkin_out = (ImageView) linearview.findViewById(R.id.img_checkedin_out);

		layout_event_statistics = (FrameLayout) linearview.findViewById(R.id.layout_event_statistics);
		layout_checkedin_out = (FrameLayout) linearview.findViewById(R.id.layout_checkedin_out);
		emptyDashboard = (LinearLayout) linearview.findViewById(R.id.lay_emptyDashboard);

	}


	public void setEventStatistics() {
		int totalCheckins = 0;
		onsiterevenue_layout = (LinearLayout) linearview.findViewById(R.id.onsiterevenue_layout);
		totalrevenue_layout = (LinearLayout) linearview.findViewById(R.id.totalrevenue_layout);
		TextView totalOrders = (TextView) linearview.findViewById(R.id.txt_ttlOrders);
		TextView totalAttendees = (TextView) linearview.findViewById(R.id.txt_ttlAttendees);
		TextView totalRevenue = (TextView) linearview.findViewById(R.id.total_revGenerated);
		TextView onsiteOrders = (TextView) linearview.findViewById(R.id.onsite_totalorders);
		TextView onsiteAttendees = (TextView) linearview.findViewById(R.id.onsite_totalAttendees);
		TextView onsiterevenue = (TextView) linearview.findViewById(R.id.onsite_revenue);

		/*for(int i=0; i< mDahboardHandler.Checkins.size(); i++){

			if(Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount)!=0)
			{

				totalCheckins=totalCheckins+Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount);
			}

		}*/
		totalOrders.setText(String.valueOf((int) dashboard_data.totalOrders));
		totalAttendees.setText(String.valueOf((int) dashboard_data.totalAttendee));
		/*Double i=dashboard_data.totalrevenue;
		NumberFormat format = NumberFormat.getCurrencyInstance();
		totalRevenue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + format.format(i));
		*/
		totalRevenue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(dashboard_data.totalrevenue));
		onsiteOrders.setText(String.valueOf((int) dashboard_data.OnsiteOrderCount));
		onsiteAttendees.setText(String.valueOf((int) dashboard_data.OnsiteAttendeereg));
		onsiterevenue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(dashboard_data.OnsitePaymentRevenue));
		if(event_data.Events.Revenue_visibility__c.equalsIgnoreCase("false")){
			onsiterevenue_layout.setVisibility(View.GONE);
			totalrevenue_layout.setVisibility(View.GONE);
		}else {
			onsiterevenue_layout.setVisibility(View.VISIBLE);
			totalrevenue_layout.setVisibility(View.VISIBLE);
		}
		/*ttlTicket.setText(String.valueOf((int) dashboard_data.totalTickets));
		ttlAttendee.setText(String.valueOf((int) dashboard_data.totalAttendee));
		ttlCheckin.setText(String.valueOf((int) dashboard_data.chekinCont + "/" + (dashboard_data.checloutCnt + dashboard_data.notCheckdin)));
		ttlOrders.setText(String.valueOf((int) dashboard_data.totalcompOrders));
		ttlSold.setText(String.valueOf((int) dashboard_data.OnsiteAttendeereg));
		ttlRevGenerated.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(dashboard_data.totalrevenue));
	*/

	}

	public void setTicketSale() {
		TextView txt_ticmoreinfo = (TextView) linearview.findViewById(R.id.txt_ticmoreinfo);
		LinearLayout noTicketLay = (LinearLayout) linearview.findViewById(R.id.noTicketLay);
		ListView listview = (ListView) linearview.findViewById(R.id.list_expand);
		txt_ticmoreinfo.setVisibility(View.GONE);
		double frontPercentage = 0.0;
		ArrayList<BarAttr> barAttr = new ArrayList<BarAttr>();
		if (dashboard_data.dashboardticketsList.size() > 0) {
			for (int i = 0; i < dashboard_data.dashboardticketsList.size(); i++) {

				int total_qty = dashboard_data.dashboardticketsList.get(i).item.item_count__c;
				//+ mDahboardHandler.dashboardticketsList.get(i).gettAvailable();
				if (total_qty != 0) {
					frontPercentage = (dashboard_data.dashboardticketsList
							.get(i).ItemQuantity * 100) / total_qty;

				} else {
					frontPercentage = 0.0;

				}
				String revenue = Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(dashboard_data.dashboardticketsList.get(i).itrevenue);

				BarAttr attr = new BarAttr();
				event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
				if(event_data.Events.Revenue_visibility__c.equalsIgnoreCase("false")) {
					attr.barBottomLabels = dashboard_data.dashboardticketsList.get(i).item.item_name__c;
				}else
					attr.barBottomLabels = dashboard_data.dashboardticketsList.get(i).item.item_name__c + " " + revenue;
				attr.barTopLabels = String.valueOf(dashboard_data.dashboardticketsList.get(i).ItemQuantity + "/" + total_qty);
				attr.frontBarPercentage = frontPercentage;
				attr.backBarPercentage = 0.0;
				// attr.barColor=ColorTemplates.VORDIPLOM_COLORS;
				barAttr.add(attr);
			}


			BarChartAdapter adapter = new BarChartAdapter(DashboardActivity.this, barAttr);
			listview.setAdapter(adapter);
			Util.setListViewHeightBasedOnChildren(listview);
		}

	}

	public void setPayments() {
		TextView totalSales = (TextView) linearview.findViewById(R.id.txttotalsales);
		FrameLayout paymentLayout = (FrameLayout) linearview.findViewById(R.id.paymentLayout);
		FrameLayout nopaymentLayout = (FrameLayout) linearview.findViewById(R.id.nopaymentLayout);
		PieChart pieChart = (PieChart) linearview.findViewById(R.id.pieChart);
		ListView paymentList = (ListView) linearview.findViewById(R.id.paymentview);
		LinearLayout paymentpielayout_notfound = (LinearLayout) linearview.findViewById(R.id.paymentpielayout_notfound);
		int total_sales = 0;

		int[] colors = new int[dashboard_data.dashboardTicketsPaymentList.size()];

		String[] slicesName = new String[dashboard_data.dashboardTicketsPaymentList.size()];
		float[] slicePercent = new float[dashboard_data.dashboardTicketsPaymentList.size()];

		if (dashboard_data.dashboardTicketsPaymentList.size() > 0) {
			for (int i = 0; i < dashboard_data.dashboardTicketsPaymentList.size(); i++) {
				total_sales += dashboard_data.dashboardTicketsPaymentList.get(i).revenue;

			}
		}

		if (dashboard_data.dashboardTicketsPaymentList.size() > 0) {
			for (int i = 0; i < dashboard_data.dashboardTicketsPaymentList.size(); i++) {
				//Log.i("--------------Revenue-------------",":"+mDahboardHandler.dashboardTicketsPaymentList.get(i).revenue+pieChart);
				if (dashboard_data.dashboardTicketsPaymentList.get(i).revenue > 0) {
					colors[i] = ColorTemplates.PAI_COLORS[i];

					slicePercent[i] = (float) ((dashboard_data.dashboardTicketsPaymentList.get(i).revenue * 100) / total_sales);
					slicesName[i] = "";
				}
			}
			setPieChart(slicesName, slicePercent, pieChart);
		} else {
			total_sales = 0;
		}
		paymentList.setAdapter(new PaymentAdapter(DashboardActivity.this, dashboard_data, checkedin_event_record));
		totalSales.setText("Total Sales: " + Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(total_sales));

	}

	public void setCheckedin_out() {
		final ArrayList<BarAttr> groupBarAttr = new ArrayList<BarAttr>();
		HashMap<Integer, ArrayList<BarAttr>> childBarAttr = new HashMap<Integer, ArrayList<BarAttr>>();
		//LinearLayout noTicketLay = (LinearLayout) linearview.findViewById(R.id.noTicketLay);
		final ExpandableListView listview = (ExpandableListView) linearview.findViewById(R.id.expanded_barchart);
		double frontPercentage = 0.0;
		int pTotalCheckin = 0, pTotalCheckout = 0;

		for (int i = 0; i < dashboard_data.sessionsReport.size(); i++) {
			pTotalCheckin = 0;
			pTotalCheckout = 0;
			boolean isFreeSession = false;
			BarAttr groupAttr = new BarAttr();

			pTotalCheckin = dashboard_data.sessionsReport.get(i).totalItemschkQty;
			pTotalCheckout = dashboard_data.sessionsReport.get(i).totalItemsbuyQty;
			if(pTotalCheckin==0){
				groupAttr.barBottomLabels = dashboard_data.sessionsReport.get(i).sess.Name;
			}else {
				groupAttr.barBottomLabels = dashboard_data.sessionsReport.get(i).sess.Name + " (" + pTotalCheckout + ")";
			}
			groupAttr.session_name = dashboard_data.sessionsReport.get(i).sess.Name;
			for (int j = 0; j < dashboard_data.sessionsReport.get(i).lineItems.size(); j++) {
				String item_pool_id = dashboard_data.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id;
				if (Util.db.isItemPoolFreeSession(item_pool_id, checkedin_event_record.Events.Id)) {
					isFreeSession = true;
					break;
				}
			}
			if (isFreeSession) {
				frontPercentage = (pTotalCheckin * 100) / 100;
			} else if (pTotalCheckout != 0) {
				frontPercentage = (pTotalCheckin * 100) / (pTotalCheckout);
			} else {
				frontPercentage = 0.0;
			}
			if (isFreeSession) {
				groupAttr.barTopLabels = pTotalCheckin + "";
			} else {
				//groupAttr.barTopLabels = pTotalCheckin + "/" + (pTotalCheckout);
				groupAttr.barTopLabels = pTotalCheckin + "";
			}

			groupAttr.frontBarPercentage = frontPercentage;
			groupAttr.isType = true;
			groupAttr.isPackage = true;
			groupAttr.item_pool_c = dashboard_data.sessionsReport.get(i).sess.Id;
			groupBarAttr.add(i, groupAttr);

			if (dashboard_data.sessionsReport.get(i).lineItems.size() > 0) {

				ArrayList<BarAttr> childAttrList = new ArrayList<BarAttr>();
				//Log.i("----------------Session Name--------------",":"+dashboard_data.sessionsReport.get(i).sess.Name+ " : "+dashboard_data.sessionsReport.get(i).lineItems.size());

				for (int j = 0; j < dashboard_data.sessionsReport.get(i).lineItems.size(); j++) {
					//Log.i("-----------------pool Name--------------",":"+dashboard_data.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c+ " : "+dashboard_data.sessionsReport.get(i).lineItems.size());
					BarAttr childAttr = new BarAttr();
					String parent_id = Util.db.getItemPoolParentId(dashboard_data.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id, BaseActivity.checkedin_event_record.Events.Id);
					if (!Util.NullChecker(parent_id).isEmpty()) {
						String package_name = Util.db.getItem_Pool_Name(parent_id, BaseActivity.checkedin_event_record.Events.Id);
						childAttr.barBottomLabels = dashboard_data.sessionsReport.get(i).lineItems
								.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c + " ( " + package_name + " )" + " (" + dashboard_data.sessionsReport.get(i).lineItems.get(j).totalBuyQty + ")";
						childAttr.session_name = dashboard_data.sessionsReport.get(i).lineItems
								.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c + " ( " + package_name + " )";
					} else {
						childAttr.barBottomLabels = dashboard_data.sessionsReport.get(i).lineItems
								.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c + " (" + dashboard_data.sessionsReport.get(i).lineItems.get(j).totalBuyQty + ")";
						childAttr.session_name = dashboard_data.sessionsReport.get(i).lineItems
								.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c;
					}


					if (isFreeSession) {
						childAttr.barTopLabels = dashboard_data.sessionsReport.get(i).lineItems.get(j).chekinCont
								+ "";
					} else {
						childAttr.barTopLabels = dashboard_data.sessionsReport.get(i).lineItems.get(j).chekinCont
								+ "";
						/*childAttr.barTopLabels = dashboard_data.sessionsReport.get(i).lineItems.get(j).chekinCont
								+ "/" + ( dashboard_data.sessionsReport.get(i).lineItems.get(j).totalBuyQty);*/
					}


					childAttr.item_pool_c = dashboard_data.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id;

					if (isFreeSession) {
						childAttr.frontBarPercentage = (dashboard_data.sessionsReport.get(i).lineItems.get(j).chekinCont * 100) / 100;
					} else if (dashboard_data.sessionsReport.get(i).lineItems.get(j).totalBuyQty != 0) {

						childAttr.frontBarPercentage = (dashboard_data.sessionsReport.get(i).lineItems.get(j).chekinCont * 100)
								/ dashboard_data.sessionsReport.get(i).lineItems.get(j).totalBuyQty;

					} else {
						childAttr.frontBarPercentage = 0.0;
					}
					childAttrList.add(childAttr);
				}
				childBarAttr.put(i, childAttrList);
			}
		}

		final ExpandableVerticalBarChartAdapter adapter = new ExpandableVerticalBarChartAdapter(DashboardActivity.this, groupBarAttr, childBarAttr, listview);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listview.setAdapter(adapter);

		 /*SessionCheckinReportAdapter adapter = new SessionCheckinReportAdapter(this, groupBarAttr, childBarAttr);
		 listview.setAdapter(adapter);
*/        //Util.setListViewHeightBasedOnChildren(listview);
				Util.setListViewHeight(listview, groupBarAttr.size());
				/*for (int i = 0; i < groupBarAttr.size(); i++) {
				 *//*if (isEventAdmin()) {
						listview.expandGroup(i);
					}*//*
					Util.setListViewHeight(listview, i);
					//layout_checkedin_out.performClick();
				}*/

			}
		});
		listview.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
										int groupPosition, long id) {

				BarAttr groupAttr = (BarAttr) adapter.getGroupAttr(groupPosition);
				//Log.i("-------------------Group Click Index-------------", " = "+groupPosition+"\n Is Package ="+groupAttr.isPackage+"\n itempool id= "+groupAttr.item_pool_c);
				if (groupAttr.isPackage) {

					boolean isFreeSession = false;
					if (adapter.getChildrenCount(groupPosition) > 0) {
						BarAttr childAttr = adapter.getChildAttr(groupPosition, 0);
						isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c, BaseActivity.checkedin_event_record.Events.Id);
					}
					//boolean isFreeSession = Util.db.isItemPoolFreeSession(groupAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
					if (isFreeSession) {
						Util.slide_menu_id = 0;
						Intent sessionIntent = new Intent(DashboardActivity.this, SessionListActivity.class);
						sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, adapter.getChildAttr(groupPosition, 0).item_pool_c);
						sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
						sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.session_name);
						startActivity(sessionIntent);
					} else {
						//if (!isBuyersAndAttendeesHide()) {
						if (adapter.getChildrenCount(groupPosition) > 0) {
							Util.slide_menu_id = 0;
							String item_pool_id = ITransaction.EMPTY_STRING;
							int i = 0;
							for (BarAttr attr : adapter.getChildAttrList(groupPosition)) {
								item_pool_id = item_pool_id + attr.item_pool_c;
								if (i != ( adapter.getChildAttrList(groupPosition).size() - 1)) {
									item_pool_id = item_pool_id + ";";
								}
								i++;
							}
							Intent sessionIntent = new Intent(DashboardActivity.this, AttendeeListActivity.class);
							sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, item_pool_id);
							sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
							sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.session_name);
							sessionIntent.putExtra(Util.INTENT_KEY_4, "Group");
							startActivity(sessionIntent);
						} else {
							showAlert("Did not assigned any tickets to this session!");
						}

						//}
					}

				}
				return true;
			}
		});

		listview.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				BarAttr childAttr = (BarAttr) adapter.getChildAttr(groupPosition, childPosition);
				BarAttr groupAttr = (BarAttr) adapter.getGroupAttr(groupPosition);
				//Log.i("-------------------Child Click Index-------------", " = "+groupPosition+":"+childPosition+"\n Is Package ="+childAttr.isPackage+"\n itempool id= "+childAttr.item_pool_c);

				boolean isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c, BaseActivity.checkedin_event_record.Events.Id);
				if (isFreeSession) {
					Util.slide_menu_id = 0;
					Intent sessionIntent = new Intent(DashboardActivity.this, SessionListActivity.class);
					sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
					sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
					sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.session_name + "\n( " + groupAttr.session_name + " ) ");
					startActivity(sessionIntent);
				} else {
					//if (!isBuyersAndAttendeesHide()) {
					Util.slide_menu_id = 0;
					Intent sessionIntent = new Intent(DashboardActivity.this, AttendeeListActivity.class);
					sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
					sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
					sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
					sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.session_name + "\n( " + groupAttr.session_name + " ) ");
					sessionIntent.putExtra(Util.INTENT_KEY_4, "Child");
					startActivity(sessionIntent);
					//}
				}

				return false;
			}
		});
		if (!isEventAdmin()) {
			listview.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isPanelOpen){
						layout_checkedin_out.performClick();
					}

				}
			}, 500);

		}/*else{
			listview.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isPanelOpen){
						layout_event_statistics.performClick();
					}

				}
			}, 200);
		}*/

	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		String isFrom = resultData.getString("isFrom");
		if (resultCode == DownloadService.STATUS_FINISHED) {
			if(isFrom !=null && isFrom.equals("Statistics")){
				/*dialog.dismiss();
				if (attendee_view.isRefreshing()) {
					attendee_view.onRefreshComplete();
				}
				if (buyers_list.isRefreshing()) {
					buyers_list.onRefreshComplete();
				}
				if (!is_buyer) {
					UpdateAttendeeCursor();
				} else {
					UpdateBuyerCursor();
				}*/
				//setDashboardDetails();
				if (!Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim().isEmpty()) {
					//request_type=Util.DASHBORD;
					//parseJsonResponse(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""));
					dashboard_data = new Gson().fromJson(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim(), DashboradHandler.class);
					Util.dashboardHandler = dashboard_data;
					//setDashboardDetails();
					setEventStatistics();
					setDashboardvisibility();

				}
			}else{
				//loadAttendeesInBackground();
			}
		}else /*if(isFrom !=null && isFrom.equals("Statistics") && resultCode == DownloadService.STATUS_RUNNING) {*/
		{if (!Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim().isEmpty()) {
			//request_type=Util.DASHBORD;
			//parseJsonResponse(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""));
			dashboard_data = new Gson().fromJson(Util.dashboard_data_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").trim(), DashboradHandler.class);
			Util.dashboardHandler = dashboard_data;
			//setDashboardDetails();
			setDashboardvisibility();
			setEventStatistics();
		}/*if (!is_buyer) {
				UpdateAttendeeCursor();
			} else {
				UpdateBuyerCursor();
			}*/
		}

	}

	class LongRunningTask extends AsyncTask<Void, Void, Void> {
		ProgressDialog dialog;
		//= new ProgressDialog(DashboardActivity.this);
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						dialog = new ProgressDialog(DashboardActivity.this);
						dialog.setMessage("Please wait Loading Sessions...");
						dialog.setCancelable(true);
						dialog.show();
					}catch (Exception e){
						dialog.dismiss();
						e.printStackTrace();
					}
				}
			});

		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(dialog!=null) {
						if(dialog.isShowing())
							dialog.dismiss();
					}
					//dialog.dismiss();
					emptyDashboard.setVisibility(View.GONE);
					AppUtils.displayLog("Time thread", System.currentTimeMillis()+" after ");
					//setCheckedin_out();
					setEventStatistics();
					setTicketSale();
					setPayments();

					if (isEventAdmin()) {
						//layout_checkedin_out.performClick();

						if (dashboard_data.sessionsReport.size() == 0) {
							panel_checkedin_out.setVisibility(View.GONE);
						} else {
							panel_checkedin_out.setVisibility(View.VISIBLE);
						}
						if(isFromEventlist)
							layout_event_statistics.performClick();
						if(isStaticsExpandOpen)
							layout_event_statistics.performClick();
					}

                /*DashboardListAdapter dashboardAdapter = new DashboardListAdapter(DashboardActivity.this, GroupNames,
                        dashboard_data, checkedin_event_record, exp_dashboard);
                exp_dashboard.setAdapter(dashboardAdapter);
                exp_dashboard.expandGroup(0);*/
                /*if(GroupNames.size()==1){
                    Util.setListViewHeight(exp_dashboard,0);
                }else{
                    Util.setListViewHeight(exp_dashboard,3);
                }*/

				}
			});

            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isPanelOpen) {
                        layout_checkedin_out.performClick();
                    }
                }
            });*/

            /*if (!isEventAdmin()) {
                listview.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (!isPanelOpen) {
                            layout_checkedin_out.performClick();
                        }

                    }
                }, 500);
            }*/
		}

		@Override
		protected Void doInBackground(Void... params) {

            /*//Util.setListViewHeightBasedOnChildren(listview);
            for (int i = 0; i < groupBarAttr.size(); i++) {
                if (isEventAdmin()) {
                    listview.expandGroup(i);
                }
                Util.setListViewHeight(listview, i);
                //layout_checkedin_out.performClick();
            }*/
			setCheckedin_out();
			return null;
		}

	}
	public void setPieChart(String[] sliceName, float[] slicePercent, PieChart paymentChart_layout) {
		// paymentChart_layout = (PieChart) findViewById(R.id.chart1);
		paymentChart_layout.setUsePercentValues(true);
		paymentChart_layout.setDescription("");
		paymentChart_layout.setDragDecelerationFrictionCoef(0.95f);
		mTf = Typeface.createFromAsset(DashboardActivity.this.getAssets(), "OpenSans-Regular.ttf");
		paymentChart_layout.setCenterTextTypeface(Typeface.createFromAsset(DashboardActivity.this.getAssets(), "OpenSans-Regular.ttf"));
		paymentChart_layout.setDrawHoleEnabled(true);
		paymentChart_layout.setHoleColorTransparent(true);
		paymentChart_layout.setTransparentCircleColor(Color.WHITE);
		paymentChart_layout.setHoleRadius(30f);
		paymentChart_layout.setTransparentCircleRadius(35f);
		paymentChart_layout.setDrawCenterText(true);
		paymentChart_layout.setRotationAngle(0);
		//enable rotation of the chart by touch
		paymentChart_layout.setRotationEnabled(true);
		paymentChart_layout.setOnChartValueSelectedListener(this);
		paymentChart_layout.setCenterText("");
		setPieData(sliceName, slicePercent, paymentChart_layout);
		paymentChart_layout.animateY(1500, Easing.EasingOption.EaseInOutQuad);
		//mChart.spin(2000, 0, 360);
		Legend l = paymentChart_layout.getLegend();
		l.setPosition(LegendPosition.RIGHT_OF_CHART);
		l.setXEntrySpace(7f);
		l.setYEntrySpace(0f);
		l.setYOffset(0f);
		l.setEnabled(false);
	}

	private void setPieData(String[] names, float[] percent, PieChart paymentChart_layout) {
		// float mult = range;
		ArrayList<Entry> yVals1 = new ArrayList<Entry>();
		for (int i = 0; i < percent.length; i++) {
			//Log.i("------------Percent Values-------------",":"+percent[i]);
			yVals1.add(new Entry(percent[i], i));
		}
		ArrayList<String> xVals = new ArrayList<String>();
		for (int i = 0; i < names.length; i++) {
			if (!AppUtils.NullChecker(names[i]).isEmpty()) {
				xVals.add(names[i]);
			} else {
				xVals.add("");
			}
		}

		PieDataSet dataSet = new PieDataSet(yVals1, "");
		dataSet.setSliceSpace(3f);
		dataSet.setSelectionShift(5f);
		// add a lot of colors
		ArrayList<Integer> colors = new ArrayList<Integer>();

		for (int c : ColorTemplates.PAI_COLORS) {
			colors.add(c);
		}
		dataSet.setColors(colors);

		PieData data = new PieData(xVals, dataSet);
		data.setValueFormatter(new PercentFormatter());
		data.setValueTextSize(11f);
		data.setValueTextColor(Color.BLACK);
		data.setValueTypeface(mTf);
		paymentChart_layout.setData(data);

		// undo all highlights
		paymentChart_layout.highlightValues(null);

		paymentChart_layout.invalidate();
	}

	/* (non-Javadoc)
	 * @see com.github.mikephil.charting.listener.OnChartValueSelectedListener#onValueSelected(com.github.mikephil.charting.data.Entry, int, com.github.mikephil.charting.utils.Highlight)
	 */
	private void createWebSocketClient() {
		URI uri;
		try {
			uri = new URI("https://cs17.salesforce.com/services/data/v46.0/sobjects/PL_BLN_Event__e");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		httpGet = new WebSocketClient(uri) {
			@Override
			public void onOpen() {
				System.out.println("onOpen");
				//httpGet.send("Hello, World!");
			}

			@Override
			public void onTextReceived(String message) {
				System.out.println("onTextReceived");
			}

			@Override
			public void onBinaryReceived(byte[] data) {
				System.out.println("onBinaryReceived");
			}

			@Override
			public void onPingReceived(byte[] data) {
				System.out.println("onPingReceived");
			}

			@Override
			public void onPongReceived(byte[] data) {
				System.out.println("onPongReceived");
			}

			@Override
			public void onException(Exception e) {
				System.out.println(e.getMessage());
			}

			@Override
			public void onCloseReceived() {
				System.out.println("onCloseReceived");
			}
		};

		httpGet.setConnectTimeout(10000);
		httpGet.setReadTimeout(60000);
		//httpGet.addHeader("Origin", "http://developer.example.com");

		httpGet.addHeader("Authorization", "Bearer " + "00Dg0000006Nl2Q!AQgAQJtg.TEFIbqqZFgZ_uTT4E3fmmKO3ohYyih1JNShbHrLNiFQc9BwQCS6y8Zu2I7O436zx55H2i6Mz1iBRTLjk0qAREFt");
		httpGet.addHeader("Content-type", "application/json");
		httpGet.addHeader("consumer_key", "3MVG9ahGHqp.k2_zXQS1Hefh0f6yrn7Dj6PFAty.rJKNEGtjF6ihS1d6juoLNdtF_Dark7lGqrQprTl9tBDhZ");
		httpGet.addHeader("consumer_secret", "F29E51CAFEAA808BD831653DC102178E4BAC65613ADE28AB8EFD807283F797CB");
		httpGet.addHeader("username", "durga@boothleads.com.uditgaurav");
		httpGet.addHeader("password", "Gaurav@UditQFFEiVaeUeeT5GfC0LmDF9PKw");
		httpGet.addHeader("sandbox", "True");
		httpGet.enableAutomaticReconnection(5000);
		httpGet.connect();
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.github.mikephil.charting.listener.OnChartValueSelectedListener#onNothingSelected()
	 */
	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub

	}

	public void showAlert(String msg) {
		Util.setCustomAlertDialog(DashboardActivity.this);
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
		Util.openCustomDialog("Alert", msg);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			activity = null;
			Intent intent = new Intent(DashboardActivity.this, EventListActivity.class);
			intent.putExtra(Util.INTENT_KEY_1, DashboardActivity.class.getName());
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;

		}
		return super.onKeyDown(keyCode, event);

	}
	public void setDashboardvisibility(){
		event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
		if (!isEventAdmin()) {
			//GroupNames.add("CheckIn-CheckOut Reports");
			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_statistics.setVisibility(View.GONE);
			panel_ticket_sales.setVisibility(View.GONE);
			panel_payments.setVisibility(View.GONE);
		}
		else if(event_data.Events.Revenue_visibility__c.equalsIgnoreCase("false")){
			panel_statistics.setVisibility(View.VISIBLE);
			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_payments.setVisibility(View.GONE);
			panel_ticket_sales.setVisibility(View.VISIBLE);
		}else {
			panel_checkedin_out.setVisibility(View.VISIBLE);
			panel_statistics.setVisibility(View.VISIBLE);
			//layout_event_statistics.performClick();
			panel_ticket_sales.setVisibility(View.VISIBLE);
			panel_payments.setVisibility(View.VISIBLE);
		}
	}
}

