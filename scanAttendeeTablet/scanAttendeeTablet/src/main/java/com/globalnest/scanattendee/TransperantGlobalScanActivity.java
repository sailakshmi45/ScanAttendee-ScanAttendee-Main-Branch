//  ScanAttendee Android
//  Created by Ajay on 29-Jun-2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

/**
 * @author saila_000
 *
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import com.globalnest.mvc.PrintDetails;
import com.globalnest.mvc.TStatus;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class TransperantGlobalScanActivity extends BaseActivity {
    // scanning fields
    private HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
    private HttpPostData postMehod;
    private TotalOrderListHandler totalorderlisthandler;
    private Cursor attendee_cursor;
    // private Dialog ticket_dialog;
    private ImageView img_checkin_done, img_checkin_cancel, img_print;
    private TextView ticketname, txtticketnum, txt_oops, att_name;
    private ListCheckInAdapter checkin_adapter;
    //private ImageView tick_img;
    // private String _checked_in_eventId, _user_id;
    private CheckBox check_select;
    private boolean isOrderScaneed=false;// back_type = false;
    PrintAndCheckin printT;
    PrintDetails printDetails;
    Boolean isReasonEmpty=false;
    @SuppressWarnings("unused")
    private String  QrId = "", orderId = "", url = "",
            checked_time = "", requestType = "", reason = "",qrcodename="";
    private BadgeListAdapter badgelistadapter;
    // private LayoutParams lp;
    private ArrayList<String> mFiles = new ArrayList<String>();
    private Cursor payment_cursor;
    private SearchThread searchPrinter;
    //private FrameLayout print_badge;
    private ListView badge_list, ticket_view;
    private FrameLayout linear_badge_parent,print_badge,frame_transparentbadge;
    private FrameLayout frame_popup;
    private TextView txt_fullname,txt_company;
    private ArrayList<String> qrcode_name = new ArrayList<String>();
    private ArrayList<String> attendee_id;
    private ArrayList<ReasonHandler> reaseon_array = new ArrayList<ReasonHandler>();
    private ArrayList<String> badgelabel_list = new ArrayList<String>();
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_global_scan);
        setCustomContentView(R.layout.activity_global_scan);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        width = display.getWidth();// point.x;
        height = display.getHeight();
        badge_creator = new BadgeCreation(this, width, height);
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        if( sfdcddetails==null){
            sfdcddetails= Util.db.getSFDCDDETAILS();
        }
        ext_settings = new ExternalSettings();
        data=getIntent();
        if(!NullChecker(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "")).isEmpty()){
            ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }
        if(Util.db.getGroupCount(BaseActivity.checkedin_event_record.Events.Id) == 0){
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                showSingleButtonDialog("Alert","You don't have any session to scan.Please contact Event Organizer!",this);
            }else{
                ScannedTicketsAlertwithCancelable("Please Buy at least one scanattendee ticket to scan session.",true);}
        }else if(Util.db.getSwitchedOnScanItem(BaseActivity.checkedin_event_record.Events.Id).isEmpty()){
            ScannedTicketsAlertwithCancelable("Please TurnON at least one session for scanning.",true);
        }else if(Util.isMyServiceRunning(DownloadService.class, TransperantGlobalScanActivity.this)){
            showServiceRunningAlert(BaseActivity.checkedin_event_record.Events.Name);
        }else if(isOnline()){
            DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
        }else{

            String scannedData = new String(data.getCharArrayExtra(Util.SCANDATA));
            generateCsvFile(scannedData.toString().trim());
            finish();
        }

        back_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().getBooleanExtra(Util.ISFINISH, false)) {
                    Intent i = new Intent(TransperantGlobalScanActivity.this, ManageTicketActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else {
                    Intent result = new Intent();
                    setResult(1987, result);
                    finish();
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // ticket_dialog.dismiss();
            if(getIntent().getBooleanExtra(Util.ISFINISH,false)){
                Intent i = new Intent(TransperantGlobalScanActivity.this,	ManageTicketActivity.class);
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
		/*Util.setCustomAlertDialog(GlobalScanActivity.this);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.dismiss_dialog.dismiss();
			}
		});
		Util.setDismissAlertDialog(GlobalScanActivity.this);
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.dismiss_dialog.dismiss();
				ShowTicketsDialog();
			}
		});*/
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
        setContentView(layout);
		/*View v = inflater.inflate(layout, null);
		linearview.addView(v);*/
		/*txt_title.setText("Scanned Tickets");
		img_menu.setImageResource(R.drawable.back_button);*/
        badge_list = (ListView) findViewById(R.id.listview_badges);
        data = getIntent();

        if(NullChecker(data.getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())){
            txt_title.setText("Order Tickets");
        }
        // ticket popup code
        att_name = (TextView) findViewById(R.id.checkinattname);
        img_checkin_done = (ImageView) findViewById(R.id.btncheckindone);
        img_checkin_cancel = (ImageView) findViewById(R.id.btncheckincancel);
        img_print = (ImageView) findViewById(R.id.btnprint);
        txt_oops = (TextView) findViewById(R.id.txt_global_scan_oops);
        ticket_view = (ListView) findViewById(R.id.checkinticketview);
        linear_badge_parent = (FrameLayout)findViewById(R.id.linear_badge_parent);
        frame_popup  = (FrameLayout)findViewById(R.id.frame_popup);
        txt_fullname  = (TextView) findViewById(R.id.txt_fullname);
        txt_company  = (TextView) findViewById(R.id.txt_company);
        print_badge = (FrameLayout) findViewById(R.id.frame_attdetailqrcodebadge);
        frame_transparentbadge = (FrameLayout) findViewById(R.id.frame_transparentbadge);
        //ShowTicketsDialog();
        frame_popup.setVisibility(View.GONE); //TODO
    }

    public void DoScannedData(char[] Data) {
        try {
            scanned_value = new String(Data);
            orderId = scanned_value.toString().trim();
			/*if (ext_settings.quick_checkin) {
				generateCsvFile(scanned_value.toString().trim());
			}*/
            if(isBadgeScanned(orderId)){
                isOrderScaneed= false;
                if(ShowTicketsDialog()){
                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                        checkForSelfCheckinWithSingleAttendee();
                    }else {
                        doCheckinProcess();
                    }
                }else{
                    isScannedItemNotopened();
                }
            }else if(isticketScanned(orderId)){
                isOrderScaneed= false;
                if(attendee_cursor==null) {
                    attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
                }
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    checkForSelfCheckinWithSingleAttendee();
                }
                else if(attendee_cursor.getCount()>=1) {
                    startSingleOrderCheckinProcess();
                }else{
                    Intent global_intent = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
                    global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(global_intent);
                    finish();
                }
            }else if(isOrderScanned(orderId)){
                isOrderScaneed = true;
                //ShowTicketsDialog();
                if(attendee_cursor==null) {
                    attendee_cursor = SocketBroadCastReciever.getAttendeeCursor(orderId);
                }else{
                    attendee_cursor.close();
                    attendee_cursor = SocketBroadCastReciever.getAttendeeCursor(orderId);
                }
                //ticketCheckin(this,attendee_cursor,Util.db.getSwitchedONGroupId(checked_in_eventId));
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    checkForSelfCheckinWithSingleAttendee();
                }
                else if(attendee_cursor.getCount()>=1) {
                    startSingleOrderCheckinProcess();
                }else{
                    Intent global_intent = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
                    global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(global_intent);
                    finish();
                }
            }else if (isOnline()) {
                requestType = Util.GET_TICKET;
                doRequest();
            } else {
                startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }


    public void doCheckinProcess(){
        if (ext_settings.quick_checkin && attendee_cursor.getCount() > 0 && !isOrderScaneed) {
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            boolean isFreeSession = false;
            if (scanticks.size() > 0) {
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                        checked_in_eventId);
            }
            if(isFreeSession){
                attendee_id = new ArrayList<String>();
                tickets_register.clear();
                boolean isBreak = false;
                for(int i = 0; i<attendee_cursor.getCount();i++){
                    attendee_cursor.moveToPosition(i);
                    //if (Util.db.isItemPoolBadgable(	attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
                    String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    boolean ischeckin = Util.db.SessionCheckInStatus(id,Util.db.getSwitchedONGroupId(checked_in_eventId));
                    //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                    if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                        showCustomToast(this,
                                Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                R.drawable.img_like, R.drawable.toast_redrounded, false);
                        playSound(R.raw.error);
                        /*isBreak = true;
                        showMessageAlert(getString(R.string.checkin_only_msg),true);
                        break;*/
                    }else{
                        attendee_id.add(id);
                        tickets_register.put(id,ischeckin);
                    }

                    //}
                    // sai change for merge tickets for Non ticketed session

                }
                if(!isBreak&&attendee_id.size()>0){
                    requestType = Util.CHECKIN;
                    doRequest();
                }else {
                    finish();
                }

            }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
                openScanSettingsAlert(TransperantGlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),GlobalScanActivity.class.getName());
            }else{
                attendee_id = new ArrayList<String>();
                tickets_register.clear();
                boolean isBreak = false;
                for(int i = 0; i<attendee_cursor.getCount();i++){
                    attendee_cursor.moveToPosition(i);
                    String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    String status = Util.db.getTStatusBasedOnGroup(id, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    boolean ischeckin = Boolean.valueOf(NullChecker(status));
                    if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                       /* isBreak = true;
                        showMessageAlert(getString(R.string.checkin_only_msg),true);
                        break;*/
                        showCustomToast(this,
                                Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                R.drawable.img_like, R.drawable.toast_redrounded, false);
                        playSound(R.raw.error);
                    }else{
                        attendee_id.add(id);
                        tickets_register.put(id,ischeckin);
                    }

                }
                if(!isBreak&&attendee_id.size()>0){
                    requestType = Util.CHECKIN;
                    doRequest();
                }else {
                    finish();
                }
            }

        }else {
            ticket_view.setVisibility(View.VISIBLE);
        }
    }
    private void checkForSelfCheckinWithSingleAttendee() {
        try {
            //if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
            if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)&&Util.getselfcheckinbools(Util.ISPRINTALLOWED)&&Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT)) {
                String whereClause = "";
                if (BaseActivity.isOrderScanned(orderId)) {
                    whereClause = " where Event_Id='" + checked_in_eventId + "' AND Order_Id='" + orderId.trim() + "'";
                    attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
                } else {
                    whereClause = " where Event_Id='" + checked_in_eventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
                    attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                }
                if (attendee_cursor.getCount() == 0) {
                    doRequest();
                } else if (attendee_cursor.getCount() == 1) {
                    if (NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")&&!isBadgeScanned(orderId)) {
						/*FrameLayout print_badge = (FrameLayout) findViewById(R.id.frame_attdetailqrcodebadge);
						FrameLayout frame_transparentbadge = (FrameLayout) findViewById(R.id.frame_transparentbadge);*/
                        attendee_cursor.moveToFirst();
                        String attendeeid = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                        printT = new PrintAndCheckin();
                        printDetails = new PrintDetails();
                        printDetails.attendeeId = attendeeid;
                        printDetails.checked_in_eventId = checked_in_eventId;
                        printDetails.frame_transparentbadge = frame_transparentbadge;
                        printDetails.order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
                        printDetails.print_badge = print_badge;
                        printDetails.sfdcddetails = sfdcddetails;
                        /**these params are required when any order/badge will scanned**/
                        printDetails.attendeeWhereClause = whereClause;
                        printDetails.isOrderScaneed = isOrderScaneed;
                        printDetails.qrCode = orderId;
                        //SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor=attendee_cursor;
                        if (!(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) && !(Util.getselfcheckinbools(Util.ISREPRINTALLOWED))) {

                            String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
                            String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.
                                            getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
                                    attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                            boolean ischeckin = Boolean.valueOf(NullChecker(status));
                            if (!sessionid.isEmpty() && ischeckin) {
                                Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
                                Util.alert_dialog.setCancelable(false);
                                Util.openCustomDialog("Alert", "Your Badge is Already Printed.Please contact Event Organizer!");
                                Util.txt_okey.setText("OK");
                                Util.txt_dismiss.setVisibility(View.GONE);
                                Util.txt_okey.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        Util.alert_dialog.dismiss();
                                        //startActivity(new Intent(BarCodeScanActivity.this, BarCodeScanActivity.class));
                                        finish();
                                    }
                                });
                            } else {
                                showCustomToast(this,
                                        "Your Badge is Already Printed.Please contact Event Organizer!",
                                        R.drawable.img_like, R.drawable.toast_redrounded, false);
                                if (!sessionid.isEmpty()) {
                                    ticketCheckin(this, attendee_cursor, sessionid);
                                }else{
                                    finish();
                                }
                            }
						/*AlertDialogCustom dialog = new AlertDialogCustom(
								BarCodeScanActivity.this);
						dialog.setParamenters("Alert",
								"Your Badge is Already Printed.Please contact Event Organizer!", null, null,
								1, false);
						dialog.show();*/
                        } else if(!(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) && (Util.getselfcheckinbools(Util.ISREPRINTALLOWED))){
                            if(isBadgeSelected()) {
                                if(BaseActivity.isBadgeScanned(orderId)) {
                                    openprintDialog();
                                }else if(BaseActivity.isBadgeScanned(orderId)){
                                    Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
                                    Util.alert_dialog.setCancelable(false);
                                    Util.openCustomDialog("Alert", "Please Scan only Order QR Code for Printing new Badge!");
                                    Util.txt_okey.setText("OK");
                                    Util.txt_dismiss.setVisibility(View.GONE);
                                    Util.txt_okey.setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            Util.alert_dialog.dismiss();
                                            //startActivity(new Intent(BarCodeScanActivity.this, BarCodeScanActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            }else {
                                BaseActivity.showSingleButtonDialog("Alert",
                                        "No Badge Selected, Please contact your Event Organizer!",this);
                            }
                        }else {
                            if(isBadgeSelected()){
                                //if(BaseActivity.isBadgeScanned(orderId)) {
                                printT.doSaveAndPrint(this, printDetails);
                                /*}else if(BaseActivity.isBadgeScanned(orderId)){
                                    Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
                                    Util.alert_dialog.setCancelable(false);
                                    Util.openCustomDialog("Alert", "Please Scan only Order QR Code for Printing new Badge!");
                                    Util.txt_okey.setText("OK");
                                    Util.txt_dismiss.setVisibility(View.GONE);
                                    Util.txt_okey.setOnClickListener(new OnClickListener() {

                                        @Override
                                        public void onClick(View arg0) {
                                            Util.alert_dialog.dismiss();
                                            //startActivity(new Intent(BarCodeScanActivity.this, BarCodeScanActivity.class));
                                            finish();
                                        }
                                    });
                                }*/
                            }else{
                                BaseActivity.showSingleButtonDialog("Alert",
                                        "No Badge Selected, Please contact your Event Organizer!",this);
                            }
                        }
                    }else{
                        String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
                        String status=Util.db.getTStatusBasedOnGroup(attendee_cursor.
                                        getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
                                attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                        boolean ischeckin = Boolean.valueOf(NullChecker(status));
                        if (!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge"))
                        {
                            showCustomToast(this,
                                    "Order ID contains NON-Badgeable ticket",
                                    R.drawable.img_like, R.drawable.toast_redrounded, false);
                        }
                        if(ischeckin){
                            showCustomToast(this,
                                    " Already Checked In",
                                    R.drawable.img_like,R.drawable.toast_redrounded,false);
                            playSound(R.raw.error);
                            finish();
                        }
                        if (!sessionid.isEmpty()&&!ischeckin) {
                            ticketCheckin(this, attendee_cursor, sessionid);
                        }
							/*barcodeScanned=true;
							scanStart();*/
                    }
                }/*else if(attendee_cursor.getCount() == 1&&!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")){
					String sessionid = Util.db.getSwitchedONGroupId(checked_in_eventId);
					String status=Util.db.getTStatusBasedOnGroup(attendee_cursor.
									getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
							attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
					boolean ischeckin = Boolean.valueOf(NullChecker(status));
					showCustomToast(this,
							"Order ID contains NON-Badgeable ticket",
							R.drawable.img_like, R.drawable.toast_redrounded, false);

					if (!sessionid.isEmpty()&&!ischeckin) {
						ticketCheckin(this, attendee_cursor, sessionid);
					}
						*//*barcodeScanned=true;
						scanStart();*//*
				}*/ else {
                    //	isScannedItemNotopened();
                    Intent i = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                    i.putExtra(Util.SCANDATA, orderId.toCharArray());
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                }
            }
            else if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)){
                try {
                    if(attendee_cursor.getCount()==0){
                        if(isOnline()) {
                            doRequest();
                        }else{
                            startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                        }
                    }
                    else if(attendee_cursor.getCount()==1){
                        startSingleOrderCheckinProcess();
						/*Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
						i.putExtra(Util.SCANDATA, orderId.toCharArray());
						i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(i);*/
                    }else{
                        Intent global_intent = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                        global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
                        global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(global_intent);
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                showCustomToast(this,
                        "You don't have permission to Scan, Please contact Event Organizer!",
                        R.drawable.img_like, R.drawable.toast_redrounded, false);
                finish();
            }
			/*else if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)&& getAttendeeCursor().getCount()==1){
				try {
					Intent i = new Intent(BarCodeScanActivity.this, TransperantGlobalScanActivity.class);
					i.putExtra(Util.SCANDATA, orderId.toCharArray());
					i.putExtra(Util.ACTION, BarcodeScanActivity.class.getName());
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}*/
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }

    private void startSingleOrderCheckinProcess() {
        try {
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            boolean isFreeSession = false;
            if (scanticks.size() > 0) {
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                        checked_in_eventId);
            }
            if (isFreeSession) {
                attendee_id = new ArrayList<String>();
                tickets_register.clear();
                for (int i = 0; i < attendee_cursor.getCount(); i++) {
                    attendee_cursor.moveToPosition(i);
                    //if (Util.db.isItemPoolBadgable(	attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
                    String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    boolean ischeckin = Util.db.SessionCheckInStatus(id, Util.db.getSwitchedONGroupId(checked_in_eventId));
                    //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                    if (ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
                        showCustomToast(this,
                                Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                R.drawable.img_like, R.drawable.toast_redrounded, false);
                        playSound(R.raw.error);
                        //Already checked in. Check out is disabled.
                        /*showMessageAlert(getString(R.string.checkin_only_msg), true);
                        break;*/
                    } else {
                        attendee_id.add(id);
                        tickets_register.put(id, ischeckin);
                    }

                    //}

                }
                if(isOnline()){
                    if(tickets_register.size()>0) {
                        requestType = Util.CHECKIN;
                        doRequest();
                    }else{
                        finish();
                    }
                }

            } /*else if (!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
                String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                String item_pool_name="";
                String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                if(!NullChecker(parent_id).isEmpty()) {
                    item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                } else {
                    item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                }
                showCustomToast(TransperantGlobalScanActivity.this, "Sorry! You are not allowed to check-in for \""+item_pool_name+"\".", R.drawable.img_like, R.drawable.toast_redrounded, false);

                *//*if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                    String item_pool_name = Util.db.getItem_Pool_Name(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    showSingleButtonDialog("Alert", "Sorry! You are not allowed to check-in for " + item_pool_name, this);
                } else {
                    openScanSettingsAlert(TransperantGlobalScanActivity.this, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), TransperantGlobalScanActivity.class.getName());
                }*//*//changed class name by sai
            }*/ else {
                attendee_id = new ArrayList<String>();
                tickets_register.clear();
                boolean isBreak = false;
                for (int i = 0; i < attendee_cursor.getCount(); i++) {
                    attendee_cursor.moveToPosition(i);
                    String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    String status = Util.db.getTStatusBasedOnGroup(id, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    boolean ischeckin = Boolean.valueOf(NullChecker(status));
                    if (Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)) {
                        if (ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
                            showCustomToast(this,
                                    Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                    R.drawable.img_like, R.drawable.toast_redrounded, false);
                            playSound(R.raw.error);
                        /*  isBreak = true;
                        showMessageAlert(getString(R.string.checkin_only_msg), true);
                        break;*/
                        }else {
                            attendee_id.add(id);
                            tickets_register.put(id, ischeckin);
                        }
                    }else {
                        String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                        String item_pool_name="";
                        String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                        if(!NullChecker(parent_id).isEmpty()) {
                            item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        } else {
                            item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                        }
                        showCustomToast(TransperantGlobalScanActivity.this,"Sorry! You don't have permission to Check-In "+Util.db.getSwitchedONGroup(checked_in_eventId).Name, R.drawable.img_like, R.drawable.toast_redrounded, false);

                        // showCustomToast(TransperantGlobalScanActivity.this, "Sorry! You are not allowed to check-in for \""+item_pool_name+"\".", R.drawable.img_like, R.drawable.toast_redrounded, false);
                      /*  attendee_id.add(id);
                        tickets_register.put(id, ischeckin);*/
                    }

                }
                if(isOnline()){
                    //if (!isBreak) {
                    if(tickets_register.size()>0) {
                        requestType = Util.CHECKIN;
                        doRequest();
                    }else{
                        finish();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
                openScanSettingsAlert(TransperantGlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),TransperantGlobalScanActivity.class.getName());
            }
        }
    }
    private boolean ShowTicketsDialog() {

        try {
            dialog_count = 0;
            tickets_register.clear();
            reaseon_array.clear();
            qrcode_name.clear();
            badge_frame_layout.clear();
            badgelabel_list.clear();
            String whereClause = "";
            //Log.i("BarCode Open Ticket Dialog", "Order_Id=" + orderId);
            if (isOrderScaneed||isOrderScanned(orderId)) {
                whereClause = " where Event_Id='" + checked_in_eventId	+ "' AND Order_Id='" + orderId.trim() + "'";
                attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
            } else if(isticketScanned(orderId)){
                if(attendee_cursor==null) {
                    attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
                }
            }else {
                whereClause = " where Event_Id='" + checked_in_eventId  + "' AND (BadgeId='" + orderId.trim() + "' OR "+DBFeilds.ATTENDEE_CUSTOM_BARCODE+" = '"+orderId.trim()+"')";
				/*if(checkedin_event_record.Events.AllowNoticketSessions__c){
					String email_id = Util.db.getAttendeeEmail(orderId.trim());
					String orderid = Util.db.getAttendeeOrderId(orderId.trim());
					whereClause = " where Event_Id='" + checked_in_eventId  +"' AND "+DBFeilds.ATTENDEE_EMAIL_ID+"='"+email_id+"' AND "+DBFeilds.ATTENDEE_ORDER_ID+"='"+orderid+"'";
				}else if(Util.db.isCustomBarcode(orderId.trim())){

					String email_id = Util.db.getAttendeeEmail(orderId.trim());
					String orderid = Util.db.getAttendeeOrderId(orderId.trim());
					whereClause = " where Event_Id='" + checked_in_eventId  +"' AND "+DBFeilds.ATTENDEE_EMAIL_ID+"='"+email_id+"' AND "+DBFeilds.ATTENDEE_ORDER_ID+"='"+orderid+"'";
				}*/
                attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
            }
            if(attendee_cursor!=null&&attendee_cursor.getCount() > 0) {
                attendee_cursor.moveToFirst();
                att_name.setTypeface(Util.droid_bold);
			/*att_name.setText(totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo
					.getFirstName()
					+ " "
					+ totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo
					.getLastName());*/

                txt_oops.setVisibility(View.GONE);
                att_name.setText(Util.db.getBuyerName(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))));
                checkin_adapter = new ListCheckInAdapter();
                ticket_view.setAdapter(checkin_adapter);
            } else{
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
                    for (int i = 0; i < tickets_register.size(); i++) {
                        file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
                        //Log.i("Attendee Detail", "Image Path=" + file_path);
                        mFiles.add(file_path);
                    }
                    zebraPrinter.doZebraPrint(TransperantGlobalScanActivity.this,mFiles);
                }
            }).start();
        } else {
            setDialog();
            searchPrinter = new SearchThread();
            searchPrinter.start();
        }
    }

    public void GlobalPrint(String ticket_id) {
        try {
            qrcode_name = new ArrayList<String>();
            attendee_id = new ArrayList<String>();
            // int dialog_count = 0;
            String where_att = " Where EventID = '" + checked_in_eventId
                    + "' AND isBadgeSelected = 'Yes'";
            badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
            if (badge_res.size() > 0) {
                attendee_id.add(ticket_id);
                if (isOnline()) {
                    requestType = Util.GET_BADGE_ID;
                    doRequest();
                } else {
                    startErrorAnimation(
                            getResources().getString(R.string.connection_error),
                            txt_error_msg);
                }
            } else {
                AlertDialogCustom custom = new AlertDialogCustom(
                        TransperantGlobalScanActivity.this);
                custom.setParamenters("Alert",
                        "No Badge Found, Do you want to select a Badge",
                        new Intent(TransperantGlobalScanActivity.this,
                                BadgeTemplateNewActivity.class), null, 2,false);
                custom.show();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public void openprintDialog(final ArrayList<ReasonHandler> resonHandeler) {
        try {
            dialog_count++;
            print_dialog = new AlertDialog.Builder(TransperantGlobalScanActivity.this);
            LayoutInflater li = LayoutInflater.from(TransperantGlobalScanActivity.this);
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
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    reason = edit_reason.getText().toString();
                                    if (!reason.isEmpty()) {
                                        reaseon_array=resonHandeler;
                                        //Log.i("Print Dialog ===","Reason array====="+ reaseon_array.size());
                                        if (isOnline()) {
                                            executePrinterStatusTask();
                                            //requestType = Util.GET_BADGE_ID;
                                            //doRequest();
                                        } else {
                                            startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
                                        }
                                        dialog.dismiss();
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
                                    dialog.dismiss();
                                    //ShowTicketsDialog();
                                }
                            });
            // create alert dialog
            AlertDialog alertDialog = print_dialog.create();
            // show it
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }



    private String setBadgeIdUrl() {
        return sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT;
    }

    @SuppressWarnings("unused")
    private JSONArray setPrintBadgeBody() {
        try {
            int i = 0;
            String where_att = " Where EventID = '" + checked_in_eventId
                    + "' AND isBadgeSelected = 'Yes'";
            //Cursor updated_badge1 = Util.db.getBadgeTemplate(where_att);
            //updated_badge1.moveToFirst();
            JSONArray badgearray = new JSONArray();
            try {
                for (int j = 0; j < attendee_id.size(); j++) {
                    i++;
                    JSONObject obj = new JSONObject();
                    obj.put("TicketId", attendee_id.get(j));
                    //obj.put("BadgeLabel", updated_badge1.getString(updated_badge1.getColumnIndex("BadgeName")));
                    //Log.i("----------------Badge Label List Size------------",":"+badgelabel_list.size());
                    if(badgelabel_list.size() > 0){
                        //Log.i("----------------Badge Label------------",":"+badgelabel_list.get(j));
                        obj.put("BadgeLabel", NullChecker(badgelabel_list.get(j)));
                    }
                    if (reaseon_array.size() > 0) {
                        ////Log.i("====== Reason Array Size =====",": In the if condition " + reaseon_array.size()+ " Reason array item="+ reaseon_array.get(j));
                        if(reaseon_array.get(j)!=null){
                            if(attendee_id.get(j).equals(reaseon_array.get(j).id))
                                obj.put("Reason", reaseon_array.get(j));
                        }else{
                            obj.put("Reason", "");
                        }
                    } else {
                        ////Log.i("====== Reason Array Size =====",": In the else Condition ");
                        obj.put("Reason", "");
                    }
                    obj.put("devicenm",Util.getDeviceNameandAppVersion());
                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                        obj.put("screenmode", "self checkin");
                    else obj.put("screenmode", "attendee mobile");
                    obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
                    obj.put("printtime",Util.getCurrentDateTimeInGMT());
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
                    obj.put("sessionid", Util.db.getSwitchedONGroupId(checked_in_eventId));
                    obj.put("sTime", Util.getCurrentDateTimeInGMT());
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

    private class ListCheckInAdapter extends BaseAdapter {


        public ListCheckInAdapter() {
            // TODO Auto-generated constructor stub
            //Log.i("-----------------Attendee List Count---------------",":"+attendee_cursor.getCount());
        }
        @Override
        public int getCount() {
            return attendee_cursor.getCount();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            try {
                attendee_cursor.moveToPosition(position);
                View v = inflater.inflate(R.layout.checkin_ticket_item_layout,null);
                v.setBackgroundColor(getResources().getColor(R.color.screen_bg_color));

                txtticketnum = (TextView) v.findViewById(R.id.ticketnum);
                ticketname = (TextView) v.findViewById(R.id.ticketname);
                //txt_isCheckin=(TextView) v.findViewById(R.id.txt_isCheckin);
                // sai change tick_img = (ImageView) v.findViewById(R.id.imgticketcheckin);

                check_select = (CheckBox) v.findViewById(R.id.check_select);
                //ImageView img_unselect = (ImageView)v.findViewById(R.id.check_unselect);
                if(tickets_register.containsKey(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))){
                    //img_unselect.setVisibility(View.GONE);
                    check_select.setChecked(true);
                    check_select.setVisibility(View.VISIBLE);
                }else{
                    check_select.setChecked(false);
                    check_select.setVisibility(View.VISIBLE);
                    //img_unselect.setVisibility(View.VISIBLE);
                }
                //check_select.setVisibility(View.GONE);
                check_select.setFocusable(false);
                // check_select.setEnabled(false);
                check_select.setClickable(true);
                // sai change tick_img.setFocusable(false);
                txtticketnum.setFocusable(false);
                txtticketnum.setTypeface(Util.roboto_regular);


                if (attendee_cursor.getCount() > 0) {
                    if(isOrderScaneed){
                        if(!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")){
                            check_select.setVisibility(View.GONE);
                            //img_unselect.setVisibility(View.GONE);
                        }
                    }

                    txtticketnum.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ("+  attendee_cursor
                            .getString(attendee_cursor
                                    .getColumnIndex("Tikcet_Number"))+")");

                    ticketname.setText(attendee_cursor
                            .getString(attendee_cursor
                                    .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                            + " "
                            + attendee_cursor.getString(attendee_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                    String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    if (NullChecker(status).equalsIgnoreCase("true")) {
                        //tick_img.setBackgroundResource(R.drawable.green_price_bg);
                        // sai change tick_img.setImageResource(R.drawable.yes);
                        //check_select.setCircleColor(R.color.orange_bg);
                    } else {
                        //tick_img.setBackgroundResource(R.drawable.red_price_bg);
                        // sai change tick_img.setImageResource(R.drawable.no);
                        //check_select.setCircleColor(R.color.green_color);
                    }
                } else {
                    txtticketnum.setText("Sorry! No Ticket Found with this order id.");
                }
                return v;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
			/*postObjectMehod = new HttpPostDataObject(setTicketCheckinUrl(),
					null, makeCheckin(), sfdcddetails.token_type,
					sfdcddetails.access_token, null, GlobalScanActivity.this);
			postObjectMehod.execute();*/
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)&&Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)||Util.getselfcheckinbools(Util.ISALLOWSCANTOPRINT)){
                String status=Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                boolean ischeckin = Boolean.valueOf(NullChecker(status));
				/*boolean ischeckin = Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),
						Util.db.getSwitchedONGroupId(checked_in_eventId));*/
                boolean isFreeSession = false;
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                if (scanticks.size() > 0) {
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                            checked_in_eventId);
                }
                if (isFreeSession) {
                    status = Util.db.SessionCheckInStringStatus(
                            attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),
                            Util.db.getSwitchedONGroupId(checked_in_eventId));
                    ischeckin=Boolean.valueOf(status);
                }
                if (ischeckin) {
                    showCustomToast(this,
                            " Already Checked In",
                            R.drawable.img_like,R.drawable.toast_redrounded,false);
                    finish();
                    //showMessageAlert("Already Checked In", true);
                    //Toast.makeText(this,"Attendee Already Checked In",Toast.LENGTH_SHORT).show();
                }else {
                    postMehod = new HttpPostData("Attendee Checking In/Checking Out...", setTicketCheckinUrl(), makeCheckin().toString(), access_token, TransperantGlobalScanActivity.this);
                    postMehod.execute();
                }
            }else {
                postMehod = new HttpPostData("Attendee Checking In/Checking Out...", setTicketCheckinUrl(), makeCheckin().toString(), access_token, TransperantGlobalScanActivity.this);
                postMehod.execute();
            }
        }
        if (requestType.equals(Util.GET_ATTENDEE)) {
        	/*		postMehod = new HttpPostData(setAttendeeInfoUrl(), null,
					sfdcddetails.token_type, sfdcddetails.access_token,
					GlobalScanActivity.this);
			postMehod.execute();*/
            postMehod = new HttpPostData("Loading Attendees...",setAttendeeInfoUrl(), null, access_token, TransperantGlobalScanActivity.this);
            postMehod.execute();

        } else if (requestType.equals(Util.GET_TICKET)) {

            postMehod = new HttpPostData("Checking Attendee....",setTicketInfoUrl(), null, access_token, TransperantGlobalScanActivity.this);
            postMehod.execute();

        } else if (requestType.equals(Util.GET_BADGE_ID)) {

/*			postObjectMehod = new HttpPostDataObject(setBadgeIdUrl(), null,
					setPrintBadgeBody(), sfdcddetails.token_type,
					sfdcddetails.access_token, null, GlobalScanActivity.this);
			postObjectMehod.execute();
*/
            postMehod = new HttpPostData("Generating Badge ID...",setBadgeIdUrl(), setPrintBadgeBody().toString(), access_token, TransperantGlobalScanActivity.this);
            postMehod.execute();

        }
    }

    public String setTicketCheckinUrl() {
        try {
            return sfdcddetails.instance_url
                    + WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="
                    + sfdcddetails.user_id+"&eventId="+checked_in_eventId+"&source=Online"+"&DeviceType="+Util.getDeviceNameandAppVersion().replaceAll(" ", "%20")
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
            if (requestType.equals(Util.CHECKIN)) {
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
                            boolean isCheckIn = Boolean.valueOf(checkin_obj.Status);//success.optJSONObject(i).optBoolean("Status");
                            String time = checkin_obj.TimeStamp;//success.optJSONObject(i).optString("TimeStamp");
                            String attendee_Id = checkin_obj.STicketId.Ticket__c;//success.optJSONObject(i).optJSONObject("STicketId").optString("Ticket__c");

                            dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time, checkedin_event_record.Events.Time_Zone__c);
                            //time = Util.new_db_date_format.format(Util.date_format_sec.parse(time));
                            //Log.i("Attendee List Activity", "Database date="+ time + "dialog time=" + dialogtime);
                            String name=Util.db.getAttendeeNameWithId(attendee_Id);
                            String company=Util.db.getAttendeeCompany(checkin_obj.STicketId.Ticket__r.Badge_ID__c);
                            if(isFreeSession){
                                List<TStatus> session_attendee = new ArrayList<TStatus>();
                                session_attendee.add(checkin_obj.STicketId);
                                Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                            }else{
                                Util.db.updateCheckedInStatus(checkin_obj.STicketId,checked_in_eventId);
                            }
                            if(isCheckIn){
                                //txt_fullname.setText(name);
                                // txt_company.setText(company);
                                showCustomToast(this,
                                        name+" Checked In Successfully! ",
                                        R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                                playSound(R.raw.beep);
                                // playSound(R.raw.badgescanned);
                                finish();
                            }else {
                                // txt_fullname.setText(name);
                                //  txt_company.setText(company);
                                showCustomToast(this,
                                        name+" Checked out Successfully! ",
                                        R.drawable.img_like,R.drawable.toast_redrounded,false);
                                playSound(R.raw.checkout);
                                finish();
                            }
                            if(ext_settings.quick_print&&!Util.db.getAttendeeisBadgePrinted(attendee_Id).equals("Printed")){
                                attendee_id.add(attendee_Id);
                            }
                            Util.db.deleteOffLineScans("("+DBFeilds.OFFLINE_BADGE_ID+" = '"+orderId
                                    +"' OR "+DBFeilds.OFFLINE_BADGE_ID+" = '"+orderId+"') AND "+DBFeilds.OFFLINE_ITEM_POOL_ID+" = '"+checkin_obj.STicketId.BLN_Session_Item__r.BLN_Item_Pool__c
                                    +"' AND "+DBFeilds.OFFLINE_EVENT_ID+" = '"+BaseActivity.checkedin_event_record.Events.Id+"'");
                        }


                        ShowTicketsDialog();
                        boolean isCheckIn = false;
                        if(offlineResponse.SuccessTickets.size() == 1){
                            isCheckIn = Boolean.valueOf(offlineResponse.SuccessTickets.get(0).Status);//success.getJSONObject(0).optBoolean("Status");
                        }

                        //Log.i("--------------Quick Print Options-----------",":"+ ext_settings.quick_print + success.length() + isCheckIn);
                        if(ext_settings.quick_print && offlineResponse.SuccessTickets.size() == 1 && isCheckIn&&!Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                            showCustomToast(this,
                                    "Checked In Successfully! ",
                                    R.drawable.img_like,R.drawable.toast_greenroundededge,false);
                            playSound(R.raw.beep);
                            for(String ticket_id:attendee_id){
                                Cursor attendee = Util.db.getAttendeeDataCursor(" where "+DBFeilds.ATTENDEE_EVENT_ID+" = '"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_ID+"='"+ticket_id+"'",ITransaction.EMPTY_STRING,TransperantGlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
                                attendee.moveToFirst();
                                if(attendee.getCount()==0){
                                    attendee.close();
                                    attendee=Util.db.getAllTypeAttendeeCursor(" where "+DBFeilds.ATTENDEE_EVENT_ID+" = '"+checked_in_eventId+"' AND "+DBFeilds.ATTENDEE_ID+"='"+ticket_id+"'");
                                    attendee.moveToFirst();
                                }
                                badgelabel_list.add(attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                                String status = Util.db.getTStatusBasedOnGroup(ticket_id, attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                                tickets_register.put(ticket_id, Boolean.valueOf(status));
                                if (!Util.NullChecker(attendee.getString(attendee.getColumnIndex("BadgeId"))).isEmpty()) {
                                    ReasonHandler handler=new ReasonHandler();
                                    handler.id=attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_ID));
                                    handler.attName=attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                    reaseon_array.add(handler);
                                }
                                attendee.close();
                            }
                            if(!isBadgeScanned(orderId)&&attendee_id.size()>0) {
                                executePrinterStatusTask();
                            }else{
                                finish();
                            }
							/*requestType = Util.GET_BADGE_ID;
							doRequest();*/
                        }else if(ext_settings.quick_checkin&&!Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                            if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())){
                                Intent i = new Intent(TransperantGlobalScanActivity.this,ManageTicketActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(i);
                                finish();
                            }
                        }
                    } else {
                        String msg = ITransaction.EMPTY_STRING;
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
                            msg = NullChecker(failes_ticks.msg);
                            attendee_name = attendee_name+Util.db.getAttendeeNameWithId(failes_ticks.tStaus.Ticket__c);
                            if(i != (offlineResponse.FailureTickets.size()-1)){
                                attendee_name = attendee_name+" , ";
                            }
                            i++;
                        }
					/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
                        playSound(R.raw.error);
                        Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
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

                        if(NullChecker(msg).isEmpty() || NullChecker(msg).equalsIgnoreCase("Already in system")){
                            Util.openCustomDialog("Alert",attendee_name + " is Already" +statusdisplay+ "\n at Time:"+dialogtime);
                            //Util.openCustomDialog("Check-in Failed",attendee_name + ". These attendees are already checked-in/out some other place in the event, Please check their status and try again.");
                        }else{
                            Util.openCustomDialog("Check-in Failed",msg);
                        }

                    }
                }
            } else if (requestType.equals(Util.GET_ATTENDEE)) {
                gson = new Gson();
                totalorderlisthandler = gson.fromJson(response,	TotalOrderListHandler.class);
                Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
                requestType = Util.GET_TICKET;
                DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
            } else if (requestType.equals(Util.GET_TICKET)) {
                JSONObject order_obj = new JSONObject(response);
                gson = new Gson();
                totalorderlisthandler = gson.fromJson(response,
                        TotalOrderListHandler.class);
                if (totalorderlisthandler.TotalLists.size() > 0) {

                    isOrderScaneed = Boolean.valueOf(NullChecker((order_obj.optString("isorder"))));
                    Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
/*					MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.right_sound);
					mediaPlayer.start();
*/					if(totalorderlisthandler.TotalLists.get(0).orderInn.getOrderStatus().equalsIgnoreCase("Cancelled")){
                        String msg = "This order is cancelled.";
                        if(totalorderlisthandler.TotalLists.get(0).orderInn.getOrderTotalAmount() > 0){
                            msg = "This order is cancelled please refund "+String.format("%.2f", totalorderlisthandler.TotalLists.get(0).orderInn.getOrderTotalAmount())+" to "+totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getFirstName()+" "
                                    +totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getLastName();
                        }
                        openCancelledOrderAlert("Error", msg);
                    }else{
                        if(ShowTicketsDialog()){
                            // createTemplates();
                            if (ext_settings.quick_checkin && attendee_cursor.getCount() > 0 && !isOrderScaneed) {
                                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                                boolean isFreeSession = false;
                                if (scanticks.size() > 0) {
                                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                                            checked_in_eventId);
                                }
                                if (isFreeSession) {
                                    attendee_id = new ArrayList<String>();
                                    tickets_register.clear();
                                    boolean isBreak = false;
                                    for (int i = 0; i < attendee_cursor.getCount(); i++) {
                                        attendee_cursor.moveToPosition(i);
                                        if (Util.db.isItemPoolBadgable(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),checked_in_eventId)) {
                                            String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                                            boolean ischeckin = Util.db.SessionCheckInStatus(id,Util.db.getSwitchedONGroupId(checked_in_eventId));
                                            if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                                showCustomToast(this,
                                                        Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                                        R.drawable.img_like, R.drawable.toast_redrounded, false);
                                                playSound(R.raw.error);
                                                /*isBreak = true;
                                                showMessageAlert(getString(R.string.checkin_only_msg),true);
                                                break;*/
                                            }else{
                                                attendee_id.add(id);
                                                tickets_register.put(id, ischeckin);
                                            }
                                        }

                                    }
                                    if(!isBreak&&attendee_id.size()>0) {
                                        requestType = Util.CHECKIN;
                                        doRequest();
                                    }else {
                                        finish();
                                    }

                                } else if (!Util.db.isItemPoolSwitchON(
                                        attendee_cursor.getString(
                                                attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                                        checked_in_eventId)) {

                                    openScanSettingsAlert(TransperantGlobalScanActivity.this,
                                            attendee_cursor.getString(
                                                    attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                                            TransperantGlobalScanActivity.class.getName());
                                } else {
                                    attendee_id = new ArrayList<String>();
                                    tickets_register.clear();
                                    boolean isBreak = false;
                                    for (int i = 0; i < attendee_cursor.getCount(); i++) {
                                        attendee_cursor.moveToPosition(i);
                                        String id = attendee_cursor
                                                .getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));

                                        String status = Util.db.getTStatusBasedOnGroup(
                                                attendee_cursor.getString(
                                                        attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),
                                                attendee_cursor.getString(
                                                        attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),
                                                checked_in_eventId);
                                        boolean ischeckin = Boolean.valueOf(NullChecker(status));

                                        if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                            showCustomToast(this,
                                                    Util.db.getAttendeeNameWithId(id) + " Already checked in. Check out is disabled.",
                                                    R.drawable.img_like, R.drawable.toast_redrounded, false);
                                            playSound(R.raw.error);
                                            /*isBreak = true;
                                            showMessageAlert(getString(R.string.checkin_only_msg),true);
                                            break;*/
                                        }else{
                                            attendee_id.add(id);
                                            tickets_register.put(id, ischeckin);
                                        }
                                    }
                                    if(!isBreak&&attendee_id.size()>0){
                                        requestType = Util.CHECKIN;
                                        doRequest();

                                    }else {
                                        finish();
                                    }
                                }

                            }else  if(isticketScanned(orderId)){
                                isOrderScaneed= false;
                                if(attendee_cursor==null) {
                                    attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
                                }
                                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                    checkForSelfCheckinWithSingleAttendee();
                                }
                                else if(attendee_cursor.getCount()>=1) {
                                    startSingleOrderCheckinProcess();
                                }else{
                                    Intent global_intent = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                                    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
                                    global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(global_intent);
                                    finish();
                                }

                            }else if(isOrderScaneed){
                                if(attendee_cursor==null) {
                                    attendee_cursor = SocketBroadCastReciever.getAttendeeCursor(orderId);
                                }else{
                                    attendee_cursor.close();
                                    attendee_cursor = SocketBroadCastReciever.getAttendeeCursor(orderId);
                                }
                                if(attendee_cursor.getCount()>1) {
                                    //ticketCheckin(this, attendee_cursor, Util.db.getSwitchedONGroupId(checked_in_eventId));
                                    startSingleOrderCheckinProcess();
                                }else {
                                    Intent global_intent = new Intent(TransperantGlobalScanActivity.this, GlobalScanActivity.class);
                                    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
                                    global_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(global_intent);
                                    finish();
                                }
                            }
                        }else{
                            //Log.i("-----------------Tickets Size---------",":"+totalorderlisthandler.TotalLists.get(0).ticketsInn.size());
                            if(totalorderlisthandler.TotalLists.get(0).ticketsInn.size() == 0){
                                openCancelledOrderAlert("Error", "This ticket is cancelled. Please check with event admin.");
                            }else{
                                String whereClause = " where Event_Id='" + checked_in_eventId  + "' AND (BadgeId='" + orderId.trim() + "' OR "+DBFeilds.ATTENDEE_CUSTOM_BARCODE+" = '"+orderId.trim()+"')";
                                attendee_cursor = Util.db.getAttendeeDetailsWithAllTypes(whereClause);
								/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
								mediaPlayer.start();*/
                                //Log.i("----------------Attendee Cursor Before If---------",":"+attendee_cursor);
                                if(attendee_cursor == null){
                                    openCancelledOrderAlert("Error", "This badge is not valid for this session. Please check with event admin.");
                                }else if(attendee_cursor.getCount() > 0){
                                    if(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TICKET_STATUS))).equalsIgnoreCase("Cancelled")){
                                        openCancelledOrderAlert("Error", "This ticket is cancelled. Please check with event admin.");
                                    }else{
                                        //Log.i("----------------Attendee Cursor In Else---------",":"+attendee_cursor);
                                        openScanSettingsAlert(TransperantGlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),TransperantGlobalScanActivity.class.getName());
                                    }
                                }
                            }

                        }
                    }

                } else {
					/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
                    playSound(R.raw.somethingwentwrong);
                    Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
                    Util.txt_dismiss.setVisibility(View.GONE);
                    Util.alert_dialog.setCancelable(false);
                    Util.setCustomDialogImage(R.drawable.error);
                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // ticket_dialog.dismiss();
                            Util.alert_dialog.dismiss();
                            //ShowTicketsDialog();
                            finish();
                        }
                    });
                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // ticket_dialog.dismiss();
                            Util.alert_dialog.dismiss();
                            ShowTicketsDialog();
                        }
                    });
                    String eventName="";

                    if(BaseActivity.checkedin_event_record!=null)
                        eventName=AppUtils.NullChecker(BaseActivity.checkedin_event_record.Events.Name);
                    else
                        eventName="";

                    if(eventName.trim().equalsIgnoreCase(""))
                        eventName="this";

                    Util.openCustomDialog("Alert","Invalid Badge \n This Order does not belongs to "+eventName+" event.");

                    //Util.openCustomDialog("Alert", "This Order does not belongs to this event.");
                }


            } else if (requestType.equals(Util.GET_BADGE_ID)) {
                //badge_list.setVisibility(View.VISIBLE);
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
                            Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
                            Util.txt_dismiss.setVisibility(View.GONE);
                            Util.setCustomDialogImage(R.drawable.error);
                            Util.txt_okey.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    // ticket_dialog.dismiss();
                                    Util.alert_dialog.dismiss();
                                    ShowTicketsDialog();
                                }
                            });
                            Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View arg0) {
                                    // ticket_dialog.dismiss();
                                    Util.alert_dialog.dismiss();
                                    ShowTicketsDialog();
                                }
                            });
                            Util.openCustomDialog("Alert",
                                    badge_obj.optString("Error"));
                        }
                    }
                    createTemplates();
                    //mListViewDidLoadHanlder.sendEmptyMessage(0);
                    //Log.i("Global Scan Activity",				"---------After the response loop----------");
                    // ticket_dialog.dismiss();
                    //badge_list.setVisibility(View.VISIBLE);
					/*badgelistadapter = new BadgeListAdapter();
					badge_list.setAdapter(badgelistadapter);*/
					/*if(createTemplates()){
						if(payment_cursor != null){
							payment_cursor.close();
						}
						if(ext_settings.quick_checkin || ext_settings.quick_print){
							finish();
						}
					}*/

                } else {
                    //Log.i("Attendee Detail", "respons====>" + response);
                    startErrorAnimation("Error in network", txt_error_msg);
                }
            }
        }

        catch (Exception e) {
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
            payment_cursor = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,TransperantGlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
            payment_cursor.moveToFirst();
				/*String where_att = " Where EventID = '" + checked_in_eventId
						+ "' AND isBadgeSelected = 'Yes'";
				Cursor updated_badge = Util.db.getBadgeTemplate(where_att);
				//Log.i("--------- GlobalScanActivity ----------",
						"-----Updated Badge Count is----"
								+ updated_badge.getCount());
				*/
            print_badge.setVisibility(View.VISIBLE);

            if (payment_cursor.getCount() > 0){
                //CreateQrcode(updated_badge);
                try {
                    badge_creator.createBadgeTemplate(badge_res.get(0),print_badge, payment_cursor,true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
			
				
/*				print_badge.setDrawingCacheEnabled(true);
				print_badge.buildDrawingCache(true);
				print_badge.buildDrawingCache();
*/				qrcodename = payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                    + payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME))
                    + attendee_id.get(position);
            qrcode_name.add(qrcodename);
            badge_frame_layout.add(print_badge);
            //saveBitmap(print_badge.getDrawingCache(), qrcodename);
				/*button_layout.setVisibility(View.GONE);
				btn_cancel.setVisibility(View.GONE);
				event_layout.setVisibility(View.GONE);
				btn_save.setVisibility(View.GONE);*/

            // txt_save.setVisibility(View.GONE);
            // txt_print.setVisibility(View.GONE);
            //btn_save.setText("Print");
            //for(int i=0;i<attendee_id.size();i++)
            // badge_list.smoothScrollToPosition(position);
				/*if(position==attendee_id.size()-1){
					//isLastPosition=true;
					mListViewDidLoadHanlder.sendEmptyMessage(0);
				}*/
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
                    TransperantGlobalScanActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);

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
                                    finish();

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
                editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL-720NW"));
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
                            TransperantGlobalScanActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(TransperantGlobalScanActivity.this,
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
                editor.putString("customSetting", sharedPreferences.getString("customSetting", ""));
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
                editor.putString("macAddress",
                        printerInfo.macAddress.toString());
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
                }else {
                    AlertDialogCustom custom = new AlertDialogCustom(
                            TransperantGlobalScanActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(TransperantGlobalScanActivity.this,
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
            for (int i = 0; i < tickets_register.size(); i++) {
                file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
                //Log.i("Attendee Detail", "Image Path=" + file_path);
                mFiles.add(file_path);
            }
            MsgDialog.intent_value = TransperantGlobalScanActivity.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
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
    			/*MediaPlayer mediaPlayer = MediaPlayer.create(TransperantGlobalScanActivity.this, R.raw.badgescanned);
				mediaPlayer.start();*/
                playSound(R.raw.badgescanned);
            }
            Intent result = new Intent();
            setResult(1987, result);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Handler mListViewDidLoadHanlder = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
	    	/*for(int i=0;i<attendee_id.size();i++)
	    	badge_list.smoothScrollToPosition(i);*/

            //badgelistadapter.notifyDataSetChanged();
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
        Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                // ShowTicketsDialog();
                Intent startentent = new Intent(TransperantGlobalScanActivity.this,DashboardActivity.class);
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
                ShowTicketsDialog();
            }
        });
        Util.openCustomDialog(alert, msg);
    }
    private boolean createTemplates() {
        // TODO Auto-generated method stub
        badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
        if(badge_res.size()==0){
            AlertDialogCustom custom = new AlertDialogCustom(TransperantGlobalScanActivity.this);
            custom.setParamenters("Alert", "No Badges Found, Do you want to select a Badge.",
                    new Intent(TransperantGlobalScanActivity.this, BadgeTemplateNewActivity.class), null, 2,
                    true);
            custom.show();
        }else{
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
                payment_cursor = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,TransperantGlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
                payment_cursor.moveToFirst();

                final View v = inflater.inflate(R.layout.badge_sample_layout, null);
                LinearLayout linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
                FrameLayout badgelayout = (FrameLayout) v.findViewById(R.id.badgelayout);
                linear_badge.setVisibility(View.INVISIBLE);
                try {
                    badge_creator.createBadgeTemplate(badge_res.get(0), badgelayout,payment_cursor,true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                linear_badge_parent.addView(v);
                badge_layouts_list.add(linear_badge);

            }
            Collections.reverse(badge_layouts_list);
            Collections.reverse(attendee_id);
            // _badgeadapter.notifyDataSetChanged();
            CreateBadges badge_task = new CreateBadges(badge_layouts_list);
            badge_task.execute();
        }



        return true;
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
        @SuppressLint("WrongThread")
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            int position=0;
            for(final LinearLayout layout : badge_layouts_list){
                String whereClause = " where Event_Id = '" + checked_in_eventId
                        + "'" + " AND Attendee_Id = " + "'"
                        + attendee_id.get(position) + "'";
                payment_cursor = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,TransperantGlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
                payment_cursor.moveToFirst();
                //final String name = payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)) + payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME));


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
                onProgressUpdate(createImage(layout,qrcodename, position));
                   /* }
                });*/
                position++;
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
        }
    }
    public void showServiceRunningAlert(String event_name){
        Util.setCustomAlertDialog(TransperantGlobalScanActivity.this);
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

    public void openprintDialog() {
        final Context context = TransperantGlobalScanActivity.this;
        try {
            print_dialog = new AlertDialog.Builder(TransperantGlobalScanActivity.this);
            LayoutInflater li = LayoutInflater
                    .from(TransperantGlobalScanActivity.this);
            View promptsView = li.inflate(R.layout.print_dialog_layout, null);
            print_dialog.setView(promptsView);
            final EditText edit_reason = (EditText) promptsView.findViewById(R.id.edit_reason);
            final TextView txt_message=(TextView) promptsView.findViewById(R.id.txt_message);
            final TextView txt_top=(TextView) promptsView.findViewById(R.id.textView1);
            edit_reason.setText("");
            if(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()){
                txt_message.setVisibility(View.VISIBLE);
                edit_reason.setVisibility(View.GONE);
            }else{
                txt_top.setText("Badge is already printed. Do you want to reprint ?\n" +
                        "The previous badge will become invalid.");
                txt_message.setVisibility(View.GONE);
                edit_reason.setVisibility(View.VISIBLE);
            }
            print_dialog
                    .setCancelable(false)
                    .setPositiveButton("Reprint",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    reason = edit_reason.getText().toString();
                                    if(!attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()){
                                        if (!reason.equalsIgnoreCase("") ) {
                                            try {
                                                printDetails.reason=edit_reason.getText().toString();
                                                printT.doSaveAndPrint(context, printDetails);
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }else if(reason.trim().isEmpty()){
                                            isReasonEmpty=true;
                                            edit_reason.setFocusable(true);
                                            edit_reason.setError("Reason should not be empty");

                                        }
                                    }
                                }})
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isReasonEmpty=false;
                            hideSoftKeyboard(TransperantGlobalScanActivity.this);
                            dialog.dismiss();
                            finish();

                        }
                    });


            // create alert dialog
            final AlertDialog alertDialog = print_dialog.create();

            alertDialog.show();

            alertDialog
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            //hideSoftKeyboard(BarCodeScanActivity.this);
                            if (isReasonEmpty) {
                                hideSoftKeyboard(TransperantGlobalScanActivity.this);
                                edit_reason.setEnabled(false);
                                alertDialog.dismiss();
                                alertDialog.show();
                                edit_reason.setError("Reason should not be empty");
                                edit_reason.requestFocus();
                            } else {
                                return;
                            }

                        }
                    });
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
            txt_message.setText("Do you want to print the badge?");




        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executePrinterStatusTask() {
        if(isOnline()) {
            if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
                new IsBrotherPrinterConnectTask().execute();
            } else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                new IsPrinterConnectTask().execute();
            }else{
                openprinterNotConnectedDialog(this,true);
            }
        }else{
            startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
        }
    }

    public static void openprinterNotConnectedDialog(final Context context, final boolean isFinish){
        Util.setCustomAlertDialog(context);
        String alertmsg = "";
        //if ((!(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))||!(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")))) {
        if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
            alertmsg = "Printer is disconnected.Do you want to Connect?";
        } else {
            alertmsg = "Printer is not connected.Do you want to Connect?";
        }
        Util.openCustomDialog("Alert", alertmsg);
        Util.txt_okey.setText("CONNECT");
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.txt_okey.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //ShowTicketsDialog();
                context.startActivity(new Intent(context, PrintersListActivity.class));
                Util.alert_dialog.dismiss();
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                Util.alert_dialog.dismiss();
                if(isFinish){
                    ((Activity)context).finish();
                }
            }
        });
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


    private  class IsPrinterConnectTask extends com.globalnest.stripe.android.compat.AsyncTask<Void,Void,Void> {
        private boolean isPrinterConnectedStatus=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                if(!BaseActivity.isValidate_badge_reg_settings){
                    createTemplates();
                }else {
                    if (isOnline()) {
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
                }
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openprinterNotConnectedDialog(TransperantGlobalScanActivity.this,true);
                    }
                });
            }
        }
    }



    private  class IsBrotherPrinterConnectTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                if(!BaseActivity.isValidate_badge_reg_settings){
                    createTemplates();
                }else{
                    if (isOnline()) {
                        requestType = Util.GET_BADGE_ID;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                    }
                }

            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openprinterNotConnectedDialog(TransperantGlobalScanActivity.this,true);
                    }
                });
            }
        }
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
                    openprinterNotConnectedDialog(TransperantGlobalScanActivity.this,true);
                }
            });
            e.printStackTrace();
            return false;
        }
    }
}

