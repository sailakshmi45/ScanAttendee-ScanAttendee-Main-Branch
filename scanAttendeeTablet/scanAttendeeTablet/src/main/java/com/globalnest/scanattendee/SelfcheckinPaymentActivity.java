//  ScanAttendee Android
//  Created by Ajay on Aug 25, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.globalnest.classes.EditCard;
import com.globalnest.classes.PaymentConstants;
import com.globalnest.database.DBFeilds;
import com.globalnest.database.EventPayamentSettings;
import com.globalnest.mvc.BuyerInfoHandler;
import com.globalnest.mvc.ItemTypeController;
import com.globalnest.mvc.OrderDetailsHandler;
import com.globalnest.mvc.OrderItemListHandler;
import com.globalnest.mvc.PaytmStatusCheckGson;
import com.globalnest.mvc.PaytmStatusCheckResponseGson;
import com.globalnest.mvc.PrintDetails;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.PaymentType;
import com.globalnest.payments.AuthNetTask;
import com.globalnest.payments.PaypalDirectTask;
import com.globalnest.payments.TrustCommerceWebView;
import com.globalnest.printer.PrinterDetails;
import com.globalnest.printer.ZebraPrinter;
import com.globalnest.retrofit.rest.ApiClient;
import com.globalnest.retrofit.rest.ApiInterface;
import com.globalnest.stripe.android.Stripe;
import com.globalnest.stripe.android.TokenCallback;
import com.globalnest.stripe.android.model.Card;
import com.globalnest.unimug.UniMugTransparentActivity;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.stripe.exception.APIConnectionException;
import com.stripe.exception.APIException;
import com.stripe.exception.AuthenticationException;
import com.stripe.exception.CardException;
import com.stripe.exception.InvalidRequestException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.net.RequestOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import IDTech.MSR.uniMag.uniMagReader;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import retrofit2.Call;
import retrofit2.Response;
import us.fatehi.magnetictrack.bankcard.BankCardMagneticTrack;


/**
 * @author Sailakshmi
 *
 */
public class SelfcheckinPaymentActivity extends BaseActivity {

	/*static {
		System.loadLibrary("iconv");
	}*/

    private String format_string = "", cardNo = "", card_exp_months = "MM",
            card_exp_years = "YYYY", cvcNo = "",brand="";
    private String[] exp_month = { "MM", "01", "02", "03", "04", "05", "06", "07",
            "08", "09", "10", "11", "12" };
    private String[] exp_years = { "YYYY",
            "2020", "2021", "2022", "2023", "2024", "2025", "2026",
            "2027", "2028", "2029", "2030", "2031", "2032", "2033", "2035",
            "2035", "2036", "2037", "2038", "2039", "2040" ,"2041"};
    private BuyerInfoHandler buyer_info;
    public String BuyerAttendeeImage="";
    public boolean buyerimage=false;
    private String attendeePhoneForCitrus = "";
    private ArrayList<OrderItemListHandler> order_line_items;
    private ArrayList<ItemTypeController> items_list;
    private double total = 0.00, service_tax = 0.00,servicefee=0.00;
    private double stripe_total=0.0,stripe_servicefee=0.0;
    private Cursor _event_payment_setting;
    private Cursor _event_card_pgateway;
    private List<PaymentType> _event_payment_keys = new ArrayList<PaymentType>();
    private ArrayList<Integer> payment_option_layouts = new ArrayList<Integer>();
    private AlertDialogCustom dialog;
    private ArrayList<String> TITLES = new ArrayList<String>();
    private ArrayList<String> VIEWNAMES = new ArrayList<String>();
    private ViewPager view_pages;
    private PagerSlidingTabStrip tabs;
    private MyPagerAdapter adapter;
    private HashMap<String, String> params = new HashMap<String, String>();
    private String request_type = "",transaction_id = "",card_last_4="", check_number = "",order_status="",order_id="",payment_gateway_name="",order_name="";
    private String custumer_id ="", pay_id = "",requestType="";
    private int pay_errorcode;
    private Card card;
    private Animation shake,right_slide_in;
    private int keyDel = 0;
    public static final int PAYPAL_BUTTON_ID = 10001;
    private TotalOrderListHandler totalorderlisthandler;
    TextView txt_payable, txt_grand_total, txt_payment_mode, txt_paypal_error,
            txt_summaryamount, txt_summaryfee,txt_summarytax, txt_summarytotal;
    private PaymentType stripekeys_evetndex;
    private double entered_cash_amount = 0;
    private String paytmTxnAmount="",scanCardNumber="",scanCCV="";
    private int scanExpMonth=0,scanExpYear=0;
    PrintAndCheckin printT;
    PrintDetails printDetails;
    Cursor attendee_cursor;
    public static final int REQUEST_SCAN = 1110;
    public uniMagReader myUniMagReader = null;
    public static boolean isUnimagReaderConnected=false;
    public static boolean isReaderConnected=false;
    UniMugTransparentActivity umimugObj ;
    private boolean issingleticket=false;
    private boolean isCardSwiped=false;
    ProgressDialog progressDialog;

	/*private class TabController{
		String title="";
		int icon=0;
	}*/

    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.ordersummery_new);
        order_id = this.getIntent().getStringExtra("ORDERID");
        order_name = this.getIntent().getStringExtra("ORDERNAME");
        total = this.getIntent().getDoubleExtra(Util.TOTAL, 0.00);
        servicefee = this.getIntent().getDoubleExtra(Util.SERVICE_FEE, 0.00);
        service_tax = this.getIntent().getDoubleExtra(Util.SERVICE_TAX, 0.00);
        buyerimage=this.getIntent().getExtras().getBoolean("FirstAttendeeImage");
        if(buyerimage){
            BuyerAttendeeImage=CollectOrderInfo.BuyerAttendeeImage;
        }
        issingleticket =this.getIntent().getBooleanExtra(Util.ONLY1FORM,false);
        buyer_info = (BuyerInfoHandler) this.getIntent().getSerializableExtra(Util.INTENT_KEY_1);
        //order_line_items = (ArrayList<OrderItemListHandler>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_2);
        //items_list = (ArrayList<ItemTypeController>) this.getIntent().getSerializableExtra(Util.INTENT_KEY_3);
        fillOrderLineItems();

        _event_payment_setting = Util.db.getEvent_Payment_Setting(checked_in_eventId);
        _event_card_pgateway = Util.db.getEvent_Card_PGateway(checked_in_eventId);
        dialog = new AlertDialogCustom(SelfcheckinPaymentActivity.this);

        shake = AnimationUtils.loadAnimation(SelfcheckinPaymentActivity.this,
                R.anim.shake);
        right_slide_in = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.pull_out_from_left);

        right_slide_in = AnimationUtils.loadAnimation(this,
                R.anim.pull_from_right);
        right_slide_in.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation arg0) {

            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {

            }
        });
        if(total>0)
            setUI();
        //doRequest();

        back_layout.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                openWaringAlert();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.onResume();
        try {
            if (myUniMagReader == null) {
                umimugObj = new UniMugTransparentActivity();
                umimugObj.onCreate(this, false);
            }
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            getApplicationContext().registerReceiver(mReceiver, filter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        umimugObj.onDestroy();
    }
    private void openWaringAlert() {
        Util.setCustomAlertDialog(SelfcheckinPaymentActivity.this);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_dismiss.setText("CANCEL");
        Util.txt_okey.setText("DISCARD");
        Util.alert_dialog.setCancelable(false);

        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                umimugObj.onDestroy();if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    Intent startentent = new Intent(SelfcheckinPaymentActivity.this,SelfCheckinAttendeeList.class);
                    startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startentent);
                    finish();
                }else {
                    Intent startentent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
                    startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startentent);
                    finish();
                }
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                umimugObj.onDestroy();
            }
        });
        Util.openCustomDialog("Alert","Do you want to discard the order?");
    }

    /* (non-Javadoc)
     * @see com.globalnest.network.IPostResponse#doRequest()
     */
    @Override
    public void doRequest() {
        if(isOnline()){
            // TODO Auto-generated method stub
            String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
            if(order_id.isEmpty()){
                callSellTickets();
               /* String url = sfdcddetails.instance_url + WebServiceUrls.SA_SELL_TICKET+ "eveid=" + checked_in_eventId + "&userid=" + sfdcddetails.user_id;
                postMethod = new HttpPostData("Processing your order...",url,setJsonBody().toString(), access_token, SelfcheckinPaymentActivity.this);
                postMethod.execute();*/
            }else if (requestType.equalsIgnoreCase(WebServiceUrls.SA_PAYMENT_UPDATE)) {
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_PAYMENT_UPDATE;
                postMethod = new HttpPostData("Please wait....", url, setPaymentJSON().toString(), access_token, SelfcheckinPaymentActivity.this);
                postMethod.execute();
            } else{
                String url = sfdcddetails.instance_url + WebServiceUrls.SA_PAYMENT_UPDATE;
                postMethod = new HttpPostData("Processing your payment...",url,setPaymentJSON().toString(), access_token, SelfcheckinPaymentActivity.this);
                postMethod.execute();
            }
        }else{
            AlertDialogCustom custom=new AlertDialogCustom(SelfcheckinPaymentActivity.this);
            custom.setParamenters("Alert!",getString(R.string.network_error), null, null, 1, false);
            custom.show();
        }
    }

    private JSONObject setJsonBody() {
        // String where=" where ";
        JSONObject parent = new JSONObject();
        try {
            Cursor card_pay_setting= Util.db.getEvent_Card_PGateway(checked_in_eventId);
            card_pay_setting.moveToFirst();
            JSONObject buyer = new JSONObject();
            JSONArray order_array = new JSONArray();
            // set buyer info body
            buyer.put("AmountPayable", total+"");
            buyer.put("AfterDiscountPrice", String.valueOf(total));
            buyer.put("Company", buyer_info.getCompany());
            buyer.put("CurrencyCode", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
            buyer.put("DiscountedPrice", "0");
            buyer.put("EMailId", buyer_info.getEmail().toLowerCase());
            //newly added
            //buyer.put("AttendeePic", buyer_info.getUserPic());
            buyer.put("FirstName", buyer_info.getFirstName());
            buyer.put("LastName", buyer_info.getLastName());
            //	buyer.put("buyerImage", BuyerAttendeeImage);
            buyer.put("PhoneNumber", buyer_info.getMobile());
            buyer.put("OrderStatus", "Fully  Paid".replaceAll(" ", "%20"));
            buyer.put("OrderTaxes", String.format("%.2f", service_tax));
            buyer.put("OrderTotal", String.valueOf(total));
            buyer.put("Note",buyer_info.getnote());
            buyer.put("PayKey", transaction_id);
            buyer.put("PaymentType", "");
            buyer.put("PaygateWayType","");
            buyer.put("ChargeType", "");
            buyer.put("cardno","");
            buyer.put("FeeAmount", String.valueOf(servicefee));
            buyer.put("keyname","");
            buyer.put("CustomBarcode", NullChecker(buyer_info.getCustomBarcode()));
            buyer.put("Designation", NullChecker(buyer_info.getDesignation()));


            // set order body
            for (int i = 0; i < items_list.size(); i++) {
                String where_condition = " where EventId='"	+ checked_in_eventId + "' AND ItemId='" + items_list.get(i).getItemId() + "'";
                Cursor c = Util.db.getTicketCursor(where_condition);
                ////Log.i("----------------Cursor Count---------------",":" + c.getCount());
                c.moveToFirst();
                String ticket_setting1 = c.getString(c.getColumnIndex("ItemOption"));
                // sai change
                //String badgeable=Util.db.getItemBadgble(checked_in_eventId,items_list.get(i).getItemId());
                ////Log.i("----------------Cursor Count---------------", ":"+ ticket_setting1);
                JSONObject item_object = new JSONObject();
                JSONArray item_line_array = new JSONArray();
                item_object.put("ItemDiscount", "0.00");
                //double item_type_price = Util.db.getItemType_BL_FEE(checked_in_eventId, items_list.get(i).getItemTypeId());
                //if(!Boolean.parseBoolean(items_list.get(i).getItemFeeSetting()) && (items_list.get(i).getItemPaidType().equalsIgnoreCase("Paid") || items_list.get(i).getItemPaidType().equalsIgnoreCase("Donation"))) {
                item_object.put("FeeAmount", items_list.get(i).getBL_Fee());
                //}
                item_object.put("ItemId", items_list.get(i).getItemId());
                item_object.put("ItemName", items_list.get(i).getItemName());
                item_object.put("ItemPoolId", items_list.get(i).getItemPoolId());
                item_object.put("ItemPrice",String.valueOf(items_list.get(i).getItemPrice()));
                item_object.put("ItemQuantity",String.valueOf(items_list.get(i).getItemQuantity()));
                item_object.put("ItemTotal", String.valueOf(items_list.get(i).getItemQuantity()*items_list.get(i).getItemPrice()));
                Boolean imageset=false;
                if (ticket_setting1.trim().equalsIgnoreCase(getString(R.string.infofrombuyer))) {
                    for (int j = 0; j < order_line_items.size(); j++) {
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            JSONObject line_item_object = new JSONObject();
                            if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))){
                                line_item_object.put("AttendeeImage", "");
                            }else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");
                            }
                            line_item_object.put("Company",
                                    buyer_info.getCompany());
                            line_item_object.put("EmailId",
                                    buyer_info.getEmail().toLowerCase());
                            line_item_object.put("FirstName",
                                    buyer_info.getFirstName());
                            line_item_object.put("LastName",
                                    buyer_info.getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", buyer_info.getMobile());
                            if(!buyer_info.getBadge_lable().trim().isEmpty()){
                                line_item_object.put("BadgeLabel", buyer_info.getBadge_lable());
                            }else{
                                line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(order_line_items.get(j).getItemPoolId(),checked_in_eventId));
                            }
                            line_item_object.put("Tag",buyer_info.getTag().toString().replace(" ", ","));
                            line_item_object.put("Seatno",buyer_info.getSeatno());
                            line_item_object.put("Designation", buyer_info.getDesignation());
                            line_item_object.put("Note",buyer_info.getnote());
                            //line_item_object.put("CustomBarcode", buyer_info.getCustomBarcode());
                            item_line_array.put(line_item_object);
                        }
                    }
                } else if(ticket_setting1.trim().equalsIgnoreCase(getString(R.string.infofromattendee))){
                    for (int j = 0; j < order_line_items.size(); j++) {
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            JSONObject line_item_object = new JSONObject();
                           /* if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))){
                                line_item_object.put("AttendeeImage", "");
                            }else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");
                            }
                            line_item_object.put("Company",
                                    buyer_info.getCompany());
                            line_item_object.put("EmailId",
                                    buyer_info.getEmail().toLowerCase());
                            line_item_object.put("FirstName",
                                    buyer_info.getFirstName());
                            line_item_object.put("LastName",
                                    buyer_info.getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", buyer_info.getMobile());
                            if(!buyer_info.getBadge_lable().trim().isEmpty()){
                                line_item_object.put("BadgeLabel", buyer_info.getBadge_lable());
                            }else{
                                line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(order_line_items.get(j).getItemPoolId(),checked_in_eventId));
                            }
                            line_item_object.put("Tag",buyer_info.getTag().toString().replace(" ", ","));
                            line_item_object.put("Seatno",buyer_info.getSeatno());
                            line_item_object.put("Designation", buyer_info.getDesignation());
                            line_item_object.put("Note",buyer_info.getnote());
                            //line_item_object.put("CustomBarcode", buyer_info.getCustomBarcode());
                            item_line_array.put(line_item_object);*/
                            if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))) {
                                line_item_object.put("AttendeeImage", order_line_items.get(j).getUserImage());
                            }
                            else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                imageset=true;
                                line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                            }else{
                                line_item_object.put("AttendeeImage", "");

                            }
                            line_item_object.put("Company", order_line_items.get(j).getCompanyName());
                            line_item_object.put("EmailId", order_line_items.get(j).getEmail().toLowerCase());
                            line_item_object.put("FirstName", order_line_items.get(j).getFirstName());
                            line_item_object.put("LastName", order_line_items.get(j).getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", order_line_items.get(j).getMobile());
                            line_item_object.put("BadgeLabel", order_line_items.get(j).getBadgeLabel());
                            line_item_object.put("Tag", order_line_items.get(j).getTag().toString().trim().replace(" ", ","));
                            line_item_object.put("Seatno", order_line_items.get(j).getSeatno());
                            line_item_object.put("Designation", order_line_items.get(j).getDesignation());
                            line_item_object.put("Note", order_line_items.get(j).getnote());
                            line_item_object.put("CustomBarcode", order_line_items.get(j).getCustomBarCode());
                            item_line_array.put(line_item_object);
                        }
                    }
                }else {
                    for (int j = 0; j < order_line_items.size(); j++) {
                        JSONObject line_item_object = new JSONObject();
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            if(!(order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))
                            {
                                if((imageset.equals(true)) &&((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge"))){
                                    line_item_object.put("AttendeeImage", "");
                                }else if((order_line_items.get(j).Item_Pool__r.Badgable__c).equals("B - Badge")){
                                    imageset=true;
                                    line_item_object.put("AttendeeImage", BuyerAttendeeImage);
                                }else{
                                    line_item_object.put("AttendeeImage", "");
                                }
                                line_item_object.put("Company",
                                        buyer_info.getCompany());
                                line_item_object.put("EmailId",
                                        buyer_info.getEmail().toLowerCase());
                                line_item_object.put("FirstName",
                                        buyer_info.getFirstName());
                                line_item_object.put("LastName",
                                        buyer_info.getLastName());
                                line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                                line_item_object.put("Phoneno", buyer_info.getMobile());
                                if(!buyer_info.getBadge_lable().trim().isEmpty()){
                                    line_item_object.put("BadgeLabel", buyer_info.getBadge_lable());
                                }else{
                                    line_item_object.put("BadgeLabel", Util.db.getItemPoolBadgeLabel(order_line_items.get(j).getItemPoolId(),checked_in_eventId));
                                }
                                line_item_object.put("Tag",buyer_info.getTag().toString().replace(" ", ","));
                                line_item_object.put("Seatno",buyer_info.getSeatno());
                                line_item_object.put("Designation", buyer_info.getDesignation());
                                line_item_object.put("Note",buyer_info.getnote());
                                //line_item_object.put("CustomBarcode", buyer_info.getCustomBarcode());
                                item_line_array.put(line_item_object);
                            }else{
                                line_item_object.put("Company", order_line_items.get(j).getCompanyName());
                                line_item_object.put("EmailId", order_line_items.get(j).getEmail().toLowerCase());
                                // newly Added
                                line_item_object.put("AttendeeImage", order_line_items.get(j).getUserImage());
                                line_item_object.put("FirstName", order_line_items.get(j).getFirstName());
                                line_item_object.put("LastName", order_line_items.get(j).getLastName());
                                line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                                line_item_object.put("Phoneno", order_line_items.get(j).getMobile());
                                line_item_object.put("BadgeLabel", order_line_items.get(j).getBadgeLabel());
                                line_item_object.put("Tag", order_line_items.get(j).getTag().toString().trim().replace(" ", ","));
                                line_item_object.put("Seatno", order_line_items.get(j).getSeatno());
                                line_item_object.put("Designation", order_line_items.get(j).getDesignation());
                                line_item_object.put("Note", order_line_items.get(j).getnote());
                                line_item_object.put("CustomBarcode", order_line_items.get(j).getCustomBarCode());
                                item_line_array.put(line_item_object);
                            }
                        }
                    }
                }/*{
                    for (int j = 0; j < order_line_items.size(); j++) {
                        JSONObject line_item_object = new JSONObject();
                        if (order_line_items.get(j).getItemId().equals(items_list.get(i).getItemId())) {
                            line_item_object.put("Company", order_line_items.get(j).getCompanyName());
                            line_item_object.put("EmailId", order_line_items.get(j).getEmail().toLowerCase());
                            // newly Added
                            line_item_object.put("AttendeeImage", order_line_items.get(j).getUserImage());
                            line_item_object.put("FirstName", order_line_items.get(j).getFirstName());
                            line_item_object.put("LastName", order_line_items.get(j).getLastName());
                            line_item_object.put("TicketId", order_line_items.get(j).getTicketNumber());
                            line_item_object.put("Phoneno", order_line_items.get(j).getMobile());
                            line_item_object.put("BadgeLabel", order_line_items.get(j).getBadgeLabel());
                            line_item_object.put("Tag",order_line_items.get(j).getTag().toString().trim().replace(" ", ","));
                            line_item_object.put("Seatno",order_line_items.get(j).getSeatno());
                            line_item_object.put("Designation", order_line_items.get(j).getDesignation());
                            line_item_object.put("Note",order_line_items.get(j).getnote());
                            line_item_object.put("CustomBarcode",order_line_items.get(j).getCustomBarCode());
                            item_line_array.put(line_item_object);
                        }
                    }
                }*/
                item_object.put("OrderLineItems", item_line_array);
                order_array.put(item_object);
            }
            parent.put("BuyerInfo", buyer);
            parent.put("Orders", order_array);
            parent.put("SessionAbntime", checkedin_event_record.sessiontime);
            //Log.i("----json Date---", ":"+parent.toString());

        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
            //Log.i("----json Date Exception---------------", ":"+e.getMessage());
        }

        return parent;
    }

    private JSONObject setPaymentJSON(){
        JSONObject main_obj = new JSONObject();
        try {
            Cursor card_pay_setting= Util.db.getEvent_Card_PGateway(checked_in_eventId);
            if(card_pay_setting.moveToFirst()){
                //card_pay_setting.moveToFirst();
                payment_gateway_name = card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
            }


            main_obj.put("eventId", checked_in_eventId);
            main_obj.put("orderId", order_id);
            main_obj.put("isTransactionSuccess", "Success");
            main_obj.put("errorMessage", "");
            if(brand.isEmpty() && card != null){
                brand = card.getType();
            }
            main_obj.put("cardType", brand);
            main_obj.put("cardLastDigits", card_last_4);
            main_obj.put("RegType", "ScanAttendee");

            if (request_type.equals(Util.CHECK)) {
                main_obj.put("Payment_Mode", Util.CHECK);
                main_obj.put("transactionId", check_number);
                main_obj.put("OrderStatus", order_status);

            }else if(request_type.equals(Util.CASH)){
                main_obj.put("Payment_Mode", Util.CASH);
            }else if(request_type.equals(Util.EXTERNAL_PAY)){
                main_obj.put("Payment_Mode", Util.EXTERNAL_PAY);
            }else if(total==0){
                main_obj.put("PaymentType", "");
                main_obj.put("Payment_Mode", "Free");
            }else{
                main_obj.put("transactionId", transaction_id);
                main_obj.put("Payment_Mode", Util.CRADITCARD);
                main_obj.put("PaymentType ", card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME)));
                main_obj.put("paygatewayid", card_pay_setting.getString(card_pay_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C)));
                if(String.valueOf(pay_errorcode).equalsIgnoreCase("0")){
                    main_obj.put("gatewaytxcode", "");
                }else {
                    main_obj.put("gatewaytxcode", String.valueOf(pay_errorcode));
                }
                main_obj.put("gatewaytxstatus", pay_id.replace("true",""));
            }

            card_pay_setting.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return main_obj;
    }

    /* (non-Javadoc)
     * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
     */
    @Override
    public void parseJsonResponse(String response) {
        // TODO Auto-generated method stub
        //Log.i("---ParseJson Response---", "---"+response);

        gson = new Gson();
        if (paytmTxnAmount.length() > 0 && response != null) {
            PaytmStatusCheckResponseGson objPayJson = gson.fromJson(response, PaytmStatusCheckResponseGson.class);
            Double amt = Double.parseDouble(paytmTxnAmount);
            Double amt1 = Double.parseDouble(objPayJson.TXNAMOUNT);

            if (objPayJson.STATUS != null && objPayJson.STATUS.equals("TXN_SUCCESS") && objPayJson.TXNAMOUNT != null && amt.equals(amt1)) {
                paytmTxnAmount = "";
                doRequest();//Paytm transationupdate service call
            }

        } else {
            try {

                if (params.isEmpty() && order_id.isEmpty()) {
                    if (!isValidResponse(response)) {
                        openSessionExpireAlert(errorMessage(response));
                        Util.txt_okey.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent startentent = new Intent(SelfcheckinPaymentActivity.this,SelfCheckinAttendeeList.class);
                                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(startentent);
                                finish();
                            }
                        });
                    }

                    totalorderlisthandler = new Gson().fromJson(response, TotalOrderListHandler.class);
                    //Log.i("---ORDER ARRAY SIZE---", "Server"+ totalorderlisthandler.TotalLists.size());

                    if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
					/*MediaPlayer mediaPlayer = MediaPlayer.create(SelfcheckinPaymentActivity.this, R.raw.somethingwentwrong);
					mediaPlayer.start();*/
                        playSound(R.raw.somethingwentwrong);
                        openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
                    } else {
                        Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                        //Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
                        order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
                        order_name = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName();// sending this order name to paytm custid
                        if (total == 0) {
                            doRequest();
                        }
                    }


                } else if (params.isEmpty() && !order_id.isEmpty()) {
                    //totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
                    ////Log.i("---ORDER ARRAY SIZE---", "Server"+ totalorderlisthandler.TotalLists.size());
                    if (!isValidResponse(response)) {
                        openSessionExpireAlert(errorMessage(response));
                    }
                    JSONObject obj = new JSONObject(response);
                    String status = obj.optString("status");
                    String orderStatus = NullChecker(obj.optString("orderStatus"));
                  //  if (status.toLowerCase().equalsIgnoreCase("booked")) {
                    if (!status.toLowerCase().isEmpty()) {
                       // PaymentObject payment = new Gson().fromJson(obj.optJSONObject("Paymentinfo").toString(), PaymentObject.class);
                        OrderDetailsHandler orderDetailsHandler = new Gson().fromJson(response, OrderDetailsHandler.class);
                        totalorderlisthandler.TotalLists.get(0).orderInn.setOrderStatus(orderStatus);
                        totalorderlisthandler.TotalLists.get(0).setPaymentInn(orderDetailsHandler.paymentInnmultiple);
                        //TODO PAYMENTITEMS
                        /*totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber(Util.NullChecker(check_number));
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setId(payment.Id);
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCreditCardType(payment.credit_card_type__c);
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setlast4degits(payment.credit_card_last_4digits__c);
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setCheckNumber(payment.Payment_Ref_Number__c);
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setRegistrationType(payment.Registration_Type__c);
                        totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).setPaymentMode(payment.Payment_Mode__c);

                        if (payment.BLN_Pay_Gateway__c != null) {
                            totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Id = payment.BLN_Pay_Gateway__c;
                            totalorderlisthandler.TotalLists.get(0).paymentInnmultiple.get(0).BLN_Pay_Gateway__r.PGateway_Type__r.Name = payment_gateway_name;
                        }
*/
                        for (OrderItemListHandler ticket : totalorderlisthandler.TotalLists.get(0).ticketsInn) {
                            ticket.setTicketStatus(status);
                        }

                        Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                        Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(orderDetailsHandler.ticklist);

                       /* for (int i = 0; i < orderDetailsHandler.ticklist.size(); i++){
                            String BadgeId = orderDetailsHandler.ticklist.get(i).Badge_ID__c;
                            String TicketId = orderDetailsHandler.ticklist.get(i).Id;
                            String BadgeParentId = NullChecker(orderDetailsHandler.ticklist.get(i).badgeparentid__c);
                            String printstatus = "";//Printed
                            Util.db.insertandupdateAttendeeBadgeIdandBadgeParentID(BadgeParentId,
                                    BadgeId, "", TicketId,printstatus);
                        }*/
                        if(!orderStatus.equalsIgnoreCase("Abandoned")) {
                            attendee_cursor = Util.db.getBadgeableTicketOrderDetails(order_id);
                            //attendee_cursor.moveToFirst();
                            String msg = "";
                            //if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&((PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))||(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")))){
                            if (attendee_cursor != null && attendee_cursor.getCount() >= 1
                                    && !(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())
                                    && Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {
                                msg = "Do you want to Print Badge?";
                            } else {
                                msg = "";//"Do you want to Print Badge?"
                            }
                            Util.setCustomAlertDialog(SelfcheckinPaymentActivity.this);
                            String alert = "Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!";
                            if (orderDetailsHandler.paymentInnmultiple.get(0).Payment_Mode__c.equalsIgnoreCase(Util.CASH)) {
                                double change = entered_cash_amount - (total + service_tax + servicefee);
                                if (change > 0) {
                                    msg = "Please return " + String.format("%.2f", change) + ". " + msg;
                                }
                            }
                            Util.openCustomDialog(alert, msg);
                            if (attendee_cursor != null && attendee_cursor.getCount() >= 1) {
                                Util.txt_dismiss.setVisibility(View.VISIBLE);
                            } else {
                                Util.txt_dismiss.setVisibility(View.GONE);
                            }
                            Util.txt_okey.setText("OK");
                            Util.alert_dialog.setCancelable(false);
                            Util.txt_okey.setOnClickListener(new OnClickListener() {

                                @Override
                                public void onClick(View arg0) {
                                    Util.alert_dialog.dismiss();
								/*if(attendee_cursor!=null&&attendee_cursor.getCount()>=1&&
										((PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Brother"))
												||(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").equalsIgnoreCase("Zebra")))){

							*/
                                    if (attendee_cursor != null && attendee_cursor.getCount() >= 1 &&
                                            !(PrinterDetails.selectedPrinterPrefrences.getString(ZebraPrinter.SELECTED_PRINTER, "").isEmpty())
                                            && Util.getselfcheckinbools(Util.ISPRINTALLOWED)) {

                                        //if(attendee_cursor!=null&&attendee_cursor.getCount()>=1) {
									/*Intent endintent = new Intent(SelfcheckinPaymentActivity.this, OrderSucessPrintActivity.class);
									endintent.putExtra(Util.ORDER_ID, order_id);
									endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(endintent);*/
                                        if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                                            Intent startentent = new Intent(SelfcheckinPaymentActivity.this, SelfCheckinAttendeeList.class);
                                            startentent.putExtra(Util.ORDER_ID, order_id);
                                            startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(startentent);
                                        } else {
                                            Intent endintent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
                                            endintent.putExtra(Util.ORDER_ID, order_id);
                                            endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(endintent);
                                        }
                                        //finish();
                                        //doBadgePrint(attendee_cursor);
                                    } else {
                                        if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                                            Intent startentent = new Intent(SelfcheckinPaymentActivity.this, SelfCheckinAttendeeList.class);
                                            startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(startentent);
                                        } else {
                                            Intent endintent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
									/*endintent.putExtra(Util.ORDER_ID, order_id);
									endintent.putExtra(Util.ORDER_CLOUD_PRINT,true);*/
                                            endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(endintent);
                                        }
                                    }
                                    //finish();
                                }
                            });
                            Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Util.alert_dialog.dismiss();
                                    if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                                        Intent startentent = new Intent(SelfcheckinPaymentActivity.this, SelfCheckinAttendeeList.class);
                                        startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(startentent);
                                    } else {
                                        Intent endintent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
                                        endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(endintent);
                                    }
                                }
                            });
                        }else  if(!NullChecker(pay_id).isEmpty()&&!NullChecker(pay_id).equalsIgnoreCase("true")){
                            openCardErrorDialog("Error", pay_id+"\n Your payment is declined, Please try again!");
                        }
						/*Intent startentent = new Intent(SelfcheckinPaymentActivity.this, GlobalScanActivity.class);
						ordersuccess_popupok_clicked=true;
						startentent.putExtra(Util.SCANDATA, order_id.toCharArray());
						startentent.putExtra(Util.ISFINISH, true);
						startentent.putExtra(Util.INTENT_KEY_1, SelfcheckinPaymentActivity.class.getName());
						startentent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


						Intent endintent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
						endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						AlertDialogCustom ask_dialog = new AlertDialogCustom(SelfcheckinPaymentActivity.this);
						//Your payment for Order ID: 0-23444 was Successful. Do you want to print a Badge?
						//String msg = "Do you want to Print/Checkin Badges?";
						String msg = "Do you want to Print Badges?";
						if (payment.Payment_Mode__c.equalsIgnoreCase(Util.CASH)) {
							double change = entered_cash_amount - (total + service_tax + servicefee);
							if (change > 0) {
								msg = "Please return " + String.format("%.2f", change) + ". " + msg;
							}
						}

						// msg = "Do you want to Print/Checkin Badges?";

						String alert = "Your payment for Order ID: " + totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName() + " was Successful!";
						ask_dialog.setParamenters(alert, msg, startentent, endintent, 2, true);
						ask_dialog.setAlertImage(R.drawable.success, "success");
						ask_dialog.show();*/

                    } else {
                        //startErrorAnimation("Payment Process Failed.", txt_error_msg);
                        openCardErrorDialog("Error", "Your payment is declined please check you card details or discard the order?");
                    }

                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.AUTHORIZATION_REQ_CODE) {
                    params.clear();
                    String[] res = response.split("\\$");
                    String resCode = res[0];
                    String resMsg = res[3];
                    System.out.println(resCode + " ---------------------- " + "------- " + resMsg);
                    if (resCode != null && resCode != "") {
                        if (resCode.equals("1")) {//This transaction has been approved
                            transaction_id = res[6];
                            card_last_4 = res[50].substring(res[50].length() - 4, res[50].length());
                            brand = res[51];
                            doRequest();
                        } else {
                            openCardErrorDialog("Error", "Your payment is declined please check you card details or discard the order?");
                        }
                    }

                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.PAYPAL_REQ_CODE) {
                    this.params.clear();
                    //Log.i("----------------Response------------",":"+response);
                    HashMap<String, String> res_values = new HashMap<String, String>();
                    final String s = new String(response.getBytes(), "UTF-8");
                    //Log.i("----------------Params------------",":"+s);
                    for (String newstring : s.split("&")) {
                        res_values.put(newstring.split("=")[0], newstring.split("=")[1]);
                    }

                    //Log.i("----------------ACK------------",":"+res_values.get("ACK").toLowerCase());
                    String ack = res_values.get("ACK").toLowerCase();

                    if (ack.equalsIgnoreCase("Success")) {
                        transaction_id = res_values.get("TRANSACTIONID");
                        card_last_4 = cardNo.substring(cardNo.length() - 4, cardNo.length());
                        doRequest();
                    } else {
                        openCardErrorDialog("Error", URLDecoder.decode(res_values.get("L_LONGMESSAGE0")));
                        //startErrorAnimation(res_values.get("L_LONGMESSAGE0"), txt_error_msg);
                    }
                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.STRIPE_CARD_TOKEN_REQ_CODE) {
                    params.clear();
                    //Log.i("-------------Stripe Response ---------------",":"+response);
                    JSONObject res_obj = new JSONObject(response);
                    //Log.i("-------------Stripe Response Before If---------------",":"+response+" : "+res_obj.has("error"));
                    if (!res_obj.has("error")) {
                        String card_token = res_obj.optString("id");
                        new  doStripeAdaptivecustumer().execute(card_token);
                       // StripeAdaptivePayment(card_token);
                    } else if(res_obj.has("error")){
                        openCardErrorDialog("Error", res_obj.optJSONObject("error").optString("message")+" Please check you card details!");
                    }else {
                        //Log.i("-------------Stripe Response Else---------------",":"+response+" : "+res_obj.has("error"));

                        StripeAdaptiveTokenRefresh();
                    }

                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.STRIPE_REFRESH_TOKEN_CODE) {
                    JSONObject res_obj = new JSONObject(response);
                    if (res_obj.has("error")) {
                        openCardErrorDialog("Error", res_obj.optString("error_description"));
                    } else {
                       /* _event_payment_keys.get(0).PG_User_Key__c = res_obj.optString("access_token");
                        _event_payment_keys.get(0).PG_Signature__c = res_obj.optString("refresh_token");*/
                        _event_payment_keys.get(0).PG_Signature__c = res_obj.optString("refresh_token");
                        _event_payment_keys.get(0).PG_Pass_Secret__c = res_obj.optString("access_token");
                        Util.db.InsertAndUpdatePay_GAteway_Key(_event_payment_keys.get(0));
                        StripeCardToken();
                    }

                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.STRIPE_REQ_CODE) {
                    //Log.i("---------------Stripe Payment Response--------------",":"+response);
                    this.params.clear();
                    JSONObject res_obj = new JSONObject(response);
                    //boolean paid =res_obj.optBoolean("paid");
                    String status = res_obj.optString("status");

                    if (status.equalsIgnoreCase("succeeded") || status.equalsIgnoreCase("paid")) {
                        transaction_id = res_obj.optString("id");
                        card_last_4 = res_obj.optJSONObject("source").optString("last4");
                        brand = res_obj.optJSONObject("source").optString("brand");
                        doRequest();
                    } else if (res_obj.has("error")) {

                        JSONObject error_obj = res_obj.optJSONObject("error");
                        openCardErrorDialog(error_obj.optString("type"), error_obj.optString("message"));
                        //startErrorAnimation(status, txt_error_msg);
                    } else {
                        openCardErrorDialog("Error", "Your payment is declined please check you card details or discard the order?");
                    }

                } else if (Integer.valueOf(params.get(WebServiceUrls.WEBSERVICE_CODE)) == WebServiceUrls.TRUSTCOMMERCE_CODE) {
                    params.clear();
                    response = response.replace("\n", "&&");
                    //Log.i("-----------------Afetr Replase Response--------------",":"+response);
                    HashMap<String, String> values_map = convert(response);
                    if (values_map.get("status").equalsIgnoreCase("approved")) {
                        transaction_id = values_map.get("transid");
                        card_last_4 = cardNo.substring(cardNo.length() - 4, cardNo.length());
                        doRequest();
                    } else {
                        openCardErrorDialog("Error", values_map.get("responsecodedescriptor"));
                        //startErrorAnimation(values_map.get("responsecodedescriptor"), txt_error_msg);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                this.params.clear();
                startErrorAnimation(e.getMessage(), txt_error_msg);
                e.printStackTrace();
            }
        }
    }
    private void callSellTickets() {
        try {
            progressDialog =new ProgressDialog(SelfcheckinPaymentActivity.this);
            progressDialog.setMessage("Processing your order...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            ApiInterface apiService = ApiClient.getClient(sfdcddetails.instance_url).create(ApiInterface.class);
            // Call<Void> jsonbody= apiService.setSurveys(setSellJsonBody());
            Call<TotalOrderListHandler> call = apiService.getSellTicketAttendees(checked_in_eventId,sfdcddetails.user_id,""
                    ,setJsonBody().toString(),sfdcddetails.token_type + " "+ sfdcddetails.access_token);
            call.enqueue(new retrofit2.Callback<TotalOrderListHandler>() {
                @Override
                public void onResponse(Call<TotalOrderListHandler> call, Response<TotalOrderListHandler> response) {
                    Log.e(call+"------success-------", "------response started-------");
                    if(AppUtils.isLogEnabled){AppUtils.displayLog(call+"------JSON Response-------", response.toString());}
                    try {
                        if (!isValidResponse(response.toString())) {
                            dismissProgressDialog();
                            openSessionExpireAlert(errorMessage(response.toString()));
                        }else if (response.code() == 200) {
                        totalorderlisthandler = response.body();
                        if (!NullChecker(totalorderlisthandler.errorMsg).isEmpty()) {
                            playSound(R.raw.somethingwentwrong);
                            openDuplicateBarcodeAlert(totalorderlisthandler.errorMsg);
                            dismissProgressDialog();
                        } else {
                            Util.db.upadteOrderList(totalorderlisthandler.TotalLists, checked_in_eventId);
                            //Log.i("---ORDER ARRAY SIZE---","Order id===>"+ totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId());
                            order_id = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderId();
                            order_name = totalorderlisthandler.TotalLists.get(0).orderInn.getOrderName();// sending this order name to paytm custid
                            dismissProgressDialog();
                            if (total == 0) {
                                //requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
                                doRequest();
                            }
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
        }catch (Exception e){
            dismissProgressDialog();
            e.printStackTrace();
        }
    }
    private void dismissProgressDialog() {
        if(progressDialog!=null) {
            if(progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void openDuplicateBarcodeAlert(String msg){
        Util.setCustomAlertDialog(SelfcheckinPaymentActivity.this);
        Util.alert_dialog.setCancelable(false);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        //Util.txt_dismiss.setText("DISCARD");
        //Util.txt_okey.setText("CHANGE");
        Util.txt_dismiss.setVisibility(View.GONE);
        Util.txt_okey.setText("Ok");
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                Intent i = new Intent();
                setResult(200, i);
                finish();
            }
        });
		/*Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				Intent startentent = new Intent(SelfcheckinPaymentActivity.this,ManageTicketActivity.class);
				startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(startentent);
				finish();
			}
		});*/
        Util.openCustomDialog("Error",msg);
        //Util.openCustomDialog("Error",msg+" Do you want to change the barcode or discard the order?");
    }

    private void openCardErrorDialog(String alert, String msg){
        Util.setCustomAlertDialog(SelfcheckinPaymentActivity.this);
        Util.alert_dialog.setCancelable(false);
        Util.txt_dismiss.setVisibility(View.VISIBLE);
        Util.setCustomDialogImage(R.drawable.error);
        Util.txt_dismiss.setText("DISCARD");
        Util.txt_okey.setText("OK");
        Util.txt_okey.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
            }
        });
        Util.txt_dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // ticket_dialog.dismiss();
                Util.alert_dialog.dismiss();
                if(Util.getselfcheckinbools(Util.ISSELFCHECKIN)){
                    Intent startentent = new Intent(SelfcheckinPaymentActivity.this,SelfCheckinAttendeeList.class);
                    startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startentent);
                    finish();
                }else {
                    Intent startentent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
                    startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(startentent);
                    finish();
                }
            }
        });
        Util.openCustomDialog(alert,msg);
    }

    public static HashMap<String, String> convert(String str) {
        String[] tokens = str.split("&&");
        HashMap<String, String> map = new HashMap<String, String>();
        for (String s : tokens) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        return map;
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
        // TODO Auto-generated method stub
        activity = this;
        v = inflater.inflate(layout, null);
        linearview.addView(v);
        txt_title.setText("Payment");
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        img_setting.setVisibility(View.GONE);
        img_menu.setImageResource(R.drawable.back_button);
        event_layout.setVisibility(View.GONE);
        button_layout.setVisibility(View.GONE);
        event_layout.setVisibility(View.VISIBLE);

        view_pages = (ViewPager)linearview.findViewById(R.id.pager);
        tabs = (PagerSlidingTabStrip)linearview.findViewById(R.id.tabs);
        //txt_payable = (TextView) linearview.findViewById(R.id.txtpayable);
        //txt_grand_total = (TextView) linearview.findViewById(R.id.txtgrandtotal);
        //txt_payment_mode = (TextView) linearview.findViewById(R.id.txtpaymentmode);
        txt_paypal_error = (TextView) linearview.findViewById(R.id.txtpaypalerror);

        txt_summaryamount = (TextView) linearview.findViewById(R.id._txttotal_amount);
        txt_summaryfee = (TextView) linearview.findViewById(R.id._txttotal_fee);
        txt_summarytax = (TextView) linearview.findViewById(R.id._txttotal_tax);
        txt_summarytotal = (TextView) linearview.findViewById(R.id._txttotal_payable);

    }

    private void setUI(){
        if(_event_payment_setting != null){
            for(int i=0;i<_event_payment_setting.getCount();i++){
                _event_payment_setting.moveToPosition(i);
                String pay_gate_name = Util.db.getPayment_Type_Name(_event_payment_setting.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID)));
                //Log.i("-------------------Event Payment Gateway Name In For Loop---------------",":"+pay_gate_name);

                String whereCondition=" Where "+ EventPayamentSettings.KEY_PAY_GATEWAY_ID+ " = '"+_event_payment_setting.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_GATEWAY__C))+"' AND "+ EventPayamentSettings.KEY_PGATEWAY_TYPE_ID
                        +" = '"+_event_payment_setting.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID))+"'";
                ////Log.i("--------------Where Clause Position--------------  "+i,":"+pay_gate_name+"  &&  "+whereCondition);
                if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.stripe_direct))){
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.auth_net_direct))){
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.paypal_direct))) {
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.trust_commerce_direct))) {
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.stripe_adaptive))) {
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.paypal_adaptive))) {
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }else if (pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.citrus_direct)) || pay_gate_name.equalsIgnoreCase(getResources().getString(R.string.paytm_direct))) {
                    _event_payment_keys = Util.db.getPay_Gateway_Key(whereCondition);
                    ////Log.i("--------------Keys Size--------------  " + i, ":"+ _event_payment_keys);
                }
            }
        }
        //Log.i("-----------Event Keys-----------",":"+_event_payment_keys);

        //ticket_setting = this.getIntent().getStringExtra(Util.TICKETSETTING);

        txt_summaryamount.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f", total));
        txt_summaryfee.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",servicefee));
        txt_summarytax.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",service_tax));
        Double totalvalue= Double.valueOf(String.format("%.2f",total))+Double.valueOf(String.format("%.2f",servicefee))+Double.valueOf(String.format("%.2f",service_tax));
        txt_summarytotal.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+String.format("%.2f",totalvalue));

        if (_event_payment_setting.getCount() > 0) {
            for (int i = 0; i < _event_payment_setting.getCount(); i++) {
                _event_payment_setting.moveToPosition(i);
                String payment_setting_name = _event_payment_setting.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
                //Log.i("---------------Payment Setting Name----------------",":"+payment_setting_name+" - "+_event_card_pgateway.getCount());
                String payment_label_name=NullChecker(_event_payment_setting.getString(_event_payment_setting.getColumnIndex(EventPayamentSettings.EVENT_PAYGATEWAY_LABEL_NAME)));
                /*if (payment_setting_name.equalsIgnoreCase(getString(R.string.cash))) {
                    //payment_option_layouts.add(R.layout.cash_layout);
                    if(!payment_label_name.isEmpty()&&!payment_label_name.equalsIgnoreCase(payment_setting_name)){
                        VIEWNAMES.add(payment_label_name);
                    }else {
                        VIEWNAMES.add(payment_setting_name);
                    }
                    TITLES.add(getString(R.string.cash));
                }else if (payment_setting_name.equalsIgnoreCase(getString(R.string.check))) {
                    //payment_option_layouts.add(R.layout.check_layout);
                    if(!payment_label_name.isEmpty()&&!payment_label_name.equalsIgnoreCase(payment_setting_name)){
                        VIEWNAMES.add(payment_label_name);
                    }else {
                        VIEWNAMES.add(payment_setting_name);
                    }
                    TITLES.add(getString(R.string.check));
                }else if (payment_setting_name.equalsIgnoreCase(getString(R.string.extrn_pay))) {
                    //payment_option_layouts.add(R.layout.external_gateway_layout);
                    if(!payment_label_name.isEmpty()&&!payment_label_name.equalsIgnoreCase(payment_setting_name)){
                        VIEWNAMES.add(payment_label_name);
                    }else {
                        VIEWNAMES.add(payment_setting_name);
                    }
                    TITLES.add(getString(R.string.extrn_pay));
                }else */if (_event_card_pgateway.getCount() > 0) {
                    //payment_option_layouts.add(R.layout.credit_card_layout);
                    for(int j=0; j<_event_card_pgateway.getCount(); j++){
                        _event_card_pgateway.moveToPosition(j);
                        String gateway_name = _event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
                        if(!gateway_name.isEmpty()){
                            if(!TITLES.contains(getString(R.string.creditcard))){
                                TITLES.add(getString(R.string.creditcard));
                                VIEWNAMES.add(getString(R.string.creditcard));

                            }
                        }
                    }

                }
            }


        } else {
            Intent i = new Intent(SelfcheckinPaymentActivity.this,PaymentSetting.class);
            i.putExtra(Util.EDIT_EVENT_ID, checked_in_eventId);
            dialog.setParamenters("Alert","Do you want to set Payment Settings", i, null, 3,true);
        }

        //Collections.sort(TITLES);
        //_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
		/*for(String view_name : TITLES){

			if (view_name.equalsIgnoreCase(getString(R.string.cash))) {
				payment_option_layouts.add(R.layout.cash_layout);
			}else if (view_name.equalsIgnoreCase(getString(R.string.check))) {
				payment_option_layouts.add(R.layout.check_layout);
			}else if (view_name.equalsIgnoreCase(getString(R.string.extrn_pay))) {
				payment_option_layouts.add(R.layout.external_gateway_layout);
			}else if (view_name.equalsIgnoreCase(getString(R.string.creditcard))) {
				payment_option_layouts.add(R.layout.credit_card_layout);
			}
		}*/
        for (int i=0;i<VIEWNAMES.size();i++) {
            /*if (TITLES.get(i).equalsIgnoreCase(getString(R.string.cash))) {
                payment_option_layouts.add(R.layout.cash_layout);
            } else if (TITLES.get(i).equalsIgnoreCase(getString(R.string.check))) {
                payment_option_layouts.add(R.layout.check_layout);
            } else if (TITLES.get(i).equalsIgnoreCase(getString(R.string.extrn_pay))) {
                payment_option_layouts.add(R.layout.external_gateway_layout);
            } else if (TITLES.get(i).equalsIgnoreCase(getString(R.string.creditcard))) {
                payment_option_layouts.add(R.layout.credit_card_layout);
            }*/
            if (TITLES.get(i).equalsIgnoreCase(getString(R.string.creditcard))) {
                payment_option_layouts.add(R.layout.selfcheckin_creditcard_layout);
            }
        }
        adapter = new MyPagerAdapter();
        view_pages.setAdapter(adapter);
        tabs.setViewPager(view_pages);
        view_pages.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                view_pages.setCurrentItem(position);
                tabs.notifyDataSetChanged();
                try {
                    if (TITLES.get(position).equalsIgnoreCase(getString(R.string.creditcard))) {
                        if (!isCardSwiped) {
                            if (myUniMagReader == null) {
                                umimugObj.onCreate(SelfcheckinPaymentActivity.this, true);
                            } else if (isReaderConnected&&myUniMagReader != null&&AppUtils.isMicrophonePermissionGranted(SelfcheckinPaymentActivity.this)) {
                                if (!isUnimagReaderConnected) {
                                    umimugObj.startAutoConfig();
                                } else {
                                    myUniMagReader.startSwipeCard();
                                }
                            }
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    public class MyPagerAdapter extends PagerAdapter {


        //private final String[] TITLES = { "Profile", "Change Password" };



        @Override
        public CharSequence getPageTitle(int position) {

            Locale l = Locale.getDefault();
            Drawable drawableIcon = null;
            return VIEWNAMES.get(position);
        }

        @Override
        public int getCount() {
            return TITLES.size();
        }

        @Override
        public Object instantiateItem(View collection, final int position) {

            View view = inflater.inflate(payment_option_layouts.get(position), null);

            if(TITLES.get(position).equalsIgnoreCase(getString(R.string.creditcard))){
                CreditCardView(view);
            }/*else if(TITLES.get(position).equalsIgnoreCase(getString(R.string.cash))){
                CashView(view);
            }else if(TITLES.get(position).equalsIgnoreCase(getString(R.string.check))){
                CheckView(view);
            }else if(TITLES.get(position).equalsIgnoreCase(getString(R.string.extrn_pay))){
                ExternalView(view);
            }*/


            ((ViewPager) collection).addView(view, 0);
            return view;
        }


        @Override
        public void destroyItem(View collection, int position, Object view) {
            ((ViewPager) collection).removeView((View) view);

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {

            return view == ((View) object);
        }
    }

    //Payment Gateways
    private void StripeCardToken(){
        params.clear();
        params.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.STRIPE_CARD_TOKEN_REQ_CODE));
        params.put("card[number]", cardNo.replace(" ", ""));
        params.put("card[exp_month]", card_exp_months);
        params.put("card[exp_year]", card_exp_years);
        params.put("card[cvc]", cvcNo);
        params.put("card[name]", buyer_info.getFirstName()+" "+buyer_info.getLastName());
        String access_token = sfdcddetails.token_type+" "+_event_payment_keys.get(0).PG_Pass_Secret__c;
        //String access_token = sfdcddetails.token_type+" "+_event_payment_keys.get(0).PG_User_Key__c;
        postMethod = new HttpPostData("Validating Card...", WebServiceUrls.STRIPE_TOKENT_URL, AppUtils.prepareRequest(params), access_token, SelfcheckinPaymentActivity.this);
        postMethod.execute();
    }
    private void StripeAdaptivePayment(String card_token){
        fillTicketPriceTotalIncludeBLFEE();
        params.clear();
        params.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.STRIPE_REQ_CODE));
        params.put("amount", String.valueOf(Math.round(stripe_total*100)+Math.round(service_tax*100)));
        params.put("application_fee", String.valueOf(Math.round(stripe_servicefee*100)));
        params.put("currency", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
        params.put("description", "ScanAttendee App for the event: "+checkedin_event_record.Events.Name+" "+order_name);
        params.put("card", card_token);
        String access_token = sfdcddetails.token_type+" "+_event_payment_keys.get(0).PG_Pass_Secret__c;
        postMethod = new HttpPostData("Connecting to stripe...", WebServiceUrls.STRIPE_PAYMENT, AppUtils.prepareRequest(params), access_token, SelfcheckinPaymentActivity.this);
        postMethod.execute();
    }
    private void StripeAdaptiveTokenRefresh(){
        stripekeys_evetndex = Util.db.getPay_Gateway_Key(" where "+ EventPayamentSettings.KEY_PAYGATEWAY_NAME+" = '"+getString(R.string.eventdex_stripe_keys)+"'").get(0);
        params.clear();
        params.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.STRIPE_REFRESH_TOKEN_CODE));
        params.put("grant_type", "refresh_token");
        params.put("client_secret", stripekeys_evetndex.PG_Pass_Secret__c);
        //params.put("refresh_token", _event_payment_keys.get(0).PG_User_Key__c);
        params.put("refresh_token", _event_payment_keys.get(0).PG_Signature__c);
        //String access_token = sfdcddetails.token_type+" "+_event_payment_keys.get(0).PG_User_Key__c;
        postMethod = new HttpPostData("Validating Card...", WebServiceUrls.STRIPE_AUTHORIZATION_URL, AppUtils.prepareRequest(params), null, SelfcheckinPaymentActivity.this);
        postMethod.execute();
    }

    private void AuthNetFeilds(){
        try {
            params.clear();
            params.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.AUTHORIZATION_REQ_CODE));
            params.put(PaymentConstants.X_DELIM_DATA, "TRUE");
            params.put(PaymentConstants.X_DELIM_CHAR, "$");
            params.put(PaymentConstants.X_RELAY_RESPONSE, "FALSE");
            params.put(PaymentConstants.X_URL, "FALSE");
            params.put(PaymentConstants.X_METHOD, "CC");
            params.put(PaymentConstants.X_TYPE, "AUTH_ONLY");
            params.put(PaymentConstants.X_LOGIN, _event_payment_keys.get(0).PG_User_Key__c);//TODO
            params.put(PaymentConstants.X_TRAN_KEY, _event_payment_keys.get(0).PG_Signature__c);//TODO
            params.put(PaymentConstants.X_DESCRIPTION, "ScanAttendee App for the event: " + checkedin_event_record.Events.Name + " " + order_name);
            params.put(PaymentConstants.X_VERSION, "3.1");

            //credit card details
            params.put(PaymentConstants.X_CARD_NUM, cardNo.replace(" ", "").trim());
            //Log.i("--------------------Month And Year-----------------",":"+card_exp_months+card_exp_years.substring(2).toString());
            params.put(PaymentConstants.X_EXP_DATE, card_exp_months + card_exp_years.substring(2).toString());
            params.put(PaymentConstants.X_AMOUNT, String.valueOf(total + servicefee + service_tax));
            params.put(PaymentConstants.X_PO_NUM, "");
            params.put(PaymentConstants.X_TAX, "");
            params.put(PaymentConstants.X_CARD_CODE, cvcNo);
            //address
            params.put(PaymentConstants.X_DEVICE_TYPE, "1");
            params.put(PaymentConstants.X_FIRST_NAME, NullChecker(buyer_info.getFirstName()));
            params.put(PaymentConstants.X_LAST_NAME, NullChecker(buyer_info.getLastName()));
            params.put(PaymentConstants.X_EMAIL, NullChecker(buyer_info.getEmail()));
            params.put(PaymentConstants.X_PHONE_NUMBER, NullChecker(buyer_info.getMobile()));
            params.put(PaymentConstants.X_ADDRESS, NullChecker(buyer_info.getBuyer_address1()));
            params.put(PaymentConstants.X_STATE, NullChecker(buyer_info.getBuyer_city()));
            params.put(PaymentConstants.X_RESPONSE_FORMAT, "1");
            params.put(PaymentConstants.X_ZIP, NullChecker(buyer_info.getBuyer_zipcode()));

            if (NullChecker(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF)).equalsIgnoreCase("PRODUCTION")) {
                authNet_Task = new AuthNetTask(WebServiceUrls.AUTHORIZATION_DOT_NET_URL_LIVE, params, SelfcheckinPaymentActivity.this);
            } else {
                authNet_Task = new AuthNetTask(WebServiceUrls.AUTHORIZATION_DOT_NET_URL, params, SelfcheckinPaymentActivity.this);
            }

            authNet_Task.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void PayPal_Payment(){
        params.clear();
        params.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.PAYPAL_REQ_CODE));
        params.put(PaymentConstants.USER, _event_payment_keys.get(0).PG_User_Key__c);
        params.put(PaymentConstants.PWD, _event_payment_keys.get(0).PG_Pass_Secret__c);
        params.put(PaymentConstants.SIGNATURE, _event_payment_keys.get(0).PG_Signature__c);
        params.put(PaymentConstants.METHOD, "DoDirectPayment");
        params.put(PaymentConstants.VERSION, "1");
        params.put(PaymentConstants.PAYMENTACTION, "Sale");
        params.put(PaymentConstants.AMT, String.valueOf(total+servicefee+service_tax));
        params.put(PaymentConstants.ACCT, cardNo.replace(" ", "").trim());
        params.put(PaymentConstants.CREDITCARDTYPE, "");
        params.put(PaymentConstants.CVV, cvcNo);
        params.put(PaymentConstants.FIRSTNAME, NullChecker(buyer_info.getFirstName()));
        params.put(PaymentConstants.LASTNAME, NullChecker(buyer_info.getLastName()));
        params.put(PaymentConstants.STREET, NullChecker(buyer_info.getBuyer_address1()));
        params.put(PaymentConstants.CITY, NullChecker(buyer_info.getBuyer_city()));
        params.put(PaymentConstants.STATE, buyer_info.buyer_state);
        params.put(PaymentConstants.ZIP, NullChecker(buyer_info.getBuyer_zipcode()));
        params.put(PaymentConstants.COUNTRYCODE, Util.db.getCountrySortName(checkedin_event_record.Events.BLN_Country__c));
        params.put(PaymentConstants.CURRENCYCODE, Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
        params.put(PaymentConstants.EXPDATE, card_exp_months+card_exp_years);

        if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")) {
            paypal_direct = new PaypalDirectTask(WebServiceUrls.PAYPAL_DIRECT_URL_LIVE, params, this);
        }else {
            paypal_direct = new PaypalDirectTask(WebServiceUrls.PAYPAL_DIRECT_URL, params, this);
        }
        paypal_direct.execute();
    }

    private void openPayPalInfoDialog() {
        final Dialog paypal_dialog = new Dialog(this);
        paypal_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        paypal_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        paypal_dialog.setContentView(R.layout.paypal_address_info);
        paypal_dialog.setCancelable(false);
        // Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = paypal_dialog.getWindow();
        lp.copyFrom(window.getAttributes());

        // This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        paypal_dialog.getWindow().setAttributes(lp);

        final EditText edt_street = (EditText)paypal_dialog.findViewById(R.id.edt_form_street);
        final EditText edt_city = (EditText)paypal_dialog.findViewById(R.id.edt_form_city);
        final EditText edt_state = (EditText)paypal_dialog.findViewById(R.id.edt_form_state);
        final EditText edt_zip = (EditText)paypal_dialog.findViewById(R.id.edt_form_zip);
        Button btn_pay = (Button)paypal_dialog.findViewById(R.id.btn_pay_pal);
        Button btn_close = (Button)paypal_dialog.findViewById(R.id.paypal_dialog_close);
		/*edt_city.setText(NullChecker(user_profile.Profile.City__c));
		edt_state.setText(NullChecker(user_profile.Profile.State__c));
		edt_zip.setText(NullChecker(user_profile.Profile.Zip_Code__c));*/
        btn_pay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String street = edt_street.getText().toString().trim();
                String city = edt_city.getText().toString().trim();
                String state = edt_state.getText().toString().trim();
                String zipcode = edt_zip.getText().toString().trim();
                if(street.isEmpty()){
                    edt_street.setError("Please enter street.");
                    edt_street.requestFocus();
                }else if(city.isEmpty()){
                    edt_city.setError("Please enter city.");
                    edt_city.requestFocus();
                }else if(state.isEmpty()){
                    edt_state.setError("Please enter state.");
                    edt_state.requestFocus();
                }else if(zipcode.isEmpty()){
                    edt_zip.setError("Please enter zipcode.");
                    edt_zip.requestFocus();
                }/*else if(zipcode.length() < 5){
					edt_zip.setError("Please enter valid zipcode.");
					edt_zip.requestFocus();
				}*/else{
                    paypal_dialog.dismiss();
                    buyer_info.setBuyer_address1(street);
                    buyer_info.setBuyer_city(city);
                    buyer_info.buyer_state = state;
                    buyer_info.setBuyer_zipcode(zipcode);
                    PayPal_Payment();
                }
            }
        });

        btn_close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                paypal_dialog.dismiss();
            }
        });
        paypal_dialog.show();
    }
    public void TrustcommercePayment(){
        params.clear();
        params.put(WebServiceUrls.WEBSERVICE_CODE, WebServiceUrls.TRUSTCOMMERCE_CODE+"");
        params.put("custid", _event_payment_keys.get(0).PG_User_Key__c);
        params.put("password", _event_payment_keys.get(0).PG_Signature__c);
        params.put("currency", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
        params.put("amount", String.valueOf(Math.round((total+service_tax+servicefee) * 100)));
        params.put("action", "sale");
        params.put("cc", cardNo.replace(" ", "").trim());
        params.put("exp", card_exp_months+card_exp_years.substring(2).toString());
        params.put("cvv", cvcNo+"");
        params.put("name", buyer_info.getFirstName()+" "+buyer_info.getLastName());
        params.put("email", buyer_info.getEmail());
        String event_name = checkedin_event_record.Events.Name.replaceAll("[^a-zA-Z0-9 -]", "");
        event_name = event_name.replace("&", "");
        if(event_name.length() > 20){
            event_name = event_name.substring(0, 20);
        }
        params.put("ticket", checkedin_event_record.Events.Name.replaceAll("[^a-zA-Z0-9 -]", ""));

        String url = WebServiceUrls.TRUSTCOMMERCE_URL;
        postMethod = new HttpPostData("Processing Payment...", url, AppUtils.prepareRequest(params), "", this);
        postMethod.execute();
    }

   /* private void citrusLoginUserCheck() {
        try {
            if (CitrusClient.getInstance(this) != null) {
                CitrusClient.getInstance(this).isUserSignedIn(new Callback<Boolean>() {
                    @Override
                    public void success(Boolean aBoolean) {
                        if (aBoolean) {
                            CitrusFlowManager.logoutUser(SelfcheckinPaymentActivity.this);
                        } else {

                        }
                    }

                    @Override
                    public void error(CitrusError citrusError) {

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(e, "");
            //  logoutBtn.setVisibility(View.GONE);
        }
    }*/

    //for PayTM
    public void paytmPayment(String mobile){
        try {
            PaytmPGService Service;
            if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
                Service = PaytmPGService.getProductionService();
            }else{
                Service = PaytmPGService.getProductionService();
                //Service = PaytmPGService.getStagingService();
            }

            Map<String, String> paramMap = new HashMap<String, String>();

            // these are mandatory parameters

            paytmTxnAmount = total+servicefee+service_tax+"";
            final String OrderID = initOrderId();
            paramMap.put("ORDER_ID", OrderID);
            paramMap.put("MID", _event_payment_keys.get(0).PG_User_Key__c);
            paramMap.put("CUST_ID", order_name+"-"+mobile+"-"+buyer_info.getEmail().trim());
            paramMap.put("CHANNEL_ID",_event_payment_keys.get(0).PP_Payment_Type__c);
            paramMap.put("INDUSTRY_TYPE_ID",  _event_payment_keys.get(0).PP_Fee_Payer__c);
            paramMap.put("WEBSITE", _event_payment_keys.get(0).PG_Signature__c);
            paramMap.put("TXN_AMOUNT", paytmTxnAmount);
            paramMap.put("THEME", "merchant");
            paramMap.put("EMAIL", buyer_info.getEmail().trim());
            paramMap.put("MOBILE_NO", mobile);
            PaytmOrder Order = new PaytmOrder(paramMap);

            PaytmMerchant Merchant = new PaytmMerchant(_event_payment_keys.get(0).citrus_param__c,_event_payment_keys.get(0).Service_Fee__c);
					/*"https://eventdex.com/paytm-mobile-live/generateChecksum.php",
					"https://eventdex.com/paytm-mobile-live/verifyChecksum.php");*/

            Service.initialize(Order, Merchant, null);

            Service.startPaymentTransaction(this, true, true,
                    new PaytmPaymentTransactionCallback() {
                        @Override
                        public void someUIErrorOccurred(String inErrorMessage) {
                            // Some UI Error Occurred in Payment Gateway Activity.
                            // // This may be due to initialization of views in
                            // Payment Gateway Activity or may be due to //
                            // initialization of webview. // Error Message details
                            // the error occurred.
                        }

                        @Override
                        public void onTransactionSuccess(Bundle inResponse) {
                            // After successful transaction this method gets called.
                            // // Response bundle contains the merchant response
                            // parameters.
                            // sample response in success	Bundle[{STATUS=TXN_SUCCESS, BANKNAME=INDUSIND BANK LIMITED, ORDERID=ORDER200009983, TXNAMOUNT=50.00, TXNDATE=2017-02-13 19:27:21.0, MID=Global15892989980511, TXNID=32280477, RESPCODE=01, PAYMENTMODE=CC, BANKTXNID=201702132916324, CURRENCY=INR, GATEWAYNAME=ICICI, IS_CHECKSUM_VALID=Y, RESPMSG=Txn Successful.}]
                            brand = "";//Visa or master paytm not providing
                            card_last_4 = "";
                            transaction_id = inResponse.getString("BANKTXNID");
                            Gson paytmGson = new Gson();
                            PaytmStatusCheckGson objPy = new PaytmStatusCheckGson();
                            objPy.MID = _event_payment_keys.get(0).PG_User_Key__c;
                            objPy.ORDERID = OrderID;
                            String payJson = paytmGson.toJson(objPy);
                            try {
                                payJson = URLEncoder.encode(payJson, "UTF-8");
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            if(isOnline()){
                                if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
                                    postMethod = new HttpPostData("Checking your payment with Paytm...","https://secure.paytm.in/oltp/HANDLER_INTERNAL/TXNSTATUS?JsonData="+payJson,"", "", SelfcheckinPaymentActivity.this);
                                }else{
                                    postMethod = new HttpPostData("Checking your payment with Paytm...","https://secure.paytm.in/oltp/HANDLER_INTERNAL/TXNSTATUS?JsonData="+payJson,"", "", SelfcheckinPaymentActivity.this);

                                    //postMethod = new HttpPostData("Checking your payment with Paytm...","https://pguat.paytm.com/oltp/HANDLER_INTERNAL/TXNSTATUS?JsonData="+payJson,"", "", SelfcheckinPaymentActivity.this);
                                }

                                postMethod.execute();
                            }

                            //doRequest();
                            AppUtils.displayLog("LOG", "Payment Transaction is successful " + inResponse);
                            Toast.makeText(getApplicationContext(), "Payment Transaction is successful ", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onTransactionFailure(String inErrorMessage,
                                                         Bundle inResponse) {
                            // This method gets called if transaction failed. //
                            // Here in this case transaction is completed, but with
                            // a failure. // Error Message describes the reason for
                            // failure. // Response bundle contains the merchant
                            // response parameters.

                            AppUtils.displayLog("LOG", "Payment Transaction Failed " + inErrorMessage);
                            Toast.makeText(getBaseContext(), "Payment Transaction Failed ", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void networkNotAvailable() { // If network is not
                            // available, then this
                            // method gets called.
                            Toast.makeText(getBaseContext(), "Network NotAvailable ", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void clientAuthenticationFailed(String inErrorMessage) {
                            // This method gets called if client authentication
                            // failed. // Failure may be due to following reasons //
                            // 1. Server error or downtime. // 2. Server unable to
                            // generate checksum or checksum response is not in
                            // proper format. // 3. Server failed to authenticate
                            // that client. That is value of payt_STATUS is 2. //
                            // Error Message describes the reason for failure.
                            Toast.makeText(getBaseContext(), "Client Authentication Failed ", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onErrorLoadingWebPage(int iniErrorCode,
                                                          String inErrorMessage, String inFailingUrl) {

                        }

                        // had to be added: NOTE
                        @Override
                        public void onBackPressedCancelTransaction() {
                            Toast.makeText(getBaseContext(), "Canceled Transaction by user ", Toast.LENGTH_LONG).show();
                        }

                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private String initOrderId() {
        Random r = new Random(System.currentTimeMillis());
        String orderId = "ORDER" + (1 + r.nextInt(2)) * 10000
                + r.nextInt(10000);
        return orderId;
    }


    //For citrus
   /* public void citrusPayment(String mobile){

        try {

            CitrusFlowManager.startShoppingFlowStyle(SelfcheckinPaymentActivity.this,
                    buyer_info.getEmail().trim(), mobile, total+servicefee+service_tax+"", R.style
                            .AppTheme_Green, false);

        }catch (Exception e){
            e.printStackTrace();;
        }

    }
*/
    private void fillTicketPriceTotalIncludeBLFEE(){
        stripe_total = 0.0;stripe_servicefee = 0.0;
        for(ItemTypeController item : items_list){
            if(Boolean.parseBoolean(item.getItemFeeSetting()) && (item.getItemPaidType().equalsIgnoreCase("Paid") || item.getItemPaidType().equalsIgnoreCase("Donation"))){
                stripe_servicefee = (Double.parseDouble(item.getBL_Fee()) * item.getItemQuantity()) + stripe_servicefee;
                //changed  total for credit
                stripe_total = (item.getItemPrice() * item.getItemQuantity()) + stripe_total+(Double.parseDouble(item.getBL_Fee()) * item.getItemQuantity());
            }else if(!Boolean.parseBoolean(item.getItemFeeSetting()) && (item.getItemPaidType().equalsIgnoreCase("Paid") || item.getItemPaidType().equalsIgnoreCase("Donation"))) {
				/*if (item.getItemTypeName().equalsIgnoreCase("Package")) {
					Double includefee =Util.db.getTotalItemIncludeFee(item.getItemPoolId());
					stripe_total = ((item.getItemPrice() - includefee) * item.getItemQuantity()) + stripe_total;
					stripe_servicefee = (includefee * item.getItemQuantity()) + stripe_servicefee;
				} else {*/
                //For ItemFee
                stripe_total = (item.getItemPrice()  * item.getItemQuantity()) + stripe_total;
                //stripe_total = ((item.getItemPrice() - Double.parseDouble(item.getBL_Fee())) * item.getItemQuantity()) + stripe_total;
                stripe_servicefee = (Double.parseDouble(item.getBL_Fee()) * item.getItemQuantity()) + stripe_servicefee; //}
            }
            //item.getOrderLineItems().get()
				/*stripe_total = ((item.getItemPrice() - Double.parseDouble(item.getBL_Fee())) * item.getItemQuantity()) + stripe_total;
				stripe_servicefee = (Double.parseDouble(item.getBL_Fee()) * item.getItemQuantity()) + stripe_servicefee;
		*/

        }

    }


    private void CashView(View view) {

        final EditText edit_cash_amt = (EditText) view.findViewById(R.id.editcashamt);
        Button btn_cash_pay = (Button) view.findViewById(R.id.btncashpaynow);
        final TextView txt_cash_error = (TextView) view.findViewById(R.id.txtcasherror);
        edit_cash_amt.setTypeface(Util.roboto_regular);
        btn_cash_pay.setTypeface(Util.roboto_regular);
        txt_cash_error.setTypeface(Util.roboto_regular);
        // btn_cash_pay.setFocusable(false);

        edit_cash_amt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                txt_cash_error.setVisibility(View.GONE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_cash_pay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Util.NullChecker(edit_cash_amt.getText().toString()).isEmpty()) {

                    if (Double.parseDouble(edit_cash_amt.getText().toString().trim()) >= (total + service_tax + servicefee)) {
                        entered_cash_amount = Double.parseDouble(edit_cash_amt.getText().toString().trim());
                        request_type = Util.CASH;
                        //total_entered_amount = edit_cash_amt.getText().toString().trim();
                        if (isOnline()) {
                            doRequest();
                        } else {
                            txt_cash_error.setVisibility(View.VISIBLE);
                            txt_cash_error.setText("Please check your Internet connection.");
                        }
                    } else {
                        txt_cash_error.setVisibility(View.VISIBLE);
                        txt_cash_error.setText("Entered Amount should not be less then Total amount.");
                        edit_cash_amt.startAnimation(shake);
                    }
                } else {
                    txt_cash_error.setVisibility(View.VISIBLE);
                    txt_cash_error.setText("Please Enter The Amount.");
                    edit_cash_amt.startAnimation(shake);
                }
            }

        });
    }


    private void CreditCardView(View v) {
        final Spinner spnr_month = (Spinner) v.findViewById(R.id.spnrexpmonth);
        final Spinner spnr_year = (Spinner) v.findViewById(R.id.spnrexpyear);
        final EditCard edit_cardnum = (EditCard) v.findViewById(R.id.editcardnum);
        final EditText edit_cvv = (EditText) v.findViewById(R.id.editcvv);
        ScrollView ccView = (ScrollView) v.findViewById(R.id.event_cc_scrollview);
        final FrameLayout indianGateWay = (FrameLayout) v.findViewById(R.id.btnframeindiangateway);
        final EditText edt_att_phone =(EditText) v.findViewById(R.id.edt_att_phone);
        final RadioButton radio_indiangateway = (RadioButton) v.findViewById(R.id.radio_indiangateway);
        final Button btncheckpaynow = (Button) v.findViewById(R.id.btncheckpaynow);
        Button btnscancard=(Button) v.findViewById(R.id.btnscancard);
        Button btn_credit_pay = (Button) v.findViewById(R.id.btncreditpay);
        Button btn_swipeCard=(Button)v.findViewById(R.id.btn_swipeCard);
        btn_credit_pay.setTypeface(Util.roboto_regular);
        edit_cardnum.setTypeface(Util.roboto_regular);
        edit_cvv.setTypeface(Util.roboto_regular);
        _event_card_pgateway.moveToFirst();
        final String payment_setting_name = Util.db.getPayment_Type_Name(_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID)));

        if(payment_setting_name.equalsIgnoreCase(getString(R.string.citrus_direct)) || payment_setting_name.equalsIgnoreCase(getString(R.string.paytm_direct))){
            ccView.setVisibility(View.GONE);
            indianGateWay.setVisibility(View.VISIBLE);
            attendeePhoneForCitrus = buyer_info.getMobile();
            if(attendeePhoneForCitrus !=null && attendeePhoneForCitrus.length()>0){
                attendeePhoneForCitrus = attendeePhoneForCitrus.replace("(","");
                attendeePhoneForCitrus = attendeePhoneForCitrus.replace(")","");
                attendeePhoneForCitrus = attendeePhoneForCitrus.replace("-","");
                attendeePhoneForCitrus = attendeePhoneForCitrus.replace(" ","");
            }
            edt_att_phone.setText(attendeePhoneForCitrus);
            if(payment_setting_name.equalsIgnoreCase(getString(R.string.paytm_direct))){
                radio_indiangateway.setText("Paytm");

            }/*else if(payment_setting_name.equalsIgnoreCase(getString(R.string.citrus_direct))){
                if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
                    CitrusFlowManager.initCitrusConfig(_event_payment_keys.get(0).PP_Fee_Payer__c,
                            _event_payment_keys.get(0).PP_Payment_Type__c, _event_payment_keys.get(0).citrus_param__c,
                            _event_payment_keys.get(0).Service_Fee__c, ContextCompat.getColor(this, R.color.white),
                            SelfcheckinPaymentActivity.this, Environment.PRODUCTION, _event_payment_keys.get(0).PG_Signature__c, "https://eventdex.com/citrus-mobile-live/bill_production.php?access_key="+_event_payment_keys.get(0).PG_User_Key__c+"&secret_key="+_event_payment_keys.get(0).PG_Pass_Secret__c,
                            "https://eventdex.com/citrus-mobile-live/redirectUrlLoadCash.php", false);
                }else{
                    CitrusFlowManager.initCitrusConfig(_event_payment_keys.get(0).PP_Fee_Payer__c,
                            _event_payment_keys.get(0).PP_Payment_Type__c, _event_payment_keys.get(0).citrus_param__c,
                            _event_payment_keys.get(0).Service_Fee__c, ContextCompat.getColor(this, R.color.white),
                            SelfcheckinPaymentActivity.this, Environment.PRODUCTION, _event_payment_keys.get(0).PG_Signature__c, "https://eventdex.com/citrus-mobile-live/bill_production.php?access_key="+_event_payment_keys.get(0).PG_User_Key__c+"&secret_key="+_event_payment_keys.get(0).PG_Pass_Secret__c,
                            "https://eventdex.com/citrus-mobile-live/redirectUrlLoadCash.php", false);
					*//*CitrusFlowManager.initCitrusConfig(_event_payment_keys.get(0).PP_Fee_Payer__c,
							_event_payment_keys.get(0).PP_Payment_Type__c, _event_payment_keys.get(0).citrus_param__c,
							_event_payment_keys.get(0).Service_Fee__c, ContextCompat.getColor(this, R.color.white),
							SelfcheckinPaymentActivity.this, Environment.SANDBOX, _event_payment_keys.get(0).PG_Signature__c, "https://eventdex.com/citrus-mobile/bill_sanbox.php?access_key="+_event_payment_keys.get(0).PG_User_Key__c+"&secret_key="+_event_payment_keys.get(0).PG_Pass_Secret__c,
							"https://eventdex.com/citrus-mobile-live/redirectUrlLoadCash.php", true);*//*
                }


                citrusLoginUserCheck();
            }*/

            btncheckpaynow.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    attendeePhoneForCitrus = edt_att_phone.getText().toString();
                    if(attendeePhoneForCitrus !=null && attendeePhoneForCitrus.length()>0){
                        if(payment_setting_name.equalsIgnoreCase(getString(R.string.paytm_direct))){
                            paytmPayment(attendeePhoneForCitrus);
                        }/*else if(payment_setting_name.equalsIgnoreCase(getString(R.string.citrus_direct))){
                            citrusPayment(attendeePhoneForCitrus);
                        }*/

                    }else{
                        Toast.makeText(SelfcheckinPaymentActivity.this,"Please Enter mobile number",Toast.LENGTH_LONG).show();
                    }

                }
            });


        }

        spnr_month.setAdapter(new ArrayAdapter<String>(this,
                R.layout.spinner_item_layout, exp_month) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(getResources().getColor(
                        R.color.black));
                ((TextView) v).setTypeface(Util.roboto_regular);
                return v;
            }

        });
        spnr_year.setAdapter(new ArrayAdapter<String>(this,
                R.layout.spinner_item_layout, exp_years) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(getResources().getColor(
                        R.color.black));
                ((TextView) v).setTypeface(Util.roboto_regular);
                return v;
            }

        });

        spnr_month.setEnabled(true);
        spnr_year.setEnabled(true);
        edit_cvv.setEnabled(true);
        edit_cardnum.setEnabled(true);
        edit_cvv.setText("");
        edit_cardnum.setText("");
        spnr_month.setSelection(0);
        spnr_year.setSelection(0);
        edit_cardnum.setText(scanCardNumber.toString());
        edit_cvv.setText(scanCCV);
        spnr_month.setSelection(scanExpMonth);
        spnr_year.setSelection(getSelectedPosition(exp_years,String.valueOf(scanExpYear)));

        btnscancard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (AppUtils.isCamPermissionGranted(SelfcheckinPaymentActivity.this)) {
                        onScan();
                    } else {
                        AppUtils.giveCampermission(SelfcheckinPaymentActivity.this);
                    }
                } catch (Exception e) {
                    AppUtils.giveCampermission(SelfcheckinPaymentActivity.this);
                    e.printStackTrace();
                }
            }
        });

        btn_swipeCard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AppUtils.isMicrophonePermissionGranted(SelfcheckinPaymentActivity.this)) {
                    if (myUniMagReader == null) {
                        umimugObj.onCreate(SelfcheckinPaymentActivity.this, true);
                    } else if (isReaderConnected&&myUniMagReader != null) {
                        if (!isUnimagReaderConnected&&isReaderConnected) {
                            umimugObj.startAutoConfig();
                        } else {
                            myUniMagReader.startSwipeCard();
                        }
                    } else {
                        showMessageAlert("No External card reader is connected \n Please Connect and try again!",false);
                       // AppUtils.showError(SelfcheckinPaymentActivity.this, "Please reconnect reader");
                    }
                }else{
                    AppUtils.giveMicrophonepermission(SelfcheckinPaymentActivity.this);
                }

            }
        });
        btn_credit_pay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                cardNo = edit_cardnum.getCardNumber().trim();
                cvcNo = edit_cvv.getText().toString();
                card_exp_months = (String) spnr_month.getSelectedItem();
                card_exp_years = (String) spnr_year.getSelectedItem();

                if(card_exp_months.equals("MM"))
                    card_exp_months="0";
                if(card_exp_years.equals("YYYY"))
                    card_exp_years="0";

                //Log.i(card_exp_years + "Selected Expiry Month and Year", ":"+ card_exp_months);
                card = new Card(cardNo, Integer.valueOf(card_exp_months), Integer.valueOf(card_exp_years), cvcNo,
                        buyer_info.getFirstName()+" "+buyer_info.getLastName());

                if (cardNo.isEmpty() || !card.validateNumber()) {
                    edit_cardnum.setError(getResources().getString(R.string.cardnumalert));
                    edit_cardnum.requestFocus();
                    edit_cardnum.startAnimation(shake);
                } else if (card_exp_months.equals("0") || !card.validateExpMonth()) {
                    spnr_month.startAnimation(shake);
                } else if (card_exp_years.equals("0") || !card.validateExpYear()) {
                    spnr_year.startAnimation(shake);
                } else if (cvcNo.length() < 3 || !card.validateCVC()) {
                    edit_cvv.setError(getResources().getString(R.string.cvvalert));
                    edit_cvv.requestFocus();
                    edit_cvv.startAnimation(shake);
                }else if(!card.validateCard()){
                    startErrorAnimation("Please Enter Valid Card Details.", txt_error_msg);
                }else if( _event_payment_keys.size() == 0){
                    showMessageAlert("Sorry no credentials found for your payment, Please add your credentials in payment settings!",false);
                }else {
                    if (isOnline()) {
                        _event_card_pgateway.moveToFirst();
                        //String payment_setting_name = _event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PAY_NAME));
                        String payment_setting_name = Util.db.getPayment_Type_Name(_event_card_pgateway.getString(_event_card_pgateway.getColumnIndex(EventPayamentSettings.EVENT_PGATEWAY_ID)));
                        //Log.i("-----------Pyamrnt Gateway Name---------", ":"+payment_setting_name);
                        if(payment_setting_name.equalsIgnoreCase(getString(R.string.stripe_direct))){
                            saveCreditCard();
                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.auth_net_direct))){
                            AuthNetFeilds();
                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.stripe_adaptive))){
                            StripeCardToken();
                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.paypal_direct))){
                            //PayPal_Payment();
                            openPayPalInfoDialog();
                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.trust_commerce_direct))){
                            TrustcommercePayment();
                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.paytm_direct))){

                        }else if(payment_setting_name.equalsIgnoreCase(getString(R.string.citrus_direct))){
                            //citrusPayment();
                        }
                    } else {
                        startErrorAnimation(
                                getResources()
                                        .getString(R.string.network_error),
                                txt_error_msg);


                    }

                }
            }
        });
    }
    public void onSwipMessageReceived(String data) {
        isCardSwiped=true;
        AppUtils.displayLog("---data---",' '+data);
        BankCardMagneticTrack track = BankCardMagneticTrack.from(data);
        Log.i("----Track Data ----"," "+track);
        String str=track.getTrack1().getAccountNumber().getAccountNumber()+" "+track.getTrack1().getName().getFullName()+" "+track.getTrack1().getExpirationDate().getExpirationDate()+" "+track.getTrack1().getServiceCode().getServiceCode();;
        Log.i("----Account data----"," "+str);
        //openTrackDialog(track,data);
        if(!AppUtils.NullChecker(track.getTrack1().getAccountNumber().getAccountNumber()).isEmpty()) {
            doSwipPayment(track.getTrack1().getAccountNumber().getAccountNumber(), track.getTrack1().getExpirationDate().getExpirationDate().getMonth().getValue(), track.getTrack1().getExpirationDate().getExpirationDate().getYear());
        }else{
            AppUtils.showError(this,"Please Swipe Again");
        }
        //doSwipPayment("32323232323232323",6,2021);

    }

    public void doSwipPayment(String mcardNo,int mcard_exp_months,int mcard_exp_years) {
        scanCardNumber=formatCardNumber("-",mcardNo.replace(" ",""),4);
        if(scanCardNumber.substring(scanCardNumber.length()-1,scanCardNumber.length()).equals("-")) {
            scanCardNumber=scanCardNumber.substring(0,scanCardNumber.length()-1);
        }
        scanExpMonth=mcard_exp_months;
        scanExpYear=mcard_exp_years;
        scanCCV="";
        Log.i("----Account data----"," "+mcardNo+" "+scanCardNumber+" "+scanExpMonth+" /"+scanExpYear);
        int pagerPosition=view_pages.getCurrentItem();
        adapter = new MyPagerAdapter();
        view_pages.setAdapter(adapter);
        tabs.setViewPager(view_pages);
        view_pages.setCurrentItem(pagerPosition);
    }


    public void saveCreditCard() {

        Card card = new Card(cardNo,
                (Integer) Integer.parseInt(card_exp_months),
                (Integer) Integer.parseInt(card_exp_years), cvcNo, buyer_info.getFirstName()+" "+buyer_info.getLastName());
        boolean validation = card.validateCard();

        //Log.i("-----------------IS CARD VALID----", ":" + validation);
        if (validation) {
            progress_dialog.show();
            progress_dialog.setCancelable(false);
            //Log.i("--------------Publish Key-----------",":"+_event_payment_keys.get(0).PG_User_Key__c);
            new Stripe().createToken(card, _event_payment_keys.get(0).PG_User_Key__c,new TokenCallback() {
                @Override
                public void onSuccess(com.globalnest.stripe.android.model.Token token) {
                    // TODO Auto-generated method stub
                    String token_id = token.getId();
                    //Card card = token.getCard();
                    new docreatecustumer().execute(token_id);

                    //new doPayment().execute(token_id);
                }
                public void onError(Exception error) {
                    if ((progress_dialog != null) && progress_dialog.isShowing()){
                        progress_dialog.dismiss();
                    }
                    startErrorAnimation(error.getMessage(), txt_error_msg);
                    error.printStackTrace();
                }
            });

        } else {
            startErrorAnimation("Please Enter Valid Card Number.", txt_error_msg);
        }

    }
    private void CheckView(final View view) {

        final EditText edit_check_num = (EditText) view.findViewById(R.id.editcheknum);
        Button btn_check_pay = (Button) view.findViewById(R.id.btncheckpaynow);
        final TextView txt_check_error = (TextView) view.findViewById(R.id.txtcheckerror);
        //final RadioButton radio_fullypaid = (RadioButton) view.findViewById(R.id.radio_fullpaid);
        //final RadioButton radio_checknotrevieve = (RadioButton)view.findViewById(R.id.radio_checknotrecieve);
        final RadioGroup radio_group = (RadioGroup) view.findViewById(R.id.radiogroup_orderstatus);
        txt_check_error.setTypeface(Util.roboto_regular);
        btn_check_pay.setTypeface(Util.roboto_regular);
        edit_check_num.setTypeface(Util.roboto_regular);
        edit_check_num.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {
                txt_check_error.setVisibility(View.GONE);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_check_pay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if(!isOnline()){
                    startErrorAnimation("Please check internet connection.", txt_error_msg);
                }else if(edit_check_num.getText().toString().isEmpty()){
                    txt_check_error.setVisibility(View.VISIBLE);
                    txt_check_error.setText("Enter check number.");
                    edit_check_num.startAnimation(shake);
                }else if(radio_group.getCheckedRadioButtonId() == -1){
                    txt_check_error.setVisibility(View.VISIBLE);
                    txt_check_error.setText("Please select order status.");
                    radio_group.startAnimation(shake);
                }else{
                    request_type = Util.CHECK;
                    check_number = edit_check_num.getText().toString().trim();
                    RadioButton radio_orderstatus= (RadioButton) view.findViewById(radio_group.getCheckedRadioButtonId());
                    order_status = radio_orderstatus.getText().toString().trim();
                    doRequest();
                }

            }
        });

    }

    private void ExternalView(View view){
        EditText edt_notes = (EditText)view.findViewById(R.id.edt_external_notes);
        edt_notes.setTypeface(Util.roboto_regular);
        Button btn_external_pay = (Button)view.findViewById(R.id.btn_external_pay);

        btn_external_pay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                request_type = Util.EXTERNAL_PAY;
                if(isOnline()){
                    doRequest();
                }else{
                    startErrorAnimation("Please check your internet connection.", txt_error_msg);
                }
            }
        });
    }
    private class docreatecustumer extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            if ((progress_dialog != null) && !progress_dialog.isShowing()) {
                progress_dialog.show();
                /* progress_dialog.setCancelable(true);*/
            }
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            Customer c=null;
            try {
                //Stripe.apiKey = "sk_test_cT9zTRnILXKy40xIkWb4tr2I";

                Map<String, Object> customerParams = new HashMap<String, Object>();
                customerParams.put("description", "ScanAttendee App for the event: "+checkedin_event_record.Events.Name+" "+order_name);
                customerParams.put("email", buyer_info.getEmail());
                //customerParams.put("card",params[0]);
                customerParams.put("source",params[0]);
                c=Customer.create(customerParams, _event_payment_keys.get(0).PG_Pass_Secret__c);
                if(!c.getId().isEmpty()){
                    custumer_id = String.valueOf(c.getId());
                    new Stripe().createToken(card, _event_payment_keys.get(0).PG_User_Key__c,new TokenCallback() {
                        @Override
                        public void onSuccess(com.globalnest.stripe.android.model.Token token) {
                            // TODO Auto-generated method stub
                            String token_id = token.getId();
                            pay_id = "";
                            pay_errorcode = 0;
                            new doPayment().execute(token_id);
                        }
                        public void onError(Exception error) {
                            if ((progress_dialog != null) && progress_dialog.isShowing()){
                                progress_dialog.dismiss();
                            }
                            startErrorAnimation(error.getMessage(), txt_error_msg);
                            error.printStackTrace();
                        }
                    });

                }else{
                    startErrorAnimation(c.getId(), txt_error_msg);
                }

                // transferCustomer("sk_test_WJPhx2sFtohYsmoyRrFlWVZ2");
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (InvalidRequestException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIConnectionException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (CardException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            }

            //Log.i("----------------Exception-------------",":"+pay_id);

            return pay_id;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress_dialog.dismiss();
			/*if(Boolean.valueOf(result)){
				if (isOnline()) {
					card_last_4 = cardNo.substring(cardNo.length()-4, cardNo.length());
					doRequest();
				} else {
					startErrorAnimation("Please check your internet connection.", txt_error_msg);
				}
			}else{
				startErrorAnimation(result, txt_error_msg);
			}*/
        }
    }
    private class doPayment extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            //progress_dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String pay_id = "";
            Charge c=null;

            HashMap<String, Object> chargeParams = new HashMap<String, Object>();
            //			chargeParams.put("amount", String.valueOf(Math.round(total * 100)+Math.round(servicefee*100)+Math.round(service_tax*100)));
            chargeParams.put("amount", String.valueOf(Math.round(total * 100)+Math.round(servicefee*100)+Math.round(service_tax*100)));
            chargeParams.put("currency", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
            //chargeParams.put("card", params[0]); // obtained with Stripe.js
            chargeParams.put("description", "ScanAttendee App for the event: "+checkedin_event_record.Events.Name+" "+order_name);
            chargeParams.put("receipt_email",buyer_info.getEmail());
            chargeParams.put("customer",NullChecker(custumer_id));
            try {
                RequestOptions options = RequestOptions
                        .builder()
                        .setIdempotencyKey(order_id).setApiKey(_event_payment_keys.get(0).PG_Pass_Secret__c)
                        .build();
                c = Charge.create(chargeParams, options);
                // c = Charge.create(chargeParams, _event_payment_keys.get(0).PG_Pass_Secret__c);
                //c = Charge.create(chargeParams, "sk_test_QJloTueyPZo6RYUyIJlhK2N1");

               /* for (int i=0;i<=2;i++){//for testing to avoid multiple deductions
                    c = Charge.create(chargeParams, options);
                }*/
                if(c.getPaid() && !c.getId().isEmpty()){
                    transaction_id = c.getId();
                }
                pay_id = String.valueOf(c.getPaid());
                // transferCustomer("sk_test_WJPhx2sFtohYsmoyRrFlWVZ2");
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (InvalidRequestException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIConnectionException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (CardException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());

            } catch (APIException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            }

            //Log.i("----------------Exception-------------",":"+pay_id);

            return pay_id;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                if ((progress_dialog != null) && progress_dialog.isShowing()) {
                    progress_dialog.dismiss();
                }
                if (Boolean.valueOf(result)) {
                    if (isOnline()) {
                        card_last_4 = cardNo.substring(cardNo.length() - 4, cardNo.length());
                        doRequest();
                    } else {
                        startErrorAnimation("Please check your internet connection.", txt_error_msg);
                    }
                } else {
                    startErrorAnimation(result, txt_error_msg);
                }
            }catch (Exception e){
                if ((progress_dialog != null) && progress_dialog.isShowing()) {
                    progress_dialog.dismiss();
                }
            }
        }
    }
    private class doStripeAdaptivecustumer extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            progress_dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            Customer c=null;
            try {
                //Stripe.apiKey = "sk_test_cT9zTRnILXKy40xIkWb4tr2I";

                Map<String, Object> customerParams = new HashMap<String, Object>();
                customerParams.put("description", "ScanAttendee App for the event: "+checkedin_event_record.Events.Name+" "+order_name);
                customerParams.put("email", buyer_info.getEmail());
                //customerParams.put("card",params[0]);
                customerParams.put("source",params[0]);
                c=Customer.create(customerParams, _event_payment_keys.get(0).PG_Pass_Secret__c);
                if(!c.getId().isEmpty()){
                    custumer_id = String.valueOf(c.getId());
                    new Stripe().createToken(card, _event_payment_keys.get(0).PG_User_Key__c,new TokenCallback() {
                        @Override
                        public void onSuccess(com.globalnest.stripe.android.model.Token token) {
                            // TODO Auto-generated method stub
                            String token_id = token.getId();
                            pay_id = "";
                            pay_errorcode = 0;
                            new doStripeAdaptivePayment().execute(token_id);
                            //StripeAdaptivePayment(token_id,custumer_id);
                        }
                        public void onError(Exception error) {
                            if ((progress_dialog != null) && progress_dialog.isShowing()){
                                progress_dialog.dismiss();
                            }
                            StripeAdaptiveTokenRefresh();
                            startErrorAnimation(error.getMessage(), txt_error_msg);
                            error.printStackTrace();
                        }
                    });

                }

                // transferCustomer("sk_test_WJPhx2sFtohYsmoyRrFlWVZ2");
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());

            } catch (InvalidRequestException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIConnectionException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (CardException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                pay_errorcode = e.getStatusCode();
                paymenterror(e.getMessage(),e.getStatusCode());
            }

            //Log.i("----------------Exception-------------",":"+pay_id);

            return pay_id;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progress_dialog.dismiss();
			/*if(Boolean.valueOf(result)){
				if (isOnline()) {
					card_last_4 = cardNo.substring(cardNo.length()-4, cardNo.length());
					doRequest();
				} else {
					startErrorAnimation("Please check your internet connection.", txt_error_msg);
				}
			}else{
				startErrorAnimation(result, txt_error_msg);
			}*/
        }
    }

    private class doStripeAdaptivePayment extends AsyncTask<String, Void, String> {

        protected void onPreExecute() {
            //progress_dialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            //String pay_id = "";
            Charge c=null;
            fillTicketPriceTotalIncludeBLFEE();
            HashMap<String, Object> chargeParams = new HashMap<String, Object>();
            //			chargeParams.put("amount", String.valueOf(Math.round(total * 100)+Math.round(servicefee*100)+Math.round(service_tax*100)));
            //chargeParams.put(WebServiceUrls.WEBSERVICE_CODE, String.valueOf(WebServiceUrls.STRIPE_REQ_CODE));
            chargeParams.put("amount", String.valueOf(Math.round(stripe_total*100)+Math.round(service_tax*100)));
            chargeParams.put("application_fee", String.valueOf(Math.round(stripe_servicefee*100)));
            chargeParams.put("currency", Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Name);
            chargeParams.put("description", "ScanAttendee App for the event: "+checkedin_event_record.Events.Name+" "+order_name);
            //chargeParams.put("card", card_token);
            chargeParams.put("receipt_email",buyer_info.getEmail());
            chargeParams.put("customer",NullChecker(custumer_id));
            try {
                RequestOptions options = RequestOptions
                        .builder()
                        .setIdempotencyKey(order_id).setApiKey(_event_payment_keys.get(0).PG_Pass_Secret__c)
                        .build();
                c = Charge.create(chargeParams, options);
                // c = Charge.create(chargeParams, _event_payment_keys.get(0).PG_Pass_Secret__c);
                //c = Charge.create(chargeParams, "sk_test_QJloTueyPZo6RYUyIJlhK2N1");

                /*for (int i=0;i<=2;i++){//for testing to avoid multiple deductions
                    c = Charge.create(chargeParams, options);
                }*/
                if(c.getPaid() && !c.getId().isEmpty()){
                    transaction_id = c.getId();
                }
                pay_id = String.valueOf(c.getPaid());
                // transferCustomer("sk_test_WJPhx2sFtohYsmoyRrFlWVZ2");
            } catch (AuthenticationException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (InvalidRequestException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (APIConnectionException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            } catch (CardException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());

            } catch (APIException e) {
                // TODO Auto-generated catch block
                pay_id = e.getMessage();
                paymenterror(e.getMessage(),e.getStatusCode());
            }

            //Log.i("----------------Exception-------------",":"+pay_id);

            return pay_id;
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if ((progress_dialog != null) && progress_dialog.isShowing()){
                progress_dialog.dismiss();
            }
            if(Boolean.valueOf(result)){
                if (isOnline()) {
                    card_last_4 = cardNo.substring(cardNo.length()-4, cardNo.length());
                    doRequest();
                } else {
                    startErrorAnimation("Please check your internet connection.", txt_error_msg);
                }
            }else{
                startErrorAnimation(result, txt_error_msg);
            }
        }
    }
    private void paymenterror(String errormsg,int errorcode){
        pay_id = errormsg;
        pay_errorcode = errorcode;
        if ((progress_dialog != null) && progress_dialog.isShowing()){
            progress_dialog.dismiss();
        }
        (SelfcheckinPaymentActivity.this).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                requestType = WebServiceUrls.SA_PAYMENT_UPDATE;
                doRequest();
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == TrustCommerceWebView.TRUST_COMMERCE_REQUEST_CODE) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                String result = bundle.getString(TrustCommerceWebView.TRUST_COMMERCE_RESULT);
                //Log.i("------------------Trust Message-----------",":"+result);
                Uri uri = Uri.parse(result);
                if(uri.getQueryParameter("status").toLowerCase().equalsIgnoreCase("approved")){
                    transaction_id = uri.getQueryParameter("transid");
                    card_last_4 = uri.getQueryParameter("cc");
                    brand = uri.getQueryParameter("card_type");
                    doRequest();
                }else{
                    Toast.makeText(getApplicationContext(),  uri.getQueryParameter("responsecodedescriptor"),	Toast.LENGTH_LONG).show();
                }
            }
        } /*else if(requestCode == CitrusFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK ) {
            try {
                // You will get data here if transaction flow is started through pay options other than wallet
                TransactionResponse transactionResponse = data.getParcelableExtra(CitrusUIActivity
                        .INTENT_EXTRA_TRANSACTION_RESPONSE);
                // You will get data here if transaction flow is started through wallet
                //ResultModel resultModel = data.getParcelableExtra(ResultFragment.ARG_RESULT);

                if (transactionResponse.getTransactionStatus().name().equals("SUCCESSFUL")) {
                    String jsonResponse = transactionResponse.getJsonResponse();
                    Gson objJsonRes = new Gson();
                    CitrusPaymentGson objPayGson = objJsonRes.fromJson(jsonResponse, CitrusPaymentGson.class);
                    brand = objPayGson.cardType;
                    card_last_4 = objPayGson.maskedCardNumber;
                    transaction_id = transactionResponse.getTransactionDetails().getTxRefNo();
                    doRequest();
                    // Toast.makeText(MainActivity.this,transactionResponse.getTransactionDetails().getTxRefNo()+"",Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(SelfcheckinPaymentActivity.this," Transaction "+transactionResponse.getTransactionStatus().name()+"",Toast.LENGTH_LONG).show();
                }



            }catch (Exception e){
                e.printStackTrace();
            }

        }*/else if (requestCode == REQUEST_SCAN  && data != null
                && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            String outStr = new String();
            Bitmap cardTypeImage = null;
            CreditCard result = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
            if (result != null) {
                outStr += "Card number: " + result.getRedactedCardNumber() + "\n";
                scanCardNumber=formatCardNumber("-",result.getFormattedCardNumber().replace(" ",""),4);
                scanExpMonth=result.expiryMonth;
                scanExpYear=result.expiryYear;
                scanCCV=result.cvv;
                int pagerPosition=view_pages.getCurrentItem();
                adapter = new MyPagerAdapter();
                view_pages.setAdapter(adapter);
                tabs.setViewPager(view_pages);
                view_pages.setCurrentItem(pagerPosition);
                //adapter.notifyDataSetChanged();

				/*CardType cardType = result.getCardType();
				cardTypeImage = cardType.imageBitmap(this);
				outStr += "Card type: " + cardType.name() + " cardType.getDisplayName(null)=" + cardType.getDisplayName(null) + "\n";
				outStr += "Card number: " + result.getFormattedCardNumber() + "\n";
				outStr += "Expiry: " + result.expiryMonth + "/" + result.expiryYear + "\n";
				outStr += "CVV: " + result.cvv + "\n";
				AppUtils.displayLog("Scan Card Resiult=",""+outStr);*/
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            /*Intent startentent=null;
            // ticket_dialog.dismiss();
            if (Util.getselfcheckinbools(Util.ISSELFCHECKIN)) {
                startentent = new Intent(SelfcheckinPaymentActivity.this, SelfCheckinAttendeeList.class);
            } else {
                startentent = new Intent(SelfcheckinPaymentActivity.this, ManageTicketActivity.class);
            }
            startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            AlertDialogCustom ask_dialog = new AlertDialogCustom(SelfcheckinPaymentActivity.this);
            ask_dialog.setFirstButtonName("DISCARD");
            ask_dialog.setParamenters("Alert", "Do you want to discard the order?", startentent, null, 2, true);
            ask_dialog.show();*/
            openWaringAlert();
            return true;
        }
        return false;
    }

    private void fillOrderLineItems(){
        try {
            String json_string = Util.order_request.getString(Util.ORDER_REQUEST_STRING, "");
            String json_items = Util.order_Items.getString(Util.ORDER_ITEMS_STRING, "");
            String totalorderlist_string = Util.totalorderlisthandler.getString("TTT", "");
            if(!totalorderlist_string.isEmpty()){
                Type type = new TypeToken<TotalOrderListHandler>(){}.getType();
                totalorderlisthandler = new Gson().fromJson(totalorderlist_string, type);
            }if(!json_string.isEmpty()){
                Type type = new TypeToken<List<OrderItemListHandler>>(){}.getType();
                order_line_items = new Gson().fromJson(json_string, type);
            }
            if(!json_items.isEmpty()){
                Type type = new TypeToken<List<ItemTypeController>>(){}.getType();
                items_list = new Gson().fromJson(json_items, type);
            }
            Util.order_request.edit().clear().commit();
            Util.order_Items.edit().clear().commit();
            Util.totalorderlisthandler.edit().clear().commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onScan() {
        try {
            Intent intent = new Intent(this, CardIOActivity.class)
                   /* .putExtra(CardIOActivity.EXTRA_APP_TOKEN, Util.CARDIO_APP_TOKEN)
                    .putExtra(CardIOActivity.EXTRA_NO_CAMERA, false)
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true)
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
                    .putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false)
                    .putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false)
                    .putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, "en")
                    .putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, Color.GREEN)
                    .putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, false)
                    .putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);
            try {
                //int unblurDigits = Integer.parseInt(mUnblurEdit.getText().toString());
            } catch (NumberFormatException ignored) {
            }

            startActivityForResult(intent, REQUEST_SCAN);*/
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true) // default: false
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false) // default: false
                    .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

            // MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
            //startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
            try {
                //int unblurDigits = Integer.parseInt(mUnblurEdit.getText().toString());
            } catch (NumberFormatException ignored) {
            }

            startActivityForResult(intent, REQUEST_SCAN);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String formatCardNumber(String t, String s, int num) {
        StringBuilder retVal;

        if (null == s || 0 >= num) {
            throw new IllegalArgumentException("Don't be silly");
        }

        if (s.length() <= num) {
            return s;
        }

        retVal = new StringBuilder(s);

        for(int i = retVal.length(); i > 0; i -= num){
            retVal.insert(i, t);
        }
        return retVal.toString();
    }
    public static int getSelectedPosition(String[] arrayList, String value) {
        int pos = 0;
        for (int i = 0; i < arrayList.length; i++) {
            if (NullChecker(arrayList[i]).trim().equals(value.trim())) {
                pos = i;
            }
        }
        return pos;
    }

    public void doBadgePrint(Cursor cursor){
        try{
            if (BaseActivity.isOrderScanned(order_id)) {
                //attendee_cursor = Util.db.getAttendeeDataCursorForNonBadgable(whereClause);
                attendee_cursor=cursor;
            }
            //for (int i = 0; i < attendee_cursor.getCount(); i++) {
            //attendee_cursor.moveToPosition(i);
            String id = attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID));
            if (NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE))).equalsIgnoreCase("B - Badge")) {
                FrameLayout print_badge = (FrameLayout) linearview.findViewById(R.id.frame_attdetailqrcodebadge);
                FrameLayout frame_transparentbadge = (FrameLayout) linearview.findViewById(R.id.frame_transparentbadge);
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
                //SelfCheckinAttendeeDetailActivity.selfcheckin_payment_cursor=attendee_cursor;

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
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                Log.d("HeadSetPlugInTest", "state: " + intent.getIntExtra("state", -1));
                if(intent.getIntExtra("state", -1)==1){
                    isReaderConnected=true;
                }else if(intent.getIntExtra("state", -1)==0){
                    isReaderConnected=false;
                }
                Log.d("HeadSetPlugInTest", "microphone: " + intent.getIntExtra("microphone", -1));
            }
        }
    };



    @Override
    protected void onStop() {
        super.onStop();

        getApplicationContext().unregisterReceiver(mReceiver);
    }
/*
	public static Cursor getattendeecursor(String orderId) {
		Cursor c = Util.db.getBadgeableTicketOrderDetails(orderId);
		try {
			if (c.getCount() > 0) {
				c.moveToFirst();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}
*/
}

