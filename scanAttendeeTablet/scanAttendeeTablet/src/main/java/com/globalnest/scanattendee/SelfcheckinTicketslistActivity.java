package com.globalnest.scanattendee;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.classes.MultiDirectionSlidingDrawer;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BlockTicketListController;
import com.globalnest.mvc.BlockTicketResponse;
import com.globalnest.mvc.ItemTypeController;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.mvc.TicketHandler;
import com.globalnest.mvc.TicketTypeContoller;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.PullToRefreshListView;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sailakshmi on 27-11-2017.
 */

public class SelfcheckinTicketslistActivity extends BaseActivity implements View.OnClickListener {

    TicketAdapter ticketAdapter;
    int[] selected_ticket_qty;
    int[] selected_ticket_price;
    int no_of_cart = 0;
    HashMap<Integer, ItemTypeController> selected_item_data = new HashMap<Integer, ItemTypeController>();
    HashMap<Integer, TicketTypeContoller> selected_ticket_data = new HashMap<Integer, TicketTypeContoller>();
    ArrayList<TicketTypeContoller> _cartTickets = new ArrayList<TicketTypeContoller>();
    ArrayList<BlockTicketListController> ticketname_for_packageitems = new ArrayList<BlockTicketListController>();
    ArrayList<BlockTicketResponse> block_tic_res;
    HashMap<String,ArrayList<BlockTicketListController>> block_ticket_map=new HashMap<String, ArrayList<BlockTicketListController>>();
    int pos=0,innerpos=0;
    String requestType = "",whereClause="";
    ListView itemView;
    LinearLayout layout_promoceode;
    TextView txt_afterapplied;

    Cursor c_ticket;
    double main_total_serviceFee=0,main_total_servicetax=0,main_total = 0,main_discountedamount=0,main_totalafterdiscountwithTax=0;
    double item_servicefee=0,item_servicetax=0,item_total=0;
    String order_id="";
    EditText edt_promocode;
    String promocode="";
    Button btnapplynow;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.selfmanage_tickets_layout);
        order_id=getIntent().getStringExtra(Util.ORDER_ID);
        if(!NullChecker(order_id).isEmpty()) {
            Intent endintent = new Intent(SelfcheckinTicketslistActivity.this, OrderSucessPrintActivity.class);
            endintent.putExtra(Util.ORDER_ID, order_id);
            endintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(endintent);
        }
        img_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                _cartTickets.clear();
                for (int key : selected_ticket_data.keySet()) {
                    _cartTickets.add(selected_ticket_data.get(key));
                }

                if(_cartTickets.size()==1){
                    if(isOnline()){
                        requestType= WebServiceUrls.SA_BLOCKING_TICKETS;
                        doRequest();
                    }else{
                        AlertDialogCustom dialog = new AlertDialogCustom(SelfcheckinTicketslistActivity.this);
                        dialog.setParamenters("Warning", "Please check your internet connection.", null, null, 2, true);
                        dialog.show();
                    }
                }else {
                    if(_cartTickets.size()>1) {
                        if (isOnline()) {
                            requestType = WebServiceUrls.SA_BLOCKING_TICKETS;
                            doRequest();
                        }
                    }
                  /*  Intent i = new Intent(SelfcheckinTicketslistActivity.this,
                            TicketCartActivity.class);
                    i.putExtra("CART TICKETS", _cartTickets);
                   *//* i.putExtra(Util.TOTAL, main_total);
                    i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
                    i.putExtra(Util.SERVICE_FEE,main_total_serviceFee);
                    i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);*//*
                    i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
                    startActivity(i);*/
                }
            }
        });
        img_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestType = "TICKET LIST";
                doRequest();
            }
        });
        btnapplynow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promocode=edt_promocode.getText().toString().trim();
                if(NullChecker(promocode).isEmpty()){
                    edt_promocode.setError("Please Enter any Promcode!");
                    edt_promocode.requestFocus();
                }else {
                    requestType=WebServiceUrls.SA_PROMOCODE;
                    doRequest();
                }
            }
        });
    }

    @Override
    public void setCustomContentView(int layout) {
        try {
            activity = this;
            View v = inflater.inflate(layout, null);
            linearview.addView(v);
            txt_title.setText("Tickets");
            img_menu.setImageResource(R.drawable.back_button);
            img_setting.setVisibility(View.VISIBLE);
            img_setting.setImageResource(R.drawable.dashboardrefresh);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            back_layout.setOnClickListener(this);
       /* cartSlider = (MultiDirectionSlidingDrawer) linearview
                .findViewById(R.id.cartslider);*/
            //loadtickets = (LinearLayout) linearview.findViewById(R.id.loadtickets);
            // txt_no_ticket = (TextView) linearview.findViewById(R.id.notickets);
            txt_cartno = (TextView) findViewById(R.id.txt_cartitems_no);
            //txt_proceed = (TextView) linearview.findViewById(R.id.btnticketcart);
            img_cart = (FrameLayout) findViewById(R.id.imgcatr);
            // itemView = (ListView) linearview.findViewById(R.id.ticketpager);
            itemView = (ListView) linearview
                    .findViewById(R.id.selfcheckinticketpager);
            layout_promoceode = (LinearLayout) linearview.findViewById(R.id.lay_selfpromocode);
            txt_afterapplied=(TextView) linearview.findViewById(R.id.txt_appliedpromo);
            edt_promocode=(EditText)linearview.findViewById(R.id.edt_promocode);
            btnapplynow =(Button) linearview.findViewById(R.id.btnapplynow);
        /*txt_proceed.setTypeface(Util.roboto_bold);
        txt_proceed.setOnClickListener(this);
        txt_no_ticket.setTypeface(Util.roboto_regular);*/
            String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package')";
            whereClause = ",ItemPoolDetails where ItemDetails.EventId='" + checked_in_eventId + "' AND ItemDetails.Item_SA_Visibility ='true' AND "+where_condition;
            c_ticket = Util.db.getSelfCheckinTicketCursor(whereClause);//getOnsiteTicketCursor
            AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
            ticketAdapter = new TicketAdapter(SelfcheckinTicketslistActivity.this, c_ticket);
            requestType = "TICKET LIST";
            selected_ticket_qty = new int[c_ticket.getCount()];

            // if(NullChecker(getIntent().getStringExtra(Util.ADDEVENT)).equals(Util.ADDEVENT))

            if (c_ticket.getCount() > 0) {
                //Log.i("checked_in_eventId", "---No tickets in the database");
                AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
                itemView.setAdapter(ticketAdapter);
                itemView.setVisibility(View.VISIBLE);
           /* txt_no_ticket.setVisibility(View.GONE);
            loadtickets.setVisibility(View.GONE);*/
            }else{
                if (isOnline()) {
                    ////Log.i("checked_in_eventId", "---Loging from the server");
                /*loadtickets.setVisibility(View.VISIBLE);
                txt_no_ticket.setVisibility(View.GONE);*/
                    doRequest();
                } else {
                    startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void doRequest() {
        String access_token = sfdcddetails.token_type + " "
                + sfdcddetails.access_token;

        try {
            if(requestType.equalsIgnoreCase(WebServiceUrls.SA_PROMOCODE)){
                String _url = sfdcddetails.instance_url + WebServiceUrls.SA_PROMOCODE + "&eventid=" + checked_in_eventId+"&isPromo=true&promocode="+edt_promocode.getText().toString();
                postMethod = new HttpPostData("Applying Promocode...", _url, setJsonArray().toString(), access_token, SelfcheckinTicketslistActivity.this);
                postMethod.execute();
            }else if(requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)){
                String _url = sfdcddetails.instance_url+WebServiceUrls.SA_BLOCKING_TICKETS+"&sessiontime="+checkedin_event_record.sessiontime;
                postMethod = new HttpPostData("Checking Tickets Availability...",_url, setJsonArray().toString(), access_token, SelfcheckinTicketslistActivity.this);
                postMethod.execute();
            }else if (requestType.equalsIgnoreCase("TICKET LIST")) {
                String url = sfdcddetails.instance_url
                        + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="
                        + checked_in_eventId;
                postMethod = new HttpPostData("Refreshing Tickets...", url, null,
                        access_token, SelfcheckinTicketslistActivity.this);
                postMethod.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private JSONArray setJsonArray() {
        JSONArray ticketArray = new JSONArray();
        try {
            for (int i = 0; i < _cartTickets.size(); i++) {
                JSONObject obj = new JSONObject();
                obj.put("ItemId", _cartTickets.get(i).getTicketsId());
                obj.put("Qty", Integer.valueOf(_cartTickets.get(i).getSelectedTickets()));
                ticketArray.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ticketArray;
    }

    @Override
    public void parseJsonResponse(String response) {
        try {
            if (!isValidResponse(response)) {
                openSessionExpireAlert(errorMessage(response));
            }
            String zeroAvailableTicketArray = "";
            String lessAvailableTicketArray = "";
            //Log.i("--------TicketCartActivity--------", ":"+response);
            if (requestType.equalsIgnoreCase(WebServiceUrls.SA_BLOCKING_TICKETS)) {
                gson = new Gson();
                ticketname_for_packageitems.clear();
                java.lang.reflect.Type listType = new TypeToken<ArrayList<BlockTicketResponse>>() {
                }.getType();
                block_tic_res = gson.fromJson(response, listType);

                for (int i = 0; i < block_tic_res.size(); i++) {
                    int package_qty = 0;
                    pos = i;
                    ArrayList<BlockTicketListController> ticketname = new ArrayList<BlockTicketListController>();
                    Cursor c = Util.db.getSelfCheckinTicketCursor(" where " + DBFeilds.ADDED_ITEM_ID + " = '" + block_tic_res.get(i).ItemId + "'");
                    c.moveToFirst();
                    String item_type = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME));
                    String attendee_setting = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_OPTION));
                    c.close();
                    if (item_type.equalsIgnoreCase("Package")) {

                        if (attendee_setting.equalsIgnoreCase(getString(R.string.donotinfofromattendee))) {
                            for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
                                if (block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && !TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)) {
                                    ticketname.add(block_tic_res.get(i).ticketsList.get(j));
                                } else {
                                    package_qty = package_qty + 1;
                                }
                            }
                        } else {
                            for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
                                if (block_tic_res.get(i).ticketsList.get(j).Item_Type__r.Name.equalsIgnoreCase("Package") && TextUtils.isEmpty(block_tic_res.get(i).ticketsList.get(j).Parent_ID__c)) {
                                    package_qty = package_qty + 1;
                                    ticketname.add(block_tic_res.get(i).ticketsList.get(j));
                                } else {
                                    ticketname_for_packageitems.add(block_tic_res.get(i).ticketsList.get(j));
                                }
                            }
                        }

                    } else {
                        for (int j = 0; j < block_tic_res.get(i).ticketsList.size(); j++) {
                            ticketname.add(block_tic_res.get(i).ticketsList.get(j));
                        }
                    }
                    block_ticket_map.put(block_tic_res.get(i).ItemId, ticketname);

                    if (!item_type.equalsIgnoreCase("Package")) {
					/*for (int k = 0; k < _cartTickets.size(); k++) {
						if (_cartTickets.get(k).getTicketsId().equals(block_tic_res.get(i).ItemId)){
							_cartTickets.get(k).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
						}
					}*/
                        if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0) {
                            zeroAvailableTicketArray = zeroAvailableTicketArray + ", " + Util.db.getItemName(block_tic_res.get(i).ItemId);
                        } else {
                            for (int m = 0; m < _cartTickets.size(); m++) {
                                if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
                                    if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
                                            .parseInt(_cartTickets.get(m).getSelectedTickets())) {
                                        innerpos = m;
                                        lessAvailableTicketArray = lessAvailableTicketArray + ", "
                                                + Util.db.getItemName(block_tic_res.get(i).ItemId);
                                    }
                                }
                            }
                        }

                    } else {
                        if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) == 0) {
                            zeroAvailableTicketArray = Util.db.getItemName(block_tic_res.get(i).ItemId);

                        }
                        //_cartTickets.get(i).setSelectedTickets(block_tic_res.get(i).tickesAvilable);
                        else {
                            for (int m = 0; m < _cartTickets.size(); m++) {
                                if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
                                    if (Integer.parseInt(block_tic_res.get(i).tickesAvilable) < Integer
                                            .parseInt(_cartTickets.get(m).getSelectedTickets())) {
                                        innerpos = m;
                                        lessAvailableTicketArray = lessAvailableTicketArray + ", "
                                                + Util.db.getItemName(block_tic_res.get(i).ItemId);
                                    }
                                }
                            }
                            //	_cartTickets.get(i).setSelectedTickets(String.valueOf(package_qty));
                        }
                    }
                }

                AlertDialogCustom dialog;
                if (!lessAvailableTicketArray.isEmpty()) {
                    String msg = "";
                    Util.setCustomAlertDialog(SelfcheckinTicketslistActivity.this);
                    msg = "Your selected quantity of " + _cartTickets.get(innerpos).getSelectedTickets() + " for " + Util.db.getItemName(block_tic_res.get(pos).ItemId)
                            + " ticket is more than available quantity." + "\n" + "Would you like to decrease the Ticket Quantity to "
                            + block_tic_res.get(pos).tickesAvilable + " and continue the Order";
                    Util.alert_dialog.setCancelable(false);
                    Util.openCustomDialog("Alert",
                            msg);
                    Util.txt_okey.setText("Ok");
                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                    Util.alert_dialog.setCancelable(false);
                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                            for (int m = 0; m < _cartTickets.size(); m++) {
                                if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
                                    _cartTickets.get(m).setSelectedTickets(block_tic_res.get(pos).tickesAvilable);
                                    //t_cart_adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                            requestType = WebServiceUrls.SA_BLOCKING_TICKETS;
                            doRequest();

                        }
                    });
                    Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();

                        }
                    });
				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Your selected quantity of "+ lessAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/
                } else if (!zeroAvailableTicketArray.isEmpty()) {
                    String msg = "";
                    Util.setCustomAlertDialog(SelfcheckinTicketslistActivity.this);
                    if (_cartTickets.size() == 1) {
                        msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId) +
                                " tickets are not available.";

                    } else if (_cartTickets.size() > 1) {
                        msg = "Sorry! " + Util.db.getItemName(block_tic_res.get(pos).ItemId) +
                                " tickets are not available. Would you like to remove the Ticket and continue the Order";
                    }
                    Util.alert_dialog.setCancelable(false);
                    Util.openCustomDialog("Alert",
                            msg);
                    Util.txt_okey.setText("Ok");
                    Util.txt_dismiss.setVisibility(View.VISIBLE);
                    Util.alert_dialog.setCancelable(false);
                    Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();
                            if (_cartTickets.size() == 1) {
                                requestType="TICKET LIST";
                                doRequest();
                                /*Intent startentent = new Intent(SelfcheckinTicketslistActivity.this, SelfcheckinTicketslistActivity.class);
                                startentent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(startentent);
                                finish();*/
                            } else if (_cartTickets.size() > 1) {
                                for (int m = 0; m < _cartTickets.size(); m++) {
                                    if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(pos).ItemId)) {
                                        _cartTickets.remove(m);
                                        //	t_cart_adapter.notifyDataSetChanged();
                                        break;
                                    }
                                }
                                //if (_cartTickets.get(m).getTicketsId().equals(block_tic_res.get(i).ItemId)) {
                                /*_*//*ticketcart.indexOf(block_tic_res.get(pos).ItemId);
							_cartTickets.remove(pos);*/

                            }

                            //finish();
                        }
                    });
                    Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            Util.alert_dialog.dismiss();

                        }
                    });

				/*dialog=new AlertDialogCustom(TicketCartActivity.this);
				dialog.setParamenters("Alert", "Sorry! +"+zeroAvailableTicketArray +"tickets are not available.", null, null, 1, false);
				//dialog.setParamenters("Alert", "Your selected quantity of "+zeroAvailableTicketArray +" is more than available quantity.", null, null, 1, false);
				dialog.show();*/


                } else if (!lessAvailableTicketArray.isEmpty() && !zeroAvailableTicketArray.isEmpty()) {
                    dialog = new AlertDialogCustom(SelfcheckinTicketslistActivity.this);
                    dialog.setParamenters("Alert", "Your selected quantity of " + lessAvailableTicketArray + " " + zeroAvailableTicketArray + " is more than available quantity.", null, null, 1, false);
                    dialog.show();
                } else {
                    getTotalAmount();
                    // Intent i = new Intent(SelfcheckinTicketslistActivity.this, CollectOrderInfo.class);
                    Intent i = new Intent(SelfcheckinTicketslistActivity.this, SelfcheckinCollectOrderInfo.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(Util.CART_TICKETS, _cartTickets);
                    i.putExtra(Util.TOTAL, main_total);
                    i.putExtra(Util.SERVICE_TAX, main_total_servicetax);
                    i.putExtra(Util.SERVICE_FEE, main_total_serviceFee);
                    i.putExtra(Util.BLOCK_TICKET_MAP, block_ticket_map);
                    i.putExtra(Util.BLOCK_PACKAGE_TICKET, ticketname_for_packageitems);
                    //getTotalAmount();
                    startActivity(i);
                }

                //getTotalAmount();
                //ticketinfo.setAdapter(t_cart_adapter);

            } else if (requestType.equalsIgnoreCase("TICKET LIST")) {
                Gson gson = new Gson();
                if(isRefresh && !Util.isMyServiceRunning(DownloadService.class, SelfcheckinTicketslistActivity.this)){
                    Util.db.deleteTable(DBFeilds.TABLE_ADDED_TICKETS);
                    Util.db.deleteTable(DBFeilds.TABLE_ITEM_POOL);
                    Util.db.deleteTable(DBFeilds.TABLE_ITEM_REG_SETTINGS);
                    isRefresh=false;
                }

                ItemsListResponse items_response = new Gson().fromJson(response, ItemsListResponse.class);
                Util.db.upadteItemListRecordInDB(items_response.Itemscls_infoList,
                        checked_in_eventId);
                Util.db.InsertAndUpdateSEMINAR_AGENDA(items_response.agndaInfo);
                List<SessionGroup> group_list = Util.db.getGroupList(BaseActivity.checkedin_event_record.Events.Id);
                Util.db.deleteEventScannedTicketsGroup(BaseActivity.checkedin_event_record.Events.Id);
                Util.db.deleteEventScannedTickets(BaseActivity.checkedin_event_record.Events.Id);

                if(!group_list.isEmpty()){
                    for(SessionGroup group : group_list){
                        for(SessionGroup server_group : items_response.userSessions){
                            if(group.Id.equalsIgnoreCase(server_group.Id)){
                                if(server_group.BLN_Session_users__r.records.size() > 0){
                                    server_group.BLN_Session_users__r.records.get(0).DefaultValue__c = group.Scan_Switch;
                                }

                            }
                        }
                    }
                }
                Util.db.InsertAndUpdateSESSION_GROUP(items_response.userSessions);

                setListViewData();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void insertDB() {

    }
    public double getItemPoolFeeCount(String item_pool_id){

        double fee_total=0.0;
        Cursor pool_cursor = Util.db.getTicketPoolCursor(" where "+DBFeilds.ITEMPOOL_ADDON_PARENT+"='"+item_pool_id+"'");
        if(pool_cursor == null){
            return 0;
        }else if(pool_cursor.getCount() == 0){
            return 0;
        }else{
            pool_cursor.moveToFirst();
            for(int i=0; i<pool_cursor.getCount();i++){
                String item_type_id = pool_cursor.getString(pool_cursor.getColumnIndex(DBFeilds.ITEMPOOL_ITEMTYPE_ID));
                double item_type_price = Util.db.getItemType_BL_FEE(checked_in_eventId, item_type_id);
                fee_total = fee_total + (item_type_price * Integer.parseInt(pool_cursor.getString(pool_cursor.getColumnIndex(DBFeilds.ITEMPOOL_ADDON_COUNT))));
                pool_cursor.moveToNext();
            }
            pool_cursor.close();
        }

        return fee_total;
    }
    private  String getItemPrice(Cursor cursor,int quantity) {
        item_servicefee=0;item_servicetax=0;item_total=0;
        double total=0.00;
        String itemfee="";
        //cursor.getDouble(cursor.getColumnIndex("ItemPrice")
        //cursor.getString(cursor.getColumnIndex("ServiceFee"))

        /*if(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)).equalsIgnoreCase("Package")){
            double package_fee = Util.db.getItemFee(checked_in_eventId, cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_ID)));
            if(package_fee == 0){
                item_servicefee = getItemPoolFeeCount(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)))*quantity;/*//* Double.parseDouble(_cartTickets.get(i).getSelectedTickets()
            }else{
                item_servicefee = Util.db.getItemType_BL_FEE(checked_in_eventId, cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)))*quantity; // * Double.parseDouble(String.valueOf(selected_ticket_qty[i])
            }
        }else {*/
        if (cursor.getString(cursor.getColumnIndex("ServiceFee")).equalsIgnoreCase("true") && cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Paid")) {
            item_servicefee = Util.db.getItemFee(checked_in_eventId,(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_ID))))*quantity;
            //}

        }
        item_total=cursor.getDouble(cursor.getColumnIndex("ItemPrice"))*quantity;
        EventObjects event_data = Util.db.getSelectedEventRecord(checked_in_eventId);

        if (NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")&&
                cursor.getString(cursor
                        .getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)).equalsIgnoreCase("true") &&
                cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Paid") ||
                cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Donation"))
        {
            if(!event_data.Events.Tax_Rate__c.trim().isEmpty()){
                if (cursor.getString(cursor.getColumnIndex("ServiceFee")).equalsIgnoreCase("true")){
                    item_servicefee = Util.db.getItemFee(checked_in_eventId,(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_ID))))*quantity;
                }
                item_servicetax= Double.valueOf(event_data.Events.Tax_Rate__c);
                item_servicetax=(item_servicetax/100)*(cursor.getDouble(cursor.getColumnIndex("ItemPrice"))*quantity+item_servicefee);
            }
        } else {
            item_servicetax=item_servicetax+0.00;
        }
        total=item_total+item_servicetax+item_servicefee;
        itemfee=String.format("%.2f",total);
        return itemfee;
    }

    private void getTotalAmount() {
        main_total=0;item_servicefee=0;main_total_serviceFee=0;item_servicetax=0;main_total_servicetax=0;
        EventObjects event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
        for (int i = 0; i < _cartTickets.size(); i++) {
            item_servicefee=0;item_servicetax=0;item_total=0;
            if(Integer.parseInt(_cartTickets.get(i).getSelectedTickets())!=0)
            {
                if (NullChecker(checkedin_event_record.Events.Accept_Tax_Rate__c).equals("true")&&Util.NullChecker(_cartTickets.get(i).getIsTicketTaxable()).equalsIgnoreCase("true") && (Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid") ||
                        Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
                    if(!event_data.Events.Tax_Rate__c.trim().isEmpty()){
                        if (Util.NullChecker(_cartTickets.get(i).getTicketFeeSetting()).equalsIgnoreCase("true"))
                        {
                            item_servicefee = Util.db.getItemFee(checked_in_eventId, _cartTickets.get(i).getTicketsId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
                        }
                        item_servicetax= Double.valueOf(event_data.Events.Tax_Rate__c);
                        item_servicetax=(item_servicetax/100)*(Integer.parseInt(_cartTickets.get(i).getSelectedTickets())*_cartTickets.get(i).getTicketPrice()+item_servicefee);
                    }

                    main_total_servicetax=main_total_servicetax+item_servicetax;
                } else {
                    main_total_servicetax=main_total_servicetax+0.00;
                }

                if (Util.NullChecker(_cartTickets.get(i).getTicketFeeSetting()).equalsIgnoreCase("true") && (Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Paid")
                        || Util.NullChecker(_cartTickets.get(i).getTicketPaymentType()).equalsIgnoreCase("Donation"))) {
                    //item_servicefee = Double.parseDouble("1.50");
                    //item_servicefee = Double.parseDouble(_cartTickets.get(i).getBLFeeAmount()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());

                   /* if(_cartTickets.get(i).getTicketType().equalsIgnoreCase("Package")){
                        double package_fee = Util.db.getItemType_BL_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId());
                        if(package_fee == 0){
                            item_servicefee = getItemPoolFeeCount(_cartTickets.get(i).getTicketPoolId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
                        }else{
                            item_servicefee = Util.db.getItemType_BL_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
                        }
                    }else{*/
                    item_servicefee = Util.db.getItemFee(checked_in_eventId, _cartTickets.get(i).getTicketsId()) * Double.parseDouble(_cartTickets.get(i).getSelectedTickets());
                    //}
                    /*double max_fee = Util.db.getItemType_BL_MAX_FEE(checked_in_eventId, _cartTickets.get(i).getTicketTypeId());

                    if( max_fee != 0 && item_servicefee > max_fee){
                        item_servicefee = max_fee;
                    }*/
                    main_total_serviceFee=main_total_serviceFee+item_servicefee;

                } else {
                    main_total_serviceFee=main_total_serviceFee+0.00;
                }
                item_total =(_cartTickets.get(i).getTicketPrice() * Integer.parseInt(_cartTickets.get(i).getSelectedTickets()));
                main_total=main_total+item_total;
            }else{
                main_total=main_total+0.00;
            }
        }
        //btnbuynow_info.setText("Price Total="+String.format( "%.2f",main_total)+" Fee:"+String.format("%.2f",main_total_serviceFee)+" Tax:"+String.format( "%.2f", main_total_servicetax ));
    }
    public void setListViewData() {
        selected_item_data.clear();
        selected_ticket_data.clear();
        img_cart.setVisibility(View.GONE);
        layout_promoceode.setVisibility(View.GONE);
        String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package')";
        whereClause = ",ItemPoolDetails where ItemDetails.EventId='" + checked_in_eventId + "' AND ItemDetails.Item_SA_Visibility ='true' AND "+where_condition;
        // c_ticket = Util.db.getEventItems(checked_in_eventId);
        c_ticket = Util.db.getSelfCheckinTicketCursor(whereClause);
        ////Log.i(checked_in_eventId + "---MANAGE TICKET CURSOR SIZE----", ":"+ c_ticket.getCount());
        ticketAdapter = new TicketAdapter(SelfcheckinTicketslistActivity.this, c_ticket);
        requestType = "TICKET LIST";
        selected_ticket_qty = new int[c_ticket.getCount()];
        if (c_ticket.getCount() > 0) {
            itemView.setAdapter(ticketAdapter);
            itemView.setVisibility(View.VISIBLE);
            /*txt_no_ticket.setVisibility(View.GONE);
            loadtickets.setVisibility(View.GONE);*/
        } else {
            itemView.setVisibility(View.GONE);
            /*txt_no_ticket.setVisibility(View.VISIBLE);
            loadtickets.setVisibility(View.GONE);*/
        }
			/*if (getIntent().getBooleanExtra(Util.TICKET,false)) {
				if (dialog == null) {
					Intent intent_social = new Intent(
							ManageTicketActivity.this, SocialMedia.class);
					intent_social.putExtra("Status", "CREATE");
					intent_social.putExtra("IS_FROM", "AddTicketActivity");
					intent_social.putExtra(Util.ADDED_EVENT_ID, getIntent().getStringExtra(Util.ADDED_EVENT_ID));
					intent_social.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Intent i = new Intent(ManageTicketActivity.this,
							ManageTicketActivity.class);
					i.putExtra("Type", "Ticket");
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent_social);
					*//*dialog = new AlertDialogCustom(ManageTicketActivity.this);
					dialog.setParamenters(
							"Alert",
							"Do you want to publish your event on social sites ?",
							intent_social, null, 2, false);

					dialog.show();*//*
				}
			}*/

    }

    @Override
    public void onClick(View v) {
        if(v == back_layout){
            finish();
        }
    }
    private class TicketAdapter extends CursorAdapter {
        // int sel_qty = 0;
        String price = "";

        // private final ImageLoadingListener animateFirstListener = new
        // AnimateFirstDisplayListener();
        private class ViewHolder {
            TextView t_name, t_line_items, t_price,
                    txt_isPackage,txt_closed,t_qty;
            Button btn_add, btn_minus;
            LinearLayout lay_buttons,lay_isPackage;
            TextView selected_ticket_price;
            //EditText edit_price;
            FrameLayout fram_soldout;
            public boolean needInflate;
        }

        @SuppressWarnings("deprecation")
        public TicketAdapter(Context context, Cursor c) {
            super(context, c);
            AppUtils.displayLog("----------------Item Name------------",":"+c.getCount());
        }

        @Override
        public void bindView(View parent_view, Context context, final Cursor cursor) {

            final View v;
            if (parent_view==null) {
                v = newView(context, cursor, null);
            }
            else if (((TicketAdapter.ViewHolder)parent_view.getTag()).needInflate) {
                v = newView(context, cursor, null);

            }else{
                v = parent_view;
            }


            final TicketAdapter.ViewHolder viewholder = (TicketAdapter.ViewHolder) v.getTag();
            if (!cursor.getString(cursor.getColumnIndex("ItemImageUrl"))
                    .isEmpty()) {

            }
            AppUtils.displayLog("----------------Item Name------------",":"+cursor.getString(cursor.getColumnIndex("ItemName")));
            viewholder.t_name.setText(cursor.getString(cursor
                    .getColumnIndex("ItemName")));

            int i = cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity"));



            if(cursor.getInt(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SOLDQUANTITY))==0)
            {
                viewholder.fram_soldout.setVisibility(View.VISIBLE);
                viewholder.lay_buttons.setVisibility(View.GONE);
                //viewholder.txt_avi.setPaintFlags(viewholder.txt_avi.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }else{
                viewholder.fram_soldout.setVisibility(View.GONE);
                viewholder.lay_buttons.setVisibility(View.VISIBLE);
            }

            if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Paid")) {
                //viewholder.priceLayout.setVisibility(8);
                // viewholder.t_price.setVisibility(0);

                viewholder.t_price
                        .setText(Util.db
                                .getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
                                + " "
                                + String.format( "%.2f",cursor.getDouble(cursor
                                .getColumnIndex("ItemPrice"))));

                /*viewholder.t_type.setTextColor(getResources().getColor(
                        R.color.light_blue));
                viewholder.t_type.setText("Paid");*/

            } else if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equalsIgnoreCase("Donation")) {

                //viewholder.t_price.setVisibility(8);

                viewholder.t_price
                        .setText(Util.db
                                .getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
                                + " "
                                + String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("ItemPrice"))) );
                /*viewholder.t_type.setTextColor(getResources().getColor(R.color.light_blue));
                viewholder.t_type.setText("Donation");*/


            } else {
                //viewholder.priceLayout.setVisibility(8);
                viewholder.t_price.setText("");

               /* viewholder.t_price
                        .setText(Util.db
                                .getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c
                                + " "
                                + String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("ItemPrice"))) );*/
                /*viewholder.t_type.setTextColor(getResources().getColor(
                        R.color.light_blue));
                viewholder.t_type.setText("Free");
*/
            }

            //Log.i("----------------ItemType--------------",":"+cursor.getString(cursor.getColumnIndex("ItemTypeName")));
            if (cursor.getString(cursor.getColumnIndex("ItemTypeName"))
                    .equalsIgnoreCase("Package")) {
                String parant_id = cursor.getString(4);//cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_POOLID));
                //Log.i("----------------Item Pool Id--------------",":"+parant_id);
                if (!parant_id.isEmpty()) {
                    Cursor c1 = Util.db.getTicketPoolCursor(" where "
                            + DBFeilds.ITEMPOOL_ADDON_PARENT + " = '"
                            + parant_id + "'");
                    String line_items = "";

                    line_items="";
                    if (c1 != null) {
                        c1.moveToFirst();
                        for (int j = 0; j < c1.getCount(); j++) {
                            line_items = line_items
                                    + c1.getString(c1
                                    .getColumnIndex(DBFeilds.ITEMPOOL_ITEMPOOLNAME))
                                    + "-"
                                    + c1.getString(c1
                                    .getColumnIndex(DBFeilds.ITEMPOOL_ADDON_COUNT));
                            if (j != (c1.getCount() - 1)) {
                                line_items = line_items + ", ";
                            }
                            c1.moveToNext();
                        }
                        if (!line_items.isEmpty()) {
                            viewholder.t_line_items.setText("(" + line_items + ")");
                            viewholder.t_line_items.setVisibility(View.VISIBLE);
                            viewholder.txt_isPackage.setVisibility(View.VISIBLE);
                            viewholder.lay_isPackage.setVisibility(View.VISIBLE);


                        }else{
                            viewholder.t_line_items.setVisibility(View.GONE);
                            viewholder.lay_isPackage.setVisibility(View.GONE);
                        }
                        c1.close();
                    }
                    //viewholder.t_line_items.setText("(" + line_items + ")");
                    ////Log.i("------------Line Item String----------", ":"+ line_items.toString());
                }

            }else{
                viewholder.t_line_items.setVisibility(View.GONE);
                viewholder.lay_isPackage.setVisibility(View.GONE);
            }


            viewholder.btn_add.setTag(Integer.valueOf(cursor.getPosition()));
            viewholder.btn_minus.setTag(Integer.valueOf(cursor.getPosition()));
            //viewholder.edit_price.setTag(Integer.valueOf(cursor.getPosition()));
            // viewholder.btn_add.setVisibility(0);
            viewholder.t_qty.setError(null);

            if (selected_ticket_qty[cursor.getPosition()] == 0) {
                viewholder.btn_minus.setVisibility(View.VISIBLE);
                viewholder.selected_ticket_price.setVisibility(View.GONE);
                viewholder.btn_minus.setEnabled(false);
                viewholder.t_qty.setText((new StringBuilder()).append(
                        selected_ticket_qty[cursor.getPosition()]).toString());
                // viewholder.selected_ticket_price.setText(new StringBuilder().append(selected_ticket_qty[cursor.getPosition()]).toString());
//Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+ " "+ String.format("%.2f", cursor.getDouble(cursor.getColumnIndex("ItemPrice")))
                //cursor.getString(cursor.getColumnIndex("ServiceFee"))
              /* String fee= String.valueOf(getItemPrice(cursor,selected_ticket_qty[cursor.getPosition()]));
                //double feee=cursor.getDouble(cursor.getColumnIndex("ItemPrice"));
                viewholder.selected_ticket_price.setVisibility(View.VISIBLE);
                viewholder.selected_ticket_price.setText(fee);*/
            } else {
                //viewholder.btn_minus.setVisibility(0);
                viewholder.btn_minus.setEnabled(true);
                viewholder.selected_ticket_price.setVisibility(View.VISIBLE);
                viewholder.t_qty.setText((new StringBuilder()).append(
                        selected_ticket_qty[cursor.getPosition()]).toString());
                //viewholder.selected_ticket_price.setVisibility(View.GONE);

            }

            if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_STATUS))).equalsIgnoreCase("Closed")){
                viewholder.btn_add.setVisibility(View.GONE);
                viewholder.btn_minus.setVisibility(View.GONE);
                viewholder.txt_closed.setVisibility(View.VISIBLE);
            }else{
                viewholder.btn_add.setVisibility(View.VISIBLE);
                viewholder.btn_minus.setVisibility(View.VISIBLE);
                viewholder.txt_closed.setVisibility(View.GONE);
            }


            viewholder.btn_add.setTag(cursor.getPosition());
            viewholder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // no_of_cart++;
                    int i = ((Integer) view.getTag()).intValue();
                    int j = 1 + selected_ticket_qty[i];
                    Cursor cursor = ticketAdapter.getCursor();
                    cursor.moveToPosition(i);
                    //Log.i("----------------DB Item Sales Date-----------",":"+cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SALESSTARTDATE)));
                    String item_sales_date = Util.change_US_ONLY_DateFormat(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_SALESSTARTDATE)), checkedin_event_record.Events.Time_Zone__c);
                    //Log.i("----------------Item Sales Date-----------",":"+item_sales_date);
                    if(isPastEvent()){
                        AlertDialogCustom dialog = new AlertDialogCustom(SelfcheckinTicketslistActivity.this);
                        dialog.setParamenters("Alert", "Sorry! You cannot sell past event tickets.", null, null, 1, false);
                        dialog.show();
                    }else if(Util.isMyServiceRunning(DownloadService.class, SelfcheckinTicketslistActivity.this)){
                        showServiceRunningAlert(checkedin_event_record.Events.Name);
                    }else if(isFutureTicket(item_sales_date)){
                        AlertDialogCustom dialog = new AlertDialogCustom(SelfcheckinTicketslistActivity.this);
                        dialog.setParamenters("Alert", "Sorry! "+cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_NAME))+" sales are not yet started. Please check sales start date and time.", null, null, 1, false);
                        dialog.show();
                    }else{
                        Cursor _event_payment_setting = Util.db.getEvent_Payment_Setting(checked_in_eventId);
                        //Log.i("---------------Is Paid Tickets Exists------------",":"+Util.db.isPaidTicketExists(checked_in_eventId)+_event_payment_setting.getCount());
                        if(Util.db.isPaidTicketExists(checked_in_eventId) && _event_payment_setting.getCount()==0){
                            if(isEventOrganizer()){
                                Util.setCustomAlertDialog(SelfcheckinTicketslistActivity.this);
                                Util.openCustomDialog("Alert", "Please select at least one payment type to sell tickets, \n As EventOrganizer you don't have access to payment settings please contact EventAdmin");
                                Util.txt_okey.setText("Ok");
                                Util.txt_dismiss.setVisibility(View.GONE);
                                Util.txt_okey.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View arg0) {

                                        Util.alert_dialog.dismiss();
                                    }
                                });
                                //Toast.makeText(BaseActivity.this,"As EventOrganizer you don't have access to payment settings please contact EventAdmin.",Toast.LENGTH_LONG).show();
                            }else {
                                AlertDialogCustom dialog = new AlertDialogCustom(SelfcheckinTicketslistActivity.this);
                                Intent intent = new Intent(SelfcheckinTicketslistActivity.this, PaymentSetting.class);
                                intent.putExtra(Util.EDIT_EVENT_ID, checked_in_eventId);
                                // i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                dialog.setParamenters("Alert", "Please select at least one payment type to sell tickets ?", intent, null, 2, false);
                                dialog.setFirstButtonName("NOW");
                                dialog.setSecondButtonName("NOT NOW");
                                dialog.show();
                            }}else{
                            // selected_ticket_qty[i]++;
                            int available_items = cursor.getInt(cursor.getColumnIndex("ItemSoldQuantity"))
                                    - selected_ticket_qty[i];

                            if (available_items > 0) {
                                //viewholder.btn_add.setVisibility(0);
                                no_of_cart++;
                                selected_ticket_qty[i] = j;

                                TicketTypeContoller tickettypecontoller = new TicketTypeContoller();
                                if (cursor.getString(cursor.getColumnIndex("ItemPaymentType")).equals("Donation")) {
                                    if (!price.isEmpty()) {
                                        tickettypecontoller.setTicketPrice(Double.parseDouble(price));
                                    } else {
                                        tickettypecontoller
                                                .setTicketPrice(cursor.getDouble(cursor.getColumnIndex("ItemPrice")));
                                    }
                                } else {
                                    tickettypecontoller
                                            .setTicketPrice(cursor.getDouble(cursor.getColumnIndex("ItemPrice")));
                                }
                                tickettypecontoller.setTicketId(cursor.getString(cursor.getColumnIndex("ItemId")));
                                tickettypecontoller.setTicketPoolId(cursor.getString(4));
                                tickettypecontoller
                                        .setTicketTypeName(cursor.getString(cursor.getColumnIndex("ItemName")));
                                tickettypecontoller.setSelectedTickets(String.valueOf(selected_ticket_qty[i]));
                                tickettypecontoller
                                        .setTicketQuantity(cursor.getInt(cursor.getColumnIndex("ItemQuantity")));
                                tickettypecontoller
                                        .setTicketsEventId(cursor.getString(cursor.getColumnIndex("EventId")));
                                tickettypecontoller
                                        .setTicketFeeSetting(cursor.getString(cursor.getColumnIndex("ServiceFee")));
                                tickettypecontoller.setIsTicketTaxable(
                                        cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));
                                tickettypecontoller.setBLFeeAmount(
                                        cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_BL_FEE)));
                                tickettypecontoller.setTicketType(
                                        cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)));
                                tickettypecontoller.setTicketTypeId(
                                        cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)));
                                tickettypecontoller.setTicketPaymentType(
                                        cursor.getString(cursor.getColumnIndex("ItemPaymentType")));


                                selected_ticket_data.put(Integer.valueOf(i), tickettypecontoller);
                                //Log.i("---First time----",(new StringBuilder(":")).append(selected_ticket_data.size()).toString());
                                String fee=getItemPrice(cursor,selected_ticket_qty[cursor.getPosition()]);
                                //double feee=cursor.getDouble(cursor.getColumnIndex("ItemPrice"));
                                if(selected_ticket_qty[cursor.getPosition()]>0) {//String.format("%.2f",fee)
                                    viewholder.selected_ticket_price.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+fee);
                                }else{
                                    viewholder.selected_ticket_price.setVisibility(View.GONE);
                                }
                                ticketAdapter.notifyDataSetChanged();
                                if (selected_ticket_data.size() > 0 || selected_item_data.size() > 0) {
                                    img_cart.setVisibility(View.VISIBLE);
                                    // layout_promoceode.setVisibility(View.VISIBLE);
                                   // no_of_cart = selected_ticket_data.size();
                                    txt_cartno.setText(String.valueOf(no_of_cart));
                                    // txt_proceed.setVisibility(0);
                                    return;
                                } else {
                                    // txt_proceed.setVisibility(8);
                                    img_cart.setVisibility(View.GONE);
                                    layout_promoceode.setVisibility(View.GONE);
                                    return;
                                }
                            } else {
                                viewholder.btn_add.setVisibility(View.INVISIBLE);
                                viewholder.t_qty.requestFocus();
                                viewholder.t_qty.setError("Sorry! your selected ticket quantity is more than available quantity");                                return;
                            }

                        }
                    }

                }
            });
            // viewholder.btn_minus.setTag(cursor.getPosition());
            viewholder.btn_minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    no_of_cart--;
                    int i = ((Integer) view.getTag()).intValue();
                    int j = -1 + selected_ticket_qty[i];
                    Cursor cursor = ticketAdapter.getCursor();
                    cursor.moveToPosition(i);
                    selected_ticket_qty[i] = j;
                    if (selected_ticket_qty[i] == 0) {
                        selected_ticket_data.remove(Integer.valueOf(i));
                    } else {
                        TicketTypeContoller tickettypecontoller = new TicketTypeContoller();
                        tickettypecontoller.setTicketId(cursor.getString(cursor
                                .getColumnIndex("ItemId")));
                        tickettypecontoller.setTicketTypeName(cursor
                                .getString(cursor.getColumnIndex("ItemName")));
                        tickettypecontoller.setTicketPoolId(cursor.getString(4));
                        tickettypecontoller.setSelectedTickets(String
                                .valueOf(selected_ticket_qty[i]));
                        tickettypecontoller.setTicketPrice(cursor
                                .getDouble(cursor.getColumnIndex("ItemPrice")));
                        // tickettypecontoller.setTicketQuantity(cursor.getInt(cursor.getColumnIndex("TicketQuantity")));
                        tickettypecontoller.setTicketsEventId(cursor
                                .getString(cursor.getColumnIndex("EventId")));
                        tickettypecontoller.setTicketFeeSetting(cursor
                                .getString(cursor.getColumnIndex("ServiceFee")));
                        tickettypecontoller.setIsTicketTaxable(cursor.getString(cursor
                                .getColumnIndex(DBFeilds.ADDED_ITEM_IS_TAX_APPLICABLE)));
                        tickettypecontoller.setTicketType(cursor.getString(cursor
                                .getColumnIndex(DBFeilds.ADDED_ITEM_TYPENAME)));
                        tickettypecontoller.setBLFeeAmount(cursor.getString(cursor
                                .getColumnIndex(DBFeilds.ADDED_ITEM_BL_FEE)));
                        tickettypecontoller
                                .setTicketTypeId(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_TYPE)));
                        tickettypecontoller
                                .setTicketPaymentType(cursor.getString(cursor.getColumnIndex("ItemPaymentType")));
                        selected_ticket_data.put(Integer.valueOf(i),
                                tickettypecontoller);
                        String fee= getItemPrice(cursor,selected_ticket_qty[cursor.getPosition()]);
                        //double feee=cursor.getDouble(cursor.getColumnIndex("ItemPrice"));
                        if(selected_ticket_qty[cursor.getPosition()]>0) {
                            viewholder.selected_ticket_price.setText(
                                    Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+fee);
                        }else{
                            viewholder.selected_ticket_price.setVisibility(View.GONE);
                        }

                    }
                    if (selected_ticket_data.size() > 0
                            || selected_item_data.size() > 0) {
                        img_cart.setVisibility(View.VISIBLE);
                        //no_of_cart = selected_ticket_data.size();
                        txt_cartno.setText(String.valueOf(no_of_cart));
                    } else {
                        img_cart.setVisibility(View.GONE);
                    }
                    ticketAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
            View v = inflater.inflate(R.layout.selfcheckin_ticketlist_item, parent, false);
            TicketAdapter.ViewHolder holder = new TicketAdapter.ViewHolder();
            holder.t_name = (TextView) v.findViewById(R.id.txtticketname);
            holder.selected_ticket_price =(TextView) v.findViewById(R.id.txt_selected_ticket_price);
            holder.t_line_items = (TextView) v.findViewById(R.id.packege_line_items);
            holder.t_price = (TextView) v.findViewById(R.id.txtticketprice);
            holder.txt_isPackage=(TextView) v.findViewById(R.id.txt_isPackage);
            holder.lay_isPackage=(LinearLayout) v.findViewById(R.id.lay_isPackage);
            holder.fram_soldout=(FrameLayout)v.findViewById(R.id.fram_soldout);
            holder.lay_buttons=(LinearLayout)v.findViewById(R.id.lay_buttons);
            holder.btn_add = (Button) v.findViewById(R.id.btnaddqty);
            holder.btn_minus = (Button) v.findViewById(R.id.btnminusqty);
            holder.txt_closed = (TextView)v.findViewById(R.id.txt_closed);
            holder.t_qty = (TextView) v.findViewById(R.id.txteqty);
           /* holder.t_price.setTypeface(Util.droid_boldItalic);
            holder.t_name.setTypeface(Util.droid_bold);
            holder.t_type.setTypeface(Util.OpenSans);
            holder.t_availabilty.setTypeface(Util.droid_boldItalic);
            holder.btn_minus.setTypeface(Util.droid_bold);
            holder.btn_add.setTypeface(Util.droid_bold);
            holder.txt_isPackage.setTypeface(Util.droid_boldItalic);
            holder.txt_closed.setTypeface(Util.droid_boldItalic);*/
            v.setTag(holder);

            return v;
        }
    }

}
