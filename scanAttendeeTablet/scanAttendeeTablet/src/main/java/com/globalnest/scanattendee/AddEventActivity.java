//  ScanAttendee Android
//  Created by Ajay
//  This class is used to add or edit an event and upload data to the backend 
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.Currency;
import com.globalnest.objects.EditEventResponse;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AddEventActivity extends BaseActivity implements OnClickListener {
	private static final String CREATE_EVENT = "Create_Event";
	public static final String EDIT_EVENT = "Edit_Event";
	SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a",Locale.US);
	public Uri mImageCaptureUri;
	//String added_Event_id;
	private String TAG = "-----Add Event Activity------";
	private int smyear=-1, smmonth=-1, smday=-1, emyear, emmonth, emday, shour, ehour,
			sminute, eminute;
	static final int STARTDATE_DIALOG_ID = 0;
	static final int ENDDATE_DIALOG_ID = 1;
	static final int STARTTIME_DIALOG_ID = 1111;
	static final int ENDTIME_DIALOG_ID = 1112;
	boolean isOnCreate;
	ImageView img_logo;
	FrameLayout frame_pay_setting;
	EditText edit_eventName, edit_city, edit_event_des, edit_eventLoc,
			edit_event_address, edit_zipcode;
	Spinner spnr_state, spnr_country,spnr_currency;
	TextView txtstart, txtend, txt_startdate, txt_enddate, txt_starttime,
			txt_endtime, txt_city, txt_state, txt_country, txt_desc, txt_loc,
			txt_address, txt_zipcode,
			txtstate_error;

	Bitmap photo;
	ArrayAdapter<String> adaptercountry, indianstates, americanstates;
	CurrencyAdapter adapter_currency;
	private String eventName = "";
	private String event_city = "";
	private String event_state = "";
	private String event_desc = "";
	private String event_location = "";
	private String event_address = "";
	private String event_zipcode = "";
	private String event_country = "";
	private String server_start_date = "";
	private String server_end_date = "";
	private String event_start_date = "";
	private String event_end_date = "";
	private String status = "";
	private Date start_date, end_date;
	private Cursor _event_payment_setting;
	private List<Currency> currency_list  = new ArrayList<Currency>();
	EventObjects eventController = new EventObjects();
	ArrayList<EventObjects> event_details_controller;
	EventObjects event_controller = new EventObjects();
	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
	SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy",Locale.US);
	SimpleDateFormat format2 = new SimpleDateFormat("MMMM dd, yyyy h:mm a",Locale.US);
	Calendar c = Calendar.getInstance();
	Animation errorAnimation, animTranslate;
	CountDownTimer timer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.addevent_layout);

		isOnCreate = true;
		errorAnimation = AnimationUtils.loadAnimation(AddEventActivity.this,
				R.anim.txt_translate_finish);
		errorAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {

			}
		});

		timer = new CountDownTimer(3000, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {

			}

			@Override
			public void onFinish() {

				txt_error_msg.startAnimation(errorAnimation);

			}
		};

		spnr_country.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				String country = (String) spnr_country.getSelectedItem();

				setStateAdapter(country);

				if (!isOnCreate) {
					if (arg2 == 0) {
						int maxLength = 5;
						InputFilter[] fArray = new InputFilter[1];
						fArray[0] = new InputFilter.LengthFilter(maxLength);
						edit_zipcode.setFilters(fArray);
					} else if (arg2 == 1) {
						spnr_state.setAdapter(indianstates);
						int maxLength = 6;
						InputFilter[] fArray = new InputFilter[1];
						fArray[0] = new InputFilter.LengthFilter(maxLength);
						edit_zipcode.setFilters(fArray);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				finish();
			}
		});

	}

	private void setStateAdapter(String country) {
		state_list =  Util.db.getStateList(Util.db.getCountryId(country));
		//Arrays.sort(state_list);	
		//state_list[0] = "--Select--";
		indianstates = new ArrayAdapter<String>(this,
				R.layout.spinner_item, state_list) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);
				return v;
			}
		};

		if (!event_controller.state.isEmpty()) {
			int select = Util.getStatePosition(state_list,
					event_controller.state);
			spnr_state.setAdapter(indianstates);
			spnr_state.setSelection(select, true);
		} else {
			spnr_state.setAdapter(indianstates);
			spnr_state.setSelection(0);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		View view = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (view instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w
							.getBottom())) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getWindow().getCurrentFocus()
						.getWindowToken(), 0);
			}
		}
		return ret;

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		addevent_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.VISIBLE);
		event_layout.setVisibility(View.GONE);
		img_socket_scanner.setVisibility(View.GONE);
		if (BaseActivity.img_scanner_base != null) {
			BaseActivity.img_scanner_base.setVisibility(View.GONE);
		}
		img_logo = (ImageView) findViewById(R.id.eventlogo);
		txtstart = (TextView) findViewById(R.id.txtstart);
		txtend = (TextView) findViewById(R.id.txtend);
		txt_startdate = (TextView) findViewById(R.id.txtstartdate);
		txt_enddate = (TextView) findViewById(R.id.txtenddate);
		txt_starttime = (TextView) findViewById(R.id.txtstarttime);
		txt_endtime = (TextView) findViewById(R.id.txtendtime);
		txtstate_error = (TextView) findViewById(R.id.txtstate_error);

		txt_city = (TextView) findViewById(R.id.txtcity);
		txt_state = (TextView) findViewById(R.id.txtstate);
		txt_country = (TextView) findViewById(R.id.txtcountry);
		txt_desc = (TextView) findViewById(R.id.txtdesc);
		txt_loc = (TextView) findViewById(R.id.txtlocation);
		txt_address = (TextView) findViewById(R.id.txtaddress);
		txt_zipcode = (TextView) findViewById(R.id.txtZipCode);
		frame_pay_setting = (FrameLayout) findViewById(R.id.frame_pay_setting);

		edit_eventName = (EditText) findViewById(R.id.edtEventName);
		edit_city = (EditText) findViewById(R.id.editCity);
		spnr_state = (Spinner) findViewById(R.id.editState);
		spnr_country = (Spinner) findViewById(R.id.editCountry);
		spnr_currency = (Spinner)findViewById(R.id.editcurrency);
		edit_event_des = (EditText) findViewById(R.id.edtEventDes);
		edit_eventLoc = (EditText) findViewById(R.id.editEventLocation);
		edit_event_address = (EditText) findViewById(R.id.editEventAddress);
		edit_zipcode = (EditText) findViewById(R.id.editZipCode);


		txtstart.setTypeface(Util.roboto_regular);
		txtend.setTypeface(Util.roboto_regular);

		txt_city.setTypeface(Util.roboto_regular);
		txt_state.setTypeface(Util.roboto_regular);
		txt_country.setTypeface(Util.roboto_regular);
		txt_desc.setTypeface(Util.roboto_regular);
		txt_loc.setTypeface(Util.roboto_regular);
		txt_zipcode.setTypeface(Util.roboto_regular);
		txt_address.setTypeface(Util.roboto_regular);

		txt_startdate.setTypeface(Util.roboto_regular);
		txt_enddate.setTypeface(Util.roboto_regular);
		txt_starttime.setTypeface(Util.roboto_regular);
		txt_endtime.setTypeface(Util.roboto_regular);
		edit_zipcode.setTypeface(Util.roboto_regular);
		edit_eventName.setTypeface(Util.roboto_regular);
		edit_city.setTypeface(Util.roboto_regular);
		edit_event_des.setTypeface(Util.roboto_regular);
		edit_eventLoc.setTypeface(Util.roboto_regular);
		edit_event_address.setTypeface(Util.roboto_regular);

		img_logo.setOnClickListener(this);
		txt_starttime.setOnClickListener(this);
		txt_enddate.setOnClickListener(this);
		txt_endtime.setOnClickListener(this);
		txt_startdate.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		btn_save.setOnClickListener(this);
		frame_pay_setting.setVisibility(View.GONE);
		frame_pay_setting.setOnClickListener(this);
		btn_save.setText("NEXT");

		//Log.i("-------------- Hosting Country Name------------",":"+this.getResources().getConfiguration().locale.getCountry());
		if (getIntent().getStringExtra(Util.EVENT_ACTION).equals(EDIT_EVENT)) {
			country_list = Util.db.getCountryList(getIntent().getStringExtra(Util.EVENT_ID));
		}else{
			country_list = Util.db.getCountryList("");
		}

		currency_list = Util.db.getCurrencyList();
		adapter_currency = new CurrencyAdapter();
		spnr_currency.setAdapter(adapter_currency);
		/*Arrays.sort(country_list, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				// TODO Auto-generated method stub
				return lhs.compareToIgnoreCase(rhs);
			}
		});*/

		adaptercountry = new ArrayAdapter<String>(this,
				R.layout.spinner_item, country_list) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);
				return v;
			}
		};
		spnr_country.setAdapter(adaptercountry);


		if (getIntent().getStringExtra(Util.EVENT_ACTION).equals(EDIT_EVENT)) {
			txt_title.setText("Edit Event");
			status = EDIT_EVENT;
			btn_save.setText("Save");
			event_controller = Util.db.getSelectedEventRecord(getIntent().getStringExtra(Util.EVENT_ID));

			//country_list = Util.db.getCountryList(event_controller.Events.Id);

			for (int i = 0; i < country_list.size(); i++) {
				if (country_list.get(i).trim().equalsIgnoreCase(event_controller.country)) {
					spnr_country.setSelection(i);
					setStateAdapter(country_list.get(i));
				}
			}

			for(int i=0;i<currency_list.size();i++){
				if(currency_list.get(i).Id.equalsIgnoreCase(event_controller.Events.BLN_Currency__c)){
					spnr_currency.setSelection(i);
				}
			}
			int order_count = Util.db.totalOrderCount(event_controller.Events.Id);
			//Log.i("----------------Order Count-----------",":"+order_count + " : "+event_controller.Events.Id+" : "+event_controller.Events.Name);
			if(order_count > 0){
				spnr_currency.setEnabled(false);
			}
			String filename = event_controller.Events.Id + "-"
					+ event_controller.Events.Id + Util.FILEFORMAT;


			if (!NullChecker(event_controller.image).isEmpty()) {

				String url[] =sfdcddetails.instance_url.split("/");

		        //String x =sfdcddetails.instance_url.substring(pos+1 , sfdcddetails.instance_url.length()-1);

				Picasso.with(AddEventActivity.this).load(event_controller.image)
				.placeholder(R.drawable.default_image)
				.error(R.drawable.default_image).into(img_logo);
				//imageloder.displayImage(user_profile.profileimage, profile_img, options);
			}

			if(!NullChecker(event_controller.image).isEmpty()){
			BitmapDrawable drawable = (BitmapDrawable) img_logo.getDrawable();
			 photo = drawable.getBitmap();
			}else{
				photo=null;
			}

			//photo = Util.db.GetImage(filename);
			/*if (getIntent().getByteArrayExtra("Logo") != null) {
				photo = Util.db
						.getBitmap(getIntent().getByteArrayExtra("Logo"));
				if (photo != null)
					img_logo.setImageBitmap(photo);
			}*/

			edit_eventName.setText(event_controller.Events.Name);
			try {
				String event_start_date = Util.change_US_ONLY_DateFormat(event_controller.Events.Start_Date__c, event_controller.Events.Time_Zone__c);
				String event_end_date = Util.change_US_ONLY_DateFormat(event_controller.Events.End_Date__c, event_controller.Events.Time_Zone__c);
				txt_startdate.setText(Util.date_format.format(Util.db_date_format1.parse(event_start_date)));
				txt_enddate.setText(Util.date_format.format(Util.db_date_format1.parse(event_end_date)));
				String event_start_time[] = Util.db_server_ticket_format.format(Util.db_date_format1.parse(event_start_date)).split(" ");
				txt_starttime.setText(event_start_time[1].trim()+" "+event_start_time[2].trim());
				String event_end_time[] = Util.db_server_ticket_format.format(Util.db_date_format1.parse(event_end_date)).split(" ");
				txt_endtime.setText(event_end_time[1].trim()+" "+event_end_time[2].trim());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			edit_event_address.setText(event_controller.Events.Street1__c);
			edit_eventLoc.setText(event_controller.Events.Venue_Name__c);
			edit_city.setText(event_controller.Events.City__c);

			if (!NullChecker(event_controller.Events.ZipCode__c).equals(
					"(null)")) {
				edit_zipcode
						.setText(NullChecker(event_controller.Events.ZipCode__c));
			}
			if (!NullChecker(event_controller.Events.Description__c).equals(
					"(null)")) {
				edit_event_des.setText(Html.fromHtml(event_controller.Events.Description__c));
			}

		} else {
			status = CREATE_EVENT;
			txt_title.setText("Add Event");

			//spnr_country.setSelection(0);
			//setStateAdapter((String) spnr_country.getSelectedItem());
			for (int i = 0; i < country_list.size(); i++) {
				if (country_list.get(i).trim().equalsIgnoreCase("United States Of America")) {
					spnr_country.setSelection(i);
					setStateAdapter(country_list.get(i));
					break;
				}
			}

			String country_name = country_list.get(spnr_country.getSelectedItemPosition()).trim();
			String country_id = Util.db.getCountryId(country_name);
			Currency currency = Util.db.getCurrencyFromCountry(country_id);
			for(int i=0;i<currency_list.size();i++){
				if(currency_list.get(i).Id.equalsIgnoreCase(currency.Id)){
					spnr_currency.setSelection(i);
				}
			}
			setCurrentDateOnView();
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {

		if (v == btn_cancel) {
			finish();
		} else if (v == txt_startdate) {
			if(smday==-1&&smmonth==-1&&smyear==-1){
				Calendar c = Calendar.getInstance();
				smyear = c.get(Calendar.YEAR);
				smmonth = c.get(Calendar.MONTH);
				smday = c.get(Calendar.DAY_OF_MONTH);
			}
			openStarDatePickerDialog();
			//showDialog(STARTDATE_DIALOG_ID);
		} else if (v == txt_starttime) {

			 Calendar c = Calendar.getInstance();
			shour = c.get(Calendar.HOUR_OF_DAY);
			sminute = c.get(Calendar.MINUTE);
			//startdatePickerListener
			showDialog(STARTTIME_DIALOG_ID);
		} else if (v == txt_enddate) {
			//Calendar c = Calendar.getInstance();
			if(smday==-1&&smmonth==-1&&smyear==-1){
				Calendar c = Calendar.getInstance();
				smyear = c.get(Calendar.YEAR);
				smmonth = c.get(Calendar.MONTH);
				smday = c.get(Calendar.DAY_OF_MONTH);
			}
			emyear = smyear;//c.get(Calendar.YEAR);
			emmonth = smmonth;//c.get(Calendar.MONTH);
			emday = smday;//c.get(Calendar.DAY_OF_MONTH);

			openEndDatePicker();
			//showDialog(ENDDATE_DIALOG_ID);
		} else if (v == txt_endtime) {
		    Calendar c = Calendar.getInstance();
			ehour = shour;//c.get(Calendar.HOUR_OF_DAY);
			eminute = sminute;//c.get(Calendar.MINUTE);
			showDialog(ENDTIME_DIALOG_ID);
		} else if (v == btn_save) {
			if (checkEventValidation()) {
				try {
					Date varDate = Util.server_dateFormat.parse(txt_startdate.getText().toString().trim());
					Date e_varDate = Util.server_dateFormat.parse(txt_enddate.getText().toString().trim());

					SimpleDateFormat server_format = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
					server_start_date = server_format.format(varDate);
					server_end_date = server_format.format(e_varDate);

					setEventData();
					if (isOnline()) {
						btn_save.setEnabled(false);
						doRequest();
					} else {
						btn_save.setEnabled(true);
						startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
					}

				} catch (ParseException e) {
					btn_save.setEnabled(true);
					e.printStackTrace();
				}
			}
			/*
			 * }else { Intent eventactivity = new Intent(AddEventActivity.this,
			 * AddTicketActivity.class); eventactivity.putExtra("Status",
			 * "CREATE"); eventactivity.putExtra(Util.ADDEVENT, Util.ADDEVENT);
			 * eventactivity.putExtra(Util.ADDED_EVENT_ID, added_Event_id);
			 * eventactivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 * startActivity(eventactivity); finish(); }
			 */
		} /*else if (v == txt_camera) {
			photo_slider.animateClose();
			takeImage(Util.PIC_FROM_CAMERA,AddEventActivity.this);
		} else if (v == txt_gallery) {
			photo_slider.animateClose();
			takeImage(Util.PIC_FROM_GALLERY,AddEventActivity.this);
		} else if (v == txt_cancel) {
			photo_slider.animateClose();
		}*/ else if (v == img_logo) {
			openTakeFromDialg(AddEventActivity.this);
		} else if (v == frame_pay_setting) {
			/*
			 * Intent i=new Intent(AddEventActivity.this,PaymentSetting.class);
			 * i.putExtra(Util.EDIT_EVENT_ID,
			 * getIntent().getStringExtra("EventId")); startActivity(i);
			 */
		}

	}

	private void setEventData() {

		// eventController.setEventId(getIntent().getStringExtra(Constants.EVENT_ID));
		eventController.Events.Name = eventName;
		eventController.Events.Id = event_controller.Events.Id;
		if (eventController.Events.Tax_Rate__c.isEmpty())
			eventController.Events.Tax_Rate__c = "00.00";
		else
			eventController.Events.Tax_Rate__c = event_controller.Events.Tax_Rate__c;

		eventController.Events.Start_Date__c = txt_startdate.getText()
				.toString();
		eventController.Events.Start_Time__c = txt_starttime.getText()
				.toString();

		eventController.Events.End_Date__c = txt_enddate.getText().toString();
		eventController.Events.End_Time__c = txt_endtime.getText().toString();
		eventController.Events.Street1__c = event_address;
		eventController.Events.Venue_Name__c = event_location;
		eventController.Events.Phone_Number__c = "";
		eventController.Events.ZipCode__c = event_zipcode;
		eventController.Events.Event_Type__c = "";
		eventController.country = event_country;
		eventController.Events.City__c = event_city;
		// eventController.setEventBadgeName(event_controller.getEventBadgeName());
		eventController.state = event_state;
		eventController.Events.Time_Zone__c = event_controller.Events.Time_Zone__c;
		eventController.Events.Description__c = event_desc;
        eventController.Events.BLN_Currency__c = currency_list.get(spnr_currency.getSelectedItemPosition()).Id.trim();
	}

	private boolean checkEventValidation() {
		boolean ret = true;

		eventName = edit_eventName.getText().toString().trim();
		event_city = edit_city.getText().toString().trim();
		// event_state = edit_state.getText().toString().trim();
		event_address = edit_event_address.getText().toString().trim();
		event_location = edit_eventLoc.getText().toString().trim();
		event_desc = edit_event_des.getText().toString().trim();
		event_zipcode = edit_zipcode.getText().toString().trim();
		event_country = Util.db.getCountryId(spnr_country.getSelectedItem()
				.toString());
		event_state = spnr_state.getSelectedItem().toString();

		try {
			if (event_state.equalsIgnoreCase("--Select--")) {
				event_state = "";

			} else {
				event_state = Util.db.getStateId(event_state);
			}

			start_date = Util.eventDate_format.parse(txt_startdate.getText()
					.toString() + " " + txt_starttime.getText().toString());
			end_date = Util.eventDate_format.parse(txt_enddate.getText()
					.toString() + " " + txt_endtime.getText().toString());
			// today_date =
			// Util.eventDate_format.parse(Util.eventDate_format.format(new
			// Date()));

		} catch (ParseException e) {

			e.printStackTrace();
		}

		if (!Validation.hasTextAll(edit_eventName,	getResources().getString(R.string.event_name_alert)))
			ret = false;
		else if (!Validation.hasValidDate(txt_endtime, getResources()
				.getString(R.string.event_date_alert), start_date, end_date)) {
			ret = false;
		}else if(NullChecker(event_location).isEmpty()){
			ret = false;
			edit_eventLoc.setError("Event location is missing.");
		}

		/*else if (event_state.isEmpty()) {
			ret = false;
			txtstate_error.requestFocus();
			txtstate_error.setError("State Should not be empty");
		}*/
		_event_payment_setting = Util.db.getEvent_Payment_Setting(getIntent().getStringExtra(Util.EVENT_ID));

		/*
		 * if(_event_payment_setting.getCount()==0) ret=false;
		 */

		return ret;
	}

	public void setCurrentDateOnView() {

		final Calendar c = Calendar.getInstance();
		shour = c.get(Calendar.HOUR_OF_DAY);
		// Current Minute
		sminute = c.get(Calendar.MINUTE);
		smyear = c.get(Calendar.YEAR);
		smmonth = c.get(Calendar.MONTH);
		smday = c.get(Calendar.DAY_OF_MONTH);

		final Calendar c1 = Calendar.getInstance();

		ehour = c1.get(Calendar.HOUR_OF_DAY);
		// Current Minute
		eminute = c1.get(Calendar.MINUTE);

		updateStartTime(shour, sminute);
		updateEndTime(ehour, eminute);
		emyear = c1.get(Calendar.YEAR);
		emmonth = c1.get(Calendar.MONTH);
		emday = c1.get(Calendar.DAY_OF_MONTH);

		if (String.valueOf(smday).length() == 1) {
			event_start_date = new StringBuilder().append(smyear).append("-")
					.append(smmonth + 1).append("-").append("0" + smday)
					.append(" ").toString().trim();

			try {
				c.setTime(format1.parse(event_start_date));
			} catch (ParseException e) {

				e.printStackTrace();
			}
			txt_startdate.setText(format.format(c.getTime()));
		} else {
			event_start_date = new StringBuilder().append(smyear).append("-")
					.append(smmonth + 1).append("-").append(smday).append(" ")
					.toString().trim();
			try {
				c.setTime(format1.parse(event_start_date));
			} catch (ParseException e) {

				e.printStackTrace();
			}
			txt_startdate.setText(format.format(c.getTime()));
		}
		if (String.valueOf(emday).length() == 1) {
			event_end_date = new StringBuilder()
					// Month is 0 based, just add 1
					.append(emyear).append("-").append(emmonth + 1).append("-")
					.append("0" + emday).append(" ").toString().trim();
			try {
				c.setTime(format1.parse(event_end_date));
			} catch (ParseException e) {

				e.printStackTrace();
			}
			txt_enddate.setText(format.format(c.getTime()));
		} else {
			event_end_date = new StringBuilder()
					// Month is 0 based, just add 1
					.append(emyear).append("-").append(emmonth + 1).append("-")
					.append(emday).append(" ").toString().trim();
			try {
				c.setTime(format1.parse(event_end_date));
			} catch (ParseException e) {

				e.printStackTrace();
			}

			txt_enddate.setText(format.format(c.getTime()));
		}

	}

	private void updateStartTime(int hours, int mins) {

		String timeSet = "";
		if (hours > 12) {
			hours -= 12;
			timeSet = "PM";
		} else if (hours == 0) {
			hours += 12;
			timeSet = "AM";
		} else if (hours == 12)
			timeSet = "PM";
		else
			timeSet = "AM";

		String minutes = "";
		int mode = mins % 15;

		int time_slot = 15;

		//if (mode > 15 / 2) {
			time_slot = time_slot - mode;
			mins = mins + time_slot;
			minutes = String.valueOf(mins);
			if (mins == 0) {
				minutes = "00";
			}if (mins == 60) {
				hours = hours + 1;
				minutes = "00";
			}

		/*} else {
			mins = mins - mode;
			minutes = String.valueOf(mins);
			if (mins == 0) {
				minutes = "00";
			}
		}*/

		// Append in a StringBuilder
		String aTime = new StringBuilder().append(hours).append(':')
				.append(minutes).append(" ").append(timeSet).toString();

		txt_starttime.setText(aTime);
		txt_endtime.setText(txt_starttime.getText().toString());
	}

	private void updateEndTime(int hours, int mins) {

		String timeSet = "";
		if (hours > 12) {
			hours -= 12;
			timeSet = "PM";
		} else if (hours == 0) {
			hours += 12;
			timeSet = "AM";
		} else if (hours == 12)
			timeSet = "PM";
		else
			timeSet = "AM";
		String minutes = "";
		int mode = mins % 15;

		int time_slot = 15;

		if (mode > 15 / 2) {
			time_slot = time_slot - mode;
			mins = mins + time_slot;
			minutes = String.valueOf(mins);
			if (mins == 60) {
				hours = hours + 1;
				minutes = "00";
			}

		} else {
			mins = mins - mode;
			minutes = String.valueOf(mins);
			if (mins == 0) {
				minutes = "00";
			}
		}

		String aTime = new StringBuilder().append(hours).append(':')
				.append(minutes).append(" ").append(timeSet).toString();

		txt_endtime.setText(aTime);
	}

	static Date toNearestWholeHour(Date d) {
		Calendar c = new GregorianCalendar();
		c.setTime(d);

		if (c.get(Calendar.MINUTE) >= 30)
			c.add(Calendar.HOUR, 1);

		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);

		return c.getTime();
	}

	private  TimePickerDialog.OnTimeSetListener starttimepickerlistener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {

			if ((hourOfDay >= c.get(Calendar.HOUR_OF_DAY))
					|| (hourOfDay == c.get(Calendar.HOUR_OF_DAY) && minutes >= c
							.get(Calendar.MINUTE))) {


				shour = hourOfDay;
				sminute = minutes;

				updateStartTime(shour, sminute);
				dismissDialog(STARTTIME_DIALOG_ID);
			}else{

				dismissDialog(STARTTIME_DIALOG_ID);
			}
		}


	};


	private  TimePickerDialog.OnTimeSetListener endtimepickerlistener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {

			if ((hourOfDay >= c.get(Calendar.HOUR_OF_DAY))
					|| (hourOfDay == c.get(Calendar.HOUR_OF_DAY) && minutes >= c
							.get(Calendar.MINUTE))) {
				ehour = hourOfDay;
				eminute = minutes;

				updateEndTime(ehour, eminute);

			}
		}
	};


	public void openStarDatePickerDialog(){
		DatePickerDialog _date = new DatePickerDialog(this, startdatePickerListener, smyear,
				smmonth, smday) {
			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				if (year < smyear)
					view.updateDate(smyear, smmonth, smday);

				if (monthOfYear < smmonth && year == smyear)
					view.updateDate(smyear, smmonth, smday);

				if (dayOfMonth < smday && year == smyear
						&& monthOfYear == smmonth)
					view.updateDate(smyear, smmonth, smday);


			}
		};
		_date.setTitle("Set Start Date");
		_date.show();
	}

	public void openEndDatePicker(){
		emyear=smyear;
		emmonth=smmonth;
		emday=smday;
		// set date picker as current date
		DatePickerDialog _date = new DatePickerDialog(this, enddatePickerListener, emyear,
				emmonth, emday) {
			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				if (year < emyear)
					view.updateDate(emyear, emmonth, emday);

				if (monthOfYear < emmonth && year == emyear)
					view.updateDate(emyear, emmonth, emday);

				if (dayOfMonth < emday && year == emyear
						&& monthOfYear == emmonth)
					view.updateDate(emyear, emmonth, emday);

			}
		};
		_date.setTitle("Set End Date");
		_date.show();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		DatePickerDialog _date;

		switch (id) {
		case STARTDATE_DIALOG_ID:
			// set date picker as current date

			_date = new DatePickerDialog(this, startdatePickerListener, smyear,
					smmonth, smday) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < smyear)
						view.updateDate(smyear, smmonth, smday);

					if (monthOfYear < smmonth && year == smyear)
						view.updateDate(smyear, smmonth, smday);

					if (dayOfMonth < smday && year == smyear
							&& monthOfYear == smmonth)
						view.updateDate(smyear, smmonth, smday);

				}
			};
			_date.setTitle("Set Start Date");
			return _date;
		}
		switch (id) {
		case ENDDATE_DIALOG_ID:

			emyear=smyear;
			emmonth=smmonth;
			emday=smday;
			// set date picker as current date
			_date = new DatePickerDialog(this, enddatePickerListener, emyear,
					emmonth, emday) {
				@Override
				public void onDateChanged(DatePicker view, int year,
						int monthOfYear, int dayOfMonth) {
					if (year < emyear)
						view.updateDate(emyear, emmonth, emday);

					if (monthOfYear < emmonth && year == emyear)
						view.updateDate(emyear, emmonth, emday);

					if (dayOfMonth < emday && year == emyear
							&& monthOfYear == emmonth)
						view.updateDate(emyear, emmonth, emday);

				}
			};
			_date.setTitle("Set End Date");
			return _date;
		}
		switch (id) {
		case STARTTIME_DIALOG_ID:

			// set date picker as current date

			  Calendar c = Calendar.getInstance();
				shour = c.get(Calendar.HOUR_OF_DAY);
				sminute = c.get(Calendar.MINUTE);

			if (status.equalsIgnoreCase(EDIT_EVENT)) {
				try {
					String event_start_date = Util.change_US_ONLY_DateFormat(event_controller.Events.Start_Date__c, event_controller.Events.Time_Zone__c);
					String event_start_time[] = Util.db_server_ticket_format.format(Util.db_date_format1.parse(event_start_date)).split(" ");
					c.setTime(time_format
							.parse(event_start_time[1].trim()+" "+event_start_time[2].trim()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				shour = c.get(Calendar.HOUR_OF_DAY);
				sminute = c.get(Calendar.MINUTE);
			}
			return new TimePickerDialog(this, starttimepickerlistener, shour,
					sminute, true);


		}
		switch (id) {
		case ENDTIME_DIALOG_ID:

			// set date picker as current date
			if (status.equalsIgnoreCase("Edit")) {
				try {
					String event_end_date = Util.change_US_ONLY_DateFormat(event_controller.Events.End_Date__c, event_controller.Events.Time_Zone__c);
					String event_end_time[] = Util.db_server_ticket_format.format(Util.db_date_format1.parse(event_end_date)).split(" ");
					c.setTime(time_format
							.parse(event_end_time[1].trim()+" "+event_end_time[2].trim()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				ehour = c.get(Calendar.HOUR_OF_DAY);
				eminute = c.get(Calendar.MINUTE);
			}
			return new TimePickerDialog(this, endtimepickerlistener, ehour,
					eminute, true);
		}
		return null;

	}

	private final DatePickerDialog.OnDateSetListener startdatePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			smyear = selectedYear;
			smmonth = selectedMonth;
			smday = selectedDay;

			if (String.valueOf(smday).length() == 1) {

				event_start_date = new StringBuilder().append(smyear)
						.append("-").append(smmonth + 1).append("-")
						.append("0" + smday).append(" ").toString().trim();

				try {
					c.setTime(format1.parse(event_start_date));
				} catch (ParseException e) {

					e.printStackTrace();
				}

				txt_startdate.setText(format.format(c.getTime()));
			} else {

				event_start_date = new StringBuilder().append(smyear)
						.append("-").append(smmonth + 1).append("-")
						.append(smday).append(" ").toString().trim();

				try {
					c.setTime(format1.parse(event_start_date));
				} catch (ParseException e) {

					e.printStackTrace();
				}

				txt_startdate.setText(format.format(c.getTime()));
				txt_enddate.setText(txt_startdate.getText().toString());
			}
		}
	};

	private final DatePickerDialog.OnDateSetListener enddatePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,int selectedMonth, int selectedDay) {
			emyear = selectedYear;
			emmonth = selectedMonth;
			emday = selectedDay;

			if (String.valueOf(emday).length() == 1) {

				event_end_date = new StringBuilder()
						// Month is 0 based, just add 1
						.append(emyear).append("-").append(emmonth + 1)
						.append("-").append("0" + emday).append(" ").toString()
						.trim();

				try {
					c.setTime(format1.parse(event_end_date));
				} catch (ParseException e) {

					e.printStackTrace();
				}

				txt_enddate.setText(format.format(c.getTime()));

			} else {

				event_end_date = new StringBuilder()
						// Month is 0 based, just add 1
						.append(emyear).append("-").append(emmonth + 1)
						.append("-").append(emday).append(" ").toString()
						.trim();

				try {
					c.setTime(format1.parse(event_end_date));
				} catch (ParseException e) {

					e.printStackTrace();
				}

				txt_enddate.setText(format.format(c.getTime()));

			}

		}
	};

	private JSONObject setJsonBodyData() {
		JSONObject obj = new JSONObject();

		try {
			if (status.equals(EDIT_EVENT)) {
				obj.put("EventID", eventController.Events.Id);
			}
			obj.put("EventName", eventController.Events.Name);
			obj.put("EventSDate", server_start_date + " "+ eventController.Events.Start_Time__c);
			obj.put("EventEDate", server_end_date + " "  + eventController.Events.End_Time__c);
			obj.put("AttendeeReg", "");
			obj.put("CurrencySetting", "");
			obj.put("EventCategory", "");
			obj.put("EventCity", eventController.Events.City__c);
			obj.put("EventCountry", event_country);
			obj.put("EventDesc", eventController.Events.Description__c);
			obj.put("EventHashTag", "");
			obj.put("EventLocation", eventController.Events.Venue_Name__c);
			obj.put("EventOrgEmail", user_profile.Profile.Email__c);
			obj.put("EventStatus", "Live");
			obj.put("EventState", event_state);
			obj.put("FacebookUrl", "");
			obj.put("HostingAddress1", eventController.Events.Street1__c);
			obj.put("HostingAddress2", "");
			TimeZone time_zone = TimeZone.getDefault();

			if (status.equals(EDIT_EVENT)){
				obj.put("HostingTimeZone", eventController.Events.Time_Zone__c);
			}else{
				 if(time_zone.getID().equalsIgnoreCase("Asia/Calcutta")){
				     obj.put("HostingTimeZone", "Asia/Kolkata");
				 }else{
					 obj.put("HostingTimeZone", time_zone.getID());
				 }
			}
			obj.put("LanguageSetting", "en_US");
			obj.put("OrgName", "");
			obj.put("PhoneNo", "");
			obj.put("TwitterUrl", "");
			obj.put("WebUrl", "");
			obj.put("ZipCode", eventController.Events.ZipCode__c);
			obj.put("CurrencySetting", eventController.Events.BLN_Currency__c);

			if (photo != null)
				obj.put("Image",
						Util.db.getimagedata(Util.db.getByteArray(photo)));
			else
				obj.put("Image", "");

			// //Log.i("Image",
			// Util.db.getimagedata(Util.db.getByteArray(photo)));

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return obj;
	}

	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " "+ sfdcddetails.access_token;
		if (status.equals(CREATE_EVENT)) {
			String _url = sfdcddetails.instance_url+ WebServiceUrls.SA_ADD_EVENT + "Userid="+ sfdcddetails.user_id + "&CompanyId="+ user_profile.Profile.Default_Company_ID__c;
			postMethod = new HttpPostData("Saving Event Info...",_url, setJsonBodyData().toString(),access_token, AddEventActivity.this);
			postMethod.execute();
		} else if (status.equals(EDIT_EVENT)) {
			String url = sfdcddetails.instance_url	+ WebServiceUrls.SA_ADD_EVENT + "Userid="+ sfdcddetails.user_id + "&CompanyId="+ user_profile.Profile.Default_Company_ID__c;
			postMethod = new HttpPostData("Saving Event Info...",url,setJsonBodyData().toString(), access_token, AddEventActivity.this);
			postMethod.execute();
		}

	}

	@Override
	public void parseJsonResponse(String response) {

		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}

			Object json = new JSONTokener(response).nextValue();
			if (json instanceof JSONArray) {
				JSONArray array = new JSONArray(response);
				startLoginErrorAnimation(
						array.optJSONObject(0).optString("message"),
						txt_error_msg);
			} else if (json instanceof JSONObject) {
				JSONObject obj = new JSONObject(response);
				if(obj.has("msg")){
					if(!NullChecker(obj.optString("msg")).isEmpty() && !NullChecker(obj.optString("msg")).toLowerCase().equalsIgnoreCase("success")){
						btn_save.setEnabled(true);
					   showCurrencyAlert(obj.optString("msg"));
					   return;
					}
				}

				gson = new Gson();
				EditEventResponse eventresponse = gson.fromJson(response,EditEventResponse.class);
				checked_in_eventId  = eventresponse.Events.Id;
				Util.db.UpdateEditEvent(eventresponse);
				//added_Event_id = eventresponse.Events.Id;
				if (getIntent().getStringExtra(Util.EVENT_ACTION).equals(EDIT_EVENT)) {

					if (Util.db.checkEventCount() == 1) {
						if (isOnline()) {
							if (!getIntent().getStringExtra(Util.EVENT_ACTION).equals(EDIT_EVENT)) {
								Intent i = new Intent(AddEventActivity.this,DashboardActivity.class);
								i.putExtra("CheckIn Event", event_details_controller.get(0));
								startActivity(i);
								finish();
							} else {
								Intent eventactivity = new Intent(AddEventActivity.this,EventListActivity.class);
								eventactivity
										.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								startActivity(eventactivity);
								finish();
							}
						}
						//btn_save.setEnabled(true);
					} else {
						// btn_save.setText(Util.NEXT);
						Intent eventactivity = new Intent(AddEventActivity.this, EventListActivity.class);
						eventactivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(eventactivity);
						finish();
					}
				} else {

					Util._savePreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID,eventresponse.Events.Id);
					Intent eventactivity = new Intent(AddEventActivity.this,
							AddTicketActivity.class);
					eventactivity.putExtra("Status", "CREATE");
					eventactivity.putExtra(Util.ADDEVENT, Util.ADDEVENT);
					eventactivity.putExtra(Util.ADDED_EVENT_ID, eventresponse.Events.Id);
					eventactivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(eventactivity);
					finish();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();

			startLoginErrorAnimation(e.getLocalizedMessage(), txt_error_msg);
			btn_save.setEnabled(true);
		}
	}

	private void startLoginErrorAnimation(String msg, TextView view) {
		animTranslate = AnimationUtils.loadAnimation(AddEventActivity.this,
				R.anim.text_translate);
		animTranslate.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {

				timer.start();

			}
		});
		view.setVisibility(View.VISIBLE);
		view.setText(msg);
		view.startAnimation(animTranslate);

	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){

            String path = data.getStringExtra(CropImage.IMAGE_PATH);

            if ((path == null)||(TextUtils.isEmpty(path))) {
                return;
            }
          Bitmap  bitmap = BitmapFactory.decodeFile(path);
          photo = bitmap;
          img_logo.setImageBitmap(bitmap);

           if(mFileTemp!=null){
           	mFileTemp.delete();
           }
		}else if ((requestCode == PICK_FROM_CAMERA) && (resultCode == RESULT_OK)) {

			if (data != null) {

				//doCrop();
			} else {

				File mediaStorageDir = new File(
						Environment.getExternalStorageDirectory(),
						"ScanAttendee");
				if(!mediaStorageDir.exists()){
					mediaStorageDir.mkdir();
				}
				mediaFile = new File(mediaStorageDir.getPath() + File.separator+ "IMG_1.jpg");
				mImageCaptureUri = Uri.fromFile(mediaFile);
				AppUtils.displayLog("-------------mImageCaptureUri---------",":"+mImageCaptureUri);
				if (mImageCaptureUri != null) {
					try {
						startCropImage(AddEventActivity.this);
					} catch (Exception e) {
						e.printStackTrace();
						AppUtils.displayLog("-------------Image Data Exception---------",":"+e.getMessage());
					}
				}

			}

		} else if (requestCode == PICK_FROM_FILE && data != null
				&& data.getData() != null) {

			mImageCaptureUri = data.getData();
			//mediaFile = new File(getRealPathFromURI(mImageCaptureUri));
			try {
				File mediaStorageDir = new File(
						Environment.getExternalStorageDirectory(),
						"ScanAttendee");
				if(!mediaStorageDir.exists()){
					mediaStorageDir.mkdir();
				}
				mFileTemp = new File(mediaStorageDir.getPath() + File.separator
						+ "IMG_1.jpg");
				InputStream inputStream = getContentResolver().openInputStream(data.getData());
	            FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
	            CropUtil.copyStream(inputStream, fileOutputStream);
	            mediaFile = mFileTemp;
				startCropImage(AddEventActivity.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == CROP_FROM_CAMERA) {

			Bundle extras = data.getExtras();
			if (extras != null) {
				photo = extras.getParcelable("data");
				img_logo.setImageBitmap(photo);
			}
		}else if (requestCode == FINISH_RESULT) {
			startActivity(new Intent(AddEventActivity.this, SplashActivity.class));
			finish();
		}



	}

	private void doCrop() {

		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app.",
					Toast.LENGTH_SHORT).show();

			return;
		} else {

			intent.setData(mImageCaptureUri);
			intent.putExtra("crop", "true");
			intent.putExtra("outputX", 90);
			intent.putExtra("outputY", 90);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			// intent.putExra("circleCrop", "true");
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {

							int id = getLastImageId();
							removeImage(id);

							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}

	}

	private int getLastImageId() {

		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
		@SuppressWarnings("deprecation")
		Cursor imageCursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				null, null, imageOrderBy);
		// this.startManagingCursor(imageCursor);
		if (imageCursor.moveToFirst()) {
			int id = imageCursor.getInt(imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID));
			return id;
		} else {
			return 0;
		}

	}

	private void removeImage(int id) {

		ContentResolver cr = getContentResolver();
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media._ID + "=?",
				new String[] { Long.toString(id) });

	}

   private class CurrencyAdapter extends BaseAdapter{

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return currency_list.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Currency getItem(int position) {
		// TODO Auto-generated method stub
		return currency_list.get(position);
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
		View v = inflater.inflate(R.layout.spinner_item, null);
		TextView text_view = (TextView)v.findViewById(R.id.spinner_text);
		text_view.setText(getItem(position).Currency_Name__c);
		return v;
	}

   }

   private void showCurrencyAlert(String msg){
		Util.setCustomAlertDialog(AddEventActivity.this);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.error);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {

	}
}
