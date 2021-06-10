package com.globalnest.scanattendee;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.globalnest.BackgroundReciver.DownloadResultReceiver;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sailakshmi on 27-06-2017.
 */

public class SelfCheckinAttendeeList extends BaseActivity implements OnClickListener, DownloadResultReceiver.Receiver{
    LinearLayout selfcheckinsearchlayout,animation_selcheckinlayout,lay_sideeventlogo,lay_topeventlogo;
    ImageView selfcheckin_eventlogo,selfcheckin_eventlogoside;
    TextView txt_noattendee,selfcheckin_eventname;//selfcheckin_eventnameside,
    LinearLayout full_selfcheckinlayout,registerbuttonlayout,scanningbuttonlayout,hint_msglayout;
    Button registerbutton,scanningbutton,btn_search;
    AutoCompleteTextView selfcheckineditsearchrecord;
    private String session_id = ITransaction.EMPTY_STRING, activity_name = ITransaction.EMPTY_STRING;
    private ListView attendee_selfview;
    private Cursor att_cursor;
    private AttendeeAdapter adapter;
    private String whereClause = "";
    private String requestType = "";
    private ProgressBar progress;
    private TextView txt_load_msg,txt_searchagainmsg;
    private static final String ORDERBY_CASE = " ORDER BY CASE  WHEN Scan_Time NOT NULL THEN Scan_Time  ELSE  CheckedInDate END DESC";
    private String item_pool_id = ITransaction.EMPTY_STRING;
    private ProgressDialog progressDialog;
    String order_id="";
    private TotalOrderListHandler totalorderlisthandler;
    boolean islocalsearch=false;
    private View loadMoreView;
    private ImageView img_load_more;
    WindowManager winMan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            setCustomContentView(R.layout.selfcheckin_list_layout);
            winMan = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

            if (winMan != null) {
                int orientation = winMan.getDefaultDisplay().getOrientation();
                if (orientation == 0) {
                    animation_selcheckinlayout.setGravity(Gravity.CENTER);
                    /*lay_topeventlogo.setVisibility(View.VISIBLE);
                    lay_sideeventlogo.setVisibility(View.GONE);
                */ } else if (orientation == 1) {
                    animation_selcheckinlayout.setGravity(Gravity.TOP|Gravity.CENTER);
                    /*lay_topeventlogo.setVisibility(View.GONE);
                    lay_sideeventlogo.setVisibility(View.VISIBLE);*/

                }         // lay_sideeventlogo.setVisibility(View.VISIBLE);
            }
            order_id = getIntent().getStringExtra(Util.ORDER_ID);
            if (!NullChecker(order_id).isEmpty()) {
                Intent endintent = new Intent(SelfCheckinAttendeeList.this, OrderSucessPrintActivity.class);
                endintent.putExtra(Util.ORDER_ID, order_id);
                endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(endintent);
            }
            if (Util.db.totalOrderCount(checked_in_eventId) == 0) {
                requestType = "Order";
                if (isOnline()) {
                    doRequest();
                }
            }/*else if (Util.db.totalOrderCount(checked_in_eventId) < Util.dashboardHandler.totalOrders) {
            requestType = "Order";
            doRequest();
        }*/
            if (NullChecker(item_pool_id).isEmpty() && !NullChecker(session_id).isEmpty()) {
                AppUtils.displayLog("-----------Session Name in If-----------", ":");
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
            back_layout.setOnClickListener(this);
            img_setting.setOnClickListener(this);
            selfcheckineditsearchrecord.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (before > count && Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)) {
                        islocalsearch = false;
                    }
                    selfcheckineditsearchrecord.setFocusable(true);
                    if (Util.getselfcheckinbools(Util.ISSELFCHECKIN) && selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                        txt_noattendee.setVisibility(View.GONE);
                        att_cursor = null;
                        adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                        attendee_selfview.setAdapter(adapter);
                        attendee_selfview.setVisibility(View.GONE);
                        lay_topeventlogo.setVisibility(View.VISIBLE);
                        setselfcheckinbuttons();
                    } else if (selfcheckineditsearchrecord.getText().toString().trim().length() > 1 && before < count && !Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)) {
                        setselfcheckinbuttons();
                        SortFunction(R.id.selfcheckineditsearchrecord);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            attendee_selfview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> _adapter, View view, int position, long index) {
                    Cursor c1 = adapter.getCursor();
                    c1.moveToPosition(position);
                    Intent att_activity = new Intent(SelfCheckinAttendeeList.this, SelfCheckinAttendeeDetailActivity.class);
                    att_activity.putExtra("EVENT_ID", c1.getString(c1.getColumnIndex("Event_Id")));
                    att_activity.putExtra("ATTENDEE_ID", c1.getString(c1.getColumnIndex("Attendee_Id")));
                    att_activity.putExtra("ORDER_ID", c1.getString(c1.getColumnIndex("Order_Id")));
                    att_activity.putExtra(Util.INTENT_KEY_2, session_id);
                    startActivityForResult(att_activity, 2017);

                }
            });
            if (Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)) {
                String hinttext = "", comma = ", ";
                if (Util.getselfcheckinbools(Util.FIRSTNAMESEARCH)) {
                    hinttext = "First Name";
                }
                if (Util.getselfcheckinbools(Util.LASTNAMESEARCH)) {
                    if (!hinttext.trim().toString().isEmpty()) {
                        hinttext = hinttext + comma + "Last Name";
                    } else {
                        hinttext = hinttext + "Last Name";
                    }
                }
                if (Util.getselfcheckinbools(Util.COMPANYSEARCH)) {
                    if (!hinttext.trim().toString().isEmpty()) {
                        hinttext = hinttext + comma + "Company";
                    } else {
                        hinttext = hinttext + "Company";
                    }
                }
                if (Util.getselfcheckinbools(Util.EMAILIDSEARCH)) {
                    if (!hinttext.trim().toString().isEmpty()) {
                        hinttext = hinttext + comma + "Email Id";
                    } else {
                        hinttext = hinttext + "Email Id";
                    }
                }
                if (Util.getselfcheckinbools(Util.ORDERIDSEARCH)) {
                    if (!hinttext.trim().toString().isEmpty()) {
                        hinttext = hinttext + comma + "Order Id";
                    } else {
                        hinttext = hinttext + "Order Id";
                    }
                }
                if (Util.getselfcheckinbools(Util.TICKETNOSEARCH)) {
                    if (!hinttext.trim().toString().isEmpty()) {
                        hinttext = hinttext + comma + "Ticket No";
                    } else {
                        hinttext = hinttext + "Ticket No";
                    }
                }
                selfcheckineditsearchrecord.setHint("Search by " + hinttext);
            }

            selfcheckineditsearchrecord.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH && !selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                        requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                        doRequest();
                        return true;
                    } else if (selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                        Toast.makeText(SelfCheckinAttendeeList.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
                    }
                    return false;
                }
            });
            selfcheckineditsearchrecord.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getX() > (view.getWidth() - 50)) {
                            ((AutoCompleteTextView) view).setText("");
                            selfcheckineditsearchrecord.setText("");
                            islocalsearch = false;
                            txt_noattendee.setVisibility(View.GONE);
                            att_cursor = null;
                            adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                            attendee_selfview.setAdapter(adapter);
                            attendee_selfview.setVisibility(View.GONE);
                            lay_topeventlogo.setVisibility(View.VISIBLE);
                            hideSoftKeyboard(SelfCheckinAttendeeList.this);
                            setselfcheckinbuttons();
                        }
                    }
                    return false;
                }
            });
            registerbutton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> paid_items_ids = new HashMap<String, String>();
                    paid_items_ids = Util.db.getSelfCheckinPaidItemNameAndId(checked_in_eventId);
                    HashMap<String, String> free_items_ids = new HashMap<String, String>();
                    free_items_ids = Util.db.getSelfCheckinFreeItemNameAndId(checked_in_eventId);
                    //Util.getselfcheckinbools(Util.ISPAIDTICKETSALLOWED)&& removed because of refresh problem
                    if (Util.db.isCreditCardPaymentON(checked_in_eventId) && paid_items_ids.size() > 0) {
                        Intent addattendee = new Intent(SelfCheckinAttendeeList.this, SelfcheckinTicketslistActivity.class);
                        startActivity(addattendee);
                    } else if (free_items_ids.size() > 0) {
                        Intent addattendee = new Intent(SelfCheckinAttendeeList.this, AddAttendeeActivity.class);
                        startActivity(addattendee);
                    }
                }
            });
            btn_search.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)) {
                        if (!selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                            if (islocalsearch) {
                                //hint_msglayout.setVisibility(View.GONE);
                                islocalsearch = false;
                                requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                                doRequest();
                            } else {
                                islocalsearch = true;
                                //hint_msglayout.setVisibility(View.VISIBLE);
                                searchselfcheckinAttendee(true);
                            }
                        } else {
                            Toast.makeText(SelfCheckinAttendeeList.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (!selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                            requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                            doRequest();
                        } else {
                            Toast.makeText(SelfCheckinAttendeeList.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
                        }
                    }
                }

            });
            scanningbutton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        if (AppUtils.isCamPermissionGranted(SelfCheckinAttendeeList.this)) {
                            frontcam = true;
                   /* if(Util.db.getGroupCount(checked_in_eventId) == 0){
                        showScannedTicketsAlert("Please Buy at least one scanattendee ticket to scan session.",false);
                    }else if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
                        showScannedTicketsAlert("Please TurnON at least one session for scanning.",true);
                    }else if(Util.isMyServiceRunning(DownloadService.class, SelfCheckinAttendeeList.this)){
                        showServiceRunningAlert(checkedin_event_record.Events.Name);
                    }else{*/
                            Intent i = new Intent(SelfCheckinAttendeeList.this, BarCodeScanActivity.class);
                            i.putExtra(Util.SCAN, "scan_layout");
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                        //}
                        else {
                            AppUtils.giveCampermission(SelfCheckinAttendeeList.this);
                        }

                    }
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void SortFunction(int id) {
        if (id == R.id.selfcheckineditsearchrecord) {
            searchselfcheckinAttendee(false);
        }
    }

    public void searchselfcheckinAttendee(boolean searchvalidation){
        try {
            String sString = selfcheckineditsearchrecord.getText().toString().trim().toLowerCase();
            if (sString.contains("'")) {
                sString = sString.replace("'", "''");
            }
            String fName = DBFeilds.ATTENDEE_FIRST_NAME;
            String lName = DBFeilds.ATTENDEE_LAST_NAME;
            String tnumber = DBFeilds.ATTENDEE_TIKCET_NUMBER;
            String company = DBFeilds.ATTENDEE_COMPANY;
            String email = DBFeilds.ATTENDEE_EMAIL_ID;
            String itempoolId = DBFeilds.ATTENDEE_ITEMPOOL_NAME;
            String seatNumber = DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER;
            String ordernumber = DBFeilds.ATTENDEE_ORDER_NUMBER;
            selfcheckineditsearchrecord.setEnabled(true);
            if (!selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
                if(Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)) {
                    String clause="",addor=" OR ";
                    if(Util.getselfcheckinbools(Util.FIRSTNAMESEARCH)&&Util.getselfcheckinbools(Util.LASTNAMESEARCH)){
                        clause = clause+ fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%'" ;
                    }
                    if (Util.getselfcheckinbools(Util.FIRSTNAMESEARCH)) {
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause +addor+ fName + " like '" + "%" + sString + "%'";
                        }else {
                            clause = clause + fName + " like '" + "%" + sString + "%'";
                        }

                    }
                    if (Util.getselfcheckinbools(Util.LASTNAMESEARCH)) {
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause+addor+ lName + " like '" + "%" + sString + "%'";
                        }else {
                            clause = clause+ lName + " like '" + "%" + sString + "%'";
                        }
                    }
                    if (Util.getselfcheckinbools(Util.EMAILIDSEARCH)) {
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause+addor+"lower(" + email + ") = '"  + sString + "'";
                        }else {
                            clause = clause+ "lower(" +email + ") = '" + sString + "'";
                        }
                    }
                    if (Util.getselfcheckinbools(Util.COMPANYSEARCH)) {
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause+addor+ company + " like '" + "%" + sString + "%'";
                        }else {
                            clause = clause+ company + " like '" + "%" + sString + "%'";
                        }
                    }
                    if (Util.getselfcheckinbools(Util.TICKETNOSEARCH)) {
                        String tktid="";
                        tktid=sString;
                        tktid=tktid.toLowerCase().replaceAll("t", "").replaceAll("k", "").replaceAll("-", "");
                        tktid="tkt-"+tktid;
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause+addor+"lower(" + tnumber + ") = '" + tktid + "'";
                        }else {
                            clause = clause+ "lower(" +tnumber + ") = '"  + tktid + "'";
                        }
                    } if (Util.getselfcheckinbools(Util.ORDERIDSEARCH)) {
                        String orderid="";
                        orderid=sString;
                        orderid=orderid.toLowerCase().replace("o", "").replaceAll("-", "");
                        orderid="o-"+orderid;
                        if(!clause.trim().toString().isEmpty()){
                            clause = clause+addor+"lower(" +ordernumber + ") = '"  + orderid + "'";
                        }else {
                            clause = clause+"lower(" +ordernumber + ") = '" + orderid + "'";
                        }
                    }

                    whereClause = " where Event_Id = '" + checked_in_eventId + "' AND ("+clause+")";
                }
                else {
                    whereClause = " where Event_Id = '" + checked_in_eventId + "' AND (" + fName + " like '" + "%" + sString
                            + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                            + "%" + "' OR " + tnumber + " like '%" + sString + "%"
                            + "%" + "' OR " + ordernumber + " like '%" + sString + "%"
                            // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                            // TRIM(user."+lName+")) like '%"+sString+"%"
                            + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                            + itempoolId + " like '%" + sString + "%" + "' OR " + email + " like '%" + sString + "%' OR "
                            + seatNumber + " like '%" + sString + "%')";

                }
                if (att_cursor == null) {
                    att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(whereClause, item_pool_id, activity_name, session_id);

                } else {
                    att_cursor.close();
                    att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(whereClause, item_pool_id, activity_name, session_id);

                }

                if (att_cursor.getCount() > 0) {
                    txt_noattendee.setVisibility(View.GONE);
                    adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                    attendee_selfview.setVisibility(View.VISIBLE);
                    if (winMan != null) {
                        int orientation = winMan.getDefaultDisplay().getOrientation();
                        if (orientation == 0) {
                            lay_topeventlogo.setVisibility(View.VISIBLE);
                        }  else if (orientation == 1) {
                            lay_topeventlogo.setVisibility(View.GONE);
                        }
                    }
                    registerbuttonlayout.setVisibility(View.GONE);
                    scanningbuttonlayout.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                    params.gravity = Gravity.TOP;
                    animation_selcheckinlayout.setLayoutParams(params);
                    selfcheckinsearchlayout.setVisibility(View.VISIBLE);
                    selfcheckin_eventname.setText(checkedin_event_record.Events.Name);
                    //selfcheckin_eventnameside.setText(checkedin_event_record.Events.Name);
                    setEventLogo();
                    attendee_selfview.setAdapter(adapter);
                } else {
                    /*if (searchvalidation) {
                        requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                        doRequest();
                    }else*/
                    txt_noattendee.setVisibility(View.VISIBLE);
                }/*else {

                    // request to get search from the server
                    if (!isFromService) {
                        requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                        doRequest();
                    }
                }*/
            } else {
                if (att_cursor == null) {
                    att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(whereClause, item_pool_id, activity_name, session_id);

                } else {
                    att_cursor.close();
                    att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(whereClause, item_pool_id, activity_name, session_id);

                }
                attendee_selfview.setVisibility(View.VISIBLE);
                lay_topeventlogo.setVisibility(View.GONE);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                params.gravity = Gravity.TOP;
                animation_selcheckinlayout.setLayoutParams(params);
                selfcheckinsearchlayout.setVisibility(View.VISIBLE);
                adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                attendee_selfview.setAdapter(adapter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setCustomContentView(int layout) {
        try{
            activity = this;
            View v = inflater.inflate(layout, null);
            linearview.addView(v);
            txt_title.setText(checkedin_event_record.Events.Name);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            img_menu.setImageResource(R.drawable.back_button);
            img_setting.setVisibility(View.VISIBLE);
            img_setting.setImageResource(R.drawable.dashboardrefresh);
            progress = (ProgressBar) linearview.findViewById(R.id.progress);
            txt_load_msg = (TextView) linearview.findViewById(R.id.txtloadmsg);
            attendee_selfview = (ListView) linearview.findViewById(R.id.attendee_selflistView);
            selfcheckinsearchlayout =(LinearLayout) linearview.findViewById(R.id.selfcheckinsearchlayout);
            animation_selcheckinlayout =(LinearLayout) linearview.findViewById(R.id.animation_selcheckinlayout);
            lay_sideeventlogo =(LinearLayout) linearview.findViewById(R.id.lay_sideeventlogo);
            lay_topeventlogo =(LinearLayout) linearview.findViewById(R.id.lay_topeventlogo);
            selfcheckin_eventlogo =(ImageView) linearview.findViewById(R.id.selfcheckin_eventlogo);
            selfcheckin_eventlogoside =(ImageView) linearview.findViewById(R.id.selfcheckin_eventlogoside);
            selfcheckin_eventname =(TextView) linearview.findViewById(R.id.selfcheckin_eventname);
            //selfcheckin_eventnameside =(TextView) linearview.findViewById(R.id.//selfcheckin_eventnameside);
            txt_noattendee =(TextView) linearview.findViewById(R.id.txt_noattendee);
            full_selfcheckinlayout =(LinearLayout) linearview.findViewById(R.id.full_selfcheckinlayout);
            selfcheckineditsearchrecord =(AutoCompleteTextView) linearview.findViewById(R.id.selfcheckineditsearchrecord);
            registerbuttonlayout =(LinearLayout) linearview.findViewById(R.id.btn_register_layout);
            scanningbuttonlayout =(LinearLayout) linearview.findViewById(R.id.btn_scanning_layout);
            // hint_msglayout =(LinearLayout)linearview.findViewById(R.id.hint_msg);
            registerbutton =(Button) linearview.findViewById(R.id.btn_register);
            scanningbutton =(Button) linearview.findViewById(R.id.btn_scanning);
            btn_search =(Button) linearview.findViewById(R.id.btn_search);
            loadMoreView = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.load_more_footer, null, false);
            img_load_more = (ImageView) loadMoreView.findViewById(R.id.btn_order_load_more);
            img_load_more.setBackgroundDrawable(getResources().getDrawable(R.drawable.search));
            txt_searchagainmsg = (TextView) loadMoreView.findViewById(R.id.txt_loading);
            txt_searchagainmsg.setText("If you still can't find, please click on Search again to retrieve from server!");
            attendee_selfview.addFooterView(loadMoreView);
            setselfcheckinbuttons();
            loadMoreView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                    doRequest();
                }
            });
            selfcheckin_eventname.setText(checkedin_event_record.Events.Name);
            //selfcheckin_eventnameside.setText(checkedin_event_record.Events.Name);
            if (!NullChecker(checkedin_event_record.selfcheckinbgcolor).isEmpty()&&NullChecker(checkedin_event_record.selfcheckinbgcolor).contains("#")){
                full_selfcheckinlayout.setBackgroundColor(Color.parseColor(checkedin_event_record.selfcheckinbgcolor));
            }
            else if (!NullChecker(checkedin_event_record.SADashboardimgUrl).isEmpty()&&NullChecker(checkedin_event_record.SADashboardimgUrl).length()>8) {
                String[] fullurl=NullChecker(checkedin_event_record.image).split("&id=");
                String url=fullurl[0];
                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(fullurl[0]+"&id="+checkedin_event_record.SADashboardimgUrl))
                        .placeholder(R.drawable.selfcheckinattendeebackgroud).error(R.drawable.selfcheckinattendeebackgroud).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                        full_selfcheckinlayout.setBackground(new BitmapDrawable(bitmap));
                    }
                    @Override
                    public void onBitmapFailed(Drawable drawable) {
                    }
                    @Override
                    public void onPrepareLoad(Drawable drawable) {
                    }
                });
            }/*else if(!NullChecker(checkedin_event_record.SADashboardimgUrl).isEmpty()){
                full_selfcheckinlayout.setBackgroundColor(Color.parseColor("#"+checkedin_event_record.SADashboardimgUrl));
            }*/else {
                full_selfcheckinlayout.setBackground(getResources().getDrawable(R.drawable.selfcheckinattendeebackgroud));
            }
            /*if (!NullChecker(checkedin_event_record.SADashboardimgUrl).isEmpty()&&NullChecker(checkedin_event_record.SADashboardimgUrl).length()>8) {

                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(checkedin_event_record.SADashboardimgUrl))
                        .placeholder(R.drawable.selfcheckinattendeebackgroud).error(R.drawable.selfcheckinattendeebackgroud).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
                        full_selfcheckinlayout.setBackground(new BitmapDrawable(bitmap));
                    }
                    @Override
                    public void onBitmapFailed(Drawable drawable) {
                    }
                    @Override
                    public void onPrepareLoad(Drawable drawable) {
                    }
                });
            }else if(!NullChecker(checkedin_event_record.SADashboardimgUrl).isEmpty()){
                full_selfcheckinlayout.setBackgroundColor(Color.parseColor("#"+checkedin_event_record.SADashboardimgUrl));
            }else {
                full_selfcheckinlayout.setBackground(getResources().getDrawable(R.drawable.selfcheckinattendeebackgroud));
            }*/
            setEventLogo();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private class ViewHolder {
        TextView attName, attComp, ticketType, txtseatno, txt_image,att_badgestatus,txt_badgelabel;
        ImageView image_attendee_delete,attendee_img;
        FrameLayout statusbar;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            animation_selcheckinlayout.setGravity(Gravity.TOP|Gravity.CENTER);           // lay_sideeventlogo.setVisibility(View.VISIBLE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            animation_selcheckinlayout.setGravity(Gravity.CENTER);  }
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

            View v = inflater.inflate(R.layout.selfcheckin_attendee_item_layout, null, false);
            ViewHolder holder = new ViewHolder();
            holder.attName = (TextView) v.findViewById(R.id.txtattname);
            holder.attComp = (TextView) v.findViewById(R.id.txtattcomp);
            holder.txt_image = (TextView) v.findViewById(R.id.txt_image);
            holder.attendee_img = (ImageView) v.findViewById(R.id.attendee_img);
            holder.ticketType = (TextView) v.findViewById(R.id.txttickettype);
            holder.image_attendee_delete = (ImageView) v.findViewById(R.id.img_attendee_delete);
            holder.statusbar =(FrameLayout) v.findViewById(R.id.statusbar);
            holder.txtseatno = (TextView) v.findViewById(R.id.txtseatnum);
            holder.att_badgestatus = (TextView) v.findViewById(R.id.txtbadgeprintstatus);
            holder.txt_badgelabel = (TextView) v.findViewById(R.id.txt_badgeheading);
            holder.attName.setTypeface(Util.roboto_regular);
            holder.attComp.setTypeface(Util.roboto_regular);
            holder.ticketType.setTypeface(Util.roboto_regular);
            holder.txtseatno.setTypeface(Util.roboto_regular);
            holder.att_badgestatus.setTypeface(Util.roboto_regular, Typeface.BOLD);
            v.setTag(holder);
            return v;

        }

        @Override
        public void bindView(View v, Context context, final Cursor c) {

            final ViewHolder holder = (ViewHolder) v.getTag();
            if(!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_IMAGE))).isEmpty()){
                holder.txt_image.setVisibility(View.GONE);
                holder.attendee_img.setVisibility(View.VISIBLE);
                String[] fullurl=NullChecker(checkedin_event_record.image).split("&id=");
                String url=fullurl[0];
                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(url)+"&id="+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_IMAGE)))
                        .placeholder(R.drawable.default_image)
                        .error(R.drawable.default_image).into(holder.attendee_img);
            }
            else if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))).isEmpty()
                    &&!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))).isEmpty()){
                holder.attendee_img.setVisibility(View.GONE);
                holder.txt_image.setVisibility(View.VISIBLE);

                holder.txt_image
                        .setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)).substring(0, 1).toUpperCase()
                                + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)).substring(0, 1).toUpperCase());
            }
            else{
                holder.txt_image.setText("NO");

            }
            if(!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))).isEmpty()
                    &&!NullChecker( c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))).isEmpty()){
                holder.attName.setText(Html.fromHtml("<b>" + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " "
                        + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)) + "</b>"));
            }
            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))).isEmpty()) {
                holder.attComp.setVisibility(View.VISIBLE);
                holder.attComp.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY)));
            } else {
                holder.attComp.setVisibility(View.INVISIBLE);
            }


            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER))).isEmpty()) {
                holder.txtseatno
                        .setText("Seat No:" + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TICKET_SEAT_NUMBER)));
            } else {
                holder.txtseatno.setVisibility(View.INVISIBLE);
            }
            holder.txt_badgelabel.setVisibility(View.VISIBLE);
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
            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))).isEmpty()) {
                String parent_id = Util.db.getItemPoolParentId(
                        c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                if (!NullChecker(parent_id).isEmpty()) {
                    String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                    holder.ticketType.setText(Html.fromHtml("<Html> <font>"+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER)) + " ( "
                            + package_name + " ) " + "<small> (" +c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+") </small> </font> </Html>"));
                } else {
                    holder.ticketType.setText(Html.fromHtml("<Html> <font>"+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+//"-"+
                            "<small> (" +c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+") </small> </font> </Html>"));
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
            holder.statusbar.setVisibility(View.VISIBLE);
            if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                holder.statusbar.setBackgroundColor(getResources().getColor(R.color.event_date_green__dark_bg));
            }else if (NullChecker(tstatus).equalsIgnoreCase("false")) {
                holder.statusbar.setBackgroundColor(getResources().getColor(R.color.red));
            }else{
                holder.statusbar.setBackgroundColor(getResources().getColor(R.color.gray_color));

            }
        }

    }
    @Override
    public void onClick(View v) {
        try{
            if(v == back_layout){
                hideSoftKeyboard(SelfCheckinAttendeeList.this);
                selfcheckineditsearchrecord.setText("");
                txt_noattendee.setVisibility(View.GONE);
                //islocalsearch=false;
                setselfcheckinbuttons();
                attendee_selfview.setVisibility(View.GONE);
                lay_topeventlogo.setVisibility(View.VISIBLE);
                try {
                    att_cursor=null;
                    adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                    attendee_selfview.setAdapter(adapter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                openSelfCheckinDialog(SelfCheckinAttendeeList.this,false,Util.selfcheckinpref.getString(Util.PASSWORD,""));

            }else if(v == img_setting){
                if (isOnline()) {
                    requestType = Util.ITEMS_ORDERS_REFRESH;
                    doRequest();
                }else{
                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK&&Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            selfcheckineditsearchrecord.setText("");
            //islocalsearch=false;
            txt_noattendee.setVisibility(View.GONE);
            hideSoftKeyboard(SelfCheckinAttendeeList.this);
            setselfcheckinbuttons();
            attendee_selfview.setVisibility(View.GONE);
            lay_topeventlogo.setVisibility(View.VISIBLE);
            try {
                att_cursor=null;
                adapter = new AttendeeAdapter(SelfCheckinAttendeeList.this, att_cursor);
                attendee_selfview.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
            hideSoftKeyboard(SelfCheckinAttendeeList.this);
            openSelfCheckinDialog(SelfCheckinAttendeeList.this,false,Util.selfcheckinpref.getString(Util.PASSWORD,""));

        }
        return super.onKeyDown(keyCode, event);
    }
    private void dismissProgressDialog() {
        if(progressDialog!=null) {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String isFrom = resultData.getString("isFrom");
        if (resultCode == DownloadService.STATUS_FINISHED) {
            if(isFrom !=null && isFrom.equals("Refresh")){
                progressDialog.dismiss();
                UpdateAttendeeCursor();
            }
        }else if(isFrom !=null && isFrom.equals("Refresh") && resultCode == DownloadService.STATUS_RUNNING) {
            UpdateAttendeeCursor();
        }
    }
    /* @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         // Checks the orientation of the screen
         if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
             setCustomContentView(R.layout.selfcheckin_list_land);
         } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
             setCustomContentView(R.layout.selfcheckin_list_layout);
         }
     }*/
    @Override
    protected void onResume() {
        super.onResume();
        try {
            frontcam=false;
            selfcheckineditsearchrecord.setText("");
            islocalsearch=false;
            txt_noattendee.setVisibility(View.GONE);
            hideSoftKeyboard(SelfCheckinAttendeeList.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void doRequest() {
        if(isOnline()) {
            String msg = "";
            String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
            if (requestType.equalsIgnoreCase("Order")) {
                txt_load_msg.setText("Loading Attendees...");
                msg = "Loading Attendees...";
                txt_title.setText(checkedin_event_record.Events.Name);
                String offset = Util.offset_pref.getString(checked_in_eventId, "");
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                        + checked_in_eventId + "&User_id=" + sfdcddetails.user_id + "&offset=" + offset + "&limit="
                        + checkedin_event_record.Events.scan_attendee_limit__c;
                postMethod = new HttpPostData(msg, url, null, access_token, SelfCheckinAttendeeList.this);
                postMethod.execute();

            } else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
                progressDialog =new ProgressDialog(SelfCheckinAttendeeList.this);
                progressDialog.setMessage("Searching Attendee.....");
                progressDialog.setCancelable(false);
                progressDialog.show();
                ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
                // Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
                Call<TotalOrderListHandler> call = apiService.getSearchAttendees(checked_in_eventId,sfdcddetails.user_id,selfcheckineditsearchrecord.getText().toString().trim(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
                if(AppUtils.isLogEnabled) {
                    AppUtils.displayLog(call + "------ Url-------", url);
                    AppUtils.displayLog(call + "------JSON Retrofit-------", getSearchValues().toString());
                }
                call.enqueue(new Callback<TotalOrderListHandler>() {
                    @Override
                    public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
                        Log.e(call+"------success-------", "------response started-------");
                        if(AppUtils.isLogEnabled){AppUtils.displayLog(call+"------JSON Response-------", response.toString());}
                        try {
                            if (!isValidResponse(response.toString())) {
                                dismissProgressDialog();
                                openSessionExpireAlert(errorMessage(response.toString()));
                            } else if (response.code() == 200) {
                                totalorderlisthandler = response.body();
                                if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
                                    playSound(R.raw.somethingwentwrong);
                                    Toast.makeText(SelfCheckinAttendeeList.this, totalorderlisthandler.errorMsg, Toast.LENGTH_LONG).show();
                                    // openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
                                    dismissProgressDialog();
                                } else {
                                    if (totalorderlisthandler.TotalLists.size() > 0) {
                                        Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                                        searchselfcheckinAttendee(false);
                                    } else {
                                        Toast.makeText(SelfCheckinAttendeeList.this, "No Attendee Found!", Toast.LENGTH_LONG).show();
                                        txt_noattendee.setVisibility(View.VISIBLE);
                                    }
                                    dismissProgressDialog();

                                }
                            }
                            else {
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
               /* String url = sfdcddetails.instance_url + WebServiceUrls.SA_SEARCH_ATTENDEE + getSearchValues();
                postMethod = new HttpPostData("Loading Search...", url, null, access_token, SelfCheckinAttendeeList.this);
                postMethod.execute();*/
            } else if (requestType.equalsIgnoreCase(Util.ITEMS_ORDERS_REFRESH)) {
                progressDialog = new ProgressDialog(SelfCheckinAttendeeList.this);
                progressDialog.setMessage("Please wait refreshing attendees...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                DownloadResultReceiver mReceiver = new DownloadResultReceiver(new Handler());
                mReceiver.setReceiver(SelfCheckinAttendeeList.this);
                Intent intent = new Intent(Intent.ACTION_SYNC, null, SelfCheckinAttendeeList.this, DownloadService.class);
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
                intent.putExtra(DownloadService.ACTIVITY_NAME, SelfCheckinAttendeeList.class.getName());
                intent.putExtra(DownloadService.reload, "");
                intent.putExtra("requestId", 101);
                startService(intent);

            }
        }else{
            startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
        }
    }
    private String getValues(String batchCount) {
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
                values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
            }
        }else{
            values.add(new BasicNameValuePair("LastModifiedDate", checkedin_event_record.lastRefreshDate));
        }
        values.add(new BasicNameValuePair("Request_Flag", "Itemandattendees"));
        values.add(new BasicNameValuePair("startbatch", batchCount));
        return AppUtils.getQuery(values);
    }
    private String getSearchValues() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("search_string", selfcheckineditsearchrecord.getText().toString().trim()));
        return AppUtils.getQuery(values);
    }
    @Override
    public void parseJsonResponse(String response) {
        try {
            if (requestType.equalsIgnoreCase("Order")) {

                TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);

                if (totalorderlisthandler.TotalLists.size() > 0) {
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                    String last_record_id = totalorderlisthandler.TotalLists
                            .get(totalorderlisthandler.TotalLists.size() - 1).orderInn.getOrderId();
                    Util._saveDataPreference(Util.offset_pref, checked_in_eventId, last_record_id);
                }

                if (totalorderlisthandler.ticketTags.size() > 0) {
                    Util.db.InsertAndUpdateTicketTag(totalorderlisthandler.ticketTags);
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        UpdateAttendeeCursor();

                    }
                });

            } else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
                TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
                if (totalorderlisthandler.TotalLists.size() > 0) {
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                } else {
                    /*showCustomToast(SelfCheckinAttendeeList.this,
                            "No Attendee Found!",
                            R.drawable.ic_action_user, R.color.event_date_green__dark_bg,true);*/
                    Toast.makeText(SelfCheckinAttendeeList.this, "No Attendee Found!", Toast.LENGTH_LONG).show();
                }
                searchselfcheckinAttendee(false);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void UpdateAttendeeCursor() {
        if (selfcheckineditsearchrecord.getText().toString().trim().isEmpty()) {
            selfcheckineditsearchrecord.setText("");
            txt_noattendee.setVisibility(View.GONE);
            String query = " where Event_Id='" + checked_in_eventId + "'"+ORDERBY_CASE;
            if (att_cursor == null) {
                att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(query, item_pool_id, activity_name,session_id);
            } else {
                att_cursor.close();
                att_cursor = Util.db.getSelfCheckinAttendeeDataCursor(query, item_pool_id, activity_name,session_id);
            }
            txt_title.setText(checkedin_event_record.Events.Name);

        } else {
            searchselfcheckinAttendee(false);
        }
    }
    @Override
    public void insertDB() {

    }
    private void setselfcheckinbuttons(){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        float d = getResources().getDisplayMetrics().density;
        params.bottomMargin=(int)(80 * d);
        params.gravity=Gravity.CENTER;
        animation_selcheckinlayout.setLayoutParams(params);
        LinearLayout.LayoutParams params1=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin=(int)(180 * d);
        selfcheckinsearchlayout.setLayoutParams(params1);
        if(Util.getselfcheckinbools(Util.ISADDGUEST)){
            registerbuttonlayout.setVisibility(View.VISIBLE);
        }else{
            registerbuttonlayout.setVisibility(View.INVISIBLE);
        }
        if((Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT))||(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN))){
            scanningbuttonlayout.setVisibility(View.VISIBLE);
        }else{
            scanningbuttonlayout.setVisibility(View.INVISIBLE);
        }
        /*if(Util.getselfcheckinbools(Util.ISSEARCHVALIDATIONON)){
            btn_search.setVisibility(View.VISIBLE);
        }*/
    }
    private void setEventLogo(){
        try {
            String[] fullurl = {};
            if(!NullChecker(checkedin_event_record.selfcheckineventlogo).isEmpty()||!NullChecker(checkedin_event_record.image).isEmpty()) {
                fullurl = BaseActivity.NullChecker(checkedin_event_record.image).split("&id=");
            }
            if(!NullChecker(checkedin_event_record.selfcheckineventlogo).isEmpty()&&!NullChecker(fullurl[0]).trim().isEmpty()) {
                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(fullurl[0]+"&id="+checkedin_event_record.selfcheckineventlogo))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(selfcheckin_eventlogo);
                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(fullurl[0]+"&id="+checkedin_event_record.selfcheckineventlogo))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(selfcheckin_eventlogoside);
            }
            else if (!NullChecker(fullurl[1]).trim().isEmpty()) {

                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(checkedin_event_record.image))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(selfcheckin_eventlogo);
                Picasso.with(SelfCheckinAttendeeList.this).load(NullChecker(checkedin_event_record.image))
                        .placeholder(R.drawable.default_image).error(R.drawable.default_image).into(selfcheckin_eventlogoside);

            } else {
                selfcheckin_eventlogo.setVisibility(View.GONE);
                selfcheckin_eventlogoside.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
