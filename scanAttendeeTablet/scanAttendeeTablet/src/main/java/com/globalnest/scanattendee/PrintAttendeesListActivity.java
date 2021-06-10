package com.globalnest.scanattendee;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.brother.ptouch.sdk.printdemo.common.MsgDialog;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.classes.QRCodeEncoder;
import com.globalnest.data.Contents;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeCreation;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.PrintStatusObject;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.scroll.PoppyViewHelper;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class PrintAttendeesListActivity extends BaseActivity {
    // scanning fields
    private HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> tickets_registerfor_print = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> backupticketsregisterfor_print = new HashMap<String, Boolean>();

    private HttpPostData postMehod;
    private TotalOrderListHandler totalorderlisthandler;
    private Cursor attendee_cursor;
    // private Dialog ticket_dialog;
    private ImageView  img_print_icon;
    private TextView txtprint;
    private TextView  txt_oops;
    private ListCheckInAdapter checkin_adapter;
    //private ImageView tick_img;
    // private String _checked_in_eventId, _user_id;

    private boolean isOrderScaneed=false;// back_type = false;
    @SuppressWarnings("unused")
    private String  QrId = "", orderId = "", url = "",
            checked_time = "", requestType = "", reason = "",qrcodename="";
    private BadgeListAdapter badgelistadapter;
    // private LayoutParams lp;
    private ArrayList<String> mFiles = new ArrayList<String>();
    private Cursor payment_cursor;
    private SearchThread searchPrinter;
    //private FrameLayout print_badge;
    private ListView  ticket_view;
    private FrameLayout linear_badge_parent;
    private ArrayList<String> qrcode_name;
    private ArrayList<String> attendee_id,AllPrintedattendeeids;
    private ArrayList<ReasonHandler> reaseon_array;
    private ArrayList<String> badgelabel_list = new ArrayList<String>();
    String itempoolid="",badge_status="";
    @SuppressWarnings("unused")
    //private ArrayList<String> badge_id;
            boolean isclicked = false;
    private Intent data;
    private AlertDialog.Builder print_dialog;
    private int dialog_count = 0;
    //private boolean isLastPosition=false;
    private ArrayList<BadgeResponseNew>  badge_res = new ArrayList<BadgeResponseNew>();
    ArrayList<FrameLayout> badge_frame_layout = new ArrayList<FrameLayout>();
    BadgeCreation badge_creator;
    ExternalSettings ext_settings;
    ArrayList<ReasonHandler> resonNameList ;
    ReasonHandler handler;
    private ArrayAdapter<String> array_adapter,badgearray_adapter;
    private Spinner spnr_item_name,spnr_badge_status;
    private CheckBox checkbox_selectall;
    private boolean isSelectAllSelected = false;
    private HashMap<String, String> free_items_ids = new HashMap<String, String>();
    private String afterprinting="";
    private ArrayList<PrintStatusObject> statusObject;
    private Dialog sort_dialog;
    private TextView txt_sortby, txt_sort1, txt_sort2, txt_sort3, txt_sort4, txt_sort_cancel;
    private LinearLayout sort_layout,filter_layout;
    private PoppyViewHelper mPoppyViewHelper;
    private String whereClause = "",wherefiltersearch="";
    private static  String ORDERBY_CASE = " ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.printing_attendeelist);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        width = display.getWidth();// point.x;
        height = display.getHeight();
        badge_creator = new BadgeCreation(this, width, height);
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        ext_settings = new ExternalSettings();
        resonNameList = new ArrayList<PrintAttendeesListActivity.ReasonHandler>();
        handler = new ReasonHandler();
        reaseon_array = new ArrayList<ReasonHandler>();
        qrcode_name = new ArrayList<String>();
        afterprinting= NullChecker(getIntent().getStringExtra("fromPrintSucess"));
        if(afterprinting.equals("printedSuccesssfully")){
            String json_string = Util.printStatusupdateAttendees.getString(Util.PRINT_STATUS, "");
            if(!json_string.isEmpty()){
                Type type = new TypeToken<List<PrintStatusObject>>(){}.getType();
                statusObject = new Gson().fromJson(json_string, type);
                if(statusObject.size()>0&&isOnline()) {
                    requestType=WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE;
                    doRequest();
                }
            }
        }else {
            //AllPrintedattendeeids =new ArrayList<String>();
        }
        // settting adapters
        String[] badgestatus = getResources().getStringArray(R.array.staticbadgestatus_list);
        badgearray_adapter = new ArrayAdapter<String>(PrintAttendeesListActivity.this, android.R.layout.simple_spinner_dropdown_item, badgestatus);
        spnr_badge_status.setAdapter(badgearray_adapter);
        final TreeMap<String, String> key_values = Util.db.getAllAttendeeItemPoolsWithNames(checked_in_eventId);
        final ArrayList<String> item_pool_names = new ArrayList<>(key_values.values());
        item_pool_names.add(0,"  ALL  ");
        free_items_ids = Util.db.getALLItemNameAndId(checked_in_eventId);

        List<String> free_item_name = new ArrayList<String>(free_items_ids.values());
        free_item_name.add(0,"   ALL   ");
        array_adapter = new ArrayAdapter<String>(PrintAttendeesListActivity.this, android.R.layout.simple_spinner_dropdown_item, item_pool_names);
        spnr_item_name.setAdapter(array_adapter);


        spnr_item_name.setVisibility(View.VISIBLE);
        spnr_badge_status.setVisibility(View.VISIBLE);


        spnr_badge_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkbox_selectall.setChecked(false);
                badge_status = spnr_badge_status.getSelectedItem().toString();
                if (spnr_badge_status.getSelectedItemPosition() == 0) {
                    badge_status="";
                }
                ShowTicketsDialog(1);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spnr_item_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkbox_selectall.setChecked(false);
                String spnr_selected_item = spnr_item_name.getSelectedItem().toString();
                String item_id = Util.getKey(free_items_ids, spnr_item_name.getSelectedItem().toString());
                if (spnr_item_name.getSelectedItemPosition() == 0) {
                    itempoolid="";
                    //checkin_adapter.notifyDataSetChanged();
                    ShowTicketsDialog(1);
                }else if (spnr_item_name.getSelectedItemPosition() > 0 && !Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                    for(String key : key_values.keySet()){
                        if(NullChecker(spnr_selected_item).equalsIgnoreCase(key_values.get(key))){
                            itempoolid = key;
                            break;
                        }
                    }
                    if(!itempoolid.isEmpty())
                        ShowTicketsDialog(1);

                    /*if (!item_id.isEmpty()) {
                        itempoolid=Util.db.getItem_Pool_ID(item_id, checked_in_eventId);
                        ShowTicketsDialog();
                        // edt_badge_label.setText(Util.db.getItemPoolBadgeLabel(itempoolid, checked_in_eventId));
                    }*/
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // end

        if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
            ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }
        if(Util.isMyServiceRunning(DownloadService.class, PrintAttendeesListActivity.this)){
            showServiceRunningAlert(BaseActivity.checkedin_event_record.Events.Name);
        }//else if(isOnline()){
        ShowTicketsDialog(1);
        //DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
        /* }else{
         *//*Intent global_intent = new Intent(PrintAttendeesListActivity.this, OfflineScanActivity.class);
		    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
		    global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(global_intent);*//*
            String scannedData = new String(data.getCharArrayExtra(Util.SCANDATA));
            generateCsvFile(scannedData.toString().trim());
            finish();
        }*/

        img_addticket.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                txtprint.setVisibility(View.GONE);
                checkbox_selectall.setChecked(false);
                ShowTicketsDialog(1);
            }
        });
        img_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(PrintAttendeesListActivity.this);
                back_layout.setVisibility(View.VISIBLE);
                top_layout.setBackgroundResource(R.color.green_top_header);
                back_layout.setBackgroundResource(R.color.green_top_header);
                search_layout.setVisibility(View.GONE);
                search_view.setText("");
                if (mDrawerLayout.isDrawerOpen(left_menu_slider))
                    mDrawerLayout.closeDrawer(left_menu_slider);
                // search_view.setFocusable(false);
                ShowTicketsDialog(1);
            }
        });
        img_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                back_layout.setVisibility(View.GONE);
                search_layout.setVisibility(View.VISIBLE);
                img_backmenu.setImageResource(R.drawable.top_more);
                img_backmenu.setVisibility(View.VISIBLE);
                search_view.setFocusable(true);
                search_view.requestFocus();
                if (search_view.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(search_view, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });
        img_backmenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hideSoftKeyboard(PrintAttendeesListActivity.this);
                    if (mDrawerLayout.isDrawerOpen(left_menu_slider))
                        mDrawerLayout.closeDrawer(left_menu_slider);
                    else
                        mDrawerLayout.openDrawer(left_menu_slider);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
        search_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_view.getText().toString().trim().isEmpty()) {
                    search_view.setHint("First Name, Last Name, Company, Email, Item Name, Order ID and Ticket ID");
                    requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                    doRequest();
                    return true;
                }else if(search_view.getText().toString().trim().isEmpty()){
                    Toast.makeText(PrintAttendeesListActivity.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
        search_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_view.getText().toString().trim().isEmpty()) {
                    requestType = WebServiceUrls.SA_SEARCH_ATTENDEE;
                    doRequest();
                    //SortFunction(R.id.editsearchrecord, is_buyer);
                    return true;
                }else if(search_view.getText().toString().trim().isEmpty()){
                    Toast.makeText(PrintAttendeesListActivity.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
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
                    }
                }
                return false;
            }
        });
        search_view.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_view.setFocusable(true);
                search_view.setHint("First Name, Last Name, Company and Email");
                searchAttendee();
                //ShowTicketsDialog(R.id.editsearchrecord);

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        sort_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSortDialog(PrintAttendeesListActivity.this);

            }
        });


//		DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
        back_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title_auto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtcheckin_selfcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*requestType=Util.GET_BADGE_ID;
                doRequest();*/
               /* if (!Util.dashboardHandler.availableScanAttendeeTicket) {
                    Util.setCustomAlertDialog(PrintAttendeesListActivity.this);
                    Util.openCustomDialog("Alert", Util.NOPRINTPERMISSION);
                    Util.txt_okey.setText("OK");
                    Util.txt_dismiss.setVisibility(View.GONE);
                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                        }
                    });
                } else {*/
                    if (isprinterconnectedopendialog()) {
                        if (tickets_registerfor_print.size() == 0 && tickets_register.size() == 0) {
                            AlertDialogCustom d = new AlertDialogCustom(PrintAttendeesListActivity.this);
                            d.setParamenters("Alert !", "Please Select at least one Attendee for Printing", null, null, 1, false);
                            d.show();
                        } else if (tickets_registerfor_print.size() > 0) {
                            if (!isValidate_badge_reg_settings) {
                                doreprintProcess();
                            } else
                                doprintProcess();
                        }
                    } else {
                        openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
                    }
               // }

            }
        });


    }
    private void doreprintProcess(){
        try {
            if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                if (tickets_registerfor_print.size() > 0) {
                    qrcode_name.clear();
                    badge_frame_layout.clear();
                    attendee_id = new ArrayList<String>();
                    PrintStatusObject printstatus=new PrintStatusObject();
                    ArrayList<PrintStatusObject>statusarray=new ArrayList<>();
                    for (String key : tickets_registerfor_print.keySet()) {
                        // if(Util.db.getAttendeeBadgePrintStatus(NullChecker(key)).isEmpty()||Util.db.getAttendeeBadgePrintStatus(NullChecker(key)).equalsIgnoreCase("Not Printed")) {
                        attendee_id.add(key);
                        printstatus.TicketId = key;
                        statusarray.add(printstatus);
                        //}
                    }

                    String ids=new Gson().toJson(statusarray);
                    Util.printStatusupdateAttendees.edit().putString(Util.PRINT_STATUS, ids).commit();

                    badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
                    if (badge_res.size() > 0) {
                        if (isOnline()) {
                            executePrinterStatusTask();
                        } else {
                            startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                        }
                        //prepareForPrint();
                    } else {
                        requestType = Util.LOAD_BADGE;
                        doRequest();
                    }
                }
            }else{
                openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void doprintProcess(){
        try {
            if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                if (tickets_registerfor_print.size() > 0) {
                    qrcode_name.clear();
                    badge_frame_layout.clear();
                    attendee_id = new ArrayList<String>();
                    PrintStatusObject printstatus=new PrintStatusObject();
                    ArrayList<PrintStatusObject>statusarray=new ArrayList<>();
                    for (String key : tickets_registerfor_print.keySet()) {
                        attendee_id.add(key);
                        printstatus.TicketId=key;
                        statusarray.add(printstatus);

                    }
                    String ids=new Gson().toJson(statusarray);
                    Util.printStatusupdateAttendees.edit().putString(Util.PRINT_STATUS, ids).commit();
                    badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
                    if (badge_res.size() > 0) {
                        if (isOnline()) {
                            executePrinterStatusTask();
                        } else {
                            startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                        }
                        //prepareForPrint();
                    } else {
                        requestType = Util.LOAD_BADGE;
                        doRequest();
                    }
                }
            }else{
                openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
            }
        }catch (Exception e){
            e.printStackTrace();
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
            txt_sort3.setVisibility(View.GONE);
            txt_sort2.setText("LAST NAME");
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
                    sort_dialog.dismiss();
                    ShowTicketsDialog(R.id.txtsort1);
                }
            });
            txt_sort2.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sort_dialog.dismiss();
                    ShowTicketsDialog(R.id.txtsort2);
                }
            });
            txt_sort3.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sort_dialog.dismiss();
                    ShowTicketsDialog(R.id.txtsort3);
                }
            });
            txt_sort4.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    sort_dialog.dismiss();
                    ShowTicketsDialog(R.id.txtsort4);
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

    private void prepareForPrint(){
        try {
            String whereClause = ITransaction.EMPTY_STRING;
            if (attendee_cursor != null) {
                whereClause = " where Event_Id = '"
                        + checked_in_eventId
                        + "'"
                        + " AND " + DBFeilds.ATTENDEE_ORDER_ID + " = "
                        + "'"
                        + attendee_cursor.getString(attendee_cursor
                        .getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)) + "'";
            }


            Cursor badge_id_check = Util.db.getAttendeeDataCursorForScan(whereClause);
            if(badge_id_check.getCount()==0){
                badge_id_check.close();
                badge_id_check=Util.db.getAllTypeAttendeeCursor(whereClause);
                badge_id_check.moveToFirst();
            }
            //badge_id_check.moveToFirst();

            for (int i = 0; i < badge_id_check.getCount(); i++) {
                badge_id_check.moveToPosition(i);
                if (!Util.NullChecker(badge_id_check.getString(badge_id_check.getColumnIndex("BadgeId"))).isEmpty()) {
                    //openprintDialog();
                    ReasonHandler handler = new ReasonHandler();
                    handler.id = badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    handler.attName = badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                    resonNameList.add(handler);
                    //resonAttendeeNames[i]=badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                }
            }
            badge_id_check.close();
            if (tickets_registerfor_print.size() > 0) {
                PrintStatusObject printstatus=new PrintStatusObject();
                ArrayList<PrintStatusObject>statusarray=new ArrayList<>();
                for (String key : tickets_registerfor_print.keySet()) {
                    attendee_id.add(key);
                    printstatus.TicketId=key;
                    statusarray.add(printstatus);
                }
                String ids=new Gson().toJson(statusarray);
                Util.printStatusupdateAttendees.edit().putString(Util.PRINT_STATUS, ids).commit();
                if (resonNameList.size() > 0) {
                    //TODO openprintDialog(resonNameList);
                } else {
                    if (isOnline()) {
                        executePrinterStatusTask();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void executePrinterStatusTask() {
        if(isOnline()) {
            if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    new IsBrotherPrinterConnectTask().execute();
                }else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
                    if(!isValidate_badge_reg_settings){
                        createTemplates();
                    }else if(isOnline()){
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    }else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
                }
                //new IsBrotherPrinterConnectTask().execute();
            } else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    new IsPrinterConnectTask().execute();
                }else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()) {
                    if(!isValidate_badge_reg_settings){
                        createTemplates();
                    }else if (isOnline()) {
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
                }
                //new IsPrinterConnectTask().execute();
            }else{
                TransperantGlobalScanActivity.openprinterNotConnectedDialog(this,true);
            }
        }else{
            startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
        }
    }

    public void SearchPrinterStatusThread() /*extends Thread*/ {

        /* search for the printer for 10 times until printer has been found. */
		/*@Override
		public void run() {*/
        try {
            // search for net printer.
            if (netPrinterList(5)) {
                isBrotherPrinterConnected=true;
            } else {
                isBrotherPrinterConnected=false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}
    }

    private  class IsBrotherPrinterConnectTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseDialog.setCancelable(false);
            baseDialog.setMessage("Please wait...");
            baseDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                SearchPrinterStatusThread();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            baseDialog.dismiss();
            if(isBrotherPrinterConnected){
                if (isOnline()) {
                    if(!isValidate_badge_reg_settings){
                        createTemplates();
                    }else {
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    }
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                }
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AttendeeDetailActivity.openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
                    }
                });
            }
        }
    }

    private  class IsPrinterConnectTask extends com.globalnest.stripe.android.compat.AsyncTask<Void,Void,Void> {
        private boolean isPrinterConnectedStatus=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseDialog.setCancelable(false);
            baseDialog.setMessage("Please wait...");
            baseDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                isPrinterConnectedStatus= isprinterconnected();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            baseDialog.dismiss();
            if(isPrinterConnectedStatus) {
                if (isOnline()) {
                    requestType = Util.GET_BADGE_ID;
                    doRequest();
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                }
            }
        }
    }
    public void openprinterNotConnectedDialog(final Context context){
        Util.setCustomAlertDialog(context);
        Util.openCustomDialog("Alert", "Printer is not connected.Do you want to Connect?");
        Util.txt_okey.setText("OK");
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startActivity(new Intent(context,PrintersListActivity.class));
                Util.alert_dialog.dismiss();
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.alert_dialog.dismiss();
            }
        });
    }

    public boolean isprinterconnectedopendialog() {
        boolean connected=false;
        if (PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
            connected=false;
        }else{
            connected=true;
        }
        return connected;
    }

    public  boolean isprinterconnected() {
        try {
            zebraPrinter = new ZebraPrinter();
            if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").isEmpty()) {
                zebraPrinter.createTCPConnection();
                if (zebraPrinter.getTCPConnection().isConnected()) {
                    return true;
                }
            }else{
                zebraPrinter.createBTConnection();
                if (zebraPrinter.getBTConnection().isConnected()) {
                    return true;
                }
            }
            return false;
        }catch(Exception e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AttendeeDetailActivity.openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
                }
            });
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // ticket_dialog.dismiss();
            if(getIntent().getBooleanExtra(Util.ISFINISH,false)){
                Intent i = new Intent();
                if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(AddAttendeeActivity.class.getName())){
                    i = new Intent(PrintAttendeesListActivity.this, AddAttendeeActivity.class);
                }else{
                    i = new Intent(PrintAttendeesListActivity.this,	ManageTicketActivity.class);
                }

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else {
                Intent result = new Intent();
                setResult(1987, result);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try{
            hideSoftKeyboard(PrintAttendeesListActivity.this);
            // ShowTicketsDialog();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        if(attendee_cursor != null){
            attendee_cursor.close();
        }
        if(Util.alert_dialog != null){
            Util.alert_dialog.dismiss();
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        try {
            Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),
                    view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            Drawable bgDrawable = view.getBackground();
            if (bgDrawable != null)
                bgDrawable.draw(canvas);
            else
                canvas.drawColor(Color.WHITE);
            view.draw(canvas);
            return returnedBitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setVisibility(View.GONE);
        img_addticket.setVisibility(View.VISIBLE);
        img_addticket.setImageResource(R.drawable.dashboardrefresh);
        img_search.setVisibility(View.VISIBLE);
        img_socket_scanner.setVisibility(View.GONE);
        img_scanner_base.setVisibility(View.GONE);
        img_menu.setImageResource(R.drawable.back_button);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mPoppyViewHelper  = new PoppyViewHelper(this, PoppyViewHelper.PoppyViewPosition.BOTTOM);
        View poppyView = mPoppyViewHelper.createPoppyViewOnListView(R.id.checkinticketview, R.layout.header_layout);
        sort_layout = (LinearLayout) poppyView.findViewById(R.id.ordersortlayout);
        filter_layout = (LinearLayout) poppyView.findViewById(R.id.orderfilterlayout);
        filter_layout.setVisibility(View.GONE);
        data = getIntent();
        txtcheckin_selfcheckin.setVisibility(View.VISIBLE);
        txtcheckin_selfcheckin.setText("Print");
        txtprint = (TextView) linearview.findViewById(R.id.txt_printselected);
        spnr_item_name = (Spinner)linearview.findViewById(R.id.spnr_item_name);
        spnr_badge_status = (Spinner)linearview.findViewById(R.id.spnr_badge_status);
        checkbox_selectall = (CheckBox) linearview.findViewById(R.id.checkbox_selectall);
        txt_oops = (TextView) linearview
                .findViewById(R.id.txt_global_scan_oops);
        ticket_view = (ListView) linearview
                .findViewById(R.id.checkinticketview);
        linear_badge_parent = (FrameLayout)linearview.findViewById(R.id.linear_badge_parent);
        //txtprint.setVisibility(View.GONE);
        checkbox_selectall.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                    if (isprinterconnectedopendialog()) {
                        int size = attendee_cursor.getCount();
                        if (size > 0) {
                            attendee_cursor.moveToFirst();
                            for (int i = 0; i < size; i++) {
                                tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                                badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                                attendee_cursor.moveToNext();
                            }
                            checkin_adapter.notifyDataSetChanged();
                        }

                    } else {
                        checkbox_selectall.setChecked(false);
                        openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
                    }
                }else {
                    //buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                    txtprint.setVisibility(View.GONE);
                    if (tickets_registerfor_print != null&&NullChecker(afterprinting).isEmpty()) {
                        tickets_registerfor_print.clear();
                        checkin_adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        //ShowTicketsDialog();
    }

    /*public void DoScannedData(char[] Data) {
        try {
            scanned_value = new String(Data);
            orderId = scanned_value.toString().trim();

            if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())){
                isOrderScaneed = true;
                ShowTicketsDialog();
            }else if(isBadgeScanned(orderId)){
                isOrderScaneed= false;
                if(ShowTicketsDialog()){
                    doCheckinProcess();
                }else{
                    isScannedItemNotopened();
                }
            }else if(isOrderScanned(orderId)){
                isOrderScaneed = true;
                ShowTicketsDialog();
            }else if (isOnline()) {
                requestType = Util.GET_TICKET;
                doRequest();
            } else {
                startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
            }
            *//*if(isOrderScaneed){
                txtprint.setVisibility(View.VISIBLE);
            }else{
                txtprint.setVisibility(View.GONE);
            }*//*
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }*/

    public void doCheckinProcess(){
        if (ext_settings.quick_checkin && attendee_cursor.getCount() == 1 && !isOrderScaneed) {
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            boolean isFreeSession = false;
            if (scanticks.size() > 0) {
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                        checked_in_eventId);
            }
            if(isFreeSession){
                String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                attendee_id = new ArrayList<String>();
                attendee_id.add(id);
                tickets_register.clear();
                //boolean ischeckin = Boolean.valueOf(NullChecker(c.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN))));
                boolean ischeckin = Util.db.SessionCheckInStatus(id,Util.db.getSwitchedONGroupId(checked_in_eventId));
                //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                tickets_register.put(id,ischeckin);
                requestType = Util.CHECKIN;
                doRequest();
            }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    String item_pool_name = Util.db.getItem_Pool_Name(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    showSingleButtonDialog("Alert","Sorry! You are not allowed to check-in for "+item_pool_name,PrintAttendeesListActivity.this);
                }else {
                    openScanSettingsAlert(PrintAttendeesListActivity.this, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), PrintAttendeesListActivity.class.getName());
                }
            }else{
                String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                String item_pool_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID));
                attendee_id = new ArrayList<String>();
                attendee_id.add(id);
                tickets_register.clear();
                boolean ischeckin = Boolean.valueOf(NullChecker(Util.db.getTStatusBasedOnGroup(id, item_pool_id, checked_in_eventId)));

                tickets_register.put(id,
                        ischeckin);
                requestType = Util.CHECKIN;
                doRequest();
            }

        }
    }

    private void isScannedItemNotopened(){
        String whereClause = " where Event_Id='" + checked_in_eventId  + "' AND (BadgeId='" + orderId.trim() + "' OR "+DBFeilds.ATTENDEE_CUSTOM_BARCODE+" = '"+orderId.trim()+"')";
        attendee_cursor = Util.db.getAttendeeDetailsWithAllTypes(whereClause);
		/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
		mediaPlayer.start();*/

        if(attendee_cursor == null){
            openCancelledOrderAlert("Error", "This badge is not valid for this session. Please check with event admin.");
        }else if(attendee_cursor.getCount() > 0){
            if(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TICKET_STATUS))).equalsIgnoreCase("Cancelled")){
                openCancelledOrderAlert("Error", "This ticket is cancelled. Please check with event admin.");
            }else{
                openScanSettingsAlert(PrintAttendeesListActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),TransperantGlobalScanActivity.class.getName());
            }
        }
    }

    public void searchAttendee() {
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
            sort_layout.setEnabled(true);
            if (!search_view.getText().toString().trim().isEmpty()) {
                wherefiltersearch = " where Event_Id = '" + checked_in_eventId + "' AND (" + fName + " like '" + "%" + sString
                        + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                        + "%" + "' OR " + tnumber + " like '%" + sString + "%"
                        + "%" + "' OR " + ordernumber + " like '%" + sString + "%"
                        // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                        // TRIM(user."+lName+")) like '%"+sString+"%"
                        + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                        + itempoolId + " like '%" + sString + "%" + "' OR " + email + " like '%" + sString + "%')";
               /* wherefiltersearch = " (" + fName + " like '" + "%" + sString
                        + "%" + "' OR " + lName + " like '%" + sString + "%" + "' OR " + company + " like '%" + sString
                        + "%" + "' OR " + tnumber + " like '%" + sString + "%"
                        + "%" + "' OR " + ordernumber + " like '%" + sString + "%"
                        // + "' OR CONCAT(TRIM(user."+fName+"), ' ',
                        // TRIM(user."+lName+")) like '%"+sString+"%"
                        + "' OR " + fName + ' ' + "|| " + lName + " like '%" + sString.replace(" ", "") + "%" + "' OR "
                        + itempoolId + " like '%" + sString + "%" + "' OR " + email + " like '%" + sString + "%' OR " + seatNumber + " like '%" + sString + "%')"+" AND ";
*/
                if (!whereClause.trim().isEmpty() && attendee_cursor != null) {
                    whereClause= whereClause.replace("where"," AND ");
                    attendee_cursor = Util.db.getAttendeeDataCursor(wherefiltersearch + whereClause+ORDERBY_CASE, "", "", "");
                } else if (attendee_cursor == null) {
                    attendee_cursor = Util.db.getAttendeeDataCursor(wherefiltersearch+ORDERBY_CASE, "", "", "");
                    //attendee_cursor = Util.db.getAttendeeDataCursor(whereClause, item_pool_id, activity_name,session_id);
                } else {
                    attendee_cursor.close();
                    attendee_cursor = Util.db.getAttendeeDataCursor(wherefiltersearch+ORDERBY_CASE, "", "", "");
                }
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (attendee_cursor!=null&&attendee_cursor.getCount() > 0) {
                            //ticket_view.setVisibility(View.VISIBLE);
                            ticket_view.setVisibility(View.VISIBLE);
                            checkin_adapter = new ListCheckInAdapter(PrintAttendeesListActivity.this,attendee_cursor);
                            ticket_view.setAdapter(checkin_adapter);
                            checkin_adapter.changeCursor(attendee_cursor);
                            checkin_adapter.notifyDataSetChanged();

                        }else{
                            ticket_view.setVisibility(View.GONE);
                            txt_oops.setVisibility(View.VISIBLE);
                            //txt_title.setText("Attendees ("+0+")"   );
                        }
                    }});

            } else {
                if (!whereClause.trim().isEmpty() && attendee_cursor != null) {
                    whereClause=whereClause.replace("AND  Event_Id=","where Event_Id=");
                    attendee_cursor = Util.db.getAttendeeDataCursor(whereClause+ORDERBY_CASE, "", "", "");
                }else {
                    if (attendee_cursor == null) {
                        whereClause=whereClause.replace("AND  Event_Id=","where Event_Id=");
                        attendee_cursor = Util.db.getAttendeeDataCursor(
                                whereClause+ ORDERBY_CASE, "", "", "");
                    } else {
                        attendee_cursor.close();
                        whereClause=whereClause.replace("AND  Event_Id=","where Event_Id=");
                        attendee_cursor = Util.db.getAttendeeDataCursor(
                                whereClause+ ORDERBY_CASE, "","", "");
                    }
                }


                checkin_adapter = new ListCheckInAdapter(PrintAttendeesListActivity.this,attendee_cursor);
                ticket_view.setAdapter(checkin_adapter);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void ShowTicketsDialog(int id) {

        try {
            dialog_count = 0;
            tickets_register.clear();
            if (tickets_registerfor_print != null&&NullChecker(afterprinting).isEmpty()) {
                tickets_registerfor_print.clear();
            }
            if(id==R.id.txtsort1)
                ORDERBY_CASE=" ORDER BY "+DBFeilds.ATTENDEE_FIRST_NAME+" COLLATE NOCASE";
            else if(id==R.id.txtsort2)
                ORDERBY_CASE=" ORDER BY "+DBFeilds.ATTENDEE_LAST_NAME+" COLLATE NOCASE";
            else if(id==R.id.txtsort4)
                ORDERBY_CASE=" ORDER BY "+DBFeilds.ATTENDEE_COMPANY+" COLLATE NOCASE";

            //reaseon_array.clear();
            qrcode_name.clear();
            badge_frame_layout.clear();
            badgelabel_list.clear();
            String badgestatus="";
            if(badge_status.isEmpty()){
                badgestatus=" = 'Not Printed' OR print_status = '' OR print_status = 'Printed' )";
            }else if(badge_status.equalsIgnoreCase("Printed"))
            {
                badgestatus=" = 'Printed' )";
            }else {
                badgestatus=" = 'Not Printed' OR print_status = '' )";// IS NOT NULL
            }
            if(NullChecker(itempoolid).isEmpty()){
                whereClause = " where Event_Id='" + checked_in_eventId  + "'  AND ("+DBFeilds.ATTENDEE_BADGE_PRINTSTATUS+badgestatus+" "
                        +" AND "+DBFeilds.ATTENDEE_BADGABLE+"= 'B - Badge'"
                        +" AND "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+"= ''"
                        +" AND ((item_type_name = '" +
                        Util.ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR " +
                        "(" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + Util.ITEM_TYPES.Package + "'))";//AND "+DBFeilds.ATTENDEE_BADGEID+" =''
            }else {
                whereClause = " where Event_Id='" + checked_in_eventId + "'  AND ("+DBFeilds.ATTENDEE_BADGE_PRINTSTATUS+badgestatus
                        +" AND "+DBFeilds.ATTENDEE_BADGABLE+"= 'B - Badge'"
                        +" AND "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+"= ''"+      //AND " + DBFeilds.ATTENDEE_BADGEID + " =''"+" AND
                        " AND "+DBFeilds.ATTENDEE_ITEM_POOL_ID+" = '"+itempoolid+"'"+" AND ((item_type_name = '" +
                        Util.ITEM_TYPES.Package + "' AND " + DBFeilds.ATTENDEE_ITEM_PARENT_ID + " != 'null') OR " +
                        "(" + DBFeilds.ATTENDEE_ITEMTYPE_NAME + " != '" + Util.ITEM_TYPES.Package + "'))";
            }
            if(attendee_cursor!=null&&!wherefiltersearch.trim().isEmpty()&&!search_view.getText().toString().trim().isEmpty()){
                wherefiltersearch=wherefiltersearch.replace("where"," AND ");
                attendee_cursor = Util.db.getAttendeeDataCursor(whereClause+wherefiltersearch +ORDERBY_CASE, "", "", "");
            }else
                attendee_cursor = Util.db.getAttendeeDataCursor(whereClause +ORDERBY_CASE, "", "", "");
            attendee_cursor.moveToFirst();

            if(attendee_cursor.getCount()==0){
                ticket_view.setVisibility(View.GONE);
                txt_oops.setVisibility(View.VISIBLE);
                txt_print.setBackgroundColor(getResources().getColor(R.color.green_top_header));
                title_auto.setText("Attendees(" + 0 + ")  ");
            }else if (attendee_cursor != null && attendee_cursor.getCount() > 0) {
                txt_oops.setVisibility(View.GONE);
                ticket_view.setVisibility(View.VISIBLE);
                if(!NullChecker(itempoolid).isEmpty()) {
                    txt_print.setBackgroundColor(getResources().getColor(R.color.green_top_header));
                    title_auto.setText(Util.db.getItem_Pool_Name(itempoolid, checked_in_eventId) + " Attendees (" + attendee_cursor.getCount() + ")");
                } else {
                    txt_print.setBackgroundColor(getResources().getColor(R.color.green_top_header));
                    title_auto.setText("Attendees (" + attendee_cursor.getCount() + ")");
                }
                checkin_adapter = new ListCheckInAdapter(PrintAttendeesListActivity.this,attendee_cursor);
                ticket_view.setAdapter(checkin_adapter);
                // progress_dialog.dismiss();
            } else{
                title_auto.setText("Attendees(" + 0 + ")  ");
            }
            //}});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doPrint() {
        if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER,"").equalsIgnoreCase("Zebra")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mFiles.clear();
                    String file_path = null;
                    File root = android.os.Environment.getExternalStorageDirectory();
                    File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
                    for (int i = 0; i < tickets_registerfor_print.size(); i++) {
                        file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
                        //Log.i("Attendee Detail", "Image Path=" + file_path);
                        mFiles.add(file_path);
                    }
                    MsgDialog.intent_value = PrintAttendeesListActivity.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
                    zebraPrinter.doZebraPrint(PrintAttendeesListActivity.this,mFiles);
                }
            }).start();
        } else {
            //Log.i("Attendee Detail-----doprint", "sharedPrefrence is empty");
            if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                searchPrinter = new SearchThread();
                searchPrinter.start();
            }else{
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                myPrint.setBluetoothAdapter(bluetoothAdapter);
                NetPrinter printer=new NetPrinter();
                printer.modelName=PrinterDetails.selectedPrinterPrefrences.getString("printer","");
                printer.serNo="";
                printer.ipAddress="";
                printer.macAddress=PrinterDetails.selectedPrinterPrefrences.getString("macAddress","");
                setPrefereces(printer);
                printBadge();
            } /* setDialog();
            searchPrinter = new SearchThread();
            searchPrinter.start();*/
        }
    }

    public void GlobalPrint(String ticket_id) {
        try {
            qrcode_name = new ArrayList<String>();
            attendee_id = new ArrayList<String>();
            // int dialog_count = 0;
            badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
            if (badge_res.size() > 0) {
                attendee_id.add(ticket_id);
                if (isOnline()) {
                    /*requestType = Util.GET_BADGE_ID;
                    doRequest();*/
                    executePrinterStatusTask();
                } else {
                    startErrorAnimation(
                            getResources().getString(R.string.connection_error),
                            txt_error_msg);
                }
            } else {
                AlertDialogCustom custom = new AlertDialogCustom(
                        PrintAttendeesListActivity.this);
                custom.setParamenters("Alert",
                        "No Badge Found, Do you want to select a Badge",
                        new Intent(PrintAttendeesListActivity.this,
                                BadgeTemplateNewActivity.class), null, 2,false);
                custom.show();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openprintDialog(final ArrayList<ReasonHandler> resonHandeler, final CompoundButton check_print) {
        try {
            dialog_count++;
            print_dialog = new AlertDialog.Builder(PrintAttendeesListActivity.this);
            LayoutInflater li = LayoutInflater.from(PrintAttendeesListActivity.this);
            View promptsView = li.inflate(R.layout.print_dialog_layout, null);
            print_dialog.setView(promptsView);
            final EditText edit_reason = (EditText) promptsView
                    .findViewById(R.id.edit_reason);
            final TextView txt_cash_error = (TextView) promptsView
                    .findViewById(R.id.txt_dialogerror);
            final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
            txt_message.setVisibility(View.GONE);
            print_dialog
                    .setCancelable(false)
                    .setPositiveButton("Print",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    reason = edit_reason.getText().toString();
                                    //Log.i("Print Dialog ===", "Reason ====="+ reason);
                                    if (!reason.equalsIgnoreCase("")) {
                                        handler.reson = reason;
                                        reaseon_array=resonHandeler;
                                        dialog.dismiss();
                                        hideSoftKeyboard(PrintAttendeesListActivity.this);
                                        //Log.i("Print Dialog ===","Reason array====="+ reaseon_array.size());
                                        /* if (isOnline()) {
                                         *//*requestType = Util.GET_BADGE_ID;
                                            doRequest();*//*
                                            executePrinterStatusTask();
                                        } else {
                                            startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
                                        }*/

                                    } else {
                                        edit_reason.setError("Reason should not be empty");
                                        edit_reason.requestFocus();
                                    }
                                }
                            })
                    .setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    // ticket_dialog.dismiss();
                                    tickets_registerfor_print.remove(handler.id);
                                    resonHandeler.remove(handler);
                                    //check_print.setChecked(false);
                                    check_print.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                    dialog.dismiss();
                                    hideSoftKeyboard(PrintAttendeesListActivity.this);
                                    //ShowTicketsDialog();
                                }
                            });
            // create alert dialog
            AlertDialog alertDialog = print_dialog.create();
            // show it
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String setBadgeIdUrl() {
        return sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT;
    }
    private JSONArray setPrintStatusBody() {
        try {
            JSONArray badgearray = new JSONArray();

            try {
                for (int i=0;i<statusObject.size();i++){
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", statusObject.get(i).TicketId.trim());
                    obj.put("status", "Printed");
                    badgearray.put(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return badgearray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getSearchValues() {
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", sfdcddetails.user_id));
        values.add(new BasicNameValuePair("search_string", search_view.getText().toString()));
        return AppUtils.getQuery(values);
    }
    @SuppressWarnings("unused")
    private JSONArray setPrintBadgeBody() {
        try {
            int j = 0;
            String where_att = " Where EventID = '" + checked_in_eventId
                    + "' AND isBadgeSelected = 'Yes'";
            //Cursor updated_badge1 = Util.db.getBadgeTemplate(where_att);
            //updated_badge1.moveToFirst();
            JSONArray badgearray = new JSONArray();

            try {
                for (String key : tickets_registerfor_print.keySet()) {
                   /* attendee_id.add(key);
                }
                for (int j = 0; j < tickets_registerfor_print.size(); j++) {
                */    JSONObject obj = new JSONObject();

                    obj.put("TicketId", key.trim());
                    //obj.put("BadgeLabel", updated_badge1.getString(updated_badge1.getColumnIndex("BadgeName")));
                    //Log.i("----------------Badge Label List Size------------",":"+badgelabel_list.size());
                    if(badgelabel_list.size() > 0) {
                        //Log.i("----------------Badge Label------------",":"+badgelabel_list.get(j));
                      /*  if (key.trim().equals(NullChecker(badgelabel_list.get(j)))) {
                            obj.put("BadgeLabel", NullChecker(badgelabel_list.get(j)));
                        }else{*/
                        obj.put("BadgeLabel", NullChecker(badgelabel_list.get(j)));
                        //}
                    }else{
                        obj.put("BadgeLabel", NullChecker(badgelabel_list.get(j)));
                    }
                    //if (reaseon_array.size() > 0) {
                    ////Log.i("====== Reason Array Size =====",": In the if condition " + reaseon_array.size()+ " Reason array item="+ reaseon_array.get(j));
                    /*if (reaseon_array.size() > 0&&reaseon_array.size()>j) {
                        if(reaseon_array.get(j)!=null) {
                            if (key.trim().equals(reaseon_array.get(j).id)) {
                                obj.put("Reason", reaseon_array.get(j).reson);
                            } else {
                                obj.put("Reason", "");
                            }
                        }
                    }
                    else {*/
                    ////Log.i("====== Reason Array Size =====",": In the else Condition ");
                    obj.put("Reason", "false");
                    // }
                    badgearray.put(obj);
                    j++;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return badgearray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unused")
    public JSONArray makeCheckin() {
        try {
            Set<String> keys = tickets_register.keySet();
            JSONArray ticketarray = new JSONArray();
            // JSONObject parent = new JSONObject();
            checked_time = getESTFormat();
            for (String key : tickets_register.keySet()) {
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", key.trim());
                    obj.put("device", "ANDROID");
                    List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                    if(scanticks.size() == 0){
                        obj.put("freeSemPoolId", "");
                    }else if(scanticks.size() > 0){
                        if(Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId)){
                            obj.put("freeSemPoolId", scanticks.get(0).BLN_Item_Pool__c);
                        }else{
                            obj.put("freeSemPoolId", "");
                        }
                    }
                    if (String.valueOf(tickets_register.get(key)).equals("true")){
                        obj.put("isCHeckIn", false);
                    }else{
                        obj.put("isCHeckIn", true);
                    }
                    obj.put("sTime", Util.getCurrentDateTimeInGMT());
                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                    obj.put("scandevicemode",Util.scanmode_checkin_pref.getString(sfdcddetails.user_id+checked_in_eventId,""));

                    ticketarray.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return ticketarray;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private class ViewHolder {
        CheckBox check_print;
        private ImageView  img_print_icon;
        private TextView txtprint;
        private TextView txt_attendeename, ticketnameandnumber, txt_oops,txtbadgeprintstatus,txt_attendeecompany;

    }

    private class ListCheckInAdapter extends CursorAdapter {


        Cursor c;

        public ListCheckInAdapter(Context context, Cursor c) {
            super(context, c);
            this.c = c;

        }
        public void changeCursor(Cursor cursor) {
            super.changeCursor(cursor);
            this.c = cursor;
        }

        @Override
        public Object getItem(int position) {
            return totalorderlisthandler.TotalLists.get(0).ticketsInn
                    .get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View v = inflater.inflate(R.layout.printattendee_list_item,null);
            ViewHolder holder = new ViewHolder();
            v.setBackgroundColor(getResources().getColor(R.color.screen_bg_color));

            holder.ticketnameandnumber = (TextView) v.findViewById(R.id.ticketnameandnumber);
            holder.txt_attendeename = (TextView) v.findViewById(R.id.txt_attendeename);
            holder.txt_attendeecompany = (TextView) v.findViewById(R.id.txt_attendeecompany);
            // sai change tick_img = (ImageView) v.findViewById(R.id.imgticketcheckin);

            holder.check_print = (CheckBox) v.findViewById(R.id.check_print);
            holder.txtbadgeprintstatus =(TextView) v.findViewById(R.id.txtbadgeprintstatus);
            holder.img_print_icon = (ImageView) v.findViewById(R.id.img_print_icon);


            holder.check_print.setFocusable(false);
            holder.check_print.setClickable(true);
            // sai change tick_img.setFocusable(false);
            holder.ticketnameandnumber.setFocusable(false);
            holder.ticketnameandnumber.setTypeface(Util.roboto_regular);
            v.setTag(holder);
            return v;
        }

        @Override
        public void bindView(View v, Context context, final Cursor c) {
            final ViewHolder holder = (ViewHolder) v.getTag();
            if(!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
                holder.check_print.setVisibility(View.GONE);
                holder.img_print_icon.setVisibility(View.GONE);
            }
            String parent_id = Util.db.getItemPoolParentId(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
            if(!NullChecker(parent_id).isEmpty()){
                String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                holder.ticketnameandnumber.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+//"-"+
                        Html.fromHtml("<font <small> ("+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+") </small> </font>"));
            }else{
                holder.ticketnameandnumber.setText(Html.fromHtml("<Html> <font>"+c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+//"-"+
                        " <small> (" +c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+") </small> </font> </Html>"));
            } /*if(!NullChecker(parent_id).isEmpty()){
                        String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        ticketnameandnumber.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+" - "+  attendee_cursor
                                .getString(c.getColumnIndex("Tikcet_Number")));
                    }else{
                        ticketnameandnumber.setText(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" - "+  attendee_cursor
                                .getString(attendee_cursor
                                        .getColumnIndex("Tikcet_Number")));
                    }*/
            if (!NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).isEmpty()&&
                    NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS))).trim().equals("Printed")) {
                holder.txtbadgeprintstatus.setTextColor(getResources().getColor(R.color.green_color));
                holder.txtbadgeprintstatus.setText("Printed ");
            } else {
                holder.txtbadgeprintstatus.setTextColor(getResources().getColor(R.color.orange_bg));
                holder.txtbadgeprintstatus.setText("Not Printed ");
            }

            holder.txt_attendeename.setText(NullChecker(attendee_cursor.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " "
                    + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))));
            if (!NullChecker(c.getString(attendee_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_COMPANY))).isEmpty()) {
                holder.txt_attendeecompany.setVisibility(View.VISIBLE);
                holder.txt_attendeecompany.setText(NullChecker(attendee_cursor.getString(c.getColumnIndex(DBFeilds.ATTENDEE_COMPANY))));
            } else {
                holder.txt_attendeecompany.setVisibility(View.GONE);
            }

            // sai change tick_img.setVisibility(View.VISIBLE);
            String tstatus = ITransaction.EMPTY_STRING;
            boolean isFreeSession = false;
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            if(scanticks.size() > 0){
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
            }
            if(isFreeSession){
                tstatus = String.valueOf(Util.db.SessionCheckInStatus(c.getString(c.getColumnIndex("Attendee_Id")), Util.db.getSwitchedONGroupId(checked_in_eventId)));
            }else{
                tstatus = Util.db.getTStatusBasedOnGroup(c.getString(c.getColumnIndex("Attendee_Id")), c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId,Util.db.getSwitchedONGroupId(checked_in_eventId));
            }
            holder.check_print.setTag(c.getPosition());

            holder.check_print.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // TODO Auto-generated method stub
                    int position_tag = (Integer) buttonView.getTag();
                    AppUtils.displayLog("------------List Position---------", ":"+position_tag);
                    c.moveToPosition(position_tag);
                    if(isChecked){
                        if(isprinterconnectedopendialog()) {
                            tickets_registerfor_print.put(c.getString(c.getColumnIndex("Attendee_Id")), true);
                            badgelabel_list.add(NullChecker(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                            buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                                /*if (!Util.NullChecker(c.getString(c.getColumnIndex("BadgeId"))).isEmpty()) {
                                    handler.id = c.getString(c.getColumnIndex("Attendee_Id"));
                                    handler.attName = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                    resonNameList.add(handler);
                                    openprintDialog(resonNameList, buttonView);
                                    //resonAttendeeNames[i]=badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                }*/
                        }
                        else {
                            tickets_registerfor_print.remove(c.getString(c.getColumnIndex("Attendee_Id")));
                            buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                            if(badgelabel_list.size() > 0){
                                badgelabel_list.remove(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                            }
                            handler.id = c.getString(c.getColumnIndex("Attendee_Id"));
                            handler.attName = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                            resonNameList.remove(handler);
                            openprinterNotConnectedDialog(PrintAttendeesListActivity.this);
                        }
                    }else{
                        tickets_registerfor_print.remove(c.getString(c.getColumnIndex("Attendee_Id")));
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                        if(badgelabel_list.size() > 0){
                            badgelabel_list.remove(c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                        }
                        handler.id = c.getString(c.getColumnIndex("Attendee_Id"));
                        handler.attName = c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + c.getString(c.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                        resonNameList.remove(handler);
                    }
                    //int i=tickets_registerfor_print.size();
                    if(tickets_registerfor_print.size()>0) {
                        txtprint.setVisibility(View.VISIBLE);
                        txtprint.setText("Selected-" + tickets_registerfor_print.size());
                    }else {
                        txtprint.setVisibility(View.GONE);
                    }

                }
            });


            try {
                if(tickets_registerfor_print!=null && tickets_registerfor_print.containsKey(c.getString(c.getColumnIndex("Attendee_Id")))
                        && tickets_registerfor_print.get(c.getString(c.getColumnIndex("Attendee_Id")))){
                    holder.check_print.setChecked(true);
                        /*check_print.setVisibility(View.VISIBLE);
                        check_print.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));*/

                }else{
                    holder.check_print.setChecked(false);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }

    public String setAttendeeInfoUrl() {
        try {
			/*return sfdcddetails.instance_url + WebServiceUrls.SA_GET_ORDER_LIST
					+ "Event_id=" + checked_in_eventId + "&User_id="
					+ sfdcddetails.user_id;*/
            String whereClause = " where (orders." + DBFeilds.ORDER_BUYER_ID + " = user." + DBFeilds.USER_GNUSER_ID + ") AND orders."
                    + DBFeilds.ORDER_EVENT_ID + " = '" + checked_in_eventId + "'order by orders.Order_Date DESC";
            Cursor sales_cursor = Util.db.getPaymentDataCursor(whereClause);
            String url = sfdcddetails.instance_url
                    + WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="
                    + checked_in_eventId + "&User_id=" + sfdcddetails.user_id
                    + "&offset=" + sales_cursor.getCount() + "&limit="
                    + checkedin_event_record.Events.scan_attendee_limit__c;
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void doRequest() {
        //Log.i("----------------Request Type-----------",":"+requestType);

        String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
        if (requestType.equals(Util.CHECKIN)) {
            postMehod = new HttpPostData("Ticket Checking In/Out...",setTicketCheckinUrl(), makeCheckin().toString(), access_token, PrintAttendeesListActivity.this);
            postMehod.execute();
        }
        else  if (requestType.equals(Util.GET_ATTENDEE)) {
        	/*		postMehod = new HttpPostData(setAttendeeInfoUrl(), null,
					sfdcddetails.token_type, sfdcddetails.access_token,
					PrintAttendeesListActivity.this);
			postMehod.execute();*/
            postMehod = new HttpPostData("Loading Attendees...",setAttendeeInfoUrl(), null, access_token, PrintAttendeesListActivity.this);
            postMehod.execute();

        } else if(requestType.equals(WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE)){
            postMehod = new HttpPostData("Updating PrintStatus...",sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE, setPrintStatusBody().toString(), access_token, PrintAttendeesListActivity.this);
            postMehod.execute();
        }else if (requestType.equals(Util.GET_TICKET)) {

            postMehod = new HttpPostData("Attendee Loading...",setTicketInfoUrl(), null, access_token, PrintAttendeesListActivity.this);
            postMehod.execute();

        } else if (requestType.equals(Util.GET_BADGE_ID)) {

            postMehod = new HttpPostData("Generating Badge ID...",setBadgeIdUrl(), setPrintBadgeBody().toString(), access_token, PrintAttendeesListActivity.this);
            postMehod.execute();

        }else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
            postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, PrintAttendeesListActivity.this);
            postMethod.execute();
        }else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
            String url = sfdcddetails.instance_url + WebServiceUrls.SA_SEARCH_ATTENDEE + getSearchValues();
            postMethod = new HttpPostData("Loading Search...", url, null, access_token, PrintAttendeesListActivity.this);
            postMethod.execute();
        }
    }

    public String setTicketCheckinUrl() {
        try {
            return sfdcddetails.instance_url
                    + WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="
                    + sfdcddetails.user_id+"&eventId="+checked_in_eventId+"&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion()
                    +"&checkin_only="+String.valueOf(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String setTicketInfoUrl() {
        try {
            if (!isOrderScaneed) {
                //String items  = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                //url = sfdcddetails.instance_url + WebServiceUrls.SA_SCAN_TICKET	+ "Event_id=" + checked_in_eventId + "&Order_Id="+ orderId + "&User_id=" + user_profile.Userid+"&itemId="+items;
                url = sfdcddetails.instance_url + WebServiceUrls.SA_SCAN_TICKET	+getTicketValues();
            } else {
                //Log.i("----------Scan result---------", " ORCode === " + QrId);
                url = sfdcddetails.instance_url
                        + WebServiceUrls.SA_TICKETS_SCAN_URL + "EventId="
                        + checked_in_eventId + "&UserId=" + user_profile.Userid
                        + "&QRcode=" + QrId;
            }
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getTicketValues(){
        List<ScannedItems> items = Util.db.getSwitchedOnScanItem(checked_in_eventId);
        List<NameValuePair> values = new ArrayList<NameValuePair>();
        values.add(new BasicNameValuePair("Event_id", checked_in_eventId));
        values.add(new BasicNameValuePair("User_id", user_profile.Userid));
        values.add(new BasicNameValuePair("Order_Id", orderId));
        if(items.size() > 0){
            values.add(new BasicNameValuePair("itemId", items.get(0).BLN_Item_Pool__c));
        }



        return AppUtils.getQuery(values);
    }
    @SuppressWarnings("null")
    public void parseJsonResponse(String response) {
        // Gson gson;

        //Log.i("---------------GlobalScan Activity Response----------", ":"+response);
        try{
            if(!isValidResponse(response)){
                openSessionExpireAlert(errorMessage(response));
            }
            if(requestType.equals(WebServiceUrls.SA_BADGE_PRINTSTATUSUPDATE)){
                if (!response.isEmpty()) {
                    JSONArray badge_array = new JSONArray(response);
                    for (int i = 0; i < badge_array.length(); i++) {
                        JSONObject badge_obj = badge_array.optJSONObject(i);
                        String printstatus = badge_obj.optString("status");
                        String TicketId = badge_obj.optString("ticketId");
                        String lastmodifieddate = badge_obj.optString("lastmodifieddate");
                        Util.db.insertandupdateAttendeeBadgeIdandPrintstatus(TicketId,printstatus,lastmodifieddate);
                    }
                    Util.printStatusupdateAttendees.edit().clear().commit();
                    showCustomToast(this, "Updated Successfully! ",
                            R.drawable.img_like, R.drawable.toast_greenroundededge, true);
                }
            }
            else if (requestType.equals(Util.CHECKIN)) {
                OfflineSyncResController offlineResponse = new Gson().fromJson(response, OfflineSyncResController.class);
                //JSONObject obj = new JSONObject(response);
                //Log.i("Attendee List Activity", "requestType=  " + requestType);
                if (NullChecker(offlineResponse.ErrorMsg).isEmpty()) {
                    //Log.i("Attendee List Activity", "error is null");
                    //JSONArray success = obj.optJSONArray("SuccessTickets");
                    //JSONArray failure = obj.optJSONArray("FailureTickets");
                    String dialogtime = "";
                    List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                    boolean isFreeSession = false;
                    if(scanticks.size() > 0){
                        isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                    }
                    if (offlineResponse.SuccessTickets.size() > 0) {
                        attendee_id = new ArrayList<String>();
                        for (OfflineSyncSuccessObject checkin_obj : offlineResponse.SuccessTickets) {
                            //Log.i("Attendee List Activity","success is not null");
                            boolean status = Boolean.valueOf(checkin_obj.Status);//success.optJSONObject(i).optBoolean("Status");
                            String Attendeename=checkin_obj.STicketId.Ticket__r.tkt_profile__r.First_Name__c+" "+checkin_obj.STicketId.Ticket__r.tkt_profile__r.Last_Name__c;

                            String time = checkin_obj.TimeStamp;//success.optJSONObject(i).optString("TimeStamp");
                            String attendee_Id = checkin_obj.STicketId.Ticket__c;//success.optJSONObject(i).optJSONObject("STicketId").optString("Ticket__c");

                            dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time, checkedin_event_record.Events.Time_Zone__c);
                            //time = Util.new_db_date_format.format(Util.date_format_sec.parse(time));
                            //Log.i("Attendee List Activity", "Database date="+ time + "dialog time=" + dialogtime);


                            if(isFreeSession){
                                List<TStatus> session_attendee = new ArrayList<TStatus>();
                                session_attendee.add(checkin_obj.STicketId);
                                Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                            }else{
                                Util.db.updateCheckedInStatus(checkin_obj.STicketId,checked_in_eventId);
                            }
                            /*if(ext_settings.quick_print){
                                attendee_id.add(attendee_Id);
                            }*/
                            if(status) {
                                showCustomToast(this,
                                        Attendeename+" Checked In Successfully! ",
                                        R.drawable.img_like, R.drawable.toast_greenroundededge, false);
                                playSound(R.raw.beep);
                            }else{
                                showCustomToast(this,
                                        Attendeename+" Checked out Successfully! ",
                                        R.drawable.img_like, R.drawable.toast_redrounded, false);
                                playSound(R.raw.checkout);
                            }
                        }


                        ShowTicketsDialog(1);
                        boolean isCheckIn = false;
                        if(offlineResponse.SuccessTickets.size() == 1){
                            isCheckIn = Boolean.valueOf(offlineResponse.SuccessTickets.get(0).Status);//success.getJSONObject(0).optBoolean("Status");
                        }
                        if(ext_settings.quick_checkin){
                            if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())){
                                Intent i = new Intent(PrintAttendeesListActivity.this,ManageTicketActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }

                            finish();
                        }
                    } else {
                        String attendee_name = ITransaction.EMPTY_STRING;
                        String statusdisplay=" Checked-Out ";
                        int i =0 ;
                        for (OfflineSyncFailuerObject failes_ticks : offlineResponse.FailureTickets) {
                            boolean status = Boolean.valueOf(failes_ticks.Status);//failure.optJSONObject(i).optBoolean("Status");
                            String time = failes_ticks.TimeStamp;//failure.optJSONObject(i).optString("TimeStamp");
                            String attendee_Id = failes_ticks.STicketId;
                            if(status){
                                statusdisplay =" Checked-In ";
                            }//failure.optJSONObject(i).optString("STicketId");
                            dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time, checkedin_event_record.Events.Time_Zone__c);
                            //time = Util.new_db_date_format.format(Util.db_server_ticket_format.parse(time));
                            // checkin_adapter.notifyDataSetChanged();
                            if(isFreeSession){
                                List<TStatus> session_attendee = new ArrayList<TStatus>();
                                session_attendee.add(failes_ticks.tStaus);
                                Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                            }else{
                                Util.db.updateCheckedInStatus(failes_ticks.tStaus,checked_in_eventId);
                            }
                            attendee_name = attendee_name+Util.db.getAttendeeNameWithId(failes_ticks.tStaus.Ticket__c);
                            if( i != (offlineResponse.FailureTickets.size()-1)){
                                attendee_name = attendee_name+" , ";
                            }
                            i++;
                        }
						/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
						mediaPlayer.start();*/
                        playSound(R.raw.somethingwentwrong);
                        Util.setCustomAlertDialog(PrintAttendeesListActivity.this);
                        Util.txt_dismiss.setVisibility(View.GONE);
                        Util.txt_okey.setText("Ok");
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
                                finish();
                            }
                        });
                        Util.openCustomDialog("Alert",attendee_name + " is Already" +statusdisplay+ "\n at Time:"+dialogtime);

                        // Util.openCustomDialog("Check-in Failed",attendee_name + ". These attendees are already checked-in/out some other place in the event, Please check their status and try again.");
                    }

                    /*if (tickets_registerfor_print.size() > 0) {
                        doprintProcess();
                    }*/
                }
            }   else if (requestType.equals(Util.GET_BADGE_ID)) {
                if (!response.isEmpty()) {
                    JSONArray badge_array = new JSONArray(response);
                    for (int i = 0; i < badge_array.length(); i++) {
                        JSONObject badge_obj = badge_array.optJSONObject(i);
                        if (badge_obj.optString("Error").equalsIgnoreCase("null")) {
                            String BadgeLabel = badge_obj.optString("BadgeLabel");
                            String BadgeId = badge_obj.optString("BadgeId");
                            String Reason = badge_obj.optString("Reason");
                            String TicketId = badge_obj.optString("TicketId");
                            String printstatus = "Printed";
                            String BadgeParentId = Util.NullChecker(badge_obj.optString("BadgeParentId"));
                            Util.db.insertandupdateAttendeeBadgeId(BadgeLabel,
                                    BadgeId, Reason, TicketId,printstatus,BadgeParentId);
                            //printClicked();

                        } else {
                            Util.setCustomAlertDialog(PrintAttendeesListActivity.this);
                            Util.txt_dismiss.setVisibility(View.GONE);
                            Util.setCustomDialogImage(R.drawable.error);
                            Util.alert_dialog.setCancelable(false);
                            Util.txt_okey.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    // ticket_dialog.dismiss();
                                    Util.alert_dialog.dismiss();
                                    ShowTicketsDialog(1);
                                }
                            });
                            Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    // ticket_dialog.dismiss();
                                    Util.alert_dialog.dismiss();
                                    ShowTicketsDialog(1);
                                }
                            });
                            Util.openCustomDialog("Alert",
                                    badge_obj.optString("Error"));
                        }
                    }

                    createTemplates();


                }
            } else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
                Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
                List<BadgeResponseNew> badges =  new Gson().fromJson(response, listType);
                //Log.i("---------------- parseJsonResponse Badge Size----------", ":" + badges.size());
                Util.db.deleteBadges(checked_in_eventId);
                sharedPreferences.edit().clear().commit();
                for(BadgeResponseNew badge : badges){
                    badge.badge.event_id = checked_in_eventId;
                    Util.db.InsertAndUpdateBadgeTemplateNew(badge);
                }
                badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
                if(badge_res.size() > 0){
                    prepareForPrint();
                }else{
                    AlertDialogCustom custom = new AlertDialogCustom(
                            PrintAttendeesListActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(PrintAttendeesListActivity.this,
                                    BadgeTemplateNewActivity.class), null, 2, false);
                    custom.show();
                }
            }else if (requestType.equals(WebServiceUrls.SA_SEARCH_ATTENDEE)) {
                TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
                if (totalorderlisthandler.TotalLists.size() > 0) {
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                    ShowTicketsDialog(1);
                }else{
                    Toast.makeText(PrintAttendeesListActivity.this, "No Attendee Found!", Toast.LENGTH_LONG).show();
                }


            }else {
                //Log.i("Attendee Detail", "respons====>" + response);
                startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
            }
        }catch (Exception e) {
            e.printStackTrace();
            startErrorAnimation(
                    getResources().getString(R.string.connection_error),
                    txt_error_msg);
        }

    }

    /*
     * Code for printing the badges
     */
    private class BadgeListAdapter extends BaseAdapter {


        public BadgeListAdapter() {
            // TODO Auto-generated constructor stub
            badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();

        }
        @Override
        public int getCount() {
            return attendee_id.size();
        }

        @Override
        public Object getItem(int position) {
            return attendee_id.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                //View v = inflater.inflate(R.layout.badge_item_layout, null);
                //print_badge = (FrameLayout) v.findViewById(R.id.frame_attdetailqrcodebadge);
                //qrcode_name.clear();
                View v = inflater.inflate(R.layout.badge_sample_layout, null);
                LinearLayout linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
                FrameLayout print_badge = (FrameLayout) v.findViewById(R.id.badgelayout);
                linear_badge.setVisibility(View.VISIBLE);
                //badge_creator.createBadgeTemplate(badge_res, badgelayout,null);
                String whereClause;
                if (isOrderScaneed) {
                    whereClause = " where Event_Id = '" + checked_in_eventId
                            + "'" + " AND Attendee_Id = " + "'"
                            + attendee_id.get(position) + "'"
                            + " AND Order_Id = " + "'" + orderId + "'";
                } else {
                    whereClause = " where Event_Id = '" + checked_in_eventId
                            + "'" + " AND Attendee_Id = " + "'"
                            + attendee_id.get(position) + "'";
                }
                payment_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                payment_cursor.moveToFirst();
				/*String where_att = " Where EventID = '" + checked_in_eventId
						+ "' AND isBadgeSelected = 'Yes'";
				Cursor updated_badge = Util.db.getBadgeTemplate(where_att);
				//Log.i("--------- PrintAttendeesListActivity ----------",
						"-----Updated Badge Count is----"
								+ updated_badge.getCount());
				*/

                if(payment_cursor.getCount()==0){
                    payment_cursor.close();
                    payment_cursor=Util.db.getAllTypeAttendeeCursor(whereClause);
                    payment_cursor.moveToFirst();
                }

                print_badge.setVisibility(View.VISIBLE);

                if (payment_cursor.getCount() > 0) {
                    //CreateQrcode(updated_badge);
                    badge_creator.createBadgeTemplate(badge_res.get(0), print_badge, payment_cursor, true);
                }


/*				print_badge.setDrawingCacheEnabled(true);
				print_badge.buildDrawingCache(true);
				print_badge.buildDrawingCache();
*/
                qrcodename = payment_cursor.getString(payment_cursor
                        .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                        + payment_cursor.getString(payment_cursor
                        .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))
                        + attendee_id.get(position);
                qrcode_name.add(qrcodename);
                badge_frame_layout.add(print_badge);



            }catch (Exception e){
                e.printStackTrace();
            }
            return v;
        }
    }

    public Bitmap encodeQrCode(String data) {
        Bitmap b = null;
        try {
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;
            QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(data, null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(),
                    smallerDimension);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            try {
                b = qrCodeEncoder.encodeAsBitmap();
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setDialog() {
        try {
            msgDialog.showMsgNoButton(
                    getString(R.string.netPrinterListTitle_label),
                    getString(R.string.search_printer));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class SearchThread extends Thread {
        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {
            try {
                // search for net printer.
                if (netPrinterList(5)) {
                    isPrinter = true;
                    msgDialog.close();
                    printBadge();
                } else {
                    msgDialog.close();
                    PrintAttendeesListActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.setCustomAlertDialog(PrintAttendeesListActivity.this);

                            Util.txt_okey.setText("REPRINT");
                            Util.txt_dismiss.setVisibility(View.VISIBLE);
                            Util.txt_okey.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    doPrint();
                                    Util.alert_dialog.dismiss();
                                }
                            });

                            Util.txt_dismiss.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    //ShowTicketsDialog();
                                    Util.alert_dialog.dismiss();

                                }
                            });
                            Util.openCustomDialog("Alert", "No printer found. Do you want to reprint ?");
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean netPrinterList(int times) {
        boolean searchEnd = false;
        try {
            // clear the item list
            if (mItems != null) {
                mItems.clear();
            }
            // get net printers of the particular model
            mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            mNetPrinter = myPrinter.getNetPrinters(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW").replace("_","-"));
            final int netPrinterCount = mNetPrinter.length;

            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
                setPrefereces(mNetPrinter[0]);
            } else if (netPrinterCount == 0
                    && times == (Common.SEARCH_TIMES - 1)) { // when no printer
                // is found
                String dispBuff[] = new String[1];
                dispBuff[0] = getString(R.string.noNetDevice);
                mItems.add(dispBuff[0]);
                searchEnd = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchEnd;
    }


    public void setPrefereces(NetPrinter mNetPrinter) {
        try {
            // initialization for print
            PrinterInfo printerInfo = new PrinterInfo();
            Printer printer = new Printer();
            printerInfo = printer.getPrinterInfo();
            if (sharedPreferences.getString("printerModel", "").equals("")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW"));
                editor.putString("port", "NET");
                editor.putString("address", printerInfo.ipAddress.toString());
                editor.putString("macAddress", printerInfo.macAddress.toString());
                editor.putString("address", mNetPrinter.ipAddress);
                editor.putString("macAddress", mNetPrinter.macAddress);
                editor.putString("printer", mNetPrinter.modelName);
                editor.putString("paperSize", "W62H100");
                editor.putString("serNo", mNetPrinter.serNo);

                if(badge_res.size()==0) {
                    badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
                }
                else if(badge_res.size()>0){
                    BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
                    //Log.i("-----------------Badge Paper Size-----------",":"+badge_data.paperSize.contains(Util.BROTHER_DKN_5224)+" : "+badge_data.paperSize);
                    if (badge_data.paperSize.contains(Util.BROTHER_DK_1202)) {
                        // editor.putString("paperSize", "W62H100");
                        editor.putString("paperSize", "W62H100");
                    } else if (badge_data.paperSize.contains(Util.BROTHER_DK_12345)) {
                        editor.putString("paperSize", "W60H86");
                    } else if (badge_data.paperSize.contains(Util.BROTHER_DKN_5224)) {
                        editor.putString("paperSize", "W54");
                    }else if (badge_data.paperSize.contains("3\" x 1\"")) {//added for test in QA6
                        editor.putString("paperSize", "W29H90");
                    }else{
                        editor.putString("paperSize", "W62");
                    }
                }else{
                    AlertDialogCustom custom = new AlertDialogCustom(
                            PrintAttendeesListActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(PrintAttendeesListActivity.this,
                                    BadgeTemplateNewActivity.class), null, 2, false);
                    custom.show();
                }
                editor.putString("orientation", "LANDSCAPE");
                editor.putString("numberOfCopies", "1");
                editor.putString("halftone", "PATTERNDITHER");
                editor.putString("printMode", "FIT_TO_PAGE");
                editor.putString("pjCarbon", "false");
                editor.putString("pjDensity", "5");
                editor.putString("pjFeedMode", "PJ_FEED_MODE_FIXEDPAGE");
                editor.putString("align", "CENTER");
                editor.putString("leftMargin", "0");
                editor.putString("valign", "MIDDLE");
                editor.putString("topMargin", "0");
                editor.putString("customPaperWidth", "0");
                editor.putString("customPaperLength", "0");
                editor.putString("customFeed", "0");
                editor.putString("customSetting",
                        sharedPreferences.getString("customSetting", ""));
                editor.putString("rjDensity", "0");
                editor.putString("rotate180", "false");
                editor.putString("peelMode", "false");
                editor.putString("autoCut", "true");
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreferecesold(NetPrinter mNetPrinter) {
        try {
            // initialization for print
            PrinterInfo printerInfo = new PrinterInfo();
            Printer printer = new Printer();
            printerInfo = printer.getPrinterInfo();
            if (sharedPreferences.getString("printerModel", "").equals("")) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("printerModel", "QL_720NW");
                editor.putString("port", "NET");
                editor.putString("address", printerInfo.ipAddress.toString());
                editor.putString("macAddress", printerInfo.macAddress.toString());
                editor.putString("address", mNetPrinter.ipAddress);
                editor.putString("macAddress", mNetPrinter.macAddress);
                editor.putString("printer", mNetPrinter.modelName);
                // String[] arr =
                // modelInfo.getPortOrPaperSizeInfo("QL_720NW",Common.SETTINGS_PAPERSIZE);
                editor.putString("paperSize", "W62H100");
                if(badge_res.size()>0){
                    BadgeDataNew badge_data = new Gson().fromJson(badge_res.get(0).badge.Data__c, BadgeDataNew.class);
                    if (badge_data.paperSize.contains(Util.BROTHER_DK_1202)) {
                        // editor.putString("paperSize", "W62H100");
                        editor.putString("paperSize", "W62H100");
                    } else if (badge_data.paperSize.contains(Util.BROTHER_DK_12345)) {
                        editor.putString("paperSize", "W60H86");
                    } else if (badge_data.paperSize.contains(Util.BROTHER_DKN_5224)) {
                        editor.putString("paperSize", "W54");
                    }else{
                        editor.putString("paperSize", "W62");
                    }
                }else{
                    AlertDialogCustom custom = new AlertDialogCustom(
                            PrintAttendeesListActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(PrintAttendeesListActivity.this,
                                    BadgeTemplateNewActivity.class), null, 2, false);
                    custom.show();
                }
                editor.putString("orientation", "LANDSCAPE");
                editor.putString("numberOfCopies", "1");
                editor.putString("halftone", "PATTERNDITHER");
                editor.putString("printMode", "FIT_TO_PAGE");
                editor.putString("pjCarbon", "false");
                editor.putString("pjDensity", "5");
                editor.putString("pjFeedMode", "PJ_FEED_MODE_FIXEDPAGE");
                editor.putString("align", "CENTER");
                editor.putString("leftMargin", "0");
                editor.putString("valign", "TOP");
                editor.putString("topMargin", "0");
                editor.putString("customPaperWidth", "0");
                editor.putString("customPaperLength", "0");
                editor.putString("customFeed", "0");
                editor.putString("customSetting",
                        sharedPreferences.getString("customSetting", ""));
                editor.putString("rjDensity", "0");
                editor.putString("rotate180", "false");
                editor.putString("peelMode", "false");
                editor.putString("autoCut", "true");
                editor.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBitmap(Bitmap bitmap, String name) {
        String newFolder = "/Badges";
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/ScanAttendee");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File filename = new File(dir + newFolder);
        // Create a name for the saved image
        if (!filename.exists()) {
            filename.mkdir();
        }
        File file = new File(filename, name + ".png");// myimage.png
        //Log.i("Attendee Detail", "Save image path" + dir.toString() + newFolder);
        // Where to save it
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            // boolean success =
            // bitmap.compress(CompressFormat.PNG, 100,
            // out);
            if (bitmap != null)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            else
                //Log.i("Attendeee Detail", "Bitmap is null");
                out.flush();
            out.close();
            // Toast.makeText(getApplicationContext(), "File is Saved in  " +
            // filename, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printBadge() {
        try {
            mFiles.clear();
            String file_path = null;
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
            for (int i = 0; i < tickets_registerfor_print.size(); i++) {
                file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
                //Log.i("Attendee Detail", "Image Path=" + file_path);
                mFiles.add(file_path);
            }
            MsgDialog.intent_value = PrintAttendeesListActivity.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
            MsgDialog.isShowDialog = ext_settings.quick_print;
            if(ext_settings.doubleSide_badge) {
                sharedPreferences.edit().putString("autoCut","false").commit();
                sharedPreferences.edit().putString("endCut","true").commit();
                sharedPreferences.edit().putString("numberOfCopies", "2").commit();
            }else{
                sharedPreferences.edit().putString("autoCut","true").commit();
                sharedPreferences.edit().putString("endCut","").commit();
                sharedPreferences.edit().putString("numberOfCopies", "1").commit();
            }
            ((ImagePrint) myPrint).setFiles(mFiles);
            myPrint.print();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.globalnest.network.IPostResponse#insertDB()
     */
    @Override
    public void insertDB() {
        try {
            generateCsvFile(orderId);
            List<ScannedItems> scannedItem=Util.db.getSwitchedOnScanItem(checked_in_eventId);
            if(Util.db.getScannedItems(checked_in_eventId).size() > 0 && !scannedItem.isEmpty()){
    			/*MediaPlayer mediaPlayer = MediaPlayer.create(PrintAttendeesListActivity.this, R.raw.badgescanned);
				mediaPlayer.start();*/
                playSound(R.raw.badgescanned);
            }
            AlertDialogCustom alert_dialog = new AlertDialogCustom(PrintAttendeesListActivity.this);
            alert_dialog.setParamenters("Error", "Server is not reachable. Please check your internet connection.", null, null, 1, false);
            alert_dialog.setAlertImage(R.drawable.error,"error");
            alert_dialog.show();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
    private Handler mListViewDidLoadHanlder = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            doPrint();
            return false;
        }


    });

    public class ReasonHandler{
        String id="",reson="",attName="";
        @Override
        public String toString() {
            return "ResonHandler [id=" + id + ", reson=" + reson + "]";
        }
    }

    public void openCancelledOrderAlert(String alert,String msg) {
		/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
		mediaPlayer.start();*/
        playSound(R.raw.somethingwentwrong);
        Util.setCustomAlertDialog(PrintAttendeesListActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                // ShowTicketsDialog();
                Intent startentent = new Intent(PrintAttendeesListActivity.this,DashboardActivity.class);
                startentent.putExtra("CheckIn Event", checkedin_event_record);
                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startentent.putExtra(Util.HOME, "home_layout");
                startActivity(startentent);
                finish();
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                ShowTicketsDialog(1);
            }
        });
        Util.openCustomDialog(alert, msg);
    }
    private boolean createTemplates() {
        // TODO Auto-generated method stub
        try {
            badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
            ArrayList<LinearLayout> badge_layouts_list = new ArrayList<LinearLayout>();
            for (String attendeeid : attendee_id) {
                String whereClause;
                if (isOrderScaneed) {
                    whereClause = " where Event_Id = '" + checked_in_eventId
                            + "'" + " AND Attendee_Id = " + "'"
                            + attendeeid + "'"
                            + " AND Order_Id = " + "'" + orderId + "'";
                } else {
                    whereClause = " where Event_Id = '" + checked_in_eventId
                            + "'" + " AND Attendee_Id = " + "'"
                            + attendeeid + "'";
                }
                payment_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                payment_cursor.moveToFirst();
                if(payment_cursor.getCount()==0){
                    payment_cursor.close();
                    payment_cursor=Util.db.getAllTypeAttendeeCursor(whereClause);
                    payment_cursor.moveToFirst();
                }

                final View v = inflater.inflate(R.layout.badge_sample_layout, null);
                LinearLayout linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
                FrameLayout badgelayout = (FrameLayout) v.findViewById(R.id.badgelayout);
                linear_badge.setVisibility(View.INVISIBLE);
                badge_creator.createBadgeTemplate(badge_res.get(0), badgelayout, payment_cursor, true);
                linear_badge_parent.addView(v);
                badge_layouts_list.add(linear_badge);

            }
            Collections.reverse(badge_layouts_list);
            Collections.reverse(attendee_id);
            // _badgeadapter.notifyDataSetChanged();
            CreateBadges badge_task = new CreateBadges(badge_layouts_list);
            badge_task.execute();

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static  synchronized int createImage(final LinearLayout layout,final String qrcodename,int position){
        layout.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                layout.setDrawingCacheEnabled(true);
                layout.setDrawingCacheQuality(LinearLayout.DRAWING_CACHE_QUALITY_HIGH);
                layout.buildDrawingCache(true);
                saveBitmap(layout.getDrawingCache(), qrcodename);

            }
        });
        return position;
    }


    private class CreateBadges extends AsyncTask<String, Integer, Boolean>{
        private ArrayList<LinearLayout>badge_layouts_list = new ArrayList<LinearLayout>();
        public CreateBadges(ArrayList<LinearLayout> badge_layouts_list){
            this.badge_layouts_list = badge_layouts_list;
        }

        protected void onPreExecute(){
            super.onPreExecute();
            baseDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            baseDialog.setMax(attendee_id.size());
            baseDialog.setCancelable(false);
            baseDialog.setProgress(0);
            baseDialog.show();
        }

        /* (non-Javadoc)
         * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
         */
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                int position = 0;
                for (final LinearLayout layout : badge_layouts_list) {
                    String whereClause = " where Event_Id = '" + checked_in_eventId
                            + "'" + " AND Attendee_Id = " + "'"
                            + attendee_id.get(position) + "'";
                    payment_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                    payment_cursor.moveToFirst();
                    //final String name = payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)) + payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME));

                    if(payment_cursor.getCount()==0){
                        payment_cursor.close();
                        payment_cursor=Util.db.getAllTypeAttendeeCursor(whereClause);
                        payment_cursor.moveToFirst();
                    }
                    qrcodename = payment_cursor.getString(payment_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                            + payment_cursor.getString(payment_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))
                            + attendee_id.get(position);
                    //Log.i("-----------------Badge Name---------", ":" + qrcodename);
                    qrcode_name.add(qrcodename);
                    /*final int finalPosition = position;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {*/
                    onProgressUpdate(createImage(layout, qrcodename, position));
                   /*     }
                    });*/


                    position++;
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            baseDialog.setProgress(values[0]);
        }

        protected void onPostExecute(Boolean result){
            super.onPostExecute(result);
            baseDialog.dismiss();
            for(LinearLayout layout:badge_layouts_list){
                linear_badge_parent.removeView(layout);
            }
            mListViewDidLoadHanlder.sendEmptyMessage(0);
           /* if (isOnline()) {
                requestType = Util.CHECKIN;
                doRequest();
            } else {
                startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
            }*/
        }
    }
    public void showServiceRunningAlert(String event_name){
        Util.setCustomAlertDialog(PrintAttendeesListActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.alert);
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

            }
        });
        Util.openCustomDialog("Alert", "Please wait "+event_name+" event data is downloading...");
    }



}

