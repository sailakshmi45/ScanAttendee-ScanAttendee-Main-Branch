package com.globalnest.scanattendee;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.globalnest.BackgroundReciver.CheckServiceRunning;
import com.globalnest.BackgroundReciver.DownloadResultReceiver;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.OfflineScansObject;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.RefreshResponse;
import com.globalnest.mvc.SeminaAgenda;
import com.globalnest.mvc.SessionCountGson;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.SessionItemSpnrAdapter;
import com.globalnest.mvc.SessionSpnrAdapter;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.objects.ScannedItems;
import com.globalnest.scroll.PoppyViewHelper;
import com.globalnest.scroll.PoppyViewHelper.PoppyViewPosition;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.PullToRefreshListView.OnRefreshListener;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

@SuppressWarnings("deprecation")
@SuppressLint("NewApi")
public class DashboardAttendeesList extends BaseActivity implements OnClickListener, DownloadResultReceiver.Receiver {
    // tab bar
    LinearLayout tab_bar;
    TextView buyer_tab, attendee_tab, offline_tab, buyer_strip, attendee_strip;
    int ANIMATION_DURATION = 800, list_item_position = 0, last_opened_position;
    private Dialog sort_dialog;
    private ProgressDialog dialog;
    private PoppyViewHelper mPoppyViewHelper;
    // SwipeListView attendee_view;
    private PullToRefreshListView buyers_list, attendee_view;
    private ListView list_offline;
    // SwipeMenuListView attendee_view;
    private BuyerCursorAdapter buyer_adapter;
    private String whereClause = "",wherefiltersearch="";
    private Cursor att_cursor,  buyer_cursor;//order_cursor,
    private AttendeeAdapter adapter;
    private String requestType = "", filter_by = "ALL",badgefilter_by="ALL", delete_attendeeId = "",attendeeRecords="";
    boolean isFilter, isCheckedIn = false, isDialogVisible = false, isError, is_buyer = false, isOffline = false;
    int total_ticket_num = 0, checked_ticket_num = 0, total_ticket_sold = 0, list_index, selected_item_index = 0;
    private LinearLayout sort_layout, filter_layout, sortfilterlayout,tablayout,layout_session_name;
    private TextView txt_sort, txt_filter, txt_sortby, txt_sort1, txt_sort2, txt_sort3, txt_sort4, txt_sort5, txt_sort6,
            txt_sort_cancel, txt_load_msg, txtLoadingmore;
    // private ArrayList<ProgressBar> front_checkin_bar_array = new
    // ArrayList<ProgressBar>();
    @SuppressWarnings("unused")
    // private ArrayList<ProgressBar> buyerprogressBarArray = new
            // ArrayList<ProgressBar>();
            // session headder fields
            TextView txt_session_name, txt_startdate, txt_enddate, txt_room, txt_roomnumber, txt_checkin, txt_checkout,txt_total_checkin,txt_total_checkout,txt_session_on_name;
    LinearLayout linear_dates, linear_rooms;
    private TextView txt_noattendee;
    private HttpPostData postMethod;
    private ArrayAdapter<String> badgearray_adapter;
    private String whereClause_name = "";
    // private Map<Integer, ViewList> viewArray = new HashMap<Integer,
    // ViewList>();
    // @SuppressWarnings("unused")
    // private int buyer_progress_bar_index_count = -1;
    private ProgressBar progress;
    private LinearLayout btn_order_load_more, lay_SessioHeadder;
    private View loadMoreView;
    private boolean isLoadMore = false;
    private DownloadResultReceiver mReceiver;
    // SwipeMenuCreator attendeeCreator, buyerMenuCreator;
    // boolean devMode = false;
    private String item_pool_id = ITransaction.EMPTY_STRING;
    private List<OfflineScansObject> offlineList = new ArrayList<OfflineScansObject>();
    private String session_id = ITransaction.EMPTY_STRING, activity_name = ITransaction.EMPTY_STRING;
    private String session_name = ITransaction.EMPTY_STRING;
    private boolean isFromAttendDetails = false,isFromBuyerDetails= false;
    private static final String ORDERBY_CASE = " ORDER BY CASE  WHEN Scan_Time NOT NULL THEN Scan_Time  ELSE  CheckedInDate END DESC";
    private static  String WHERE_EVENTID = ITransaction.EMPTY_STRING;
    //private static  String WHERE_EVENTID = " where Event_Id='" + Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID) + "'";

    private String isFrom="",totalCount="0",sessionUserCheckOuts="",sessionUserCheckIns="",sessionStartTime="",sessionCheckOuts="",sessionCheckIns="",SessionStartTime="";
    private boolean showsessionname=false;
    int progress_bar_index_count = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checked_in_eventId.trim().isEmpty()) {
            checked_in_eventId = Util._getPreference(Util.eventPrefer, Util.EVENT_CHECKIN_ID);}
        WHERE_EVENTID = " where Event_Id='"+checked_in_eventId+"' ";
        showsessionname = getIntent().getBooleanExtra("sessionnamelayoutshow",false);
        item_pool_id = getIntent().getStringExtra(WebServiceUrls.SA_GETSESSIONCHECKINS);
        session_id = getIntent().getStringExtra(Util.INTENT_KEY_1);
        activity_name = getIntent().getStringExtra(Util.INTENT_KEY_2);
        session_name = getIntent().getStringExtra(Util.INTENT_KEY_3);
        isFrom = getIntent().getStringExtra(Util.INTENT_KEY_4);
        setCustomContentView(R.layout.attendee_list_layout);
        txt_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(DashboardAttendeesList.this,PrintAttendeesListActivity.class);
                i.putExtra("","");
                startActivity(i);
            }
        });
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        externalSettings = new ExternalSettings();
        if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
            externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }
        /*
         * if(!NullChecker(item_pool_id).isEmpty()){ item_pool_id =
         * DBFeilds.ATTENDEE_ITEM_POOL_ID + "='" + item_pool_id + "'"; }else
         */
        AppUtils.displayLog("-----------Session Id-----------",":"+session_id);
        if(!Util.db.getSwitchedONGroup(checked_in_eventId).Name.isEmpty()&&showsessionname){
            layout_session_name.setVisibility(View.VISIBLE);
            txt_session_on_name.setText(Html.fromHtml("<font color=#000000> Session Name: </font><font color=#617E8C>" + Util.db.getSwitchedONGroup(checked_in_eventId).Name+"</font>"));
        }else {
            layout_session_name.setVisibility(View.GONE);
        }
        if (NullChecker(item_pool_id).isEmpty() && !NullChecker(session_id).isEmpty()) {
            AppUtils.displayLog("-----------Session Name in If-----------",":"+session_name);
            item_pool_id = NullChecker(item_pool_id).trim();
            List<ScannedItems> scanned_item_list = Util.db
                    .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, session_id);
            int i = 0;
            for (ScannedItems scan_items : scanned_item_list) {
                item_pool_id = item_pool_id + scan_items.BLN_Item_Pool__c;
                if (i != (scanned_item_list.size() - 1)) {
                    item_pool_id = item_pool_id + ";";
                }
                i++;
            }
        }

        // setEnableSessionHeadder();
        if(session_id != null && session_id.length() > 0){
            requestType = "CheckInCount";
            doRequest();
        }else{
            dialog = new ProgressDialog(DashboardAttendeesList.this);
            dialog.setMessage("Please wait loading attendees...");
            dialog.setCancelable(false);
            if (Util.isMyServiceRunning(DownloadService.class, DashboardAttendeesList.this)) {
                requestType = Util.CHECK_SERVICE;
                dialog.show();
                doRequest();
            } else {
                getTicketCursor();
			/*UpdateBuyerCursor();
			UpdateAttendeeCursor();*/
                // loadAttendeesInBackground();
            }
        }

        lay_loadmore.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (!isOnline()) {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                } else if (Util.db.totalOrderCountwithoutCancelled(checked_in_eventId) < Util.dashboardHandler.totalOrders) {
                    requestType = "Order";
                    isLoadMore = true;
                    txtLoadingmore.setText("Loading....");
                    doRequest();
                } else {
                    AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                    dialog.setParamenters("Alert", "All attendees are downloaded.", null, null, 1, false);
                    if (is_buyer) {
                        dialog.setParamenters("Alert", "All buyers are downloaded.", null, null, 1, false);
                    }
                    dialog.show();
                }
            }
        });

        img_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(DashboardAttendeesList.this);
                back_layout.setVisibility(View.VISIBLE);
                top_layout.setBackgroundResource(R.color.green_top_header);
                back_layout.setBackgroundResource(R.color.green_top_header);
                search_layout.setVisibility(View.GONE);
                search_view.setText("");
                // search_view.setFocusable(false);
                if (!is_buyer)
                    UpdateAttendeeCursor();
                else {
                    UpdateBuyerCursor();
                }
            }
        });
        img_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                back_layout.setVisibility(View.GONE);
                search_view.setFocusable(true);
                search_view.requestFocus();
                search_layout.setVisibility(View.VISIBLE);

                //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				/*InputMethodManager imm = (InputMethodManager)
						getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(search_view ,
						InputMethodManager.SHOW_IMPLICIT);*/
                //openKeybord(v);
            }
        });
        //TODO nextday
        search_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_view.getText().toString().trim().isEmpty()) {
                    if (is_buyer)
                        search_view.setHint("First Name, Last Name, Company and Email");
                    else {
                        search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
                    }
                    requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                    doRequest();
                    //SortFunction(R.id.editsearchrecord, is_buyer);
                    return true;
                }else if(search_view.getText().toString().trim().isEmpty()){
                    Toast.makeText(DashboardAttendeesList.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        search_view.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_view.setFocusable(true);
                if (is_buyer)
                    search_view.setHint("First Name, Last Name, Company and Email");
                else {
                    search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
                }
                //if(before<count) {
                SortFunction(R.id.editsearchrecord, is_buyer);
                //}
				/*requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
				doRequest();*/

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        attendee_view.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> _adapter, View view, int position, long index) {
                Cursor c1 = adapter.getCursor();
                c1.moveToPosition(position);
                // Log.i("DashboardAttendeesList", "Order_Id " +
                // c1.getString(c1.getColumnIndex("Order_Id")));

                Intent att_activity = new Intent(DashboardAttendeesList.this, AttendeeDetailActivity.class);
                att_activity.putExtra("EVENT_ID", c1.getString(c1.getColumnIndex("Event_Id")));
                att_activity.putExtra("ATTENDEE_ID", c1.getString(c1.getColumnIndex("Attendee_Id")));
                att_activity.putExtra("ORDER_ID", c1.getString(c1.getColumnIndex("Order_Id")));
                //att_activity.putExtra("BADGE_ID", c1.getString(c1.getColumnIndex("BadgeId")));
                att_activity.putExtra(Util.INTENT_KEY_2,session_id);
                // att_activity.putExtra("Badge_Res", badge_res);
                //startActivity(att_activity);
                startActivityForResult(att_activity,2017);

            }
        });

        buyer_tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                search_view.setHint("First Name, Last Name, Company and Email");
                OntabClick(true);
            }
        });

        attendee_tab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
                OntabClick(false);
            }
        });

        offline_tab.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                OnOfflineTabClick();
            }
        });
        btn_order_load_more.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String whereClause = " where orders.Buyer_Id = user.GNUserID" + " AND orders.Event_Id = '"
                        + checked_in_eventId + "' order by orders.Order_Date DESC";
                //order_cursor = Util.db.getPaymentDataCursor(whereClause);
                int totalcount=Util.db.totalOrderCountwithoutCancelled(checked_in_eventId);
                if (isOnline()) {

                    if (totalcount < Util.dashboardHandler.totalOrders) {
                        requestType = "Order";
                        isLoadMore = true;
                        txtLoadingmore.setText("Loading....");
                        doRequest();
                    } else {
                        AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                        dialog.setParamenters("Alert", "All records are downloaded", null, null, 1, false);
                        dialog.show();
                    }
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                }

            }
        });

        attendee_view.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                if(session_id !=null && session_id.length() >0){
                    requestType = "CheckInCountRefresh";
                    if (isOnline()) {
                        doRequest();
                    } else {
                        //startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
                        attendee_view.onRefreshComplete();
                    }
                }else{
                    requestType = Util.ITEMS_ORDERS_REFRESH;
                    if (isOnline()) {
                        doRequest();
                    } else {
                        //startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
                        attendee_view.onRefreshComplete();
                    }
                }
            }
        });

        buyers_list.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {

                requestType = Util.ITEMS_ORDERS_REFRESH;
                if (isOnline()) {
                    doRequest();
                } else {
                    //startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
                    attendee_view.onRefreshComplete();
                }
            }
        });

        img_setting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent addattendee = new Intent(DashboardAttendeesList.this, AddAttendeeActivity.class);
                startActivity(addattendee);
            }
        });
        img_refund_history.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
				/*requestType = DBFeilds.STATUS_OFFLINE;
				doRequest();*/
                Intent i = new Intent(DashboardAttendeesList.this, OfflineDataSyncActivty.class);
                i.putExtra(Util.INTENT_KEY_1, SessionListActivity.class.getName());
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

    }


    public void OntabClick(boolean isBuyer) {
        isOffline = false;
        list_offline.setVisibility(View.GONE);
        tablayout.setVisibility(View.VISIBLE);
        if (isBuyer) {
            is_buyer = true;
            // search_view.set
            sortfilterlayout.setVisibility(View.GONE);
            //buyer_tab.setBackgroundColor(getResources().getColor(R.color.green_button_color));
            buyer_tab.setBackgroundColor(getResources().getColor(R.color.green_button_color));
            buyer_tab.setTextColor(getResources().getColor(R.color.white));
            attendee_tab.setBackgroundColor(getResources().getColor(R.color.white));
            attendee_tab.setTextColor(getResources().getColor(R.color.green_button_color));
            offline_tab.setBackgroundColor(getResources().getColor(R.color.white));
            offline_tab.setTextColor(getResources().getColor(R.color.green_button_color));

            attendee_view.setVisibility(View.GONE);
            buyers_list.setVisibility(View.VISIBLE);
            //SortFunction(R.id.editsearchrecord, is_buyer);
            SortFunction(1, is_buyer);
            txt_title.setText(ITransaction.EMPTY_STRING);
            txt_title.setText("Buyers" + "(" + buyer_cursor.getCount() + ")     ");
            txt_noattendee.setVisibility(View.GONE);
            if (buyer_cursor.getCount() == 0) {
                txt_noattendee.setVisibility(View.VISIBLE);
            }
            if (lay_SessioHeadder.getVisibility() == View.VISIBLE) {
                txt_checkin.setVisibility(View.VISIBLE);
                txt_checkout.setVisibility(View.VISIBLE);
                txt_total_checkin.setVisibility(View.VISIBLE);
            }
        } else {
            is_buyer = false;
            sortfilterlayout.setVisibility(View.VISIBLE);
            attendee_tab.setBackgroundColor(getResources().getColor(R.color.green_button_color));
            attendee_tab.setTextColor(getResources().getColor(R.color.white));

            buyer_tab.setBackgroundColor(getResources().getColor(R.color.white));
            buyer_tab.setTextColor(getResources().getColor(R.color.green_button_color));

            offline_tab.setBackgroundColor(getResources().getColor(R.color.white));
            offline_tab.setTextColor(getResources().getColor(R.color.green_button_color));

            buyers_list.setVisibility(View.GONE);
            attendee_view.setVisibility(View.VISIBLE);
            //UpdateAttendeeCursor();
            //SortFunction(R.id.editsearchrecord, is_buyer);
            if(!NullChecker(item_pool_id).isEmpty()){
                UpdateAttendeeCursor();
            }else {
                SortFunction(1, is_buyer);
            }
            txt_title.setText(ITransaction.EMPTY_STRING);
            String actionBarLable = "Attendees" + "(" + att_cursor.getCount() + ")   ";
            if (actionBarLable.length() > 20) {
                actionBarLable = actionBarLable.substring(0, 20);
            }
            txt_title.setText(actionBarLable);

            txt_noattendee.setVisibility(View.GONE);
            if (lay_SessioHeadder.getVisibility() == View.VISIBLE) {
                txt_checkin.setVisibility(View.VISIBLE);
                txt_checkout.setVisibility(View.VISIBLE);
                txt_total_checkin.setVisibility(View.VISIBLE);
            }
            if (att_cursor.getCount() == 0) {
                txt_noattendee.setVisibility(View.VISIBLE);
            }
        }

    }

    private void OnOfflineTabClick() {
        is_buyer = false;
        isOffline = true;
        sortfilterlayout.setVisibility(View.GONE);
        attendee_tab.setBackgroundColor(getResources().getColor(R.color.white));
        attendee_tab.setTextColor(getResources().getColor(R.color.green_button_color));
        buyer_tab.setBackgroundColor(getResources().getColor(R.color.white));
        buyer_tab.setTextColor(getResources().getColor(R.color.green_button_color));
        buyers_list.setVisibility(View.GONE);
        attendee_view.setVisibility(View.GONE);
        txt_total_checkin.setVisibility(View.GONE);
        offline_tab.setBackgroundColor(getResources().getColor(R.color.green_button_color));
        offline_tab.setTextColor(getResources().getColor(R.color.white));

        //SortFunction(R.id.editsearchrecord, is_buyer);
        txt_title.setText(ITransaction.EMPTY_STRING);
        txt_title.setText("Offline Scans" + "(" + offlineList.size() + ")     ");
        txt_checkin.setVisibility(View.GONE);
        txt_checkout.setVisibility(View.GONE);
        txt_noattendee.setVisibility(View.GONE);
        if (offlineList.size() == 0) {
            txt_noattendee.setVisibility(View.VISIBLE);
        }

        list_offline.setVisibility(View.VISIBLE);
        if(!NullChecker(session_id).isEmpty()){
            offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                    +"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
        }else{
            offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                    + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
        }
        if (list_offline != null && offlineList.size() > 0) {
            if (!NullChecker(activity_name).isEmpty()) {
                tab_bar.setVisibility(View.VISIBLE);
                buyer_tab.setVisibility(View.GONE);
            }
            list_offline.setAdapter(new OfflineListAdapter(offlineList));
            //attendee_view.setVisibility(View.GONE);
            offline_tab.setVisibility(View.VISIBLE);
            img_refund_history.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // FlurryAgent.onStartSession(this,
        // ScannerSettingsApplication.IMPROVE_APP_ID);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

		/* if (list_offline != null && offlineList.size() > 0) {
            offline_tab.setVisibility(View.VISIBLE);

            //attendee_view.setVisibility(View.GONE);// july 25th
            //OntabClick(false);//sai changed
            img_refund_history.setVisibility(View.VISIBLE);
            if(isOffline){
                list_offline.setAdapter(new OfflineListAdapter(offlineList));
            }
        }else {
            offline_tab.setVisibility(View.GONE);
        }
        if (!NullChecker(activity_name).isEmpty()) {
            tab_bar.setVisibility(View.GONE);
            if (offlineList.size() > 0) {
                tab_bar.setVisibility(View.VISIBLE);
                buyer_tab.setVisibility(View.GONE);
            }
        }
    }*/
        String itempool_id="",group_id="";
        //search_view.setText("");
        back_layout.setVisibility(View.VISIBLE);
        search_layout.setVisibility(View.GONE);
        try {
            hideSoftKeyboard(DashboardAttendeesList.this);
            top_layout.setBackgroundResource(R.color.green_top_header);
            back_layout.setBackgroundResource(R.color.green_top_header);
            Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
            externalSettings = new ExternalSettings();
            if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
                externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
            }if (!NullChecker(session_id).isEmpty()) {
                txt_session_name.setText(NullChecker(session_name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setEnableSessionHeadder();
        //searchBuyer(false);
        if (att_cursor != null) {
            att_cursor.close();
        }
        if (NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id).isEmpty()&&NullChecker(session_id).isEmpty()) {
            itempool_id = "";
            att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID +attendeeRecords + ORDERBY_CASE, "", activity_name, "");
            if (adapter != null) {
                attendee_view.setAdapter(adapter);
                attendee_view.setVisibility(View.VISIBLE);
                adapter.changeCursor(att_cursor);
                adapter.notifyDataSetChanged();
            }else {
                adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                //attendee_view.setVisibility(View.VISIBLE);
                attendee_view.setAdapter(adapter);
                attendee_view.setVisibility(View.VISIBLE);
                adapter.changeCursor(att_cursor);
                adapter.notifyDataSetChanged();
            }
        }else if ((!NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id).isEmpty()||!NullChecker(session_id).isEmpty())&&(!isOffline)&&(!is_buyer)&&NullChecker(item_pool_id).isEmpty())
        {
            if(search_view.getText().toString().isEmpty()) {
                if(NullChecker(session_id).isEmpty()){
                    group_id = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
                }else {
                    group_id = session_id;
                }

                itempool_id="";
                if (!group_id.isEmpty()) {
                    List<ScannedItems> scanned_item_list = Util.db
                            .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
                    int i = 0;
                    for (ScannedItems scan_items : scanned_item_list) {
                        itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
                        if (i != (scanned_item_list.size() - 1)) {
                            itempool_id = itempool_id + ";";
                        }
                        i++;
                    }
                }
				/*att_cursor.close();
				att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
						, itempool_id, activity_name, group_id);*/
                if (att_cursor == null || whereClause_name.isEmpty()) {
                    att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
                            +attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
                    //att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
                } else if (!whereClause_name.isEmpty()) {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
                            , itempool_id, activity_name, group_id);
                }else {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
                            , itempool_id, activity_name, group_id);
                }
				/*if (att_cursor == null || whereClause_name.isEmpty()) {
					att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
							+attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
					//att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
				} else if (!whereClause_name.isEmpty()&&NullChecker(item_pool_id).isEmpty()) {
					att_cursor.close();
					att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
							, item_pool_id, activity_name, group_id);
				}else {
					att_cursor.close();
					att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
							, itempool_id, activity_name, group_id);
				}*/
                if (att_cursor != null && att_cursor.getCount() > 0) {
                    filter_layout.setEnabled(true);
                    sort_layout.setEnabled(true);
                    txt_title.setText(ITransaction.EMPTY_STRING);
                    txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    setEnableSessionHeadder();
                } else {
                    txt_noattendee.setVisibility(View.VISIBLE);
                    txt_title.setText("Attendees(" + 0 + ") ");
                }
            }
            else {
                searchAttendee(false);
            }
        }else if (NullChecker(session_id).isEmpty() && NullChecker(item_pool_id).isEmpty()) {
            SortFunction(1,is_buyer);
            //updateView();
        }else if(!NullChecker(session_id).isEmpty() && !NullChecker(item_pool_id).isEmpty()&&NullChecker(activity_name).isEmpty()){
            if(search_view.getText().toString().isEmpty()) {
                if(NullChecker(session_id).isEmpty()){
                    group_id = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
                }else {
                    group_id = session_id;
                }

                itempool_id="";
                if (!group_id.isEmpty()) {
                    List<ScannedItems> scanned_item_list = Util.db
                            .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
                    int i = 0;
                    for (ScannedItems scan_items : scanned_item_list) {
                        itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
                        if (i != (scanned_item_list.size() - 1)) {
                            itempool_id = itempool_id + ";";
                        }
                        i++;
                    }
                }
                if (att_cursor == null || whereClause_name.isEmpty()) {
                    att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
                            +attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
                    //att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
                } else if (!whereClause_name.isEmpty()&&NullChecker(item_pool_id).isEmpty()) {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
                            , item_pool_id, activity_name, group_id);
                }else {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
                            , itempool_id, activity_name, group_id);
                }
                if (att_cursor != null && att_cursor.getCount() > 0) {
                    filter_layout.setEnabled(true);
                    sort_layout.setEnabled(true);
                    txt_title.setText(ITransaction.EMPTY_STRING);
                    txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    setEnableSessionHeadder();
                } else {
                    txt_noattendee.setVisibility(View.VISIBLE);
                    txt_title.setText("Attendees(" + 0 + ") ");
                }
            }
            else {
                searchAttendee(false);
            }
        }else{
            UpdateAttendeeCursor();
        }/*else {
			group_id = Util.db.getSwitchedONGroup(checked_in_eventId).Id;
			List<ScannedItems> scanned_item_list = Util.db
					.getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
			int i = 0;
			for (ScannedItems scan_items : scanned_item_list) {
				itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
				if (i != (scanned_item_list.size() - 1)) {
					itempool_id = itempool_id + ";";
				}
				i++;
			}
			att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID + ORDERBY_CASE, itempool_id, activity_name, group_id);
			if (adapter != null) {
				adapter.changeCursor(att_cursor);
				adapter.notifyDataSetChanged();
			}
		}*/




        if(!NullChecker(session_id).isEmpty()){
            offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                    +"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
        }else{
            offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                    + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
        }


        if (list_offline != null && offlineList.size() > 0) {
            offline_tab.setVisibility(View.VISIBLE);

            //attendee_view.setVisibility(View.GONE);// july 25th
            //OntabClick(false);//sai changed
            img_refund_history.setVisibility(View.VISIBLE);
            if(isOffline){
                list_offline.setAdapter(new OfflineListAdapter(offlineList));
            }
        } else {
            list_offline.setVisibility(View.GONE);
            offline_tab.setVisibility(View.GONE);
            img_refund_history.setVisibility(View.GONE);
            if(isFromAttendDetails||!is_buyer){
                if (att_cursor != null && att_cursor.getCount() > 0) {
                    back_layout.setVisibility(View.VISIBLE);
                    filter_layout.setEnabled(true);
                    sort_layout.setEnabled(true);
                    txt_title.setText(ITransaction.EMPTY_STRING);
                    txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    //attendee_view.setVisibility(View.VISIBLE);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    adapter.changeCursor(att_cursor);
                    adapter.notifyDataSetChanged();
                    setEnableSessionHeadder();
                }
				/*back_layout.setVisibility(View.VISIBLE);
				//search_view.setText("");
				search_layout.setVisibility(View.GONE);
				attendee_view.setVisibility(View.VISIBLE);*/
                //OntabClick(false);
            }
            else if(is_buyer){
                OntabClick(true);
                if(buyer_cursor != null){
                    buyer_cursor.close();
                    String whereClause = "where " + DBFeilds.USER_EVENT_ID + " = '" + checked_in_eventId + "' ORDER BY "
                            + DBFeilds.USER_USERID + " DESC";
                    buyer_cursor = Util.db.getGNUser(whereClause);
                    if(buyer_adapter != null){
                        attendee_view.setVisibility(View.GONE);
                        buyers_list.setAdapter(buyer_adapter);
                        buyers_list.setVisibility(View.VISIBLE);
                        buyer_adapter.changeCursor(buyer_cursor);
                        buyer_adapter.notifyDataSetChanged();
                    }
                }
            }else {
                attendee_view.setVisibility(View.VISIBLE);
            }

        }

        if (!NullChecker(activity_name).isEmpty()) {
            tab_bar.setVisibility(View.GONE);
            if (offlineList.size() > 0) {
                tab_bar.setVisibility(View.VISIBLE);
                buyer_tab.setVisibility(View.GONE);
            }
        }
		/*if(!NullChecker(filter_by).equalsIgnoreCase("ALL")){
			isFilter = true;
			SortFunction(R.id.txtsort1, false);
		}*/
        if(isFromAttendDetails){
            isFromAttendDetails = false;
        }
        txt_title.setText(ITransaction.EMPTY_STRING);
        if (isOffline) {
            txt_title.setText("Offline Scans" + "(" + offlineList.size() + ")  ");
        } else if (is_buyer) {
            txt_title.setText("Buyers" + "(" + buyer_cursor.getCount() + ")    ");
        } else {
            String actionBarLable = "Attendees" + "(" + att_cursor.getCount() + ")   ";
            if (actionBarLable.length() > 20) {
                actionBarLable = actionBarLable.substring(0, 20);
            }
            txt_title.setText(actionBarLable);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.scanattendee.BaseActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {

        super.onDestroy();
        Util.slide_menu_id = 0;
    }

    private void getOrderCursor() {

        // Util.db.totalOrderCount(checked_in_eventId) + " Attendees out of " + Util.dashboardHandler.totalOrders)
        //if ( Util.db.totalOrderCount(checked_in_eventId) != Util.dashboardHandler.totalOrders) {
        //UpdateAttendeeCursor();
        //TODO babu commented this for only one call this laad more service
        if ( Util.db.totalOrderCount(checked_in_eventId) == 0 ) {
            requestType = "Order";
            if (isOnline()){
                doRequest();
            }else{// added by babu for offline entry no attendees are displaying
                search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
                //OntabClick(false);
            }
        }
    }

    private void  getTicketCursor() {
        whereClause = " where EventId='" + checked_in_eventId + "'";
        // Cursor c_ticket = Util.db.getTicketCursor(whereClause);
        if (Util.db.getItemsCount(whereClause) > 0) {
            getOrderCursor();
        } else {
            if (isOnline()) {
                requestType = "Ticket";
                doRequest();
            } /*else {
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}*/
        }
    }

    private void UpdateBuyerCursor() {
        setEnableSessionHeadder();
        // set buyers adapter by mail_id
        String whereClause = "where " + DBFeilds.USER_EVENT_ID + " = '" + checked_in_eventId + "' ORDER BY "
                + DBFeilds.USER_USERID + " DESC";
        // Log.i("--------------Where Condition--------------", ":" +
        // whereClause);
        if (buyer_cursor == null) {
            buyer_cursor = Util.db.getGNUser(whereClause);
        } else {
            buyer_cursor.close();
            buyer_cursor = Util.db.getGNUser(whereClause);
        }
        // Log.i("---------------On UpdateBuyerCursor count--------------", ":"
        // + buyer_cursor.getCount());
        if (buyer_cursor.getCount() > 0) {
            txt_noattendee.setVisibility(View.GONE);
            buyer_adapter = new BuyerCursorAdapter(DashboardAttendeesList.this, buyer_cursor, DashboardAttendeesList.this,
                    sfdcddetails, false, checked_in_eventId, NullChecker(session_id));
            buyers_list.setAdapter(buyer_adapter);
            // txt_title.setText("Buyers"+"("+buyer_cursor.getCount()+")"   );
            // buyers_list.closeOpenedItems();
        } else {
            progress.setVisibility(View.GONE);
            txt_load_msg.setVisibility(View.GONE);
            txt_noattendee.setVisibility(View.VISIBLE);
            txt_noattendee.setText(getResources().getString(R.string.no_buyer));
        }
    }

    private void UpdateAttendeeCursor() {
        setEnableSessionHeadder();
        txt_title.setText(ITransaction.EMPTY_STRING);
        if (search_view.getText().toString().trim().isEmpty()) {
            //search_view.setText("");
            txt_noattendee.setVisibility(View.GONE);
            String query = WHERE_EVENTID +" AND "+ DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' "+attendeeRecords+ORDERBY_CASE;
            if (att_cursor == null) {
                att_cursor = Util.db.getAttendeeDataCursor(query, item_pool_id, activity_name,session_id);
            } else {
                att_cursor.close();
                att_cursor = Util.db.getAttendeeDataCursor(query, item_pool_id, activity_name,session_id);
            }
            // Log.i("---Size of attendee cursorin UPDATE CURSOR Function---",
            // ":" + att_cursor.getCount());
            if (att_cursor.getCount() > 0) {
                //attendee_view.setVisibility(View.VISIBLE);
                if (!isLoadMore) {
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    // buyers_list.setSelectionFromTop(buyCurrentPosition+1,0);
                } else {
                    int attCurrentPosition = adapter.getCursor().getCount();
                    isLoadMore = false;
                    txtLoadingmore.setText("Load More");
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    if(adapter != null){
                        attendee_view.setAdapter(adapter);
                        attendee_view.setVisibility(View.VISIBLE);
                        adapter.changeCursor(att_cursor);
                        adapter.notifyDataSetChanged();
                    }
                    attendee_view.setSelectionFromTop(attCurrentPosition + 1, 0);
                }
                txt_title.setText("Attendees");
                String actionBarLable = "Attendees" + "(" + att_cursor.getCount() + ")    ";
                if (actionBarLable.length() > 20) {
                    actionBarLable = actionBarLable.substring(0, 20);
                }
                txt_title.setText(actionBarLable);
                txt_noattendee.setVisibility(View.GONE);

            } else {
                txt_title.setText("Attendees" + "(" + 0 + ")    ");
                txt_noattendee.setVisibility(View.VISIBLE);
                txt_noattendee.setText(getResources().getString(R.string.no_attendee));

            }
            if (is_buyer) {
                attendee_view.setVisibility(View.GONE);
            } else {
                attendee_view.setVisibility(View.VISIBLE);
            }
            if(!NullChecker(filter_by).isEmpty()){
                if (list_offline != null && offlineList.size() > 0) {
                    if (!NullChecker(activity_name).isEmpty()) {
                        tab_bar.setVisibility(View.VISIBLE);
                        buyer_tab.setVisibility(View.GONE);
                    }
                    list_offline.setAdapter(new OfflineListAdapter(offlineList));
                    //attendee_view.setVisibility(View.GONE);
                    offline_tab.setVisibility(View.VISIBLE);
                    img_refund_history.setVisibility(View.VISIBLE);
                }
                isFilter = true;
                SortFunction(R.id.txtsort1, false);
            }
        } else {
            searchAttendee(false);
        }
    }

    @Override
    public void doRequest() {
        if(isOnline()) {
            String msg = "";
            String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
            if (requestType.equalsIgnoreCase("CheckInCount") || requestType.equalsIgnoreCase("CheckInCountRefresh")) {
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_BLN_ASC_CHECKINSCOUNT + getCheckInCountValues();
                postMethod = new HttpPostData("Updating Session Count ...", url, null, access_token,
                        DashboardAttendeesList.this);
                postMethod.execute();
            } else if (requestType.equalsIgnoreCase("Order")) {
                if (!is_buyer) {
                    txt_load_msg.setText("Loading Attendees...");
                    msg = "Loading Attendees...";
                } else {
                    txt_load_msg.setText("Loading Buyers...");
                    msg = "Loading Buyers...";
                }
                String offset = Util.offset_pref.getString(checked_in_eventId, "");
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                        + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
                        + checkedin_event_record.Events.scan_attendee_limit__c;
                if(Util.db.totalOrderCountwithoutCancelled(checked_in_eventId)==0){
                    url = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                            + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + "&limit="
                            + checkedin_event_record.Events.scan_attendee_limit__c;
                }else {
                    url = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                            + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
                            + checkedin_event_record.Events.scan_attendee_limit__c;
                }
                postMethod = new HttpPostData(msg, url, null, access_token, DashboardAttendeesList.this);
                postMethod.execute();

            } else if (requestType.equals(Util.DELETE)) {

                postMethod = new HttpPostData("Deleting Attendee...", getDeteteAttendeeUrl(), null, access_token,
                        DashboardAttendeesList.this);
                postMethod.execute();

            } else if (requestType.equals(Util.CHECK_SERVICE)) {
                doCheckBackgroundService();
            } else if (requestType.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)) {

                dialog = new ProgressDialog(DashboardAttendeesList.this);
                dialog.setMessage("Please wait refreshing attendees...");
                dialog.setCancelable(false);
                dialog.show();
                DownloadResultReceiver mReceiver = new DownloadResultReceiver(new Handler());
                mReceiver.setReceiver(DashboardAttendeesList.this);
                Intent intent = new Intent(Intent.ACTION_SYNC, null, DashboardAttendeesList.this, DownloadService.class);
                String ticURL = sfdcddetails.instance_url + WebServiceUrls.SA_REFRESH_URL + getValues("1");
                intent.putExtra(DownloadService.ATT_URL, "");
                intent.putExtra(DownloadService.TIC_URL, ticURL);
                intent.putExtra(DownloadService.ACCESSTOKEN, sfdcddetails.token_type + " " + sfdcddetails.access_token);
                intent.putExtra(DownloadService.EVENTID, checked_in_eventId);
                intent.putExtra(DownloadService.BADGE_URL, sfdcddetails.user_id);
                intent.putExtra(DownloadService.REQUESTTYPE, "Refresh");
                String url[] = getValues("1").split("LastModifiedDate=");
                url=url[1].split("&Request_Flag");
                intent.putExtra(DownloadService.LASTMODIFIEDDATE, NullChecker(url[0]));
                intent.putExtra(DownloadService.RECEIVER, mReceiver);
                intent.putExtra(DownloadService.ACTIVITY_NAME, DashboardAttendeesList.class.getName());
                intent.putExtra(DownloadService.reload, "");
                intent.putExtra("requestId", 101);
                startService(intent);

            } else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_SEARCH_ATTENDEE + getSearchValues();
                postMethod = new HttpPostData("Loading Search...", url, null, access_token, DashboardAttendeesList.this);
                postMethod.execute();
            } else if (requestType.equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)) {
                String urlValuesString = getOfflineValues();
                String accessToken = sfdcddetails.token_type + " " + sfdcddetails.access_token;
                postMethod = new HttpPostData("Please Wait...",
                        sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL + urlValuesString,
                        getOfflineJsonBody().toString(), accessToken, DashboardAttendeesList.this);

                postMethod.execute();
            } else {
                txt_load_msg.setText("Loading Attendees...");
                /*
                 * postMethod = new HttpPostData(sfdcddetails.instance_url +
                 * WebServiceUrls.SA_GET_TICKET_LIST + "Event_id=" +
                 * checked_in_eventId, null, sfdcddetails.token_type,
                 * sfdcddetails.access_token, DashboardAttendeesList.this);
                 */
                String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="
                        + checked_in_eventId;
                postMethod = new HttpPostData("Loading Tickets...", _url, null, access_token, DashboardAttendeesList.this);
                postMethod.execute();
            }
        }else{


            startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
        }
    }
    private String getCheckInCountValues() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("SessionId", session_id));
        return AppUtils.getQuery(values);
    }
    private String getSearchValues() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("search_string", search_view.getText().toString()));
        return AppUtils.getQuery(values);
    }

    private String getValues(String batchCount) {
        String lastmofitedate=Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"");
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("appname", ""));
        if(session_id !=null && session_id.length() >0 && !( Util.sessionCountPref.getBoolean(Util.isExternalDataDownloaded,false) )){
            if(Util.sessionCountPref.getString(Util.isSessionRefreshed,"false").equals("false") && Util.sessionCountPref.getString(Util.SessionId,"").equals(session_id)
                    && !( Util.sessionCountPref.getString(Util.SessionsToBeRefreshed,"").contains(session_id))
                    ){
                if(Util.sessionCountPref.getString(Util.SessionsToBeRefreshed,"").length() >0 ){
                    if(!( Util.sessionCountPref.getString(Util.SessionsToBeRefreshed,"").contains(session_id)))
                        Util.sessionCountPref.edit().putString(Util.SessionsToBeRefreshed,Util.sessionCountPref.getString(Util.SessionsToBeRefreshed,"")+";"+session_id).commit();
                }else{
                    Util.sessionCountPref.edit().putString(Util.SessionsToBeRefreshed,session_id).commit();
                }
                values.add(new BasicNameValuePair("LastModifiedDate", Util.sessionCountPref.getString(Util.SessionStartTime,checkedin_event_record.lastRefreshDate)));
            }else{
                //	values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
                if(NullChecker(Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"")).trim().isEmpty()){
                    values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
                }else {
                    values.add(new BasicNameValuePair("LastModifiedDate", lastmofitedate));
                }
            }
        }else{
            //	values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));

            if(NullChecker(Util.lastModifideDate.getString(Util.ITEMSANDATTENDEESLASTMODITIFEDATE,"")).trim().isEmpty()){
                values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
            }else {
                values.add(new BasicNameValuePair("LastModifiedDate", lastmofitedate));
            }		}
        //values.add(new BasicNameValuePair("LastModifiedDate", "2017-05-15 03:26:07"));
        values.add(new BasicNameValuePair("Request_Flag", "Itemandattendees"));
        values.add(new BasicNameValuePair("startbatch", batchCount));
        return AppUtils.getQuery(values);
    }

    private String getOfflineValues() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("scannedby", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("eventId", BaseActivity.checkedin_event_record.Events.Id));
        values.add(new BasicNameValuePair("source", "Offline"));
        values.add(new BasicNameValuePair("DeviceType", Util.getDeviceNameandAppVersion()));
        values.add(new BasicNameValuePair("checkin_only",String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))));
        return AppUtils.getQuery(values);
    }

    private String getOfflineJsonBody() {
        JSONArray ticketarray = new JSONArray();
        for (OfflineScansObject scanObj : offlineList) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("TicketId", scanObj.badge_id);
                obj.put("sTime", Util.getOfflineSyncServerFormat(scanObj.scan_date_time));
                // boolean isFreeSession =
                // Util.db.isItemPoolFreeSession(scanObj.item_pool_id,
                // scanObj.event_id);

                obj.put("isCHeckIn", true);
                obj.put("freeSemPoolId", scanObj.item_pool_id);
                obj.put("device", "ANDROID");
                obj.put("sessionid", scanObj.scan_group_id);
                obj.put("scandevicemode",scanObj.scandevicemode);
                ticketarray.put(obj);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return ticketarray.toString();
    }

    private void doCheckBackgroundService() {
        CheckServiceRunning checkReq = new CheckServiceRunning(DashboardAttendeesList.this);
        checkReq.execute("");
    }

    /*
     * public String httpCheckIn(String tickets, String attendeeid, String
     * checked_time) { String url = "", response = ""; HttpClient client = new
     * DefaultHttpClient(); HttpResponse httpResponse;
     *
     * HttpPost postMethod; try { JSONArray checkin_array = new JSONArray();
     * JSONObject att_info = new JSONObject(); JSONArray ticketarray = new
     * JSONArray();
     *
     * JSONObject parent = new JSONObject();
     *
     * if (isCheckedIn) { for (String s : tickets.split(",")) { JSONObject obj =
     * new JSONObject();
     *
     * if (tickets_register.get(s).equals("CheckedIn")) {
     * obj.put("TicketNumber", s); obj.put("CheckInTime", checked_time);
     * obj.put("isCheckIn", false); obj.put("Scannedby", user_profile.Userid);
     * ticketarray.put(obj); } } } else { for (String s : tickets.split(",")) {
     * JSONObject obj = new JSONObject();
     *
     * if (tickets_register.get(s).equals("Not CheckedIn")) {
     * obj.put("TicketNumber", s); obj.put("CheckInTime", checked_time);
     * obj.put("isCheckIn", true); obj.put("Scannedby", user_profile.Userid);
     * ticketarray.put(obj); } } }
     *
     * att_info.put("AttendeeId", attendeeid); att_info.put("MobileCheckInTime",
     * checked_time);
     *
     * att_info.put("TicketDetails", ticketarray); checkin_array.put(att_info);
     * parent.put("CheckInInfo", checkin_array);
     *
     * url = sfdcddetails.instance_url + WebServiceUrls.SA_TICKET_CHECKED_IN +
     * "Event_Id=" + checked_in_eventId;
     *
     * postMethod = new HttpPost(url); postMethod.addHeader("Authorization",
     * sfdcddetails.token_type + " " + sfdcddetails.access_token); StringEntity
     * se = new StringEntity(parent.toString()); postMethod.setEntity(se);
     * httpResponse = client.execute(postMethod); response =
     * EntityUtils.toString(httpResponse.getEntity()); //Log.i(
     * "---Bult Check In response----", ":" + response);
     *
     * } catch (Exception e) {
     *
     * } return response; }
     */
    @Override
    public void parseJsonResponse(String response) {

        try {
            if (!isValidResponse(response)) {
                openSessionExpireAlert(errorMessage(response));
            } else {
                // Log.i("-- REQUEST TYPE---", requestType);
                gson = new Gson();
                if(requestType.equalsIgnoreCase("CheckInCount") || requestType.equalsIgnoreCase("CheckInCountRefresh")){
                    SessionCountGson objsessionGson = gson.fromJson(response, SessionCountGson.class);
                    if(objsessionGson != null){
                        sessionCheckIns = objsessionGson.SessionCheckIns;
                        sessionCheckOuts = objsessionGson.SessionCheckOuts;
                        sessionUserCheckIns = objsessionGson.SessionUserCheckIns;
                        sessionUserCheckOuts = objsessionGson.SessionUserCheckOuts;
                        totalCount = objsessionGson.TotalCount;
                        sessionStartTime = objsessionGson.SessionStartTime;
                        setAttendeeCheckinAndCheckout();
                        if(!Util.sessionCountPref.getString(Util.SessionId,"").equals(session_id)){
                            Util.sessionCountPref.edit().putString(Util.isSessionRefreshed,"false").commit();
                        }
                        Util.sessionCountPref.edit().putString(Util.SessionUserCheckIns,sessionUserCheckIns).commit();
                        Util.sessionCountPref.edit().putString(Util.SessionUserCheckOuts,sessionUserCheckOuts).commit();
                        Util.sessionCountPref.edit().putString(Util.SessionCheckIns,sessionCheckIns).commit();
                        Util.sessionCountPref.edit().putString(Util.SessionCheckOuts,sessionCheckOuts).commit();
                        Util.sessionCountPref.edit().putString(Util.SessionStartTime,sessionStartTime).commit();
                        Util.sessionCountPref.edit().putString(Util.TotalCount,totalCount).commit();
                        Util.sessionCountPref.edit().putString(Util.SessionId,session_id).commit();
                    }
                    if(requestType.equalsIgnoreCase("CheckInCount")){
                        dialog = new ProgressDialog(DashboardAttendeesList.this);
                        dialog.setMessage("Please wait loading attendees...");
                        dialog.setCancelable(false);
                        if (Util.isMyServiceRunning(DownloadService.class, DashboardAttendeesList.this)) {
                            requestType = Util.CHECK_SERVICE;
                            dialog.show();
                            doRequest();
                        } else {
                            getTicketCursor();
			/*UpdateBuyerCursor();
			UpdateAttendeeCursor();*/
                            // loadAttendeesInBackground();
                        }
                    }else{
                        requestType = Util.ITEMS_ORDERS_REFRESH;
                        if (isOnline()) {
                            doRequest();
                        } else {
                            if (attendee_view.isRefreshing()) {
                                attendee_view.onRefreshComplete();
                            }
                        }
                    }
                }else if (requestType.equalsIgnoreCase("Order")) {

                    TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);

                    // Log.i("---ORDER ARRAY SIZE---", "Server" +
                    // totalorderlisthandler.TotalLists.size());
                    if (totalorderlisthandler.TotalLists.size() > 0) {
                        Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                        String last_record_id = totalorderlisthandler.TotalLists
                                .get(totalorderlisthandler.TotalLists.size() - 1).orderInn.getOrderId();
                        Util._saveDataPreference(Util.offset_pref, checked_in_eventId, last_record_id);
                    }

                    if (totalorderlisthandler.ticketTags.size() > 0) {
                        Util.db.InsertAndUpdateTicketTag(totalorderlisthandler.ticketTags);
                    }
                    // swipeContainer.setRefreshing(false);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            if (!is_buyer)
                                UpdateAttendeeCursor();
                            else
                                UpdateBuyerCursor();
                        }
                    });

                } else if (requestType.equals(Util.CHECK_SERVICE)) {

                    if (response.equals("true")) {
                        doCheckBackgroundService();
                    } else {
                        dialog.dismiss();
                        UpdateAttendeeCursor();
                        getTicketCursor();
                        UpdateBuyerCursor();
                        // loadAttendeesInBackground();
                    }
                } else if (requestType.equalsIgnoreCase("Ticket")) {

                    ItemsListResponse items_response = gson.fromJson(response, ItemsListResponse.class);
                    /*
                     * TicketListResponseHandler[] responseHandler =
                     * gson.fromJson(response,
                     * TicketListResponseHandler[].class);
                     */
                    // Log.i("---TICKET ARRAY SIZE---", "" +
                    // responseHandler.length);
                    Util.db.upadteItemListRecordInDB(items_response.Itemscls_infoList, checked_in_eventId);
                    Util.db.InsertAndUpdateSEMINAR_AGENDA(items_response.agndaInfo);
                    getOrderCursor();
                } else if (requestType.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)) {

					/*if (attendee_view.isRefreshing()) {
						attendee_view.onRefreshComplete();
					}
					if (buyers_list.isRefreshing()) {
						buyers_list.onRefreshComplete();
					}
					RefreshResponse refresh = gson.fromJson(response, RefreshResponse.class);
					if (refresh.BLN_ASC_ItemsListOUTPUT != null) {
						Util.db.upadteItemListRecordInDB(refresh.BLN_ASC_ItemsListOUTPUT, checked_in_eventId);
					}
					Util.db.updateEventRefreshDate(checked_in_eventId, refresh.LastRefreshedDate);
					if (refresh.TotalLists != null) {
						Util.db.upadteOrderList(refresh.TotalLists, checked_in_eventId);
					}
					if (refresh.ticketTags != null) {
						Util.db.InsertAndUpdateTicketTag(refresh.ticketTags);
					}
					if (!is_buyer) {
						UpdateAttendeeCursor();
					} else {
						UpdateBuyerCursor();
					}*/
                } else if (requestType.equalsIgnoreCase("Check in")) {
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
                        updateCheckinView();

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
                        updateCheckinView();

						/*AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
						dialog.setParamenters("Check-in Failed",
								"This Ticket has been already Checked In" + "\nat " + dialogtime, null, null, 1, false);
						dialog.show();*/

                        Util.setCustomAlertDialog(DashboardAttendeesList.this);
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
                                    obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                                    array.put(obj);
                                    requestType = "Check in";
                                    new doTicketCheckIn((0), array.toString()).execute();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // TODO: handle exception
                                }

                            }
                        });

                        if(NullChecker(msg).isEmpty() || NullChecker(msg).equalsIgnoreCase("Already in system")){
                            if(offlineResponse.FailureTickets.get(0).tStaus.Tstatus_name__c){
                                playSound(R.raw.error);
                                if(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                    Util.openCustomDialog("Check-in Failed",
                                            "This Ticket has been already Checked In" + "\nat "
                                                    + dialogtime+". Check out is disabled.");
                                }else{
                                    Util.openCustomDialog("Check-in Failed",
                                            "This Ticket has been already Checked In" + "\nat "
                                                    + dialogtime+". Do you want to Check Out ?");
                                }

                            }else{
                                Util.openCustomDialog("Check-Out Failed",
                                        "This Ticket has been already Checked Out" + "\nat "
                                                + dialogtime+". Do you want to Check In ?");
                            }

                        }else{
                            Util.openCustomDialog("Check-in/out Failed",msg);
                        }

                    } else {
                        startErrorAnimation(offlineResponse.ErrorMsg, txt_error_msg);
                    }

                } else if (requestType.equals(Util.DELETE)) {

                    RefreshResponse refresh_res = new Gson().fromJson(response, RefreshResponse.class);
                    Util.db.deleteAttendeeInfo(checked_in_eventId, delete_attendeeId);
                    Util.db.upadteOrderList(refresh_res.TotalLists, checked_in_eventId);
                    if (!is_buyer) {
                        UpdateAttendeeCursor();
                    }
                } else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
                    TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);

                    // Log.i("---ORDER ARRAY SIZE---", "Server" +
                    // totalorderlisthandler.TotalLists.size());
                    if (totalorderlisthandler.TotalLists.size() > 0) {
                        Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                        if (!is_buyer) {
                            searchAttendee(true);
                        } else {
                            searchBuyer(true);
                        }
                    }else{
                        Toast.makeText(DashboardAttendeesList.this, "No Attendee Found!", Toast.LENGTH_LONG).show();
                    }


                } else if (requestType.equalsIgnoreCase(DBFeilds.STATUS_OFFLINE)) {
                    Gson gson = new Gson();
                    OfflineSyncResController offlineResponse = gson.fromJson(response, OfflineSyncResController.class);
                    if (offlineResponse != null) {
                        int invalid_scans = 0;
                        HashMap<String, String> duplicate_barcodes = new HashMap<>();
                        for (OfflineSyncFailuerObject syncObj : offlineResponse.FailureTickets) {

                            if (isDuplicateFailureRecord(syncObj, offlineResponse.SuccessTickets)) {
                                continue;
                            } else if(duplicate_barcodes.containsKey(syncObj.tbup.ticketId)){
                                if(duplicate_barcodes.get(syncObj.tbup.ticketId).equalsIgnoreCase(syncObj.tbup.sessionid)){
                                    continue;
                                }else{
                                    duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
                                    invalid_scans++;
                                }
                            }else{
                                duplicate_barcodes.put(syncObj.tbup.ticketId, syncObj.tbup.sessionid);
                                invalid_scans++;
                            }
                            OfflineScansObject scanObject = new OfflineScansObject();
                            scanObject.badge_id = syncObj.tbup.ticketId;
                            scanObject.badge_status = DBFeilds.STATUS_INVALID;
                            scanObject.event_id = BaseActivity.checkedin_event_record.Events.Id;
                            scanObject.item_pool_id = syncObj.tbup.freeSemPoolId;
                            scanObject.scan_date_time = Util.getOfflineSyncClientFormat(syncObj.tbup.sTime,
                                    checkedin_event_record.Events.Time_Zone__c);
                            scanObject.error = syncObj.msg;
                            scanObject.scan_group_id = syncObj.tbup.sessionid;
                            Util.db.UpdateOfflineInvalidScans(scanObject);
                        }

                        for (OfflineSyncSuccessObject syncObj : offlineResponse.SuccessTickets) {
                            if (Util.db.isItemPoolFreeSession(syncObj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c,
                                    BaseActivity.checkedin_event_record.Events.Id)) {
                                List<TStatus> session_attendees = new ArrayList<TStatus>();
                                session_attendees.add(syncObj.STicketId);
                                Util.db.InsertAndUpdateSessionAttendees(session_attendees,
                                        BaseActivity.checkedin_event_record.Events.Id);
                            } else {
                                Util.db.updateCheckedInStatus(syncObj.STicketId, checked_in_eventId);
                            }
                            Util.db.deleteOffLineScans("(" + DBFeilds.OFFLINE_BADGE_ID + " = '"
                                    + syncObj.STicketId.Ticket__r.Id + "' OR "+ DBFeilds.OFFLINE_BADGE_ID + " = '"
                                    + syncObj.STicketId.Ticket__r.Badge_ID__c + "' OR " + DBFeilds.OFFLINE_BADGE_ID
                                    + " = '" + syncObj.STicketId.Ticket__r.Custom_Barcode__c + "') AND "
                                    + DBFeilds.OFFLINE_GROUP_ID + " = '"
                                    + syncObj.STicketId.BLN_Session_user__r.BLN_Group__c + "' AND "
                                    + DBFeilds.OFFLINE_EVENT_ID + " = '" + checked_in_eventId + "'");
                        }

                        Intent i = new Intent(this, OfflineSyncDialogActivity.class);
                        String totalScans = String.valueOf(offlineList.size());
                        String successScans = String.valueOf(offlineResponse.SuccessTickets.size());
                        String faluerScans = String.valueOf(invalid_scans);
                        i.putExtra("SYNCHRESPONSE", totalScans + "," + successScans + "," + faluerScans);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            // swipeContainer.setRefreshing(false);

            if (attendee_view.isRefreshing()) {
                attendee_view.onRefreshComplete();
            }
            if (buyers_list.isRefreshing()) {
                buyers_list.onRefreshComplete();
            }
            //startErrorAnimation(getResources().getString(R.string.network_error1), txt_error_msg);
        }
    }

    private void updateView() {
        try {


            runOnUiThread(new Runnable() {

                @Override
                public void run() {

                    setEnableSessionHeadder();
                    if (search_view.getText().toString().trim().isEmpty()) {

                        if (!whereClause_name.trim().isEmpty() && att_cursor != null) {
                            att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, item_pool_id, activity_name, session_id);
                            adapter.changeCursor(att_cursor);
                            adapter.notifyDataSetChanged();
                        } else {
						/*String groupid = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
						String itempool_id="";
						if(!groupid.isEmpty()) {
							List<ScannedItems> scanned_item_list = Util.db
									.getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, groupid);
							int i = 0;
							for (ScannedItems scan_items : scanned_item_list) {
								itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
								if (i != (scanned_item_list.size() - 1)) {
									itempool_id = itempool_id + ";";
								}
								i++;
							}
						}*/
                            if (att_cursor == null) {
                                att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID + attendeeRecords + ORDERBY_CASE, item_pool_id, activity_name, session_id);
                                adapter.changeCursor(att_cursor);
                                adapter.notifyDataSetChanged();
                                /*
                                 * adapter.changeCursor(Util.db.getAttendeeDataCursor(
                                 * " where Event_Id='" + checked_in_eventId +
                                 * "'"));//ORDER BY CheckedInDate DESC
                                 */
                            } else {
                                att_cursor.close();
                                att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID + attendeeRecords + ORDERBY_CASE, item_pool_id, activity_name, session_id);
                                adapter.changeCursor(att_cursor);
                                adapter.notifyDataSetChanged();
                                // adapter.swapCursor(Util.db.getAttendeeDataCursor("
                                // where Event_Id='" + checked_in_eventId + "'"));
                                /*
                                 * adapter.changeCursor(Util.db.getAttendeeDataCursor(
                                 * WHERE_EVENTID));//
                                 * ORDER BY CheckedInDate DESC
                                 */
                            }
                        }
                        txt_title.setText("Attendees(" + att_cursor.getCount() + ")  ");

                    } else {
                        searchAttendee(false);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            OntabClick(is_buyer);
        }
    }
    private void updateCheckinView() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                setEnableSessionHeadder();
                if (search_view.getText().toString().trim().isEmpty()) {
                    String groupid = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
                    String itempool_id="";
                    if(!groupid.isEmpty()) {
                        List<ScannedItems> scanned_item_list = Util.db
                                .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, groupid);
                        int i = 0;
                        for (ScannedItems scan_items : scanned_item_list) {
                            itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
                            if (i != (scanned_item_list.size() - 1)) {
                                itempool_id = itempool_id + ";";
                            }
                            i++;
                        }
                        session_id=groupid;
                        item_pool_id=itempool_id;
                        whereClause_name="";
                    }

                    if (!whereClause_name.trim().isEmpty() && att_cursor != null) {
                        att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, item_pool_id, activity_name, session_id);
                        adapter.changeCursor(att_cursor);
                        adapter.notifyDataSetChanged();
                    } else {
                        if (att_cursor == null) {
                            att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID +attendeeRecords+ ORDERBY_CASE, itempool_id, activity_name, groupid);
                            adapter.changeCursor(att_cursor);
                            adapter.notifyDataSetChanged();
                            /*
                             * adapter.changeCursor(Util.db.getAttendeeDataCursor(
                             * " where Event_Id='" + checked_in_eventId +
                             * "'"));//ORDER BY CheckedInDate DESC
                             */
                        } else {
                            att_cursor.close();
                            att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID +attendeeRecords+ ORDERBY_CASE, itempool_id, activity_name, groupid);
                            adapter.changeCursor(att_cursor);
                            adapter.notifyDataSetChanged();
                            // adapter.swapCursor(Util.db.getAttendeeDataCursor("
                            // where Event_Id='" + checked_in_eventId + "'"));
                            /*
                             * adapter.changeCursor(Util.db.getAttendeeDataCursor(
                             * WHERE_EVENTID));//
                             * ORDER BY CheckedInDate DESC
                             */
                        }
                    }
                    txt_title.setText("Attendees(" + att_cursor.getCount() + ")  ");

                } else {
                    searchAttendee(false);
                }
            }
        });

    }

    /**
     *
     */
    private void loadAttendeesInBackground() {

        /* Starting Download Service */
        mReceiver = new DownloadResultReceiver(new Handler());
        mReceiver.setReceiver(DashboardAttendeesList.this);
        Intent intent = new Intent(Intent.ACTION_SYNC, null, DashboardAttendeesList.this, DownloadService.class);

        /* Send optional extras to Download IntentService */
        String ticURL = "";

        String offset = Util.offset_pref.getString(checked_in_eventId, "");
        /*
         * if (att_corsor.getCount() > 0) { att_corsor.moveToLast(); offset =
         * att_corsor.getString(att_corsor.getColumnIndex(DBFeilds.
         * ATTENDEE_ORDER_ID)); }
         */
        String attURL = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
                + checkedin_event_record.Events.scan_attendee_limit__c;
        if(Util.db.totalOrderCountwithoutCancelled(checked_in_eventId)==0){
            attURL = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                    + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + "&limit="
                    + checkedin_event_record.Events.scan_attendee_limit__c;
        }else {
            attURL = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                    + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
                    + checkedin_event_record.Events.scan_attendee_limit__c;
        }
        intent.putExtra(DownloadService.ATT_URL, attURL);
        intent.putExtra(DownloadService.TIC_URL, ticURL);
        intent.putExtra(DownloadService.ACCESSTOKEN, sfdcddetails.token_type + " " + sfdcddetails.access_token);
        intent.putExtra(DownloadService.EVENTID, checked_in_eventId);
        intent.putExtra(DownloadService.REQUESTTYPE, "orders");
        intent.putExtra(DownloadService.RECEIVER, mReceiver);
        intent.putExtra("requestId", 101);
        String whereClause = " where orders.Buyer_Id = user.GNUserID" + " AND orders.Event_Id = '" + checked_in_eventId
                + "' order by orders.Order_Date DESC";
        //Cursor order_cursor = Util.db.getPaymentDataCursor(whereClause);
        int totalcount=Util.db.totalOrderCountwithoutCancelled(checked_in_eventId);

		/*if (isOnline()) {
			if (totalcount < Util.dashboardHandler.totalOrders) {
				order_cursor.close();
				// Log.i("DashboardAttendeesList", "---Downloading orders---");
				startService(intent);
			} else {
				order_cursor.close();
				// Log.i("DashboardAttendeesList", "---All Records are
				// downloaded---");
			}
		}*/

    }

    public void searchBuyer(boolean isFromService) {

        String sString = search_view.getText().toString().toLowerCase();
        String fName = DBFeilds.USER_FIRST_NAME;
        String lName = DBFeilds.USER_LAST_NAME;
        // String oNmae=DBFeilds.ORDER_ORDER_NAME;
        String company = DBFeilds.USER_COMPANY;
        String email = DBFeilds.USER_EMAIL_ID;
        if(sString.contains("'")){
            sString=sString.replace("'","''");
        }
        if (!search_view.getText().toString().trim().isEmpty()) {
            whereClause = " where Event_Id = '" + checked_in_eventId + "' AND (" + fName + " like '" + "%" + sString
                    + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                    + "%"
                    // + "' OR orders."+oNmae+" like '%"+sString+"%"
                    // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                    // TRIM(user."+lName+")) like '%"+sString+"%"
                    + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                    + email + " like '%" + sString + "%')";

            if (buyer_cursor == null) {
                buyer_cursor = Util.db.getGNUser(whereClause);
            } else {
                buyer_cursor.close();
                buyer_cursor = Util.db.getGNUser(whereClause);
            }
            if (buyer_cursor.getCount() > 0) {
                buyer_adapter = new BuyerCursorAdapter(DashboardAttendeesList.this, buyer_cursor,
                        DashboardAttendeesList.this, sfdcddetails, false, checked_in_eventId, NullChecker(session_id));
                buyers_list.setAdapter(buyer_adapter);
            } else {
                txt_noattendee.setVisibility(View.VISIBLE);
                txt_noattendee.setText(R.string.no_buyer);
                txt_title.setText("Buyers(" + 0 + ")  ");
            }
			/*else {
				if (!isFromService) {
					requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
					doRequest();
				}
			}*/
        } else {
            String whereClause = " where " + DBFeilds.USER_EVENT_ID + " = '" + checked_in_eventId + "' ORDER BY "
                    + DBFeilds.USER_USERID + " DESC";
            if (buyer_cursor == null) {
                buyer_cursor = Util.db.getGNUser(whereClause);
            } else {
                buyer_cursor.close();
                buyer_cursor = Util.db.getGNUser(whereClause);
            }
            // Log.i("--------------Search Buyer Count----------", ":" +
            // buyer_cursor.getCount());
            if (buyer_cursor.getCount() > 0) {
                buyer_adapter = new BuyerCursorAdapter(DashboardAttendeesList.this, buyer_cursor,
                        DashboardAttendeesList.this, sfdcddetails, false, checked_in_eventId, NullChecker(session_id));
                buyers_list.setAdapter(buyer_adapter);
            }
        }

    }
    public void searchAttendee(boolean isFromService) {
        try {
            String sString = search_view.getText().toString().toLowerCase();
            if(sString.contains("'")){
                sString=sString.replace("'","''");
            }
            String fName = DBFeilds.ATTENDEE_FIRST_NAME;
            String lName = DBFeilds.ATTENDEE_LAST_NAME;
            String tnumber = DBFeilds.ATTENDEE_TIKCET_NUMBER;
            String company = DBFeilds.ATTENDEE_COMPANY;
            String email = DBFeilds.ATTENDEE_EMAIL_ID;
            String itempoolId = DBFeilds.ATTENDEE_ITEMPOOL_NAME;
            String seatNumber = DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER;
            String ordernumber = DBFeilds.ATTENDEE_ORDER_NUMBER;
            filter_layout.setEnabled(true);
            sort_layout.setEnabled(true);
            if (!search_view.getText().toString().trim().isEmpty()) {
                whereClause = " where Event_Id = '" + checked_in_eventId + "' AND (" + fName + " like '" + "%" + sString
                        + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                        + "%" + "' OR " + tnumber + " like '%" + sString + "%"
                        + "%" + "' OR " + ordernumber + " like '%" + sString + "%"
                        // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                        // TRIM(user."+lName+")) like '%"+sString+"%"
                        + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                        + itempoolId + " like '%" + sString + "%" + "' OR " + email + " like '%" + sString + "%' OR " + seatNumber + " like '%" + sString + "%')";
                wherefiltersearch = " (" + fName + " like '" + "%" + sString
                        + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                        + "%" + "' OR " + tnumber + " like '%" + sString + "%"
                        + "%" + "' OR " + ordernumber + " like '%" + sString + "%"
                        // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                        // TRIM(user."+lName+")) like '%"+sString+"%"
                        + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                        + itempoolId + " like '%" + sString + "%" + "' OR " + email + " like '%" + sString + "%' OR " + seatNumber + " like '%" + sString + "%')"+" AND ";

                if (!whereClause_name.trim().isEmpty() && att_cursor != null) {
                    att_cursor = Util.db.getAttendeeDataCursor(wherefiltersearch + whereClause_name, item_pool_id, activity_name, session_id);
                } else if (att_cursor == null) {
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause+ORDERBY_CASE, item_pool_id, activity_name, session_id);
                    //att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
                } else {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause+ORDERBY_CASE, item_pool_id, activity_name, session_id);
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (att_cursor.getCount() > 0) {
                            if(!is_buyer&&!isOffline)
                            {attendee_view.setVisibility(View.VISIBLE);}
                            adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                            attendee_view.setAdapter(adapter);
                            attendee_view.setVisibility(View.VISIBLE);
                            adapter.changeCursor(att_cursor);
                            adapter.notifyDataSetChanged();
                            setAttendeeCheckinAndCheckout();
                            txt_noattendee.setVisibility(View.GONE);
                        }else{
                            attendee_view.setVisibility(View.GONE);
                            txt_noattendee.setVisibility(View.VISIBLE);
                            //txt_title.setText("Attendees ("+0+")"   );
                        }
                    }});/*else {
					// request to get search from the server
					if (!isFromService) {
						requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
						doRequest();\
					}
				}*/
            } else {
                if (!whereClause_name.trim().isEmpty() && att_cursor != null) {
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, item_pool_id, activity_name, session_id);
                }else {
                    if (att_cursor == null) {
                        att_cursor = Util.db.getAttendeeDataCursor(
                                WHERE_EVENTID +attendeeRecords+ ORDERBY_CASE, item_pool_id,
                                activity_name, session_id);
                    } else {
                        att_cursor.close();
                        att_cursor = Util.db.getAttendeeDataCursor(
                                WHERE_EVENTID +attendeeRecords+ ORDERBY_CASE, item_pool_id,
                                activity_name, session_id);
                    }
                }

				/*else
					attendee_view.setVisibility(View.GONE);*/
                adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                attendee_view.setAdapter(adapter);
                if(!is_buyer&&!isOffline)
                {attendee_view.setVisibility(View.VISIBLE);}
                setAttendeeCheckinAndCheckout();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void FilterandSortFunction(){

    }
    private void SortFunction(int id, boolean isBuyer) {
        try {
            String wherebadgestatus = "", group_id = "", itempool_id = "";
            if (att_cursor != null) {
                att_cursor.close();
            }
            if (id == 1) {
                if (!isBuyer) {
                    final List<SessionGroup> group_list = Util.db.getTicketedGroupList(checked_in_eventId);
                    if (NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id).isEmpty()) {
                        itempool_id = "";
                    } else if (NullChecker(whereClause_name).isEmpty()) {

                    } else {
                        group_id = Util.db.getSwitchedONGroup(checked_in_eventId).Id;
                        List<ScannedItems> scanned_item_list = Util.db
                                .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
                        int i = 0;
                        for (ScannedItems scan_items : scanned_item_list) {
                            itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
                            if (i != (scanned_item_list.size() - 1)) {
                                itempool_id = itempool_id + ";";
                            }
                            i++;
                        }

					/*scanned_items_list.clear();
					for(int i=0;i<group_list.size();i++){
						if(group_list.get(i).equals(Util.db.getSwitchedONGroup(checked_in_eventId).Id))
							//Log.i("--------------Scanned Items Size---------", ":" + scanned_items_list.size());
							scanned_items_list.addAll(group_list.get(0).BLN_Session_Items__r.records);
					}

					for (ScannedItems scan_item : scanned_items_list) {
						scan_item.DefaultValue__c = item_pool_id.contains(scan_item.BLN_Item_Pool__c);
					}*/
                    }


                    //adapter.notifyDataSetChanged();
                    //setAttendeeCheckinAndCheckout();
                } else {
                    String whereClause = " where " + DBFeilds.USER_EVENT_ID + " = '" + checked_in_eventId + "' ORDER BY "
                            + DBFeilds.USER_USERID + " DESC";
                    if (buyer_cursor == null) {
                        buyer_cursor = Util.db.getGNUser(whereClause);
                    } else {
                        buyer_cursor.close();
                        buyer_cursor = Util.db.getGNUser(whereClause);
                    }
                    // Log.i("--------------Search Buyer Count----------", ":" +
                    // buyer_cursor.getCount());
                    if (buyer_cursor.getCount() > 0) {
                        buyer_adapter = new BuyerCursorAdapter(DashboardAttendeesList.this, buyer_cursor,
                                DashboardAttendeesList.this, sfdcddetails, false, checked_in_eventId, NullChecker(session_id));
                        buyers_list.setVisibility(View.VISIBLE);
                        buyers_list.setAdapter(buyer_adapter);
                    }

                }
            } else if (id == R.id.editsearchrecord) {
                //whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                if (!isBuyer)
                    searchAttendee(false);
                else
                    searchBuyer(false);
            } else if (id == R.id.txtsort1) {
                if (badgefilter_by.equals("PRINTED")) {
                    wherebadgestatus = " AND print_status= '" + "Printed" + "'";
                } else if (badgefilter_by.equals("NOT PRINTED")) {
                    wherebadgestatus = " AND (print_status= '') OR (print_status= '" + "Not Printed" + "')";
                }
                if (!isFilter) {
                    whereClause_name = WHERE_EVENTID+ wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE"; //"' ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE ";
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE";
                    } else if (filter_by.equals("ALL"))
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE";//+" ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE ";
                    else if (filter_by.equals("CHECKED OUT")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("false") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE ";
                    } else if (filter_by.equalsIgnoreCase("NOT REPORTED")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id NOT IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE ";
                    }

                } else {

                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("ALL")) {
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("CHECKED OUT")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("false") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equalsIgnoreCase("NOT REPORTED")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id NOT IN "
                                + innerQueryString("") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    }

                }


            } else if (id == R.id.txtsort2) {

                // Log.i("Attendee List", "R.id.txtsort2 clicked" + " isfilter= " +
                // isFilter);
                if (badgefilter_by.equals("PRINTED")) {
                    wherebadgestatus = " AND print_status= '" + "Printed" + "'";
                } else if (badgefilter_by.equals("NOT PRINTED")) {
                    wherebadgestatus = " AND (print_status= '') OR (print_status= '" + "Not Printed" + "')";
                }
                if (!isFilter) {

                    whereClause_name = WHERE_EVENTID +attendeeRecords+ "' ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE ";
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +attendeeRecords+" AND Attendee_Id IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE";
                    } else if (filter_by.equals("ALL"))
                        whereClause_name = " Where Event_Id='" + checked_in_eventId
                                + "'" + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE ";
                    else if (filter_by.equals("CHECKED OUT")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("false") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE ";
                    } else if (filter_by.equalsIgnoreCase("NOT REPORTED")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id NOT IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE ";
                    }

                } else {
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND isCheckin= '" + "true" + "'" + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("ALL")) {
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("NOT CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND isCheckin= '" + "false" + "'" + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    }
                }
            } else if (id == R.id.txtsort4) {

                // Log.i("Attendee List", "R.id.txtsort4 clicked" + " isfilter= " +
                // isFilter);
                if (badgefilter_by.equals("PRINTED")) {
                    wherebadgestatus = " AND print_status= '" + "Printed" + "'";
                } else if (badgefilter_by.equals("NOT PRINTED")) {
                    wherebadgestatus = " AND (print_status= '') OR (print_status= '" + "Not Printed" + "')";
                }
                if (!isFilter) {

                    whereClause_name = WHERE_EVENTID+" ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE ";
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE";
                    } else if (filter_by.equals("ALL"))
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE ";
                    else if (filter_by.equals("CHECKED OUT")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("false") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE ";
                    } else if (filter_by.equalsIgnoreCase("NOT REPORTED")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id NOT IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ " ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE ";
                    }


                } else {
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND isCheckin= '" + "true" + "'" + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("ALL"))
                        whereClause_name = WHERE_EVENTID + ORDERBY_CASE;
                    else if (filter_by.equals("NOT CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND isCheckin= '" + "false" + "'" + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    }
                }
            } else if (id == R.id.txtsort5) {
                // Log.i("Attendee List", "R.id.txtsort5 clicked" + " isfilter= " +
                // isFilter);
                whereClause_name = WHERE_EVENTID;

            } else if (id == R.id.txtsort6) {
                // Log.i("Attendee List", "R.id.txtsort6 clicked");
                whereClause_name = WHERE_EVENTID;

            } else if (id == R.id.txtsort3) {
                // Log.i("Attendee List", "R.id.txtsort3 clicked" + " isfilter= " +
                // isFilter);
                if (badgefilter_by.equals("PRINTED")) {
                    wherebadgestatus = " AND print_status= '" + "Printed" + "'";
                } else if (badgefilter_by.equals("NOT PRINTED")) {
                    wherebadgestatus = " AND (print_status= '') OR (print_status= '" + "Not Printed" + "')";
                }
                if (!isFilter) {
                    whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    if (filter_by.equals("CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("ALL"))
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    else if (filter_by.equals("CHECKED OUT")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id IN "
                                + innerQueryString("false") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equalsIgnoreCase("NOT REPORTED")) {
                        whereClause_name = WHERE_EVENTID +" AND Attendee_Id NOT IN "
                                + innerQueryString("true") + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    }

                } else {
                    if (filter_by.equals("CHECKED IN")) {

                        whereClause_name = WHERE_EVENTID +" AND isCheckin= '" + "true"
                                + "'" + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("ALL")) {
                        whereClause_name = WHERE_EVENTID + wherebadgestatus +attendeeRecords+ ORDERBY_CASE;
                    } else if (filter_by.equals("NOT CHECKED IN")) {
                        whereClause_name = WHERE_EVENTID + " AND isCheckin= '" + "false"
                                + "'" + wherebadgestatus + attendeeRecords + ORDERBY_CASE;
                    }
                }

            }
            if (id != R.id.editsearchrecord && id != 1) {

                if (att_cursor == null) {
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, item_pool_id, activity_name, session_id);
                } /*else if (id == R.id.txtsort3) {
					group_id = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
					if(!group_id.isEmpty()) {
						List<ScannedItems> scanned_item_list = Util.db
								.getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
						int i = 0;
						for (ScannedItems scan_items : scanned_item_list) {
							itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
							if (i != (scanned_item_list.size() - 1)) {
								itempool_id = itempool_id + ";";
							}
							i++;
						}
					}
					att_cursor.close();
					att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
							, itempool_id, activity_name, group_id);
				}*/ else {
                    att_cursor.close();
                    att_cursor = Util.db.getAttendeeDataCursor(whereClause_name, item_pool_id, activity_name, session_id);
                }

                // Log.i("Attendee List ", "Cursor size=" + att_cursor.getCount());
                // db_cursor_att = Util.db.getTotalTicketSold(att_whereClause);

                if (att_cursor != null && att_cursor.getCount() > 0) {
                    filter_layout.setEnabled(true);
                    sort_layout.setEnabled(true);
                    txt_title.setText(ITransaction.EMPTY_STRING);
                    txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                    adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                    attendee_view.setAdapter(adapter);
                    attendee_view.setVisibility(View.VISIBLE);
                    setEnableSessionHeadder();
                    txt_noattendee.setVisibility(View.GONE);
                } else {
                    txt_noattendee.setVisibility(View.VISIBLE);
                    txt_title.setText("Attendees(" + 0 + ")   ");

                }
                //item_pool_id = ITransaction.EMPTY_STRING;// added by babu to set empty item pool id when you select any tickets
            } else if (id == 1 && !isBuyer&&!isOffline) {
                if(search_view.getText().toString().isEmpty()) {
                    group_id = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
                    itempool_id="";
                    if (!group_id.isEmpty()) {
                        List<ScannedItems> scanned_item_list = Util.db
                                .getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
                        int i = 0;
                        for (ScannedItems scan_items : scanned_item_list) {
                            itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
                            if (i != (scanned_item_list.size() - 1)) {
                                itempool_id = itempool_id + ";";
                            }
                            i++;
                        }
                    }
				/*att_cursor.close();
				att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
						, itempool_id, activity_name, group_id);*/
                    if (att_cursor == null || whereClause_name.isEmpty()) {
                        att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
                                +attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
                        //att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
                    } else if (!whereClause_name.isEmpty()) {
                        att_cursor.close();
                        att_cursor = Util.db.getAttendeeDataCursor(whereClause_name//WHERE_EVENTID
                                , itempool_id, activity_name, group_id);
                    }
                    if (att_cursor != null && att_cursor.getCount() > 0) {
                        filter_layout.setEnabled(true);
                        sort_layout.setEnabled(true);
                        txt_title.setText(ITransaction.EMPTY_STRING);
                        txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                        adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                        attendee_view.setAdapter(adapter);
                        attendee_view.setVisibility(View.VISIBLE);
                        setEnableSessionHeadder();
                    } else {
                        txt_noattendee.setVisibility(View.VISIBLE);
                        txt_title.setText("Attendees(" + 0 + ") ");
                    }
                }
                else {
                    searchAttendee(false);
                }
				/*else {
					group_id = NullChecker(Util.db.getSwitchedONGroup(checked_in_eventId).Id);
					if(!group_id.isEmpty()) {
						List<ScannedItems> scanned_item_list = Util.db
								.getScannedItemsGroup(BaseActivity.checkedin_event_record.Events.Id, group_id);
						int i = 0;
						for (ScannedItems scan_items : scanned_item_list) {
							itempool_id = itempool_id + scan_items.BLN_Item_Pool__c;
							if (i != (scanned_item_list.size() - 1)) {
								itempool_id = itempool_id + ";";
							}
							i++;
						}
					}
					att_cursor.close();
					att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
							, itempool_id, activity_name, group_id);
					*//*
					att_cursor.close();
					att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID
							, itempool_id, activity_name, group_id);*//*
				}*/

            }else{

            }
        }catch (Exception e){
            e.printStackTrace();
            if (att_cursor == null || whereClause_name.isEmpty()) {
                att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID +attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
                //att_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
            } else {
                att_cursor.close();
                att_cursor = Util.db.getAttendeeDataCursor(WHERE_EVENTID+attendeeRecords+ ORDERBY_CASE, "", activity_name, "");
            }
            if (att_cursor != null && att_cursor.getCount() > 0) {
                attendee_view.setVisibility(View.VISIBLE);
                filter_layout.setEnabled(true);
                sort_layout.setEnabled(true);
                txt_title.setText(ITransaction.EMPTY_STRING);
                txt_title.setText("Attendees(" + att_cursor.getCount() + ") ");
                adapter = new AttendeeAdapter(DashboardAttendeesList.this, att_cursor);
                attendee_view.setAdapter(adapter);
                attendee_view.setVisibility(View.VISIBLE);
                setEnableSessionHeadder();
            } else {
                txt_noattendee.setVisibility(View.VISIBLE);
                txt_title.setText("Attendees(" + 0 + ")  ");
            }
        }
    }

    public String innerQueryString(String status) {
        String query = ITransaction.EMPTY_STRING;
        if (NullChecker(item_pool_id).isEmpty()) {
            if (status.isEmpty()) {
                query = "(select Ticket__c  from TStatus innercondition  Group_Id = '" + session_id
                        + "' AND T_Event_Id = '" + checked_in_eventId + "')";
            } else {
                query = "(select Ticket__c  from TStatus innercondition Tstatus_name__c = '" + status
                        + "' AND Group_Id = '" + session_id + "' AND T_Event_Id = '" + checked_in_eventId + "')";
            }

        } else if (NullChecker(item_pool_id).contains(";")) {
            String[] s = item_pool_id.split(";");
            String pool_ids = ITransaction.EMPTY_STRING;
            for (int i = 0; i < s.length; i++) {
                pool_ids = pool_ids + " BLN_Session_Item__c = '" + s[i] + "'";
                if (i != (s.length - 1)) {
                    pool_ids = pool_ids + " OR ";
                }
            }
            if (status.isEmpty()) {
                query = "(select Ticket__c  from TStatus innercondition (" + pool_ids + ") AND Group_Id = '"
                        + session_id + "'  AND T_Event_Id = '" + checked_in_eventId + "')";
            } else {
                query = "(select Ticket__c  from TStatus innercondition Tstatus_name__c = '" + status + "' AND ("
                        + pool_ids + ") AND Group_Id = '" + session_id + "'  AND T_Event_Id = '" + checked_in_eventId
                        + "')";
            }

        } else {

            if (status.isEmpty()) {
                query = "(select Ticket__c  from TStatus innercondition (BLN_Session_Item__c = '" + item_pool_id
                        + "') AND Group_Id = '" + session_id + "'  AND T_Event_Id = '" + checked_in_eventId + "')";
            } else {
                query = "(select Ticket__c  from TStatus innercondition Tstatus_name__c = '" + status
                        + "' AND (BLN_Session_Item__c = '" + item_pool_id + "') AND Group_Id = '" + session_id
                        + "'  AND T_Event_Id = '" + checked_in_eventId + "')";
            }

        }

        return query;

    }

    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        img_socket_scanner.setVisibility(View.GONE);
        img_scanner_base.setVisibility(View.GONE);
        txt_title.setText("Attendees");
        Util.slide_menu_id = R.id.listlayout;
        img_menu.setImageResource(R.drawable.top_more);
        event_layout.setVisibility(View.VISIBLE);
        img_search.setVisibility(View.VISIBLE);
        img_import.setVisibility(View.VISIBLE);
        img_backmenu.setOnClickListener(this);
        back_layout.setOnClickListener(this);
        img_import.setVisibility(View.GONE);
        lay_loadmore.setVisibility(View.VISIBLE);
        top_layout.setBackgroundResource(R.color.green_top_header);
        back_layout.setBackgroundResource(R.color.green_top_header);
        img_setting.setImageResource(R.drawable.plus);
        txt_save.setVisibility(View.GONE);
        txt_save.setText("Print");
        img_refund_history.setVisibility(View.VISIBLE);
        img_refund_history.setImageResource(R.drawable.img_offline_sync);
        txt_error_msg.setVisibility(View.GONE);
        if (BaseActivity.isEventAdmin()) {
            img_setting.setVisibility(View.VISIBLE);
        }

        // data fields for session detail
        lay_SessioHeadder = (LinearLayout) linearview.findViewById(R.id.lay_includeSessionHeadder);
        txt_session_name = (TextView) linearview.findViewById(R.id.txt_session_name);
        txt_startdate = (TextView) linearview.findViewById(R.id.txt_session_startdate);
        txt_enddate = (TextView) linearview.findViewById(R.id.txt_session_enddate);
        txt_room = (TextView) linearview.findViewById(R.id.txt_room);
        txt_roomnumber = (TextView) linearview.findViewById(R.id.txt_room_number);
        txt_checkin = (TextView) linearview.findViewById(R.id.txt_session_checkins);
        txt_checkout = (TextView) linearview.findViewById(R.id.txt_session_checkout);
        txt_total_checkin = (TextView) linearview.findViewById(R.id.txt_total_session_checkins);
        txt_total_checkout = (TextView) linearview.findViewById(R.id.txt_total_session_checkout);
        linear_dates = (LinearLayout) linearview.findViewById(R.id.linear_dates);
        linear_rooms = (LinearLayout) linearview.findViewById(R.id.linear_rooms);

        txt_noattendee = (TextView) linearview.findViewById(R.id.txtnoattendee);
        search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
        progress = (ProgressBar) linearview.findViewById(R.id.progress);
        txt_load_msg = (TextView) linearview.findViewById(R.id.txtloadmsg);
        buyers_list = (PullToRefreshListView) linearview.findViewById(R.id.buyerlistview);
        attendee_view = (PullToRefreshListView) linearview.findViewById(R.id.attendee_listView);
        list_offline = (ListView) linearview.findViewById(R.id.list_offline);
        // tab bar initialization
        tab_bar = (LinearLayout) linearview.findViewById(R.id.tab_bar);
        buyer_tab = (TextView) linearview.findViewById(R.id.tab_buyer);
        attendee_tab = (TextView) linearview.findViewById(R.id.tab_attendee);
        tablayout =(LinearLayout) linearview.findViewById(R.id.tab_layout);
        layout_session_name =(LinearLayout) linearview.findViewById(R.id.lay_session_name);
        txt_session_on_name =(TextView) linearview.findViewById(R.id.txt_session_on_name);
        offline_tab = (TextView) linearview.findViewById(R.id.tab_offline);
        buyer_strip = (TextView) linearview.findViewById(R.id.strip_buyer);
        attendee_strip = (TextView) linearview.findViewById(R.id.strip_attendee);

        img_menu.setImageResource(R.drawable.top_more);
        attendee_view.setVisibility(View.VISIBLE);
        buyers_list.setVisibility(View.GONE);
        list_offline.setVisibility(View.GONE);

        mPoppyViewHelper = new PoppyViewHelper(this, PoppyViewPosition.BOTTOM);
        View poppyView = mPoppyViewHelper.createPoppyViewOnListView(R.id.attendee_listView, R.layout.header_layout);
        sortfilterlayout = (LinearLayout) poppyView.findViewById(R.id.sortfilterlayout);
        sort_layout = (LinearLayout) poppyView.findViewById(R.id.ordersortlayout);
        filter_layout = (LinearLayout) poppyView.findViewById(R.id.orderfilterlayout);
        txt_sort = (TextView) poppyView.findViewById(R.id.txtsortorder);
        txt_filter = (TextView) poppyView.findViewById(R.id.txtfilterorder);
        txt_noattendee.setVisibility(View.GONE);
        txt_sort.setTypeface(Util.roboto_regular);
        txt_filter.setTypeface(Util.roboto_regular);

        sort_layout.setOnClickListener(this);
        filter_layout.setOnClickListener(this);

        attendee_strip.setBackgroundResource(R.color.light_blue);
        buyer_strip.setBackgroundResource(R.color.white);

        loadMoreView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.load_more_footer, null, false);
        btn_order_load_more = (LinearLayout) loadMoreView.findViewById(R.id.loadmoreLay);

        txtLoadingmore = (TextView) loadMoreView.findViewById(R.id.txt_loading);
        attendee_view.addFooterView(loadMoreView);

        // buyers_list.addFooterView(loadMoreView);

        //if (!isEventAdmin()) {

        List<RegistrationSettingsController> reg_settings = Util.db
                .getRegSettingsList("where " + DBFeilds.REG_EVENT_ID + "='" + checked_in_eventId + "' AND "
                        + DBFeilds.REG_SETTING_TYPE + "='ScanAttendeeapp'");
        for (RegistrationSettingsController settings : reg_settings) {
            boolean onlybuyertab=false;
            if (settings.Label_Name__c.equalsIgnoreCase("Display Buyers")) {
                if (!Boolean.valueOf(settings.Included__c)) {
                    buyer_tab.setVisibility(View.GONE);
                    OntabClick(false);
                }//else OntabClick(true);
            } else if (settings.Label_Name__c.equalsIgnoreCase("Display Attendees")) {
                if (!Boolean.valueOf(settings.Included__c)) {
                    attendee_tab.setVisibility(View.GONE);
                    OntabClick(true);
                }else OntabClick(false);
            }
        }
        //}


    }



    private class ViewHolder {
        TextView attName, attComp, att_checkedin, ticketType, txtseatno, txt_att_checkin, txt_delete, txt_image,att_badgestatus,txt_badgelabel;
        Button count;
        ImageView image_attendee_delete,attendee_img;
        @SuppressWarnings("unused")
        RelativeLayout checkin_layout, delete_layout;
        @SuppressWarnings("unused")
        ProgressBar checkinprogress_front;
        FrameLayout frame_line;
        FrameLayout statusbar;
        public boolean needInflate;

    }

    private class AttendeeAdapter extends CursorAdapter {

        Cursor c_new;

        public AttendeeAdapter(Context context, Cursor c) {
            super(context, c);
            this.c_new = c;

        }

        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            this.c_new = cursor;
        }

        @Override
        public int getViewTypeCount() {
            // menu type count
            return 2;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            try {
                View v = inflater.inflate(R.layout.attendee_item_layout, null, false);
                ViewHolder holder = new ViewHolder();

                holder.checkin_layout = (RelativeLayout) v.findViewById(R.id.checkin_layout);
                holder.delete_layout = (RelativeLayout) v.findViewById(R.id.deletelayout);

                holder.txt_delete = (TextView) v.findViewById(R.id.txt_att_delete);
                holder.txt_att_checkin = (TextView) v.findViewById(R.id.txt_att_checkin);
                holder.attName = (TextView) v.findViewById(R.id.txtattname);
                holder.attComp = (TextView) v.findViewById(R.id.txtattcomp);

                holder.att_checkedin = (TextView) v.findViewById(R.id.txtcheckindate);
                holder.txt_image = (TextView) v.findViewById(R.id.txt_image);
                holder.attendee_img = (ImageView) v.findViewById(R.id.attendee_img);
                // holder.image_att= (ImageView) v.findViewById(R.id.img_att);
                // holder.att_checkedin_year = (TextView)
                // v.findViewById(R.id.txtcheckin_year);
                // holder.att_checkedin_daymon = (TextView)
                // v.findViewById(R.id.txtcheckin_day_month);

                holder.ticketType = (TextView) v.findViewById(R.id.txttickettype);
                holder.image_attendee_delete = (ImageView) v.findViewById(R.id.img_attendee_delete);
                // holder.image_attendee_delete.setVisibility(View.GONE);
                holder.count = (Button) v.findViewById(R.id.btncheckin);
                holder.statusbar = (FrameLayout) v.findViewById(R.id.statusbar);
                holder.txtseatno = (TextView) v.findViewById(R.id.txtseatnum);
                holder.att_badgestatus = (TextView) v.findViewById(R.id.txtbadgeprintstatus);
                holder.txt_badgelabel = (TextView) v.findViewById(R.id.txt_badgeheading);

                // holder.delete_progress = (ProgressBar)
                // v.findViewById(R.id.deleteprogress);
                // holder.checkin_progress = (ProgressBar)
                // v.findViewById(R.id.checkinprogress);
                holder.checkinprogress_front = (ProgressBar) v.findViewById(R.id.checkinprogress_front);
                holder.frame_line = (FrameLayout) v.findViewById(R.id.frame_vertical_line);

                holder.txt_delete.setTypeface(Util.roboto_regular);
                holder.txt_att_checkin.setTypeface(Util.roboto_regular);
                holder.attName.setTypeface(Util.roboto_regular);
                holder.attComp.setTypeface(Util.roboto_regular);
                holder.att_checkedin.setTypeface(Util.roboto_regular);
                holder.ticketType.setTypeface(Util.roboto_regular);
                holder.txtseatno.setTypeface(Util.roboto_regular, Typeface.ITALIC);
                holder.att_badgestatus.setTypeface(Util.roboto_regular, Typeface.BOLD);
                holder.count.setTypeface(Util.roboto_regular, Typeface.BOLD);
                v.setTag(holder);
                return v;
            }catch (Exception e){
                e.printStackTrace();
                return v;
            }
        }

        @Override
        public void bindView(View v, Context context, final Cursor c) {

            /*
             * final View parent_view; if (v == null) { parent_view =
             * newView(context, c, null); ViewList vl = new ViewList(); vl.v =
             * parent_view; vl.id = c.getPosition();
             * viewArray.put(c.getPosition(), vl);
             *
             * } else if (((ViewHolder) v.getTag()).needInflate) { parent_view =
             * newView(context, c, null); ViewList vl = new ViewList(); vl.v =
             * parent_view; vl.id = c.getPosition(); // viewArray.add(vl);
             *
             * } else { parent_view = v; ViewList vl = new ViewList(); vl.v =
             * parent_view; vl.id = c.getPosition();
             * viewArray.put(c.getPosition(), vl); }
             */

            // holder.ThreeDimension.setVisibility(object.getVisibility());
            final ViewHolder holder = (ViewHolder) v.getTag();

            // OrderListHandler order_details =
            // Util.db.getTicketOrderDetails(c.getString(c.getColumnIndex("Order_Id")));
			/*if(!params.img_url.trim().isEmpty()){
				params.img_view.setVisibility(View.VISIBLE);
				params.layout.setVisibility(View.GONE);
			}else{
				params.img_view.setVisibility(View.GONE);
				params.layout.setVisibility(View.VISIBLE);
				params.txt_view.setText(AppUtils.NullChecker(params.fname.substring(0, 1).toUpperCase())+" "+AppUtils.NullChecker(params.lname.substring(0, 1).toUpperCase()));
*/
            if(!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_IMAGE))).isEmpty()){
                holder.txt_image.setVisibility(View.GONE);
                holder.attendee_img.setVisibility(View.VISIBLE);
                String[] fullurl=checkedin_event_record.image.split("&id=");
                String url=fullurl[0];
                Glide.with(context).load(url+"&id="+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)))
                        .dontAnimate().into(holder.attendee_img);
				/*Picasso.with(DashboardAttendeesList.this).load(url+"&id="+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)))
						.placeholder(R.drawable.default_image)
						.error(R.drawable.default_image).into(holder.attendee_img);*/
            }
            else if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))).isEmpty()
                    &&!NullChecker( c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))).isEmpty()){
                holder.attendee_img.setVisibility(View.GONE);
                holder.txt_image.setVisibility(View.VISIBLE);
                holder.txt_image
                        .setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)).substring(0, 1).toUpperCase()
                                + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)).substring(0, 1).toUpperCase());
            }
            else{
                holder.txt_image.setText("NO");

            }
            // Log.i("----Company Name---", ":" +
            // c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
            if(!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))).isEmpty()
                    &&!NullChecker( c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))).isEmpty()){
                holder.attName.setText(Html.fromHtml("<b>" + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " "
                        + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)) + "</b>"));
            }
            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))).isEmpty()) {
                holder.attComp.setVisibility(View.VISIBLE);
                holder.attComp.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
            } else {
                holder.attComp.setVisibility(View.GONE);
            }
            if(!Util.db.getSwitchedONGroup(checked_in_eventId).Name.isEmpty()&&showsessionname){
                holder.att_checkedin.setVisibility(View.VISIBLE);
            }else  if(!NullChecker(session_id).isEmpty()){
                holder.att_checkedin.setVisibility(View.VISIBLE);
            }else {
                holder.att_checkedin.setVisibility(View.GONE);
            }


            /*
             * holder.att_checkedin.setText(Util.db_date_format1 .format(new
             * Date()));
             */
            // Log.i("-----------------Event Time
            // Zone------------",":"+checkedin_event_record.Events.Time_Zone__c);
            if (!Util.NullChecker(c.getString(c.getColumnIndex("Scan_Time"))).isEmpty()) {
                // Log.i("Checked in date", ":--" +
                // c.getString(c.getColumnIndex("CheckedInDate")));
                String checkin_time = Util.change_US_ONLY_DateFormatWithSEC(
                        c.getString(c.getColumnIndex("Scan_Time")), checkedin_event_record.Events.Time_Zone__c);
                holder.att_checkedin.setText(checkin_time);
            }else{
                String checkin_time = Util.change_US_ONLY_DateFormatWithSEC(
                        c.getString(c.getColumnIndex("CheckedInDate")), checkedin_event_record.Events.Time_Zone__c);
                holder.att_checkedin.setText(checkin_time);
            }

            holder.checkin_layout.setTag(c.getPosition());
            // holder.checkinprogress_front.setTag(c.getString(c.getColumnIndex("Attendee_Id")));

            /*
             * if(!NullChecker(c.getString(c.getColumnIndex("Tikcet_Number"))).
             * isEmpty()){
             * holder.ticketType.setText(c.getString(c.getColumnIndex("Item_Id")
             * )+" - "+c.getString(c.getColumnIndex("Tikcet_Number")));
             * holder.txtseatno.setText("Seat No: "
             * +c.getString(c.getColumnIndex("Tikcet_Number"))); }else {
             * holder.ticketType.setText(c.getString(c.getColumnIndex("Item_Id")
             * )); }
             */

            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER))).isEmpty()) {
                holder.txtseatno.setVisibility(View.VISIBLE);
                holder.txtseatno.setText("Seat No:" + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER)));
            } else {
                holder.txtseatno.setVisibility(View.GONE);
            }
            holder.txt_badgelabel.setVisibility(View.VISIBLE);
            if(!NullChecker(activity_name).isEmpty()){
                if(!Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))).isEmpty()){
					/*holder.att_badgestatus.setText(Util.db.getAttendeeBadgeParentPrintStatus
							(Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))));*/
                    if(!Util.db.getAttendeeBadgeParentPrintStatus
                            (Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))).isEmpty()&&Util.db.getAttendeeBadgeParentPrintStatus
                            (Util.NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID)))).trim().equals("Printed")){
                        holder.att_badgestatus.setTextColor(getResources().getColor(R.color.green_color));
                        holder.att_badgestatus.setText("Printed");
                    }else {
                        holder.att_badgestatus.setTextColor(getResources().getColor(R.color.orange_bg));
                        holder.att_badgestatus.setText("Not Printed");
                    }
                }
                else {
                    if (!NullChecker(c.getString(
                            c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)))
                            .isEmpty() && NullChecker(c.getString(
                            c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).equals("Printed")) {
                        holder.att_badgestatus.setTextColor(getResources().getColor(R.color.green_color));
                        holder.att_badgestatus.setText("Printed");
                    } else {
                        holder.att_badgestatus.setTextColor(getResources().getColor(R.color.orange_bg));
                        holder.att_badgestatus.setText("Not Printed");
                    }
                }
            }else {
                if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).isEmpty() && NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).trim().equals("Printed")) {
                    holder.att_badgestatus.setTextColor(getResources().getColor(R.color.green_color));
                    holder.att_badgestatus.setText("Printed ");
                } else {
                    holder.att_badgestatus.setTextColor(getResources().getColor(R.color.orange_bg));
                    holder.att_badgestatus.setText("Not Printed ");
                }
            }
            if (!NullChecker(c.getString(c.getColumnIndex("Tikcet_Number"))).isEmpty()) {
                String parent_id = Util.db.getItemPoolParentId(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                if (!NullChecker(parent_id).isEmpty()) {
                    String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                    holder.ticketType.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " ( "
                            + package_name + " ) - " + Html.fromHtml("<font color=#E3E2E5>"
                            + c.getString(c.getColumnIndex("Tikcet_Number")) + "</font>"));
                } else {
                    holder.ticketType.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " - "
                            + Html.fromHtml("<font color=#E3E2E5>" + c.getString(c.getColumnIndex("Tikcet_Number"))
                            + "</font>"));
                }
            } else {
                String parent_id = Util.db.getItemPoolParentId(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                if (!NullChecker(parent_id).isEmpty()) {
                    String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                    holder.ticketType.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)) + " ( "
                            + package_name + " )");
                } else {
                    holder.ticketType.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME)));
                }

            }

            String badge_id = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGEID));
            int total_tickets = 1, total_checkin_tickets = 0,total_checkout_tickets=0,total_checkins_forunique = 0,total_checkouts_forunique=0, totalrecords=0;

            //if (NullChecker(activity_name).isEmpty()) {
            //if (!AppUtils.NullChecker(badge_id).isEmpty()) {
            if (NullChecker(activity_name).isEmpty()) {
                total_tickets = Util.db.getAttendeeTicketsCount(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId);

            }if (NullChecker(session_id).isEmpty()) {
                total_checkin_tickets = Util.db.getAttendeeCheckinTicketCountforParent(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId, NullChecker(Util.db.getSwitchedONGroupId(checked_in_eventId)));
                total_checkout_tickets = Util.db.getAttendeeCheckOutTicketCountforParent(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId, NullChecker(Util.db.getSwitchedONGroupId(checked_in_eventId)));
                if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")) {

                    total_checkins_forunique=Util.db.getAttendeeCheckinsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(Util.db.getSwitchedONGroupId(checked_in_eventId)));
                    total_checkouts_forunique=Util.db.getAttendeeCheckoutsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(Util.db.getSwitchedONGroupId(checked_in_eventId)));

                }
            } else {
                total_checkin_tickets = Util.db.getAttendeeCheckinTicketCountforParent(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId, NullChecker(session_id));
                total_checkout_tickets = Util.db.getAttendeeCheckOutTicketCountforParent(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId, NullChecker(session_id));
                if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")) {
                    total_checkins_forunique=Util.db.getAttendeeCheckinsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(session_id));
                    total_checkouts_forunique=Util.db.getAttendeeCheckoutsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(session_id));

                }
            }
				/*total_checkin_tickets = Util.db.getAttendeeCheckinTicketsCount(
						c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ID)), checked_in_eventId,NullChecker(session_id));*/
            //}

            //}
            if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")) {

                totalrecords = Util.db.getAllTicketsCountwithMailId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId);
				/*total_checkins_forunique=Util.db.getAttendeeCheckinsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
						c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
						c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(session_id));
				total_checkouts_forunique=Util.db.getAttendeeCheckoutsUniqueCount(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
						c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)),
						c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId,NullChecker(session_id));*/

            }
            String tstatus = ITransaction.EMPTY_STRING;
            if (!NullChecker(session_id).trim().isEmpty()) {
                tstatus = Util.db.getTStatusBasedOnGroup(c.getString(c.getColumnIndex("Attendee_Id")),
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId, session_id);
            } else {
                boolean isFreeSession = false;
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                if (scanticks.size() > 0) {
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                            checked_in_eventId);
                }
                if(isFreeSession) {
                    tstatus = Util.db.SessionCheckInStringStatus(
                            c_new.getString(c_new.getColumnIndex("Attendee_Id")),
                            Util.db.getSwitchedONGroupId(checked_in_eventId));
                    //tstatus=String.valueOf(ischeckin);
                }else {
                    tstatus = Util.db.getTStatusBasedOnGroup(c.getString(c.getColumnIndex("Attendee_Id")),
                            c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                }
            }
            //Log.i("-----------------TStatus Name-------------", ":" + tstatus);

            if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                holder.count.setBackgroundResource(R.drawable.green_price_bg);
                holder.txt_att_checkin.setText("Check Out");
                holder.checkin_layout.setBackgroundResource(R.color.orange_bg);

				/*if (!NullChecker(activity_name).isEmpty()) {
					holder.count.setText("1/1");
				} *//*else if (AppUtils.NullChecker(badge_id).isEmpty()) {
						holder.count.setText("1/1");
					}*//* else*/ if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")){
                    holder.count.setText(total_checkins_forunique + "/" + totalrecords);
                }else {
                    holder.count.setText(total_checkin_tickets + "/" + total_tickets);
                }

            } else if (NullChecker(tstatus).equalsIgnoreCase("false")) {
                holder.count.setBackgroundResource(R.drawable.red_price_bg);
                holder.txt_att_checkin.setText("Check In");
                holder.checkin_layout.setBackgroundResource(R.color.green_button_color);
				/*if (!NullChecker(activity_name).isEmpty()) {
					holder.count.setText("0/1");
				} *//*else if (AppUtils.NullChecker(badge_id).isEmpty()) {
						holder.count.setText("0/1");
					}*//* else*/ if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")){
                    //holder.count.setText(total_checkouts_forunique + "/" + totalrecords);
                    holder.count.setText(total_checkins_forunique + "/" + totalrecords);

                }else {
                    //					holder.count.setText(total_checkout_tickets + "/" + total_tickets);

                    holder.count.setText(total_checkin_tickets + "/" + total_tickets);
                }
            } else {

                holder.count.setBackgroundResource(R.drawable.gray_price_bg);
                holder.statusbar.setBackgroundColor(getResources().getColor(R.color.gray_color));
                holder.txt_att_checkin.setText("Check In");
                holder.checkin_layout.setBackgroundResource(R.color.gray_color);
                if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")){
                    holder.count.setText(total_checkins_forunique + "/" + totalrecords);
                }else {
					/*if (!NullChecker(activity_name).isEmpty()) {
						holder.count.setText("0/1");
					} *//*else if (AppUtils.NullChecker(badge_id).isEmpty()) {
							holder.count.setText("0/1");
						}*//* else {*/
                    holder.count.setText(total_checkin_tickets + "/" + total_tickets);
                    //}
                }

            }

            /*
             * String whereClause = " where orders.Buyer_Id = user.UserID" +
             * " AND orders.Event_Id = '" + checked_in_eventId + "' AND orders."
             * + DBFeilds.ORDER_ORDER_ID + "= '" +
             * c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)) +
             * "'order by orders.Order_Date DESC";
             *
             * if (order_cursor == null) { order_cursor =
             * Util.db.getPaymentDataCursor(whereClause); } else {
             * order_cursor.close(); order_cursor =
             * Util.db.getPaymentDataCursor(whereClause); }
             */

            String ticket_item_type = Util.db
                    .getItemTypeName(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_TYPE_ID)));
            if (ticket_item_type.equalsIgnoreCase("Package")) {
                holder.image_attendee_delete.setVisibility(View.GONE);
                holder.frame_line.setVisibility(View.GONE);
            } else {
                holder.image_attendee_delete.setVisibility(View.VISIBLE);
                holder.frame_line.setVisibility(View.VISIBLE);
            }
            if (!isEventAdmin()) {
                holder.image_attendee_delete.setVisibility(View.GONE);
                holder.frame_line.setVisibility(View.GONE);
            }

            holder.count.setTag(c.getPosition());
            holder.image_attendee_delete.setTag(c.getPosition());

            holder.count.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        c.moveToPosition((Integer) v.getTag());
                        c_new = c;
                        String badge_id,attendee_id,attendee_itempool_id,attendee_orderid,
                                attendee_firstname,attendee_lastname,attendee_mailid;
                        badge_id = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGEID));
                        attendee_id = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ID));
                        attendee_itempool_id = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID));
                        attendee_orderid = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
                        attendee_mailid = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID));
                        attendee_firstname = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME));
                        attendee_lastname = c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));


                        int tickets_count = 1;
                        if (NullChecker(activity_name).isEmpty()) {
                            //if (!AppUtils.NullChecker(badge_id).isEmpty()) {
                            tickets_count = Util.db.getAttendeeTicketsCount(attendee_id, checked_in_eventId);
                            //}
                        }
                        String orderstatus=Util.db.getOrderStatuswithAttendee(attendee_orderid,checked_in_eventId);
                        final String tstatus = Util.db.getTStatusBasedOnGroup(attendee_id, attendee_itempool_id, checked_in_eventId);
                        boolean isFreeSession = false;
                        List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                        if (scanticks.size() > 0) {
                            isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                                    checked_in_eventId);
                        }
                        int totalrecords = Util.db.getAllTicketsCountwithMailId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)),
                                c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), checked_in_eventId);

                        if(attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")&&totalrecords>1) {
                            Intent i = new Intent(DashboardAttendeesList.this, UniqueAttendeesListActivity.class);
                            i.putExtra(Util.ATTENDEE_ID, attendee_id);
                            i.putExtra(Util.GET_BADGE_ID, c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                            i.putExtra(DBFeilds.ATTENDEE_FIRST_NAME, attendee_firstname);
                            i.putExtra(DBFeilds.ATTENDEE_LAST_NAME, attendee_lastname);
                            i.putExtra(DBFeilds.ATTENDEE_EMAIL_ID, attendee_mailid);
                            startActivity(i);
                        }
                        //if (tickets_count > 1) {
                        else if (!attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ")&&tickets_count > 1) {
                            Intent i = new Intent(DashboardAttendeesList.this, MergeLevelAttendeesListActivity.class);
                            i.putExtra(Util.ATTENDEE_ID,
                                    attendee_id);
                            i.putExtra(Util.GET_BADGE_ID,
                                    c_new.getString(c_new.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)));
                            startActivity(i);
                        }else if (Util.db.getGroupCount(checked_in_eventId) == 0) {
                            showScannedTicketsAlert("Please Buy at least one scanattendee ticket to scan session.", false);
                        }else if (Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                            showScannedTicketsAlert("Please TurnON at least one session for scanning.", true);
                        } else if (isFreeSession) {
                            final boolean ischeckin = Util.db.SessionCheckInStatus(
                                    attendee_id,
                                    Util.db.getSwitchedONGroupId(checked_in_eventId));
                            if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                showMessageAlert(getString(R.string.checkin_only_msg),false);
                            }/*else if(!externalSettings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")){
								final String att_id=attendee_id;
								final List<ScannedItems> scantickss = Util.db.getSwitchedOnScanItem(checked_in_eventId);
								Util.setCustomAlertDialog(DashboardAttendeesList.this);
								Util.txt_okey.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										Util.alert_dialog.dismiss();
										if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
											showMessageAlert(getString(R.string.checkin_only_msg),false);
										}else if(isOnline()){
											JSONArray array = new JSONArray();
											JSONObject obj = new JSONObject();
											try {
												obj.put("TicketId", att_id);
												obj.put("device", "ANDROID");
												obj.put("freeSemPoolId", scantickss.get(0).BLN_Item_Pool__c);
												obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
												if (ischeckin) {
													obj.put("isCHeckIn", false);
												} else {
													obj.put("isCHeckIn", true);
												}
												obj.put("sTime", Util.getCurrentDateTimeInGMT());
											} catch (JSONException e) {
												e.printStackTrace();
											}
											array.put(obj);

											requestType = "Check in";
											new doTicketCheckIn((progress_bar_index_count), array.toString()).execute();
										}else{
											generateCsvFile(att_id);
											if(!NullChecker(session_id).isEmpty()){
												offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
														+"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
											}else{
												offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
														+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
											}
											AppUtils.displayLog("---------------Offline Size-------------",":"+offlineList.size() + list_offline);
											if (list_offline != null && offlineList.size() > 0) {
												if (!NullChecker(activity_name).isEmpty()) {
													tab_bar.setVisibility(View.VISIBLE);
													buyer_tab.setVisibility(View.GONE);
												}
												list_offline.setAdapter(new OfflineListAdapter(offlineList));
												OntabClick(false);//sai changed
												offline_tab.setVisibility(View.VISIBLE);
												img_refund_history.setVisibility(View.VISIBLE);
											}
										}
									}
								});
								Util.txt_dismiss.setOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View arg0) {
										Util.alert_dialog.dismiss();
									}
								});
								Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");
							}*/
                            else if(!NullChecker(session_id).isEmpty() && (!NullChecker(session_id).equalsIgnoreCase(Util.db.getSwitchedONGroupId(checked_in_eventId)))){
                                openScanSettingsAlert(DashboardAttendeesList.this,attendee_itempool_id,
                                        DashboardAttendeesList.class.getName());
                                Util.txt_dismiss.setVisibility(View.GONE);
                            }else if(isOnline()){
                                JSONArray array = new JSONArray();
                                JSONObject obj = new JSONObject();
                                obj.put("TicketId", attendee_id);
                                obj.put("device", "ANDROID");
                                if (Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)) {
                                    obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                } else {
                                    obj.put("freeSemPoolId", "");
                                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                }

                                if (ischeckin) {
                                    obj.put("isCHeckIn", false);
                                } else {
                                    obj.put("isCHeckIn", true);
                                }
                                obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));

                                array.put(obj);

                                requestType = "Check in";
                                new doTicketCheckIn((progress_bar_index_count), array.toString()).execute();
                            }else{
                                generateCsvFile(attendee_id);
                                if(!NullChecker(session_id).isEmpty()){
                                    offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                            +"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                }else{
                                    offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                            + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                }
                                if (list_offline != null && offlineList.size() > 0) {
                                    if (!NullChecker(activity_name).isEmpty()) {
                                        tab_bar.setVisibility(View.VISIBLE);
                                        buyer_tab.setVisibility(View.GONE);
                                    }
                                    list_offline.setAdapter(new OfflineListAdapter(offlineList));
                                    //attendee_view.setVisibility(View.GONE);
                                    offline_tab.setVisibility(View.VISIBLE);
                                    img_refund_history.setVisibility(View.VISIBLE);
                                }
                            }


                        }else if (!Util.db.isItemPoolSwitchON(
                                attendee_itempool_id,
                                checked_in_eventId)) {
                            openScanSettingsAlert(DashboardAttendeesList.this,attendee_itempool_id,
                                    DashboardAttendeesList.class.getName());
                            Util.txt_dismiss.setVisibility(View.GONE);
                        } else {
                            if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                showMessageAlert(getString(R.string.checkin_only_msg),false);
                            }else if(!externalSettings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")){
                                final String att_id=attendee_id;
                                Util.setCustomAlertDialog(DashboardAttendeesList.this);
                                Util.txt_okey.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        Util.alert_dialog.dismiss();
										/*String tstatus = Util.db.getTStatusBasedOnGroup(
												attendee_id,
												attendee_itempool_id,
												checked_in_eventId);*/
                                        if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                            showMessageAlert(getString(R.string.checkin_only_msg),false);
                                        }else if(isOnline()){
                                            JSONArray array = new JSONArray();
                                            JSONObject obj = new JSONObject();
                                            try {
                                                obj.put("TicketId", att_id);

                                                obj.put("device", "ANDROID");
                                                obj.put("freeSemPoolId", "");

                                                if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                                                    obj.put("isCHeckIn", false);
                                                } else {
                                                    obj.put("isCHeckIn", true);
                                                }
                                                obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                                obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                                obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            array.put(obj);
                                            requestType = "Check in";
                                            new doTicketCheckIn((progress_bar_index_count), array.toString()).execute();
                                        }else{
                                            generateCsvFile(att_id);
                                            if(!NullChecker(session_id).isEmpty()){
                                                offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                                        +"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                            }else{
                                                offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                                        + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                            }
                                            AppUtils.displayLog("---------------Offline Size-------------",":"+offlineList.size() + list_offline);
                                            if (list_offline != null && offlineList.size() > 0) {
                                                if (!NullChecker(activity_name).isEmpty()) {
                                                    tab_bar.setVisibility(View.VISIBLE);
                                                    buyer_tab.setVisibility(View.GONE);
                                                }
                                                list_offline.setAdapter(new OfflineListAdapter(offlineList));
                                                OntabClick(false);
                                                //attendee_view.setVisibility(View.GONE);//sai changed
                                                offline_tab.setVisibility(View.VISIBLE);
                                                img_refund_history.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }
                                });
                                Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View arg0) {
                                        Util.alert_dialog.dismiss();
                                    }
                                });
                                Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");
                            }else{
                                if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                    showMessageAlert(getString(R.string.checkin_only_msg),false);
                                }else if(isOnline()){
                                    JSONArray array = new JSONArray();
                                    JSONObject obj = new JSONObject();
                                    obj.put("TicketId", attendee_id);
                                    obj.put("device", "ANDROID");
                                    obj.put("freeSemPoolId", "");

                                    if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                                        obj.put("isCHeckIn", false);
                                    } else {
                                        obj.put("isCHeckIn", true);
                                    }
                                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                                    obj.put("sTime", Util.getCurrentDateTimeInGMT());
                                    obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));
                                    array.put(obj);
                                    requestType = "Check in";
                                    new doTicketCheckIn((progress_bar_index_count), array.toString()).execute();
                                }else{
                                    generateCsvFile(attendee_id);
                                    if(!NullChecker(session_id).isEmpty()){
                                        offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                                +"' AND "+DBFeilds.OFFLINE_GROUP_ID+" = '"+session_id+ "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                    }else{
                                        offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='" + checked_in_eventId
                                                + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                    }
                                    AppUtils.displayLog("---------------Offline Size-------------",":"+offlineList.size() + list_offline);
                                    if (list_offline != null && offlineList.size() > 0) {
                                        if (!NullChecker(activity_name).isEmpty()) {
                                            tab_bar.setVisibility(View.VISIBLE);
                                            buyer_tab.setVisibility(View.GONE);
                                        }
                                        list_offline.setAdapter(new OfflineListAdapter(offlineList));
                                        OntabClick(false);
                                        //attendee_view.setVisibility(View.GONE);//sai changed
                                        offline_tab.setVisibility(View.VISIBLE);
                                        img_refund_history.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            holder.image_attendee_delete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    selected_item_index = (Integer) v.getTag();
                    final Cursor c1 = adapter.getCursor();
                    c1.moveToPosition(selected_item_index);
                    // Log.i(user_profile.Profile.Email__c + "-----Attendee
                    // Email----",":" +
                    // c1.getString(c1.getColumnIndex(""+DBFeilds.ATTENDEE_EMAIL_ID+"")));
                    String whereClause1 = " where Event_Id = '" + c1.getString(c1.getColumnIndex("Event_Id"))
                            + "' AND Buyer_Id = '" + c1.getString(c1.getColumnIndex("buyer_id")) + "'";
                    if (!isOnline()) {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    } else if (user_profile.Profile.Email__c
                            .equalsIgnoreCase(c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_EMAIL_ID)))) {
                        AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                        dialog.setParamenters("Alert", "Event admin record can not be deleted.", null, null, 1, false);
                        dialog.show();
                    } else if (!getAttendeeDeleteData(whereClause1,
                            c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)))) {
                        AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                        dialog.setParamenters("Alert", "Paid attendee record can not be deleted.", null, null, 1,
                                false);
                        dialog.show();
                    } else {
                        final AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                        dialog.setParamenters("Alert",
                                "Are you sure do you want to delete? \n "+c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " "
                                        + c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)), null, null, 2,
                                false);
                        dialog.btnOK.setText("DELETE");
                        dialog.setAlertImage(R.drawable.error,"");
                        dialog.btnOK.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                delete_attendeeId = c1.getString(c1.getColumnIndex("Attendee_Id"));
                                requestType = Util.DELETE;
                                doRequest();
                            }
                        });
                        dialog.show();
						/*customAskDialog(
								c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " "
										+ c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)),
								"Would you like to delete? Data can not be recovered.",
								c1.getString(c1.getColumnIndex("Event_Id")),
								c1.getString(c1.getColumnIndex("Attendee_Id")), null, "Delete Attendee");*/
                    }
                }
            });

        }

    }

    public void customAskDialog(String title, String message, final String eventId, final String attendeeId,
                                final Cursor c, final String alertMode) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DashboardAttendeesList.this, R.style.CustomForDialog);
        alertDialog.setIcon(android.R.drawable.ic_dialog_info);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setInverseBackgroundForced(true);
        String no = "", yes = "";
        if (alertMode.equalsIgnoreCase("Delete Attendee")) {
            no = "Cancel";
            yes = "Delete";
        } else {
            no = "Cancel";
            yes = "Check In";
        }

        alertDialog.setNegativeButton(no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setPositiveButton(yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete_attendeeId = attendeeId;
                requestType = Util.DELETE;
                doRequest();
            }

        });

        Dialog d = alertDialog.show();

        int textViewId = d.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = (TextView) d.findViewById(textViewId);
        tv.setTextColor(getResources().getColor(R.color.blue_text_color));
        int dividerId = d.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        View divider = d.findViewById(dividerId);
        divider.setBackgroundColor(getResources().getColor(R.color.blue_text_color));
    }

    private String getDeteteAttendeeUrl() {
        return url = sfdcddetails.instance_url + WebServiceUrls.SA_DELETE_ORDER_OR_TICKET + "Event_id="
                + checked_in_eventId + "&ticketid=" + delete_attendeeId + "&orderitemid=&orderid=";

    }

    private class doTicketCheckIn extends SafeAsyncTask<String> {

        int _index = 0;
        String _body = "";

        /**
         *
         */
        public doTicketCheckIn(int index, String body) {

            this._index = index;
            this._body = body;
        }

        protected void onPreExecute() throws Exception {
            super.onPreExecute();
            dialog.setMessage("Attendee Checking In/Out...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public String call() throws Exception {
            return postTicketCheckIn(_body);
        }

        protected void onSuccess(String result) throws Exception {
            super.onSuccess(result);
            dialog.dismiss();
            if (result != null) {
                parseJsonResponse(result);
            }
        }

    }

    private String postTicketCheckIn(String body) {
        String response = "";
        try {

            HttpParams params = new BasicHttpParams();
            int timeoutconnection = 30000;
            HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
            int sockettimeout = 30000;
            HttpConnectionParams.setSoTimeout(params, sockettimeout);
            HttpClient _httpclient = HttpClientClass.getHttpClient(30000);
            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="
                    + sfdcddetails.user_id + "&eventId=" + checked_in_eventId + "&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion().replaceAll(" ", "%20")
                    +"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
            AppUtils.displayLog("--------------Check In URL-------------", ":" + _url);
            HttpPost _httppost = new HttpPost(_url);
            _httppost.addHeader("Authorization", sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("-----BEARER TOKEN---", ":" + sfdcddetails.token_type + " " + sfdcddetails.access_token);
            AppUtils.displayLog("---------------Checkin in Body--------------", body);
            _httppost.setEntity(new StringEntity(body.toString()));

            HttpResponse _httpresponse = _httpclient.execute(_httppost);
            // int _responsecode =
            // _httpresponse.getStatusLine().getStatusCode();
            // Log.i("HTTP RESPONSE CODE", ":" + _responsecode);

            response = EntityUtils.toString(_httpresponse.getEntity());
            AppUtils.displayLog("--------------Post Method Response -----------", ":" + response);

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getLocalizedMessage();
        }
        return response;

    }

    public boolean getAttendeeDeleteData(String whereClause, String name) {
        boolean candeleteAttendee = true;
        try {
            Cursor cursor = Util.db.getPaymentCursor(whereClause);

            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {

                    if (cursor.getDouble(cursor.getColumnIndex("Order_Total")) > 0) {
                        candeleteAttendee = false;
                        break;
                    }

                    cursor.moveToNext();
                }

            }
            if (!cursor.isClosed() && cursor != null)
                cursor.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        return candeleteAttendee;
    }

    public void openFilterDialog(Context ctx) {
        try {
            sort_dialog = new Dialog(ctx);
            sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            sort_dialog.setContentView(R.layout.filter_itempools_layout);

            // Grab the window of the dialog, and change the width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = sort_dialog.getWindow();
            lp.copyFrom(window.getAttributes());
            // This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            sort_dialog.getWindow().setAttributes(lp);

            final Spinner spnr_items = (Spinner) sort_dialog.findViewById(R.id.spnr_item_name);
            TextView txt_badge_status = (TextView) sort_dialog.findViewById(R.id.txt_badge_status);
            final Spinner spnr_badge_status =(Spinner) sort_dialog.findViewById(R.id.spnr_badge_status);
            Button btn_done = (Button) sort_dialog.findViewById(R.id.btn_filter_done);
            final RadioGroup radio_group=(RadioGroup) sort_dialog.findViewById(R.id.radio_group);
            final RadioButton radio_all=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_all);
            RadioButton radio_unique=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_unique);
            RadioButton radio_dublicate=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_dublicates);
            sort_dialog.show();
            final TreeMap<String, String> key_values = Util.db.getAllAttendeeItemPoolsWithNames(checked_in_eventId);
            final ArrayList<String> item_pool_names = new ArrayList<>(key_values.values());
            item_pool_names.add(0,"ALL");
            ArrayAdapter
                    <String> adapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_dropdown_item_1line,item_pool_names);
            spnr_items.setAdapter(adapter);
            int i =0;
            for(String key : key_values.keySet()){
                AppUtils.displayLog("---------------key Name--------",":"+key+" : "+key_values.get(key)+ " : "+NullChecker(item_pool_id).equalsIgnoreCase(key));
                if(NullChecker(item_pool_id).equalsIgnoreCase(key)){
                    spnr_items.setSelection(i,false);
                    break;
                }
                i++;
            }
            String[] badgestatus = getResources().getStringArray(R.array.badge_status_list);
            badgearray_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, badgestatus);
            spnr_badge_status.setAdapter(badgearray_adapter);
            int j=0;
            for(String key : badgestatus){
                if(badgefilter_by.equalsIgnoreCase(key)){
                    spnr_badge_status.setSelection(j,false);
                    break;
                }
                j++;
            }
            if(!attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" "))
                radio_all.setChecked(true);
            else {
                radio_unique.setChecked(true);
            }
            btn_done.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    sort_dialog.dismiss();
                    String spnr_selected_item = spnr_items.getSelectedItem().toString();
                    String badge_status = (String) spnr_badge_status.getSelectedItem().toString();
                    int selectedId = radio_group .getCheckedRadioButtonId();
                    switch(selectedId){
                        case R.id.radio_btn_all:
                            //if (NullChecker(spnr_selected_item).trim().equalsIgnoreCase("ALL")) {
                            if(NullChecker(activity_name).isEmpty()) {
                                attendeeRecords = " AND Badge_Parent_Id=''";
                            }
                            else{
                                attendeeRecords =" ";
                            }
                            break;
                        case R.id.radio_btn_unique:
                            //if(NullChecker(activity_name).isEmpty()) {
                            attendeeRecords = " GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ";
                            //}
                            break;
                        case R.id.radio_btn_dublicates:
                            attendeeRecords="";
                            break;
                    }
                    if(!NullChecker(spnr_selected_item).trim().equalsIgnoreCase("ALL")){
                        for(String key : key_values.keySet()){
                            if(NullChecker(spnr_selected_item).equalsIgnoreCase(key_values.get(key))){
                                item_pool_id = key;
                                break;
                            }
                        }
                    }else{
                        item_pool_id = "";
                    }
                    badgefilter_by = badge_status;
                    isFilter = true;
                    SortFunction(R.id.txtsort1, is_buyer);
                }
            });



        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    public void openFilterDialogNew(final Context ctx, final boolean isopening,boolean isNoSession) {
        try {

            sort_dialog = new Dialog(ctx);
            sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            sort_dialog.setContentView(R.layout.filter_dialog_layout_new);

            // Grab the window of the dialog, and change the width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = sort_dialog.getWindow();
            lp.copyFrom(window.getAttributes());
            // This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            sort_dialog.getWindow().setAttributes(lp);

            TextView txt_filetr_by = (TextView) sort_dialog.findViewById(R.id.txtfilterby);
            TextView txt_session_name = (TextView) sort_dialog.findViewById(R.id.txt_session_name);
            TextView txt_ticket_name = (TextView) sort_dialog.findViewById(R.id.txt_ticket_name);
            TextView txt_ticket_status = (TextView) sort_dialog.findViewById(R.id.txt_ticket_status);
            final TextView txt_badge_status = (TextView) sort_dialog.findViewById(R.id.txt_badge_status);
            final Spinner spnr_badge_status =(Spinner) sort_dialog.findViewById(R.id.spnr_badge_status);
            final TextView spnr_ticket_names = (TextView) sort_dialog.findViewById(R.id.spnr_ticket_name);
            final Spinner spnr_ticket_status = (Spinner) sort_dialog.findViewById(R.id.spnr_ticket_status);
            final Spinner spnr_session_name = (Spinner) sort_dialog.findViewById(R.id.spnr_session_name);
            final RadioGroup radio_group=(RadioGroup) sort_dialog.findViewById(R.id.radio_group);
            final RadioButton radio_all=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_all);
            RadioButton radio_unique=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_unique);
            RadioButton radio_dublicate=(RadioButton) sort_dialog.findViewById(R.id.radio_btn_dublicates);

            Button btn_done = (Button) sort_dialog.findViewById(R.id.btn_filter_done);
            TextView txt_reset = (TextView) sort_dialog.findViewById(R.id.txt_reset);
            txt_reset.setText(Html.fromHtml("<u>"+"Reset Filter"+"</u>"));
            final LinearLayout lay_tickets = (LinearLayout)sort_dialog.findViewById(R.id.lay_tickets);
            final LinearLayout layout_status = (LinearLayout) sort_dialog.findViewById(R.id.layout_status);
//
/*			LinearLayout layout_session = (LinearLayout)sort_dialog.findViewById(R.id.layout_session);
			LinearLayout layout_status = (LinearLayout) sort_dialog.findViewById(R.id.layout_status);
*/
			/*if(!NullChecker(activity_name).isEmpty()) {
				radio_all.setVisibility(View.GONE);
				radio_unique.setVisibility(View.GONE);
			}*/
            if(!attendeeRecords.equalsIgnoreCase(" GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" "))
                radio_all.setChecked(true);
            else {
                radio_unique.setChecked(true);
            }
            txt_filetr_by.setTypeface(Util.roboto_regular);
            txt_ticket_name.setTypeface(Util.roboto_regular);
            txt_ticket_status.setTypeface(Util.roboto_regular);
            txt_badge_status.setTypeface(Util.roboto_regular);
            btn_done.setTypeface(Util.roboto_regular);
            txt_session_name.setTypeface(Util.roboto_regular);

            sort_dialog.show();

            final List<SessionGroup> group_list = Util.db.getTicketedGroupList(checked_in_eventId);
            SessionGroup group = new SessionGroup();
            group.Name="ALL";
            group_list.add(0,group);
            //group_list.add(group);
            final List<ScannedItems> scanned_items_list = new ArrayList<ScannedItems>();

            SessionSpnrAdapter session_adapter = new SessionSpnrAdapter(DashboardAttendeesList.this, group_list);
            spnr_session_name.setAdapter(session_adapter);
            if (!NullChecker(activity_name).isEmpty()) {
                spnr_session_name.setEnabled(false);
            }else {
                spnr_session_name.setEnabled(true);
            }
            spnr_session_name.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (!NullChecker(session_id).equalsIgnoreCase(group_list.get(position).Id)) {
                        spnr_ticket_names.setText("ALL");
                        spnr_ticket_status.setSelection(0);
                        item_pool_id = "";

                    }
                    scanned_items_list.clear();
                    //Log.i("--------------Scanned Items Size---------", ":" + scanned_items_list.size());
                    scanned_items_list.addAll(group_list.get(position).BLN_Session_Items__r.records);
                    //Log.i("--------------After Scanned Items Size---------", ":" + scanned_items_list.size());
                    for (ScannedItems scan_item : scanned_items_list) {
						/*Log.i("--------------Selected Item pools--------", ":" + scan_item.BLN_Item_Pool__c + " : "
								+ item_pool_id.contains(scan_item.BLN_Item_Pool__c));*/

                        scan_item.DefaultValue__c = item_pool_id.contains(scan_item.BLN_Item_Pool__c);
                    }



					/*// TODO Auto-generated method stub
					//if (!NullChecker(session_id).isEmpty()&&!NullChecker(session_id).equalsIgnoreCase(group_list.get(position).Id)) {
						if (!NullChecker(session_id).equalsIgnoreCase(group_list.get(position).Id)) {
						spnr_ticket_names.setText("ALL");
						item_pool_id = "";
						*//*lay_tickets.setVisibility(View.VISIBLE);
						layout_status.setVisibility(View.VISIBLE);
					}else {
						lay_tickets.setVisibility(View.GONE);
						layout_status.setVisibility(View.GONE);*//*
					}
					scanned_items_list.clear();
					//Log.i("--------------Scanned Items Size---------", ":" + scanned_items_list.size());
					scanned_items_list.addAll(group_list.get(position).BLN_Session_Items__r.records);
					//Log.i("--------------After Scanned Items Size---------", ":" + scanned_items_list.size());
					for (ScannedItems scan_item : scanned_items_list) {
						*//*Log.i("--------------Selected Item pools--------", ":" + scan_item.BLN_Item_Pool__c + " : "
								+ item_pool_id.contains(scan_item.BLN_Item_Pool__c));*//*

						scan_item.DefaultValue__c = item_pool_id.contains(scan_item.BLN_Item_Pool__c);
					}*/

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // TODO Auto-generated method stub

                }
            });

            for (int i = 0; i < group_list.size(); i++) {
				/*if (NullChecker(session_id).isEmpty()) {
					if (Util.db.getSwitchedONGroupId(checked_in_eventId).equalsIgnoreCase(group_list.get(i).Id)) {
						spnr_session_name.setSelection(i);
						break;
					}
				} else */if (NullChecker(session_id).equalsIgnoreCase(group_list.get(i).Id)) {
                    spnr_session_name.setSelection(i);
                    break;
                }
            }

            //Log.i("---------------Item Pool in Dialog opening----------", ":" + item_pool_id);
            String item_pool_name = ITransaction.EMPTY_STRING;
            if (!NullChecker(item_pool_id).isEmpty()) {
                String[] pools = item_pool_id.split(";");
                for (int i = 0; i < pools.length; i++) {

                    String parent_id = Util.db.getItemPoolParentId(pools[i], checked_in_eventId);
                    if (!NullChecker(parent_id).isEmpty()) {
                        String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        // txt_item_poolname.setText(item_pool_name+" (
                        // "+package_name+" )");
                        item_pool_name = item_pool_name + Util.db.getItem_Pool_Name(pools[i], checked_in_eventId)
                                + " ( " + package_name + " ) ";
                    } else {
                        item_pool_name = item_pool_name + Util.db.getItem_Pool_Name(pools[i], checked_in_eventId);
                    }
                    if (i != (pools.length - 1)) {
                        item_pool_name = item_pool_name + "\n";
                    }
                }
            }
            //Log.i("---------------Item Pool in Dialog opening----------", ":" + item_pool_name);
            if (NullChecker(item_pool_name).isEmpty()) {
                spnr_ticket_names.setText("ALL");
            } else {
                spnr_ticket_names.setText(item_pool_name);
            }

            String[] s = getResources().getStringArray(R.array.status_list);
            for (int i = 0; i < s.length; i++) {
                if (s[i].equalsIgnoreCase(filter_by)) {
                    spnr_ticket_status.setSelection(i);
                    break;
                }
            }

            String[] badgestatus = getResources().getStringArray(R.array.badge_status_list);
            badgearray_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, badgestatus);
            spnr_badge_status.setAdapter(badgearray_adapter);
            int j=0;
            for(String key : badgestatus){
                if(badgefilter_by.equalsIgnoreCase(key)){
                    spnr_badge_status.setSelection(j,false);
                    break;
                }
                j++;
            }
            spnr_ticket_names.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if(spnr_session_name.getSelectedItemPosition()==0){
                        showCustomToast(DashboardAttendeesList.this, "Please Select any Session for Session Tickets!", R.drawable.img_like,R.drawable.toast_redrounded,false);
                    }else {
                        openItemPoolDialog(ctx, spnr_ticket_names, scanned_items_list);
                    }
                }
            });
            spnr_ticket_status.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if(spnr_session_name.getSelectedItemPosition()==0&&spnr_ticket_status.getSelectedItemPosition()>0){
                        spnr_ticket_status.setSelection(0);
                        showCustomToast(DashboardAttendeesList.this, "Please Select any Session for Status!", R.drawable.img_like,R.drawable.toast_redrounded,false);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                    spnr_ticket_status.setSelection(0);
                }
            });

            txt_reset.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sort_dialog.dismiss();
                    whereClause_name="";
                    item_pool_id = ITransaction.EMPTY_STRING;
                    session_id=ITransaction.EMPTY_STRING;
                    if (NullChecker(item_pool_id).isEmpty()) {
                        spnr_ticket_status.setSelection(0);
                        spnr_badge_status.setSelection(0);
                    }
                    if (NullChecker(session_id).isEmpty() && NullChecker(item_pool_id).isEmpty()) {
                        updateView();
                    }



                }
            });
            btn_done.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // Log.i("-----------------Button click------------",":");
                    sort_dialog.dismiss();
                    String ticket_status = (String) spnr_ticket_status.getSelectedItem().toString();
                    String badge_status = (String) spnr_badge_status.getSelectedItem().toString();
                    session_id = group_list.get(spnr_session_name.getSelectedItemPosition()).Id;
                    session_name = group_list.get(spnr_session_name.getSelectedItemPosition()).Name;
                    int selectedId = radio_group .getCheckedRadioButtonId();
                    switch(selectedId){
                        case R.id.radio_btn_all:
                            //if (spnr_ticket_names.getText().toString().trim().equalsIgnoreCase("ALL")&&group_list.size()==spnr_ticket_status.getCount()) {
                            if(NullChecker(activity_name).isEmpty()) {
                                attendeeRecords = " AND Badge_Parent_Id=''";
                            }else {
                                attendeeRecords =" ";
                            }

                            break;
                        case R.id.radio_btn_unique:
                            //if(NullChecker(activity_name).isEmpty()) {
                            attendeeRecords = " GROUP BY "+DBFeilds.ATTENDEE_EMAIL_ID+","+DBFeilds.ATTENDEE_FIRST_NAME+","+DBFeilds.ATTENDEE_LAST_NAME+" ";
                            //}
                            break;
                        case R.id.radio_btn_dublicates:
                            attendeeRecords="";
                            break;
                    }
                    if (spnr_ticket_names.getText().toString().trim().equalsIgnoreCase("ALL")) {
                        // If user select All changed by babu durga prasad
                        //item_pool_id = ITransaction.EMPTY_STRING; //before change it by babu durga prasad
                        if(scanned_items_list != null && scanned_items_list.size()>0){
                            for(int i=0;i< scanned_items_list.size();i++ ){
                                if(item_pool_id.length() ==0){
                                    item_pool_id =scanned_items_list.get(i).BLN_Item_Pool__c;
                                }else{
                                    item_pool_id = item_pool_id +";"+ scanned_items_list.get(i).BLN_Item_Pool__c;
                                }
                            }
                        }else{
                            item_pool_id = ITransaction.EMPTY_STRING;
                        }
                    }
                    if (NullChecker(session_id).isEmpty() && NullChecker(ticket_status).equalsIgnoreCase("ALL")	&& NullChecker(item_pool_id).isEmpty()&&NullChecker(badge_status).equalsIgnoreCase("ALL")) {
                        UpdateAttendeeCursor();
                    } else {
                        isFilter = true;
                        filter_by = ticket_status;
                        badgefilter_by=badge_status;
                        SortFunction(R.id.txtsort1, is_buyer);
                    }

                }
            });

        } catch (Exception e) {
            // TODO: handle exception
            AppUtils.displayLog("--------------Dialog Exception--------", ":" + e.getMessage());
        }
    }

    public void openItemPoolDialog(Context ctx, final TextView txt_selected_items,
                                   final List<ScannedItems> scanned_items_list) {
        try {

            final Dialog sort_dialog = new Dialog(ctx);
            sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            sort_dialog.setContentView(R.layout.filter_itempool_dialog_layout);

            // Grab the window of the dialog, and change the width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = sort_dialog.getWindow();
            lp.copyFrom(window.getAttributes());
            // This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            sort_dialog.getWindow().setAttributes(lp);
            TextView txt_filetr_by = (TextView) sort_dialog.findViewById(R.id.txtsortby);
            ListView list_view = (ListView) sort_dialog.findViewById(R.id.list_view_items);
            Button btn_done = (Button) sort_dialog.findViewById(R.id.btn_done);
            txt_filetr_by.setTypeface(Util.roboto_regular);
            sort_dialog.show();
            SessionItemSpnrAdapter adapter = new SessionItemSpnrAdapter(ctx, scanned_items_list);
            list_view.setAdapter(adapter);

            list_view.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO Auto-generated method stub
                    CheckBox check_box = (CheckBox) view.findViewById(R.id.checkBox);
                    if (check_box.isChecked()) {
                        check_box.setChecked(false);
                    } else {
                        check_box.setChecked(true);
                    }
                    scanned_items_list.get(position).DefaultValue__c = check_box.isChecked();

                }
            });
            btn_done.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    sort_dialog.dismiss();
                    String item_pool_name = ITransaction.EMPTY_STRING;
                    item_pool_id = ITransaction.EMPTY_STRING;
                    for (int i = 0; i < scanned_items_list.size(); i++) {
                        if (scanned_items_list.get(i).DefaultValue__c) {
                            item_pool_id = item_pool_id + scanned_items_list.get(i).BLN_Item_Pool__c;
                            String parent_id = Util.db.getItemPoolParentId(scanned_items_list.get(i).BLN_Item_Pool__c,
                                    checked_in_eventId);
                            if (!NullChecker(parent_id).isEmpty()) {
                                String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                                item_pool_name = item_pool_name
                                        + Util.db.getItem_Pool_Name(scanned_items_list.get(i).BLN_Item_Pool__c,
                                        scanned_items_list.get(i).BLN_Event__c)
                                        + " ( " + package_name + " ) ";
                            } else {
                                item_pool_name = item_pool_name
                                        + Util.db.getItem_Pool_Name(scanned_items_list.get(i).BLN_Item_Pool__c,
                                        scanned_items_list.get(i).BLN_Event__c);
                            }

                            if (i != (scanned_items_list.size() - 1)) {
                                item_pool_id = item_pool_id + ";";
                                item_pool_name = item_pool_name + "\n";
                            }
                        }

                    }
                    if (NullChecker(item_pool_name).isEmpty()) {
                        txt_selected_items.setText("ALL");
                    } else {
                        txt_selected_items.setText(item_pool_name);
                    }

                }
            });

        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void openSortDialog(Context ctx) {

        try {
            sort_dialog = new Dialog(ctx);
            sort_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            sort_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            sort_dialog.setContentView(R.layout.filter_dialog_layout);

            // Grab the window of the dialog, and change the width
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = sort_dialog.getWindow();
            lp.copyFrom(window.getAttributes());
            // This makes the dialog take up the full width
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            sort_dialog.getWindow().setAttributes(lp);
            FrameLayout line1 = (FrameLayout) sort_dialog.findViewById(R.id.line4);

            txt_sortby = (TextView) sort_dialog.findViewById(R.id.txtsortby);
            txt_sort1 = (TextView) sort_dialog.findViewById(R.id.txtsort1);
            txt_sort2 = (TextView) sort_dialog.findViewById(R.id.txtsort2);
            txt_sort3 = (TextView) sort_dialog.findViewById(R.id.txtsort3);
            txt_sort4 = (TextView) sort_dialog.findViewById(R.id.txtsort4);

            txt_sort_cancel = (TextView) sort_dialog.findViewById(R.id.txtsortcancel);

            txt_sortby.setText("SORT BY");
            txt_sort1.setText("FIRST NAME");
            txt_sort2.setText("LAST NAME");
            txt_sort3.setText("DATE");
            txt_sort4.setText("COMPANY");

            txt_sortby.setTypeface(Util.roboto_regular);
            txt_sort1.setTypeface(Util.roboto_regular);
            txt_sort2.setTypeface(Util.roboto_regular);
            txt_sort3.setTypeface(Util.roboto_regular);
            txt_sort4.setTypeface(Util.roboto_regular);
            // txt_sort5.setTypeface(Util.roboto_regular);
            // txt_sort6.setTypeface(Util.roboto_regular);
            txt_sort_cancel.setTypeface(Util.roboto_regular);

            line1.setVisibility(View.VISIBLE);
            txt_sort4.setVisibility(View.VISIBLE);

            sort_dialog.show();

            txt_sort1.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    isFilter = false;
                    sort_dialog.dismiss();
                    SortFunction(R.id.txtsort1, is_buyer);
                }
            });
            txt_sort2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    isFilter = false;
                    sort_dialog.dismiss();
                    SortFunction(R.id.txtsort2, is_buyer);
                }
            });
            txt_sort3.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    isFilter = false;
                    sort_dialog.dismiss();
                    SortFunction(R.id.txtsort3, is_buyer);
                }
            });
            txt_sort4.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    isFilter = false;
                    sort_dialog.dismiss();
                    SortFunction(R.id.txtsort4, is_buyer);
                }
            });
            txt_sort_cancel.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    sort_dialog.dismiss();

                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        try {

            if (v == back_layout) {
                if (mDrawerLayout.isDrawerOpen(left_menu_slider))
                    mDrawerLayout.closeDrawer(left_menu_slider);
                else
                    mDrawerLayout.openDrawer(left_menu_slider);

            } else if (v == filter_layout) {
                //!Util.db.getTicketedGroupList(checked_in_eventId).isEmpty()
                //filter changed
                //&&!NullChecker(activity_name).equalsIgnoreCase(DashboardActivity.class.getName()
				/*if(!NullChecker(session_id).isEmpty()){
					openFilterDialog(DashboardAttendeesList.this);
				}

				else*/ if (!Util.db.getTicketedGroupList(checked_in_eventId).isEmpty()) {
                    openFilterDialogNew(DashboardAttendeesList.this, true,false);
                } else {
					/*AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
					dialog.setParamenters("Alert", "Please select at least one session.", null, null, 1, false);
					dialog.show();*/
                    openFilterDialog(DashboardAttendeesList.this);
                    //openFilterDialogNew(DashboardAttendeesList.this, true,true);
                }
            } else if (v == sort_layout) {
                openSortDialog(DashboardAttendeesList.this);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /*
     * private int dp2px(int dp) { return (int)
     * TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
     * getResources().getDisplayMetrics()); }
     */

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.network.IPostResponse#insertDB()
     */
    @Override
    public void insertDB() {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!NullChecker(activity_name).equalsIgnoreCase(DashboardActivity.class.getName())) {
                Intent startentent = new Intent(DashboardAttendeesList.this, DashboardActivity.class);
                startentent.putExtra("CheckIn Event", checkedin_event_record);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startentent.putExtra(Util.HOME, "home_layout");
                startActivity(startentent);
                finish();
            }
            finish();
            hideSoftKeyboard(DashboardAttendeesList.this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.BackgroundReciver.DownloadResultReceiver.Receiver#
     * onReceiveResult(int, android.os.Bundle)
     */
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {

        String isFrom = resultData.getString("isFrom");
        if (resultCode == DownloadService.STATUS_FINISHED) {
            if(isFrom !=null && isFrom.equals("Refresh")){
                dialog.dismiss();
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
                }
            }else{
                loadAttendeesInBackground();
            }
        }else if(isFrom !=null && isFrom.equals("Refresh") && resultCode == DownloadService.STATUS_RUNNING) {
            if (!is_buyer) {
                UpdateAttendeeCursor();
            } else {
                UpdateBuyerCursor();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Log.i("---------------onActivity Result------------", ":" +
        // requestCode + " : " + resultCode);
        if (requestCode == Util.DASHBORD_ONACTIVITY_REQ_CODE) {
            Intent i = new Intent(DashboardAttendeesList.this, SplashActivity.class);
            startActivity(i);
            finish();

        }else if(requestCode == 2017 && resultCode == 2017){
            isFromAttendDetails = true;
        }else if(requestCode == 2018 && resultCode == 2018) {
            isFromBuyerDetails = true;
        }else if(requestCode == 2019 && requestCode == 2019){
            isOffline=true;
        }
    }

    private class ViewList {
        View v;
        int id;

        @Override
        public String toString() {
            return "ViewList [v=" + v + ", id=" + id + "]";
        }
    }
    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.BackgroundReciver.CheckServiceInterface#
     * CheckServiceRequest()
     */

    private void setEnableSessionHeadder() {
        //Log.i("------------- setEnableSessionHeadder -----------", ":" + item_pool_id);

        if (NullChecker(activity_name).equalsIgnoreCase(DashboardActivity.class.getName())) {
            lay_SessioHeadder.setVisibility(View.VISIBLE);
            //OntabClick(false);
            if (!NullChecker(session_id).isEmpty()) {
                txt_session_name.setText(NullChecker(session_name));
                setAttendeeCheckinAndCheckout();
                linear_dates.setVisibility(View.GONE);
                linear_rooms.setVisibility(View.GONE);
            } else if (!NullChecker(item_pool_id).isEmpty()) {
                //Log.i("@@@@@@@@@@@@@@@@@@@@@ setEnableSessionHeadder -----------", ":" + (lay_SessioHeadder.getVisibility() == View.VISIBLE));
                setAttendeeCheckinAndCheckout();
                setSessionHeadderData(item_pool_id);
            }
        } else {
            lay_SessioHeadder.setVisibility(View.GONE);
        }
    }

    public void setSessionHeadderData(String item_pool_id) {
        txt_session_name.setText(Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId) + " ( "
                + Util.db.getGroup(checked_in_eventId, session_id).Name + " ) ");
        SeminaAgenda agenda = Util.db
                .getSeminarAgenda("where " + DBFeilds.SEMINAR_ITEM_POOL_ID + "='" + item_pool_id + "'");

        if (agenda != null) {
            String start_date = Util.change_US_ONLY_DateFormat(agenda.startTime,
                    checkedin_event_record.Events.Time_Zone__c);
            String end_date = Util.change_US_ONLY_DateFormat(agenda.endtime,
                    checkedin_event_record.Events.Time_Zone__c);
            txt_startdate.setText(Html
                    .fromHtml("<font color=#DF6B1E> Start Date : </font><font color=#000000>" + start_date + "</font>"));
            txt_enddate.setText(
                    Html.fromHtml("<font color=#DF6B1E> End  Date : </font><font color=#000000>" + end_date + "</font>"));
            txt_room.setText(
                    Html.fromHtml("<font color=#DF6B1E> Room: </font><font color=#000000>" + agenda.room + "</font>"));
            txt_roomnumber.setText(Html.fromHtml(
                    "<font color=#DF6B1E> Room Number: </font><font color=#000000>" + agenda.roomNo + "</font>"));
        } else {
            linear_dates.setVisibility(View.GONE);
            linear_rooms.setVisibility(View.GONE);
        }
    }

    public void setAttendeeCheckinAndCheckout() {
        //Log.i("---------------setAttendeeCheckinAndCheckout--------", ":" + lay_SessioHeadder.isShown());
        if (lay_SessioHeadder.getVisibility() == View.VISIBLE) {
            int checkin_count=0,checkout_count=0;
            //	final boolean isRecordExists = Util.db.isRecordExists(DBFeilds.TABLE_SCANNED_TICKETS, " where "+DBFeilds.SCANNED_ITEM_POOL_ID+"='"+item_pool_id+"' AND "+DBFeilds.SCANNED_EVENT_ID+" = '"+checked_in_eventId+"'");
			/*String ip = item_pool_id;
			if(ip !=null && ip.contains(";")){
				String names[] = ip.split(";");
				ip = names[0];
			}*/
            final boolean isRecordExists = Util.db.isRecordExists(DBFeilds.TABLE_SCANNED_TICKETS, " where "+DBFeilds.SCANNED_GROUP_ID+"='"+NullChecker(session_id)+"' AND "+DBFeilds.SCANNED_EVENT_ID+" = '"+checked_in_eventId+"'");

            //	final boolean hasPermission = ;
            //if(!NullChecker(session_id).isEmpty() && (!NullChecker(session_id).equalsIgnoreCase(Util.db.getSwitchedONGroupId(checked_in_eventId))) && !isRecordExists){
            if(!isRecordExists){
				/*checkin_count = Util.db.getTstatusCountForItemPoolId(NullChecker(session_id), item_pool_id,
						checked_in_eventId, "true","no permission");// c_checkin.getCount();
				checkout_count = Util.db.getTstatusCountForItemPoolId(NullChecker(session_id), item_pool_id,
						checked_in_eventId, "false","no permission");
				if(isOnline()){
					if(isFrom !=null && isFrom.equals("Group")){
						txt_checkin.setText("My Check-ins: " + sessionCheckIns);
						txt_checkout.setText("My Check-outs: " + sessionCheckOuts);
					}else if(isFrom !=null && isFrom.equals("Child")){
						txt_checkin.setText("My Check-ins: " + checkin_count);
						txt_checkout.setText("My Check-outs: " + checkout_count);
					}

				}else{
					txt_checkin.setText("My Check-ins: " + checkin_count);
					txt_checkout.setText("My Check-outs: " + checkout_count);
				}*/
                txt_checkin.setText("My Check-ins: " + 0);
                txt_checkout.setText("My Check-outs: " + 0);

            }else{


                checkin_count = Util.db.getTstatusCountForItemPoolId(NullChecker(session_id), item_pool_id,
                        checked_in_eventId, "true",sfdcddetails.user_id);// c_checkin.getCount();
                checkout_count = Util.db.getTstatusCountForItemPoolId(NullChecker(session_id), item_pool_id,
                        checked_in_eventId, "false",sfdcddetails.user_id);

                txt_checkin.setText("My Check-ins: " + checkin_count);
                txt_checkout.setText("My Check-outs: " + checkout_count);

            }


            if(isOnline()){
                int totalCheckIn = 0, totalCheckOut = 0;
                if(sessionCheckIns !=null && !sessionCheckIns.equals("")){
                    totalCheckIn = Integer.parseInt(sessionCheckIns);
                }
                if(sessionCheckOuts !=null && !sessionCheckOuts.equals("")){
                    totalCheckOut = Integer.parseInt(sessionCheckOuts);
                }
                String toatlCheckint = (totalCheckIn+totalCheckOut)+"/" + totalCount;
				/*if(toatlCheckint.equals("0/0")){
					toatlCheckint = "-";
				}*/
                txt_total_checkin.setText("(Total Check-ins: "+toatlCheckint+")");


            }else{
                int totalCheckIn = 0, totalCheckOut = 0;
                String chkin  = Util.sessionCountPref.getString(Util.SessionCheckIns,"0");
                String chkout = Util.sessionCountPref.getString(Util.SessionCheckOuts,"0");

                if(chkin !=null && !chkin.equals("")){
                    totalCheckIn = Integer.parseInt(chkin);
                }
                if(chkout !=null && !chkout.equals("")){
                    totalCheckOut = Integer.parseInt(chkout);
                }
                String toatlCheckint = (totalCheckIn+totalCheckOut)+"/" + Util.sessionCountPref.getString(Util.TotalCount,"0");
				/*if(toatlCheckint.equals("0/0")){
					toatlCheckint = "-";
				}*/
                txt_total_checkin.setText("Total Check-ins: "+toatlCheckint);

            }

            //txt_total_checkout.setText("Total Check-out: " + checkout_count);
        }
    }

    private class OfflineListAdapter extends BaseAdapter {

        private List<OfflineScansObject> mOfflineList;

        public OfflineListAdapter(List<OfflineScansObject> offlineList) {
            mOfflineList = offlineList;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mOfflineList.size();
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public OfflineScansObject getItem(int position) {
            // TODO Auto-generated method stub
            return mOfflineList.get(position);
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        /*
         * (non-Javadoc)
         *
         * @see android.widget.Adapter#getView(int, android.view.View,
         * android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View converterView, ViewGroup viewGroup) {
            try {


                final HolderView viewHolder;
                if (converterView == null) {
                    converterView = inflater.inflate(R.layout.offline_list_item, null);
                    viewHolder = new HolderView();
                    viewHolder.txt_offlineBarcodeName = (TextView) converterView.findViewById(R.id.txt_offlinename);
                    viewHolder.txt_scanDateTime = (TextView) converterView.findViewById(R.id.txt_dateTime);
                    viewHolder.txt_status = (TextView) converterView.findViewById(R.id.txt_status);
                    viewHolder.txt_error = (TextView) converterView.findViewById(R.id.txt_offlineerror);
                    viewHolder.img_delete = (ImageView) converterView.findViewById(R.id.img_delete);
                    viewHolder.txt_image = (TextView) converterView.findViewById(R.id.txt_image);
                    viewHolder.txt_offline_tag = (TextView) converterView.findViewById(R.id.txt_offline_tag);
                    converterView.setTag(viewHolder);
                } else {
                    viewHolder = (HolderView) converterView.getTag();
                }

                OfflineScansObject offlineobj = mOfflineList.get(position);
                if (!NullChecker(offlineobj.name).isEmpty()) {
                    viewHolder.txt_offlineBarcodeName.setText(offlineobj.name);
                    viewHolder.txt_image.setText(offlineobj.name.split(" ")[0].substring(0, 1).toUpperCase()
                            + offlineobj.name.split(" ")[1].substring(0, 1).toUpperCase());
                } else {
                    viewHolder.txt_offlineBarcodeName.setText(offlineobj.badge_id);
                    viewHolder.txt_image.setText("NO");
                }

                /*
                 * if(offlineobj.item_pool_id.contains(",")){ String item_pool_names
                 * = ITransaction.EMPTY_STRING; for(int
                 * i=0;i<offlineobj.item_pool_id.split(",").length;i++){
                 * item_pool_names =
                 * item_pool_names+Util.db.getItem_Pool_Name(offlineobj.item_pool_id
                 * .split(",")[i],checked_in_eventId); if(i !=
                 * (offlineobj.item_pool_id.split(",").length-1)){ item_pool_names =
                 * item_pool_names+","; } }
                 * viewHolder.txt_status.setText(item_pool_names); }else{
                 * viewHolder.txt_status.setText(Util.db.getItem_Pool_Name(
                 * offlineobj.item_pool_id, checked_in_eventId)); }
                 */
                viewHolder.txt_status.setText(Util.db.getGroup(checked_in_eventId, offlineobj.scan_group_id).Name);

                String checkin_time = Util.changeGMTtoEventTimeZone(offlineobj.scan_date_time,
                        checkedin_event_record.Events.Time_Zone__c);
                viewHolder.txt_scanDateTime.setText(checkin_time);

                if (offlineobj.badge_status.equals(DBFeilds.STATUS_INVALID)) {
                    viewHolder.txt_offline_tag.setTextColor(getResources().getColor(R.color.orange_bg));
                    //Log.i("-----------------Badge Invalid----------", ":" + offlineobj.scan_date_time);
                } else {
                    viewHolder.txt_offline_tag.setTextColor(getResources().getColor(R.color.red));
                    //Log.i("-----------------Badge Valid----------", ":" + offlineobj.scan_date_time);
                }
                viewHolder.txt_offline_tag.setText(offlineobj.badge_status.toUpperCase());
                //viewHolder.txt_offline_tag.setText(offlineobj.badge_status.toUpperCase()+offlineobj.scandevicemode+Util.getDeviceNameandAppVersion());

                viewHolder.img_delete.setTag(position);
			/*if (!NullChecker(offlineobj.error).isEmpty()) {
				viewHolder.txt_error.setVisibility(View.VISIBLE);
				viewHolder.txt_error.setText(offlineobj.error);
			} else {*/
                viewHolder.txt_error.setVisibility(View.GONE);
                if (!NullChecker(Util.db.getAttendeeCompany(offlineobj.badge_id)).isEmpty()) {
                    viewHolder.txt_error.setText(Util.db.getAttendeeCompany(offlineobj.badge_id));
                    viewHolder.txt_error.setVisibility(View.VISIBLE);
                }

                //}
                viewHolder.img_delete.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {

                        final AlertDialogCustom dialog = new AlertDialogCustom(DashboardAttendeesList.this);
                        dialog.setParamenters("Alert", "Do you want to delete this badge?", null, null, 2, false);
                        dialog.setFirstButtonName("DELETE");
                        dialog.btnOK.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View arg0) {
                                dialog.dismiss();
                                OfflineScansObject offlineobj = mOfflineList.get((Integer) viewHolder.img_delete.getTag());
                                Util.db.deleteOffLineScans(DBFeilds.OFFLINE_EVENT_ID + " = '" + offlineobj.event_id
                                        + "' AND " + DBFeilds.OFFLINE_BADGE_ID + " = '" + offlineobj.badge_id + "' AND "
                                        + DBFeilds.OFFLINE_ITEM_POOL_ID + " = '" + offlineobj.item_pool_id + "' AND " + DBFeilds.OFFLINE_SCAN_TIME + " = '" + offlineobj.scan_date_time + "'");

                                offlineList = Util.db.getOfflineScans(" Where " + DBFeilds.OFFLINE_EVENT_ID + " ='"
                                        + checked_in_eventId + "' ORDER BY " + DBFeilds.OFFLINE_SCAN_TIME + " DESC", true);
                                txt_title.setText("OFFLINE SCANS" + "(" + offlineList.size() + ")"   );
                                list_offline.setAdapter(new OfflineListAdapter(offlineList));
                                if (offlineList.size() > 0) {
                                    //attendee_view.setVisibility(View.GONE);
                                    offline_tab.setVisibility(View.VISIBLE);
                                    OnOfflineTabClick();//sai changed
                                    img_refund_history.setVisibility(View.VISIBLE);
                                } else {
                                    img_refund_history.setVisibility(View.GONE);
                                    list_offline.setVisibility(View.GONE);
                                    offline_tab.setVisibility(View.GONE);
                                    OntabClick(false);
                                    if (!NullChecker(activity_name).isEmpty()) {
                                        tab_bar.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });

                        dialog.btnCancel.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                });

                return converterView;
            } catch (Exception e) {
                e.printStackTrace();
                return converterView;
            }
        }
    }

    private class HolderView {
        TextView txt_offlineBarcodeName, txt_scanDateTime, txt_status, txt_error, txt_image, txt_offline_tag;
        ImageView img_delete;

    }

}

