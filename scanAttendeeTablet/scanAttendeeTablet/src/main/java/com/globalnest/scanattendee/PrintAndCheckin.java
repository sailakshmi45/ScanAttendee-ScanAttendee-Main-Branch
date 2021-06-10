package com.globalnest.scanattendee;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.globalnest.brother.ptouch.sdk.printdemo.common.Common;
import com.globalnest.brother.ptouch.sdk.printdemo.printprocess.ImagePrint;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeCreation;
import com.globalnest.mvc.BadgeDataNew;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.stripe.android.compat.AsyncTask;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zebra.sdk.comm.ConnectionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.globalnest.scanattendee.AttendeeDetailActivity.loadBitmapFromView;

/**
 * Created by sailakshmi on 23-05-2017.
 */

public class PrintAndCheckin {

    private Context mContext;
    private PrintDetails mPrintData;
    private Cursor payment_cursor;
    private String requestType="";
    private HttpPostData httpPostData;
    private InputMethodManager inputMethodManager;
    private String qrcode_name="";
    private ArrayList<BadgeResponseNew> badge_res;
    ArrayList<String> mFiles = new ArrayList<String>();
    private ExternalSettings ext_settings;

    public void doSaveAndPrint(Context context,PrintDetails printData) throws SQLException {
        try {
            mContext = context;
            mPrintData = printData;
            mPrintData.isselfCheckinbool = Util.getselfcheckinbools(Util.ISSELFCHECKIN);
            String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                    + mPrintData.checked_in_eventId + "' AND " + DBFeilds.BADGE_NEW_ID
                    + " = '" + BaseActivity.checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
            badge_res = Util.db.getAllBadges(where_att);
            //if(AppUtils.isOnline(mContext)) {
            BaseActivity.baseDialog.setMessage("Please wait...");
            BaseActivity.baseDialog.show();
            if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    new IsPrinterConnectTask().execute();
                }else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
                    dismissProgressDialog();
                    startPrintTask();
                }
            } else if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
                if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    new SearchPrinterStatusThread().run();
                }else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_BT_MAC, "").isEmpty()){
                    dismissProgressDialog();
                    startPrintTask();
                }
            } else if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Cloud Print") && BaseActivity.isCloudPrintingON) {
                dismissProgressDialog();
                startPrintTask();
            } else {
                dismissProgressDialog();
                AttendeeDetailActivity.openprinterNotConnectedDialog(mContext);
            }
        /*}else{
        dismissProgressDialog();
        AppUtils.showError(mContext,"You are not connected to any working network.");
        }*/
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        if(BaseActivity.baseDialog!=null) {
            if(BaseActivity.baseDialog.isShowing())
                BaseActivity.baseDialog.dismiss();
        }
    }
    private void openPrinterNotConnectedDialog() {
        dismissProgressDialog();
        if (!mPrintData.isselfCheckinbool) {
            ((BaseActivity)mContext).msgDialog.close();
            ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Util.setCustomAlertDialog(mContext);
                    Util.openCustomDialog("Alert", "No printer found. Do you want to reprint.");
                    Util.txt_okey.setText("REPRINT");
                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            //ShowTicketsDialog();
                            doPrint();
                            Util.alert_dialog.dismiss();
                        }
                    });

                    Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            mPrintData.frame_transparentbadge
                                    .setVisibility(View.INVISIBLE);
                            Util.alert_dialog.dismiss();
                            if(mContext instanceof OrderSucessPrintActivity){
                               /* Intent i = new Intent(mContext, ManageTicketActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                mContext.startActivity(i);*/
                                ((BaseActivity) mContext).finish();
                            }
                        }
                    });

                }
            });
        }else{
            ((BaseActivity)mContext).msgDialog.close();
            ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String msg="";
                    if (PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                        msg = "Printer is not connected.Please contact event Organizer!";
                    } else {
                        msg = "Printer is disconnected.Please contact event Organizer!";
                    }
                    showSingleButtonDialog("Alert", msg);
                }
            });

        }
    }

    private void init() {
        dismissProgressDialog();
        inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                + mPrintData.checked_in_eventId + "' AND " + DBFeilds.BADGE_NEW_ID
                + " = '" + BaseActivity.checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
        badge_res = Util.db.getAllBadges(where_att);
        if(badge_res.size()==0){
            requestType=Util.LOAD_BADGE;
            doRequest();
        }else if(badge_res.size()==1) {
            startPrintTask();
        }else{
            BaseActivity.showSingleButtonDialog("Alert",
                    "No Badge Selected, Please contact your Event Organizer!",mContext);
        }
    }

    private void startPrintTask() {
        try {
            payment_cursor = Util.db.getAttendeeDetailsForPrint(getPaymentClause());
            payment_cursor.moveToFirst();
            //if (!PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
        /*else if(isOnline1()){
requestType = Util.GET_BADGE_ID;
doRequest();
}*/
            if (!BaseActivity.isValidate_badge_reg_settings) {
                if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty()) {
                    printClicked();
                } else {
                    new CheckNetConnection().execute();
                }
            } else if (BaseActivity.isValidate_badge_reg_settings) {
                if (!payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty() &&
                        !payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PRINTSTATUS)).equalsIgnoreCase("Printed")) {
                    printClicked();
                } else {
                    new CheckNetConnection().execute();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            if (!BaseActivity.isValidate_badge_reg_settings) {
                printClicked();
            }else {
                new CheckNetConnection().execute();
            }
        }
    }

       /* requestType = Util.GET_BADGE_ID;
        doRequest();*/
        /*} else {
            showSingleButtonDialog("Alert", "Printer is not connected please contact event Organizer!");
        }*/

    public class CheckNetConnection extends android.os.AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*BaseActivity.baseDialog.setMessage("Checking Connection, please wait...");
            BaseActivity.baseDialog.show();*/
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isNetAccess();
        }

        @Override
        protected void onPostExecute(Boolean response) {
            super.onPostExecute(response);
            dismissProgressDialog();
            if(response){
                //if(payment_cursor.getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGEID)).isEmpty())
                requestType = Util.GET_BADGE_ID;
                doRequest();
            }else{
                AppUtils.showError(mContext,"You are not connected to any working network.");
            }

        }
    }

    public boolean isNetAccess(){
        if(Util.isInternetComing){
            return true;
        }else{
            return false;
        }
    }
    private String getPaymentClause(){
        return  " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
                + mPrintData.checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID
                + " = " + "'" + mPrintData.attendeeId + "'" + " AND "
                + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + mPrintData.order_id + "'";
    }

    private void doRequest() {
        String access_token = mPrintData.sfdcddetails.token_type + " " + mPrintData.sfdcddetails.access_token;
        if(requestType.equalsIgnoreCase(Util.GET_BADGE_ID)){
            httpPostData = new HttpPostData("Getting Badge Id...", setBadgeIdUrl(), setPrintBadgeBody().toString(), access_token);
            httpPostData.execute();
        }else if(requestType.equalsIgnoreCase(Util.LOAD_BADGE)){
            String _url = mPrintData.sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE_NEW + "Event_Id="+ mPrintData.checked_in_eventId;
            httpPostData = new HttpPostData("Loading Badges...", _url, null, access_token);
            httpPostData.execute();
        }
    }

    private void parseJsonResponse(String response) {
        try {
            if(!((BaseActivity)mContext).isValidResponse(response)){
                ((BaseActivity)mContext).openSessionExpireAlert(((BaseActivity)mContext).errorMessage(response));
            }
            if (requestType.equals(Util.GET_BADGE_ID)) {
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
                            payment_cursor = Util.db.getAttendeeDetailsForPrint(getPaymentClause());
                            payment_cursor.moveToFirst();
                            printClicked();
                        } else {
                            ((BaseActivity) mContext).startErrorAnimation(mContext.getResources().getString(
                                    R.string.network_error1), ((BaseActivity)mContext).txt_error_msg);
                        }
                    }
                } else {
                    ((BaseActivity) mContext).startErrorAnimation("Error in network", ((BaseActivity)mContext).txt_error_msg);
                }
            }else if(requestType.equals(Util.LOAD_BADGE)){

                Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
                List<BadgeResponseNew> badges =  new Gson().fromJson(response, listType);
                AppUtils.displayLog("---------------- parseJsonResponse Badge Size----------", ":"+BaseActivity.checkedin_event_record.Events.Mobile_Default_Badge__c+" : " + response);
                Util.db.deleteBadges(mPrintData.checked_in_eventId);
                ((BaseActivity)mContext).sharedPreferences.edit().clear().commit(); //TODO
                for(BadgeResponseNew badge : badges){
                    badge.badge.event_id = mPrintData.checked_in_eventId;
                    Util.db.InsertAndUpdateBadgeTemplateNew(badge);
                }
                if(badge_res.size()==1) {
                    startPrintTask();
                }else{
                    BaseActivity.showSingleButtonDialog("Alert",
                            "No Badge Selected, Please contact your Event Organizer!",mContext);

                }
                //startPrintTask();
            }
        }catch (Exception e){
            e.printStackTrace();
            AppUtils.showError(mContext,"Something went wrong");
        }
    }

    private String setBadgeIdUrl() {
        return mPrintData.sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT;
    }
    private JSONArray setPrintBadgeBody() {
        /*String where_att = " Where EventID = '" + mPrintData.checked_in_eventId
                + "' AND isBadgeSelected = 'Yes'";
        Cursor updated_badge1 = Util.db.getBadgeTemplate(where_att);
        updated_badge1.moveToFirst();*/
        JSONArray badgearray = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("TicketId", AppUtils.NullChecker(payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_ID))));
            // obj.put("BadgeLabel",
            // updated_badge1.getString(updated_badge1.getColumnIndex(DBFeilds.BADGE_BADGE_NAME)));
            obj.put("BadgeLabel", AppUtils.NullChecker(payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
            //obj.put("Reason", "");
            obj.put("Reason", Util.NullChecker(mPrintData.reason));
            obj.put("devicenm",Util.getDeviceNameandAppVersion());
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                obj.put("screenmode", "self checkin");
            else obj.put("screenmode", "attendee mobile");
            obj.put("printernm",Util.NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
            obj.put("printtime",Util.getCurrentDateTimeInGMT());
            badgearray.put(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return badgearray;
    }


    public void printClicked() {
        try {
            // inputMethodManager.hideSoftInputFromWindow(((Activity)mContext).getWindow().getDecorView().getWindowToken(), 0);
            String ev= mPrintData.checked_in_eventId;
            AppUtils.displayLog("-- sai   -----",mPrintData.checked_in_eventId);
            String whereClause = " where " + DBFeilds.ATTENDEE_EVENT_ID + " = '"
                    + mPrintData.checked_in_eventId + "'" + " AND " + DBFeilds.ATTENDEE_ID + " = "
                    + "'" + mPrintData.attendeeId + "'" + " AND "
                    + DBFeilds.ATTENDEE_ORDER_ID + " = " + "'" + mPrintData.order_id + "'";
            payment_cursor = Util.db.getAttendeeDetailsForPrint(getPaymentClause());
            payment_cursor.moveToFirst();
            SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor=payment_cursor;
            mPrintData.print_badge.setVisibility(View.INVISIBLE);
            String where_att = " Where " + DBFeilds.BADGE_NEW_EVENT_ID + " = '"
                    + mPrintData.checked_in_eventId + "' AND " + DBFeilds.BADGE_NEW_ID
                    + " = '" + BaseActivity.checkedin_event_record.Events.Mobile_Default_Badge__c + "'";
            badge_res = Util.db.getAllBadges(where_att);

            WindowManager manager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();

            int width = display.getWidth();// point.x;
            int height = display.getHeight();
            BadgeCreation  badge_crator=new BadgeCreation(mContext,width,height);
            badge_crator.createBadgeTemplate(badge_res.get(0), mPrintData.print_badge, payment_cursor,true);

           /* qrcode_name = Util.NullChecker(payment_cursor.getString(payment_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)))
                    + Util.NullChecker(payment_cursor
                    .getString(payment_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));


            AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name);*/
            //button_layout.setVisibility(View.GONE);
            //btn_cancel.setVisibility(View.GONE);
            //event_layout.setVisibility(View.GONE);
            if(mPrintData.frame_transparentbadge!=null)
                mPrintData.frame_transparentbadge.setVisibility(View.VISIBLE);
            mPrintData.print_badge.setDrawingCacheEnabled(true);
            mPrintData.print_badge.buildDrawingCache(true);
            mPrintData.print_badge.buildDrawingCache();
            AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name);
            if (AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name))
                mListViewDidLoadHanlder.sendEmptyMessage(0);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Handler mListViewDidLoadHanlder = new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    doPrint();
                    return false;
                }

            });

    public void doPrint() {
        try {
            /*if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BaseActivity.zebraPrinter.doZebraPrint(mContext, mPrintData.print_badge);
                    }
                }).start();

            }*/ if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mFiles.clear();
                        mPrintData.print_badge.setDrawingCacheEnabled(true);
                        mPrintData.print_badge.buildDrawingCache(true);
                        mPrintData.print_badge.buildDrawingCache();
                        AttendeeDetailActivity.saveBitmap(loadBitmapFromView(mPrintData.print_badge), qrcode_name);
                        File root = Environment.getExternalStorageDirectory();
                        File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
                        String file_path = dir.toString() + "/" + qrcode_name + ".png";
                        mFiles.add(file_path);
                        BaseActivity.zebraPrinter.doZebraPrint(mContext, mFiles);
                    }
                }).start();

            }else if (PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother")) {
                mPrintData.print_badge.setDrawingCacheEnabled(true);
                mPrintData.print_badge.buildDrawingCache(true);
                mPrintData.print_badge.buildDrawingCache();
                /*payment_cursor=null;
                payment_cursor = Util.db.getAttendeeDetailsForPrint(getPaymentClause());
                payment_cursor.moveToFirst();*/
                qrcode_name = Util.NullChecker(payment_cursor.getString(payment_cursor
                        .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)))
                        + Util.NullChecker(payment_cursor
                        .getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name);
                // setDialog();
                if (!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    setDialog();
                    new SearchThread().start();
                } else {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    ((BaseActivity) mContext).myPrint.setBluetoothAdapter(bluetoothAdapter);
                    NetPrinter printer = new NetPrinter();
                    printer.modelName = PrinterDetails.selectedPrinterPrefrences.getString("printer", "");
                    printer.serNo = "";
                    printer.ipAddress = "";
                    printer.macAddress = PrinterDetails.selectedPrinterPrefrences.getString("macAddress", "");
                    setPrefereces(printer);
                    printBadge();
                }
            } else if(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Cloud Print")&&BaseActivity.isCloudPrintingON) {
                mPrintData.print_badge.setDrawingCacheEnabled(true);
                mPrintData.print_badge.buildDrawingCache();
                qrcode_name = Util.NullChecker(payment_cursor.getString(payment_cursor
                        .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)))
                        + Util.NullChecker(payment_cursor
                        .getString(payment_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                AttendeeDetailActivity.saveBitmap(loadBitmapFromView(mPrintData.print_badge), qrcode_name);
                if (PrinterDetails.selectedPrinterPrefrences.getBoolean(ZebraPrinter.FIT_TO_PAPER, false)){
                    mFiles.clear();
                    File root = android.os.Environment.getExternalStorageDirectory();
                    File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
                    String file_path = dir.toString() + "/" + qrcode_name + ".png";
                    mFiles.add(file_path);
                    Util.createPDF(mFiles, true);
                    ZebraPrinter.googleCloudPrint(mContext, "");
                }else {
                    ZebraPrinter.googleCloudPrintImage(mContext, qrcode_name);
                }
                //}
            }else {
                mPrintData.print_badge.setDrawingCacheEnabled(true);
                mPrintData.print_badge.buildDrawingCache(true);
                mPrintData.print_badge.buildDrawingCache();
                AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name);

                if (!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP, "").isEmpty()) {
                    setDialog();
                    new SearchThread().start();
                } else {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    ((BaseActivity) mContext).myPrint.setBluetoothAdapter(bluetoothAdapter);
                    NetPrinter printer = new NetPrinter();
                    printer.modelName = PrinterDetails.selectedPrinterPrefrences.getString("printer", "");
                    printer.serNo = "";
                    printer.ipAddress = "";
                    printer.macAddress = PrinterDetails.selectedPrinterPrefrences.getString("macAddress", "");
                    setPrefereces(printer);
                    printBadge();
                }
                //                AttendeeDetailActivity.openprinterNotConnectedDialog(mContext);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setDialog() {
        try {
            ((BaseActivity)mContext).msgDialog.showMsgNoButton(
                    mContext.getString(R.string.netPrinterListTitle_label),
                    mContext.getString(R.string.search_printer));
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
                    ((BaseActivity)mContext).isPrinter = true;
                    ((BaseActivity)mContext).msgDialog.close();
                    mPrintData.print_badge.setDrawingCacheEnabled(true);
                    mPrintData.print_badge.buildDrawingCache();
                    qrcode_name = Util.NullChecker(payment_cursor.getString(payment_cursor
                            .getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME)))
                            + Util.NullChecker(payment_cursor
                            .getString(payment_cursor
                                    .getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));
                    AttendeeDetailActivity.saveBitmap(mPrintData.print_badge.getDrawingCache(), qrcode_name);
                    printBadge();
                } else {
                    if (!mPrintData.isselfCheckinbool) {
                        ((BaseActivity)mContext).msgDialog.close();
                        ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Util.setCustomAlertDialog(mContext);
                                Util.openCustomDialog("Alert", "No printer found. Do you want to reprint.");
                                Util.txt_okey.setText("REPRINT");
                                Util.txt_dismiss.setVisibility(View.VISIBLE);
                                Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        //ShowTicketsDialog();
                                        doPrint();
                                        Util.alert_dialog.dismiss();
                                    }
                                });

                                Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {
                                        mPrintData.frame_transparentbadge
                                                .setVisibility(View.INVISIBLE);
                                        Util.alert_dialog.dismiss();
                                        if(mContext instanceof OrderSucessPrintActivity){
                                           /* Intent i = new Intent(mContext, ManageTicketActivity.class);
                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            mContext.startActivity(i);*/
                                            ((BaseActivity) mContext).finish();
                                        }
                                    }
                                });

                            }
                        });
                    }else{
                        ((BaseActivity)mContext).msgDialog.close();
                        ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String msg="";
                                if (PrinterDetails.selectedPrinterPrefrences.getString("printer", "").isEmpty()) {
                                    msg = "Printer is not connected.Please contact event Organizer!";
                                } else {
                                    msg = "Printer is disconnected.Please contact event Organizer!";
                                }
                                showSingleButtonDialog("Alert", msg);
                            }
                        });

                    }
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
            if (((BaseActivity)mContext).mItems != null) {
                ((BaseActivity)mContext).mItems.clear();
            }
            // get net printers of the particular model
            ((BaseActivity)mContext).mItems = new ArrayList<String>();
            Printer myPrinter = new Printer();
            //AppUtils.displayLog("-AttendeeDetail Printer Model Name-",":"+PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL-720NW").replace("_","-"));
            String printerIP=PrinterDetails.selectedPrinterPrefrences.getString("address","");
            ((BaseActivity)mContext).mNetPrinter = myPrinter.getNetPrinters(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW").replace("_","-"));
            final int netPrinterCount = ((BaseActivity)mContext).mNetPrinter.length;

            // when find printers,set the printers' information to the list.
            if (netPrinterCount > 0) {
                searchEnd = true;
                for(NetPrinter printer:((BaseActivity)mContext).mNetPrinter){
                    if(printerIP.equals(printer.ipAddress))
                        setPrefereces(printer);
                }
            } else if (netPrinterCount == 0
                    && times == (Common.SEARCH_TIMES - 1)) { // when no printer
                // is found
                String dispBuff[] = new String[1];
                dispBuff[0] = mContext.getString(R.string.noNetDevice);
                ((BaseActivity)mContext).mItems.add(dispBuff[0]);
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
            if (((BaseActivity)mContext).sharedPreferences.getString("printerModel", "").equals("")) {
                SharedPreferences.Editor editor = ((BaseActivity)mContext).sharedPreferences.edit();
                editor.putString("printerModel", PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.PRINTERMODEL,"QL_720NW"));
                editor.putString("port", "NET");
                editor.putString("address", printerInfo.ipAddress.toString());
                editor.putString("macAddress", printerInfo.macAddress.toString());
                editor.putString("address", mNetPrinter.ipAddress);
                editor.putString("macAddress", mNetPrinter.macAddress);
                editor.putString("printer", mNetPrinter.modelName);
                editor.putString("paperSize", "W62H100");
                editor.putString("serNo", mNetPrinter.serNo);
                if(badge_res.size()>0){
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
                editor.putString("customSetting", ((BaseActivity)mContext).sharedPreferences.getString("customSetting", ""));
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

    public void printBadge() {
        try {
            //Log.i("-----------------Badge Paper Size while printing-----------",":"+sharedPreferences.getString("paperSize", ""));
            mFiles.clear();
            File root = android.os.Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + "/ScanAttendee/Badges");
            String file_path = dir.toString() + "/" + qrcode_name + ".png";
            // File dir = new File();
            // String imageUri = "drawable://" + R.drawable.country_logo;
            //Log.i("Attendee Detail", "Image Path=" + file_path);
            mFiles.add(file_path);
            //Log.i("Attendee Detail", "Files=" + mFiles.get(0));
            // mFiles.add(file_path);
            if(ext_settings==null) {
                ext_settings = new ExternalSettings();
                if (!Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, "").isEmpty()) {
                    ext_settings = new Gson().fromJson(Util.external_setting_pref.getString(BaseActivity.user_profile.Userid + BaseActivity.checkedin_event_record.Events.Id, ""), ExternalSettings.class);
                }
            }
            if(ext_settings.doubleSide_badge) {
                ((BaseActivity)mContext).sharedPreferences.edit().putString("autoCut","false").commit();
                ((BaseActivity)mContext).sharedPreferences.edit().putString("endCut","true").commit();
                ((BaseActivity)mContext).sharedPreferences.edit().putString("numberOfCopies", "2").commit();
            }else{
                ((BaseActivity)mContext).sharedPreferences.edit().putString("autoCut","true").commit();
                ((BaseActivity)mContext).sharedPreferences.edit().putString("endCut","").commit();
                ((BaseActivity)mContext).sharedPreferences.edit().putString("numberOfCopies", "1").commit();
            }
            ((ImagePrint) ((BaseActivity)mContext).myPrint).setFiles(mFiles);
            ((BaseActivity)mContext).myPrint.print();
            //frame_transparentbadge.setVisibility(View.INVISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showSingleButtonDialog(String title,String message) {
        Util.setCustomAlertDialog(mContext);
        Util.openCustomDialog(title,message);
        Util.txt_okey.setText("Ok");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.alert_dialog.setCancelable(false);
        Util.txt_okey.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mPrintData.frame_transparentbadge
                        .setVisibility(View.INVISIBLE);
                Util.alert_dialog.dismiss();
                ((BaseActivity) mContext).finish();
            }
        });
    }
    public class HttpPostData extends SafeAsyncTask<String> {

        private String _url;
        private String _values, _access_token, _message;
        private int HTTP_OK = 200;
        private ProgressDialog _dialog;
        private AlertDialogCustom alert_dialog;

        public HttpPostData(String message, String _url, String values, String access_token) {
            this._url = _url;
            this._values = values;
            this._access_token = access_token;
            this._message = message;
            this._dialog = new ProgressDialog(mContext);
            this.alert_dialog = new AlertDialogCustom((Activity) mContext);
            _dialog.setCancelable(false);

            //this._dialog.setIndeterminateDrawable(_baseactivity.getDrawable(R.drawable.image_for_rotation));
        }

        @Override
        protected void onPreExecute() throws Exception {
            super.onPreExecute();
            if (_dialog != null && !_dialog.isShowing()) {
                if (_message.isEmpty()) {
                    _message = "Please wait...";
                }
                _dialog.setMessage(_message);

                if (!_message.equalsIgnoreCase("Hide")) {
                    _dialog.show();
                }

            }
        }

        @Override
        public String call() throws Exception {
            // TODO Auto-generated method stub
            String response = executeRequest();
            return response;
        }

        protected void onSuccess(String result) throws Exception {
            super.onSuccess(result);

            _dialog.dismiss();
            if (result != null) {
                if (!isValidResponse(result)) {
                    parseJsonResponse(result);
                }else {
                    parseJsonResponse(result);
                }
            } else {
                if(mContext.getClass().getSimpleName().equals("BarCodeScanActivity")){
                    ((BarCodeScanActivity)mContext).scanStart();
                }
			/*Intent i = new Intent(_baseactivity,LoginActivity.class);
			alert_dialog.setParamenters("Error", errorMessage(result), i, null, 1, true);
			alert_dialog.setAlertImage(R.drawable.error,"error");*/
                if (!_url.contains(WebServiceUrls.SA_DEVICE_SESSION) && !_url.contains(WebServiceUrls.SA_EVENT_DASHBOARD) && !_url.contains(WebServiceUrls.SA_TICKETS_SCAN_URL) && !_url.contains(WebServiceUrls.SA_SCAN_TICKET)) {
                    alert_dialog.show();
                }


            }

        }

        private String executeRequest() {
            String response = null;
            try {
                HttpClient client = HttpClientClass.getHttpClient(30000);
                HttpPost postMethod = new HttpPost(_url);

                if (_url.contains("api.stripe.com") || _url.contains("vault.trustcommerce.com") || _url.contains("connect.stripe.com")) {
                    postMethod.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
                }
                if (_access_token != null) {
                    postMethod.addHeader("Authorization", _access_token);
                }
                //postMethod.addHeader("Content-Type", "application/x-www-form-u rlencoded");

                if (_values != null) {
                    postMethod.setEntity(new StringEntity(_values, "UTF-8"));
                }
                AppUtils.displayLog("--------------Url--------------", ":" + _url.toString());
                AppUtils.displayLog("--------------Values--------------", ":" + _values);
                AppUtils.displayLog("------------- Access_token--------------", ":" + _access_token);
			/*Log.i("--------------Url--------------", ":" + _url.toString());
			Log.i("--------------Values--------------", ":" + _values);
			Log.i("------------- Access_token--------------", ":"+ _access_token);*/

                HttpResponse http_response = client.execute(postMethod);
                int res_code = http_response.getStatusLine().getStatusCode();
                AppUtils.displayLog("------------- Response Code--------------", ":" + res_code);
                //Log.i("------------- Response Code--------------", ":"+ res_code);
                if (res_code == HTTP_OK) {
                    response = EntityUtils.toString(http_response.getEntity());
                } else {
                    response = EntityUtils.toString(http_response.getEntity());
                    alert_dialog.setParamenters("Error!", "Sorry! server has encountered an error.Please try again.", null, null, 1, false);
                    alert_dialog.setAlertImage(com.globalnest.scanattendee.R.drawable.error, "error");
                }
                AppUtils.displayLog("------------- Response--------------", ":" + response);
                //Log.i("------------- Response--------------", ":"+ response);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                alert_dialog.setParamenters("Error", "Server is not reachable. Please check your internet connection.", null, null, 1, false);
                alert_dialog.setAlertImage(com.globalnest.scanattendee.R.drawable.error, "error");
                //response = String.valueOf(res_code);
                AppUtils.displayLog("--------------IOException----", ":" + e.getMessage());

            }
            return response;
        }

        public boolean isValidResponse(String response) {
            try {
                Object obj = new JSONTokener(response).nextValue();

                if (obj instanceof JSONObject) {
                    JSONObject json_obj = new JSONObject(response);
                    if (json_obj.has("error")) {
                        return false;
                    }

                } else if (obj instanceof JSONArray) {
                    JSONArray json_array = new JSONArray(response);
                    if (json_array.length() > 0) {
                        if (json_array.optJSONObject(0).has("errorCode")) {
                            return false;
                        }
                    }

                }


            } catch (Exception e) {
                // TODO: handle exception
            }
            return true;
        }
    }
    public class SearchPrinterStatusThread extends Thread {
        /* search for the printer for 10 times until printer has been found. */
        @Override
        public void run() {
            try {
                // search for net printer.
                if (netPrinterList(5)) {
                    init();
                } else {
                    openPrinterNotConnectedDialog();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public  boolean isprinterconnected(){
        try {
            BaseActivity.zebraPrinter = new ZebraPrinter();
            if(AppUtils.isOnline(mContext) && !PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_IP,"").isEmpty()) {
                BaseActivity.zebraPrinter.createTCPConnection();
                if (BaseActivity.zebraPrinter.getTCPConnection().isConnected()) {
                    return true;
                }
            }else if(!PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.ZEBRA_WIFI_MAC, "").isEmpty()) {
                BaseActivity.zebraPrinter.createBTConnection();
                if (BaseActivity.zebraPrinter.getBTConnection().isConnected()) {
                    return true;
                }
            }
            return false;
        }catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private  class IsPrinterConnectTask extends AsyncTask<Void,Void,Void> {
        private boolean isPrinterConnectedStatus=false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*BaseActivity.baseDialog.setMessage("Please wait...");
            BaseActivity.baseDialog.show();*/
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
        protected void onPostExecute(Void aVoid){
            super.onPostExecute(aVoid);
            dismissProgressDialog();
            if(isPrinterConnectedStatus){
                PrintAndCheckin.this.init();
            }else{
                ((BaseActivity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openPrinterNotConnectedDialog();
                    }
                });
            }
        }
    }
}
