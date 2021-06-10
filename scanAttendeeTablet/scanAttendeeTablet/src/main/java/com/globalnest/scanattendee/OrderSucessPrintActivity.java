package com.globalnest.scanattendee;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

/**
 * Created by sailakshmi on 10-11-2017.
 */

public class OrderSucessPrintActivity extends BaseActivity{
    private String data="",requestType="";
    String orderId,attendeeID;
    Cursor attendee_cursor;
    PrintAndCheckin printT;
    PrintDetails printDetails;
    private FrameLayout linear_badge_parent,print_badge,frame_transparentbadge;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setCustomContentView(R.layout.ordersucessprint_layout);
            data=getIntent().getStringExtra(Util.ORDER_ID);
            //data=getIntent();
           /* if(cloudprint){
                attendee_cursor = Util.db.getBadgeableTicketOrderDetails(data);
                attendeeID=attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
                if (isOnline()) {
                    requestType = Util.GET_BADGE_ID;
                    doRequest();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
                        }
                    });
                }
            }
            else {*/
                DoAttendeePrint(data);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void DoAttendeePrint(String orderid) {
        try{
            orderId = orderid;
            attendee_cursor=Util.db.getAttendeeDataCursorForScan("where Event_Id = '"+ checked_in_eventId+"' AND Order_Id ='"+orderid+"' AND "+DBFeilds.ATTENDEE_BADGABLE + " = 'B - Badge' AND "+DBFeilds.ATTENDEE_BADGE_PARENT_ID + " = '' ");
           if(attendee_cursor!=null&&attendee_cursor.getCount()>0){
               doBadgePrint(attendee_cursor);
           }else {
               attendee_cursor = Util.db.getBadgeableTicketOrderDetails(orderId);
               doBadgePrint(attendee_cursor);
           }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        setContentView(layout);
        linear_badge_parent = (FrameLayout)findViewById(R.id.linear_badge_parent);
        print_badge = (FrameLayout) findViewById(R.id.frame_attdetailqrcodebadge);
        frame_transparentbadge = (FrameLayout) findViewById(R.id.frame_transparentbadge);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            //Log.i("------------On Activity Result-----------",":"+quick_scan);
            if(requestCode==445){

              /*  Intent i = new Intent(BarCodeScanActivity.this, SplashActivity.class);
                startActivity(i);
                finish();*/
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doRequest() {
        String access_token = sfdcddetails.token_type + " "
                + sfdcddetails.access_token;
        if (requestType.equals(Util.GET_BADGE_ID)) {

            postMethod = new HttpPostData("Getting Badge Id...",
                    sfdcddetails.instance_url + WebServiceUrls.SA_BADGE_PRINT, setPrintBadgeBody().toString(),
                    access_token, OrderSucessPrintActivity.this);
            postMethod.execute();

        }
    }
    private JSONArray setPrintBadgeBody() {
        JSONArray badgearray = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            obj.put("TicketId", attendee_cursor.getString(attendee_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_ID)));
             obj.put("BadgeLabel", NullChecker(attendee_cursor.getString(attendee_cursor
                    .getColumnIndex(DBFeilds.ATTENDEE_BADGE_LABLE))));
            obj.put("Reason", "");
            obj.put("devicenm",Util.getDeviceNameandAppVersion());
            if(Util.getselfcheckinbools(Util.ISSELFCHECKIN))
                obj.put("screenmode", "self checkin");
            else obj.put("screenmode", "attendee mobile");
            obj.put("printernm",NullChecker(PrinterDetails.selectedPrinterPrefrences.getString("printer", "")));
            obj.put("printtime",Util.getCurrentDateTimeInGMT());
            badgearray.put(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return badgearray;
    }

    @Override
    public void parseJsonResponse(String response) {
    }

    @Override
    public void insertDB() {

    }
    public void doBadgePrint(Cursor cursor){
        try{
            if (BaseActivity.isOrderScanned(orderId)) {
                //attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
                attendee_cursor=cursor;
            }
            //for (int i = 0; i < attendee_cursor.getCount(); i++) {
            //attendee_cursor.moveToPosition(i);
            String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
            if (NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
                //FrameLayout print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
                //FrameLayout frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);
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
                printDetails.attendeeWhereClause = "";
                printDetails.isOrderScaneed = false;
                //SelfCheckinAttendeeDetailActivity.selfcheckin_attendee_cursor=attendee_cursor;

                if (isBadgeSelected()) {
                    try {
                        printT.doSaveAndPrint(this, printDetails);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    BaseActivity.showSingleButtonDialog("Alert",
                            "No Badge Selected, Please contact your Event Organizer!", this);
                }
                //break;
            } else {
                showCustomToast(this, "Order ID contains NON-Badgeable ticket",
                        R.drawable.img_like, R.drawable.toast_redrounded, false);
            }
//			}
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
