//  ScanAttendee Android
//  Created by Ajay on 01-Jul-2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.database.DBFeilds;
import com.globalnest.database.EventPayamentSettings;
import com.globalnest.mvc.BlockTicketResponse;
import com.globalnest.mvc.BuyerInfoHandler;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OrderDetailsHandler;
import com.globalnest.mvc.OrderItemListHandler;
import com.globalnest.mvc.PaymentObject;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TicketHandler;
import com.globalnest.mvc.TicketResponseHandler;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

/**
 * @author saila_000
 *
 */
public class AddAttendeeActivity extends BaseActivity{

	private Spinner spnr_item_name;
	private  EditText edt_firt_name,edt_last_name,edt_email,edt_compnay,edt_mobile,edt_custom_barcode,
			edt_designation,edt_badge_label,edt_notes,edt_seat;
	private EditText self_edt_firt_name,self_edt_last_name,self_edt_email,self_edt_compnay,self_edt_mobile;
	LinearLayout fulllayout,selfcheckinlayout;
	private Button btn_barcode;
	private HashMap<String, String> free_items_ids = new HashMap<String, String>();
	private HashMap<String, String> checkin_free_item_ids=new HashMap<String, String>();
	private ArrayAdapter<String> array_adapter;
	public String requestType = ITransaction.EMPTY_STRING;
	private ArrayList<BlockTicketResponse> block_tic_res = new ArrayList<BlockTicketResponse>();
	//  private String fname = "", lname = "", email = "", company = "",str_designation="",str_tag="",str_phone="",str_badge_label="",str_seat_no="",srt_note="",str_custom_barcode="";
	private ExternalSettings extsettings;
	private BuyerInfoHandler buyerinfo = new BuyerInfoHandler();
	private String order_id= ITransaction.EMPTY_STRING;
	private TotalOrderListHandler totalorderlisthandler;
	private String payment_gateway_name = ITransaction.EMPTY_STRING;
	private TicketHandler _itemhandler = new TicketHandler();
	Bitmap attendee_photo;
	ImageView attendee_img;
	String AttendeeImageString="";
	boolean saveandprint=false;
	String whereClause="";
	Cursor payment_cursor;
	Cursor attendee_cursor;
	FrameLayout print_badge, frame_transparentbadge,frame_barcode;
	ProgressDialog progressDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.addattendee_manually);
		extsettings = new ExternalSettings();
		if(!Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, "").isEmpty()){
			extsettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id + checked_in_eventId, ""), ExternalSettings.class);
		}
		if(extsettings.custom_barcode){
			frame_barcode.setVisibility(View.VISIBLE);
		}else{
			frame_barcode.setVisibility(View.GONE);
		}
		free_items_ids = Util.db.getFreeItemNameAndId(checked_in_eventId);


		if(free_items_ids.isEmpty()&&BaseActivity.isEventAdmin()){
			showAddTicketName();
		}else if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
			checkin_free_item_ids=Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
			List<String> checkin_free_item_name = new ArrayList<String>(checkin_free_item_ids.values());
			array_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, checkin_free_item_name);
			spnr_item_name.setAdapter(array_adapter);
		}
		else{
			List<String> free_item_name = new ArrayList<String>(free_items_ids.values());
			free_item_name.add(0,"--SELECT--");
			array_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, free_item_name);
			spnr_item_name.setAdapter(array_adapter);
		}
		spnr_item_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String item_id = Util.getKey(free_items_ids, spnr_item_name.getSelectedItem().toString());
				if (spnr_item_name.getSelectedItemPosition() == 0 && !Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
					edt_badge_label.setText("");
				}else if (spnr_item_name.getSelectedItemPosition() > 0 && !Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
					if (!item_id.isEmpty()) {
						String itempoolid=Util.db.getItem_Pool_ID(item_id, checked_in_eventId);
						edt_badge_label.setText(Util.db.getItemPoolBadgeLabel(itempoolid, checked_in_eventId));
					}
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});


		txt_save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				saveandprint=false;
				validatingData();
			}
		});
		txtprint_selfcheckin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
					saveandprint = true;
				}
				validatingData();
			}
		});
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		btn_barcode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(AppUtils.isCamPermissionGranted(AddAttendeeActivity.this)) {
					Intent i = new Intent(AddAttendeeActivity.this,BarCodeScanActivity.class);
					i.putExtra(Util.INTENT_KEY_1, AddAttendeeActivity.class.getName());
					startActivityForResult(i, 200);
				}else {
					AppUtils.giveCampermission(AddAttendeeActivity.this);
				}
			}
		});
		order_id=getIntent().getStringExtra(Util.ORDER_ID);
		if(!NullChecker(order_id).isEmpty()) {
			Intent endintent = new Intent(AddAttendeeActivity.this, OrderSucessPrintActivity.class);
			endintent.putExtra(Util.ORDER_ID, order_id);
			endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(endintent);
		}
	}

	protected void onResume(){
		super.onResume();
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		}else{
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;

		if(!isOnline()){
			startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)){
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_BLOCKING_TICKETS + "&sessiontime=" + checkedin_event_record.sessiontime;
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				postMethod = new HttpPostData("Saving Attendee Info.....", _url, getValues().toString(),access_token, AddAttendeeActivity.this);
			}else {
				postMethod = new HttpPostData("Checking Tickets Availability...", _url, getValues().toString(), access_token, AddAttendeeActivity.this);
			}
			postMethod.execute();
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SELL_TICKET)){
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_SELL_TICKET+ "eveid=" + checked_in_eventId + "&userid=" + sfdcddetails.user_id;
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				postMethod = new HttpPostData("Saving Attendee Info.....",url,getSellTicketRequest(), access_token, AddAttendeeActivity.this);
				postMethod.execute();
			}
			else{
				postMethod = new HttpPostData("Processing your order...",url,getSellTicketRequest(), access_token, AddAttendeeActivity.this);
				postMethod.execute();

			}
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PAYMENT_UPDATE)){
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_PAYMENT_UPDATE;
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				//postMethod = new HttpPostData("Saving Attendee Info.....",url,setPaymentJSON().toString(), access_token, AddAttendeeActivity.this);
				postMethod = new HttpPostData("Processing your Order.....",url,setPaymentJSON().toString(), access_token, AddAttendeeActivity.this);

			}else{
				postMethod = new HttpPostData("Processing your payment...",url,setPaymentJSON().toString(), access_token, AddAttendeeActivity.this);

			}
			postMethod.execute();
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_ADD_TICKET_INFO)){
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_ADD_TICKET_INFO+ getCreateTicketQueryParam();
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				postMethod = new HttpPostData("Saving Attendee Info.....",url, setJsonBodyData().toString(), access_token, AddAttendeeActivity.this);

			}else{
				postMethod = new HttpPostData("Saving Ticket Info...",url, setJsonBodyData().toString(), access_token, AddAttendeeActivity.this);

			}
			postMethod.execute();
		}else if (requestType.equals(Util.GET_BADGE_ID)) {
			postMethod = new HttpPostData("Getting Badge Id...", setBadgeIdUrl(), setPrintBadgeBody().toString(), access_token, AddAttendeeActivity.this);
			postMethod.execute();

		}
	}
	private String setBadgeIdUrl() {
		return sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT;
	}
	private JSONArray setPrintBadgeBody() {

		JSONArray badgearray = new JSONArray();
		JSONObject obj = new JSONObject();
		try {
			obj.put("TicketId", payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_ID)));
			// obj.put("BadgeLabel",
			// updated_badge1.getString(updated_badge1.getColumnIndex(DBFeilds.BADGE_BADGE_NAME)));
			obj.put("BadgeLabel", NullChecker(payment_cursor.getString(payment_cursor
					.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
			obj.put("Reason", "");
			obj.put("devicenm",Util.getDeviceNameandAppVersion());
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
				obj.put("screenmode", "self checkin");
			else obj.put("screenmode", "attendee mobile");
			obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
			obj.put("printtime",Util.getCurrentDateTimeInGMT());
			badgearray.put(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return badgearray;
	}
	public String getCreateTicketQueryParam(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		params.add(new BasicNameValuePair("Start_date", _itemhandler.getSale_start__c()));
		params.add(new BasicNameValuePair("End_date", _itemhandler.getSale_end__c()));
		params.add(new BasicNameValuePair("ticketopt", getString(R.string.infofrombuyer)));
		params.add(new BasicNameValuePair("feeopt", getString(R.string.includefee)));
		params.add(new BasicNameValuePair("totalticketcount",String.valueOf(_itemhandler.getItem_count__c())));
		return  AppUtils.getQuery(params);
	}
	private String setJsonBodyData(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("tname", _itemhandler.getItem_name__c());
			obj.put("ispackage", String.valueOf(false));
			obj.put("tdesc", "");
			obj.put("tvisib", "Public");
			obj.put("tpaytype", _itemhandler.getPayment__c());
			String item_type_id = Util.db.getItemType("").get(0).getItemTypeId();
			obj.put("itemtype", item_type_id);
			obj.put("tprice", _itemhandler.getPrice__c());
			obj.put("tqty", _itemhandler.getItem_count__c());
			obj.put("ticketcount", "1");
			obj.put("ticketlogo", "");
			obj.put("trowid", "2");
			obj.put("taxrateflag", String.valueOf(false));
			obj.put("tebudprcflag", String.valueOf(true));
			obj.put("tbadgable", "B - Badge");
			obj.put("poolid", "");
			obj.put("scanonsitevisib", "true");
			/*if (!status.equals("CREATE"))
				obj.put("poolid", _itemhandler.getItem_Pool__c());
			else
				obj.put("poolid", "");*/

			JSONArray grouparray = new JSONArray();
			JSONObject groupobj = new JSONObject();
			groupobj.put("tprice", _itemhandler.getPrice__c());
			groupobj.put("qty", _itemhandler.getItem_count__c());
			groupobj.put("tname", _itemhandler.getItem_name__c());
			groupobj.put("minqty", "1");
			groupobj.put("maxqty", _itemhandler.getItem_count__c());
			groupobj.put("sdate", _itemhandler.getSale_start__c());
			groupobj.put("edate", _itemhandler.getSale_end__c());
			groupobj.put("itemid", "");
			obj.put("Image", "");
			/*if (!status.equals("CREATE"))
				groupobj.put("itemid", _itemhandler.getId());
			else
				groupobj.put("itemid", "");*/

			grouparray.put(groupobj);
			obj.put("grouptickets", grouparray);
			obj.put("tags", new JSONArray());
			obj.put("subpackagegroup", new JSONArray());
			//// Log.i("---ADD TICKET BODy----",obj.toString());

			/*if (photo != null)
				obj.put("Image", Util.db.getimagedata(Util.db.getByteArray(photo)));
			else
				obj.put("Image", "");*/

			// jsonarray.put(obj);

		} catch (JSONException e) {

			e.printStackTrace();
		}
		return obj.toString();
	}

	public void setUpItemHandler(String ticket_name){
		try {
			_itemhandler.setItem_name__c(ticket_name);
			_itemhandler.setPayment__c("Free");
			_itemhandler.setItem_count__c(100);
			_itemhandler.setPrice__c(0);
			Date today_date = new Date();
			String today_date_string = AddTicketActivity.ticket_date_format.format(today_date);
			_itemhandler.setSale_start__c(today_date_string);
			String event_end_date = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);
			String sales_end_date = AddTicketActivity.ticket_date_format.format(Util.db_date_format1.parse(event_end_date));
			_itemhandler.setSale_end__c(sales_end_date);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private String getValues() {
		JSONArray ticketArray = new JSONArray();
		try {
			JSONObject obj = new JSONObject();
			obj.put("ItemId", Util.getKey(free_items_ids, spnr_item_name.getSelectedItem().toString()));
			obj.put("Qty", Integer.valueOf(1));
			ticketArray.put(obj);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ticketArray.toString();
	}

	private String getSellTicketRequest(){

		JSONObject main_obj = new JSONObject();
		try {
			JSONArray order_array = new JSONArray();
			JSONObject buyer_obj = new JSONObject();
			buyer_obj.put("AmountPayable", "0.00");
			buyer_obj.put("AfterDiscountPrice", "0.00");
			buyer_obj.put("Company", buyerinfo.getCompany());
			buyer_obj.put("CurrencyCode", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
			buyer_obj.put("DiscountedPrice", "0");
			buyer_obj.put("EMailId", buyerinfo.getEmail().toLowerCase());
			buyer_obj.put("FirstName", buyerinfo.getFirstName());
			buyer_obj.put("LastName", buyerinfo.getLastName());
			buyer_obj.put("PhoneNumber", buyerinfo.getMobile());
			buyer_obj.put("OrderStatus", "Fully Paid".replaceAll(" ", "%20"));
			buyer_obj.put("OrderTaxes", "0.00");
			buyer_obj.put("OrderTotal", "0.00");
			buyer_obj.put("Note",buyerinfo.getnote());
			buyer_obj.put("PayKey", "");
			buyer_obj.put("PaymentType", "");
			buyer_obj.put("PaygateWayType","");
			buyer_obj.put("ChargeType", "");
			buyer_obj.put("cardno","");
			buyer_obj.put("FeeAmount", "0.00");
			buyer_obj.put("keyname","");
			buyer_obj.put("Designation", buyerinfo.getDesignation());

			String item_id = Util.getKey(free_items_ids, spnr_item_name.getSelectedItem().toString());
			String item_option = Util.db.getItemCollectInfoSettings(item_id, checked_in_eventId);

			JSONArray item_line_array = new JSONArray();
			JSONObject line_item_object = new JSONObject();
			JSONObject item_object = new JSONObject();
			item_object.put("ItemDiscount", "0.00");
			//double item_type_price = Util.db.getItemType_BL_FEE(checked_in_eventId, items_list.get(i).getItemTypeId());
			item_object.put("FeeAmount", "0.00");
			item_object.put("ItemId", item_id);
			item_object.put("ItemName", free_items_ids.get(item_id));
			item_object.put("ItemPoolId", Util.db.getItem_Pool_ID(item_id, checked_in_eventId));
			item_object.put("ItemPrice",String.valueOf(0.00));
			item_object.put("ItemQuantity",String.valueOf(1));
			item_object.put("ItemTotal", String.valueOf(1*0.00));
			line_item_object.put("Company",	buyerinfo.getCompany());
			line_item_object.put("EmailId",	buyerinfo.getEmail().toLowerCase());
			line_item_object.put("FirstName",buyerinfo.getFirstName());
			line_item_object.put("LastName",buyerinfo.getLastName());
			line_item_object.put("AttendeeImage",AttendeeImageString);
			line_item_object.put("TicketId", block_tic_res.get(0).ticketsList.get(0).Name);
			line_item_object.put("Phoneno", buyerinfo.getMobile());
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(Util.db.getItem_Pool_ID(item_id, checked_in_eventId), checked_in_eventId));
			}else {
				line_item_object.put("BadgeLabel", buyerinfo.getBadge_lable());
			}
			line_item_object.put("Tag","");
			line_item_object.put("Seatno",buyerinfo.getSeatno());
			line_item_object.put("Designation", buyerinfo.getDesignation());
			line_item_object.put("Note",buyerinfo.getnote());

			if (item_option.trim().equalsIgnoreCase(getString(R.string.infofrombuyer))) {
				buyer_obj.put("CustomBarcode", buyerinfo.getCustomBarcode());
			}else{
				line_item_object.put("CustomBarcode", buyerinfo.getCustomBarcode());
			}
			item_line_array.put(line_item_object);
			item_object.put("OrderLineItems", item_line_array);
			order_array.put(item_object);
			main_obj.put("BuyerInfo", buyer_obj);
			main_obj.put("Orders", order_array);
			main_obj.put("SessionAbntime", checkedin_event_record.sessiontime);

		} catch (Exception e) {
			// TODO: handle exception
			//Log.i("------------------Sell ticket Json-----------",":"+e.getMessage());
		}
		return main_obj.toString();
	}

	private String setPaymentJSON(){
		JSONObject main_obj = new JSONObject();
		try {
			Cursor card_pay_setting=Util.db.getEvent_Card_PGateway(checked_in_eventId);
			if(card_pay_setting.moveToFirst()){
				//card_pay_setting.moveToFirst();
				payment_gateway_name = card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
			}
			//Log.i("----------payment gate way name-------------",":"+payment_gateway_name);
			main_obj.put("eventId", checked_in_eventId);
			main_obj.put("orderId", order_id);
			main_obj.put("isTransactionSuccess", "Success");
			main_obj.put("errorMessage", "");

			main_obj.put("cardType", "");
			main_obj.put("cardLastDigits", "");
			main_obj.put("RegType", "ScanAttendee");
			main_obj.put("PaymentType", "");
			main_obj.put("Payment_Mode", "Free");

			card_pay_setting.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return main_obj.toString();
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}

			//Log.i("--------------Blocking Ticket Response----------",":"+response);
			if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)){
				java.lang.reflect.Type listType = new TypeToken<ArrayList<BlockTicketResponse>>() {}.getType();
				block_tic_res = new Gson().fromJson(response, listType);
				//Log.i("-------------Int Value--------------",":"+block_tic_res.get(0).tickesAvilable+" : "+Integer.parseInt(block_tic_res.get(0).tickesAvilable));
				if(Integer.parseInt(block_tic_res.get(0).tickesAvilable) == 0){
					if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
						showScannedTicketsAlert("No more available "+spnr_item_name.getSelectedItemPosition()+" tickets.Please contact Event Organizer",false);
					}
					else
					{
						Intent i = new Intent(AddAttendeeActivity.this, ManageTicketActivity.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						AlertDialogCustom dialog=new AlertDialogCustom(AddAttendeeActivity.this);
						dialog.setParamenters("Alert", "No more available tickets, Please increase "+spnr_item_name.getSelectedItem().toString()+" ticket quantity.", i, null, 1, false);
						dialog.show();
					}
				}else{
					callSellTickets();
					/*requestType = WebServiceUrls.SA_SELL_TICKET;
					doRequest();*/
				}
			}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SELL_TICKET)){
				totalorderlisthandler = new Gson().fromJson(response, TotalOrderListHandler.class);
				//Log.i("---ARRAY SIZE---", "Server"+ totalorderlisthandler.TotalLists.size());
				//Log.i("---------------Error MSG-----------",":"+totalorderlisthandler.errorMsg+totalorderlisthandler.TotalLists.get(0).orderInn);
				if(!NullChecker(totalorderlisthandler.errorMsg).isEmpty()){
					/*MediaPlayer mediaPlayer = MediaPlayer.create(AddAttendeeActivity.this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
					playSound(R.raw.somethingwentwrong);
					openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
				}else{
					Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
					//Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
					order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
					requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
					doRequest();

				}
			}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PAYMENT_UPDATE)){
				JSONObject obj  = new JSONObject(response);
				String status = obj.optString("status");
				String orderStatus = NullChecker(obj.optString("orderStatus"));
				if(status.toLowerCase().equalsIgnoreCase("booked")){
					OrderDetailsHandler orderDetailsHandler = new Gson().fromJson(response, OrderDetailsHandler.class);
					totalorderlisthandler.TotalLists.get(0).orderInn.setOrderStatus(orderStatus);
					totalorderlisthandler.TotalLists.get(0).setPaymentInn(orderDetailsHandler.paymentInnmultiple);
					//TODO PAYMENTITEMS
					/*totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber("");
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setId(payment.Id);
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCreditCardType(payment.credit_card_type__c);
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setlast4degits(payment.credit_card_last_4digits__c);
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber(payment.Payment_Ref_Number__c);
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setRegistrationType(payment.Registration_Type__c);
					totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setPaymentMode(payment.Payment_Mode__c);

					if(payment.BLN_Pay_Gateway__c != null){
						totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Id = payment.BLN_Pay_Gateway__c;
						totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Name = payment_gateway_name;
					}*/
					for(OrderItemListHandler ticket : totalorderlisthandler.TotalLists.get(0).ticketsInn){
						ticket.setTicketStatus(status);
					}

					Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
					Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(orderDetailsHandler.ticklist);

					/*for (int i = 0; i < orderDetailsHandler.ticklist.size(); i++){
						String BadgeId = orderDetailsHandler.ticklist.get(i).Badge_ID__c;
						String TicketId = orderDetailsHandler.ticklist.get(i).Id;
						String BadgeParentId = NullChecker(orderDetailsHandler.ticklist.get(i).badgeparentid__c);
						String printstatus = "";//Printed
						Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(BadgeParentId,
								BadgeId, "", TicketId,printstatus);
					}*/
					String attendeeid=totalorderlisthandler.TotalLists.get(0).ticketsInn.get(0).getId();
					attendee_cursor=Util.db.getBadgeableTicketOrderDetails(order_id);

					if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&!(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())) {

						if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
							if(saveandprint) {
								try {
									showCustomToast(AddAttendeeActivity.this,
											"Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!",
											R.drawable.img_like,R.drawable.toast_greenroundededge,true);
									hideSoftKeyboard(AddAttendeeActivity.this);
									//Thread.sleep(10000);
									if(isBadgeSelected()) {
										txtprint_selfcheckin.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_orange));
										txtprint_selfcheckin.setText(" Printing...");
										doSaveAndPrint(attendeeid);
									}else{
										BaseActivity.showSingleButtonDialog("Alert",
												"No Badge Selected, Please contact your Event Organizer!",this);
									}
								} catch (Exception e) {
									e.printStackTrace();
									AppUtils.displayLog("--------------Blocking Ticket Exception----------", ":" + e.getMessage());
								}
							}else{
								AlertDialogCustom dialog = new AlertDialogCustom(
										AddAttendeeActivity.this);
								dialog.setParamenters("Alert",
										"Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!",
										null, null,
										1, true);
								dialog.show();
							}
							/*Intent startentent = new Intent(AddAttendeeActivity.this,SelfcheckinTicketslistActivity.class);
							startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(startentent);*/
						}else {
							Util.setCustomAlertDialog(AddAttendeeActivity.this);
							String alert = "Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!";
							Util.openCustomDialog(alert, "Do you want to Print Badge?");
							if(attendee_cursor!=null&&attendee_cursor.getCount()>=1){
								Util.txt_dismiss.setVisibility(View.VISIBLE);
							}else{
								Util.txt_dismiss.setVisibility(View.GONE);
							}
							Util.txt_okey.setText("OK");
							Util.alert_dialog.setCancelable(false);
							Util.txt_okey.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View arg0) {
									Util.alert_dialog.dismiss();
									if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&!(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())) {
										Intent endintent = new Intent(AddAttendeeActivity.this, AddAttendeeActivity.class);
										endintent.putExtra(Util.ORDER_ID, order_id);
										endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(endintent);
									}else{
										Intent endintent = new Intent(AddAttendeeActivity.this, AddAttendeeActivity.class);
										endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(endintent);

									}
								}
							});
							Util.txt_dismiss.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									Util.alert_dialog.dismiss();
									finish();
									/*Intent endintent = new Intent(AddAttendeeActivity.this, AddAttendeeActivity.class);
									endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(endintent);*/

								}
							});
						}

					}else{
						showCustomToast(AddAttendeeActivity.this,
								"Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!",
								R.drawable.img_like,R.drawable.toast_greenroundededge,true);
						if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
							Intent startentent = new Intent(AddAttendeeActivity.this,SelfCheckinAttendeeList.class);
							startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(startentent);
							finish();
						}else {
							Intent endintent = new Intent(AddAttendeeActivity.this, ManageTicketActivity.class);
							endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(endintent);
						}

					}
					//finish();

				}
				else{
					openCardErrorDialog("Error", "Adding attendee is declined please check you attendee details or discard the order?");
				}
			}
			else if (requestType.equals(Util.GET_BADGE_ID)) {
				if (!response.isEmpty()) {
					JSONArray badge_array = new JSONArray(response);
					for (int i = 0; i < badge_array.length(); i++) {
						JSONObject badge_obj = badge_array.optJSONObject(i);
						if (badge_obj.optString("Error").equalsIgnoreCase(
								"null")) {
							String BadgeLabel = badge_obj
									.optString("BadgeLabel");
							String BadgeId = badge_obj.optString("BadgeId");
							String Reason = badge_obj.optString("Reason");
							String TicketId = badge_obj.optString("TicketId");
							String printstatus = "Printed";
							String BadgeParentId = Util.NullChecker(badge_obj.optString("BadgeParentId"));
							Util.db.insertandupdateAttendeeBadgeId(BadgeLabel,
									BadgeId, Reason, TicketId,printstatus,BadgeParentId);
							//printClicked();
						} else {
							startErrorAnimation(
									getResources().getString(
											R.string.network_error1),
									txt_error_msg);
						}
					}
				} else {
					//Log.i("Attendee Detail", "respons====>" + response);
					startErrorAnimation("Error in network", txt_error_msg);
				}
			}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_ADD_TICKET_INFO)){
				TicketResponseHandler response_handler = new Gson().fromJson(response,TicketResponseHandler.class);
				response_handler.Items.get(0).setImage_url(response_handler.siteurl+response_handler.ImageUrl);
				response_handler.Items.get(0).setItemFee(NullChecker(response_handler.Fee));
				Util.db.upadteItemRecordInDB(response_handler, checked_in_eventId);

				free_items_ids = Util.db.getFreeItemNameAndId(checked_in_eventId);
				List<String> free_item_name = new ArrayList<String>(free_items_ids.values());
				free_item_name.add(0, "--SELECT--");
				array_adapter = new ArrayAdapter<String>(AddAttendeeActivity.this, android.R.layout.simple_spinner_dropdown_item, free_item_name);
				if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
					CheckedTextView checkTextView = (CheckedTextView) inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
					checkTextView.setTextSize(20);
				}
				spnr_item_name.setAdapter(array_adapter);
			}else if (requestType.equalsIgnoreCase("Check in")){
				final OfflineSyncResController offlineResponse = new Gson().fromJson(response,
						OfflineSyncResController.class);
				String dialogtime = ITransaction.EMPTY_STRING;
				List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
				boolean isFreeSession = false;
				if (scanticks.size() > 0) {
					isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
							checked_in_eventId);
				}

				if (offlineResponse.SuccessTickets.size() > 0) {
					boolean status = Boolean.valueOf(offlineResponse.SuccessTickets.get(0).Status);// success.optJSONObject(0).optBoolean("Status");
					String time = offlineResponse.SuccessTickets.get(0).STicketId.scan_time__c;// success.optJSONObject(0).optString("TimeStamp");
					String attendee_Id = offlineResponse.SuccessTickets.get(0).STicketId.Ticket__c;// success.optJSONObject(0).optJSONObject("STicketId").optString("Ticket__c");
					dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time,checkedin_event_record.Events.Time_Zone__c);
					if (isFreeSession) {
						List<TStatus> session_attendee = new ArrayList<TStatus>();
						session_attendee.add(offlineResponse.SuccessTickets.get(0).STicketId);
						Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
					} else {
						Util.db.updateCheckedInStatus(offlineResponse.SuccessTickets.get(0).STicketId,
								checked_in_eventId);
					}
					//updateView();

				} else if (offlineResponse.FailureTickets.size() > 0) {

					boolean status = offlineResponse.FailureTickets.get(0).Status;// failure.optJSONObject(0).optBoolean("Status");
					String time = offlineResponse.FailureTickets.get(0).tStaus.scan_time__c;// failure.optJSONObject(0).optString("TimeStamp");
					String attendee_Id = offlineResponse.FailureTickets.get(0).STicketId;// failure.optJSONObject(0).optString("STicketId");
					dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time,
							checkedin_event_record.Events.Time_Zone__c);

					if (isFreeSession) {
						List<TStatus> session_attendee = new ArrayList<TStatus>();
						session_attendee.add(offlineResponse.FailureTickets.get(0).tStaus);
						Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
					} else {

						Util.db.updateCheckedInStatus(offlineResponse.FailureTickets.get(0).tStaus,
								checked_in_eventId);
					}
					// time = Util.db_date_format1
					// .format(Util.db_server_ticket_format.parse(time));
					String msg = offlineResponse.FailureTickets.get(0).msg;
					//updateView();

						/*AlertDialogCustom dialog = new AlertDialogCustom(AttendeeListActivity.this);
						dialog.setParamenters("Check-in Failed",
								"This Ticket has been already Checked In" + "\nat " + dialogtime, null, null, 1, false);
						dialog.show();*/

					/*Util.setCustomAlertDialog(AttendeeListActivity.this);
					Util.txt_dismiss.setVisibility(View.VISIBLE);
					Util.txt_okey.setText("DENIED");
					if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
						Util.txt_dismiss.setVisibility(View.GONE);
						Util.txt_okey.setText("OK");
					}else if(offlineResponse.FailureTickets.get(0).tStaus.Tstatus_name__c){
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
							//finish();
							try {
								JSONArray array = new JSONArray();
								JSONObject obj = new JSONObject();
								obj.put("TicketId",offlineResponse.FailureTickets.get(0).tStaus.Ticket__c);
								obj.put("device", "ANDROID");
								obj.put("freeSemPoolId", "");
								String tstatus = Util.db.getTStatusBasedOnGroup(offlineResponse.FailureTickets.get(0).tStaus.Ticket__c,
										offlineResponse.FailureTickets.get(0).tStaus.BLN_Session_Item__r.BLN_Item_Pool__c,
										checked_in_eventId);
								if (NullChecker(tstatus).equalsIgnoreCase("true")) {
									obj.put("isCHeckIn", false);
								} else {
									obj.put("isCHeckIn", true);
								}
								obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
								obj.put("sTime", Util.getCurrentDateTimeInGMT());
								array.put(obj);
								requestType = "Check in";
								new doTicketCheckIn((0), array.toString()).execute();
							} catch (Exception e) {
								// TODO: handle exception
							}

						}
					});*/

					if(NullChecker(msg).isEmpty() || NullChecker(msg).equalsIgnoreCase("Already in system")){
						if(offlineResponse.FailureTickets.get(0).tStaus.Tstatus_name__c){
							if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
								showCustomToast(AddAttendeeActivity.this, "Check-in Failed"+
										"\n This Ticket has been already Checked In at "
										+ dialogtime+". Check out is disabled.", R.drawable.img_like,R.drawable.toast_redrounded,false);
/*
								Util.openCustomDialog("Check-in Failed",
										"This Ticket has been already Checked In" + "\nat "
												+ dialogtime+". Check out is disabled.");*/
							}else{
								showCustomToast(AddAttendeeActivity.this, "Check-in Failed"+
										"\n This Ticket has been already Checked In at "
										+ dialogtime+".", R.drawable.img_like,R.drawable.toast_redrounded,false);

								/*Util.openCustomDialog("Check-in Failed",
										"This Ticket has been already Checked In" + "\nat "
												+ dialogtime+". Do you want to Check Out ?");*/
							}

						}else{
							showCustomToast(AddAttendeeActivity.this, "Check-in Failed"+
									"\n This Ticket has been already Checked Out at "
									+ dialogtime+".", R.drawable.img_like,R.drawable.toast_redrounded,false);
							/*Util.openCustomDialog("Check-Out Failed",
									"This Ticket has been already Checked Out" + "\nat "
											+ dialogtime+". Do you want to Check In ?");*/
						}

					}else{
						Util.openCustomDialog("Check-in/out Failed",msg);
					}

				} else {
					startErrorAnimation(offlineResponse.ErrorMsg, txt_error_msg);
				}

			}

		} catch (Exception e) {
			// TODO: handle exception
			AppUtils.displayLog("--------------Blocking Ticket Exception----------",":"+e.getMessage());
			startErrorAnimation(e.getMessage(),txt_error_msg);
		}
	}
	private void callSellTickets() {
		try {
			progressDialog =new ProgressDialog(AddAttendeeActivity.this);
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
				progressDialog.setMessage("Saving Attendee Info.....");
			}else {
				progressDialog.setMessage("Processing your order...");
			}
			progressDialog.setCancelable(false);
			progressDialog.show();
			ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
			Call<TotalOrderListHandler> call = apiService.getSellTicketAttendees(checked_in_eventId,sfdcddetails.user_id,""
					,getSellTicketRequest().toString(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
			call.enqueue(new retrofit2.Callback<TotalOrderListHandler>() {
				@Override
				public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
					Log.e(call+"------success-------", "------response started-------");
					try {
						if (!isValidResponse(response.toString())) {
							dismissProgressDialog();
							openSessionExpireAlert(errorMessage(response.toString()));
						} else if(response.code()==200) {
							totalorderlisthandler = response.body();

							if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
								playSound(R.raw.somethingwentwrong);
								openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
								dismissProgressDialog();
							} else {
								Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
								//Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
								order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
								//order_name = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName();// sending this order name to paytm custid
								dismissProgressDialog();
							/*if (total == 0) {
								//requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
								doRequest();
							}*/
								if(!order_id.isEmpty()) {
									requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
									doRequest();
								}
							}
						}else {
							dismissProgressDialog();
							openSessionExpireAlert(errorMessage(response.errorBody().string()));
						}
					}catch (Exception e){
						dismissProgressDialog();
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(Call<TotalOrderListHandler> call, Throwable t) {
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
	private void dismissProgressDialog() {
		if(progressDialog!=null) {
			if(progressDialog.isShowing())
				progressDialog.dismiss();
		}
	}
	private void doSaveAndPrint(String attendeeid) throws SQLException {

		PrintAndCheckin printT= new PrintAndCheckin();
		PrintDetails printDetails=new PrintDetails();
		printDetails.attendeeId=attendeeid;
		printDetails.checked_in_eventId=checked_in_eventId;
		printDetails.frame_transparentbadge=frame_transparentbadge;
		printDetails.order_id=order_id;
		printDetails.print_badge=print_badge;
		printDetails.sfdcddetails=sfdcddetails;
		printDetails.isselfCheckinbool=Util.getselfcheckinbools(Util.ISSELFCHECKIN);
		requestType = "Check in";
		printT.doSaveAndPrint(this,printDetails);

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		hideSoftKeyboard(AddAttendeeActivity.this);
		if(keyCode == KeyEvent.KEYCODE_BACK){
			try {
				finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.onKeyDown(keyCode, event);
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
		// TODO Auto-generated method stub
		activity = this;

		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);
		txt_title.setText("Add Attendee");
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		txt_save.setVisibility(View.VISIBLE);
		fulllayout =(LinearLayout) linearview.findViewById(R.id.full_layout);
		selfcheckinlayout =(LinearLayout) linearview.findViewById(R.id.selfcheckin_layout);
		attendee_img=(ImageView) linearview.findViewById(R.id.imgaddattendeepic);
		edt_firt_name = (EditText)linearview.findViewById(R.id.edt_first_name);
		self_edt_firt_name = (EditText)linearview.findViewById(R.id.self_edt_first_name);
		edt_last_name = (EditText)linearview.findViewById(R.id.edt_last_name);
		self_edt_last_name = (EditText)linearview.findViewById(R.id.self_edt_last_name);

		edt_email = (EditText)linearview.findViewById(R.id.edt_email);
		self_edt_email = (EditText)linearview.findViewById(R.id.self_edt_email);

		edt_compnay = (EditText)linearview.findViewById(R.id.edt_company);
		self_edt_compnay = (EditText)linearview.findViewById(R.id.self_edt_company);

		edt_mobile = (EditText)linearview.findViewById(R.id.edt_mobile);
		self_edt_mobile = (EditText)linearview.findViewById(R.id.self_edt_mobile);

		edt_designation = (EditText)linearview.findViewById(R.id.edt_designation);
		edt_badge_label = (EditText)linearview.findViewById(R.id.edt_badge_label);
		edt_custom_barcode = (EditText)linearview.findViewById(R.id.edt_custom_barcode);
		edt_notes = (EditText)linearview.findViewById(R.id.edt_notes);
		edt_seat = (EditText)linearview.findViewById(R.id.edt_seat_number);
		spnr_item_name = (Spinner)linearview.findViewById(R.id.spnr_item_name);
		btn_barcode = (Button)linearview.findViewById(R.id.btn_barcode);
		frame_barcode = (FrameLayout)linearview.findViewById(R.id.frame_barcode);
		print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
		frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);
		if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
			setUIformSelfcheckin();
			txt_save.setVisibility(View.GONE);
			txtprint_selfcheckin.setVisibility(View.VISIBLE);
			//txtprint_selfcheckin.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.printer_img), null );
			if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
				txtprint_selfcheckin.setText("Print Badge");
			}else{
				txtprint_selfcheckin.setText(" Save ");
			}
		}
		attendee_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openTakeFromDialg(AddAttendeeActivity.this);
			}
		});
	}

	private void validatingData(){
		try {
			if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
				String fname = self_edt_firt_name.getText().toString().trim();
				String lname = self_edt_last_name.getText().toString().trim();
				String email = self_edt_email.getText().toString().trim();
				String company = self_edt_compnay.getText().toString().trim();
				String str_phone=self_edt_mobile.getText().toString().trim();
				if (fname.isEmpty()) {
					self_edt_firt_name.setError(getResources().getString(R.string.fname_alert));
					self_edt_firt_name.requestFocus();
					//Util.openCustomDialog("Alert","Please enter first name.");
				} else if (lname.isEmpty()) {
					self_edt_last_name.setError(getResources().getString(R.string.lname_alert));
					self_edt_last_name.requestFocus();
					//Util.openCustomDialog("Alert","Please enter last name.");
				} else if (email.isEmpty()) {
					self_edt_email.setError(getResources().getString(R.string.email_alert));
					self_edt_email.requestFocus();
				} else if(!Validation.isEmailAddress(self_edt_email, true, "Please enter valid email")){
					self_edt_email.setError("Please enter valid email");
				}else {
					buyerinfo.setFirstName(fname);
					buyerinfo.setLastName(lname);
					buyerinfo.setEmail(email);
					buyerinfo.setCompany(company);
					buyerinfo.setMobile(str_phone);
					buyerinfo.setDesignation("");
					buyerinfo.setCustomBarcode("");
					buyerinfo.setBadge_lable("");
					buyerinfo.setSeatno("");
					buyerinfo.setnote("");
					requestType = WebServiceUrls.SA_BLOCKING_TICKETS;
					doRequest();
				}
			}else {
				String fname = edt_firt_name.getText().toString().trim();
				String lname = edt_last_name.getText().toString().trim();
				String email = edt_email.getText().toString().trim();
				String company = edt_compnay.getText().toString().trim();

				String str_designation = edt_designation.getText().toString().trim();
				String str_phone = edt_mobile.getText().toString().trim();
				String item_id = Util.getKey(free_items_ids, spnr_item_name.getSelectedItem().toString());
				/*if(!item_id.isEmpty()) {
					edt_badge_label.setText(Util.db.getItemPoolBadgeLabel(item_id, checked_in_eventId));
				}*/
				String str_badge_label = edt_badge_label.getText().toString().trim();
				//str_tag=edt_tag.getText().toString().trim();
				String str_seat_no = edt_seat.getText().toString().trim();
				String str_note = edt_notes.getText().toString().trim();
				String str_custom_barcode = edt_custom_barcode.getText().toString().trim();

				if (spnr_item_name.getSelectedItemPosition() == 0 && !Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
					openSelectItemNameAlert();
				} else if (fname.isEmpty()) {
					edt_firt_name.setError(getResources().getString(R.string.fname_alert));
					edt_firt_name.requestFocus();
					//Util.openCustomDialog("Alert","Please enter first name.");
				} else if (lname.isEmpty()) {
					edt_last_name.setError(getResources().getString(R.string.lname_alert));
					edt_last_name.requestFocus();
					//Util.openCustomDialog("Alert","Please enter last name.");
				} else if (email.isEmpty()) {
					edt_email.setError(getResources().getString(R.string.email_alert));
					edt_email.requestFocus();
				} else if (!Validation.isEmailAddress(edt_email, true, "Please enter valid email")) {
					edt_email.setError("Please enter valid email");
				} else if (frame_barcode.isShown() && str_custom_barcode.isEmpty()) {
					edt_custom_barcode.setError(getResources().getString(R.string.barcode_alert));
					edt_custom_barcode.requestFocus();
				} else {
					buyerinfo.setFirstName(fname);
					buyerinfo.setLastName(lname);
					buyerinfo.setEmail(email);
					buyerinfo.setCompany(company);
					buyerinfo.setMobile(str_phone);
					buyerinfo.setDesignation(str_designation);
					buyerinfo.setCustomBarcode(str_custom_barcode);
					buyerinfo.setBadge_lable(str_badge_label);
					buyerinfo.setSeatno(str_seat_no);
					buyerinfo.setnote(str_note);
					requestType = WebServiceUrls.SA_BLOCKING_TICKETS;
					doRequest();
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void openDuplicateBarcodeAlert(String msg){
		Util.setCustomAlertDialog(AddAttendeeActivity.this);
		Util.alert_dialog.setCancelable(false);
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_dismiss.setText("DISCARD");
		Util.txt_okey.setText("CHANGE");
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				/*Intent i = new Intent();
				setResult(200, i);
				finish();*/
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				/*Intent startentent = new Intent(AddAttendeeActivity.this,ManageTicketActivity.class);
				startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startentent);*/
				finish();
			}
		});
		Util.openCustomDialog("Error",msg+" Do you want to change the barcode or discard the order?");
	}

	private void openSelectItemNameAlert(){
		Util.setCustomAlertDialog(AddAttendeeActivity.this);
		Util.alert_dialog.setCancelable(true);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.alert);
		Util.txt_dismiss.setText("OK");
		Util.txt_okey.setText("OK");
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				/*Intent i = new Intent();
				setResult(200, i);
				finish();*/
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				/*Intent startentent = new Intent(AddAttendeeActivity.this,ManageTicketActivity.class);
				startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startentent);*/
				finish();
			}
		});
		Util.openCustomDialog("Alert","Please select a Ticket to add an Attendee.");
	}

	private void openCardErrorDialog(String alert, String msg){
		Util.setCustomAlertDialog(AddAttendeeActivity.this);
		Util.alert_dialog.setCancelable(false);
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_dismiss.setText("DISCARD");
		Util.txt_okey.setText("OK");
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				finish();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				/*Intent startentent = new Intent(AddAttendeeActivity.this,ManageTicketActivity.class);
				startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startentent);*/
				finish();
			}
		});
		Util.openCustomDialog(alert,msg);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + data.getStringExtra(Util.INTENT_KEY_1));
		if (requestCode == 200) {
			//setCustumViewData();
			if(frame_barcode.isShown()){
				edt_custom_barcode.setText(data.getStringExtra(Util.INTENT_KEY_1));
			}

		}
		if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){

			String path = data.getStringExtra(CropImage.IMAGE_PATH);

			if ((path == null)||(TextUtils.isEmpty(path))) {
				return;
			}
			Bitmap  bitmap = BitmapFactory.decodeFile(path);
			attendee_photo = bitmap;
			attendee_img.setImageBitmap(bitmap);
			AttendeeImageString= Util.db.getimagedata(Util.db.getByteArray(attendee_photo));
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
						startCropImage(AddAttendeeActivity.this);

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
				startCropImage(AddAttendeeActivity.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == CROP_FROM_CAMERA) {

			Bundle extras = data.getExtras();

			if (extras != null) {
				attendee_photo = extras.getParcelable("data");
				attendee_img.setImageBitmap(attendee_photo);

			}
		}else if (requestCode == FINISH_RESULT) {
			startActivity(new Intent(AddAttendeeActivity.this, SplashActivity.class));
			finish();
		}
	}

	public void showAddTicketName(){
		try {
			AlertDialog.Builder ticket_name_dialog = new AlertDialog.Builder(AddAttendeeActivity.this);
			View promptsView = inflater.inflate(R.layout.print_dialog_layout, null);
			ticket_name_dialog.setView(promptsView);
			final EditText edt_ticket_name = (EditText) promptsView.findViewById(R.id.edit_reason);
			edt_ticket_name.setText("Onsite Guest");
			final TextView txt_message = (TextView) promptsView.findViewById(R.id.txt_message);
			txt_message.setVisibility(View.GONE);
			final TextView txt_title_name = (TextView)promptsView.findViewById(R.id.textView1);
			txt_title_name.setText("Add Free Ticket");
			ticket_name_dialog.setCancelable(false);
			ticket_name_dialog.setPositiveButton("Create",	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,	int id) {

					if(NullChecker(edt_ticket_name.getText().toString().trim()).isEmpty()){
						edt_ticket_name.setError("Ticket name should not empty !");
					}else{
						dialog.dismiss();
						setUpItemHandler(edt_ticket_name.getText().toString().trim());
						requestType = WebServiceUrls.SA_ADD_TICKET_INFO;
						doRequest();
					}
				}
			});
			ticket_name_dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			ticket_name_dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void setUIformSelfcheckin(){
		fulllayout.setVisibility(View.GONE);
		selfcheckinlayout.setVisibility(View.VISIBLE);
	}
}
