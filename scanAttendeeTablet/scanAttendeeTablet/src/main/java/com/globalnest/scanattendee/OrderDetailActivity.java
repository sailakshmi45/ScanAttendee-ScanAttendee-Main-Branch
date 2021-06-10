package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.stripe.model.Order;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OrderDetailActivity extends BaseActivity {
	TextView txt_person_name,txt_total,txt_total_amt,txt_email,txt_order_id,txt_date,txt_ispaid,txt_fee,txt_discount,txt_tax,txt_company
			,txt_OrderTotalPaid,txt_OrderSubTotal,txt_OrderFee,txt_OrderTax,txt_OrderDiscount,txt_OrderAmountDue,txt_labelamountDue,txt_Regtype,txt_PhoneNo;
	//txt_trans_id,txt_payType,layout_paygateway,,txt_paygateway_name,layout_card_number,txt_card_number,layout_card_type,,txt_card_type,txt_OD_checkno

	ListView list_ticket_details,list_person_details,list_cancel_person_details,list_view_payment_items;
	LinearLayout layout_discount,layout_fee,layout_tax,layout_booked_tickets,layout_cancelled_tickets;
	TicketListAdapter ticket_adapter;
	PersonListAdapter person_adapter;
	PaymentItemListAdapter paymentItemListAdapter;
	CancelPersonListAdapter cancel_person_adapter;
	ScrollView scroll_view_order;
	String whereClause,requestType = "";
	Cursor order_cursor,order_payment_cursor;
	Button btn_order_cancel;
	private String orderid,order_item_id="",order_ticket_id="";
	private String _check_no = "", _check_status = "",order_payment_id="",payment_notes="";
	private Dialog add_key_dialog;
	//private ArrayList<String> order_items_list = new ArrayList<String>();
	private HashMap<String, Integer> order_line_items_map = new HashMap<String, Integer>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_order_detail);
		orderid = getIntent().getStringExtra(Util.ORDER_ID);
		setCustomContentViewData(orderid);
		list_person_details.setScrollContainer(false);
		list_ticket_details.setScrollContainer(false);
		list_view_payment_items.setScrollContainer(false);
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		btn_order_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				orderid=getIntent().getStringExtra(Util.ORDER_ID);
				order_item_id = "";
				order_ticket_id="";
				if(isOnline()){
					openOrderDeleteDialog("Are you sure you want to cancel the order ?");
				}else{
					startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
				}
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();

	}
	private void openOrderDeleteDialog(String msg){
		Util.setCustomAlertDialog(OrderDetailActivity.this);
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.setCustomDialogImage(R.drawable.error);
		Util.txt_dismiss.setText("NO");
		Util.txt_okey.setText("YES");
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				doRequest();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();

			}
		});
		Util.openCustomDialog("Alert",msg);
	}



	/**
	 *
	 */
	private void setCustomContentViewData(String Order_Id) {
		try{
			//double totalAmount=0;

			whereClause = " where orders.Buyer_Id = user.UserID" +
					" AND orders.Event_Id = '" + checked_in_eventId + "' AND orders." + DBFeilds.ORDER_ORDER_ID + " = '" + Order_Id + "'";
			order_cursor = Util.db.getOrderDetailDataCursor(whereClause);
			order_payment_cursor = Util.db.getPaymentItemDataCursor(" where " + DBFeilds.ORDER_PAYMENT_ITEM_EVENTID + " = '" + checked_in_eventId + "' AND " + DBFeilds.ORDER_PAYMENT_ITEM_ORDER_ID + " = '" + Order_Id + "'");
			if (order_cursor.getCount() > 0) {
				order_cursor.moveToFirst();
				order_payment_cursor.moveToFirst();
				txt_person_name.setText(NullChecker(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.USER_FIRST_NAME))) + " " + NullChecker(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.USER_LAST_NAME))));
				txt_Regtype.setText("Registration Type: " + NullChecker(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_REGISTRATION_TYPE))));
				txt_PhoneNo.setText(Util.db.getAttendeeWorkorMobile(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))));
				txt_email.setText(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.USER_EMAIL_ID)));
				if (!NullChecker(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.USER_COMPANY))).isEmpty())
					txt_company.setText(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.USER_COMPANY)));
				else
					txt_company.setText("No Company");
				txt_order_id.setText(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_NAME)));

				try {
					String order_datetime = Util.change_US_ONLY_DateFormatWithSEC(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_DATE)), checkedin_event_record.Events.Time_Zone__c);
					txt_date.setText(order_datetime);
				} catch (Exception e) {
					e.printStackTrace();
				}
				//Cancelled
				if (NullChecker(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS))).equalsIgnoreCase("Cancelled")) {
					btn_order_cancel.setVisibility(View.GONE);
				} else {
					btn_order_cancel.setVisibility(View.VISIBLE);
				}
				if (!isEventAdmin()) {
					btn_order_cancel.setVisibility(View.GONE);
				}

				txt_ispaid.setText(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_STATUS)));
				//Log.i("---------two decimal degits---------", ""+Util.RoundTo2Decimals(2));
				txt_total.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TOTAL))));
				//totalAmount=order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TOTAL));
				txt_OrderTotalPaid.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_AMOUNT_PAID))));
				txt_OrderSubTotal.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_SUBTOTAL))));
				txt_OrderFee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT))));
				txt_OrderTax.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TAX))));
				txt_OrderDiscount.setText("-" + Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_DISCOUNT))));
			/*if(!order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_AMOUNT_DUE)).isEmpty()&&
					order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_AMOUNT_DUE))>0) {
				txt_labelamountDue.setVisibility(View.VISIBLE);
				txt_OrderAmountDue.setVisibility(View.VISIBLE);
				txt_OrderAmountDue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c +" "+ Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_AMOUNT_DUE))));
			}*/
				txt_OrderAmountDue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_AMOUNT_DUE))));
				if (!order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT)).isEmpty() && Double.parseDouble(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT))) != 0) {
					txt_fee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_FEE_AMOUNT))));
					//totalAmount=totalAmount+Double.parseDouble(txt_fee.getText().toString());
				} else {
					layout_fee.setVisibility(View.GONE);
				}

				if (!order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TAX)).isEmpty() && Double.parseDouble(order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TAX))) != 0) {
					txt_tax.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TAX))));
					//totalAmount=totalAmount+Double.parseDouble(txt_tax.getText().toString());
				} else {
					layout_tax.setVisibility(View.GONE);
				}

				//txt_discount.setText("NILL");
				layout_discount.setVisibility(View.GONE);

				txt_total_amt.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(order_cursor.getDouble(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_TOTAL))));

				whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '" + checked_in_eventId + "' And "
						+ DBFeilds.ATTENDEE_ORDER_ID + " = '" + order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)) + "'"
						+ " AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Booked'";


				Cursor ticket_cursor = Util.db.getOrderDetailsCursor(whereClause);
				if (ticket_cursor != null && ticket_cursor.getCount() > 0) {
					layout_booked_tickets.setVisibility(View.VISIBLE);
					person_adapter = new PersonListAdapter(OrderDetailActivity.this, ticket_cursor);
					list_person_details.setAdapter(person_adapter);
					Util.setListViewHeightBasedOnChildren(list_person_details);

				} else {
					layout_booked_tickets.setVisibility(View.GONE);
				}

				whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '" + checked_in_eventId + "' And "
						+ DBFeilds.ATTENDEE_ORDER_ID + " = '" + order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)) + "'"
						+ " AND " + DBFeilds.ATTENDEE_TICKET_STATUS + " = 'Cancelled'";

				Cursor cancel_ticket_cursor = Util.db.getOrderDetailsCursor(whereClause);
				if (cancel_ticket_cursor.getCount() > 0) {
					layout_cancelled_tickets.setVisibility(View.VISIBLE);
					cancel_person_adapter = new CancelPersonListAdapter(OrderDetailActivity.this, cancel_ticket_cursor);
					list_cancel_person_details.setAdapter(cancel_person_adapter);
					Util.setListViewHeightBasedOnChildren(list_cancel_person_details);

				} else {
					layout_cancelled_tickets.setVisibility(View.GONE);
				}

				whereClause = " where Event_Id = '" + checked_in_eventId + "' AND Order_Id ='" + order_cursor.getString(order_cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)) + "'";

				Cursor itempool_cursor = Util.db.getOrderDetailPool(whereClause);
				double order_discount = 0;
				for (int i = 0; i < itempool_cursor.getCount(); i++) {
					itempool_cursor.moveToPosition(i);
					if (NullChecker(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_STATUS))).equalsIgnoreCase("Cancelled")) {
						//order_items_list.add(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID)));
						String item_id = itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID));
						if (order_line_items_map.containsKey(item_id)) {
							int qty = Integer.parseInt(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY))) + order_line_items_map.get(item_id);
							//Log.i("-------------------Cancelled Qty--------------",":"+Integer.parseInt(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY)))+":"+order_line_items_map.get(item_id)+":"+qty);
							order_line_items_map.put(item_id, qty);
						} else {
							order_line_items_map.put(item_id, Integer.parseInt(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY))));
						}
					} else {
						if (!Util.NullChecker(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_DISCOUNT))).isEmpty()) {
							order_discount = order_discount + Double.parseDouble(itempool_cursor.getString(itempool_cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_DISCOUNT)));
						}
					}

				}
				paymentItemListAdapter = new PaymentItemListAdapter(OrderDetailActivity.this, order_payment_cursor);
				list_view_payment_items.setAdapter(paymentItemListAdapter);
				Util.setListViewHeightBasedOnChildren(list_view_payment_items);
				ticket_adapter = new TicketListAdapter(OrderDetailActivity.this, itempool_cursor);
				list_ticket_details.setAdapter(ticket_adapter);
				Util.setListViewHeightBasedOnChildren(list_ticket_details);

				if (order_discount > 0) {
					layout_discount.setVisibility(View.VISIBLE);
					txt_discount.setText("-" + Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(order_discount));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		v=inflater.inflate(layout, null);
		linearview.addView(v);
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		txt_title.setText("Order Details");
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		txt_person_name=(TextView) linearview.findViewById(R.id.txt_OD_Name);
		txt_total=(TextView) linearview.findViewById(R.id.txt_OD_total);
		txt_total_amt=(TextView) linearview.findViewById(R.id.txt_OD_totalamount);
		txt_email=(TextView) linearview.findViewById(R.id.txt_OD_email);
		txt_order_id=(TextView) linearview.findViewById(R.id.txt_OD_Order_id_top);
		txt_date=(TextView) linearview.findViewById(R.id.txt_OD_date);
		//newly added
		txt_Regtype=(TextView) linearview.findViewById(R.id.txt_Reg_type);
		txt_PhoneNo=(TextView) linearview.findViewById(R.id.txt_phoneno);
		txt_OrderTotalPaid=(TextView) linearview.findViewById(R.id.txt_OrderTotalPaid);
		txt_OrderSubTotal=(TextView) linearview.findViewById(R.id.txt_order_subtotal);
		txt_OrderFee=(TextView) linearview.findViewById(R.id.txt_order_fee);
		txt_OrderTax=(TextView) linearview.findViewById(R.id.txt_order_tax);
		txt_OrderDiscount=(TextView) linearview.findViewById(R.id.txt_order_discount);
		txt_OrderAmountDue=(TextView) linearview.findViewById(R.id.txt_order_amountdue);
		txt_labelamountDue=(TextView) linearview.findViewById(R.id.txt_labelamountDue);
		txt_ispaid=(TextView) linearview.findViewById(R.id.txt_OD_paid);
		txt_fee=(TextView) linearview.findViewById(R.id.txt_OD_fee);
		txt_discount=(TextView) linearview.findViewById(R.id.txt_OD_discount);
		txt_tax=(TextView) linearview.findViewById(R.id.txt_OD_tax);
		txt_company=(TextView) linearview.findViewById(R.id.txt_OD_company);
		scroll_view_order=(ScrollView) linearview.findViewById(R.id.scroll_view_order);
		layout_discount=(LinearLayout) linearview.findViewById(R.id.layout_OD_discount);
		layout_fee=(LinearLayout) linearview.findViewById(R.id.layout_OD_fee);
		layout_tax=(LinearLayout) linearview.findViewById(R.id.layout_OD_tax);
		list_person_details=(ListView) linearview.findViewById(R.id.list_view_order_person_details);
		list_ticket_details=(ListView) linearview.findViewById(R.id.list_view_order_items);
		list_view_payment_items =(ListView) linearview.findViewById(R.id.list_view_payment_items);
		list_cancel_person_details = (ListView)linearview.findViewById(R.id.list_view_cancel_order_person_details);
		layout_booked_tickets = (LinearLayout)linearview.findViewById(R.id.layout_booked_tickets);
		layout_cancelled_tickets = (LinearLayout)linearview.findViewById(R.id.layout_cancelled_tickets);
		btn_order_cancel = (Button)linearview.findViewById(R.id.btn_order_cancel);
	}



	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;

		if (requestType.equals(Util.EDIT_CHECK_NO)) {
            try{
                hideSoftKeyboard(OrderDetailActivity.this);
            }catch (Exception e){
                e.printStackTrace();
            }
			postMethod = new HttpPostData("", setEditCheckUrl(), null,
					access_token, OrderDetailActivity.this);
			postMethod.execute();
		}else {
			postMethod = new HttpPostData("Cancelling Order...", sfdcddetails.instance_url + WebServiceUrls.SA_DELETE_ORDER_OR_TICKET + getQueryParams(), null, access_token, this);
			postMethod.execute();
		}
	}
	public String setEditCheckUrl() {
		return sfdcddetails.instance_url
				+ WebServiceUrls.SA_EDIT_CHECK_NO
				+ "orderId=" + orderid
				+ "&paymentId="+ order_payment_id
				+ "&chkNo=" + _check_no.replace(" ", "%20")
				+ "&chkStatus=" + _check_status.replace(" ", "%20")
				+ "&payNote="+payment_notes.replace(" ", "%20");
		/*return sfdcddetails.instance_url
				+ WebServiceUrls.SA_EDIT_CHECK_NO
				+ "orderId="
				+ payment_cursor.getString(payment_cursor
				.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))
				+ "&chkNo=" + _check_no.replace(" ", "%20") + "&chkStatus="
				+ _check_status.replace(" ", "%20");*/
	}

	private String getQueryParams(){
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		if(!orderid.isEmpty())
			values.add(new BasicNameValuePair("orderid", orderid));
		if(!order_item_id.isEmpty())
			values.add(new BasicNameValuePair("orderitemid", order_item_id));
		if(!order_ticket_id.isEmpty())
			values.add(new BasicNameValuePair("ticketid", order_ticket_id));

		return AppUtils.getQuery(values);
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		try {
			if(!isValidResponse(response)) {
			openSessionExpireAlert(errorMessage(response));
		}
		if (requestType.equals(Util.EDIT_CHECK_NO)) {
			//{"orderStatus":"Fully Paid","message":"Success"}
			JSONObject obj = new JSONObject(response);
			String orderStatus = NullChecker(obj.optString("orderStatus"));
			String message = NullChecker(obj.optString("message"));
			if (message.contains("Success")) {
				Util.db.InsertAndUpdateCheckandpaymentdetails(_check_no,orderid,_check_status,order_payment_id,payment_notes,orderStatus);
				Toast.makeText(OrderDetailActivity.this,"Updated Sucessfully!",Toast.LENGTH_LONG).show();
				finish();
			} else {
				AlertDialogCustom dialog = new AlertDialogCustom(
						OrderDetailActivity.this);
				dialog.setParamenters("Alert",
						"Sorry! Failed to update the given details", null, null,
						1, false);
				dialog.show();
			}
		}else {
			RefreshResponse refresh = new Gson().fromJson(response, RefreshResponse.class);
			Util.db.upadteOrderList(refresh.TotalLists, checked_in_eventId);
			if (refresh.TotalLists.size() > 0) {
				order_line_items_map.clear();
				orderid = refresh.TotalLists.get(0).orderInn.getOrderId();
				setCustomContentViewData(orderid);
			}
		}
		} catch (JSONException e) {
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



	public class PersonListAdapter extends CursorAdapter{

		TextView person_name,ticket_name;
		Button btn_order_ticket_delete;
		/**
		 * @param context
		 * @param c
		 */
		public PersonListAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, final Cursor cursor) {

			person_name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));

			String parent_id = Util.db.getItemPoolParentId(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
			if(!NullChecker(parent_id).isEmpty()){
				String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
				ticket_name.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+"<br><font color=#848484 >"+" - "+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+" </font>"));
			}else{
				ticket_name.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+"<br><font color='red' >"+" - "+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+" </font>"));
			}


			String ticket_item_type = Util.db.getItemTypeName(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_TYPE_ID)));
			if(ticket_item_type.equalsIgnoreCase("Package")){
				btn_order_ticket_delete.setVisibility(View.GONE);
			}else{
				btn_order_ticket_delete.setVisibility(View.VISIBLE);
			}
			if(!isEventAdmin()){
				btn_order_ticket_delete.setVisibility(View.GONE);
			}
			btn_order_ticket_delete.setTag(cursor.getPosition());
			btn_order_ticket_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					cursor.moveToPosition((Integer)v.getTag());
					//Log.i("------------------Item Name------------",":"+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)));
					order_item_id = "";
					orderid="";
					order_ticket_id=cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));;
					if(isOnline()){
						openOrderDeleteDialog("Are you sure, you want cancel the ticket?");
					}else{
						startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
					}
				}
			});
		}


		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v=inflater.inflate(R.layout.order_person_list_item, null);
			person_name=(TextView) v.findViewById(R.id.txt_OP_name);
			ticket_name=(TextView) v.findViewById(R.id.txt_OP_ticket_name);
			btn_order_ticket_delete = (Button)v.findViewById(R.id.btn_order_ticket_cancel);
			//btn_order_ticket_delete.setVisibility(View.GONE);
			return v;
		}


	}

	public class CancelPersonListAdapter extends CursorAdapter{

		TextView person_name,ticket_name;
		Button btn_order_ticket_delete;
		/**
		 * @param context
		 * @param c
		 */
		public CancelPersonListAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			person_name.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
			ticket_name.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+"<br><font color=#848484 >"+"("+cursor.getString(cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+")"+" </font>"));
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v=inflater.inflate(R.layout.order_person_list_item, null);
			person_name=(TextView) v.findViewById(R.id.txt_OP_name);
			ticket_name=(TextView) v.findViewById(R.id.txt_OP_ticket_name);
			btn_order_ticket_delete = (Button)v.findViewById(R.id.btn_order_ticket_cancel);
			btn_order_ticket_delete.setVisibility(View.GONE);
			return v;
		}


	}


	public class PaymentItemListAdapter extends CursorAdapter{
		TextView txt_PaymentMode,txt_PaymentTypeName,txt_CardNo,txt_CardType,txt_refnumber,txt_Pay_RegType,txt_PaidAmount,
				txt_Paymentstatus,txt_LabelPaymentstatus,txt_PaymentCheckno,txt_PaymentGatewayName,txt_PaymentDate;
		EditText edt_paynotes;
		LinearLayout layout_creditcarddetails,layout_checkno,layout_editcheckno;
		Button btn_save_notes;
		public PaymentItemListAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v=inflater.inflate(R.layout.order_payment_list_item, null);
			layout_creditcarddetails=(LinearLayout) v.findViewById(R.id.layout_creditcarddetails);//if not credit card hide layout
			layout_checkno=(LinearLayout) v.findViewById(R.id.layout_checkno);
			layout_editcheckno=(LinearLayout) v.findViewById(R.id.layout_editcheckno);//if not check hide layout
			txt_refnumber=(TextView) v.findViewById(R.id.txt_payment_trans_id);
			txt_PaidAmount=(TextView) v.findViewById(R.id.txt_paid_amount);
			txt_Pay_RegType=(TextView) v.findViewById(R.id.txt_pay_reg_type);
			txt_Paymentstatus=(TextView) v.findViewById(R.id.txt_payment_status);
			txt_LabelPaymentstatus =(TextView) v.findViewById(R.id.PayStatus_label);
			txt_PaymentMode=(TextView) v.findViewById(R.id.txt_payment_mode);
			txt_PaymentDate=(TextView) v.findViewById(R.id.txt_payment_date);
			txt_PaymentCheckno=(TextView) v.findViewById(R.id.txt_payment_checkno);
			txt_PaymentGatewayName=(TextView) v.findViewById(R.id.txt_paygateway_name);
			txt_PaymentTypeName=(TextView) v.findViewById(R.id.txt_payment_type);
			txt_CardNo=(TextView) v.findViewById(R.id.txt_card_number);
			txt_CardType=(TextView) v.findViewById(R.id.txt_card_type);
			edt_paynotes=(EditText) v.findViewById(R.id.edt_payment_note);
			btn_save_notes=(Button) v.findViewById(R.id.btn_save_notes);
			return v;
		}

		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			/*txt_PaidAmount.setText(Util.db.getCurrency(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CURRENCY_CODE)))+" "
					+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_AMOUNT)));*/
			txt_PaidAmount.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+" "+
					Util.RoundTo2Decimals(cursor.getDouble(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_AMOUNT))));
			txt_Paymentstatus.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)));
			txt_Pay_RegType.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAY_REG_TYPE)));
			txt_PaymentMode.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)));
			if(!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)).equalsIgnoreCase("Paid")&&
					!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)).isEmpty()){
				txt_LabelPaymentstatus.setText("Amount Due");
				//txt_PaidAmount.setTextColor(getResources().getColor(R.color.red));
				txt_Paymentstatus.setTextColor(getResources().getColor(R.color.red));
			}
			if(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)).equalsIgnoreCase(getString(R.string.creditcard))&&
					!(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)).equalsIgnoreCase("Not Paid")))
			{
				layout_creditcarddetails.setVisibility(View.VISIBLE);
				txt_PaymentTypeName.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_TYPE)));
				txt_PaymentGatewayName.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME)));
				if(!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_4DIGITS)).isEmpty())
					txt_CardNo.setText("XXXX XXXX XXXX "+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_4DIGITS)));
				else
					txt_CardNo.setText("XXXX XXXX XXXX "+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_4DIGITS)));
				txt_CardType.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CREDIT_CARD_TYPE)));
				txt_refnumber.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)));

			}
			if (cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE)).equalsIgnoreCase(getString(R.string.check)))
			{
				layout_checkno.setVisibility(View.VISIBLE);
				txt_PaymentCheckno.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)));
			}
			if(!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_SETTLEMENTDATE)).isEmpty()){
				txt_PaymentDate.setText(Util.change_GMT_DateFormat("",(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_SETTLEMENTDATE))), checkedin_event_record.Events.Time_Zone__c));
			}else{
				txt_PaymentDate.setText(Util.change_GMT_DateFormat((cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_CREATEDDATE))),"", checkedin_event_record.Events.Time_Zone__c));
			}
			if(!cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_NOTES)).isEmpty()){
				edt_paynotes.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_NOTES)));
			}
			edt_paynotes.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View view, MotionEvent motionEvent) {
					if (motionEvent.getAction() == MotionEvent.ACTION_UP){
						if (motionEvent.getX()>(view.getWidth()-50)){
							edt_paynotes.setFocusable(true);
								//btn_save_notes.setVisibility(View.VISIBLE);
						}
					}
					return false;
				}
			});
			btn_save_notes.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(!edt_paynotes.getText().toString().trim().equals(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_NOTES)))) {
						if (isOnline()) {
							_check_status = cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS));
							_check_no = cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER));
							order_payment_id = cursor.getString(cursor
									.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID));
							payment_notes = edt_paynotes.getText().toString();
							requestType = Util.EDIT_CHECK_NO;
							doRequest();
						} else {
							startErrorAnimation(
									getResources().getString(R.string.network_error),
									txt_error_msg);
						}
					}else{
						Toast.makeText(OrderDetailActivity.this, "No changes Updated in Notes!", Toast.LENGTH_LONG).show();
					}
				}
			});
			layout_editcheckno.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					openEditCheckDialog(
							cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_REF_NUMBER)), //TODO PAYMENTITEMS replased ORDER_CHECK_NUMBER
							cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS)),
							cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ORDER_ID)),
							cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_ID)),
							cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_PAYMENT_ITEM_NOTES)));//
				}
			});
		}
	}
	public class TicketListAdapter extends CursorAdapter{

		TextView txt_ticket_name,txt_ticket_price,txt_ticket_oty,txt_ticket_total;
		Button btn_order_item_delete;


		/**
		 * @param context
		 * @param c
		 */
		public TicketListAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View view, Context context, final Cursor cursor) {


			//ticket_cursor.moveToFirst();
			txt_ticket_name.setText(Util.db.getItemName(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID))));
			txt_ticket_price.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(Double.parseDouble(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_EACH_ITEM_PRICE)))));
			txt_ticket_oty.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY)));
			txt_ticket_total.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(cursor.getDouble(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_TOTAL_PRICE))));
			/*String order_item_type =  Util.db.getItemTypeName(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_TYPE)));
			if(order_item_type.equalsIgnoreCase("Package")){
				btn_order_item_delete.setVisibility(View.GONE);
			}else*/

			if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_STATUS))).equalsIgnoreCase("Cancelled")){
				btn_order_item_delete.setVisibility(View.GONE);
			}else if(order_line_items_map.containsKey(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID)))){
				int qty = Integer.parseInt(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY)));
				int cancelled_qty = order_line_items_map.get(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID)));
				int total = qty+cancelled_qty;
				if(total == 0){
					btn_order_item_delete.setVisibility(View.GONE);
				}

			}else{
				btn_order_item_delete.setVisibility(View.VISIBLE);
			}
			if(!isEventAdmin()){
				btn_order_item_delete.setVisibility(View.GONE);
			}

			btn_order_item_delete.setTag(cursor.getPosition());

			btn_order_item_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					cursor.moveToPosition((Integer)v.getTag());
					//Log.i("------------------Item Name------------",":"+Util.db.getItemName(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_ID))));
					//Log.i("------------------Item Name------------",":"+Util.db.getItemTypeName(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_TYPE))));
					order_item_id = cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ORDER_ITEM_ID));
					orderid="";
					order_ticket_id="";
					if(isOnline()){
						openOrderDeleteDialog("Are you sure, you want to cancel "+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDERITEM_ITEM_QTY))+" tickets?");
					}else{
						startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
					}
				}
			});
		}

		/* (non-Javadoc)
		 * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View v=inflater.inflate(R.layout.order_ticket_list_item, null);
			txt_ticket_name=(TextView) v.findViewById(R.id.txt_OT_ticket_name);
			txt_ticket_price=(TextView) v.findViewById(R.id.txt_OT_ticket_price);
			txt_ticket_oty=(TextView) v.findViewById(R.id.txt_OT_ticket_oty);
			txt_ticket_total=(TextView) v.findViewById(R.id.txt_OT_ticket_total);
			btn_order_item_delete = (Button)v.findViewById(R.id.btn_order_item_cancel);
			return v;
		}

	}
	private void openEditCheckDialog(String check_no, String status,String order_id,String payment_id,String paymentnotes) {
		try{
			String _type[] = { "Check Not Received", "Paid" };
			order_payment_id=payment_id;
			payment_notes=paymentnotes;
			add_key_dialog = new Dialog(OrderDetailActivity.this,
					R.style.DialogBottomSlideAnim);
			add_key_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			add_key_dialog.setCancelable(false);
			add_key_dialog.setContentView(R.layout.edit_check_number_dialog);

			final TextView edt_att_check_no = (TextView) add_key_dialog
					.findViewById(R.id.edit_att_check_no);
			final Spinner spinner_type = (Spinner) add_key_dialog
					.findViewById(R.id.spinner_edit_check_type);
			Button btn_dialog_save = (Button) add_key_dialog
					.findViewById(R.id.btn_dialog_save);
			Button btn_dialog_cancle = (Button) add_key_dialog
					.findViewById(R.id.btn_dialog_cancle);

			edt_att_check_no.setText(check_no);
			ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
					R.layout.spinner_item_layout, _type) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					View v = super.getView(position, convertView, parent);
					((TextView) v).setTextColor(getResources().getColor(
							R.color.black));
					((TextView) v).setTypeface(Util.roboto_regular);
					return v;
				}
			};
			spinner_type.setAdapter(typeAdapter);

			for (int i = 0; i < _type.length; i++) {
				if (_type[i].equals(status)) {
					spinner_type.setSelection(i);
				}
			}

			// _seat_no=check_no;
			btn_dialog_save.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					_check_no = edt_att_check_no.getText().toString();
					_check_status = spinner_type.getSelectedItem().toString();
					hideKeybord(edt_att_check_no);
					/*if(_check_no.trim().isEmpty()&&_check_status.equalsIgnoreCase("Paid")){
						edt_att_check_no.setError("Please enter check number.");
					}else*/
					if (isOnline()) {
						requestType = Util.EDIT_CHECK_NO;
						doRequest();
						add_key_dialog.dismiss();
					}else {
						add_key_dialog.dismiss();
						startErrorAnimation(
								getResources().getString(R.string.network_error),
								txt_error_msg);
					}

				}

		});

		btn_dialog_cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				hideKeybord(edt_att_check_no);
				// InputMethodManager imm = (InputMethodManager)
				// getSystemService(Context.INPUT_METHOD_SERVICE);
				// imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
				// 0);

				add_key_dialog.dismiss();
			}
		});

		add_key_dialog.show();
	}catch (Exception e){
		e.printStackTrace();
	}
}

}
