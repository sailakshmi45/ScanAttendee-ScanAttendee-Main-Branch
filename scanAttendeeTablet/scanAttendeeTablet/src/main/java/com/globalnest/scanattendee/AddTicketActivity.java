//  ScanAttendee Android
//  Created by Ajay
//  This class is used to add or edit a ticket and upload data to the backend
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

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
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.TicketHandler;
import com.globalnest.mvc.TicketResponseHandler;
import com.globalnest.mvc.TicketTypeHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class AddTicketActivity extends BaseActivity implements OnClickListener {

	private CheckBox check_minimun;
	public static SimpleDateFormat ticket_date_format = new SimpleDateFormat("MM/dd/yyyy hh:mm a",Locale.US);
	private int smyear,smmonth,smday,emyear,emmonth,emday,shour, ehour, sminute, eminute;
	static final int STARTDATE_DIALOG_ID = 0;
	static final int ENDDATE_DIALOG_ID = 1;
	static final int STARTTIME_DIALOG_ID = 1111;
	static final int ENDTIME_DIALOG_ID = 1112;

	Spinner spnr_type, spnr_ticket_type;
	EditText edit_t_name, edit_t_price, edit_t_qty;
	TextView txt_price, txt_add_photo, t_start_date,t_start_time, t_end_date, t_end_time;
	ImageView img_ticket;
	TextView txtTicSetting;
	LinearLayout image_layout;
	ExpandablePanel panel;

	String status = "", name = "", price = "", qty = "", tax = "", ticket_option="", fee_option="", item_type_id="",
			t_type = "Select ticket type", ticket_start_date="", ticket_end_date="", event_end_date="", today_date="",img_url="";
	String[] ticket_type = {  "Paid", "Free", "Donation" };
	String[] ticket_visiblity={"Public","Private","Closed"};
	//String[] ticket_option_type = { "Admission", "Item"};
	TicketHandler _itemhandler = new TicketHandler();
	ArrayAdapter<String> ticketType, ticket_opn_type,adapter_ticketVisibility;

	Bitmap photo=null;
	//SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
	//SimpleDateFormat format = new SimpleDateFormat("MMMM dd, yyyy");
	//SimpleDateFormat time_format = new SimpleDateFormat("hh:mm a");
	Calendar c = Calendar.getInstance();
	byte[] ticket_image=null;

	Spinner spnr_ticket_visibility;
	RadioButton includefee, excludefee,infofromattendees,infofrombuyer;
	Switch switch_isTax,scanattendee_onsite_visibility;
	TextView selfservice_text;
	CheckBox checkBox_Badgable;
	EditText edt_badgelabel;
	//boolean isTaxApplicable=false;
	ArrayList<TicketTypeHandler> itemType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		status = getIntent().getStringExtra("Status");
		img_url = getIntent().getStringExtra("Logo");
		if (status.equals("EDIT")){
			_itemhandler = (TicketHandler) getIntent()
					.getSerializableExtra("Edit");
		}

		itemType = Util.db.getItemType(_itemhandler.getItem_type__c());
		checkedin_event_record=Util.db.getSelectedEventRecord(checked_in_eventId);

		setCustomContentView(R.layout.add_ticket_layout);
		checkBox_Badgable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(status.equals("EDIT")&&_itemhandler.getBadgable().equalsIgnoreCase("B - Badge")) {
					edt_badgelabel.setText(_itemhandler.getBadgelabel());
				}
				if (isChecked) {
					edt_badgelabel.setEnabled(true);
				}else
					edt_badgelabel.setEnabled(false);
				/*else {
					if (checkBox_Badgable.isChecked()) {
						edt_badgelabel.setText("Attendee");
					}
				}*/
			}
		});
		spnr_ticket_visibility.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if(status.equals("CREATE")) {
					if((t_type.trim().equalsIgnoreCase("Free"))&&spnr_ticket_visibility.getSelectedItem().equals("Public")){
						scanattendee_onsite_visibility.setChecked(true);
					}else{
						scanattendee_onsite_visibility.setChecked(false);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		spnr_type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {

				// edit_empty.setError(null);
				t_type = ticketType.getItem(position);

				if(position ==1){
					edit_t_price.setText("0.00");
					edit_t_price.setEnabled(false);
					switch_isTax.setVisibility(View.GONE);
					//selfservice_text.setVisibility(View.VISIBLE);
					//scanattendee_onsite_visibility.setVisibility(View.VISIBLE);
					check_minimun.setVisibility(View.GONE);
				}/*else if (position == 2) {
					edit_t_price.setEnabled(false);
					check_minimun.setVisibility(View.VISIBLE);
					switch_isTax.setVisibility(View.VISIBLE);
				}*/ else {
					edit_t_price.setEnabled(true);
					check_minimun.setVisibility(View.GONE);
					switch_isTax.setVisibility(View.VISIBLE);
					//selfservice_text.setVisibility(View.GONE);
					//scanattendee_onsite_visibility.setVisibility(View.GONE);
				}
				if(status.equals("CREATE")) {
					if ((t_type.trim().equalsIgnoreCase("Free")) && spnr_ticket_visibility.getSelectedItem().equals("Public")) {
						scanattendee_onsite_visibility.setChecked(true);
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {


			}
		});
		spnr_ticket_type.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {

				item_type_id = itemType.get(position).getItemTypeId();
				if(status.equals("CREATE")) {
					if ((t_type.trim().equalsIgnoreCase("Free")) && itemType.get(position).getItemTypeName().equals("Admissions") && spnr_ticket_visibility.getSelectedItem().equals("Public")) {
						scanattendee_onsite_visibility.setChecked(true);
					} else {
						scanattendee_onsite_visibility.setChecked(false);
					}
					if(itemType.get(position).getItemTypeName().equals("Admissions")){
						checkBox_Badgable.setChecked(true);
					}else {
						checkBox_Badgable.setChecked(false);
						edt_badgelabel.setEnabled(false);
					}
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {


			}
		});

		switch_isTax.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

				if(isChecked){
					if(!NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")){
						switch_isTax.setChecked(false);
						AlertDialogCustom dialog=new AlertDialogCustom(AddTicketActivity.this);
						Intent i=new Intent(AddTicketActivity.this,SalesTaxActivity.class);
						dialog.setFirstButtonName("UPDATE");
						dialog.setSecondButtonName("CANCEL");
						dialog.setParamenters("Alert", "Please turn on tax on event level, before you updated on ticket level.", i, null, 2, false);
						dialog.show();
					}
				}
			}
		});

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (NullChecker(getIntent().getStringExtra(Util.ADDEVENT))
						.equals(Util.ADDEVENT)) {
					Intent i = new Intent(AddTicketActivity.this,DashboardActivity.class);
					i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					i.putExtra(Util.ADDEVENT, Util.ADDEVENT);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					finish();
				} else {
					finish();
				}
			}
		});
		scanattendee_onsite_visibility.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(t_type.trim().equalsIgnoreCase("Free")||t_type.trim().equalsIgnoreCase("Paid")){
					scanattendee_onsite_visibility.setChecked(isChecked);
				}
				else{
					Toast.makeText(AddTicketActivity.this, "Only Free and Paid Tickets can have Onsite Ticket Visibility!", Toast.LENGTH_SHORT).show();
					//showCustomToast(AddTicketActivity.this,"Only Free Tickets can have Onsite ScanAttendee Visibility!",R.drawable.img_like,R.drawable.toast_redrounded,false);
					scanattendee_onsite_visibility.setChecked(false);
				}
			}
		});
		panel.setOnExpandListener(new ExpandablePanel.OnExpandListener() {

			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				txtTicSetting.setCompoundDrawablesWithIntrinsicBounds(
						0,// left
						0,//top
						R.drawable.minus_white,// right
						0);




			}

			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				txtTicSetting.setCompoundDrawablesWithIntrinsicBounds(
						0,// left
						0,//top
						R.drawable.plus_white,// right
						0);

			}
		});

		check_minimun.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					edit_t_price.setEnabled(true);
				}else{
					edit_t_price.setEnabled(false);
				}
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

		/*String whereClause = " where EventId='" + checked_in_eventId + "' AND "+DBFeilds.ADDED_ITEM_ID+" = '"+_itemhandler.getId()+"'";
		Cursor c_ticket = Util.db.getTicketCursor(whereClause);
		c_ticket.moveToFirst();
		_itemhandler.setTaxable__c(c_ticket.getString(c_ticket.getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));*/
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(
					Util.ADDEVENT)) {
				Intent i = new Intent(AddTicketActivity.this,
						DashboardActivity.class);
				i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
				i.putExtra(Util.ADDEVENT, Util.ADDEVENT);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			} else {
				finish();
				return true;
			}

		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View view;
		boolean ret = false;
		view = getCurrentFocus();
		ret = super.dispatchTouchEvent(event);

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
	public void doRequest() {
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		postMethod = new HttpPostData("Saving Ticket Info...",setPostUrl(), setJsonBodyData().toString(), access_token, AddTicketActivity.this);
		//postMethod = new HttpPostDataObject(setPostUrl(),setJsonBodyData(),null,sfdcddetails.token_type, sfdcddetails.access_token,null, AddTicketActivity.this);
		postMethod.execute();
	}

	private class ItemTypeAdapter extends BaseAdapter{

		@Override
		public int getCount() {

			return itemType.size();
		}

		@Override
		public Object getItem(int position) {

			return itemType.get(position);
		}

		@Override
		public long getItemId(int position) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			v= inflater.inflate(R.layout.spinner_item_black, null);
			TextView name = (TextView) v.findViewById(R.id.spinnerTarget);
			name.setTypeface(Util.roboto_regular);
			name.setTextColor(getResources().getColor(R.color.black));
			name.setText(itemType.get(position).getItemTypeName());

			return v;
		}

	}

	private String setPostUrl(){
		String url="";
		if (status.equals("CREATE")) {
			url = sfdcddetails.instance_url + WebServiceUrls.SA_ADD_TICKET_INFO
					+ getCreateTicketQueryParam();
		} else {
			url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_EDIT_TICKET_INFO + getCreateTicketQueryParam();
		}


		return url;
	}
	@SuppressWarnings("deprecation")
	public String getCreateTicketQueryParam(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		params.add(new BasicNameValuePair("Start_date", ticket_start_date));
		params.add(new BasicNameValuePair("End_date", ticket_end_date));
		params.add(new BasicNameValuePair("ticketopt", ticket_option));
		params.add(new BasicNameValuePair("feeopt", fee_option));
		params.add(new BasicNameValuePair("totalticketcount",String.valueOf(_itemhandler.getItem_count__c())));
		return  AppUtils.getQuery(params);
	}

	private JSONObject setJsonBodyData(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("tname", _itemhandler.getItem_name__c());
			obj.put("ispackage", String.valueOf(false));
			obj.put("tdesc", "");
			obj.put("tvisib", spnr_ticket_visibility.getSelectedItem());
			obj.put("tpaytype",  _itemhandler.getPayment__c());
			obj.put("itemtype", item_type_id);
			obj.put("tprice", _itemhandler.getPrice__c());
			obj.put("tqty", _itemhandler.getItem_count__c());
			obj.put("ticketcount", "1");
			obj.put("ticketlogo", "");
			obj.put("trowid", "2");
			obj.put("taxrateflag", String.valueOf(switch_isTax.isChecked()));
			//if(!(NullChecker(_itemhandler.getPayment__c()).equalsIgnoreCase("Free"))||!(NullChecker(_itemhandler.getPayment__c()).equalsIgnoreCase("Paid"))){
			if(NullChecker(_itemhandler.getPayment__c()).equalsIgnoreCase("Donation")){
				obj.put("scanonsitevisib", "false");
			}else {
				obj.put("scanonsitevisib", String.valueOf(scanattendee_onsite_visibility.isChecked()));
			}
			obj.put("tebudprcflag", String.valueOf(true));
			if(checkBox_Badgable.isChecked()) {
				obj.put("tbadgable", "B - Badge");
			}else {
				obj.put("tbadgable", "N - No Badge");
			}
			obj.put("tbdglabel", edt_badgelabel.getText().toString());
			if (!status.equals("CREATE"))
				obj.put("poolid", _itemhandler.getItem_Pool__c());
			else
				obj.put("poolid", "");

			JSONArray grouparray = new JSONArray();
			JSONObject groupobj = new JSONObject();
			groupobj.put("tprice", _itemhandler.getPrice__c());
			groupobj.put("qty", _itemhandler.getItem_count__c());
			groupobj.put("tname", _itemhandler.getItem_name__c());
			if (!status.equals("CREATE")){
				groupobj.put("minqty", _itemhandler.getMin_per_order__c());
				groupobj.put("maxqty", _itemhandler.getMax_per_order__c());
			}
			else {
				groupobj.put("maxqty", _itemhandler.getItem_count__c());
				groupobj.put("minqty", "1");
			}
			groupobj.put("sdate", ticket_start_date);
			groupobj.put("edate", ticket_end_date);

			if (!status.equals("CREATE"))
				groupobj.put("itemid", _itemhandler.getId());
			else
				groupobj.put("itemid", "");

			grouparray.put(groupobj);
			obj.put("grouptickets", grouparray);
			obj.put("tags", new JSONArray());
			obj.put("subpackagegroup", new JSONArray());
			//// Log.i("---ADD TICKET BODy----",obj.toString());
			String image="";
			if (photo != null) {
				image = Util.NullChecker(Util.db.getimagedata(Util.db.getByteArray(photo)));
			}
			if(NullChecker(image).length()>0)
				obj.put("Image", Util.db.getimagedata(Util.db.getByteArray(photo)));
			else{
				obj.put("Image", "");
			}
			// jsonarray.put(obj);

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return obj;
	}


	@Override
	public void parseJsonResponse(String response) {
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			gson = new Gson();
			TicketResponseHandler response_handler = gson.fromJson(response,TicketResponseHandler.class);
			response_handler.Items.get(0).setImage_url(response_handler.siteurl+response_handler.ImageUrl);
			response_handler.Items.get(0).setItemFee(NullChecker(response_handler.Fee));
			Util.db.upadteItemRecordInDB(response_handler, checked_in_eventId);

			if (NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(Util.ADDEVENT)) {

				int paymentsetting_size = 0;
				Cursor _event_payment_setting = Util.db.getEvent_Payment_Setting(checked_in_eventId);
				paymentsetting_size = _event_payment_setting.getCount();
				_event_payment_setting.close();
				checked_in_eventId = getIntent().getStringExtra(Util.ADDED_EVENT_ID);
				if(Util.db.isPaidTicketExists(checked_in_eventId) && paymentsetting_size==0){
					if(isEventOrganizer()){
						Util.setCustomAlertDialog(AddTicketActivity.this);
						Util.openCustomDialog("Alert", "As EventOrganizer you don't have access to payment settings please contact EventAdmin");
						Util.txt_okey.setText("Ok");
						Util.txt_dismiss.setVisibility(View.GONE);
						Util.txt_okey.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View arg0) {

								Util.alert_dialog.dismiss();
							}
						});
						//Toast.makeText(BaseActivity.this,"As EventOrganizer you don't have access to payment settings please contact EventAdmin.",Toast.LENGTH_LONG).show();
					}else {
						Intent intent_social = new Intent(AddTicketActivity.this, PaymentSetting.class);
						intent_social.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
						intent_social.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

						Intent i = new Intent(AddTicketActivity.this, ManageTicketActivity.class);
						i.putExtra(Util.TICKET, true);
						i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						AlertDialogCustom social_dialog = new AlertDialogCustom(AddTicketActivity.this);
						social_dialog.setParamenters("Alert", "Do you want to set payment settings ?", intent_social,
								i, 2, true);
						social_dialog.setFirstButtonName("NOW");
						social_dialog.setSecondButtonName("NOT NOW");
						social_dialog.show();
					}	}else{
					Intent i = new Intent(AddTicketActivity.this,ManageTicketActivity.class);
					i.putExtra(Util.TICKET,true);
					i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
					finish();
				}


			}else{
				Intent i = new Intent(AddTicketActivity.this,ManageTicketActivity.class);
				i.putExtra(Util.TICKET,true);
				//i.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();
				//finish();
			}

			/*Intent i = new Intent(AddTicketActivity.this,ManageTicketActivity.class);
			i.putExtra("Type", "Ticket");
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);*/



		} catch (Exception e) {
			e.printStackTrace();
			btn_save.setEnabled(true);
			startErrorAnimation(getResources().getString(R.string.connection_error),
					txt_error_msg);

		}
	}

	private void updateStartTime(int hours, int mins) {
		String timeSet = "", minute="";
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

		minute = String.valueOf(mins);
		if(minute.length() == 1)
			minute = "0"+mins;

		// Append in a StringBuilder
		String aTime = new StringBuilder().append(hours).append(':')
				.append(minute).append(" ").append(timeSet).toString();

		t_start_time.setText(aTime);

	}
	private void updateEndTime(int hours, int mins) {

		String timeSet = "", minute="";
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
		minute = String.valueOf(mins);
		if(minute.length() == 1)
			minute = "0"+mins;

		String aTime = new StringBuilder().append(hours).append(':')
				.append(minute).append(" ").append(timeSet).toString();

		t_end_time.setText(aTime);
	}

	private final TimePickerDialog.OnTimeSetListener starttimepickerlistener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
			try {
				//DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:a");
				Date current_date = new Date();
				String current_time=Util.timeFormat.format(current_date);
				//c.setTime(time_format.parse(checkedin_event_record.Events.Start_Time__c));
				c.setTime(Util.timeFormat.parse(current_time));
				shour = c.get(Calendar.HOUR_OF_DAY);
				sminute = c.get(Calendar.MINUTE);
				if ((hourOfDay > shour)|| (hourOfDay == shour && minutes >= sminute)) {

					shour = hourOfDay;
					sminute = minutes;

					updateStartTime(shour, sminute);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};

	private final TimePickerDialog.OnTimeSetListener endtimepickerlistener = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {

			try {

				String end_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
				String end_time = Util.timeFormat.format(Util.db_date_format1.parse(end_date_time));

				c.setTime(Util.timeFormat.parse(end_time));

				ehour = c.get(Calendar.HOUR_OF_DAY);
				eminute = c.get(Calendar.MINUTE);

				Date end_date = Util.db_date_format1.parse(end_date_time);
				if (Util.date_format.parse(t_end_date.getText().toString()).before(Util.date_format.parse(Util.date_format.format(end_date)))) {
					ehour = hourOfDay;
					eminute = minutes;
					updateEndTime(ehour, eminute);
				} else {
					if ((hourOfDay < ehour) || (hourOfDay == ehour && minutes <= eminute)) {
						ehour = hourOfDay;
						eminute = minutes;
						updateEndTime(ehour, eminute);

					}
				}
			} catch (ParseException e) {
				e.printStackTrace();

			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		DatePickerDialog _date;

		switch (id) {
			case STARTDATE_DIALOG_ID:
				// set date picker as current date
				_date = new DatePickerDialog(this, startdatePickerListener,
						smyear, smmonth, smday);

				try {
					//c.setTime(format1.parse(checkedin_event_record.Events.Start_Date__c));
					//DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
					Date current_date =new Date();
					c.setTime(Util.date_format.parse(Util.date_format.format(current_date)));
					_date.getDatePicker().setMinDate(c.getTime().getTime());

					String end_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
					c.setTime(Util.db_date_format1.parse(end_date_time));
					_date.getDatePicker().setMaxDate(c.getTime().getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}

				return _date;

			case ENDDATE_DIALOG_ID:

				// set date picker as current date
				_date = new DatePickerDialog(this, enddatePickerListener, emyear,emmonth, emday);
				try {
					String start_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.Start_Date__c, checkedin_event_record.Events.Time_Zone__c);
					String end_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);

					//c.setTime(Util.db_date_format1.parse(start_date_time));
					//_date.getDatePicker().setMinDate(c.getTime().getTime());
					c.setTime(Util.date_format.parse(t_start_date.getText().toString().trim()));
					_date.getDatePicker().setMinDate(c.getTime().getTime());
					c.setTime(Util.db_date_format1.parse(end_date_time));
					_date.getDatePicker().setMaxDate(c.getTime().getTime());
				} catch (ParseException e) {
					e.printStackTrace();

				}
				return _date;

			case STARTTIME_DIALOG_ID:

				// set date picker as current date
				try {
					c.setTime(Util.timeFormat.parse(Util.timeFormat.format(new Date())));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				shour = c.get(Calendar.HOUR_OF_DAY);
				sminute = c.get(Calendar.MINUTE);
				return new TimePickerDialog(this, starttimepickerlistener, shour,
						sminute, true);




			case ENDTIME_DIALOG_ID:

				// set date picker as current date
				try {
					String end_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
					String end_time = Util.db_server_ticket_format.format(Util.db_date_format1.parse(end_date_time));
					c.setTime(Util.timeFormat.parse(end_time.split(" ")[1]+" "+end_time.split(" ")[2]));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ehour = c.get(Calendar.HOUR_OF_DAY);
				eminute = c.get(Calendar.MINUTE);
				return new TimePickerDialog(this, endtimepickerlistener, ehour,	eminute, true);
		}
		return null;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case ENDDATE_DIALOG_ID:
				try {
					c.setTime(Util.date_format.parse(t_start_date.getText().toString().trim()));
					((DatePickerDialog) dialog).getDatePicker().setMinDate(c.getTime().getTime());

				} catch (Exception e) {
					// TODO: handle exception
				}

		}
	}

	private final DatePickerDialog.OnDateSetListener startdatePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
							  int selectedMonth, int selectedDay) {
			try {
				smyear = selectedYear;
				smmonth = selectedMonth;
				smday = selectedDay;

				if (String.valueOf(smday).length() == 1) {

					ticket_start_date = new StringBuilder().append(smyear)
							.append("-").append(smmonth + 1).append("-")
							.append("0" + smday).append(" ").toString().trim();
					c.setTime(Util.db_server_format.parse(ticket_start_date));

					t_start_date.setText(Util.date_format.format(c.getTime()));
				} else {
					ticket_start_date = new StringBuilder().append(smyear)
							.append("-").append(smmonth + 1).append("-")
							.append(smday).append(" ").toString().trim();

					/*c.setTime(format1.parse(ticket_start_date));
					t_start_date.setText(format.format(c.getTime()));*/

					c.setTime(Util.db_server_format.parse(ticket_start_date));
					t_start_date.setText(Util.date_format.format(c.getTime()));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}


		}
	};
	private final DatePickerDialog.OnDateSetListener enddatePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
							  int selectedMonth, int selectedDay) {

			try {
				emyear = selectedYear;
				emmonth = selectedMonth;
				emday = selectedDay;
				if (String.valueOf(emday).length() == 1) {

					ticket_end_date = new StringBuilder()
							// Month is 0 based, just add 1
							.append(emyear).append("-").append(emmonth + 1)
							.append("-").append("0" + emday).append(" ")
							.toString().trim();

					c.setTime(Util.db_server_format.parse(ticket_end_date));
					t_end_date.setText(Util.date_format.format(c.getTime()));

				} else {

					ticket_end_date = new StringBuilder()
							// Month is 0 based, just add 1
							.append(emyear).append("-").append(emmonth + 1)
							.append("-").append(emday).append(" ").toString()
							.trim();

					c.setTime(Util.db_server_format.parse(ticket_end_date));

					t_end_date.setText(Util.date_format.format(c.getTime()));

				}

			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.VISIBLE);
		button_layout.setVisibility(View.GONE);
		txt_save.setVisibility(View.VISIBLE);
		btn_cancel.setVisibility(View.GONE);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

		txt_save.setOnClickListener(this);
		spnr_ticket_type = (Spinner) linearview
				.findViewById(R.id.spnrtickettype);
		spnr_type = (Spinner) linearview.findViewById(R.id.spnrttype);
		spnr_ticket_visibility=(Spinner) linearview.findViewById(R.id.spnr_visibility);
		edit_t_name = (EditText) linearview.findViewById(R.id.edittname);
		edit_t_price = (EditText) linearview.findViewById(R.id.edittprice);
		edit_t_qty = (EditText) linearview.findViewById(R.id.edittqty);

		check_minimun=(CheckBox) linearview.findViewById(R.id.checkBox_minimum);

		txt_add_photo = (TextView) linearview.findViewById(R.id.txtaddpic);
		txt_price = (TextView) linearview.findViewById(R.id.txtprice);
		img_ticket = (ImageView) linearview.findViewById(R.id.addpic);
		image_layout = (LinearLayout) linearview.findViewById(R.id.imglayout);
		txtTicSetting = (TextView) linearview.findViewById(R.id.txtprofiletitle);
		t_start_date = (TextView) linearview.findViewById(R.id.ticketstartdate);
		t_start_time = (TextView) linearview.findViewById(R.id.ticketstarttime);
		t_end_date = (TextView) linearview.findViewById(R.id.ticketenddate);
		t_end_time = (TextView) linearview.findViewById(R.id.ticketendtime);
		t_start_time.setOnClickListener(this);
		t_start_date.setOnClickListener(this);
		t_end_date.setOnClickListener(this);
		t_end_time.setOnClickListener(this);


		//btn_expand = (Button) linearview.findViewById(R.id.imgexpand);
		includefee = (RadioButton) linearview.findViewById(R.id.includefee);
		excludefee = (RadioButton) linearview.findViewById(R.id.excludefee);
		infofromattendees = (RadioButton) linearview.findViewById(R.id.infofromattendees);
		infofrombuyer = (RadioButton) linearview.findViewById(R.id.infofrombuyer);
		panel = (ExpandablePanel) linearview.findViewById(R.id.ticketsettingpanel);
		switch_isTax=(Switch) linearview.findViewById(R.id.switch_is_tax);
		scanattendee_onsite_visibility=(Switch) linearview.findViewById(R.id.scanattendee_onsite_visibility);
		selfservice_text=(TextView) linearview.findViewById(R.id.selfservice_text);
		checkBox_Badgable=(CheckBox) linearview.findViewById(R.id.checkbox_isbadgable);
		edt_badgelabel=(EditText) linearview.findViewById(R.id.edt_badge_label);
		excludefee.setTypeface(Util.roboto_regular);
		includefee.setTypeface(Util.roboto_regular);
		infofromattendees.setTypeface(Util.roboto_regular);
		infofrombuyer.setTypeface(Util.roboto_regular);

		txt_add_photo.setTypeface(Util.roboto_regular);
		edit_t_name.setTypeface(Util.roboto_regular);
		edit_t_price.setTypeface(Util.roboto_regular);
		edit_t_qty.setTypeface(Util.roboto_regular);

		image_layout.setOnClickListener(this);

		t_start_date.setTypeface(Util.roboto_regular);
		t_start_time.setTypeface(Util.roboto_regular);
		t_end_date.setTypeface(Util.roboto_regular);
		t_end_time.setTypeface(Util.roboto_regular);



		txt_price.setText("Price in " +Html.fromHtml("<font fgcolor=#ffff0000>"+Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+"</font>"));

		adapter_ticketVisibility = new ArrayAdapter<String>(this,R.layout.spinner_item_black, ticket_visiblity) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);
				return v;
			}
		};

		spnr_ticket_visibility.setAdapter(adapter_ticketVisibility);

			/*for (int i = 0; i < ticket_visiblity.length; i++) {
				if (ticket_type[i].equalsIgnoreCase(_itemhandler.getVisibility__c())) {
					//spnr_type.setSelection(i);
					//t_type = ticket_type[i];
				}
			}*/

		ticketType = new ArrayAdapter<String>(this,R.layout.spinner_item_black, ticket_type) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				((TextView) v).setTextColor(getResources().getColor(
						R.color.black));
				((TextView) v).setTypeface(Util.roboto_regular);

				return v;
			}
		};
		spnr_type.setAdapter(ticketType);


		//if(itemType.containsAll(Collection<TicketTypeContoller> ) )

		  /*if(!NullChecker(_itemhandler.getItem_type__c()).isEmpty()){
			  TicketTypeHandler item = Util.db.getItemType(_itemhandler.getItem_type__c());
			//Log.i("-----------------Item Name----------", ":"+itemType.contains(item)+" : "+item.getItemTypeName()+" : "+item.getItemTypeId());
		    if(!itemType.contains(item)){
		    	itemType.add(item);
		    }
		  }*/
		spnr_ticket_type.setAdapter(new ItemTypeAdapter());

		for (int i = 0; i < ticket_type.length; i++) {
			if (ticket_type[i].equalsIgnoreCase(_itemhandler.getPayment__c())) {
				spnr_type.setSelection(i);
				t_type = ticket_type[i];
			}
		}


		for (int i = 0; i < itemType.size(); i++) {
			////Log.i("-----------------Item Name In Array----------", ":"+itemType.contains(item_type_obj)+" : "+itemType.get(i).getItemTypeName()+" : "+itemType.get(i).getItemTypeId());
			if (itemType.get(i).getItemTypeId().equalsIgnoreCase(_itemhandler.getItem_type__c())) {
				spnr_ticket_type.setSelection(i);
				item_type_id = itemType.get(i).getItemTypeId();
			}
		}


		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a",Locale.US);
			Date current_date = new Date();
			String current_time[]=dateFormat.format(current_date).split(" ");
			t_start_date.setText(Util.date_format.format(Util.db_server_format.parse(current_time[0])));
			t_start_time.setText(current_time[1]+" "+current_time[2].toUpperCase());
			String event_end_date = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
			t_end_date.setText(Util.date_format.format(Util.db_date_format1.parse(event_end_date)));
			String event_end_time[] = dateFormat.format(Util.db_date_format1.parse(event_end_date)).split(" ");
			t_end_time.setText(event_end_time[1]+" "+event_end_time[2].toUpperCase());

			if(NullChecker(_itemhandler.getTaxable__c()).equalsIgnoreCase("true")){
				switch_isTax.setChecked(true);
			}else{
				switch_isTax.setChecked(false);
			}

			if (status.equals("EDIT")) {
				String Item_sale_startdate = Util.change_US_ONLY_DateFormat(_itemhandler.getSale_start__c(), checkedin_event_record.Events.Time_Zone__c);
				t_start_date.setText(Util.date_format.format(Util.db_date_format1.parse(Item_sale_startdate)));
				String Item_sale_start_time[] = dateFormat.format(Util.db_date_format1.parse(Item_sale_startdate)).split(" ");
				t_start_time.setText(Item_sale_start_time[1]+" "+Item_sale_start_time[2].toUpperCase());

				String Item_sale_enddate = Util.change_US_ONLY_DateFormat(_itemhandler.getSale_end__c(), checkedin_event_record.Events.Time_Zone__c);
				t_end_date.setText(Util.date_format.format(Util.db_date_format1.parse(Item_sale_enddate)));
				String Item_sale_end_time[] = dateFormat.format(Util.db_date_format1.parse(Item_sale_enddate)).split(" ");
				t_end_time.setText(Item_sale_end_time[1]+" "+Item_sale_end_time[2].toUpperCase());
				edt_badgelabel.setText(_itemhandler.getBadgelabel());
				if(_itemhandler.getBadgable().equalsIgnoreCase("B - Badge")){
					checkBox_Badgable.setChecked(true);
					//edt_badgelabel.setText(_itemhandler.getBadgelabel());
				}else {
					checkBox_Badgable.setChecked(false);
					edt_badgelabel.setEnabled(false);
				}if(NullChecker(_itemhandler.get_SA_Visibility__c()).equalsIgnoreCase("true")){
					scanattendee_onsite_visibility.setChecked(true);
				}else{
					scanattendee_onsite_visibility.setChecked(false);
				}
			}

		} catch (ParseException e) {

			e.printStackTrace();
		}

		if (status.equals("CREATE")) {
			txt_title.setText("Add Ticket");
			checkBox_Badgable.setChecked(true);
			edt_badgelabel.setText("Attendee");
		} else if (status.equals("EDIT")) {
			txt_title.setText("Edit Ticket");
			edit_t_name.setText(_itemhandler.getItem_name__c());
			edit_t_price.setText("" + _itemhandler.getPrice__c());
			edit_t_qty.setText("" + _itemhandler.getItem_count__c());

			if (_itemhandler.getService_fee__c().trim().equalsIgnoreCase("false")) {
				includefee.setChecked(true);
				fee_option = includefee.getText().toString();
			} else if (_itemhandler.getService_fee__c().trim().equalsIgnoreCase("true")) {
				excludefee.setChecked(true);
				fee_option = excludefee.getText().toString();
			}
			// ticket_setting.equalsIgnoreCase(getString(R.string.infofromattendee))

			if (_itemhandler.getTicket_Settings__c().equalsIgnoreCase(getString(R.string.infofromattendee))){
				infofromattendees.setChecked(true);
			}else{
				infofrombuyer.setChecked(true);
			}
			/*if(NullChecker(_itemhandler.get_SA_Visibility__c()).equalsIgnoreCase("true")){
				scanattendee_onsite_visibility.setChecked(true);
			}else{
				scanattendee_onsite_visibility.setChecked(false);
			}*/
			/*
			 * if (ticket_image != null) { photo
			 * =Util.db.getBitmap(ticket_image);
			 *
			 * txt_add_photo.setText("Change Photo");
			 * img_ticket.setImageBitmap(photo);
			 *
			 * }
			 */
			String[] fullurl= img_url.split("&id=");
			String image_url = fullurl[0];
			if (!(NullChecker(image_url)).isEmpty()&&img_url.length()>(image_url+"&id=").length()) {
				Picasso.with(inflater.getContext()).load(img_url).placeholder(R.drawable.default_image)
						.error(R.drawable.default_image).into(img_ticket);
			} else if (photo!=null) {
				Picasso.with(inflater.getContext()).load(img_url).placeholder(R.drawable.default_image)
						.error(R.drawable.default_image).into(img_ticket);
				BitmapDrawable drawable = (BitmapDrawable) img_ticket.getDrawable();
				photo = drawable.getBitmap();
			}else {
				photo = null;
			}

			if (t_type.trim().equalsIgnoreCase("Free")) {
				edit_t_price.setText("0.00");
				edit_t_price.setEnabled(false);
				switch_isTax.setVisibility(View.GONE);
				//selfservice_text.setVisibility(View.GONE);
				//scanattendee_onsite_visibility.setVisibility(View.VISIBLE);

			}/*else if(t_type.trim().equalsIgnoreCase("Donation")) {
				check_minimun.setChecked(true);
				edit_t_price.setEnabled(true);
		    }*/ else {
				edit_t_price.setEnabled(true);
				switch_isTax.setVisibility(View.VISIBLE);
				//selfservice_text.setVisibility(View.GONE);
				//scanattendee_onsite_visibility.setVisibility(View.GONE);

			}


			if(_itemhandler.getAvailable_Tickets__c() != _itemhandler.getItem_count__c()){
				spnr_ticket_type.setEnabled(false);
			}

			for (int i = 0; i < ticket_visiblity.length; i++) {
				if (ticket_visiblity[i].equalsIgnoreCase(_itemhandler.getVisibility__c())) {
					spnr_ticket_visibility.setSelection(i);
					//t_type = ticket_type[i];
				}
			}

		}
	}




	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		try {
			if (v == image_layout) {
				openTakeFromDialg(AddTicketActivity.this);
			}else if (v == t_start_date) {
				try {

					//String start_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.Start_Date__c, checkedin_event_record.Events.Time_Zone__c);
					//c.setTime(Util.db_date_format1.parse(start_date_time));
					Date current_date =new Date();
					c.setTime(Util.date_format.parse(Util.date_format.format(current_date)));
					smyear = c.get(Calendar.YEAR);
					smmonth = c.get(Calendar.MONTH);
					smday = c.get(Calendar.DAY_OF_MONTH);

				} catch (ParseException e) {

					e.printStackTrace();
				}

				showDialog(STARTDATE_DIALOG_ID);
			}else if (v == t_end_date) {

				try {
					String end_date_time = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
					c.setTime(Util.db_date_format1.parse(end_date_time));
					emyear = c.get(Calendar.YEAR);
					emmonth = c.get(Calendar.MONTH);
					emday = c.get(Calendar.DAY_OF_MONTH);
				} catch (ParseException e) {
					e.printStackTrace();

				}
				showDialog(ENDDATE_DIALOG_ID);

			}else if (v == t_start_time) {
				showDialog(STARTTIME_DIALOG_ID);

			}else if (v == t_end_time) {
				showDialog(ENDTIME_DIALOG_ID);

			}else if (v == txt_save) {

				name = edit_t_name.getText().toString().trim();
				price = edit_t_price.getText().toString().trim();
				qty = edit_t_qty.getText().toString().trim();

				if(price.isEmpty())
				{
					price="0";
				}

				if (t_type.equals("Select ticket type")) {

				} else if (name.isEmpty()) {
					edit_t_name.requestFocus();
					edit_t_name.setError("Ticket name is missing");

				} else if (price.trim().isEmpty() && !t_type.equals("Free")) {
					edit_t_price.requestFocus();
					edit_t_price.setError("Ticket price is missing");

				}else if(price.trim().isEmpty() && t_type.equals("Donation") && check_minimun.isShown()){
					edit_t_price.requestFocus();
					edit_t_price.setError("Please enter minimum ticket price");
				}else if (Double.parseDouble(price) == 0
						&& t_type.equals("Paid")) {
					edit_t_price.requestFocus();
					edit_t_price.setError("Ticket price is invalid");

				} else if (qty.isEmpty() || Integer.parseInt(qty) == 0) {
					edit_t_qty.requestFocus();
					edit_t_qty.setError("Ticket quantity is missing");

				} else if (status.equals("Edit")
						&& Integer.parseInt(qty) < _itemhandler.getAvailable_Tickets__c()) {
					edit_t_qty.requestFocus();
					edit_t_qty
							.setError("Enter a valid Ticket Quantity, which should not be less than the quantity that is "
									+ "already sold.");

				} else {

					try {
						ticket_start_date = ticket_date_format.format(Util.db_date_format1.parse(t_start_date.getText().toString()+" "+
								t_start_time.getText().toString()));
						ticket_end_date = ticket_date_format.format(Util.db_date_format1.parse(t_end_date.getText().toString()+" "+
								t_end_time.getText().toString()));
					} catch (ParseException e) {

						e.printStackTrace();
					}

					if(includefee.isChecked())
						fee_option = includefee.getText().toString();
					else if(excludefee.isChecked())
						fee_option = excludefee.getText().toString();

					if(infofromattendees.isChecked())
						ticket_option = infofromattendees.getText().toString();
					else if(infofrombuyer.isChecked())
						ticket_option = infofrombuyer.getText().toString();

					_itemhandler.setItem_name__c(name);
					_itemhandler.setItem_count__c(Integer.parseInt(qty));
					_itemhandler.setSale_start__c(ticket_start_date);
					_itemhandler.setSale_end__c(ticket_end_date);
					_itemhandler.setPayment__c(t_type);

					if (t_type.equals("Free")){
						_itemhandler.setPrice__c(0);
					}
					else {
						_itemhandler.setPrice__c(Double.parseDouble(price));
					}
					if (isOnline())
					{
						btn_save.setEnabled(false);
						doRequest();

					}
					else{
						btn_save.setEnabled(true);
						startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
					}
				}

			}
		} catch (NumberFormatException e) {
			btn_save.setEnabled(true);
			e.printStackTrace();
		} catch (NotFoundException e) {
			btn_save.setEnabled(true);
			e.printStackTrace();
		}
	}


	/*private Uri getOutputMediaFileUri(int type) {

		try {
			return Uri.fromFile(getOutputMediaFile(type));
		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	private File getOutputMediaFile(int type) {
		File mediaStorageDir = null;
		mediaStorageDir = new File(Environment.getExternalStorageDirectory(),
				"ScanAttendee");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("ScanAttendee", "failed to create directory");
				return null;
			}
		}

		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_1.jpg");

		} else {
			return null;
		}

		return mediaFile;
	}
*/
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){

			String path = data.getStringExtra(CropImage.IMAGE_PATH);

			if ((path == null)||(TextUtils.isEmpty(path))) {
				return;
			}
			Bitmap  bitmap = BitmapFactory.decodeFile(path);
			photo = bitmap;
			img_ticket.setImageBitmap(bitmap);

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
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ "IMG_1.jpg");
				mImageCaptureUri = Uri.fromFile(mediaFile);


				if (mImageCaptureUri != null) {
					try {
						startCropImage(AddTicketActivity.this);

					} catch (Exception e) {
						e.printStackTrace();
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
				startCropImage(AddTicketActivity.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == CROP_FROM_CAMERA) {

			Bundle extras = data.getExtras();

			if (extras != null) {
				photo = extras.getParcelable("data");
				img_ticket.setImageBitmap(photo);

			}
		}else if (requestCode == FINISH_RESULT) {
			startActivity(new Intent(AddTicketActivity.this, SplashActivity.class));
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
		}

		return 0;
	}

	private void removeImage(int id) {

		ContentResolver cr = getContentResolver();
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media._ID + "=?",
				new String[] { Long.toString(id) });

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {


	}
}
