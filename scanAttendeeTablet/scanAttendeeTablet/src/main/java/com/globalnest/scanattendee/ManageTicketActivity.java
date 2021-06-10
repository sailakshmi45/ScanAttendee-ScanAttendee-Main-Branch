//  ScanAttendee Android
//  Created by Ajay
//  This class contains list of items and package and allowing to sell them. user can have a access to delete them also.
//  Copyright (c) 2014 Globalnest. All rights reserved
package com.globalnest.scanattendee;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.classes.MultiDirectionSlidingDrawer;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BlockTicketListController;
import com.globalnest.mvc.BlockTicketResponse;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.ItemTypeController;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.TicketHandler;
import com.globalnest.mvc.TicketListResponseHandler;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.PullToRefreshListView.OnRefreshListener;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "UseSparseArrays" })
public class ManageTicketActivity extends BaseActivity implements OnClickListener {
	Dialog cartDialog;
	MultiDirectionSlidingDrawer cartSlider;
	TextView txt_ticket, txt_item, nocart, txt_ticekt, txt_item_l, txt_proceed,
			txt_no_ticket;
	ListView itemView;
	// PullToRefreshListView itemView;
	// QuickReturnListView ticketView;
	LinearLayout loadtickets;
	TicketAdapter ticketAdapter;
	int no_of_cart = 0;
	String whereClause = "", manageType = "", status = "", deleteUrl = "",
			delete_itemId = "", delete_poolid = "", requestType = "";
	Cursor c_ticket, c_item;
	Button btn_place_order;
	int[] selected_ticket_qty;
	AlertDialogCustom dialog;
	HashMap<Integer, TicketTypeContoller> selected_ticket_data = new HashMap<Integer, TicketTypeContoller>();
	HashMap<Integer, ItemTypeController> selected_item_data = new HashMap<Integer, ItemTypeController>();
	ArrayList<TicketTypeContoller> _cartTickets = new ArrayList<TicketTypeContoller>();
	ArrayList<ItemTypeController> _cartItems = new ArrayList<ItemTypeController>();
	ArrayList<BlockTicketResponse> block_tic_res;
	ArrayList<BlockTicketListController> ticketname_for_packageitems = new ArrayList<BlockTicketListController>();
	HashMap<String,ArrayList<BlockTicketListController>> block_ticket_map=new HashMap<String, ArrayList<BlockTicketListController>>();
	int pos=0,innerpos=0;
	double servicefee=0,total_serviceFee=0,servicetax=0,total_servicetax=0;
	double main_servicefee=0,main_total_serviceFee=0,main_servicetax=0,main_total_servicetax=0,main_total = 0,main_sub_total;
	AlertDialog.Builder alertDialog;
	String order_id="";
	ExternalSettings ext_settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideSoftKeyboard(ManageTicketActivity.this);
		setCustomContentView(R.layout.manage_tickets_layout);
		manageType = getIntent().getStringExtra("Type");
		order_id=getIntent().getStringExtra(Util.ORDER_ID);
		if(!NullChecker(order_id).isEmpty()) {
			Intent endintent = new Intent(ManageTicketActivity.this, OrderSucessPrintActivity.class);
			endintent.putExtra(Util.ORDER_ID, order_id);
			endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(endintent);
		}
		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDrawerLayout.isDrawerOpen(left_menu_slider))
					mDrawerLayout.closeDrawer(left_menu_slider);
				else
					mDrawerLayout.openDrawer(left_menu_slider);
			}
		});

		img_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txt_title.setVisibility(View.GONE);
				ticket_searchlayout.setVisibility(View.VISIBLE);
				search_ticket.setFocusable(true);
				search_ticket.requestFocus();
				if (search_ticket.requestFocus()) {
					InputMethodManager imm = (InputMethodManager)
							getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(search_ticket, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		});
		img_setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isOnline() && !Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					isRefresh = true;
					doRequest();
				}else if(Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					showServiceRunningAlert(checkedin_event_record.Events.Name);
				}else{
					startErrorAnimation(
							getResources().getString(R.string.network_error),
							txt_error_msg);
				}
			}
		});
		img_cart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Log.i("ManageTicketActivity", "Cart Clicked" + no_of_cart);

				_cartTickets.clear();
				for (int key : selected_ticket_data.keySet()) {
					_cartTickets.add(selected_ticket_data.get(key));
				}
				if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
					ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
				}
				if (ext_settings.allow_promocode) {
					getTotalAmount();
					if(main_total>0){
						Intent cartintent = new Intent(ManageTicketActivity.this,
								TicketCartActivity.class);
						cartintent.putExtra("CART TICKETS", _cartTickets);
						startActivity(cartintent);
					}else {
						if(_cartTickets.size()==1){
							if(isOnline()){
								requestType=WebServiceUrls.SA_BLOCKING_TICKETS;
								doRequest();
							}else{
								AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
								dialog.setParamenters("Warning", "Please check your internet connection.", null, null, 2, true);
								dialog.show();
							}
						}else {
							Intent cartintent = new Intent(ManageTicketActivity.this,
									TicketCartActivity.class);
							cartintent.putExtra("CART TICKETS", _cartTickets);
							startActivity(cartintent);
						}
					}
				}else {
					if(_cartTickets.size()==1){
						if(isOnline()){
							requestType=WebServiceUrls.SA_BLOCKING_TICKETS;
							doRequest();
						}else{
							AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
							dialog.setParamenters("Warning", "Please check your internet connection.", null, null, 2, true);
							dialog.show();
						}
					}else {
						Intent cartintent = new Intent(ManageTicketActivity.this,
								TicketCartActivity.class);
						cartintent.putExtra("CART TICKETS", _cartTickets);
						startActivity(cartintent);
					}
				}
			}

//TODO COMMENT
				/*if(_cartTickets.size()==1){
					if(isOnline()){
						requestType=WebServiceUrls.SA_BLOCKING_TICKETS;
						doRequest();
					}else{
						AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
						dialog.setParamenters("Warning", "Please check your internet connection.", null, null, 2, true);
						dialog.show();
					}
				}else {*/
				/*Intent cartintent = new Intent(ManageTicketActivity.this,
						TicketCartActivity.class);
				cartintent.putExtra("CART TICKETS", _cartTickets);
				startActivity(cartintent);*/
			//}

		});

		/*itemView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				if (isOnline() && !Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					isRefresh = true;
					doRequest();
				}else if(Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					showServiceRunningAlert(checkedin_event_record.Events.Name);
				}else{
					startErrorAnimation(
							getResources().getString(R.string.network_error),
							txt_error_msg);
				}
				itemView.postDelayed(new Runnable() {

					@Override
					public void run() {
						itemView.onRefreshComplete();

					}
				}, 2000);

			}

		});*/
		search_ticket.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				search_ticket.setFocusable(true);
				doSearch();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
		search_ticket.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_ticket.getText().toString().trim().isEmpty()) {

					doSearch();
					//SortFunction(R.id.editsearchrecord, is_buyer);
					return true;
				}else if(search_ticket.getText().toString().trim().isEmpty()){
					Toast.makeText(ManageTicketActivity.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
				}
				return false;
			}
		});
		search_ticket.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_UP){
					if (motionEvent.getX()>(view.getWidth()-50)){
						search_ticket.setText("");
						search_ticket.setFocusable(true);
					}
				}
				return false;
			}
		});
		searchcross.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hideSoftKeyboard(ManageTicketActivity.this);
				txt_title.setVisibility(View.VISIBLE);
				top_layout.setBackgroundResource(R.color.green_top_header);
				ticket_searchlayout.setVisibility(View.GONE);
				search_ticket.setText("");
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.scanattendee.BaseActivity#onResume()
	 */
	/*
	 * @Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (keyCode == KeyEvent.KEYCODE_BACK) { // finish(); return true; } return
	 * false; }
	 */

	@Override
	protected void onResume() {
		super.onResume();
		try {
			hideSoftKeyboard(ManageTicketActivity.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//setListViewData();


		/*
		 * IntentFilter filter = new IntentFilter(
		 * ScannerSettingsApplication.NOTIFY_DATA_ARRIVAL);
		 * registerReceiver(this.newItemsReceiver, filter); filter = new
		 * IntentFilter( ScannerSettingsApplication.NOTIFY_SCANNER_REMOVAL);
		 * registerReceiver(this.newItemsReceiver, filter);
		 */

	}

	@Override
	protected void onStart() {
		super.onStart();
		/*FlurryAgent.onStartSession(ManageTicketActivity.this,
				ScannerSettingsApplication.IMPROVE_APP_ID);*/
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Util.slide_menu_id = 0;
	}

	/*private final BroadcastReceiver newItemsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equalsIgnoreCase(
					ScannerSettingsApplication.NOTIFY_DATA_ARRIVAL)) {
				// _symbologyname=intent.getStringExtra(ScannerSettingsApplication.EXTRA_SYMBOLOGY_NAME);
				char[] data = intent
						.getCharArrayExtra(ScannerSettingsApplication.EXTRA_DECODEDDATA);
				Intent i = new Intent(ManageTicketActivity.this,
						GlobalScanActivity.class);
				i.putExtra(Util.SCANDATA, data);
				startActivity(i);
			} else if (intent.getAction().equalsIgnoreCase(
					ScannerSettingsApplication.NOTIFY_SCANNER_REMOVAL)) {
				// openAlertDialog("Unpaired Successfully with Socket 7xi.","error",
				// OrderDetailsActivity.this);
				Util.openCustomDialog("Alert",
						"Unpaired Successfully with Socket 7xi. ");

				AlertDialogCustom dialog=new AlertDialogCustom(ManageTicketActivity.this);
				dialog.setParamenters("Alert","Unpaired Successfully with Socket 7xi. ", null, null, 1, false);
				dialog.show();
			}

		}
	};*/

	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type + " "
				+ sfdcddetails.access_token;
		if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)){
			String _url = sfdcddetails.instance_url+WebServiceUrls.SA_BLOCKING_TICKETS+"&sessiontime="+checkedin_event_record.sessiontime;
			postMethod = new HttpPostData("Checking Tickets Availability...",_url, setJsonArray().toString(), access_token, ManageTicketActivity.this);
			postMethod.execute();
		}
		else if (requestType.equalsIgnoreCase("TICKET LIST")) {
			String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="
					+ checked_in_eventId;
			postMethod = new HttpPostData("Loading Tickets...", url, null,
					access_token, ManageTicketActivity.this);
			postMethod.execute();
		} else {
			postMethod = new HttpPostData("Deleting Tickets...", deleteUrl,
					null, access_token, ManageTicketActivity.this);
			postMethod.execute();
		}

	}
	private JSONArray setJsonArray() {
		JSONArray ticketArray = new JSONArray();
		try {
			for (int i = 0; i < _cartTickets.size(); i++) {
				JSONObject obj = new JSONObject();
				obj.put("ItemId", _cartTickets.get(i).getTicketsId());
				obj.put("Qty", Integer.valueOf(_cartTickets.get(i).getSelectedTickets()));
				ticketArray.put(obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ticketArray;
	}

	@Override
	public void parseJsonResponse(String response) {
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			if (requestType.equalsIgnoreCase("TICKET LIST")) {
				Gson gson = new Gson();
				if(isRefresh && !Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					Util.db.deleteTable(DBFeilds.TABLE_ADDED_TICKETS);
					Util.db.deleteTable(DBFeilds.TABLE_ITEM_POOL);
					//Util.db.deleteTable(DBFeilds.TABLE_ITEM_REG_SETTINGS);
					isRefresh=false;
				}

				ItemsListResponse items_response = new Gson().fromJson(response, ItemsListResponse.class);
				Util.db.upadteItemListRecordInDB(items_response.Itemscls_infoList,
						checked_in_eventId);
				Util.db.InsertAndUpdateSEMINAR_AGENDA(items_response.agndaInfo);
				//Util.db.InsertUpdateRegsettings(items_response.settingsForScanAttendee,false);//for attendee settings
				//Util.db.InsertandUpadteFeildTypes(items_response.tktprofilefieldtype);//for attendee settings
				List<SessionGroup> group_list = Util.db.getGroupList(BaseActivity.checkedin_event_record.Events.Id);
				//Util.db.UpdateFieldTypes();//for attendee settings
				Util.db.deleteEventScannedTicketsGroup(BaseActivity.checkedin_event_record.Events.Id);
				Util.db.deleteEventScannedTickets(BaseActivity.checkedin_event_record.Events.Id);

				if(!group_list.isEmpty()){
					for(SessionGroup group : group_list){
						for(SessionGroup server_group : items_response.userSessions){
							if(group.Id.equalsIgnoreCase(server_group.Id)){
								if(server_group.BLN_Session_users__r.records.size() > 0){
									server_group.BLN_Session_users__r.records.get(0).DefaultValue__c = group.Scan_Switch;
								}

							}
						}
					}
				}
				Util.db.InsertAndUpdateSESSION_GROUP(items_response.userSessions);

				setListViewData();
			} else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)){

				String zeroAvailableTicketArray ="";
				String lessAvailableTicketArray="";
				//Log.i("--------TicketCartActivity--------", ":"+response);
				try {
					if(!isValidResponse(response)){
						openSessionExpireAlert(errorMessage(response));
					}
					gson = new Gson();
					ticketname_for_packageitems.clear();
					java.lang.reflect.Type listType = new TypeToken<ArrayList<BlockTicketResponse>>() {}.getType();
					block_tic_res = gson.fromJson(response, listType);

					for (int i = 0; i < block_tic_res.size(); i++) {
						int package_qty = 0;
						pos=i;
						ArrayList<BlockTicketListController> ticketname = new ArrayList<BlockTicketListController>();
						Cursor c = Util.db.getTicketCursor(" where "+DBFeilds.ADDED_ITEM_ID+" = '"+block_tic_res.get(i).ItemId+"'");
						c.moveToFirst();
						String item_type = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME));
						String attendee_setting = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_OPTION));
						c.close();
						if(item_type.equalsIgnoreCase("Package")){

							if(attendee_setting.equalsIgnoreCase(getString(R.string.donotinfofromattendee))){
								for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
									if(block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && !TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)){
										ticketname.add(block_tic_res.get(i).ticketsList.get(j));
									}else if(block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)){
										ticketname.add(block_tic_res.get(i).ticketsList.get(j));
									}else{
										package_qty = package_qty + 1;
									}
								}
							}else{
								for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
									if(block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)){
										package_qty = package_qty + 1;
										ticketname.add(block_tic_res.get(i).ticketsList.get(j));
									}else{
										ticketname_for_packageitems.add(block_tic_res.get(i).ticketsList.get(j));
									}
								}
							}

						}else{
							for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
								ticketname.add(block_tic_res.get(i).ticketsList.get(j));
							}
						}
						block_ticket_map.put(block_tic_res.get(i).ItemId, ticketname);

						if(!item_type.equalsIgnoreCase("Package")){
					/*for (int k = 0; k < _cartTickets.size(); k++) {
						if (_cartTickets.get(k).getTicketsId().equals(block_tic_res.get(i).ItemId)){
							_cartTickets.get(k).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
						}
					}*/
							if(Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0){
								zeroAvailableTicketArray = zeroAvailableTicketArray + ", "+ Util.db.getItemName(block_tic_res.get(i).ItemId);
							} else {
								for (int m = 0; m < _cartTickets.size(); m++) {
									if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
										if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
												.parseInt(_cartTickets.get(m).getSelectedTickets())) {
											innerpos=m;
											lessAvailableTicketArray = lessAvailableTicketArray + ", "
													+ Util.db.getItemName(block_tic_res.get(i).ItemId);
										}
									}
								}
							}

						}else{
							if(Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0){
								zeroAvailableTicketArray = Util.db.getItemName(block_tic_res.get(i).ItemId);

							}
							//_cartTickets.get(i).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
							else {
								for (int m = 0; m < _cartTickets.size(); m++) {
									if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
										if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
												.parseInt(_cartTickets.get(m).getSelectedTickets())) {
											innerpos=m;
											lessAvailableTicketArray = lessAvailableTicketArray + ", "
													+ Util.db.getItemName(block_tic_res.get(i).ItemId);
										}
									}
								}
								//	_cartTickets.get(i).setSelectedTickets(String.valueOf(package_qty));
							}
						}
					}

					AlertDialogCustom dialog;
					if(!lessAvailableTicketArray.isEmpty())
					{String msg="";
						Util.setCustomAlertDialog(ManageTicketActivity.this);
						msg = "Your selected quantity of "+_cartTickets.get(innerpos).getSelectedTickets()+" for "+ Util.db.getItemName(block_tic_res.get(pos).ItemId)
								+" ticket is more than available quantity."+"\n"+"Would you like to decrease the Ticket Quantity to "
								+block_tic_res.get(pos).tickesAvilable+" and continue the Order";
						Util.alert_dialog.setCancelable(false);
						Util.openCustomDialog("Alert",
								msg);
						Util.txt_okey.setText("Ok");
						Util.txt_dismiss.setVisibility(View.VISIBLE);
						Util.alert_dialog.setCancelable(false);
						Util.txt_okey.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();
								for (int m = 0; m < _cartTickets.size(); m++) {
									if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
										_cartTickets.get(m).setSelectedTickets(block_tic_res.get(pos).tickesAvilable);
										//t_cart_adapter.notifyDataSetChanged();
										break;
									}
								}
								requestType=WebServiceUrls.SA_BLOCKING_TICKETS;
								doRequest();

							}
						});
						Util.txt_dismiss.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();

							}
						});
				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Your selected quantity of "+ lessAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/
					}else if(!zeroAvailableTicketArray.isEmpty()){
						String msg="";
						Util.setCustomAlertDialog(ManageTicketActivity.this);
						if(_cartTickets.size()==1){
							msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId)+
									" tickets are not available.";

						}else if(_cartTickets.size()>1) {
							msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId)+
									" tickets are not available. Would you like to remove the Ticket and continue the Order";
						}
						Util.alert_dialog.setCancelable(false);
						Util.openCustomDialog("Alert",
								msg);
						Util.txt_okey.setText("Ok");
						Util.txt_dismiss.setVisibility(View.VISIBLE);
						Util.alert_dialog.setCancelable(false);
						Util.txt_okey.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();
								if(_cartTickets.size()==1){
									requestType="TICKET LIST";
									doRequest();
									/*Intent startentent = new Intent(ManageTicketActivity.this,ManageTicketActivity.class);
									startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(startentent);
									finish();*/
								}else if(_cartTickets.size()>1) {
									for (int m = 0; m < _cartTickets.size(); m++) {
										if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
											_cartTickets.remove(m);
											//	t_cart_adapter.notifyDataSetChanged();
											break;
										}
									}
									//if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
									/*_*//*ticketcart.indexOf(block_tic_res.get(pos).ItemId);
							_cartTickets.remove(pos);*/

								}

								//finish();
							}
						});
						Util.txt_dismiss.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View arg0) {
								Util.alert_dialog.dismiss();

							}
						});

				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Sorry! +"+zeroAvailableTicketArray +"tickets are not available.", null, null, 1, false);
				//dialog.setParamenters("Alert", "Your selected quantity of "+zeroAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/



					}else if(!lessAvailableTicketArray.isEmpty() && !zeroAvailableTicketArray.isEmpty()){
						dialog=new AlertDialogCustom(ManageTicketActivity.this);
						dialog.setParamenters("Alert","Your selected quantity of "+lessAvailableTicketArray +" "+zeroAvailableTicketArray+ " is more than available quantity.", null, null, 1, false);
						dialog.show();
					}else{
						getTotalAmount();
						Intent i=new Intent(ManageTicketActivity.this,CollectOrderInfo.class);
						i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						i.putExtra(Util.CART_TICKETS, _cartTickets);
						/*if (Integer.valueOf(_cartTickets.get(0).getSelectedTickets())==1) {
								i.putExtra(Util.ONLY1FORM, true);
							}else if(Integer.valueOf(_cartTickets.get(0).getSelectedTickets())>1)
							{
								i.putExtra(Util.COPY1STFORM,true);
							}*/
						i.putExtra(Util.TOTAL, main_total);
						i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
						i.putExtra(Util.SERVICE_FEE,main_total_serviceFee);
						i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
						i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);

						startActivity(i);
					}


					//ticketinfo.setAdapter(t_cart_adapter);
				} catch (Exception e) {
					// TODO: handle exception
					startErrorAnimation(e.getMessage(), txt_error_msg);
				}


			}else {

				JSONObject res_object = new JSONObject(response);
				final String success = res_object.optString("success");
				if (success.equalsIgnoreCase("success")) {
					Util.db.deleteTicketType(delete_itemId, delete_poolid);
					setListViewData();
				}else if(success.equalsIgnoreCase(String.valueOf(false))){
					openItemDeleteDialog();
				}else {
					startErrorAnimation(success, txt_error_msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			startErrorAnimation(
					getResources().getString(R.string.connection_error),
					txt_error_msg);
		}
	}

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		Util.slide_menu_id = R.id.ticketlayout;
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL
				||(getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
			txt_title.setCompoundDrawables(null,null,null,null);
			txt_title.setTextSize(getResources().getDimension(R.dimen.TextAppearance_8sp));
		}

		txt_title.setText("Manage Tickets");
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.top_more);
		event_layout.setVisibility(View.VISIBLE);
		img_search.setVisibility(View.VISIBLE);
		img_addticket.setVisibility(View.VISIBLE);
		img_setting.setVisibility(View.VISIBLE);
		img_setting.setImageResource(R.drawable.dashboardrefresh);
		if(!isEventAdmin()){
			img_addticket.setVisibility(View.GONE);
		}
		txt_hidenow.setVisibility(View.VISIBLE);
		img_addticket.setOnClickListener(this);
		txt_hidenow.setOnClickListener(this);
		// //Log.i("----Page Index FOund----",":"+padeIndex);
		cartSlider = (MultiDirectionSlidingDrawer) linearview
				.findViewById(R.id.cartslider);
		loadtickets = (LinearLayout) linearview.findViewById(R.id.loadtickets);
		txt_no_ticket = (TextView) linearview.findViewById(R.id.notickets);
		txt_cartno = (TextView) findViewById(R.id.txt_cartitems_no);
		txt_proceed = (TextView) linearview.findViewById(R.id.btnticketcart);
		img_cart = (FrameLayout) findViewById(R.id.imgcatr);
		// itemView = (ListView) linearview.findViewById(R.id.ticketpager);
		itemView = (ListView) linearview
				.findViewById(R.id.ticketpager);
		/*itemView = (PullToRefreshListView) linearview
				.findViewById(R.id.ticketpager);*/
		txt_proceed.setTypeface(Util.roboto_bold);
		txt_proceed.setOnClickListener(this);
		txt_no_ticket.setTypeface(Util.roboto_regular);
		String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND ((ItemDetails.ItemId = HideItems.Item_Poolid) AND (HideItems.Hide_Status!='true')) ";
		whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
		c_ticket = Util.db.getTicketCursor(whereClause);
		AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
		ticketAdapter = new TicketAdapter(ManageTicketActivity.this, c_ticket);
		requestType = "TICKET LIST";
		selected_ticket_qty = new int[c_ticket.getCount()];

		// if(NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(Util.ADDEVENT))

		if (c_ticket.getCount() > 0) {
			//Log.i("checked_in_eventId", "---No tickets in the database");
			AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
			itemView.setAdapter(ticketAdapter);
			itemView.setVisibility(View.VISIBLE);
			txt_no_ticket.setVisibility(View.GONE);
			loadtickets.setVisibility(View.GONE);
			txt_title.setText("Manage Tickets ("+c_ticket.getCount()+")");
		}else {
			c_item = Util.db.getEventItems(checked_in_eventId);
			if (c_item.getCount() > 0) {
				loadtickets.setVisibility(View.GONE);
				txt_no_ticket.setVisibility(View.VISIBLE);
				txt_no_ticket.setText("No Visible Tickets Found! Please UnHide Tickets ");
			} else if (isOnline()) {
				////Log.i("checked_in_eventId", "---Loging from the server");
				loadtickets.setVisibility(View.VISIBLE);
				txt_no_ticket.setVisibility(View.GONE);
				doRequest();
			} else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}

		}

	}

	private void doSearch(){
		String sString= search_ticket.getText().toString().toLowerCase().trim();
		if(sString.contains("'")){
			sString=sString.replace("'","''");
		}
		String where="";
		if (!search_ticket.getText().toString().trim().isEmpty()) {
			where =  " AND ItemName like" +"'%"+sString+"%'";
			String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND ((ItemDetails.ItemId = HideItems.Item_Poolid) AND (HideItems.Hide_Status!='true')) "+where;
			whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
			c_ticket = Util.db.getTicketCursor(whereClause);

			//getFilterOrderCursor(whereClause);
		}else{
			String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND ((ItemDetails.ItemId = HideItems.Item_Poolid) AND (HideItems.Hide_Status!='true')) ";
			whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
			c_ticket = Util.db.getTicketCursor(whereClause);
		}
		if (c_ticket.getCount() > 0) {

			ticketAdapter = new TicketAdapter(ManageTicketActivity.this, c_ticket);
			itemView.setAdapter(ticketAdapter);
			ticketAdapter.changeCursor(c_ticket);
			ticketAdapter.notifyDataSetChanged();
			itemView.setVisibility(View.VISIBLE);
			txt_no_ticket.setVisibility(View.GONE);
			loadtickets.setVisibility(View.GONE);
			txt_title.setText("Manage Tickets ("+c_ticket.getCount()+")");
		} else {
			itemView.setVisibility(View.GONE);
			txt_no_ticket.setVisibility(View.VISIBLE);
			loadtickets.setVisibility(View.GONE);
		}
	}
	public void setListViewData() {
		selected_item_data.clear();
		selected_ticket_data.clear();
		img_cart.setVisibility(View.GONE);
		//whereClause = " where EventId='" + checked_in_eventId + "'";
		//c_ticket = Util.db.getEventItems(checked_in_eventId);
		String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND  ((ItemDetails.ItemId = HideItems.Item_Poolid) AND (HideItems.Hide_Status!='true')) ";
		whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
		c_ticket = Util.db.getTicketCursor(whereClause);

		////Log.i(checked_in_eventId + "---MANAGE TICKET CURSOR SIZE----", ":"+ c_ticket.getCount());
		ticketAdapter = new TicketAdapter(ManageTicketActivity.this, c_ticket);
		requestType = "TICKET LIST";
		selected_ticket_qty = new int[c_ticket.getCount()];
		if (c_ticket.getCount() > 0) {
			itemView.setAdapter(ticketAdapter);
			itemView.setVisibility(View.VISIBLE);
			txt_no_ticket.setVisibility(View.GONE);
			loadtickets.setVisibility(View.GONE);
			txt_title.setText("Manage Tickets ("+c_ticket.getCount()+")");
		} else {
			itemView.setVisibility(View.GONE);
			txt_no_ticket.setVisibility(View.VISIBLE);
			loadtickets.setVisibility(View.GONE);
		}
			/*if (getIntent().getBooleanExtra(Util.TICKET,false)) {
				if (dialog == null) {
					Intent intent_social = new Intent(
							ManageTicketActivity.this, SocialMedia.class);
					intent_social.putExtra("Status", "CREATE");
					intent_social.putExtra("IS_FROM", "AddTicketActivity");
					intent_social.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					intent_social.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Intent i = new Intent(ManageTicketActivity.this,
							ManageTicketActivity.class);
					i.putExtra("Type", "Ticket");
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent_social);
					*//*dialog = new AlertDialogCustom(ManageTicketActivity.this);
					dialog.setParamenters(
							"Alert",
							"Do you want to publish your event on social sites ?",
							intent_social, null, 2, false);

					dialog.show();*//*
				}
			}*/

	}

	@Override
	public void onClick(View v) {
		try {
			if (v == txt_proceed) {

				for (int key : selected_ticket_data.keySet()) {
					_cartTickets.add(selected_ticket_data.get(key));
				}
				for (int key : selected_item_data.keySet()) {
					_cartItems.add(selected_item_data.get(key));
				}
			} else if (v == img_addticket) {
				if(isPastEvent()){
					AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
					dialog.setParamenters("Alert", "Sorry! You cannot add past event tickets.", null, null, 1, false);
					dialog.show();
				}else if(Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
					showServiceRunningAlert(checkedin_event_record.Events.Name);
				}else{
					Intent i = new Intent(ManageTicketActivity.this,
							AddTicketActivity.class);
					i.putExtra("Status", "CREATE");
					startActivity(i);
				}
				/*Intent i = new Intent(ManageTicketActivity.this,
						HideandUnhideTicketsActivity.class);
				startActivity(i);*/
			}else if(v == txt_hidenow){
				Intent i = new Intent(ManageTicketActivity.this,
						HideandUnhideTicketsActivity.class);
				startActivity(i);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private class TicketAdapter extends CursorAdapter {
		// int sel_qty = 0;
		String price = "";

		// private final ImageLoadingListener animateFirstListener = new
		// AnimateFirstDisplayListener();
		private class ViewHolder {
			TextView t_name, t_line_items, t_price, t_availabilty, txt_delete,
					txt_edit, t_type, t_qty,txt_isPackage,txt_avi,txt_closed;
			ImageView t_img;
			Button btn_add, btn_minus;
			//EditText edit_price;
			FrameLayout lay_isPackage,fram_soldout,frm_avai;
			LinearLayout lay_ticketedit,layout_delete,lay_buttons;
			public boolean needInflate;
		}

		@SuppressWarnings("deprecation")
		public TicketAdapter(Context context, Cursor c) {
			super(context, c);
			AppUtils.displayLog("----------------Item Name------------",":"+c.getCount());
		}

		@Override
		public void bindView(View parent_view, Context context, final Cursor cursor) {

			final View v;
			if (parent_view==null) {
				v = newView(context, cursor, null);
			}
			else if (((ViewHolder)parent_view.getTag()).needInflate) {
				v = newView(context, cursor, null);

			}else{
				v = parent_view;
			}


			final ViewHolder viewholder = (ViewHolder) v.getTag();
			if (!cursor.getString(cursor.getColumnIndex("ItemImageUrl"))
					.isEmpty()) {
				Picasso.with(inflater.getContext())
						.load(cursor.getString(cursor
								.getColumnIndex("ItemImageUrl")))
						.placeholder(R.drawable.default_image)
						.error(R.drawable.default_image).into(viewholder.t_img);

			}
			AppUtils.displayLog("----------------Item Name------------",":"+cursor.getString(cursor.getColumnIndex("ItemName")));
			viewholder.t_name.setText(cursor.getString(cursor
					.getColumnIndex("ItemName")));

			int i = cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity"));

			viewholder.t_availabilty.setText(cursor.getString(cursor
					.getColumnIndex(DBFeilds.ADDED_ITEM_SOLDQUANTITY))+"/"+cursor.getString(cursor
					.getColumnIndex("ItemQuantity")));

			if(cursor.getInt(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SOLDQUANTITY))==0)
			{
				viewholder.fram_soldout.setVisibility(View.VISIBLE);
				viewholder.lay_buttons.setVisibility(View.GONE);

				//viewholder.txt_avi.setPaintFlags(viewholder.txt_avi.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}else{
				viewholder.fram_soldout.setVisibility(View.GONE);
				viewholder.lay_buttons.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Paid")) {
				//viewholder.priceLayout.setVisibility(8);
				//viewholder.t_price.setVisibility(0);

				viewholder.t_price
						.setText(Util.db
								.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
								+ " "
								+ String.format( "%.2f",cursor.getDouble(cursor
								.getColumnIndex("ItemPrice"))));

				viewholder.t_type.setTextColor(getResources().getColor(
						R.color.light_blue));
				viewholder.t_type.setText("Paid");

			} else if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Donation")) {

				//viewholder.t_price.setVisibility(8);

				viewholder.t_price
						.setText(Util.db
								.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
								+ " "
								+ String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("ItemPrice"))) );
				viewholder.t_type.setTextColor(getResources().getColor(R.color.light_blue));
				viewholder.t_type.setText("Donation");


			} else {
				//viewholder.priceLayout.setVisibility(8);
				//viewholder.t_price.setVisibility(0);

				/*viewholder.t_price
						.setText(Util.db
								.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
								+ " "
								+ String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("ItemPrice"))) );*/
				viewholder.t_price.setText("");
				viewholder.t_type.setTextColor(getResources().getColor(
						R.color.light_blue));
				viewholder.t_type.setText("Free");

			}

			//Log.i("----------------ItemType--------------",":"+cursor.getString(cursor.getColumnIndex("ItemTypeName")));
			if (cursor.getString(cursor.getColumnIndex("ItemTypeName"))
					.equalsIgnoreCase("Package")) {
				String parant_id = cursor.getString(4);//cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_POOLID));
				//Log.i("----------------Item Pool Id--------------",":"+parant_id);
				if (!parant_id.isEmpty()) {
					Cursor c1 = Util.db.getTicketPoolCursor(" where "
							+ DBFeilds.ITEMPOOL_ADDON_PARENT + " = '"
							+ parant_id + "'");
					String line_items = "";

					line_items="";
					if (c1 != null) {
						c1.moveToFirst();
						for (int j = 0; j < c1.getCount(); j++) {
							line_items = line_items
									+ c1.getString(c1
									.getColumnIndex(DBFeilds.ITEMPOOL_ITEMPOOLNAME))
									+ "-"
									+ c1.getString(c1
									.getColumnIndex(DBFeilds.ITEMPOOL_ADDON_COUNT));
							if (j != (c1.getCount() - 1)) {
								line_items = line_items + ", ";
							}
							c1.moveToNext();
						}
						if (!line_items.isEmpty()) {
							viewholder.t_line_items.setText("(" + line_items + ")");
							viewholder.t_line_items.setVisibility(View.VISIBLE);
							viewholder.txt_isPackage.setVisibility(View.VISIBLE);
							viewholder.lay_isPackage.setVisibility(View.VISIBLE);


						}else{
							viewholder.t_line_items.setVisibility(View.GONE);
							viewholder.lay_isPackage.setVisibility(View.GONE);
						}
						c1.close();
					}
					//viewholder.t_line_items.setText("(" + line_items + ")");
					////Log.i("------------Line Item String----------", ":"+ line_items.toString());
				}

				viewholder.txt_edit.setEnabled(false);
				viewholder.lay_ticketedit.setVisibility(View.GONE);
			}else{
				viewholder.t_line_items.setVisibility(View.GONE);
				viewholder.lay_isPackage.setVisibility(View.GONE);
				viewholder.txt_edit.setEnabled(true);
				viewholder.lay_ticketedit.setVisibility(View.VISIBLE);
			}

			if(Util.db.isItemProduct(checked_in_eventId, cursor.getString(cursor.getColumnIndex("ItemType")))){
				viewholder.txt_edit.setEnabled(false);
				viewholder.lay_ticketedit.setVisibility(View.GONE);
			}
			viewholder.btn_add.setTag(Integer.valueOf(cursor.getPosition()));
			viewholder.btn_minus.setTag(Integer.valueOf(cursor.getPosition()));
			viewholder.txt_edit.setTag(Integer.valueOf(cursor.getPosition()));
			viewholder.txt_delete.setTag(Integer.valueOf(cursor.getPosition()));
			//viewholder.edit_price.setTag(Integer.valueOf(cursor.getPosition()));
			viewholder.t_qty.setError(null);
			//viewholder.btn_add.setVisibility(0);
			if(!isEventAdmin()){
				viewholder.lay_ticketedit.setVisibility(View.GONE);
				viewholder.layout_delete.setVisibility(View.GONE);
			}

			if (selected_ticket_qty[cursor.getPosition()] == 0) {
				viewholder.btn_minus.setVisibility(View.VISIBLE);
				viewholder.btn_minus.setEnabled(false);
				viewholder.t_qty.setText((new StringBuilder()).append(
						selected_ticket_qty[cursor.getPosition()]).toString());
			} else {
				//viewholder.btn_minus.setVisibility(0);
				viewholder.btn_minus.setEnabled(true);
				viewholder.t_qty.setText((new StringBuilder()).append(
						selected_ticket_qty[cursor.getPosition()]).toString());
			}

			if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_STATUS))).equalsIgnoreCase("Closed")){
				viewholder.btn_add.setVisibility(View.GONE);
				viewholder.btn_minus.setVisibility(View.GONE);
				viewholder.txt_closed.setVisibility(View.VISIBLE);
				viewholder.t_qty.setVisibility(View.GONE);
			}else{
				viewholder.btn_add.setVisibility(View.VISIBLE);
				viewholder.btn_minus.setVisibility(View.VISIBLE);
				viewholder.txt_closed.setVisibility(View.GONE);
				viewholder.t_qty.setVisibility(View.VISIBLE);
			}

			viewholder.txt_edit.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View view) {

					if(isPastEvent()){
						AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
						dialog.setParamenters("Alert", "You can not edit past event ticket.", null, null, 1, false);
						dialog.show();
					}else if(Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
						showServiceRunningAlert(checkedin_event_record.Events.Name);
					}else{
						byte[] img = null;
						BitmapDrawable bmdr = (BitmapDrawable) viewholder.t_img.getDrawable();
						if (bmdr.getBitmap() != null)
							img = Util.db.getByteArray(bmdr.getBitmap());
						int i = ((Integer) view.getTag()).intValue();
						Cursor cursor = ticketAdapter.getCursor();
						cursor.moveToPosition(i);
						TicketHandler tickethandler = new TicketHandler();
						tickethandler.setId(cursor.getString(cursor.getColumnIndex("ItemId")));
						tickethandler.setEvent__c(cursor.getString(cursor.getColumnIndex("EventId")));
						tickethandler.setItem_name__c(cursor.getString(cursor.getColumnIndex("ItemName")));
						tickethandler.setItem_Pool__c(cursor.getString(cursor.getColumnIndex("ItemPoolId")));
						tickethandler.setPrice__c(cursor.getDouble(cursor.getColumnIndex("ItemPrice")));
						tickethandler.setItem_count__c(cursor.getInt(cursor.getColumnIndex("ItemQuantity")));
						tickethandler.setMax_per_order__c(cursor.getString(cursor.getColumnIndex("Item_max_per_order__c")));
						tickethandler.setMin_per_order__c(cursor.getString(cursor.getColumnIndex("Item_min_per_order__c")));
						tickethandler.setAvailable_Tickets__c(cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity")));
						tickethandler.setItem_type__c(cursor.getString(cursor.getColumnIndex("ItemType")));
						tickethandler.setSale_start__c(cursor.getString(cursor.getColumnIndex("ItemSalesStartDate")));
						tickethandler.setSale_end__c(cursor.getString(cursor.getColumnIndex("ItemSalesEnddate")));
						tickethandler.setPayment__c(cursor.getString(cursor.getColumnIndex("ItemPaymentType")));
						tickethandler.setTicket_Settings__c(cursor.getString(cursor.getColumnIndex("ItemOption")));
						tickethandler.setVisibility__c(cursor.getString(cursor.getColumnIndex("ItemStatus")));
						tickethandler.setSAOnsiteVisibility__c(cursor.getString(cursor.getColumnIndex("Item_SA_Visibility")));
						tickethandler.setService_fee__c(cursor.getString(cursor.getColumnIndex("ServiceFee")));
						tickethandler.setIsBadgable(cursor.getString(cursor.getColumnIndex("Badgable__c")));
						tickethandler.setBadgeLabel(cursor.getString(cursor.getColumnIndex("Badge_Label__c")));
						tickethandler.setTaxable__c(
								cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));

						Intent intent = new Intent(ManageTicketActivity.this, AddTicketActivity.class);
						intent.putExtra("Status", "EDIT");
						intent.putExtra("Edit", tickethandler);
						intent.putExtra("Logo", cursor.getString(cursor.getColumnIndex("ItemImageUrl")));
						startActivity(intent);
					}

				}
			});
			viewholder.txt_delete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {

					if (Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)) {
						showServiceRunningAlert(checkedin_event_record.Events.Name);
					} else if (!isPastEvent()) {
						int i = ((Integer) view.getTag()).intValue();
						Cursor cursor = ticketAdapter.getCursor();
						cursor.moveToPosition(i);
						delete_itemId = cursor.getString(cursor.getColumnIndex("ItemId"));
						delete_poolid = cursor.getString(cursor.getColumnIndex("ItemPoolId"));
						requestType = "TICKET DELETE ";
						customAskDialog(cursor.getString(cursor.getColumnIndex("ItemName")),
								"Would you like to delete the item?",
								cursor.getString(cursor.getColumnIndex("ItemPoolId")), "Delete");
					}
				}
			});
			viewholder.btn_add.setTag(cursor.getPosition());
			viewholder.btn_add.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {

					int i = ((Integer) view.getTag()).intValue();
					int j = 1 + selected_ticket_qty[i];
					Cursor cursor = ticketAdapter.getCursor();
					cursor.moveToPosition(i);
					//Log.i("----------------DB Item Sales Date-----------",":"+cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SALESSTARTDATE)));
					String item_sales_date = Util.change_US_ONLY_DateFormat(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SALESSTARTDATE)), checkedin_event_record.Events.Time_Zone__c);
					//Log.i("----------------Item Sales Date-----------",":"+item_sales_date);
					if(isPastEvent()){
						AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
						dialog.setParamenters("Alert", "Sorry! You cannot sell past event tickets.", null, null, 1, false);
						dialog.show();
					}else if(Util.isMyServiceRunning(DownloadService.class, ManageTicketActivity.this)){
						showServiceRunningAlert(checkedin_event_record.Events.Name);
					}else if(isFutureTicket(item_sales_date)){
						AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
						dialog.setParamenters("Alert", "Sorry! "+cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_NAME))+" sales are not yet started. Please check sales start date and time.", null, null, 1, false);
						dialog.show();
					}else{
						Cursor _event_payment_setting = Util.db.getEvent_Payment_Setting(checked_in_eventId);
						//Log.i("---------------Is Paid Tickets Exists------------",":"+Util.db.isPaidTicketExists(checked_in_eventId)+_event_payment_setting.getCount());
						if(!cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Free")&&Util.db.isPaidTicketExists(checked_in_eventId) && _event_payment_setting.getCount()==0){
							if(isEventOrganizer()){
								Util.setCustomAlertDialog(ManageTicketActivity.this);
								Util.openCustomDialog("Alert", "Please select at least one payment type to sell tickets, \n As EventOrganizer you don't have access to payment settings please contact EventAdmin");
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
								AlertDialogCustom dialog = new AlertDialogCustom(ManageTicketActivity.this);
								Intent intent = new Intent(ManageTicketActivity.this, PaymentSetting.class);
								intent.putExtra(Util.EDIT_EVENT_ID, checked_in_eventId);
								// i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
								dialog.setParamenters("Alert", "Please select at least one payment type to sell Paid tickets ?", intent, null, 2, false);
								dialog.setFirstButtonName("NOW");
								dialog.setSecondButtonName("NOT NOW");
								dialog.show();
							}
						}else{
							// selected_ticket_qty[i]++;
							int available_items = cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity"))
									- selected_ticket_qty[i];

							if (available_items > 0) {
								//viewholder.btn_add.setVisibility(0);
								no_of_cart++;
								selected_ticket_qty[i] = j;

								TicketTypeContoller tickettypecontoller = new TicketTypeContoller();
								if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equals("Donation")) {
									if (!price.isEmpty()) {
										tickettypecontoller.setTicketPrice(Double.parseDouble(price));
									} else {
										tickettypecontoller
												.setTicketPrice(cursor.getDouble(cursor.getColumnIndex("ItemPrice")));
									}
								} else {
									tickettypecontoller
											.setTicketPrice(cursor.getDouble(cursor.getColumnIndex("ItemPrice")));
								}
								tickettypecontoller.setTicketId(cursor.getString(cursor.getColumnIndex("ItemId")));
								tickettypecontoller.setTicketPoolId(cursor.getString(4));
								tickettypecontoller
										.setTicketTypeName(cursor.getString(cursor.getColumnIndex("ItemName")));
								tickettypecontoller.setSelectedTickets(String.valueOf(selected_ticket_qty[i]));
								tickettypecontoller
										.setTicketQuantity(cursor.getInt(cursor.getColumnIndex("ItemQuantity")));
								tickettypecontoller
										.setTicketsEventId(cursor.getString(cursor.getColumnIndex("EventId")));
								tickettypecontoller
										.setTicketFeeSetting(cursor.getString(cursor.getColumnIndex("ServiceFee")));
								tickettypecontoller.setIsTicketTaxable(
										cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));
								tickettypecontoller.setItemFeeAmount(
										cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_FEE)));
								tickettypecontoller.setBLFeeAmount(
										cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_BL_FEE)));
								tickettypecontoller.setTicketType(
										cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)));
								tickettypecontoller.setTicketTypeId(
										cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)));
								tickettypecontoller.setTicketPaymentType(
										cursor.getString(cursor.getColumnIndex("ItemPaymentType")));


								selected_ticket_data.put(Integer.valueOf(i), tickettypecontoller);
								//Log.i("---First time----",(new StringBuilder(":")).append(selected_ticket_data.size()).toString());

								ticketAdapter.notifyDataSetChanged();
								if (selected_ticket_data.size() > 0 || selected_item_data.size() > 0) {
									img_cart.setVisibility(View.VISIBLE);
									//no_of_cart = selected_ticket_data.size();
									txt_cartno.setText(String.valueOf(no_of_cart));
									// txt_proceed.setVisibility(0);
									return;
								} else {
									// txt_proceed.setVisibility(8);
									img_cart.setVisibility(View.GONE);
									return;
								}
							} else {
								// int _tmp =
								// cursor.getInt(cursor.getColumnIndex("ItemQuantity"))
								// -
								// cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity"));
								//viewholder.btn_add.setVisibility(0);
								viewholder.t_qty.requestFocus();
								viewholder.t_qty.setError("Sorry! your selected ticket quantity is not available");
								/*
								 * viewholder.t_qty.setOnFocusChangeListener(new
								 * View.OnFocusChangeListener() {
								 *
								 * @Override public void onFocusChange(View v,
								 * boolean hasFocus) { if (!hasFocus) { //
								 * hideKeyboard(v);
								 * viewholder.t_qty.setError(null); } } });
								 */
								return;
							}

						}
					}

				}
			});
			// viewholder.btn_minus.setTag(cursor.getPosition());
			viewholder.btn_minus.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					 no_of_cart--;
					int i = ((Integer) view.getTag()).intValue();
					int j = -1 + selected_ticket_qty[i];
					Cursor cursor = ticketAdapter.getCursor();
					cursor.moveToPosition(i);
					selected_ticket_qty[i] = j;
					if (selected_ticket_qty[i] == 0) {
						selected_ticket_data.remove(Integer.valueOf(i));
					} else {
						TicketTypeContoller tickettypecontoller = new TicketTypeContoller();
						tickettypecontoller.setTicketId(cursor.getString(cursor
								.getColumnIndex("ItemId")));
						tickettypecontoller.setTicketTypeName(cursor
								.getString(cursor.getColumnIndex("ItemName")));
						tickettypecontoller.setTicketPoolId(cursor.getString(4));
						tickettypecontoller.setSelectedTickets(String
								.valueOf(selected_ticket_qty[i]));
						tickettypecontoller.setTicketPrice(cursor
								.getDouble(cursor.getColumnIndex("ItemPrice")));
						// tickettypecontoller.setTicketQuantity(cursor.getInt(cursor.getColumnIndex("TicketQuantity")));
						tickettypecontoller.setTicketsEventId(cursor
								.getString(cursor.getColumnIndex("EventId")));
						tickettypecontoller.setTicketFeeSetting(cursor
								.getString(cursor.getColumnIndex("ServiceFee")));
						tickettypecontoller.setIsTicketTaxable(cursor.getString(cursor
								.getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));
						tickettypecontoller.setTicketType(cursor.getString(cursor
								.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)));
						tickettypecontoller.setBLFeeAmount(cursor.getString(cursor
								.getColumnIndex(DBFeilds.ADDED_ITEM_BL_FEE)));
						tickettypecontoller
								.setTicketTypeId(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)));
						tickettypecontoller
								.setTicketPaymentType(cursor.getString(cursor.getColumnIndex("ItemPaymentType")));
						selected_ticket_data.put(Integer.valueOf(i),
								tickettypecontoller);
					}
					if (selected_ticket_data.size() > 0
							|| selected_item_data.size() > 0) {
						img_cart.setVisibility(View.VISIBLE);
						//no_of_cart = selected_ticket_data.size();
						txt_cartno.setText(String.valueOf(no_of_cart));
						// txt_proceed.setVisibility(0);
					} else {
						// txt_proceed.setVisibility(8);
						img_cart.setVisibility(View.GONE);
					}
					ticketAdapter.notifyDataSetChanged();
				}
			});

		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
			View v = inflater.inflate(R.layout.ticket_list_layout, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.t_name = (TextView) v.findViewById(R.id.txtticketname);
			holder.t_line_items = (TextView) v
					.findViewById(R.id.packege_line_items);
			holder.t_price = (TextView) v.findViewById(R.id.txtticketprice);
			holder.t_availabilty = (TextView) v
					.findViewById(R.id.txtticketavail);

			holder.txt_avi=(TextView) v.findViewById(R.id.txt_avi);
			holder.txt_delete = (TextView) v.findViewById(R.id.txtticketremove);
			holder.txt_edit = (TextView) v.findViewById(R.id.txtticketedit);
			holder.lay_ticketedit=(LinearLayout)v.findViewById(R.id.lay_ticketedit);
			holder.lay_buttons=(LinearLayout)v.findViewById(R.id.lay_buttons);
			holder.layout_delete = (LinearLayout)v.findViewById(R.id.layout_delete);
			holder.t_type = (TextView) v.findViewById(R.id.ticket_type);
			holder.t_qty = (TextView) v.findViewById(R.id.txteqty);

			holder.txt_isPackage=(TextView) v.findViewById(R.id.txt_isPackage);
			holder.lay_isPackage=(FrameLayout)v.findViewById(R.id.lay_isPackage);
			holder.fram_soldout=(FrameLayout)v.findViewById(R.id.fram_soldout);
			holder.frm_avai=(FrameLayout)v.findViewById(R.id.frm_avai);
			holder.btn_add = (Button) v.findViewById(R.id.btnaddqty);
			holder.btn_minus = (Button) v.findViewById(R.id.btnminusqty);
			holder.t_img = (ImageView) v.findViewById(R.id.ticketimage);
			holder.txt_closed = (TextView)v.findViewById(R.id.txt_closed);


			holder.t_price.setTypeface(Util.droid_boldItalic);
			holder.t_name.setTypeface(Util.droid_bold);
			holder.t_type.setTypeface(Util.OpenSans);
			holder.t_availabilty.setTypeface(Util.droid_boldItalic);
			holder.btn_minus.setTypeface(Util.droid_bold);
			holder.btn_add.setTypeface(Util.droid_bold);
			holder.txt_isPackage.setTypeface(Util.droid_boldItalic);
			holder.txt_closed.setTypeface(Util.droid_boldItalic);
			v.setTag(holder);

			return v;
		}
	}

	public void customAskDialog(String title, String message,
								final String serverId, final String dialogType) {

		String buttonCancel = "", buttonDelete = "";
		alertDialog = new AlertDialog.Builder(this);
		//alertDialog.setIcon(android.R.drawable.ic_dialog_info);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setInverseBackgroundForced(true);
		if (dialogType.equals("Cart")) {
			buttonCancel = "No";
			buttonDelete = "Yes";
		} else {
			buttonCancel = "Cancel";
			buttonDelete = "Delete";
		}
		alertDialog.setNegativeButton(buttonCancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alertDialog = null;
					}
				});
		alertDialog.setPositiveButton(buttonDelete,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						alertDialog = null;
						if (dialogType.equals("Cart")) {
							cartDialog.dismiss();
							selected_ticket_data.clear();
							selected_item_data.clear();
							Intent i = new Intent(ManageTicketActivity.this,
									ManageTicketActivity.class);
							i.putExtra("Type", manageType);
							startActivity(i);
							finish();
						} else if (dialogType.equals("REMOVE TICKET")) {
							_cartTickets.remove(Integer.parseInt(serverId));
							if (_cartTickets.size() > 0
									&& _cartItems.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_ticekt.setVisibility(View.VISIBLE);
								txt_item_l.setVisibility(View.VISIBLE);
								txt_ticekt.setText("Ticket - "
										+ _cartTickets.size());
								txt_item_l.setText("Item - "
										+ _cartItems.size());
							} else if (_cartTickets.size() == 0
									&& _cartItems.size() == 0) {
								nocart.setVisibility(View.VISIBLE);
								btn_place_order.setVisibility(View.GONE);
								txt_ticekt.setVisibility(View.GONE);
								txt_item_l.setVisibility(View.GONE);
							} else if (_cartTickets.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_item_l.setVisibility(View.GONE);
								txt_ticekt.setText("Ticket - "
										+ _cartTickets.size());
							} else if (_cartItems.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_ticekt.setVisibility(View.GONE);
								txt_item_l.setText("Item - "
										+ _cartItems.size());
							}
							// t_adapter.notifyDataSetChanged();
						} else if (dialogType.equals("REMOVE ITEM")) {
							_cartItems.remove(Integer.parseInt(serverId));
							if (_cartTickets.size() > 0
									&& _cartItems.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_ticekt.setVisibility(View.VISIBLE);
								txt_item_l.setVisibility(View.VISIBLE);
								txt_ticekt.setText("Ticket - "
										+ _cartTickets.size());
								txt_item_l.setText("Item - "
										+ _cartItems.size());
							} else if (_cartTickets.size() == 0
									&& _cartItems.size() == 0) {
								nocart.setVisibility(View.VISIBLE);
								btn_place_order.setVisibility(View.GONE);
								txt_ticekt.setVisibility(View.GONE);
								txt_item_l.setVisibility(View.GONE);
							} else if (_cartTickets.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_item_l.setVisibility(View.GONE);
								txt_ticekt.setText("Ticket - "
										+ _cartTickets.size());
							} else if (_cartItems.size() > 0) {
								nocart.setVisibility(View.GONE);
								btn_place_order.setVisibility(View.VISIBLE);
								txt_ticekt.setVisibility(View.GONE);
								txt_item_l.setText("Item - "
										+ _cartItems.size());
							}
						} else {
							if (isOnline()) {
								deleteUrl = sfdcddetails.instance_url
										+ WebServiceUrls.SA_DELETE_TICKET_INFO
										+ "poolid=" + serverId;
								doRequest();
							} else
								startErrorAnimation(getResources().getString(R.string.network_error),
										txt_error_msg);
						}
					}
				});
		Dialog d = alertDialog.show();
		int textViewId = d.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(getResources().getColor(R.color.blue_text_color));
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(getResources().getColor(
				R.color.blue_text_color));

	}

	private void openItemDeleteDialog(){
		Util.setCustomAlertDialog(ManageTicketActivity.this);
		Util.txt_dismiss.setVisibility(View.GONE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_okey.setText("OK");
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();

			}
		});

		Util.openCustomDialog("Alert","Sold ticket/package cannot be deleted.");
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("---------------ManageTicketActivity onActivity Result------------", ":" + requestCode + " : " + resultCode);
		hideSoftKeyboard(ManageTicketActivity.this);
		if(requestCode==Util.DASHBORD_ONACTIVITY_REQ_CODE){
			Intent i=new Intent(ManageTicketActivity.this,SplashActivity.class);
			startActivity(i);
			finish();

		}/*else if(requestCode==445&&!order_id.isEmpty()){
			Intent endintent = new Intent(ManageTicketActivity.this, OrderSucessPrintActivity.class);
			endintent.putExtra(Util.ORDER_ID, order_id);
			endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(endintent);
		}*/
	}
	private void getTotalAmount()
	{
		main_total=0;main_servicefee=0;main_total_serviceFee=0;main_servicetax=0;main_total_servicetax=0;
		EventObjects event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
		for (int i = 0; i < _cartTickets.size(); i++) {
			main_servicefee=0;main_servicetax=0;main_sub_total=0;
			if(Integer.parseInt(_cartTickets.get(i).getSelectedTickets())!=0)
			{
				if (NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")&&Util.NullChecker(_cartTickets.get(i).getIsTicketTaxable()).equalsIgnoreCase("true") && (Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid") ||
						Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
					if(Util.NullChecker(_cartTickets.get(i).getTicketFeeSetting()).equalsIgnoreCase("true")){
						main_servicefee = Util.db.getItemFee(checked_in_eventId, _cartTickets.get(i).getTicketsId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
					}
					if(!event_data.Events.Tax_Rate__c.trim().isEmpty()){
						main_servicetax= Double.valueOf(event_data.Events.Tax_Rate__c);
						main_servicetax=(main_servicetax/100)*(Integer.parseInt(_cartTickets.get(i).getSelectedTickets())*_cartTickets.get(i).getTicketPrice()+main_servicefee);
						//main_servicetax=(main_servicetax/100)*(_cartTickets.get(i).getTicketPrice());
					}
					main_total_servicetax=main_total_servicetax+main_servicetax;
				} else {
					main_total_servicetax=main_total_servicetax+0.00;
				}

				if (Util.NullChecker(_cartTickets.get(i).getTicketFeeSetting()).equalsIgnoreCase("true") && (Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid")
						|| Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
					//main_servicefee = Double.parseDouble("1.50");
					//main_servicefee = Double.parseDouble(_cartTickets.get(i).getBLFeeAmount()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());

					/*if(_cartTickets.get(i).getTicketType().equalsIgnoreCase("Package")){
						double package_fee = Util.db.getItemType_BL_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId());
						if(package_fee == 0){
							main_servicefee = getItemPoolFeeCount(_cartTickets.get(i).getTicketPoolId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
						}else{
							main_servicefee = Util.db.getItemType_BL_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
						}
					}else{*/
					main_servicefee = Util.db.getItemFee(checked_in_eventId, _cartTickets.get(i).getTicketsId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
					//main_servicefee = Util.db.getItemType_BL_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());

					//}
					double max_fee = Util.db.getItemType_BL_MAX_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId());

					if( max_fee != 0 && main_servicefee > max_fee){
						main_servicefee = max_fee;
					}
					main_total_serviceFee=main_total_serviceFee+main_servicefee;

				} else {
					main_total_serviceFee=main_total_serviceFee+0.00;
				}
				main_sub_total =(_cartTickets.get(i).getTicketPrice() * Integer.parseInt(_cartTickets.get(i).getSelectedTickets()));
				main_total=main_total+main_sub_total;
			}else{
				main_total=main_total+0.00;
			}
		}
		//btnbuynow_info.setText("Price Total="+String.format( "%.2f",main_total)+" Fee:"+String.format("%.2f",main_total_serviceFee)+" Tax:"+String.format( "%.2f", main_total_servicetax ));
	}
/*
	public double getItemPoolFeeCount(String item_pool_id){

		double fee_total=0.0;

		Cursor pool_cursor = Util.db.getTicketPoolCursor(" where "+DBFeilds.ITEMPOOL_ADDON_PARENT+"='"+item_pool_id+"'");
		if(pool_cursor == null){
			return 0;
		}else if(pool_cursor.getCount() == 0){
			return 0;
		}else{
			pool_cursor.moveToFirst();
			for(int i=0; i<pool_cursor.getCount();i++){
				String item_type_id = pool_cursor.getString(pool_cursor.getColumnIndex(DBFeilds.ITEMPOOL_ITEMTYPE_ID));
				double item_type_price = Util.db.getItemType_BL_FEE(checked_in_eventId, item_type_id);
				fee_total = fee_total + (item_type_price * Integer.parseInt(pool_cursor.getString(pool_cursor.getColumnIndex(DBFeilds.ITEMPOOL_ADDON_COUNT))));
				pool_cursor.moveToNext();
			}
			pool_cursor.close();
		}

		return fee_total;
	}
*/

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent startentent = new Intent(ManageTicketActivity.this,DashboardActivity.class);
			startentent.putExtra("CheckIn Event", checkedin_event_record);
			startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startentent.putExtra(Util.HOME, "home_layout");
			startActivity(startentent);
			finish();
			/*Intent result = new Intent();
			setResult(1987, result);
			finish();*/
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
