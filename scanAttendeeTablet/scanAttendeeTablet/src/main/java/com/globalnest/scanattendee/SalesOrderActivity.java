package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.globalnest.BackgroundReciver.CheckServiceRunning;
import com.globalnest.BackgroundReciver.DownloadResultReceiver;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.TicketListResponseHandler;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.scroll.PoppyViewHelper;
import com.globalnest.scroll.PoppyViewHelper.PoppyViewPosition;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.PullToRefreshListView.OnRefreshListener;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import RLSDK.ad;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SalesOrderActivity extends BaseActivity implements OnClickListener , DownloadResultReceiver.Receiver{

	// ============================== Activity Constants ==========================//

	private final static String ALL = "all";
	private final static String CASH = "Cash";
	private final static String CHECK = "Check";
	private final static String AUTHNET = "Authorize.net";
	private final static String TRUSTCOMMERCE = "Trustcommerce";
	private final static String STRIPE = "Stripe";
	private final static String FREEPAY = "Free";
	private final static String EXTERNAL_PAY="External Pay Gateway";
	private final static String PAYPAL = "PayPal";
	private final static String PAID="Paid";
	private final static String NOTPAID="Not Paid";
	private final static String CANCELLED="Cancelled";

	//	private final static String NOTPAID="Cancelled";
	//	private final static String PAID="Fully Paid";

	private final int CREATE_NEW_DIALOG = 0;
	private final int CREATE_TICKET_DIALOG = 3;
	private final int CREATE_CONFIRM_DIALOG = 1;
	private final int REFUND_ASK_DIALOG = 2;

	private boolean isLoadMore=false;

	// ====================== PRE-DEFINED DATA-TYPES =======================//

	private String whereClause = "", filter_by = "ALL", requestType = "";
	private double total_amt = 0;

	// ======================= ANDROID DATA-TYPES ======================//

	private LinearLayout sort_layout, filter_layout;
	private TextView txt_no_amt, txt_filter,txtLoadingmore,txttotallabel;
	private PullToRefreshListView order_view;
	private Cursor sales_cursor;
	private Cursor orderpayment_cursor;
	private View loadMoreView;
	private ExpandableListAdapter adapter;
	private Dialog orderFilter_dialog;
	private String selected_paymentmode="",selected_orderStatus,selected_paymentstatus,selected_registrationtype,filterwhereclause="";
	int paymode_pos=0,paystatus_pos=0,regtype_pos=0,orderstatus_pos=0;
	private ArrayAdapter<String> paymentmodeAdapter,regTypeAdapter,paymentstatusAdapter,orderstatusAdapter;
	private PoppyViewHelper mPoppyViewHelper;
	private View poppyView;
	private Dialog open_ticket_dialog;
	private LinearLayout btn_order_load_more;

	// ==================== OTHER OBJECT ========================//

	private HttpPostData postMethod;
	private  ProgressDialog dialog;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.sales_order_layout);

		dialog = new ProgressDialog(SalesOrderActivity.this);
		dialog.setMessage("Please wait loading orders... ");
		dialog.setCancelable(false);
		if (Util.isMyServiceRunning(DownloadService.class, SalesOrderActivity.this)) {
			requestType = Util.CHECK_SERVICE;
			dialog.show();
			doRequest();
		}else{
			getTicketCursor();
		}



		img_refund_history.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(SalesOrderActivity.this,
						RefundHistoryActivity.class));
			}
		});

		img_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					back_layout.setVisibility(View.VISIBLE);
					search_layout.setVisibility(View.GONE);
					hideSoftKeyboard(SalesOrderActivity.this);
					search_view.setText("");
					whereClause = " where (orders." + DBFeilds.ORDER_BUYER_ID + " = user." + DBFeilds.USER_USERID + ") AND orders."
							+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId + "'order by orders.Order_Date DESC";
					sales_cursor = Util.db.getPaymentDataCursor(whereClause);
					if (sales_cursor.getCount() > 0) {
						setAdapter();
					}
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		});
		//TODO nextday
		img_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				back_layout.setVisibility(View.GONE);
				search_layout.setVisibility(View.VISIBLE);
				search_view.setFocusable(true);
				search_view.requestFocus();
				if (search_view.requestFocus()) {
					InputMethodManager imm = (InputMethodManager)
							getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(search_view, InputMethodManager.SHOW_IMPLICIT);
				}
				//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				/*InputMethodManager imm = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(search_view ,
						InputMethodManager.SHOW_IMPLICIT);*/
			}
		});
		search_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_view.getText().toString().trim().isEmpty()) {
					search_view.setHint("First Name,Last Name,Company,Order Name,Mail id");
					requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
					doRequest();
					//SortFunction(R.id.editsearchrecord, is_buyer);
					return true;
				}else if(search_view.getText().toString().trim().isEmpty()){
					Toast.makeText(SalesOrderActivity.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
				}
				return false;
			}
		});
		search_view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if (motionEvent.getAction() == MotionEvent.ACTION_UP){
					if (motionEvent.getX()>(view.getWidth()-50)){
						search_view.setText("");
						/*att_cursor=null;
						adapter = new SelfCheckinAttendeeList.AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
						attendee_selfview.setAdapter(adapter);
						attendee_selfview.setVisibility(View.GONE);
						hideSoftKeyboard(SelfCheckinAttendeeList.this);
						setselfcheckinbuttons();*/
					}
				}
				return false;
			}
		});
		search_view.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {

				search_view.setFocusable(true);
				if(before<count) {
					doSearch();
				}
				// adapter = new ExpandableListAdapter(sales_cursor,
				// SalesOrderActivity.this);

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		order_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterview, View view,
									int position, long arg3) {
				try {
					Cursor c = adapter.getCursor();
					c.moveToPosition(position);
					Intent i = new Intent(SalesOrderActivity.this,
							OrderDetailActivity.class);
					i.putExtra(Util.ORDER_ID,
							c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
					startActivity(i);
				}catch (Exception e){
					e.printStackTrace();
					Cursor c = adapter.getCursor();
					c.moveToPosition(position-1);
					Intent i = new Intent(SalesOrderActivity.this,
							OrderDetailActivity.class);
					i.putExtra(Util.ORDER_ID,
							c.getString(c.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
					startActivity(i);

				}
			}

		});

		btn_order_load_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (isOnline()) {
					requestType = "Order";
					isLoadMore=true;
					txtLoadingmore.setText("Loading....");
					doRequest();
				} else {
					startErrorAnimation(
							getResources().getString(R.string.network_error),
							txt_error_msg);
				}

			}
		});

		order_view.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {

				requestType = Util.ITEMS_ORDERS_REFRESH;
				if(isOnline()){
					doRequest();
				}else{
					order_view.onRefreshComplete();
					//startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
				}


			}
		});

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int count = Util.db.totalOrderCount(checked_in_eventId);
				if(!isOnline()){
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				}else if(count == Util.dashboardHandler.totalOrders){
					AlertDialogCustom dialog = new AlertDialogCustom(SalesOrderActivity.this);
					dialog.setParamenters("Alert", "All orders are downloaded.", null, null, 1, false);
					dialog.show();
				}else{
					requestType = "Order";
					isLoadMore=true;
					txtLoadingmore.setText("Loading....");
					doRequest();
				}

			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		try{
			hideSoftKeyboard(SalesOrderActivity.this);
		}catch (Exception e){
			e.printStackTrace();
		}		/*if(sales_cursor != null){
			AppUtils.displayLog("---------------On Resume--------",":"+sales_cursor);
			sales_cursor.close();
			whereClause = " where (orders." + DBFeilds.ORDER_BUYER_ID + " = user."
					+ DBFeilds.USER_USERID + ") AND orders."
					+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
					+ "'order by orders.Order_Date DESC";
			sales_cursor = Util.db.getPaymentDataCursor(whereClause);
			txt_title.setText("Orders ("+sales_cursor.getCount()+")"     );
			txttotallabel.setText("Total Amount : "+Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+" "+String.format( "%.2f",getTotalAmount(sales_cursor)));
			if(adapter != null){
				adapter.changeCursor(sales_cursor);
			}
		}*/

	}

	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Util.slide_menu_id = 0;
	}

	@Override
	protected void onStart() {
		super.onStart();
		/*FlurryAgent.onStartSession(SalesOrderActivity.this,
				ScannerSettingsApplication.IMPROVE_APP_ID);*/
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}

	private void getOrderCursor() {
		//Log.i("----------------Total Order Count--------------",":"+Util.db.totalOrderCount(checked_in_eventId));
		whereClause = " where (orders." + DBFeilds.ORDER_BUYER_ID + " = user." + DBFeilds.USER_USERID + ") AND orders."
				+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId + "'order by orders.Order_Date DESC";
		sales_cursor = Util.db.getPaymentDataCursor(whereClause);
		if (sales_cursor.getCount() > 0) {
			setAdapter();
		} else {
			requestType = "Order";
			if (isOnline()){
				doRequest();
			}else{
				startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
			}
		}
	}

	private void getTicketCursor() {
		whereClause = " where EventId='" + checked_in_eventId + "'";
		Cursor c_ticket = Util.db.getTicketCursor(whereClause);
		if (c_ticket.getCount() > 0) {
			getOrderCursor();
		} else {
			if (isOnline())
				doRequest();
			else
				startErrorAnimation(getResources().getString(R.string.network_error),
						txt_error_msg);

		}
	}

	private void setAdapter() {
		txt_title.setText("");
		whereClause = " where (orders." + DBFeilds.ORDER_BUYER_ID + " = user."
				+ DBFeilds.USER_USERID + ") AND orders."
				+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
				+ "'order by orders.Order_Date DESC";
		if(sales_cursor != null){
			sales_cursor.close();
		}
		sales_cursor = Util.db.getPaymentDataCursor(whereClause);
		if(checkedin_event_record.Events.Revenue_visibility__c.equalsIgnoreCase("false")){
			txttotallabel.setText("Total Orders: "+sales_cursor.getCount());
		}else {
			txttotallabel.setText("Total Amount : " + Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + String.format("%.2f", getTotalAmount(sales_cursor)));
		}
		txt_title.setText("Orders"+" ("+sales_cursor.getCount()+")"     );
		if (sales_cursor.getCount() > 0) {
			txt_no_amt.setVisibility(View.GONE);
			// txt_label.setVisibility(View.VISIBLE);
			poppyView.setVisibility(View.VISIBLE);
			adapter = new ExpandableListAdapter(sales_cursor, SalesOrderActivity.this);
			order_view.setAdapter(adapter);
			if (isLoadMore) {
				isLoadMore = false;
				txtLoadingmore.setText("Load More");
				//adapter.swapCursor(sales_cursor);
				//adapter.notifyDataSetChanged();
			}
		} else {
			// txt_label.setVisibility(View.GONE);
			poppyView.setVisibility(View.GONE);
			txt_no_amt.setVisibility(View.VISIBLE);
		}
		whereClause = " where Event_Id='" + checked_in_eventId + "'";
		total_amt = Util.db.getPaymentAmount(whereClause);


		if(sales_cursor.getCount()<20)
			btn_order_load_more.setVisibility(View.GONE);
	}

	@Override
	public void doRequest() {
		//loading_layout.setVisibility(View.GONE);
		String access_token = sfdcddetails.token_type + " "
				+ sfdcddetails.access_token;
		if (requestType.equalsIgnoreCase("Order")) {
			String offset = Util.offset_pref.getString(checked_in_eventId, "");
			String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
					+ checked_in_eventId + "&User_id=" + sfdcddetails.user_id
					+ "&offset=" + offset + "&limit="
					+ checkedin_event_record.Events.scan_attendee_limit__c;
			
			/*String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_GET_ORDER_LIST + "Event_id="
					+ checked_in_eventId + "&User_id=" + sfdcddetails.user_id;
					/*+ "&offset=" + sales_cursor.getCount() + "&limit="
					+ checkedin_event_record.Events.scan_attendee_limit__c;*/
			postMethod = new HttpPostData("Loading Orders...",url, null, access_token,
					SalesOrderActivity.this);
			postMethod.execute();

		} else if(requestType.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)){

			dialog = new ProgressDialog(SalesOrderActivity.this);
			dialog.setMessage("Please wait refreshing orders...");
			dialog.setCancelable(false);
			dialog.show();
			DownloadResultReceiver mReceiver = new DownloadResultReceiver(new Handler());
			mReceiver.setReceiver(SalesOrderActivity.this);
			Intent intent = new Intent(Intent.ACTION_SYNC, null, SalesOrderActivity.this, DownloadService.class);
			String ticURL = sfdcddetails.instance_url + WebServiceUrls.SA_REFRESH_URL + getValues("1");
			intent.putExtra(DownloadService.ATT_URL, "");
			intent.putExtra(DownloadService.TIC_URL, ticURL);
			intent.putExtra(DownloadService.ACCESSTOKEN, sfdcddetails.token_type + " " + sfdcddetails.access_token);
			intent.putExtra(DownloadService.EVENTID, checked_in_eventId);
			intent.putExtra(DownloadService.REQUESTTYPE, "Refresh");
			intent.putExtra(DownloadService.BADGE_URL, sfdcddetails.user_id);
			String url[] = getValues("1").split("LastModifiedDate=");
			url=url[1].split("&Request_Flag");
			intent.putExtra(DownloadService.LASTMODIFIEDDATE, NullChecker(url[0]));
			intent.putExtra(DownloadService.RECEIVER, mReceiver);
			intent.putExtra(DownloadService.ACTIVITY_NAME, SalesOrderActivity.class.getName());
			intent.putExtra(DownloadService.reload, "");
			intent.putExtra("requestId", 101);
			startService(intent);
		  /* String url = sfdcddetails.instance_url+WebServiceUrls.SA_REFRESH_URL+getValues();
			postMethod = new HttpPostData("Refreshing Orders...",url, null, access_token,	SalesOrderActivity.this);
			postMethod.execute();*/
		}else if(requestType.equalsIgnoreCase(Util.CHECK_SERVICE)){
			doCheckBackgroundService();
		}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SEARCH_ATTENDEE)){
			String url = sfdcddetails.instance_url+WebServiceUrls.SA_SEARCH_ATTENDEE+getSearchValues();
			postMethod = new HttpPostData("Loading Search...",url, null, access_token,	SalesOrderActivity.this);
			postMethod.execute();
		} else {
			String url = sfdcddetails.instance_url
					+ WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="
					+ checked_in_eventId;
			postMethod = new HttpPostData("Loading Tickets...",url, null, access_token,
					SalesOrderActivity.this);
			postMethod.execute();
		}

	}
	private String getValues(String batchnumber){
		//Log.i("-------------Event Last Refresh Date---------------",":"+checkedin_event_record.lastRefreshDate);
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("appname", ""));
		//values.add(new BasicNameValuePair("LastModifiedDate", "2017-09-16+13%3A23%3A34"));
		if(NullChecker(Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"")).trim().isEmpty()){
			values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
		}else {
			values.add(new BasicNameValuePair("LastModifiedDate", Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"")));
		}
		values.add(new BasicNameValuePair("Request_Flag", "Itemandattendees"));
		values.add(new BasicNameValuePair("startbatch", batchnumber));
		return AppUtils.getQuery(values);
	}
	@Override
	public void parseJsonResponse(String response) {

		try {
			Gson gson = new Gson();
			if (requestType.equalsIgnoreCase("Order")) {

				TotalOrderListHandler totalorderlisthandler = gson.fromJson(
						response, TotalOrderListHandler.class);
				Util.db.upadteOrderList(totalorderlisthandler.TotalLists,
						checked_in_eventId);
				if(totalorderlisthandler.TotalLists.size() > 0){
					String last_record_id = totalorderlisthandler.TotalLists
							.get(totalorderlisthandler.TotalLists.size() - 1).orderInn.getOrderId();
					Util._saveDataPreference(Util.offset_pref, checked_in_eventId, last_record_id);
				}
				//loading_layout.setVisibility(View.GONE);
				//loadingMore = false;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(order_view.isRefreshing()){
							order_view.onRefreshComplete();
						}
						setAdapter();
					}
				});

			} else if(requestType.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)){

				RefreshResponse refresh=gson.fromJson(response, RefreshResponse.class);
				//TicketListResponseHandler items[] =   refresh.BLN_ASC_ItemsListOUTPUT.toArray(new TicketListResponseHandler[refresh.BLN_ASC_ItemsListOUTPUT.size()]);
				Util.db.upadteItemListRecordInDB(refresh.BLN_ASC_ItemsListOUTPUT,checked_in_eventId);
				Util.lastModifideDate.edit().putString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,refresh.LastRefreshedDate).commit();
				Util.db.updateEventRefreshDate(checked_in_eventId, refresh.LastRefreshedDate);
				if(refresh.TotalLists != null){
					Util.db.upadteOrderList(refresh.TotalLists, checked_in_eventId);
				}
				if(refresh.ticketTags != null){
					Util.db.InsertAndUpdateTicketTag(refresh.ticketTags);
				}
				order_view.onRefreshComplete();
				setAdapter();
			}else if(requestType.equals(Util.CHECK_SERVICE)){
				if (response.equals("true")) {
					doCheckBackgroundService();
				}else{
					dialog.dismiss();
					setAdapter();
				}

			}else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_SEARCH_ATTENDEE)){
				TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);

				//Log.i("---ORDER ARRAY SIZE---", "Server" + totalorderlisthandler.TotalLists.size());
				if (totalorderlisthandler.TotalLists.size() > 0) {
					Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
					/*String last_record_id = totalorderlisthandler.TotalLists
							.get(totalorderlisthandler.TotalLists.size() - 1).orderInn.getOrderId();
					Util._saveDataPreference(Util.offset_pref, Util.OFFSET_STRING, last_record_id);*/
				}else{
					Toast.makeText(SalesOrderActivity.this, "No Attendee Found with "+search_view.getText().toString().trim()+"!", Toast.LENGTH_LONG).show();
					/*AlertDialogCustom dialog = new AlertDialogCustom(
							SalesOrderActivity.this);
					dialog.setParamenters("Alert", "Sorry! There are no orders with "+search_view.getText().toString().trim()+".", null,
							null, 1, false);
					dialog.show();*/
				}
				//doSearch();

			}else {
				ItemsListResponse items_response = gson.fromJson(response, ItemsListResponse.class);
				/*TicketListResponseHandler[] responseHandler = gson.fromJson(
						response, TicketListResponseHandler[].class);*/
				Util.db.upadteItemListRecordInDB(items_response.Itemscls_infoList,checked_in_eventId);
				Util.db.InsertAndUpdateSEMINAR_AGENDA(items_response.agndaInfo);
				//loading_layout.setVisibility(View.GONE);

				getOrderCursor();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(order_view.isRefreshing()){
						order_view.onRefreshComplete();
					}
					startErrorAnimation(
							getResources().getString(R.string.network_error1),
							txt_error_msg);
				}
			});
			//loading_layout.setVisibility(View.GONE);

		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		//Log.i("-----CONFIRM CASE---", "" + id);
		switch (id) {
			case CREATE_CONFIRM_DIALOG:
				return ask_dialog;
			case CREATE_TICKET_DIALOG:
				return open_ticket_dialog;
			default:
				return null;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {

		super.onPrepareDialog(id, dialog, bundle);

		switch (id) {
			case CREATE_NEW_DIALOG:
				break;
			case CREATE_CONFIRM_DIALOG:
				break;
			case REFUND_ASK_DIALOG:
				break;
			case CREATE_TICKET_DIALOG:
				break;

		}
	}

	public void openOrderFilterDialog(Context ctx) {
		orderFilter_dialog = new Dialog(ctx);
		orderFilter_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		orderFilter_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		orderFilter_dialog.setContentView(R.layout.orderslist_filter);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = orderFilter_dialog.getWindow();
		lp.copyFrom(window.getAttributes());
		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		orderFilter_dialog.getWindow().setAttributes(lp);
		final Spinner PayModetype = (Spinner) orderFilter_dialog
				.findViewById(R.id.spnr_item_paytype);
		final Spinner OrderStatus = (Spinner) orderFilter_dialog
				.findViewById(R.id.spnr_order_status);
		final Spinner RegType = (Spinner) orderFilter_dialog
				.findViewById(R.id.spnr_reg_type);
		final Spinner PayStatus = (Spinner) orderFilter_dialog
				.findViewById(R.id.spnr_pay_status);

		Button done = (Button) orderFilter_dialog.findViewById(R.id.btn_filter_done);
		paymentmodeAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Util.db.getEventPaymentFiterItems(checked_in_eventId,DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE));
		PayModetype.setAdapter(paymentmodeAdapter);

		regTypeAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Util.db.getEventPaymentFiterItems(checked_in_eventId,DBFeilds.ORDER_PAYMENT_ITEM_PAY_REG_TYPE));
		RegType.setAdapter(regTypeAdapter);

		paymentstatusAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Util.db.getEventPaymentFiterItems(checked_in_eventId,DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS));
		PayStatus.setAdapter(paymentstatusAdapter);

		orderstatusAdapter=new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Util.db.getEventOrderStatusFiterItems(checked_in_eventId,DBFeilds.ORDER_ORDER_STATUS));
		OrderStatus.setAdapter(orderstatusAdapter);
		PayModetype.setSelection(paymode_pos);
		OrderStatus.setSelection(orderstatus_pos);
		RegType.setSelection(regtype_pos);
		PayStatus.setSelection(paystatus_pos);
		orderFilter_dialog.show();
		PayModetype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selected_paymentmode = PayModetype.getSelectedItem().toString();
				paymode_pos=position;
				if(!selected_paymentmode.equalsIgnoreCase("All")){
					selected_paymentmode="' AND orderpayment." + DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE + " = '"
							+ selected_paymentmode;
				}else selected_paymentmode="";
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		OrderStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selected_orderStatus = OrderStatus.getSelectedItem().toString();
				orderstatus_pos=position;
				if(!selected_orderStatus.equalsIgnoreCase("All")){
					selected_orderStatus="' AND orders." + DBFeilds.ORDER_ORDER_STATUS + " = '"
							+ selected_orderStatus;
				}else selected_orderStatus="";
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
		RegType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selected_registrationtype = RegType.getSelectedItem().toString();
				regtype_pos=position;
				if(!selected_registrationtype.equalsIgnoreCase("All")){
					selected_registrationtype="' AND orderpayment." + DBFeilds.ORDER_PAYMENT_ITEM_PAY_REG_TYPE + " = '"
							+ selected_registrationtype;
				}else selected_registrationtype="";
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		PayStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				selected_paymentstatus = PayStatus.getSelectedItem().toString();
				paystatus_pos=position;
				if(!selected_paymentstatus.equalsIgnoreCase("All")){
					selected_paymentstatus="' AND orderpayment." + DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS + " = '"
							+ selected_paymentstatus;
				}else selected_paymentstatus="";
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				orderFilter_dialog.dismiss();
				filterwhereclause=selected_orderStatus+selected_paymentmode+selected_registrationtype+selected_paymentstatus;
				whereClause = " where orders." + DBFeilds.ORDER_BUYER_ID
						+ " = user." + DBFeilds.USER_USERID
						+ " AND orderpayment.payment_order_id=orders.Order_Id"
						+" AND orders." + DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
						+filterwhereclause+ "' order by orders.Order_Date DESC";
				getFilterOrderCursor(whereClause);
			}
		});


	}

	public void openFilterDialog(Context ctx) {
		orderFilter_dialog = new Dialog(ctx);
		orderFilter_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		orderFilter_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		orderFilter_dialog.setContentView(R.layout.filter_order_dialog_layout);

		// Grab the window of the dialog, and change the width
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		Window window = orderFilter_dialog.getWindow();
		lp.copyFrom(window.getAttributes());
		// This makes the dialog take up the full width
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
		orderFilter_dialog.getWindow().setAttributes(lp);

		TextView txt_filterAll = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_all);
		TextView txt_filterCash = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_cash);
		TextView txt_filterCheck = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_check);
		TextView txt_filterPayPal = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_paypal);
		TextView txt_filterTCommerse = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_tcommerce);
		TextView txt_filterAuthNet = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_authnet);
		TextView txt_filterStripe = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_stripe);
		TextView txt_filterPaid = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_paid);
		TextView txt_filterNotPaid = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_notPaid);
		TextView txt_filterCancelled = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_cancelled);
		txt_filterNotPaid.setVisibility(View.VISIBLE);
		TextView txt_filterCancel = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_cancle);
		TextView txt_filterFreePay = (TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_free);
		TextView txt_filterExterPay =(TextView) orderFilter_dialog
				.findViewById(R.id.txt_filter_extPay);
		TextView txt_creditcard = (TextView)orderFilter_dialog.findViewById(R.id.txt_filter_creditcard);

		orderFilter_dialog.show();
		txt_filterCancel.setText("QUIT");
		txt_filterCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				orderFilter_dialog.dismiss();
			}
		});

		txt_filterAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//Log.i("---SORT1 CLICKED----", ":");
				total_amt = 0;
				filter_by = ALL;
				orderFilter_dialog.dismiss();
				FilterOrder("", "");
			}
		});
		txt_filterCash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = CASH;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, filter_by);
			}
		});
		txt_filterCheck.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = CHECK;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, filter_by);
			}
		});

		txt_filterTCommerse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = TRUSTCOMMERCE;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME, filter_by);
			}
		});

		txt_filterAuthNet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				//Log.i("---SORT1 CLICKED----", ":");
				total_amt = 0;
				filter_by = AUTHNET;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME, filter_by);
			}
		});

		txt_filterPayPal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = PAYPAL;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME, filter_by);
			}
		});

		txt_filterStripe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = STRIPE;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAY_GATEWAY_NAME, filter_by);
			}
		});

		txt_filterFreePay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = FREEPAY;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, filter_by);
			}
		});

		txt_filterExterPay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = EXTERNAL_PAY;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, filter_by);
			}
		});

		txt_filterPaid.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = PAID;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS, filter_by);
			}
		});
		txt_filterCancelled.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				total_amt = 0;
				filter_by = CANCELLED;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_ORDER_STATUS, filter_by);
				//FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS, filter_by);

			}
		});
		txt_filterNotPaid.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				total_amt = 0;
				filter_by = NOTPAID;
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_STATUS, filter_by);
			}
		});
		txt_creditcard.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				total_amt = 0;
				filter_by = getString(R.string.creditcard);
				orderFilter_dialog.dismiss();
				FilterOrder(DBFeilds.ORDER_PAYMENT_ITEM_PAYMENT_MODE, filter_by);
			}
		});

	}

	private void doSearch(){
		String sString= search_view.getText().toString().toLowerCase().trim();
		if(sString.contains("'")){
			sString=sString.replace("'","''");
		}
		String fName=DBFeilds.USER_FIRST_NAME;
		String lName=DBFeilds.USER_LAST_NAME;
		String oNmae=DBFeilds.ORDER_ORDER_NAME;
		String company=DBFeilds.USER_COMPANY;
		String email=DBFeilds.USER_EMAIL_ID;
		if (!search_view.getText().toString().trim().isEmpty()) {
			whereClause = " where orders.Buyer_Id = user.UserID"
					+ " AND orders.Event_Id = '" + checked_in_eventId
					+ "' AND (user." + fName + " like '"+ "%" + sString+ "%"
					+ "' OR user."+lName+" like '%"+sString+"%"
					+ "' OR user."+company+" like '%"+sString+"%"
					+ "' OR orders."+oNmae+" like '%"+sString+"%"
					//+ "' OR CONCAT(TRIM(user."+fName+"), ' ', TRIM(user."+lName+")) like '%"+sString+"%"
					+ "' OR user."+fName+' '+"|| user."+lName+" like '%"+sString.replace(" ", "")+"%"
					+ "' OR user."+email+" like '%"+sString+"%')";

			getFilterOrderCursor(whereClause);
		}else{
			whereClause = " where orders." + DBFeilds.ORDER_BUYER_ID
					+ " = user." + DBFeilds.USER_USERID + " AND orders."
					+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
					+ "' order by orders.Order_Date DESC";
			getFilterOrderCursor(whereClause);
		}
	}

	private void FilterOrder(String DBField, String filterBy) {
		if(DBField.isEmpty()){
			whereClause = " where orders." + DBFeilds.ORDER_BUYER_ID
					+ " = user." + DBFeilds.USER_USERID + " AND orders."
					+ DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
					+ "' order by orders.Order_Date DESC";
			getFilterOrderCursor(whereClause);
		} else if(filterBy.equalsIgnoreCase(CANCELLED)){
			whereClause = " where orders." + DBFeilds.ORDER_BUYER_ID
					+ " = user." + DBFeilds.USER_USERID
					+ " AND orderpayment.payment_order_id=orders.Order_Id"
					+" AND orders." + DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
					+"' AND orders." + DBField + " = '"
					+ filterBy + "' order by orders.Order_Date DESC";
			getFilterOrderCursor(whereClause);
		}else{
			whereClause = " where orders." + DBFeilds.ORDER_BUYER_ID
					+ " = user." + DBFeilds.USER_USERID
					+ " AND orderpayment.payment_order_id=orders.Order_Id"
					+" AND orders." + DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId
					+"' AND orderpayment." + DBField + " = '"
					+ filterBy + "' order by orders.Order_Date DESC";
			getFilterOrderCursor(whereClause);//,filterBy
		}
	}

	private void getFilterOrderCursor(String whereClause){//,String filterBy {
		try {
			txt_title.setText("");
			sales_cursor = Util.db.getPaymentDataCursor(whereClause);
			if(checkedin_event_record.Events.Revenue_visibility__c.equalsIgnoreCase("false")){
				txttotallabel.setText("Total Orders: "+sales_cursor.getCount());
			}else {
				txttotallabel.setText("Total Amount : " + Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + " " + String.format("%.2f", getTotalAmount(sales_cursor)));
			}			txt_title.setText("Orders ("+sales_cursor.getCount()+")"     );
			adapter = new ExpandableListAdapter(sales_cursor,
					SalesOrderActivity.this);
			order_view.setAdapter(adapter);
			if (sales_cursor.getCount() == 0) {
				if(!NullChecker(search_view.getText().toString()).isEmpty()){
					/*requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
					doRequest();*/
				}else{
					final AlertDialogCustom dialog = new AlertDialogCustom(
							SalesOrderActivity.this);
					dialog.setParamenters("Alert", "Sorry! There are no orders ", null,//with "+filterBy+".
							null, 1, false);
					dialog.btnOK.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							setAdapter();
							dialog.dismiss();
						}
					});
					dialog.show();
				}
				adapter = new ExpandableListAdapter(sales_cursor,
						SalesOrderActivity.this);
				order_view.setAdapter(adapter);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private String getSearchValues(){
		List<NameValuePair> values=new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
		values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
		values.add(new BasicNameValuePair("search_string", search_view.getText().toString()));
		return AppUtils.getQuery(values);
	}
	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		Util.slide_menu_id = R.id.orderlayout;
		txt_title.setText("Orders");
		img_menu.setImageResource(R.drawable.top_more);
		img_setting.setVisibility(View.GONE);
		img_setting.setVisibility(View.GONE);
		event_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		img_refund_history.setVisibility(View.GONE);
		img_search.setVisibility(View.VISIBLE);
		img_addticket.setVisibility(View.GONE);
		img_addticket.setImageResource(R.drawable.loadmore);

		// ====================== ORDER LAYOUT VIEWS ======================//


		txt_no_amt = (TextView) linearview.findViewById(R.id.noamt);
		txttotallabel = (TextView)linearview.findViewById(R.id.txttotallabel);
		order_view = (PullToRefreshListView) linearview
				.findViewById(R.id.salesListView);
		search_view.setHint("First Name,Last Name,Company,Order Name,Mail id");
		//========================= POPPY VIEWS ========================//

		mPoppyViewHelper = new PoppyViewHelper(this, PoppyViewPosition.BOTTOM);
		poppyView = mPoppyViewHelper.createPoppyViewOnListView(
				R.id.salesListView, R.layout.header_layout,
				new AbsListView.OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsListView view,
													 int scrollState) {
						//Log.d("ListViewActivity", "onScrollStateChanged");
					}

					@Override
					public void onScroll(AbsListView view,
										 int firstVisibleItem, int visibleItemCount,
										 int totalItemCount) {
						//Log.d("ListViewActivity", "onScroll");
					}
				});
		sort_layout = (LinearLayout) poppyView
				.findViewById(R.id.ordersortlayout);
		filter_layout = (LinearLayout) poppyView
				.findViewById(R.id.orderfilterlayout);
		// txt_sort = (TextView) poppyView.findViewById(R.id.txtsortorder);
		txt_filter = (TextView) poppyView.findViewById(R.id.txtfilterorder);
		sort_layout.setVisibility(View.GONE);

		// ======================================== CLICK-LISTENER ==========================================//

		poppyView.setVisibility(View.GONE);
		filter_layout.setOnClickListener(this);
		back_layout.setOnClickListener(this);
		loadMoreView = ((LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.load_more_footer, null, false);
		btn_order_load_more = (LinearLayout) loadMoreView
				.findViewById(R.id.loadmoreLay);

		txtLoadingmore = (TextView) loadMoreView
				.findViewById(R.id.txt_loading);
		order_view.addFooterView(loadMoreView);
	}
	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		String isFrom = resultData.getString("isFrom");
		if (resultCode == DownloadService.STATUS_FINISHED) {
			if(isFrom !=null && isFrom.equals("Refresh")){
				dialog.dismiss();
				if(order_view.isRefreshing()){
					order_view.onRefreshComplete();
					paymode_pos=0;paystatus_pos=0;regtype_pos=0;orderstatus_pos=0;
				}
				setAdapter();
				/*if (buyers_list.isRefreshing()) {
					buyers_list.onRefreshComplete();
				}
				if (!is_buyer) {
					UpdateAttendeeCursor();
				} else {
					UpdateBuyerCursor();
				}*/
			}/*else{
				loadAttendeesInBackground();
			}*/
		}else if(isFrom !=null && isFrom.equals("Refresh") && resultCode == DownloadService.STATUS_RUNNING) {
			if(order_view.isRefreshing()){
				order_view.onRefreshComplete();
			}
			setAdapter();
		}

	}
	/*@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		*//*String isFrom = resultData.getString("isFrom");
		if (resultCode == DownloadService.STATUS_FINISHED) {
			if (isFrom != null && isFrom.equals("Refresh")) {
				dialog.dismiss();
				order_view.onRefreshComplete();
				setAdapter();
			}
		}*//*
	}*/

	private class ExpandableListAdapter extends CursorAdapter {

		private class ViewHolder {

			TextView pDate, pAmount, personName, chequeNum, txt_status,
					txt_order_id, compName, email, pay_type;
			// Button img_expend;
		}

		@SuppressWarnings("deprecation")
		public ExpandableListAdapter(Cursor cursor, Context context) {
			super(context, cursor);

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.CursorAdapter#bindView(android.view.View,
		 * android.content.Context, android.database.Cursor)
		 */
		@Override
		public void bindView(View parent_view, Context context, Cursor cursor) {
			final View v;
			if (parent_view==null) {
				v = newView(context, cursor, null);
			}else{
				v = parent_view;
			}


			final ViewHolder holder = (ViewHolder) v.getTag();

			holder.txt_order_id.setText("Order Id: "+ cursor.getString(cursor.getColumnIndex("Order_Name")));
			try {
				String order_datetime = Util.change_US_ONLY_DateFormatWithSEC(cursor.getString(cursor.getColumnIndex("Order_Date")), checkedin_event_record.Events.Time_Zone__c);
				holder.pDate.setText(order_datetime);
			} catch (Exception e) {
				e.printStackTrace();
			}

			holder.email.setText(cursor.getString(cursor.getColumnIndex("EmailID")));
			if(cursor.getDouble(cursor.getColumnIndex("Order_Total"))!=0)
				holder.pAmount.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(cursor.getDouble(cursor
						.getColumnIndex("Order_Total"))));
			else
				holder.pAmount.setText("Free");
			holder.personName.setText(Html.fromHtml("<b>"+ NullChecker(cursor.getString(cursor.getColumnIndex("FirstName")))+ " " +  NullChecker(cursor.getString(cursor.getColumnIndex("LastName")))+ "</b>"));
			holder.txt_status.setText(cursor.getString(cursor.getColumnIndex("Order_Status")));
			holder.pay_type.setText("Reg Type: "+ cursor.getString(cursor.getColumnIndex("registration_type")));

	       /*if (!NullChecker(cursor.getString(cursor.getColumnIndex("Fee_Amount"))).isEmpty()) {

				// holder.feeAmount.setText("Fee: "+Util.nf.format(cursor.getDouble(cursor.getColumnIndex("Fee_Amount"))));
			}*/

			if (!NullChecker(cursor.getString(cursor.getColumnIndex("Company"))).isEmpty()){
				holder.compName.setText(cursor.getString(cursor.getColumnIndex("Company")));
			}else{
				holder.compName.setVisibility(View.GONE);
			}

		}

		/*
		 * (non-Javadoc)
		 *
		 * @see android.widget.CursorAdapter#newView(android.content.Context,
		 * android.database.Cursor, android.view.ViewGroup)
		 */
		@Override
		public View newView(Context arg0, Cursor cursor, ViewGroup parent) {

			View v = inflater.inflate(R.layout.sales_order_item_layout, null);
			ViewHolder holder = new ViewHolder();
			holder.txt_order_id = (TextView) v.findViewById(R.id.salesorderid);
			// holder.img_expend = (Button)
			// v.findViewById(R.id.imgexpendtickets);
			holder.pDate = (TextView) v.findViewById(R.id.purdate);
			holder.pAmount = (TextView) v.findViewById(R.id.purMoney);
			holder.txt_status = (TextView) v.findViewById(R.id.order_status);
			holder.personName = (TextView) v.findViewById(R.id.personName);
			holder.email = (TextView) v.findViewById(R.id.buyeremail);
			holder.compName = (TextView) v.findViewById(R.id.compName);
			holder.pay_type = (TextView) v.findViewById(R.id.order_pay_type);
			holder.chequeNum = (TextView) v.findViewById(R.id.txt_chequenum);

			holder.txt_order_id.setTypeface(Util.droid_regrular);
			holder.pAmount.setTypeface(Util.droid_bold);
			holder.personName.setTypeface(Util.droid_bold);
			holder.txt_status.setTypeface(Util.droid_regrular);
			holder.pDate.setTypeface(Util.droid_regrular);
			holder.chequeNum.setTypeface(Util.droid_regrular);
			holder.compName.setTypeface(Util.droid_regrular);
			// holder.feeAmount.setTypeface(Util.roboto_regular);
			holder.email.setTypeface(Util.droid_regrular);
			v.setTag(holder);
			return v;
		}

	}

	@Override
	public void onClick(View v) {

		if (v == back_layout) {
			if (mDrawerLayout.isDrawerOpen(left_menu_slider))
				mDrawerLayout.closeDrawer(left_menu_slider);
			else
				mDrawerLayout.openDrawer(left_menu_slider);
		} else if (v == filter_layout) {
			//openFilterDialog(SalesOrderActivity.this);
			openOrderFilterDialog(SalesOrderActivity.this);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {

	}

	public double getTotalAmount(Cursor cursor){
		double total_amount = 0.00;
		for (int i =0;i<cursor.getCount();i++){
			cursor.moveToPosition(i);
			if(!NullChecker(cursor.getString(cursor.getColumnIndex("Order_Status"))).trim().equalsIgnoreCase("Cancelled".trim())&&!NullChecker(cursor.getString(cursor.getColumnIndex("Order_Status"))).trim().equalsIgnoreCase("Deleted".trim())){
				total_amount = total_amount+cursor.getDouble(cursor.getColumnIndex("Order_Total"));
			}
		}
		return total_amount;
	}

	private void doCheckBackgroundService() {
		CheckServiceRunning checkReq = new CheckServiceRunning(SalesOrderActivity.this);
		checkReq.execute("");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Log.i("---------------onActivity Result------------", ":" + requestCode + " : " + resultCode);
		if(requestCode==Util.DASHBORD_ONACTIVITY_REQ_CODE ){

			Intent i = new Intent(SalesOrderActivity.this, SplashActivity.class);
			startActivity(i);
			finish();

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent startentent = new Intent(SalesOrderActivity.this,DashboardActivity.class);
			startentent.putExtra("CheckIn Event", checkedin_event_record);
			startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startentent.putExtra(Util.HOME, "home_layout");
			startActivity(startentent);
			finish();
			return true;
		}
		return false;
	}

}
