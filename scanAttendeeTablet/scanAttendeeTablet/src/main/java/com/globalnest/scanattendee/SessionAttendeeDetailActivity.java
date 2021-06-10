//  ScanAttendee Android
//  Created by Ajay on Jul 22, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.globalnest.database.DBFeilds;
import com.globalnest.patternedEditText.FloatingHintEditTextLayout;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;

public class SessionAttendeeDetailActivity extends BaseActivity{

	String whereChause=ITransaction.EMPTY_STRING;
	String sessionAttendeeId = ITransaction.EMPTY_STRING;
	String sessionGroupId = ITransaction.EMPTY_STRING;
	private Cursor sessionAttendeeCursor;
	EditText fname, lname, company, city, zipCode, email,
			edit_jobtitle, edit_att_category,edt_mobile,
			edit_badgeLabel, edt_custombarcode,edit_ticket_type;
	FloatingHintEditTextLayout barcode_label;
	LinearLayout lay_badgeLabel, lay_checkinHistory,lay_ticketType,lay_barcode;
	TextView  txt_checkin_date,txtcheckinhistory;
	Button btn_expand, btn_seat_cancel, btn_seat_done,btn_barcode;


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		sessionAttendeeId=getIntent().getStringExtra(DBFeilds.SESSION_ATTENDEE_TICKET_ID);
		sessionGroupId = getIntent().getStringExtra(DBFeilds.SESSION_GROUP_ID);
		whereChause=" where "+DBFeilds.SESSION_ATTENDEE_TICKET_ID+" = '"+sessionAttendeeId+"' AND "+DBFeilds.SESSION_GROUP_ID+" = '"+sessionGroupId+"'";
		sessionAttendeeCursor=Util.db.getSessionAttendeeCursor(whereChause);
		sessionAttendeeCursor.moveToFirst();
		//Log.i(" Sess ATT Detail ", sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID)));
		//Log.i(" Sess ATT Detail ", sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ITEM_POOL_ID)));



		setCustomContentView(R.layout.session_attendee_detail);


		txtcheckinhistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(SessionAttendeeDetailActivity.this,	CheckinHistory.class);
				i.putExtra(Util.TICKET_ID, sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_TICKET_ID)));
				i.putExtra(Util.ATTENDEE_NAME,sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_FIRST_NAME))
						+ " "+ sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_LAST_NAME)));
				i.putExtra(Util.INTENT_KEY_1, sessionAttendeeCursor.getString(sessionAttendeeCursor.getColumnIndex(DBFeilds.SESSION_ITEM_POOL_ID)));
				i.putExtra(Util.INTENT_KEY_2,sessionGroupId);
				startActivity(i);

			}
		});

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				setResult(2018,i);
				finish();
			}
		});


	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub

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
		linearview.addView(inflater.inflate(R.layout.session_attendee_detail, null));
		txt_title.setText("Attendee Detail");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		txt_save.setVisibility(View.GONE);

		// ====================================== EditText Fields ========================================//

		fname = (EditText) linearview.findViewById(R.id.attfname);
		lname = (EditText) linearview.findViewById(R.id.attlname);
		email = (EditText) linearview.findViewById(R.id.attemail);
		edt_mobile= (EditText) linearview.findViewById(R.id.att_mobile);
		company = (EditText) linearview.findViewById(R.id.attcomp);
		edit_jobtitle = (EditText) linearview.findViewById(R.id.attjobtitle);
		edit_badgeLabel = (EditText) linearview.findViewById(R.id.attbadgeLabel);
		city = (EditText) linearview.findViewById(R.id.attcity);
		zipCode = (EditText) linearview.findViewById(R.id.attzipcode);
		edit_att_category = (EditText) linearview.findViewById(R.id.attcategoryvalue);
		edt_custombarcode = (EditText)linearview.findViewById(R.id.attCustomBarcode);
		barcode_label =(FloatingHintEditTextLayout) linearview.findViewById(R.id.barcode_label);
		edit_ticket_type= (EditText)linearview.findViewById(R.id.edit_ticket_type);
		lay_barcode = (LinearLayout)linearview.findViewById(R.id.lay_barcode);
		lay_badgeLabel=(LinearLayout)linearview.findViewById(R.id.lay_badgeLabel);
		lay_checkinHistory=(LinearLayout)linearview.findViewById(R.id.lay_checkinHistory);
		lay_ticketType=(LinearLayout)linearview.findViewById(R.id.lay_ticketType);
		btn_barcode = (Button)linearview.findViewById(R.id.btn_barcode);
		txt_checkin_date = (TextView) linearview.findViewById(R.id.txtcheckinvalue);
		txtcheckinhistory = (TextView) linearview.findViewById(R.id.txtcheckinhistory);

		setData(DBFeilds.SESSION_ATTENDEE_FIRST_NAME, sessionAttendeeCursor, fname);
		setData(DBFeilds.SESSION_ATTENDEE_LAST_NAME, sessionAttendeeCursor, lname);
		setData(DBFeilds.SESSION_ATTENDEE_EMAIL_ID, sessionAttendeeCursor, email);
		setData(DBFeilds.SESSION_ATTENDEE_COMPANY, sessionAttendeeCursor, company);
		setData(DBFeilds.SESSION_MOBILE, sessionAttendeeCursor, edt_mobile);
		setData(DBFeilds.SESSION_TKT_JOB_TITLE, sessionAttendeeCursor,edit_jobtitle);
		setCheckidData(DBFeilds.SESSION_ATTENDEE_SCANTIME,sessionAttendeeCursor,txt_checkin_date);
		setDataforIsEmpty(DBFeilds.SESSION_BADGE_LABEL, sessionAttendeeCursor, edit_badgeLabel, lay_badgeLabel);
		setDataforIsEmpty(DBFeilds.SESSION_ITEM_TYPE_NAME, sessionAttendeeCursor, edit_ticket_type, lay_ticketType);
		setDataforIsEmpty(DBFeilds.SESSION_BADGE_LABEL, sessionAttendeeCursor, edit_badgeLabel, lay_badgeLabel);
		setDataforIsEmpty(DBFeilds.SESSION_CUSTOM_BARCODE, sessionAttendeeCursor, edt_custombarcode, lay_barcode);


	}

	private void setDataforIsEmpty(String DBField,Cursor cursor,EditText edit_text,LinearLayout layout){
		cursor.moveToFirst();
		if (NullChecker(cursor.getString(cursor.getColumnIndex(DBField))).isEmpty()){
			/*if(edit_text.equals(edt_custombarcode)){
				barcode_label.setVisibility(View.GONE);
			}*/
			layout.setVisibility(View.GONE);
		}else{
			layout.setVisibility(View.VISIBLE);
			edit_text.setText(cursor.getString(cursor.getColumnIndex(DBField)));
		}
	}

	private void setDataforIsEmpty(String DBField,Cursor cursor,TextView text_view,LinearLayout layout){
		cursor.moveToFirst();
		if (NullChecker(cursor.getString(cursor.getColumnIndex(DBField))).isEmpty()){
			layout.setVisibility(View.GONE);
		}else{
			layout.setVisibility(View.VISIBLE);
			text_view.setText(cursor.getString(cursor.getColumnIndex(DBField)));
		}
	}
	private void setCheckidData(String DBField,Cursor cursor,TextView edit_text){
		cursor.moveToFirst();
		String checkin_status = "";
		if (NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS))).equalsIgnoreCase("true")) {
			checkin_status = "Check In";
			edit_text.setTextColor(getResources().getColor(R.color.green_button_color));
		} else if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.SESSION_ATTENDEE_CHECKIN_STATUS))).equalsIgnoreCase("false")){
			checkin_status = "Check Out";
			edit_text.setTextColor(getResources().getColor(
					R.color.orange_bg));
		}else {
			checkin_status = "Registered";
			edit_text.setTextColor(getResources().getColor(
					R.color.gray_color));

		}
		String checkin_time = Util.change_US_ONLY_DateFormatWithSEC(cursor.getString(cursor.getColumnIndex(DBField)),checkedin_event_record.Events.Time_Zone__c);

		edit_text.setText(checkin_time+ "(" + checkin_status + ")");
	}

	private void setData(String DBField,Cursor cursor,EditText edit_text){
		cursor.moveToFirst();
		edit_text.setText(NullChecker(cursor.getString(cursor.getColumnIndex(DBField))));
	}

	protected void onDestroy(){
		super.onDestroy();
		if(sessionAttendeeCursor != null){
			sessionAttendeeCursor.close();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*Intent i = new Intent(AttendeeDetailActivity.this,	AttendeeListActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);*/
			Intent i = new Intent();
			setResult(2018,i);
			finish();

			return true;
		}
		return false;
	}
}
