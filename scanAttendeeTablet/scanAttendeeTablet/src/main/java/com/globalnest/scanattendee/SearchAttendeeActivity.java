package com.globalnest.scanattendee;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BlockTicketListController;
import com.globalnest.mvc.BuyerInfoHandler;
import com.globalnest.mvc.ItemTypeController;
import com.globalnest.mvc.OrderItemListHandler;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class SearchAttendeeActivity extends BaseActivity {

	private ListView searchAttendee;
	private TextView txt_nodata,txtLoadingmore;
	private Cursor att_cursor;
	private String whereClause_name,ticket_setting="";
	private AttendeeAdapter adapter;
	private View loadMoreView;
	private LinearLayout btn_order_load_more;
	private boolean isLoading=false;
	//getIntentVariables for CollectOrderInfo class
	private double total=0.0,servicefee=0,servicetax=0,DiscountedValue=0.0,afterdisounttotalwithtax=0.0;
	private String promocode="";
	private int current_item_index=0,form_count=0;
	private HashMap<String, ArrayList<BlockTicketListController>> block_ticket_map=new HashMap<String, ArrayList<BlockTicketListController>>();
	private ArrayList<BlockTicketListController> ticketname_for_packageitems=new ArrayList<BlockTicketListController>();
	private HashMap<Integer, String> index_name = new HashMap<Integer, String>();
	private BuyerInfoHandler buyer_info;
	private ArrayList<TicketTypeContoller> _ticketcart;
	private ArrayList<OrderItemListHandler> order_line_items,buyer_order_line_items;
	private ArrayList<ItemTypeController> items_list = new ArrayList<ItemTypeController>();
	int formPosition =0;
	private boolean issingleticket=false;


	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_search_attendee);


		_ticketcart=(ArrayList<TicketTypeContoller>) this.getIntent().getSerializableExtra(Util.CART_TICKETS);
		formPosition=this.getIntent().getExtras().getInt("FormPosition"); //formposition for setting the data and carrying the position to set attendee image
		total=this.getIntent().getDoubleExtra(Util.TOTAL, 0.0);
		DiscountedValue=this.getIntent().getDoubleExtra(Util.DISCOUNTEDVALUE, 0.0);
		afterdisounttotalwithtax =this.getIntent().getDoubleExtra(Util.AFTERDISCOUNTTOTALWITHTAX, 0.0);
		issingleticket =this.getIntent().getBooleanExtra(Util.ONLY1FORM,false);
		servicefee=this.getIntent().getDoubleExtra(Util.SERVICE_FEE, 0.0);
		servicetax=this.getIntent().getDoubleExtra(Util.SERVICE_TAX, 0.0);
		promocode=this.getIntent().getStringExtra(Util.PROMOCODE);
		current_item_index=this.getIntent().getIntExtra(Util.INDEX, 0);
		form_count=this.getIntent().getIntExtra(Util.FORMCOUNT, 0);
		buyer_info = (BuyerInfoHandler) this.getIntent().getSerializableExtra(Util.INTENT_KEY_1);
		//For Data transfer
		String json_string = Util.order_request.getString(Util.ORDER_REQUEST_STRING, "");
		String json_items = Util.order_Items.getString(Util.ORDER_ITEMS_STRING, "");
		if(!json_string.isEmpty()){
			Type type = new TypeToken<List<OrderItemListHandler>>(){}.getType();
			order_line_items = new Gson().fromJson(json_string, type);
		}
		if(!json_items.isEmpty()){
			Type type = new TypeToken<List<ItemTypeController>>(){}.getType();
			items_list = new Gson().fromJson(json_items, type);
		}
		//order_line_items = (ArrayList<OrderItemListHandler>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_2);
		//items_list = (ArrayList<ItemTypeController>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_3);
		buyer_order_line_items=(ArrayList<OrderItemListHandler>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_4);
		index_name=(HashMap<Integer, String>) this.getIntent().getSerializableExtra(Util.COPY_VALUES);
		block_ticket_map = (HashMap<String, ArrayList<BlockTicketListController>>) this.getIntent().getSerializableExtra(Util.BLOCK_TICKET_MAP);
		ticketname_for_packageitems = (ArrayList<BlockTicketListController>)this.getIntent().getSerializableExtra(Util.BLOCK_PACKAGE_TICKET);
		ticket_setting=this.getIntent().getStringExtra(Util.TICKETSETTING);


		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent c_order_info_intent=null;
				if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
					 c_order_info_intent =new Intent(SearchAttendeeActivity.this,SelfcheckinCollectOrderInfo.class);
				}else {
					 c_order_info_intent = new Intent(SearchAttendeeActivity.this, CollectOrderInfo.class);
				}
				c_order_info_intent.putExtra(Util.ACCESS_KEY_ATTENDEE_DETAIL,Util.ATTENDEE_DETAIL);
				c_order_info_intent.putExtra(Util.ACCESS_KEY_COLLECT_ORDER_INFO, Util.ATTENDEE_DETAIL);
				c_order_info_intent.putExtra(Util.ONLY1FORM, issingleticket);
				c_order_info_intent.putExtra(Util.CART_TICKETS, _ticketcart);
				c_order_info_intent.putExtra(Util.TOTAL, total);
				c_order_info_intent.putExtra(Util.DISCOUNTEDVALUE, DiscountedValue);
				c_order_info_intent.putExtra(Util.AFTERDISCOUNTTOTALWITHTAX, afterdisounttotalwithtax);
				c_order_info_intent.putExtra(Util.SERVICE_FEE, servicefee);
				c_order_info_intent.putExtra(Util.SERVICE_TAX, servicetax);
				c_order_info_intent.putExtra(Util.PROMOCODE, promocode);
				c_order_info_intent.putExtra(Util.INDEX, current_item_index);
				c_order_info_intent.putExtra(Util.INTENT_KEY_1, buyer_info);

				//For Data transfer
				String json_string = new Gson().toJson(order_line_items);
				String json_items = new Gson().toJson(items_list);
				Util.order_request.edit().putString(Util.ORDER_REQUEST_STRING, json_string).commit();
				Util.order_Items.edit().putString(Util.ORDER_ITEMS_STRING, json_items).commit();

				//c_order_info_intent.putExtra(Util.INTENT_KEY_2,order_line_items);
				//c_order_info_intent.putExtra(Util.INTENT_KEY_3, items_list);
				c_order_info_intent.putExtra(Util.INTENT_KEY_4, buyer_order_line_items);
				c_order_info_intent.putExtra(Util.FORMCOUNT, form_count);
				c_order_info_intent.putExtra(Util.COPY_VALUES, index_name);
				c_order_info_intent.putExtra(Util.TICKETSETTING,ticket_setting);
				c_order_info_intent.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
				c_order_info_intent.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
				c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				c_order_info_intent.putExtra(Util.ISCOLLECT_INFO_FROM_BUYER, SearchAttendeeActivity.this.getIntent().getBooleanExtra(Util.ISCOLLECT_INFO_FROM_BUYER, false));
				c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				c_order_info_intent.putExtra("FormPosition",formPosition);
				startActivity(c_order_info_intent);
				finish();
			}
		});

		search_view.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				search_view.setFocusable(true);
				SearchAttendee();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {

			}
			@Override
			public void afterTextChanged(Editable s) {

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
				//search_view.setFocusable(false);
				hideKeybord(v);
			}
		});

		searchAttendee.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> _adapter, View view, int position,
									long index) {
				Cursor c1 = adapter.getCursor();
				c1.moveToPosition(position);
				//Log.i("-------------Cursor Values-----------",":"+c1+":"+c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_DESIGNATION)));
				//Log.i("AttendeeListActivity","Order_Id "+ c1.getString(c1.getColumnIndex("Order_Id")));
				Intent c_order_info_intent=null;
				if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
					c_order_info_intent =new Intent(SearchAttendeeActivity.this,SelfcheckinCollectOrderInfo.class);
				}else {
					c_order_info_intent = new Intent(SearchAttendeeActivity.this, CollectOrderInfo.class);
				}
				//Intent c_order_info_intent = new Intent(SearchAttendeeActivity.this, CollectOrderInfo.class);
				if (current_item_index == 0) {
					buyer_info.setFirstName(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)));
					buyer_info.setLastName(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
					buyer_info.setEmail(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)));
					buyer_info.setCompany(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
					buyer_info.setMobile(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)));
					buyer_info.setDesignation(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE)));
					buyer_info.setBadge_lable(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
					buyer_info.setnote(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_NOTE)));
				} else {
					order_line_items.get(current_item_index - 1).setCompanyName(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
					order_line_items.get(current_item_index - 1).setLastName(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
					order_line_items.get(current_item_index - 1).setEmail(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)));
					order_line_items.get(current_item_index - 1).setFirstName(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)));
					order_line_items.get(current_item_index - 1).setMobile(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_MOBILE)));
					order_line_items.get(current_item_index - 1).setDesignation(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_JOB_TILE)));
					order_line_items.get(current_item_index - 1).setBadgeLabel(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
					order_line_items.get(current_item_index - 1).setnote(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_NOTE)));
					//order_line_items.get(current_item_index	-1).setItemId(c1.getString(c1.getColumnIndex("Item_Id")));

				}
				c_order_info_intent.putExtra(Util.ACCESS_KEY_ATTENDEE_DETAIL,Util.ATTENDEE_DETAIL);
				c_order_info_intent.putExtra(Util.ACCESS_KEY_COLLECT_ORDER_INFO, Util.ATTENDEE_DETAIL);
				c_order_info_intent.putExtra(Util.ONLY1FORM, issingleticket);
				c_order_info_intent.putExtra(Util.CART_TICKETS, _ticketcart);
				c_order_info_intent.putExtra(Util.TOTAL, total);
				c_order_info_intent.putExtra(Util.DISCOUNTEDVALUE, DiscountedValue);
				c_order_info_intent.putExtra(Util.AFTERDISCOUNTTOTALWITHTAX, afterdisounttotalwithtax);
				c_order_info_intent.putExtra(Util.SERVICE_FEE, servicefee);
				c_order_info_intent.putExtra(Util.SERVICE_TAX, servicetax);
				c_order_info_intent.putExtra(Util.PROMOCODE, promocode);
				c_order_info_intent.putExtra(Util.INDEX, current_item_index);
				c_order_info_intent.putExtra(Util.INTENT_KEY_1, buyer_info);
				//c_order_info_intent.putExtra(Util.INTENT_KEY_2,order_line_items);
				//c_order_info_intent.putExtra(Util.INTENT_KEY_3, items_list);

				//For Data transfer
				String json_string = new Gson().toJson(order_line_items);
				String json_items = new Gson().toJson(items_list);
				Util.order_request.edit().putString(Util.ORDER_REQUEST_STRING, json_string).commit();
				Util.order_Items.edit().putString(Util.ORDER_ITEMS_STRING, json_items).commit();

				c_order_info_intent.putExtra(Util.INTENT_KEY_4, buyer_order_line_items);
				c_order_info_intent.putExtra(Util.FORMCOUNT, form_count);
				c_order_info_intent.putExtra(Util.COPY_VALUES, index_name);
				c_order_info_intent.putExtra(Util.TICKETSETTING,ticket_setting);
				c_order_info_intent.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
				c_order_info_intent.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
				c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				c_order_info_intent.putExtra(Util.ISCOLLECT_INFO_FROM_BUYER, SearchAttendeeActivity.this.getIntent().getBooleanExtra(Util.ISCOLLECT_INFO_FROM_BUYER, false));
				c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				c_order_info_intent.putExtra("FormPosition",formPosition);
				startActivity(c_order_info_intent);
				finish();

			}});

		btn_order_load_more.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String whereClause =  " where orders.Buyer_Id = user.GNUserID" +
						" AND orders.Event_Id = '" + checked_in_eventId+ "' order by orders.Order_Date DESC";
				Cursor order_cursor = Util.db.getPaymentDataCursor(whereClause);

				if (isOnline()) {

					if(order_cursor.getCount()<Util.dashboardHandler.totalOrders){
						isLoading=true;
						txtLoadingmore.setText("Loading....");
						doRequest();
					}else{
						AlertDialogCustom dialog=new AlertDialogCustom(SearchAttendeeActivity.this);
						dialog.setParamenters("Alert", "All records are downloaded", null, null, 1, false);
						dialog.show();
					}
				} else {
					startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
				}

			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		String offset = Util.offset_pref.getString(checked_in_eventId, "");
		String url = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
				+ checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
				+ checkedin_event_record.Events.scan_attendee_limit__c;
		/*String url = sfdcddetails.instance_url
				+ WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
				+ checked_in_eventId + "&User_id=" + sfdcddetails.user_id
				+ "&offset=" + att_cursor.getCount() + "&limit="
				+ checkedin_event_record.Events.scan_attendee_limit__c;*/
		postMethod = new HttpPostData("Loading Attendees...",url, null, access_token,SearchAttendeeActivity.this);
		postMethod.execute();
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {

		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			gson = new Gson();
			TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
			if (totalorderlisthandler.TotalLists.size() > 0) {
				Util.db.upadteOrderList(totalorderlisthandler.TotalLists,
						checked_in_eventId);
			}
			txtLoadingmore.setText("Completed");
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setAttendeeCursor();
				}
			});
		}catch(Exception e){
			e.printStackTrace();
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
		try {
			// TODO Auto-generated method stub
			activity = this;
			View v = inflater.inflate(layout, null);
			linearview.addView(v);
			txt_title.setText("Search Attendee");
			img_setting.setVisibility(View.GONE);
			img_menu.setImageResource(R.drawable.back_button);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			event_layout.setVisibility(View.GONE);
			button_layout.setVisibility(View.GONE);
			event_layout.setVisibility(View.VISIBLE);
			txt_save.setVisibility(View.GONE);
			img_search.setVisibility(View.VISIBLE);
			back_layout.setVisibility(View.GONE);
			search_layout.setVisibility(View.VISIBLE);
			search_view.setHint("Search by Fname, Lname, Email...");
			search_view.setFocusable(true);
			searchAttendee = (ListView) linearview.findViewById(R.id.search_attendee);
			txt_nodata = (TextView) linearview.findViewById(R.id.txt_nodata);
			loadMoreView = ((LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
					R.layout.load_more_footer, null, false);
			btn_order_load_more = (LinearLayout) loadMoreView
					.findViewById(R.id.loadmoreLay);

			txtLoadingmore = (TextView) loadMoreView
					.findViewById(R.id.txt_loading);
			searchAttendee.addFooterView(loadMoreView);
			txtLoadingmore.setText("Load More...");
			whereClause_name = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '" + checked_in_eventId + "' ORDER BY CheckedInDate DESC ";
			att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, ITransaction.EMPTY_STRING, ITransaction.EMPTY_STRING, ITransaction.EMPTY_STRING);
			if (att_cursor.getCount() > 0) {
				searchAttendee.setVisibility(View.VISIBLE);
				adapter = new AttendeeAdapter(SearchAttendeeActivity.this, att_cursor);
				searchAttendee.setAdapter(adapter);
			} else {
				searchAttendee.setVisibility(View.GONE);
				txt_nodata.setVisibility(View.VISIBLE);
				if (isOnline()) {
					doRequest();
				} else {
					startErrorAnimation(getString(R.string.network_error), txt_error_msg);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private void setAttendeeCursor(){
		txtLoadingmore.setText("Load More...");
		whereClause_name=" where "+DBFeilds.ATTENDEE_EVENT_ID+" = '"+ checked_in_eventId + "' ORDER BY CheckedInDate DESC ";
		att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
		adapter =new AttendeeAdapter(SearchAttendeeActivity.this, att_cursor);
		/*if(adapter != null){
			adapter.changeCursor(att_cursor);
			adapter.notifyDataSetChanged();
		}*/
		if(att_cursor.getCount()>0){
			searchAttendee.setVisibility(View.VISIBLE);
			searchAttendee.setAdapter(adapter);
		}else{
			searchAttendee.setVisibility(View.GONE);
			txt_nodata.setVisibility(View.VISIBLE);
		}
	}

	private class AttendeeAdapter  extends CursorAdapter
	{
		Cursor attCursor;
		public AttendeeAdapter(Context context,Cursor c){
			super(context, c);
			this.attCursor=c;
		}
		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View parent_view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			final View v;
			if (parent_view==null) {
				v = newView(context, cursor, null);
			}else{
				v = parent_view;
			}

			final ViewHolder holder = (ViewHolder) v.getTag();
			holder.email.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)));
			holder.name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
			if(!Util.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))).isEmpty()){
				holder.company.setVisibility(View.VISIBLE);
				holder.company.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
			}else{
				holder.company.setVisibility(View.GONE);
			}
			if(cursor.getPosition()%2==0)
				holder.colorView.setBackgroundResource(R.color.darl_gray_text_color);
			else
				holder.colorView.setBackgroundResource(R.color.light_green);

		}
		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View rowView=inflater.inflate(R.layout.search_attendee_item, null);
			ViewHolder holder=new ViewHolder();
			holder.name=(TextView) rowView.findViewById(R.id.ticketname);
			holder.email = (TextView) rowView.findViewById(R.id.ticketnum);
			holder.company=(TextView) rowView.findViewById(R.id.txt_comp);
			holder.colorView=(View)rowView.findViewById(R.id.color_side_bar);
			holder.name.setTypeface(Util.droid_bold);
			holder.email.setTypeface(Util.droid_regrular);
			holder.company.setTypeface(Util.droid_regrular);
			rowView.setTag(holder);
			return rowView;
		}
	}

	private class ViewHolder{
		TextView name,email,company;
		View colorView;

	}

	private void SearchAttendee(){
		searchAttendee.setVisibility(View.VISIBLE);
		whereClause_name=" Where Event_Id='"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_FIRST_NAME+" like  '"
				+"%"+search_view.getText().toString().toLowerCase()+"%"+"'"+"ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" ASC";
		if(att_cursor==null)
		{
			att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
		}else{
			att_cursor.close();
			att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
		}
		if (att_cursor.getCount() > 0) {
			adapter = new AttendeeAdapter(SearchAttendeeActivity.this, att_cursor);
			searchAttendee.setAdapter(adapter);
		} else {
			whereClause_name=" Where Event_Id='"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_LAST_NAME+" like  '"
					+"%"+search_view.getText().toString().toLowerCase()+"%"+"'"+"ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" ASC";
			att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
			if (att_cursor.getCount() > 0) {
				adapter = new AttendeeAdapter(SearchAttendeeActivity.this,att_cursor);
				searchAttendee.setAdapter(adapter);
			} else {
				whereClause_name=" Where Event_Id= '"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_COMPANY+" like  '"
						+"%"+search_view.getText().toString()+"%"+ "'" + " ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" ASC";
				att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
				//Log.i("Short by company","Cursor count"+att_cursor.getCount());
				if (att_cursor.getCount() > 0) {
					adapter = new AttendeeAdapter(SearchAttendeeActivity.this,att_cursor);
					searchAttendee.setAdapter(adapter);
				} else {
					whereClause_name=" Where Event_Id = '"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_EMAIL_ID+" like  '"
							+"%"+search_view.getText().toString().toLowerCase()+"%"+"'"+"ORDER BY "+DBFeilds.ATTENDEE_EMAIL_ID+" ASC";
					att_cursor = Util.db.getAttendeeDataCursor(whereClause_name,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING,ITransaction.EMPTY_STRING);
					if (att_cursor.getCount() > 0) {
						adapter = new AttendeeAdapter(SearchAttendeeActivity.this, att_cursor);
						searchAttendee.setAdapter(adapter);
					} else {
						searchAttendee.setVisibility(View.GONE);
						txt_nodata.setVisibility(View.VISIBLE);
					}
				}
			}
		}

	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Intent c_order_info_intent=null;
		if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
			c_order_info_intent =new Intent(SearchAttendeeActivity.this,SelfcheckinCollectOrderInfo.class);
		}else {
			c_order_info_intent = new Intent(SearchAttendeeActivity.this, CollectOrderInfo.class);
		}
		//Intent c_order_info_intent =new Intent(SearchAttendeeActivity.this,CollectOrderInfo.class);
		c_order_info_intent.putExtra(Util.ACCESS_KEY_ATTENDEE_DETAIL,Util.ATTENDEE_DETAIL);
		c_order_info_intent.putExtra(Util.ACCESS_KEY_COLLECT_ORDER_INFO, Util.ATTENDEE_DETAIL);
		c_order_info_intent.putExtra(Util.ONLY1FORM, issingleticket);
		c_order_info_intent.putExtra(Util.CART_TICKETS, _ticketcart);
		c_order_info_intent.putExtra(Util.TOTAL, total);
		c_order_info_intent.putExtra(Util.DISCOUNTEDVALUE, DiscountedValue);
		c_order_info_intent.putExtra(Util.AFTERDISCOUNTTOTALWITHTAX, afterdisounttotalwithtax);
		c_order_info_intent.putExtra(Util.SERVICE_FEE, servicefee);
		c_order_info_intent.putExtra(Util.SERVICE_TAX, servicetax);
		c_order_info_intent.putExtra(Util.PROMOCODE, promocode);
		c_order_info_intent.putExtra(Util.INDEX, current_item_index);
		c_order_info_intent.putExtra(Util.INTENT_KEY_1, buyer_info);

		//For Data transfer
		String json_string = new Gson().toJson(order_line_items);
		String json_items = new Gson().toJson(items_list);
		Util.order_request.edit().putString(Util.ORDER_REQUEST_STRING, json_string).commit();
		Util.order_Items.edit().putString(Util.ORDER_ITEMS_STRING, json_items).commit();

		//c_order_info_intent.putExtra(Util.INTENT_KEY_2,order_line_items);
		//c_order_info_intent.putExtra(Util.INTENT_KEY_3, items_list);
		c_order_info_intent.putExtra(Util.INTENT_KEY_4, buyer_order_line_items);
		c_order_info_intent.putExtra(Util.FORMCOUNT, form_count);
		c_order_info_intent.putExtra(Util.COPY_VALUES, index_name);
		c_order_info_intent.putExtra(Util.TICKETSETTING,ticket_setting);
		c_order_info_intent.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
		c_order_info_intent.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
		c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		c_order_info_intent.putExtra(Util.ISCOLLECT_INFO_FROM_BUYER, SearchAttendeeActivity.this.getIntent().getBooleanExtra(Util.ISCOLLECT_INFO_FROM_BUYER, false));
		c_order_info_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		c_order_info_intent.putExtra("FormPosition",formPosition);
		startActivity(c_order_info_intent);
		finish();
	}
}
