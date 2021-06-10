package com.globalnest.scanattendee;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgDialog;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeCreation;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.PrintStatusObject;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class BuyerLevelAttendeeList extends BaseActivity {

	ListView _attendee_list;
	String whereClause,checked_time,requestType;
	Cursor attendee_cursor;
	private TextView att_ticket, att_name;
	//private CheckBox attendee_checkin_checkbox;
	ListCheckInAdapter _adapter;
	private HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
	private HashMap<String, Boolean> tickets_registerfor_print = new HashMap<String, Boolean>();
	private ArrayList<String> badgelabel_list = new ArrayList<String>();
	private ArrayList<String> qrcode_name;
	ArrayList<FrameLayout> badge_frame_layout = new ArrayList<FrameLayout>();
	private ArrayList<String> attendee_id;
	private ArrayList<BadgeResponseNew>  badge_res = new ArrayList<BadgeResponseNew>();
	BadgeCreation badge_creator;
	private ArrayList<String> mFiles = new ArrayList<String>();
	//private ImageView img_checkin_cancel;s
	//TextView buyer_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_buyer_level_attendee_list);
		/*whereClause = " where Event_Id='" + checked_in_eventId
				+ "' AND Order_Id='" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";*/
/*		if(!getIntent().getStringExtra(Util.ACCESS_KEY_NAME).equalsIgnoreCase(Util.ORDERS))
			whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";
		else
			whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";
*/
		txt_title.setText(Util.db.getOrderName(getIntent().getStringExtra(Util.ORDER_ID).trim()));

		if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
			whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";
			attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
		}else{
			whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "' And "
					+DBFeilds.ATTENDEE_BADGEID+" = '"+getIntent().getStringExtra(Util.GET_BADGE_ID)+"'";
			attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
		}


		if(attendee_cursor.getCount()>0)
		{
			_adapter=new ListCheckInAdapter();
			_attendee_list.setAdapter(_adapter);
		}

		//buyer_name.setText(getIntent().getStringExtra(Util.BUYER_NAME));
		_attendee_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position,long arg3) {
				Cursor c1=_adapter.getItem(position);
				//Log.i("---------------Badgable String---------------",":"+c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)));
				//if (c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)).equalsIgnoreCase("B - Badge")) {
				Intent i = new Intent(BuyerLevelAttendeeList.this, AttendeeDetailActivity.class);
				i.putExtra(Util.EVENT_ID, c1.getString(c1.getColumnIndex("Event_Id")));
				i.putExtra(Util.ATTENDEE_ID, c1.getString(c1.getColumnIndex("Attendee_Id")));
				i.putExtra(Util.ORDER_ID, c1.getString(c1.getColumnIndex("Order_Id")));
				startActivity(i);

				//}// removed for checkin report for non badge attendees

			}
		});
		Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
		externalSettings = new ExternalSettings();
		if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
			externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
		}

		txt_save.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Log.i("Bar code button done clicked-----","Size  of ticket register="+ tickets_register.size());
				if(tickets_register.size()>0) {
					if (isOnline()) {
						doRequest();
					} else {
						//startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
						for (String key : tickets_register.keySet()) {
							generateCsvFile(key.trim());
						}
					}
				}else{
					AlertDialogCustom d = new AlertDialogCustom(BuyerLevelAttendeeList.this);
					d.setParamenters("Alert !", "No records selected, please select a record for Check-in/check-out", null, null, 1, false);
					d.show();
				}
			}
		});
		/*txtcheckin_selfcheckin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                *//*requestType=Util.GET_BADGE_ID;
                doRequest();*//*
				if(isprinterconnectedopendialog()){
					if(tickets_registerfor_print.size() == 0&&tickets_register.size()==0){
						AlertDialogCustom d=new AlertDialogCustom(BuyerLevelAttendeeList.this);
						d.setParamenters("Alert !", "Please Select at least one Attendee for Printing", null, null, 1, false);
						d.show();
					}else if(tickets_registerfor_print.size()>0){
						if(!isValidate_badge_reg_settings){
							doreprintProcess();
						}else
							doprintProcess();
					}}else {
					openprinterNotConnectedDialog(BuyerLevelAttendeeList.this);
				}
			}
		});*/
		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent();
				setResult(2017, i);
				finish();

			}
		});

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		if(isOnline()){
			String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
			postMethod = new HttpPostData("",setTicketCheckinUrl(), makeCheckin().toString(), access_token, BuyerLevelAttendeeList.this);
			postMethod.execute();

		}else{
			startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);

		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			OfflineSyncResController offlineResponse = new Gson().fromJson(response, OfflineSyncResController.class);
			List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
			boolean isFreeSession = false;
			if(scanticks.size() > 0){
				isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
			}
			//JSONObject obj = new JSONObject(response);
			////Log.i("Attendee List Activity", "requestType=  " + requestType);
			if (NullChecker(offlineResponse.ErrorMsg).isEmpty()) {
				//Log.i("Attendee List Activity", "error is null");
				/*JSONArray success = obj.optJSONArray("SuccessTickets");
				JSONArray failure = obj.optJSONArray("FailureTickets");*/
				String dialogtime = "";
				if (offlineResponse.SuccessTickets.size() > 0) {
					for (OfflineSyncSuccessObject success : offlineResponse.SuccessTickets) {
						//Log.i("Attendee List Activity","success is not null");
						boolean status =success.Status;// success.optJSONObject(i).optBoolean("Status");
						String time = success.TimeStamp;//success.optJSONObject(i).optString("TimeStamp");
						String attendee_Id = success.STicketId.Ticket__c;//success.optJSONObject(i).optString("STicketId");
						//	dialogtime = Util.db_date_format1.format(Util.date_format_sec.parse(time));
						//	time = Util.new_db_date_format.format(Util.date_format_sec.parse(time));
						////Log.i("Attendee List Activity", "Database date="+ time + "dialog time=" + dialogtime);

						if(isFreeSession){
							List<TStatus> session_attendee = new ArrayList<TStatus>();
							session_attendee.add(success.STicketId);
							Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
						}else{
							Util.db.updateCheckedInStatus(success.STicketId,checked_in_eventId);
						}
					}

					if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
						whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";
						attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
					}else{
						whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "' And "
								+DBFeilds.ATTENDEE_BADGEID+" = '"+getIntent().getStringExtra(Util.GET_BADGE_ID)+"'";
						attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
					}
				/*	whereClause = " where Event_Id='" + checked_in_eventId
							+ "' AND Order_Id='" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";

					attendee_cursor=Util.db.getAttendeeDataCursor(whereClause);*/
					_adapter=new ListCheckInAdapter();
					_attendee_list.setAdapter(_adapter);
					finish();
					//AlertDialogCustom dialog =new AlertDialogCustom(BuyerLevelAttendeeList.this);
					//dialog.setParamenters("Status","Checked In/Out Successfully", null, null, 1, true);
					//dialog.show();
				} else {
					String failed_attendees = ITransaction.EMPTY_STRING;
					String attendee_name = ITransaction.EMPTY_STRING;
					String statusdisplay=" Checked-Out ";
					int i =0 ;
					for (OfflineSyncFailuerObject failure : offlineResponse.FailureTickets) {
						boolean status = Boolean.valueOf(failure.Status);//failure.optJSONObject(i).optBoolean("Status");
						String time = failure.TimeStamp;//failure.optJSONObject(i).optString("TimeStamp");
						String attendee_Id = failure.STicketId;
						if(status){
							statusdisplay =" Checked-In ";
						}//failure.optJSONObject(i).optString("STicketId");
						dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time, checkedin_event_record.Events.Time_Zone__c);

						if(isFreeSession){
							List<TStatus> session_attendee = new ArrayList<TStatus>();
							session_attendee.add(failure.tStaus);
							Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
						}else{
							Util.db.updateCheckedInStatus(failure.tStaus,checked_in_eventId);
						}
						failed_attendees =failed_attendees+ Util.db.getAttendeeNameWithId(failure.tStaus.Ticket__c);
						if(i != (offlineResponse.FailureTickets.size()-1)){
							failed_attendees = failed_attendees+" , ";
						}
						i++;

					}

					if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
						whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";
						attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
					}else{
						whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "' And "
								+DBFeilds.ATTENDEE_BADGEID+" = '"+getIntent().getStringExtra(Util.GET_BADGE_ID)+"'";
						attendee_cursor=Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
					}
					/*whereClause = " where Event_Id='" + checked_in_eventId
							+ "' AND Order_Id='" + getIntent().getStringExtra(Util.ORDER_ID).trim() + "'";

					attendee_cursor=Util.db.getAttendeeDataCursor(whereClause);*/
					_adapter=new ListCheckInAdapter();
					_attendee_list.setAdapter(_adapter);

					Util.setCustomAlertDialog(BuyerLevelAttendeeList.this);
					Util.txt_dismiss.setVisibility(View.GONE);
					Util.txt_okey.setText("Ok");
					Util.txt_okey.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// ticket_dialog.dismiss();
							Util.alert_dialog.dismiss();
							//finish();
						}
					});
					Util.openCustomDialog("Alert",attendee_name + " is Already" +statusdisplay+ "\n at Time:"+dialogtime);

					//Util.openCustomDialog("Check-in/out Failed",failed_attendees+". These attendees are already checked-in/out some other place in the event, Please check their status and try again.");
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}
		// Util.openCustomDialog("Alert", ""+response);


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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent i = new Intent();
			setResult(2017, i);
			finish();

			return true;
		}
		return false;
	}
	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		v=inflater.inflate(layout, null);
		linearview.addView(v);
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		txt_title.setText("Buyer Tickets");
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		_attendee_list = (ListView) linearview.findViewById(R.id.buyer_level_attendee_list);
		txt_save.setVisibility(View.VISIBLE);
		txt_save.setText("Checkin");
		txtcheckin_selfcheckin.setVisibility(View.VISIBLE);
		txtcheckin_selfcheckin.setText("Print");
		txt_save.setTypeface(Util.roboto_bold);
		//txt_save.setOnClickListener(this);
		//img_checkin_done = (ImageView) linearview.findViewById(R.id.btncheckindone);
		//img_checkin_cancel = (ImageView) linearview.findViewById(R.id.btncheckincancel);

		//buyer_name=(TextView) linearview.findViewById(R.id.buyer_name);

	}


	private class ListCheckInAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return attendee_cursor.getCount();
		}

		@Override
		public Cursor getItem(int position) {
			attendee_cursor.moveToPosition(position);
			return attendee_cursor;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			try {
				final ViewHolder holder =new ViewHolder();
				attendee_cursor.moveToPosition(position);
				View v = inflater.inflate(R.layout.buyer_lavel_att_list_item,null);
				//txtticketnum = (TextView) v.findViewById(R.id.ticketnum);
				att_name = (TextView) v.findViewById(R.id.attendee_buyer_name);
				att_ticket=(TextView) v.findViewById(R.id.attendee_ticket_name);
				holder.attendee_checkin_checkbox = (CheckBox) v.findViewById(R.id.attendee_checkin_checkbox);
				//holder.check_print = (CheckBox) v.findViewById(R.id.check_print);
				holder.attendee_checkin_checkbox.setFocusable(false);

				att_name.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));

				String parent_id = Util.db.getItemPoolParentId(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
				if(!NullChecker(parent_id).isEmpty()){
					String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
					att_ticket.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+" - "+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER)));
				}else{
					att_ticket.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" - "+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER)));
				}
				boolean isFreeSession = false;
				List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
				if(scanticks.size() > 0){
					isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
				}

				String status = ITransaction.EMPTY_STRING;
				if(isFreeSession){
					status = String.valueOf(Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),Util.db.getSwitchedONGroupId(checked_in_eventId)));
				}else{
					status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
				}


				/*if (NullChecker(status).equalsIgnoreCase("true")) {
					holder.attendee_checkin_checkbox.setChecked(true);
					//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_h);
				} else {
					holder.attendee_checkin_checkbox.setChecked(false);
					//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_n);
				}*/

				if(tickets_register.containsKey(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))){
					holder.attendee_checkin_checkbox.setChecked(Boolean.valueOf(tickets_register.get(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))));
				}
				//holder.attendee_checkin_checkbox.setTag(position);
				/*holder.check_print.setTag(position);
				holder.check_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						// TODO Auto-generated method stub
						int position_tag = (Integer) buttonView.getTag();
						AppUtils.displayLog("------------List Position---------", ":"+position_tag);
						attendee_cursor.moveToPosition(position_tag);
						if(isChecked){
							//if(isprinterconnectedopendialog()) {
								tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
								badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
								buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
							*//*}
							else {
								tickets_registerfor_print.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
								buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
								if(badgelabel_list.size() > 0){
									badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
								}
								*//**//*handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
								handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
								resonNameList.remove(handler);*//**//*
								openprinterNotConnectedDialog(BuyerLevelAttendeeList.this);
							}*//*
						}else{
							tickets_registerfor_print.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
							if(badgelabel_list.size() > 0){
								badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
							}
							*//*handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
							handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
							resonNameList.remove(handler);*//*
						}
						//int i=tickets_registerfor_print.size();
						*//*if(tickets_registerfor_print.size()>0) {
							txtprint.setVisibility(View.VISIBLE);
							txtprint.setText("Selected-" + tickets_registerfor_print.size());
						}else {
							txtprint.setVisibility(View.GONE);
						}*//*

					}
				});*/
				holder.attendee_checkin_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						boolean withoutalert=true;
						attendee_cursor.moveToPosition(position);
						if (isChecked) {
							String tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId, Util.db.getSwitchedONGroupId(checked_in_eventId));
							String orderstatus = Util.db.getOrderStatuswithAttendee(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)), checked_in_eventId);
							String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
								holder.attendee_checkin_checkbox.setChecked(false);
								showScannedTicketsAlert("Please TurnON at least one session for scanning.",true);
							}
							else if (!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)) {
								withoutalert = false;
								/*String item_pool_name = "";
								String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
								if (!NullChecker(parent_id).isEmpty()) {
									item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
								} else {
									item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
								}*/
								holder.attendee_checkin_checkbox.setChecked(false);
								openScanSettingsAlert(BuyerLevelAttendeeList.this,item_pool_id,
										BuyerLevelAttendeeList.class.getName());
								Util.txt_dismiss.setVisibility(View.GONE);
								//showCustomToast(BuyerLevelAttendeeList.this, "Sorry! You are not allowed to check-in for \"" + item_pool_name + "\".", R.drawable.img_like, R.drawable.toast_redrounded, false);

							} else if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
								withoutalert = false;
								holder.attendee_checkin_checkbox.setChecked(false);
								showMessageAlert(getString(R.string.checkin_only_msg),false);
							}else if (!externalSettings.quick_checkin && !orderstatus.equalsIgnoreCase("Fully Paid")) {
								withoutalert = false;
								final boolean checked = isChecked;
								Util.setCustomAlertDialog(BuyerLevelAttendeeList.this);
								Util.alert_dialog.setCancelable(false);
								Util.txt_okey.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										Util.alert_dialog.dismiss();
										startCheckclickupdate(holder.attendee_checkin_checkbox, true, attendee_cursor);
									}
								});
								Util.txt_dismiss.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View v) {
										Util.alert_dialog.dismiss();
										holder.attendee_checkin_checkbox.setChecked(false);									}
								});
								Util.openCustomDialog("Alert", "This Order Status is " + orderstatus + "! \n Do you still want to Continue?");

							}
							if(withoutalert){
								startCheckclickupdate(holder.attendee_checkin_checkbox, true, attendee_cursor);
							}
						}
					}
				});
				return v;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@SuppressWarnings("unused")
	public JSONArray makeCheckin() {
		try {
			Set<String> keys = tickets_register.keySet();
			JSONArray ticketarray = new JSONArray();
			// JSONObject parent = new JSONObject();
			checked_time = getESTFormat();
			for (String key : tickets_register.keySet()) {

				try {

					JSONObject obj = new JSONObject();
					obj.put("TicketId", key.trim());
					obj.put("device", "ANDROID");
					//obj.put("isCHeckIn", tickets_register.get(key));
					List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
					if(scanticks.size() == 0){
						obj.put("freeSemPoolId", "");
					}else if(scanticks.size() > 0){
						if(Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)){
							obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
						}else{
							obj.put("freeSemPoolId", "");
						}
					}

					if (tickets_register.get(key)){
						obj.put("isCHeckIn", tickets_register.get(key));
					}else{
						obj.put("isCHeckIn", false);
					}
					obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
					obj.put("sTime", Util.getCurrentDateTimeInGMT());
					obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
					ticketarray.put(obj);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			return ticketarray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String setTicketCheckinUrl() {
		try {
			return sfdcddetails.instance_url
					+ WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="
					+ sfdcddetails.user_id+"&eventId="+checked_in_eventId+"&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion().replaceAll(" ", "%20")
					+"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	class ViewHolder
	{
		CheckBox attendee_checkin_checkbox;
		//CheckBox check_print;
	}

	protected void onResume() {
		super.onResume();
	}
	public void startCheckclickupdate(CheckBox attendee_checkin_checkbox, Boolean isChecked, Cursor attendee_cursor){
		boolean isFreeSession = false;
		List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
		if(scanticks.size() > 0){
			isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
		}if(isFreeSession){
			String ticket_id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
			String status = String.valueOf(Util.db.SessionCheckInStatus(ticket_id,Util.db.getSwitchedONGroupId(checked_in_eventId)));
			if(Boolean.valueOf(status) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
				attendee_checkin_checkbox.setChecked(false);
				showMessageAlert(getString(R.string.checkin_only_msg),false);
			}else{
				if(tickets_register.containsKey(ticket_id)){
					tickets_register.remove(ticket_id);
				}
				String check_box_value = String.valueOf(attendee_checkin_checkbox.isChecked());
				if(!NullChecker(status).equalsIgnoreCase(check_box_value)){
					tickets_register.put(ticket_id, !Boolean.valueOf(status));
				}
			}
		}else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
			String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
			if (NullChecker(status).equalsIgnoreCase("true")) {
				attendee_checkin_checkbox.setChecked(true);
				//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_h);
			} else {
				attendee_checkin_checkbox.setChecked(false);
				//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_n);
			}
			//openScanSettingsAlert(BuyerLevelAttendeeList.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),BuyerLevelAttendeeList.class.getName());
		}else{
			String ticket_id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
			String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);

			if(Boolean.valueOf(status) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
				attendee_checkin_checkbox.setChecked(false);
				showMessageAlert(getString(R.string.checkin_only_msg),false);
			}else{
				if(tickets_register.containsKey(ticket_id)){
					tickets_register.remove(ticket_id);
				}
				String check_box_value = String.valueOf(attendee_checkin_checkbox.isChecked());
				tickets_register.put(ticket_id, !Boolean.valueOf(NullChecker(status)));

				/*if(!NullChecker(status).isEmpty()){
					tickets_register.put(ticket_id, !Boolean.valueOf(status));
				}*/
			}
		}
	}
	/*public void openprinterNotConnectedDialog(final Context context){
		Util.setCustomAlertDialog(context);
		Util.openCustomDialog("Alert", "Printer is not connected.Do you want to Connect?");
		Util.txt_okey.setText("OK");
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(context,BuyerLevelAttendeeList.class));
				Util.alert_dialog.dismiss();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
			}
		});
	}

	public boolean isprinterconnectedopendialog() {
		boolean connected=false;
		if (PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
			connected=false;
		}else{
			connected=true;
		}
		return connected;
	}
	private void doreprintProcess(){
		try {
			if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
				if (tickets_registerfor_print.size() > 0) {
					qrcode_name.clear();
					badge_frame_layout.clear();
					attendee_id = new ArrayList<String>();
					PrintStatusObject printstatus=new PrintStatusObject();
					ArrayList<PrintStatusObject>statusarray=new ArrayList<>();
					for (String key : tickets_registerfor_print.keySet()) {
						// if(Util.db.getAttendeeBadgePrintStatus(NullChecker(key)).isEmpty()||Util.db.getAttendeeBadgePrintStatus(NullChecker(key)).equalsIgnoreCase("Not Printed")) {
						attendee_id.add(key);
						printstatus.TicketId = key;
						statusarray.add(printstatus);
						//}
					}

					String ids=new Gson().toJson(statusarray);
					Util.printStatusupdateAttendees.edit().putString(Util.PRINT_STATUS, ids).commit();

					badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
					if (badge_res.size() > 0) {
						if (isOnline()) {
							executePrinterStatusTask();
						} else {
							startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
						}
						//prepareForPrint();
					} else {
						requestType = Util.LOAD_BADGE;
						doRequest();
					}
				}
			}else{
				openprinterNotConnectedDialog(BuyerLevelAttendeeList.this);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void doprintProcess(){
		try {
			if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
				if (tickets_registerfor_print.size() > 0) {
					qrcode_name.clear();
					badge_frame_layout.clear();
					attendee_id = new ArrayList<String>();
					PrintStatusObject printstatus=new PrintStatusObject();
					ArrayList<PrintStatusObject>statusarray=new ArrayList<>();
					for (String key : tickets_registerfor_print.keySet()) {
						attendee_id.add(key);
						printstatus.TicketId=key;
						statusarray.add(printstatus);

					}
					String ids=new Gson().toJson(statusarray);
					Util.printStatusupdateAttendees.edit().putString(Util.PRINT_STATUS, ids).commit();
					badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
					if (badge_res.size() > 0) {
						if (isOnline()) {
							executePrinterStatusTask();
						} else {
							startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
						}
						//prepareForPrint();
					} else {
						requestType = Util.LOAD_BADGE;
						doRequest();
					}
				}
			}else{
				openprinterNotConnectedDialog(BuyerLevelAttendeeList.this);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private void executePrinterStatusTask() {
		if(isOnline()) {
			if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					new IsBrotherPrinterConnectTask().execute();
				}else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
					if(!isValidate_badge_reg_settings){
						createTemplates();
					}else if(isOnline()){
						requestType = Util.GET_BADGE_ID;
						doRequest();
					}else {
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}
				}
				//new IsBrotherPrinterConnectTask().execute();
			} else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
				if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
					new IsPrinterConnectTask().execute();
				}else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()) {
					if(!isValidate_badge_reg_settings){
						createTemplates();
					}else if (isOnline()) {
						requestType = Util.GET_BADGE_ID;
						doRequest();
					} else {
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}
				}
				//new IsPrinterConnectTask().execute();
			}else{
				TransperantGlobalScanActivity.openprinterNotConnectedDialog(this,true);
			}
		}else{
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}
	}
	private  class IsBrotherPrinterConnectTask extends AsyncTask<Void,Void,Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			baseDialog.setCancelable(false);
			baseDialog.setMessage("Please wait...");
			baseDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				SearchPrinterStatusThread();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			baseDialog.dismiss();
			if(isBrotherPrinterConnected){
				if (isOnline()) {
					if(!isValidate_badge_reg_settings){
						createTemplates();
					}else {
						requestType = Util.GET_BADGE_ID;
						doRequest();
					}
				} else {
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				}
			}else{
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						AttendeeDetailActivity.openprinterNotConnectedDialog(BuyerLevelAttendeeList.this);
					}
				});
			}
		}
	}
	public void SearchPrinterStatusThread() *//*extends Thread*//* {

		*//* search for the printer for 10 times until printer has been found. *//*
		*//*@Override
		public void run() {*//*
		try {
			// search for net printer.
			if (netPrinterList(5)) {
				isBrotherPrinterConnected=true;
			} else {
				isBrotherPrinterConnected=false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//}
	}
	private boolean netPrinterList(int times) {
		boolean searchEnd = false;
		try {
			// clear the item list
			if (mItems != null) {
				mItems.clear();
			}
			// get net printers of the particular model
			mItems = new ArrayList<String>();
			Printer myPrinter = new Printer();
			mNetPrinter = myPrinter.getNetPrinters(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW").replace("_","-"));
			final int netPrinterCount = mNetPrinter.length;

			// when find printers,set the printers' information to the list.
			if (netPrinterCount > 0) {
				searchEnd = true;
				setPrefereces(mNetPrinter[0]);
			} else if (netPrinterCount == 0
					&& times == (Common.SEARCH_TIMES - 1)) { // when no printer
				// is found
				String dispBuff[] = new String[1];
				dispBuff[0] = getString(R.string.noNetDevice);
				mItems.add(dispBuff[0]);
				searchEnd = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return searchEnd;
	}

	public void setPrefereces(NetPrinter mNetPrinter) {
		try {
			// initialization for print
			PrinterInfo printerInfo = new PrinterInfo();
			Printer printer = new Printer();
			printerInfo = printer.getPrinterInfo();
			if (sharedPreferences.getString("printerModel", "").equals("")) {
				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW"));
				if(!mNetPrinter.ipAddress.toString().isEmpty())
					editor.putString("port", "NET");
				else
					editor.putString("port", "BLUETOOTH");
				editor.putString("address", printerInfo.ipAddress.toString());
				editor.putString("macAddress", printerInfo.macAddress.toString());
				editor.putString("address", mNetPrinter.ipAddress);
				editor.putString("macAddress", mNetPrinter.macAddress);
				editor.putString("printer", mNetPrinter.modelName);
				editor.putString("paperSize", "W62H100");
				editor.putString("serNo", mNetPrinter.serNo);

				if(badge_res.size()==0) {
					badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
				}
				else if(badge_res.size()>0){
					BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
					//Log.i("-----------------Badge Paper Size-----------",":"+badge_data.paperSize.contains(Util.BROTHER_DKN_5224)+" : "+badge_data.paperSize);
					if (badge_data.paperSize.contains(Util.BROTHER_DK_1202)) {
						// editor.putString("paperSize", "W62H100");
						editor.putString("paperSize", "W62H100");
					} else if (badge_data.paperSize.contains(Util.BROTHER_DK_12345)) {
						editor.putString("paperSize", "W60H86");
					} else if (badge_data.paperSize.contains(Util.BROTHER_DKN_5224)) {
						editor.putString("paperSize", "W54");
					}else if (badge_data.paperSize.contains("3\" x 1\"")) {//added for test in QA6
						editor.putString("paperSize", "W29H90");
					}else{
						editor.putString("paperSize", "W62");
					}
				}else{
					AlertDialogCustom custom = new AlertDialogCustom(
							BuyerLevelAttendeeList.this);
					custom.setParamenters("Alert",
							"No Badge Selected, Do you want to select a Badge",
							new Intent(BuyerLevelAttendeeList.this,
									BadgeTemplateNewActivity.class), null, 2, false);
					custom.show();
				}
				editor.putString("orientation", "LANDSCAPE");
				editor.putString("numberOfCopies", "1");
				editor.putString("halftone", "PATTERNDITHER");
				editor.putString("printMode", "FIT_TO_PAGE");
				editor.putString("pjCarbon", "false");
				editor.putString("pjDensity", "5");
				editor.putString("pjFeedMode", "PJ_FEED_MODE_FIXEDPAGE");
				editor.putString("align", "CENTER");
				editor.putString("leftMargin", "0");
				editor.putString("valign", "MIDDLE");
				editor.putString("topMargin", "0");
				editor.putString("customPaperWidth", "0");
				editor.putString("customPaperLength", "0");
				editor.putString("customFeed", "0");
				editor.putString("customSetting",
						sharedPreferences.getString("customSetting", ""));
				editor.putString("rjDensity", "0");
				editor.putString("rotate180", "false");
				editor.putString("peelMode", "false");
				editor.putString("autoCut", "true");
				editor.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private boolean createTemplates() {
		// TODO Auto-generated method stub
		try {
			badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
			ArrayList<LinearLayout> badge_layouts_list = new ArrayList<LinearLayout>();
			for (String attendeeid : attendee_id) {
				String whereClause;
				if (isOrderScaneed) {
					whereClause = " where Event_Id = '" + checked_in_eventId
							+ "'" + " AND Attendee_Id = " + "'"
							+ attendeeid + "'"
							+ " AND Order_Id = " + "'" + orderId + "'";
				} else {
					whereClause = " where Event_Id = '" + checked_in_eventId
							+ "'" + " AND Attendee_Id = " + "'"
							+ attendeeid + "'";
				}
				attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
				attendee_cursor.moveToFirst();
				if(attendee_cursor.getCount()==0){
					attendee_cursor.close();
					attendee_cursor=Util.db.getAllTypeAttendeeCursor(whereClause);
					attendee_cursor.moveToFirst();
				}

				final View v = inflater.inflate(R.layout.badge_sample_layout, null);
				LinearLayout linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
				FrameLayout badgelayout = (FrameLayout) v.findViewById(R.id.badgelayout);
				linear_badge.setVisibility(View.INVISIBLE);
				badge_creator.createBadgeTemplate(badge_res.get(0), badgelayout, attendee_cursor, true);
				linear_badge_parent.addView(v);
				badge_layouts_list.add(linear_badge);

			}
			Collections.reverse(badge_layouts_list);
			Collections.reverse(attendee_id);
			// _badgeadapter.notifyDataSetChanged();
			CreateBadges badge_task = new CreateBadges(badge_layouts_list);
			badge_task.execute();

			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
	private static  synchronized int createImage(final LinearLayout layout,final String qrcodename,int position){
		layout.post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				layout.setDrawingCacheEnabled(true);
				layout.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);
				layout.buildDrawingCache(true);
				saveBitmap(layout.getDrawingCache(), qrcodename);

			}
		});
		return position;
	}


	private class CreateBadges extends AsyncTask<String, Integer, Boolean>{
		private ArrayList<LinearLayout>badge_layouts_list = new ArrayList<LinearLayout>();
		public CreateBadges(ArrayList<LinearLayout> badge_layouts_list){
			this.badge_layouts_list = badge_layouts_list;
		}

		protected void onPreExecute(){
			super.onPreExecute();
			baseDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			baseDialog.setMax(attendee_id.size());
			baseDialog.setCancelable(false);
			baseDialog.setProgress(0);
			baseDialog.show();
		}

		*//* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 *//*
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				int position = 0;
				for (final LinearLayout layout : badge_layouts_list) {
					String whereClause = " where Event_Id = '" + checked_in_eventId
							+ "'" + " AND Attendee_Id = " + "'"
							+ attendee_id.get(position) + "'";
					attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
					attendee_cursor.moveToFirst();
					//final String name = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)) + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME));

					if(attendee_cursor.getCount()==0){
						attendee_cursor.close();
						attendee_cursor=Util.db.getAllTypeAttendeeCursor(whereClause);
						attendee_cursor.moveToFirst();
					}
					qrcodename = attendee_cursor.getString(attendee_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
							+ attendee_cursor.getString(attendee_cursor
							.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))
							+ attendee_id.get(position);
					//Log.i("-----------------Badge Name---------", ":" + qrcodename);
					qrcode_name.add(qrcodename);
                    *//*final int finalPosition = position;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {*//*
					onProgressUpdate(createImage(layout, qrcodename, position));
                   *//*     }
                    });*//*


					position++;
				}


			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			baseDialog.setProgress(values[0]);
		}

		protected void onPostExecute(Boolean result){
			super.onPostExecute(result);
			baseDialog.dismiss();
			for(LinearLayout layout:badge_layouts_list){
				linear_badge_parent.removeView(layout);
			}
			mListViewDidLoadHanlder.sendEmptyMessage(0);
           *//* if (isOnline()) {
                requestType = Util.CHECKIN;
                doRequest();
            } else {
                startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
            }*//*
		}
	}

	private  class IsPrinterConnectTask extends com.globalnest.stripe.android.compat.AsyncTask<Void,Void,Void> {
		private boolean isPrinterConnectedStatus=false;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			baseDialog.setCancelable(false);
			baseDialog.setMessage("Please wait...");
			baseDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				isPrinterConnectedStatus= isprinterconnected();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			baseDialog.dismiss();
			if(isPrinterConnectedStatus) {
				if (isOnline()) {
					requestType = Util.GET_BADGE_ID;
					doRequest();
				} else {
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				}
			}
		}
	}

	private Handler mListViewDidLoadHanlder = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message message) {
			doPrint();
			return false;
		}


	});
	private void doPrint() {
		if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").equalsIgnoreCase("Zebra")) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					mFiles.clear();
					String file_path = null;
					File root = android.os.Environment.getExternalStorageDirectory();
					File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
					for (int i = 0; i < tickets_registerfor_print.size(); i++) {
						file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
						//Log.i("Attendee Detail", "Image Path=" + file_path);
						mFiles.add(file_path);
					}
					MsgDialog.intent_value = BuyerLevelAttendeeList.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
					zebraPrinter.doZebraPrint(BuyerLevelAttendeeList.this,mFiles);
				}
			}).start();
		} else {
			//Log.i("Attendee Detail-----doprint", "sharedPrefrence is empty");
			if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
				searchPrinter = new BuyerLevelAttendeeList().SearchThread();
				searchPrinter.start();
			}else{
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				myPrint.setBluetoothAdapter(bluetoothAdapter);
				NetPrinter printer=new NetPrinter();
				printer.modelName=PrinterDetails.selectedPrinterPrefrences.getString("printer","");
				printer.serNo="";
				printer.ipAddress="";
				printer.macAddress=PrinterDetails.selectedPrinterPrefrences.getString("macAddress","");
				setPrefereces(printer);
				printBadge();
			} *//* setDialog();
            searchPrinter = new SearchThread();
            searchPrinter.start();*//*
		}
	}
	public class SearchThread extends Thread {
		*//* search for the printer for 10 times until printer has been found. *//*
		@Override
		public void run() {
			try {
				// search for net printer.
				if (netPrinterList(5)) {
					isPrinter = true;
					msgDialog.close();
					printBadge();
				} else {
					msgDialog.close();
					BuyerLevelAttendeeList.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Util.setCustomAlertDialog(BuyerLevelAttendeeList.this);

							Util.txt_okey.setText("REPRINT");
							Util.txt_dismiss.setVisibility(View.VISIBLE);
							Util.txt_okey.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									doPrint();
									Util.alert_dialog.dismiss();
								}
							});

							Util.txt_dismiss.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									//ShowTicketsDialog();
									Util.alert_dialog.dismiss();

								}
							});
							Util.openCustomDialog("Alert", "No printer found. Do you want to reprint ?");
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public void printBadge() {
		try {
			mFiles.clear();
			String file_path = null;
			File root = android.os.Environment.getExternalStorageDirectory();
			File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
			for (int i = 0; i < tickets_registerfor_print.size(); i++) {
				file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
				//Log.i("Attendee Detail", "Image Path=" + file_path);
				mFiles.add(file_path);
			}
			MsgDialog.intent_value = BuyerLevelAttendeeList.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
			MsgDialog.isShowDialog = ext_settings.quick_print;
			if(ext_settings.doubleSide_badge) {
				sharedPreferences.edit().putString("autoCut","false").commit();
				sharedPreferences.edit().putString("endCut","true").commit();
				sharedPreferences.edit().putString("numberOfCopies", "2").commit();
			}else{
				sharedPreferences.edit().putString("autoCut","true").commit();
				sharedPreferences.edit().putString("endCut","").commit();
				sharedPreferences.edit().putString("numberOfCopies", "1").commit();
			}
			((ImagePrint) myPrint).setFiles(mFiles);
			myPrint.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

}


