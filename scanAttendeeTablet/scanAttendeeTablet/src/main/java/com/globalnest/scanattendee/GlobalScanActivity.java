package com.globalnest.scanattendee;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v4.widget.DrawerLayout;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
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

public class GlobalScanActivity extends BaseActivity {
    // scanning fields
    private HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();
    private HashMap<String, Boolean> tickets_registerfor_print = new HashMap<String, Boolean>();

    private HttpPostData postMehod;
    private TotalOrderListHandler totalorderlisthandler;
    private Cursor attendee_cursor;
    // private Dialog ticket_dialog;
    private ImageView img_checkin_done, img_checkin_cancel, img_print,img_print_icon;
    View statusbar;
    private TextView txtprint,txtcheckinout;
    private TextView ticketname, txtticketnum, txt_oops, att_name;
    private ListCheckInAdapter checkin_adapter;
    //private ImageView tick_img;
    // private String _checked_in_eventId, _user_id;
    LinearLayout checkin_layout,print_layout,lay_switchs;
    Switch switch_checkin_print;
    RadioButton radiobutton_checkin,radiobutton_print;
    private CheckBox check_select,check_print;
    TextView btn_edit;
    private boolean isOrderScaneed=false,isticketscanned=false;// back_type = false;
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
    private FrameLayout linear_badge_parent,frame_layout_main;
    private ArrayList<String> qrcode_name;
    private ArrayList<String> attendee_id;
    private ArrayList<ReasonHandler> reason_array;
    private static  String globalreasonforreprint="";
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
    ArrayList<ReasonHandler> reasonNameList ;
    ReasonHandler handler;
    public static boolean isfrombuyerlist=false;
    public static boolean isswitch_checkin=false;
    boolean isFreeSessionon = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.activity_global_scan);
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        width = display.getWidth();// point.x;
        height = display.getHeight();
        badge_creator = new BadgeCreation(this, width, height);
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        ext_settings = new ExternalSettings();
        reasonNameList = new ArrayList<GlobalScanActivity.ReasonHandler>();
        handler = new ReasonHandler();
        reason_array = new ArrayList<ReasonHandler>();
        qrcode_name = new ArrayList<String>();
        if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
            ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }
        List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
        if (scanticks.size() > 0) {
            isFreeSessionon = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c,
                    checked_in_eventId);
        }
        if(Util.isMyServiceRunning(DownloadService.class, GlobalScanActivity.this)){
            showServiceRunningAlert(BaseActivity.checkedin_event_record.Events.Name);
        }else if(isOnline()){
            DoScannedData(data.getCharArrayExtra(NullChecker(Util.SCANDATA)));
        }else{
			/*Intent global_intent = new Intent(GlobalScanActivity.this, OfflineScanActivity.class);
		    global_intent.putExtra(Util.SCANDATA, data.getCharArrayExtra(Util.SCANDATA));
		    global_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    startActivity(global_intent);*/
            String scannedData = new String(data.getCharArrayExtra(Util.SCANDATA));
            generateCsvFile(scannedData.toString().trim());
            finish();
        }

//		DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
        back_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BaseActivity.ordersuccess_popupok_clicked)
                {
                    Intent i = new Intent(GlobalScanActivity.this, ManageTicketActivity.class);
                    BaseActivity.ordersuccess_popupok_clicked=false;
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(i);
                    activity.finish();
                    //activity.finish();

                }
                else if (getIntent().getBooleanExtra(Util.ISFINISH, false)) {
                    Intent i = new Intent();
                    if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(AddAttendeeActivity.class.getName())){
                        i = new Intent(GlobalScanActivity.this, AddAttendeeActivity.class);
                    }else{
                        i = new Intent(GlobalScanActivity.this,	ManageTicketActivity.class);
                    }
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                } else if (getIntent().getBooleanExtra(Util.CLASSNAME, false)){
                    isswitch_checkin=false;
                    Intent i = new Intent();
                    setResult(2017, i);
                    finish();
                }else {
                    Intent result = new Intent();
                    setResult(1987, result);
                    finish();
                }
            }
        });
        radiobutton_print.setChecked(true);
        radiobutton_checkin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isswitch_checkin = true;
                tickets_registerfor_print.clear();
                checkin_adapter.notifyDataSetChanged();
            }
        });
        radiobutton_print.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isswitch_checkin = false;
                tickets_register.clear();
                checkin_adapter.notifyDataSetChanged();
            }
        });
        switch_checkin_print.setChecked(true);
        switch_checkin_print.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        isswitch_checkin = false;
                        tickets_register.clear();
                        checkin_adapter.notifyDataSetChanged();
                    } else {
                        isswitch_checkin = true;
                        tickets_registerfor_print.clear();
                        checkin_adapter.notifyDataSetChanged();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        // sai change
		/*ticket_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View container,
									int arg2, long arg3) {
				//Log.i("-----------BarcodeScanActivity----------","Item Cklicked");
				attendee_cursor.moveToPosition(arg2);
				boolean isFreeSession = false;
				List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
				if(scanticks.size() > 0){
					isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
				}
				if(isFreeSession){
					CheckBox isselected = (CheckBox) container.findViewById(R.id.check_select);
					//ImageView img_unselect = (ImageView)container.findViewById(R.id.check_unselect);
					//Log.i("-----------Global Activity----------","Item Cklicked "+isselected.isChecked());
					boolean ischeckin = Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),Util.db.getSwitchedONGroupId(checked_in_eventId));
					if (!isselected.isChecked()) {
						//isselected.setVisibility(View.VISIBLE);
						isselected.setChecked(true);
						//img_unselect.setVisibility(View.GONE);

						if (ischeckin) {
							tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),true);
							img_checkin_done.setVisibility(View.VISIBLE);
						} else {
							tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),false);
							img_checkin_done.setVisibility(View.VISIBLE);
						}
						badgelabel_list.add(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
					} else {

						if(badgelabel_list.size() > 0){
							badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
						}
						//isselected.setVisibility(View.VISIBLE);
						isselected.setChecked(false);
						//img_unselect.setVisibility(View.VISIBLE);
						if (ischeckin) {
							tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							img_checkin_done.setVisibility(View.VISIBLE);
						} else {
							tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							img_checkin_done.setVisibility(View.VISIBLE);
						}

					}

				}*//*else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
					openScanSettingsAlert(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),GlobalScanActivity.class.getName());
				}*//*else{
					CheckBox isselected = (CheckBox) container.findViewById(R.id.check_select);
					//ImageView img_unselect = (ImageView)container.findViewById(R.id.check_unselect);
					//Log.i("-----------Global Activity----------","Item Cklicked "+isselected.isChecked());
					String tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
					if (!isselected.isChecked()) {
						//isselected.setVisibility(View.VISIBLE);
						isselected.setChecked(true);
						//img_unselect.setVisibility(View.GONE);

						if (NullChecker(tstatus).equalsIgnoreCase("true")) {
							tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),true);
							img_checkin_done.setVisibility(View.VISIBLE);
						} else {
							tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),false);
							img_checkin_done.setVisibility(View.VISIBLE);
						}
						badgelabel_list.add(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
					} else {

						if(badgelabel_list.size() > 0){
							badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
						}
						//isselected.setVisibility(View.VISIBLE);
						isselected.setChecked(false);
						//img_unselect.setVisibility(View.VISIBLE);
						if (NullChecker(tstatus).equalsIgnoreCase("true")) {
							tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							img_checkin_done.setVisibility(View.VISIBLE);
						} else {
							tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
							img_checkin_done.setVisibility(View.VISIBLE);
						}

					}
				}


			}
		});*/
       /* txtprint_selfcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tickets_registerfor_print.size() == 0) {
                    AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                    d.setParamenters("Alert !", "Please Select at least one ticket for Printing", null, null, 1, false);
                    d.show();
                } else if (tickets_registerfor_print.size() > 0) {
                    doprintProcess();
                }
            }
        });*/
        txtcheckin_selfcheckin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isswitch_checkin){
                    if (tickets_register.size() == 0) {
                        AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                        d.setParamenters("Alert !", "Please Select at least one ticket for Check-in ", null, null, 1, false);
                        d.show();
                    } else if (tickets_register.size() > 0) {
                        if (isOnline()) {
                            requestType = Util.CHECKIN;
                            doRequest();
                        } else {
                            startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                        }
                    }
                }else{
                    /*if (!Util.dashboardHandler.availableScanAttendeeTicket) {
                        Util.setCustomAlertDialog(GlobalScanActivity.this);
                        Util.openCustomDialog("Alert", Util.NOPRINTPERMISSION);
                        Util.txt_okey.setText("OK");
                        Util.txt_dismiss.setVisibility(View.GONE);
                        Util.txt_okey.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View arg0) {
                                Util.alert_dialog.dismiss();
                            }
                        });
                    }else {*/
                        if (tickets_registerfor_print.size() == 0) {
                            AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                            d.setParamenters("Alert !", "Please Select at least one ticket for Printing", null, null, 1, false);
                            d.show();
                        } else if (tickets_registerfor_print.size() > 0 && reasonNameList.size() > 0 && isValidate_badge_reg_settings) {
                            openprintDialog();
                        } else if (tickets_registerfor_print.size() > 0 && reasonNameList.size() > 0) {
                            doprintProcess();
                        }
                    }
                //}
                    /*if (tickets_registerfor_print.size() == 0 && tickets_register.size() == 0) {
                        AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                        d.setParamenters("Alert !", "Please Select at least one ticket for Check-in or Printing", null, null, 1, false);
                        d.show();
                    } else if (tickets_registerfor_print.size() > 0) {
                        doprintProcess();
                    } else if (tickets_register.size() > 0) {
                        if (isOnline()) {
                            requestType = Util.CHECKIN;
                            doRequest();
                        } else {
                            startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                        }

                    }
                }*/
                /*boolean error = false,error_onlycheckin = false;
                String item_pool_id = ITransaction.EMPTY_STRING;
                boolean isFreeSession = false;
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                if(scanticks.size() > 0){
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                }
                if(isFreeSession){
                    if(tickets_register.size()>0) {
                        for (String id : tickets_register.keySet()) {
                            boolean ischeckin = Util.db.SessionCheckInStatus(id, Util.db.getSwitchedONGroupId(checked_in_eventId));
                            if (ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id + checked_in_eventId, false)) {
                                error_onlycheckin = true;
                                break;
                            }
                        }
                        if (error_onlycheckin) {
                            showMessageAlert(getString(R.string.checkin_only_msg), false);
                        } else if (isOnline()) {
                            requestType = Util.CHECKIN;
                            doRequest();
                        } else {
                            startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                        }
                    }else if(tickets_registerfor_print.size() == 0){
                        AlertDialogCustom d=new AlertDialogCustom(GlobalScanActivity.this);
                        d.setParamenters("Alert !", "Please Select at least one ticket for Check-in or Printing", null, null, 1, false);
                        d.show();
                    }else{
                        doprintProcess();
                    }
                }else if(tickets_register.size()>0){
                    for(String id:tickets_register.keySet()){
                        item_pool_id = Util.db.getItemPoolID(id);
                        String status = Util.db.getTStatusBasedOnGroup(id, item_pool_id, checked_in_eventId);
                        boolean ischeckin = Boolean.valueOf(NullChecker(status));
                        if(!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)){
                            error = true;
                            break;
                        }else if(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                            error_onlycheckin = true;
                            break;
                        }
                    }
                    if(error_onlycheckin){
                        showMessageAlert(getString(R.string.checkin_only_msg),false);
                    }else if(error){
                        String item_pool_name="";
                        String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                        if(!NullChecker(parent_id).isEmpty()) {
                            item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        }
                        else {
                            item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                        }
                        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                            showSingleButtonDialog("Alert","Sorry! You are not allowed to check-in for "+item_pool_name,GlobalScanActivity.this);
                        }else {
                            openScanSettingsAlert(GlobalScanActivity.this, item_pool_id, GlobalScanActivity.class.getName());
                        }
                    }else if (isOnline()) {
                        requestType = Util.CHECKIN;
                        doRequest();
                    } else {
                        startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
                    }
                }else if(tickets_registerfor_print.size() == 0){
                    AlertDialogCustom d=new AlertDialogCustom(GlobalScanActivity.this);
                    d.setParamenters("Alert !", "Please Select at least one ticket for Check-in or Printing", null, null, 1, false);
                    d.show();
                }else{
                    doprintProcess();
                }*/
            }
        });
        img_checkin_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // ticket_dialog.dismiss();
                finish();
            }
        });



       /* print_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (tickets_registerfor_print.size() == 0) {
                    AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                    d.setParamenters("Alert", "Please select at least one ticket for Printing", null, null, 1, false);
                    d.show();
                }
                else if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                   *//* if (tickets_registerfor_print.size() == 0) {
                        AlertDialogCustom d = new AlertDialogCustom(GlobalScanActivity.this);
                        d.setParamenters("Alert", "Please select at least one ticket for Printing", null, null, 1, false);
                        d.show();
                    } else *//*
                    if (tickets_registerfor_print.size() > 0) {
                        qrcode_name.clear();
                        badge_frame_layout.clear();
                        attendee_id = new ArrayList<String>();
                        String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                                + checked_in_eventId + "' AND " + DBFeilds.BADGE_NEW_ID
                                + " = '" + checkedin_event_record.Events.badge_name + "'";
                        badge_res = Util.db.getAllBadges(where_att);
                        if (badge_res.size() > 0) {
                            prepareForPrint();
                        } else {
                            requestType = Util.LOAD_BADGE;
                            doRequest();
                        }
                    }
                }else{

                    Util.setCustomAlertDialog(GlobalScanActivity.this);
                    Util.openCustomDialog("Alert", "Printer is not connected.Do you want to Connect?");
                    Util.txt_okey.setText("CONNECT");
                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                    Util.txt_okey.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            //ShowTicketsDialog();
                            startActivity(new Intent(GlobalScanActivity.this,PrintersListActivity.class));
                            Util.alert_dialog.dismiss();
                        }
                    });
                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                        }
                    });
                    //Util.openprinternotconnectedpopup(GlobalScanActivity.this);
                }
            }
        });*/

    }
    private void doprintProcess(){
        try {
            if(!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                if (tickets_registerfor_print.size() > 0) {
                    qrcode_name.clear();
                    badge_frame_layout.clear();
                    attendee_id = new ArrayList<String>();
                    for (String key : tickets_registerfor_print.keySet()) {
                        attendee_id.add(key);
                    }
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
                Util.setCustomAlertDialog(GlobalScanActivity.this);
                Util.openCustomDialog("Alert", "Printer is not connected.Do you want to Connect?");
                Util.txt_okey.setText("CONNECT");
                Util.txt_dismiss.setVisibility(View.VISIBLE);
                Util.txt_okey.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        startActivity(new Intent(GlobalScanActivity.this,PrintersListActivity.class));
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
        }catch (Exception e){
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


            //Cursor badge_id_check = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,GlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
            Cursor badge_id_check = Util.db.getAttendeeDataCursorForScan(whereClause);
            if(badge_id_check.getCount()==0){
                badge_id_check.close();
                badge_id_check=Util.db.getAllTypeAttendeeCursor(whereClause);
                badge_id_check.moveToFirst();
            }
            //badge_id_check.moveToFirst();

            for (int i = 0; i < badge_id_check.getCount(); i++) {
                badge_id_check.moveToPosition(i);
                if (!Util.NullChecker(badge_id_check.getString(badge_id_check.getColumnIndex("BadgeId"))).isEmpty()&&!isValidate_badge_reg_settings) {
                    //openprintDialog();
                    ReasonHandler handler = new ReasonHandler();
                    handler.id = badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_ID));
                    handler.attName = badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                    reasonNameList.add(handler);
                    //reasonAttendeeNames[i]=badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                }
            }
            badge_id_check.close();
            if (tickets_registerfor_print.size() > 0) {
                for (String key : tickets_registerfor_print.keySet()) {
                    attendee_id.add(key);
                }
                if (reasonNameList.size() > 0) {
                    //TODO openprintDialog(reasonNameList);
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
                new IsBrotherPrinterConnectTask().execute();
            } else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                new IsPrinterConnectTask().execute();
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
                        AttendeeDetailActivity.openprinterNotConnectedDialog(GlobalScanActivity.this);
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
            }
        }
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
                    AttendeeDetailActivity.openprinterNotConnectedDialog(GlobalScanActivity.this);
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
                    i = new Intent(GlobalScanActivity.this, AddAttendeeActivity.class);
                }else{
                    i = new Intent(GlobalScanActivity.this,	ManageTicketActivity.class);
                }

                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else if (getIntent().getBooleanExtra(Util.CLASSNAME, false)){
                isswitch_checkin=false;
                Intent i = new Intent();
                setResult(2017, i);
                finish();
            }else {
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
            hideSoftKeyboard(GlobalScanActivity.this);
        }catch (Exception e){
            e.printStackTrace();
        }
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
        View v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setText("Scanned Tickets");
        img_socket_scanner.setVisibility(View.GONE);
        img_scanner_base.setVisibility(View.GONE);
        img_menu.setImageResource(R.drawable.back_button);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        badge_list = (ListView) linearview.findViewById(R.id.listview_badges);
        data = getIntent();
        txtcheckin_selfcheckin.setVisibility(View.VISIBLE);

        isfrombuyerlist=data.getBooleanExtra(Util.CLASSNAME,false);
       /* if(isfrombuyerlist){
            txtcheckin_selfcheckin.setText("Checkin");
            txtprint_selfcheckin.setVisibility(View.VISIBLE);
            txtprint_selfcheckin.setText("Print");
        }else {*/
        txtcheckin_selfcheckin.setText("Done");
        //}
        if(NullChecker(data.getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())
                || NullChecker(data.getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(AddAttendeeActivity.class.getName())){
            txt_title.setText("Order Tickets");
        }
        // ticket popup code
        att_name = (TextView) linearview.findViewById(R.id.checkinattname);
        img_checkin_done = (ImageView) linearview
                .findViewById(R.id.btncheckindone);
        img_checkin_cancel = (ImageView) linearview
                .findViewById(R.id.btncheckincancel);
        img_print = (ImageView) linearview.findViewById(R.id.btnprint);
        lay_switchs  =(LinearLayout) linearview.findViewById(R.id.lay_switchs);
        checkin_layout =(LinearLayout) linearview.findViewById(R.id.checkin_layout);
        print_layout =(LinearLayout) linearview.findViewById(R.id.print_layout);
        switch_checkin_print =(Switch)  linearview.findViewById(R.id.switch_checkin_print);
        radiobutton_checkin =(RadioButton)  linearview.findViewById(R.id.radiobutton_checkin);
        radiobutton_print =(RadioButton)  linearview.findViewById(R.id.radiobutton_print);
        txtprint = (TextView) linearview
                .findViewById(R.id.txt_print);
        txtcheckinout = (TextView) linearview
                .findViewById(R.id.txt_checkin_out);
        txt_oops = (TextView) linearview
                .findViewById(R.id.txt_global_scan_oops);
        ticket_view = (ListView) linearview
                .findViewById(R.id.checkinticketview);
        linear_badge_parent = (FrameLayout)linearview.findViewById(R.id.linear_badge_parent);
        frame_layout_main = (FrameLayout)linearview.findViewById(R.id.checkinpopup);
        frame_layout_main.setVisibility(View.VISIBLE);
        lay_switchs.setVisibility(View.VISIBLE);
        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
            if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)) {
                checkin_layout.setVisibility(View.VISIBLE);
            }else{
                checkin_layout.setVisibility(View.GONE);
            }
            if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)){
                print_layout.setVisibility(View.VISIBLE);
            }else{
                print_layout.setVisibility(View.GONE);

            }

        }
        if(Util.db.getGroupCount(checked_in_eventId) == 0){
            txtcheckinout.setVisibility(View.GONE);
        }else{
            txtcheckinout.setVisibility(View.VISIBLE);
        }

        /*if(BaseActivity.ordersuccess_popupok_clicked){
            // check_select.setVisibility(View.GONE);
            txtcheckinout.setVisibility(View.GONE);
        }*/
        //ShowTicketsDialog();
    }

    public void DoScannedData(char[] Data) {
        try {
            if(Data.length>0){
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
                }else if(isticketScanned(orderId)){
                    isticketscanned=true;
                    ShowTicketsDialog();
                }else if(isOrderScanned(orderId)){
                    isOrderScaneed = true;
                    ShowTicketsDialog();
                }else if (isOnline()) {
                    requestType = Util.GET_TICKET;
                    doRequest();
                } else {
                    startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
                }
            /*if(isOrderScaneed){
                txtprint.setVisibility(View.VISIBLE);
            }else{
                txtprint.setVisibility(View.GONE);
            }*/
            }} catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

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
                //boolean ischeckin = Boolean.valueOf(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN))));
                boolean ischeckin = Util.db.SessionCheckInStatus(id,Util.db.getSwitchedONGroupId(checked_in_eventId));
                //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                tickets_register.put(id,ischeckin);
                requestType = Util.CHECKIN;
                doRequest();
            }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    String item_pool_name = Util.db.getItem_Pool_Name(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    showSingleButtonDialog("Alert","Sorry! You are not allowed to check-in for "+item_pool_name,GlobalScanActivity.this);
                }else {
                    openScanSettingsAlert(GlobalScanActivity.this, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), GlobalScanActivity.class.getName());
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
                openScanSettingsAlert(GlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),TransperantGlobalScanActivity.class.getName());
            }
        }
    }
    private boolean ShowTicketsDialog() {

        try {
            dialog_count = 0;
            tickets_register.clear();
            tickets_registerfor_print.clear();
            //reason_array.clear();
            qrcode_name.clear();
            badge_frame_layout.clear();
            badgelabel_list.clear();
            String whereClause = "";
            //Log.i("BarCode Open Ticket Dialog", "Order_Id=" + orderId);
            if (isOrderScaneed||isOrderScanned(orderId)) {
                whereClause = " where Event_Id='" + checked_in_eventId	+ "' AND Order_Id='" + orderId.trim() + "'";
                attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
            } else if(isticketscanned){
                if(attendee_cursor==null) {
                    attendee_cursor = Util.db.getAllAttendeeswithBadgeId(orderId);
                }
            }else if(isticketScanned(orderId)){
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
                Util.db.getOrderStatuswithAttendee(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)), checked_in_eventId);
            }
            att_name.setTypeface(Util.droid_bold);
            //att_name.setText(totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getFirstName()	+ " "+ totalorderlisthandler.TotalLists.get(0).orderInn.buyerInfo.getLastName());
            //att_name.setText(Util.db.getBuyerName(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))));

            if (attendee_cursor.getCount() > 0) {
                //Added for default check all prints
                for (int i=0;i<attendee_cursor.getCount();i++) {
                    attendee_cursor.moveToPosition(i);
                    if (isValidate_badge_reg_settings) {
                        if (attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)).equalsIgnoreCase("B - Badge")
                                && attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)).equalsIgnoreCase("Printed")) {
                            tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                            badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                            handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
                            handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                            reasonNameList.add(handler);}
                    }else{
                        if (attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)).equalsIgnoreCase("B - Badge"))
                            tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                        badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                    }
                }
                txt_oops.setVisibility(View.GONE);
                ticket_view.setVisibility(View.VISIBLE);
                txt_title.setText(Util.db.getBuyerName(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)))+" ("+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_NUMBER))+")");
                //att_name.setText(Util.db.getBuyerName(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID))));
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
                    for (int i = 0; i < tickets_registerfor_print.size(); i++) {
                        file_path = dir.toString() + "/" + qrcode_name.get(i) + ".png";
                        //Log.i("Attendee Detail", "Image Path=" + file_path);
                        mFiles.add(file_path);
                    }
                    MsgDialog.intent_value = GlobalScanActivity.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
                    zebraPrinter.doZebraPrint(GlobalScanActivity.this,mFiles);
                }
            }).start();
        } else {
            //Log.i("Attendee Detail-----doprint", "sharedPrefrence is empty");
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

            badge_res = Util.db.getBadgeSelectedElseifDefaultbadge();
            if (badge_res.size()> 0) {
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
                        GlobalScanActivity.this);
                custom.setParamenters("Alert",
                        "No Badge Found, Do you want to select a Badge",
                        new Intent(GlobalScanActivity.this,
                                BadgeTemplateNewActivity.class), null, 2,false);
                custom.show();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    public void openprintDialog()//(final ArrayList<ReasonHandler> reasonHandeler, final CompoundButton check_print)
    {
        try {
            dialog_count++;
            print_dialog = new AlertDialog.Builder(GlobalScanActivity.this);
            LayoutInflater li = LayoutInflater.from(GlobalScanActivity.this);
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
                    .setPositiveButton("PRINT",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    reason = edit_reason.getText().toString().trim();
                                    //Log.i("Print Dialog ===", "Reason ====="+ reason);
                                    if (!NullChecker(reason).isEmpty()) {
                                      /*  handler.reason = reason;
                                        reasonHandeler.add(handler);
                                        reason_array=reasonHandeler;*///commented for bulk reasons

                                        globalreasonforreprint=reason;
                                        dialog.dismiss();
                                        hideSoftKeyboard(GlobalScanActivity.this);
                                        doprintProcess();
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
                                    /*tickets_registerfor_print.remove(handler.id);///commented for bulk reasons
                                    reasonHandeler.remove(handler);
                                    check_print.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));*/
                                    dialog.dismiss();
                                    hideSoftKeyboard(GlobalScanActivity.this);
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

    @SuppressWarnings("unused")
    private JSONArray setPrintBadgeBody() {
        try {
            int j = 0;
            String where_att = " Where EventID = '" + checked_in_eventId
                    + "' AND isBadgeSelected = 'Yes'";
            JSONArray badgearray = new JSONArray();

            try {
                for (String key : tickets_registerfor_print.keySet()) {
                    JSONObject obj = new JSONObject();

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
                     /* if (reason_array.size() > 0&&reason_array.size()>j) {
                        if(reason_array.get(j)!=null) {
                            if (key.trim().equals(reason_array.get(j).id)) {
                                obj.put("Reason", reason_array.get(j).reason);
                            } else {
                                obj.put("Reason", "");
                            }
                        }
                    }*/
                    if (!NullChecker(globalreasonforreprint).trim().isEmpty()) {
                        obj.put("Reason", globalreasonforreprint);
                    }
                    else {
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
                // sai change tick_img = (ImageView) v.findViewById(R.id.imgticketcheckin);

                check_select = (CheckBox) v.findViewById(R.id.check_select);
                check_print = (CheckBox) v.findViewById(R.id.check_print);
                btn_edit =(TextView) v.findViewById(R.id.btn_edit);
                img_print_icon = (ImageView) v.findViewById(R.id.img_print_icon);
                statusbar =(View) v.findViewById(R.id.statusbar);
                //ImageView img_unselect = (ImageView)v.findViewById(R.id.check_unselect);
                String status="";
                if(isFreeSessionon){
                    check_select.setVisibility(View.VISIBLE);
                }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
                    check_select.setVisibility(View.GONE);
                }
                if(isFreeSessionon){
                    status = String.valueOf(Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),Util.db.getSwitchedONGroupId(checked_in_eventId)));
                }else {
                    status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                }
                if (!Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                    statusbar.setVisibility(View.VISIBLE);
                    if (status.equals("true")) {
                        statusbar.setBackgroundColor(getResources().getColor(R.color.green_connected));
                    } else if (status.equals("false")) {
                        statusbar.setBackgroundColor(getResources().getColor(R.color.orange_bg));
                    } else {
                        statusbar.setBackgroundColor(getResources().getColor(R.color.gray_color));
                    }
                }

                if(tickets_register.containsKey(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))){
                    //img_unselect.setVisibility(View.GONE);
                    check_select.setChecked(true);
                    check_select.setVisibility(View.VISIBLE);
                }/*else{
                    check_select.setChecked(false);
                    check_select.setVisibility(View.VISIBLE);
                    //img_unselect.setVisibility(View.VISIBLE);
                }*/
                //check_select.setVisibility(View.GONE);
                check_select.setFocusable(false);
                check_print.setFocusable(false);
                // check_select.setEnabled(false);
                check_select.setClickable(true);
                check_print.setClickable(true);
                // sai change tick_img.setFocusable(false);
                txtticketnum.setFocusable(false);
                txtticketnum.setTypeface(Util.roboto_regular);


                if (attendee_cursor.getCount() > 0) {
					/*if(isOrderScaneed){
						if(!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")){
							check_select.setVisibility(View.GONE);
							//img_unselect.setVisibility(View.GONE);
						}
					}*/

                    if(!NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
                        check_print.setVisibility(View.GONE);
                        img_print_icon.setVisibility(View.GONE);
                    }
                    if(Util.getselfcheckinbools(Util.ISONLYSCANCHECKIN)){
                        check_print.setVisibility(View.GONE);
                        img_print_icon.setVisibility(View.GONE);
                    }if(Util.getselfcheckinbools(Util.ISPRINTALLOWED)){
                        check_select.setVisibility(View.GONE);
                    }
                    if(Util.db.getGroupCount(checked_in_eventId) == 0){
                        check_select.setVisibility(View.GONE);
                    }if(!isOrderScaneed&&!isticketscanned){
                        check_print.setVisibility(View.GONE);
                    }
                    if(isswitch_checkin){
                        check_print.setVisibility(View.GONE);
                    }else{
                        check_select.setVisibility(View.GONE);
                    }
                    String parent_id = Util.db.getItemPoolParentId(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                    if(!NullChecker(parent_id).isEmpty()){
                        String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        txtticketnum.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+" - "+  attendee_cursor
                                .getString(attendee_cursor.getColumnIndex("Tikcet_Number")));
                    }else{
                        txtticketnum.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" - "+  attendee_cursor
                                .getString(attendee_cursor
                                        .getColumnIndex("Tikcet_Number")));
                    }


                    ticketname.setText(attendee_cursor
                            .getString(attendee_cursor
                                    .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))
                            + " "
                            + attendee_cursor.getString(attendee_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                    // sai change tick_img.setVisibility(View.VISIBLE);
                    String tstatus = ITransaction.EMPTY_STRING;
                    boolean isFreeSession = false;
                    List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                    if(scanticks.size() > 0){
                        isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                    }
                    if(isFreeSession){
                        tstatus = String.valueOf(Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), Util.db.getSwitchedONGroupId(checked_in_eventId)));
                    }else{
                        tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId,Util.db.getSwitchedONGroupId(checked_in_eventId));
                    }

                    // sai change
					/*if (NullChecker(tstatus).equalsIgnoreCase("true")) {
						//tick_img.setBackgroundResource(R.drawable.green_price_bg);
						tick_img.setImageResource(R.drawable.yes);
						//check_select.setCircleColor(R.color.orange_bg);
					} else if (NullChecker(tstatus).equalsIgnoreCase("false")){
						//tick_img.setBackgroundResource(R.drawable.red_price_bg);
						tick_img.setImageResource(R.drawable.no);
						//check_select.setCircleColor(R.color.green_color);
					}else{
						tick_img.setVisibility(View.GONE);
					}*/
                } else {
                    txtticketnum.setText("Sorry! No Ticket Found with this order id.");
                }

                check_print.setTag(attendee_cursor.getPosition());
               /* if(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")
                        &&isfrombuyerlist ){
                    tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                    badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                   // buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));

                }*/
                if(tickets_registerfor_print.containsKey(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))){
                    //img_unselect.setVisibility(View.GONE);
                    check_print.setChecked(true);
                }
                btn_edit.setTag(attendee_cursor.getPosition());
                btn_edit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent att_activity = new Intent(GlobalScanActivity.this, AttendeeDetailActivity.class);
                        att_activity.putExtra("EVENT_ID", attendee_cursor.getString(attendee_cursor.getColumnIndex("Event_Id")));
                        att_activity.putExtra("ATTENDEE_ID", attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                        att_activity.putExtra("ORDER_ID", attendee_cursor.getString(attendee_cursor.getColumnIndex("Order_Id")));
                        startActivityForResult(att_activity,2017);
                    }
                });
                check_print.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        int position_tag = (Integer) buttonView.getTag();
                        AppUtils.displayLog("------------List Position---------", ":"+position_tag);
                        attendee_cursor.moveToPosition(position_tag);
                        if(isChecked){
                            if(isprinterconnectedopendialog())//||true
                            {
                                tickets_registerfor_print.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                                badgelabel_list.add(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
                                buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                                if (!Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex("BadgeId"))).isEmpty()&&!isValidate_badge_reg_settings)
                                {
                                    //reasonAttendeeNames[i]=badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+badge_id_check.getString(badge_id_check.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                }else{
                                    handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
                                    handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                    reasonNameList.add(handler);
                                    /*  openprintDialog(reasonNameList, buttonView);*///removed for Raj due to multiple reason for bulk printing
                                }
                            }
                            else {
                                tickets_registerfor_print.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                                buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                if(badgelabel_list.size() > 0){
                                    badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                                }
                                handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
                                handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                reasonNameList.remove(handler);
                                Util.setCustomAlertDialog(GlobalScanActivity.this);
                                String msg="",txtokay="";
                                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                    msg = "Printer is not connected.Please contact event Organizer!";
                                    txtokay="Ok";
                                    Util.txt_dismiss.setVisibility(View.GONE);
                                }else {
                                    msg="Printer is not connected.Do you want to Connect?";
                                    txtokay="CONNECT";
                                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                                }
                                Util.openCustomDialog("Alert", msg);
                                Util.txt_okey.setText(txtokay);
                                Util.txt_okey.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                            Util.alert_dialog.dismiss();
                                        }else {
                                            Util.alert_dialog.dismiss();
                                            startActivity(new Intent(GlobalScanActivity.this, PrintersListActivity.class));
                                        }

                                    }
                                });
                                Util.txt_dismiss.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        Util.alert_dialog.dismiss();
                                    }
                                });
                            }
                        }else{
                            tickets_registerfor_print.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                            buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                            if(badgelabel_list.size() > 0){
                                badgelabel_list.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                            }
                            handler.id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
                            handler.attName = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                            reasonNameList.remove(handler);
                        }

                    }
                });

                check_select.setTag(attendee_cursor.getPosition());

                check_select.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        boolean withoutalert=true;
                        int position_tag = (Integer) buttonView.getTag();
                        AppUtils.displayLog("------------List Position---------", ":"+position_tag);
                        attendee_cursor.moveToPosition(position_tag);
                        if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
                            check_select.setChecked(false);
                            buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                            showScannedTicketsAlert("Please TurnON at least one session for scanning.",true);
                            //showCustomToast(GlobalScanActivity.this,"Please TurnON at least one session for Check-ins.",R.drawable.img_like, R.drawable.toast_redrounded, false);
                        }
                        else if(isChecked){
                            String	tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId,Util.db.getSwitchedONGroupId(checked_in_eventId));
                            String orderstatus=Util.db.getOrderStatuswithAttendee(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)),checked_in_eventId);
                            String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                            boolean isFreeSession = false;
                            if(scanticks.size() > 0){
                                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                            }
                            if (isFreeSession){
                                if(!ext_settings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")&&!NullChecker(tstatus).equalsIgnoreCase("true")){
                                    withoutalert=false;
                                    final CompoundButton bb=buttonView;
                                    final boolean checked=isChecked;
                                    Util.setCustomAlertDialog(GlobalScanActivity.this);
                                    Util.alert_dialog.setCancelable(false);
                                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            startCheckclickupdate(bb, checked,attendee_cursor);
                                        }
                                    });
                                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            bb.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                        }
                                    });
                                    Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");
                                }
                            }else if (!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)) {
                                String item_pool_name = "";
                                String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                                if (!NullChecker(parent_id).isEmpty()) {
                                    item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                                } else {
                                    item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                                }
                                check_select.setChecked(false);
                                buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                //showCustomToast(GlobalScanActivity.this, "Sorry! You are not allowed to check-in for \"" + item_pool_name + "\".", R.drawable.img_like, R.drawable.toast_redrounded, false);
                                showCustomToast(GlobalScanActivity.this,"Sorry! You don't have permission to Check-In\n "+Util.db.getSwitchedONGroup(checked_in_eventId).Name, R.drawable.img_like, R.drawable.toast_redrounded, false);

                            }else if(!ext_settings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")&&!NullChecker(tstatus).equalsIgnoreCase("true")){
                                withoutalert=false;
                                final CompoundButton bb=buttonView;
                                final boolean checked=isChecked;
                                Util.setCustomAlertDialog(GlobalScanActivity.this);
                                Util.alert_dialog.setCancelable(false);
                                Util.txt_okey.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        startCheckclickupdate(bb, checked,attendee_cursor);
                                    }
                                });
                                Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        bb.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                    }
                                });
                                Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");
                            }else if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)||(Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))){
                                    withoutalert=false;
                                    String msg=" Already Checked In ";
                                    if((Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))){
                                        msg=" Already Checked-in,Check-out is disabled! ";
                                    }
                                    showCustomToast(GlobalScanActivity.this,msg,R.drawable.img_like, R.drawable.toast_redrounded, false);
                                    playSound(R.raw.error);
                                    check_select.setChecked(false);
                                    buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                }else if(!ext_settings.quick_checkin&&!orderstatus.equalsIgnoreCase("Fully Paid")){
                                    withoutalert=false;
                                    final CompoundButton bb=buttonView;
                                    final boolean checked=isChecked;
                                    Util.setCustomAlertDialog(GlobalScanActivity.this);
                                    Util.alert_dialog.setCancelable(false);
                                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            startCheckclickupdate(bb, checked,attendee_cursor);
                                        }
                                    });
                                    Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Util.alert_dialog.dismiss();
                                            bb.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                                        }
                                    });
                                    Util.openCustomDialog("Alert", "This Order Status is "+orderstatus+"! \n Do you still want to Continue?");
                                }else {
                                    buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                                }
                            }else{
                                buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                                startCheckclickupdate(buttonView, isChecked,attendee_cursor);
                            }
                        }else{
                            buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                            tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                        }
                        if(withoutalert){
                            startCheckclickupdate(buttonView, isChecked,attendee_cursor);}
                    }
                });

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
            //txtcheckinout.setText("check-in/out");
            postMehod = new HttpPostData("Ticket Checking In/Out...",setTicketCheckinUrl(), makeCheckin().toString(), access_token, GlobalScanActivity.this);
            postMehod.execute();
        }
        if (requestType.equals(Util.GET_ATTENDEE)) {
        	/*		postMehod = new HttpPostData(setAttendeeInfoUrl(), null,
					sfdcddetails.token_type, sfdcddetails.access_token,
					GlobalScanActivity.this);
			postMehod.execute();*/
            postMehod = new HttpPostData("Loading Attendees...",setAttendeeInfoUrl(), null, access_token, GlobalScanActivity.this);
            postMehod.execute();

        } else if (requestType.equals(Util.GET_TICKET)) {

            postMehod = new HttpPostData("Checking Attendee....",setTicketInfoUrl(), null, access_token, GlobalScanActivity.this);
            postMehod.execute();

        } else if (requestType.equals(Util.GET_BADGE_ID)) {


            postMehod = new HttpPostData("Generating Badge ID...",setBadgeIdUrl(), setPrintBadgeBody().toString(), access_token, GlobalScanActivity.this);
            postMehod.execute();

        }else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
            String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ checked_in_eventId;
            postMethod = new HttpPostData("Loading Badges...", _url, null, access_token, GlobalScanActivity.this);
            postMethod.execute();
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


                        ShowTicketsDialog();
                        boolean isCheckIn = false;
                        if(offlineResponse.SuccessTickets.size() == 1){
                            isCheckIn = Boolean.valueOf(offlineResponse.SuccessTickets.get(0).Status);//success.getJSONObject(0).optBoolean("Status");
                        }
						/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.badgescanned);
						mediaPlayer.start();*/

                        //Log.i("--------------Quick Print Options-----------",":"+ ext_settings.quick_print + success.length() + isCheckIn);
                        // already we are giving individial print so commented SAI
                       /* if(ext_settings.quick_print && offlineResponse.SuccessTickets.size() == 1 && isCheckIn) {

                            for (String ticket_id : attendee_id) {
                                Cursor attendee = Util.db.getAttendeeDataCursor(" where " + DBFeilds.ATTENDEE_EVENT_ID + " = '" + checked_in_eventId + "' AND " + DBFeilds.ATTENDEE_ID + "='" + ticket_id + "'", ITransaction.EMPTY_STRING, GlobalScanActivity.class.getName(), ITransaction.EMPTY_STRING);
                                attendee.moveToFirst();
                                badgelabel_list.add(attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE)));
                                String tstatus = Util.db.getTStatusBasedOnGroup(ticket_id, attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                                tickets_register.put(ticket_id, Boolean.valueOf(tstatus));
                                if (!Util.NullChecker(attendee.getString(attendee.getColumnIndex("BadgeId"))).isEmpty()) {
                                    ReasonHandler handler = new ReasonHandler();
                                    handler.id = attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_ID));
                                    handler.attName = attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)) + " " + attendee.getString(attendee.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME));
                                    reason_array.add(handler);
                                }
                                attendee.close();
                            }
                            if (isOnline()) {
                                executePrinterStatusTask();
                            }else{
                                startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
                            }
                        }else */
                        if(ext_settings.quick_checkin){
                            if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(SellOrderActivity.class.getName())){
                                Intent i = new Intent(GlobalScanActivity.this,ManageTicketActivity.class);
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
                        playSound(R.raw.error);
                        Util.setCustomAlertDialog(GlobalScanActivity.this);
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
            } else if (requestType.equals(Util.GET_ATTENDEE)) {
                gson = new Gson();
                totalorderlisthandler = gson.fromJson(response,	TotalOrderListHandler.class);
                Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
                requestType = Util.GET_TICKET;
                DoScannedData(data.getCharArrayExtra(Util.SCANDATA));
            } else if (requestType.equals(Util.GET_TICKET)) {
                JSONObject order_obj = new JSONObject(response);
                gson = new Gson();
                totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
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
                                    // tickets_registerfor_print.clear();
                                    //boolean ischeckin = Boolean.valueOf(NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN))));
                                    boolean ischeckin = Util.db.SessionCheckInStatus(id,Util.db.getSwitchedONGroupId(checked_in_eventId));
                                    //Log.i("---------------Free Session check in Status----------",":"+ischeckin);
                                    tickets_register.put(id,ischeckin);
                                    tickets_registerfor_print.put(id,ischeckin);
                                    requestType = Util.CHECKIN;
                                    doRequest();
                                }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
                                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                        String item_pool_name = Util.db.getItem_Pool_Name(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                                        showSingleButtonDialog("Alert","Sorry! You are not allowed to check-in for "+item_pool_name,GlobalScanActivity.this);
                                    }else {
                                        openScanSettingsAlert(GlobalScanActivity.this, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), GlobalScanActivity.class.getName());
                                    }
                                }else{
                                    String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                                    attendee_id = new ArrayList<String>();
                                    attendee_id.add(id);
                                    tickets_register.clear();
                                    //  tickets_registerfor_print.clear();
                                    String status = Util.db.getTStatusBasedOnGroup(id, attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                                    boolean ischeckin = Boolean.valueOf(NullChecker(status));
                                    tickets_registerfor_print.put(id,ischeckin);
                                    tickets_register.put(id,
                                            ischeckin);
                                    requestType = Util.CHECKIN;
                                    doRequest();
                                }

                            }
                        }else{
                            if(totalorderlisthandler.TotalLists.get(0).ticketsInn.size() == 0){
                                openCancelledOrderAlert("Error", "This ticket is cancelled. Please check with event admin.");
                            }else{
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
                                        if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                                            String item_pool_name = Util.db.getItem_Pool_Name(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                                            showSingleButtonDialog("Alert","Sorry! You are not allowed to check-in for "+item_pool_name,GlobalScanActivity.this);
                                        }else {
                                            openScanSettingsAlert(GlobalScanActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),GlobalScanActivity.class.getName());
                                        }
                                    }
                                }
                            }

                        }
                    }

                } else {
					/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
                    playSound(R.raw.somethingwentwrong);
                    Util.setCustomAlertDialog(GlobalScanActivity.this);
                    Util.alert_dialog.setCancelable(false);
                    Util.txt_dismiss.setVisibility(View.GONE);
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
                            Util.setCustomAlertDialog(GlobalScanActivity.this);
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
                            GlobalScanActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(GlobalScanActivity.this,
                                    BadgeTemplateNewActivity.class), null, 2, false);
                    custom.show();
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
                // payment_cursor = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,GlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
                payment_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                payment_cursor.moveToFirst();
				/*String where_att = " Where EventID = '" + checked_in_eventId
						+ "' AND isBadgeSelected = 'Yes'";
				Cursor updated_badge = Util.db.getBadgeTemplate(where_att);
				//Log.i("--------- GlobalScanActivity ----------",
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
                    GlobalScanActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Util.setCustomAlertDialog(GlobalScanActivity.this);

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
                            GlobalScanActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(GlobalScanActivity.this,
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
                            GlobalScanActivity.this);
                    custom.setParamenters("Alert",
                            "No Badge Selected, Do you want to select a Badge",
                            new Intent(GlobalScanActivity.this,
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
            MsgDialog.intent_value = GlobalScanActivity.this.getIntent().getStringExtra(Util.INTENT_KEY_1);
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
    			/*MediaPlayer mediaPlayer = MediaPlayer.create(GlobalScanActivity.this, R.raw.badgescanned);
				mediaPlayer.start();*/
                playSound(R.raw.badgescanned);
            }
            AlertDialogCustom alert_dialog = new AlertDialogCustom(GlobalScanActivity.this);
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
	    	/*for(int i=0;i<attendee_id.size();i++)
	    	badge_list.smoothScrollToPosition(i);*/

            //badgelistadapter.notifyDataSetChanged();
            doPrint();
            return false;
        }


    });

    public class ReasonHandler{
        String id="",reason="",attName="";
        @Override
        public String toString() {
            return "ReasonHandler [id=" + id + ", reason=" + reason + "]";
        }
    }

    public void openCancelledOrderAlert(String alert,String msg) {
		/*MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.somethingwentwrong);
		mediaPlayer.start();*/
        playSound(R.raw.somethingwentwrong);
        Util.setCustomAlertDialog(GlobalScanActivity.this);
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                // ShowTicketsDialog();
                Intent startentent = new Intent(GlobalScanActivity.this,DashboardActivity.class);
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
                //payment_cursor = Util.db.getAttendeeDataCursor(whereClause, ITransaction.EMPTY_STRING, GlobalScanActivity.class.getName(), ITransaction.EMPTY_STRING);
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
                    //payment_cursor = Util.db.getAttendeeDataCursor(whereClause,ITransaction.EMPTY_STRING,GlobalScanActivity.class.getName(),ITransaction.EMPTY_STRING);
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
        Util.setCustomAlertDialog(GlobalScanActivity.this);
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


    /*private void checkForSelfCheckinWithSingleAttendee() {
        try {
            if (BaseActivity.isselfCheckin) {
                String whereClause = "";
                if(attendee_cursor.getCount()==0) {
                    if (isOrderScaneed) {
                        whereClause = " where Event_Id='" + checked_in_eventId + "' AND Order_Id='" + orderId.trim() + "'";
                        attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
                    } else {
                        whereClause = " where Event_Id='" + checked_in_eventId + "' AND (BadgeId='" + orderId.trim() + "' OR " + DBFeilds.ATTENDEE_CUSTOM_BARCODE + " = '" + orderId.trim() + "')";
                        attendee_cursor = Util.db.getAttendeeDataCursorForScan(whereClause);
                    }
                }
                    if (attendee_cursor.getCount() == 1) {
                        final View v = inflater.inflate(R.layout.badge_sample_layout, null);
                        LinearLayout linear_badge = (LinearLayout) v.findViewById(R.id.linear_badge);
                        FrameLayout badgelayout = (FrameLayout) v.findViewById(R.id.badgelayout);
                        linear_badge.setVisibility(View.INVISIBLE);
                        *//*FrameLayout print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
						FrameLayout frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);*//*
						attendee_cursor.moveToFirst();
						String attendeeid = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
						PrintAndCheckin printT = new PrintAndCheckin();
						PrintDetails printDetails = new PrintDetails();
						printDetails.attendeeId = attendeeid;
						printDetails.checked_in_eventId = checked_in_eventId;
						printDetails.frame_transparentbadge = null;
						printDetails.order_id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID));
						printDetails.print_badge = badgelayout;
						printDetails.sfdcddetails = sfdcddetails;
						printT.doSaveAndPrint(this, printDetails);
						return;
					}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}*/
    public void startCheckclickupdate(CompoundButton buttonView,Boolean isChecked,Cursor attendee_cursor){
        boolean isFreeSession = false;
        List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
        if(scanticks.size() > 0){
            isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
        }
        if(!Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()&&isFreeSession){
            boolean ischeckin = Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),Util.db.getSwitchedONGroupId(checked_in_eventId));
            if(isChecked){
                if (ischeckin) {
                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)||(ischeckin && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))){
                        String msg=" Already Checked In ";
                        if((Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false))){
                            msg=" Already Checked-in,Check-out is disabled! ";
                        }
                        showCustomToast(GlobalScanActivity.this, msg, R.drawable.img_like, R.drawable.toast_redrounded, false);
                        playSound(R.raw.error);
                        check_select.setChecked(false);
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                    }else {
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                        tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), true);
                    }
                } else {
                    buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                    tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),false);
                }

            }else{
                if (ischeckin) {
                    tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                } else {
                    tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                }
                buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
            }
        }else if(!Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
            AppUtils.displayLog("----------------Cursor----------",":"+attendee_cursor.getCount());
            if(attendee_cursor.getCount() != 0){
                String tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex("Item_Pool_Id")), checked_in_eventId);
                String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                // String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), item_pool_id, checked_in_eventId);
                //boolean ischeckin = Boolean.valueOf(NullChecker(status));
                if(isChecked){
                    if(!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)){
                        String item_pool_name="";
                        String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
                        if(!NullChecker(parent_id).isEmpty()) {
                            item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                        } else {
                            item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
                        }
                        check_select.setChecked(false);
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                        //showCustomToast(GlobalScanActivity.this, "Sorry! You are not allowed to check-in for \""+item_pool_name+"\".", R.drawable.img_like, R.drawable.toast_redrounded, false);
                        showCustomToast(GlobalScanActivity.this,"Sorry! You don't have permission to Check-In\n "+Util.db.getSwitchedONGroup(checked_in_eventId).Name, R.drawable.img_like, R.drawable.toast_redrounded, false);
                    }
                    else if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                        tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),true);
                    } else {
                        buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_selected));
                        tickets_register.put(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")),false);
                    }
                }else{
                    buttonView.setButtonDrawable(getResources().getDrawable(R.drawable.check_unselected));
                    if (NullChecker(tstatus).equalsIgnoreCase("true")) {
                        tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                    } else {
                        tickets_register.remove(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                    }
                }
            }
        }
    }
    public  void docheckinprocessafterprint(String msg){
        Util.setCustomAlertDialog(GlobalScanActivity.this);
        if(msg.equals(getString(R.string.ErrorMessage_ERROR_NONE))){
            Util.openCustomDialog("Alert", msg);
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.GONE);
        }else if(tickets_register.size()>0){
            Util.openCustomDialog(msg, "Print Failed Do you checkin the Selected Attendees");
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.VISIBLE);
        }else {
            Util.openCustomDialog("Alert", msg);
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.VISIBLE);
        }
        Util.alert_dialog.setCancelable(false);
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Util.alert_dialog.dismiss();
                if(tickets_register.size()>0) {
                    requestType = Util.CHECKIN;
                    doRequest();
                }else if(BaseActivity.ordersuccess_popupok_clicked){
                    Intent i=new Intent();
                    if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                        i = new Intent(GlobalScanActivity.this, SelfCheckinAttendeeList.class);
                    }else {
                        i = new Intent(GlobalScanActivity.this, ManageTicketActivity.class);
                    }
                    BaseActivity.ordersuccess_popupok_clicked=false;
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(i);
                    activity.finish();
                }else{
                    ShowTicketsDialog();
                }

            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Util.alert_dialog.dismiss();
                finish();

            }
        });
    }



}

