package com.globalnest.scanattendee;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.TextViewCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.globalnest.database.DBFeilds;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class HideandUnhideTicketsActivity extends BaseActivity implements View.OnClickListener{
    TicketAdapter ticketAdapter;
    String requestType = "",whereClause="";
    ListView itemView;
    Cursor c_ticket;
    TextView txtnosearch;
    String where_condition="";
    List<Integer> selectedItemsPositions;
    boolean hideall=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCustomContentView(R.layout.selfmanage_tickets_layout);
    }
    @Override
    public void setCustomContentView(int layout) {
        try {
            activity = this;
            View v = inflater.inflate(layout, null);
            linearview.addView(v);
            txt_title.setText("Hide/UnHide Tickets");
            img_menu.setImageResource(R.drawable.back_button);
            img_setting.setVisibility(View.GONE);
            img_search.setVisibility(View.VISIBLE);
            img_setting.setImageResource(R.drawable.dashboardrefresh);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            back_layout.setOnClickListener(HideandUnhideTicketsActivity.this);

            img_cart = (FrameLayout) findViewById(R.id.imgcatr);
            // itemView = (ListView) linearview.findViewById(R.id.ticketpager);
            itemView = (ListView) linearview.findViewById(R.id.selfcheckinticketpager);
            txtnosearch = (TextView) linearview.findViewById(R.id.txtnosearch);
           /* txt_hidenow.setText(Html.fromHtml("<u>"+"Hide All"+"</u>"));
            txt_hidenow.setVisibility(View.VISIBLE);
            txt_hidenow.setOnClickListener(this);*/
            selfcheckinonlysave.setVisibility(View.VISIBLE);
            selfcheckinonlysave.setText("Hide All");
            selfcheckinonlysave.setOnClickListener(this);
            where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package')"
                    +" AND (ItemDetails.ItemId=HideItems.Item_Poolid)";
            whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "' AND "+where_condition;
            //c_ticket = Util.db.getEventItems(whereClause);//getOnsiteTicketCursor
            c_ticket = Util.db.getHideTicketCursor(whereClause);
            AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
            ticketAdapter = new TicketAdapter(this, c_ticket);
            requestType = "TICKET LIST";

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
        search_view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search_view.setFocusable(true);
                search_view.setHint("Search by Ticket Name...");
                doSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search_view.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH&&!search_view.getText().toString().trim().isEmpty()) {

                    doSearch();
                    //SortFunction(R.id.editsearchrecord, is_buyer);
                    return true;
                }else if(search_view.getText().toString().trim().isEmpty()){
                    Toast.makeText(HideandUnhideTicketsActivity.this, "Please Enter Text!", Toast.LENGTH_LONG).show();
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
                        search_view.setFocusable(true);
                    }
                }
                return false;
            }
        });
        img_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(HideandUnhideTicketsActivity.this);
                back_layout.setVisibility(View.VISIBLE);
                top_layout.setBackgroundResource(R.color.green_top_header);
                back_layout.setBackgroundResource(R.color.green_top_header);
                search_layout.setVisibility(View.GONE);
                search_view.setText("");
            }
        });
        img_search.setOnClickListener(new View.OnClickListener() {
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
            }
        });
    }
    private void doSearch(){
        String sString= search_view.getText().toString().toLowerCase().trim();
        if(sString.contains("'")){
            sString=sString.replace("'","''");
        }
        String where="";
        if (!search_view.getText().toString().trim().isEmpty()) {
            where =  " AND ItemName like" +"'%"+sString+"%'";
            String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND ((ItemDetails.ItemId = HideItems.Item_Poolid)) "+where;
            whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
            c_ticket = Util.db.getTicketCursor(whereClause);

            //getFilterOrderCursor(whereClause);
        }else{
            String where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package') AND ((ItemDetails.ItemId = HideItems.Item_Poolid)) ";
            whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "'  AND "+where_condition;
            c_ticket = Util.db.getTicketCursor(whereClause);
        }
        if (c_ticket.getCount() > 0) {
            ticketAdapter = new TicketAdapter(HideandUnhideTicketsActivity.this, c_ticket);
            itemView.setAdapter(ticketAdapter);
            txtnosearch.setVisibility(View.GONE);
            itemView.setVisibility(View.VISIBLE);
            ticketAdapter.changeCursor(c_ticket);
            ticketAdapter.notifyDataSetChanged();
        } else {
            txtnosearch.setVisibility(View.VISIBLE);
            itemView.setVisibility(View.GONE);
        }
    }
    /*
        public void setListViewData() {

            whereClause = " where EventId='" + checked_in_eventId + "'";
            // c_ticket = Util.db.getEventItems(checked_in_eventId);
            c_ticket = Util.db.getHideTicketCursor(whereClause);
            ticketAdapter = new TicketAdapter(HideandUnhideTicketsActivity.this, c_ticket);
            requestType = "TICKET LIST";
            if (c_ticket.getCount() > 0) {
                itemView.setAdapter(ticketAdapter);
                itemView.setVisibility(View.VISIBLE);
                */
/*txt_no_ticket.setVisibility(View.GONE);
            loadtickets.setVisibility(View.GONE);*//*

        } else {
            itemView.setVisibility(View.GONE);
            */
/*txt_no_ticket.setVisibility(View.VISIBLE);
            loadtickets.setVisibility(View.GONE);*//*

        }

    }
*/
    @Override
    public void onClick(View v) {
        if(v==back_layout){
            Intent i = new Intent(HideandUnhideTicketsActivity.this, ManageTicketActivity.class);
            // i.putExtra("Type", "Ticket");
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            //startActivityForResult(i, Util.DASHBORD_ONACTIVITY_REQ_CODE);
        }else if(v==selfcheckinonlysave){
            if(!hideall){
                hideall=true;
                selfcheckinonlysave.setText("UnHide All");
            }else {
                selfcheckinonlysave.setText("Hide All");
                hideall=false;
            }
            where_condition = "(ItemDetails.ItemPoolId = ItemPoolDetails.ItemPoolId) AND (ItemPoolDetails.ItemPool_Ticketed_Sessions__c = 'true' OR ItemDetails.ItemTypeName = 'Package')"
                    +" AND (ItemDetails.ItemId=HideItems.Item_Poolid)";
            whereClause = ",ItemPoolDetails,HideItems where ItemDetails.EventId='" + checked_in_eventId + "' AND "+where_condition;
            //c_ticket = Util.db.getEventItems(whereClause);//getOnsiteTicketCursor
            if(c_ticket!=null&&c_ticket.getCount()>0){
                selectedItemsPositions.clear();
                for(int i=0;i<c_ticket.getCount();i++){
                    c_ticket.moveToPosition(i);
                    String itemPoolId = c_ticket.getString(c_ticket.getColumnIndex(DBFeilds.ADDED_ITEM_ID));
                    if(!hideall){
                        //Util.db.InsertandUpdateHideStatus("true", itemPoolId, checked_in_eventId);
                        Util.db.InsertandUpdateHideStatus("false", itemPoolId, checked_in_eventId);

                    }else {
                        //Util.db.InsertandUpdateHideStatus("false", itemPoolId, checked_in_eventId);
                        Util.db.InsertandUpdateHideStatus("true", itemPoolId, checked_in_eventId);

                    }
                }
                c_ticket = Util.db.getHideTicketCursor(whereClause);
                AppUtils.displayLog("----------------Item Count------------",":"+c_ticket.getCount());
                ticketAdapter = new TicketAdapter(this, c_ticket);
                itemView.setAdapter(ticketAdapter);
                ticketAdapter.notifyDataSetChanged();
                itemView.setVisibility(View.VISIBLE);
            }

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){

            try {
                Intent i = new Intent(HideandUnhideTicketsActivity.this, ManageTicketActivity.class);
                // i.putExtra("Type", "Ticket");
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return super.onKeyDown(keyCode, event);
    }
    class ViewHolder  {
        TextView t_name;
        TextView  t_line_items, t_price,
                txt_isPackage,txt_closed;//,t_qty
        LinearLayout lay_buttons,lay_isPackage;
        //EditText edit_price;
        FrameLayout fram_soldout;
        ToggleButton toggle_hide;
        public boolean needInflate;


        public ViewHolder(View v) {
        }
    }
    private class TicketAdapter extends CursorAdapter {



        @SuppressWarnings("deprecation")
        public TicketAdapter(Context context, Cursor c) {
            super(context, c);
            selectedItemsPositions = new ArrayList<>();
            AppUtils.displayLog("----------------Item Name------------",":"+c.getCount());
        }

        @Override
        public void bindView(View parent_view, Context context,  Cursor cursor) {

            final View v;
            if (parent_view==null) {
                v = newView(context, cursor, null);
            }
            else if (((ViewHolder)parent_view.getTag()).needInflate) {
                v = newView(context, cursor, null);

            }else{
                v = parent_view;
            }


            ViewHolder viewholder = (ViewHolder) v.getTag();
           /* if (!cursor.getString(cursor.getColumnIndex("ItemImageUrl"))
                    .isEmpty()) {

            }*/
            AppUtils.displayLog("----------------Item Name------------",":"+cursor.getString(cursor.getColumnIndex("ItemName")));
            viewholder.t_name.setText(cursor.getString(cursor
                    .getColumnIndex("ItemName")));
            TextViewCompat.setAutoSizeTextTypeWithDefaults(viewholder.t_name,TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
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



            //viewholder.t_qty.setError(null);



            if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_STATUS))).equalsIgnoreCase("Closed")){
                viewholder.txt_closed.setVisibility(View.VISIBLE);
            }else{
                viewholder.txt_closed.setVisibility(View.GONE);
            }


            viewholder.toggle_hide.setTag(cursor.getPosition());
//&&!selectedItemsPositions.contains(cursor.getPosition())
            if(NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.HIDE_ITEMSTATUS))).equals("false")){
                viewholder.toggle_hide.setChecked(true);
                // selectedItemsPositions.add(cursor.getPosition());
            }else {
                if (selectedItemsPositions.contains(cursor.getPosition()))
                    viewholder.toggle_hide.setChecked(true);
                else
                    viewholder.toggle_hide.setChecked(false);
                // viewholder.toggle_hide.setChecked(Boolean.valueOf(cursor.getString(cursor.getColumnIndex(DBFeilds.HIDE_ITEMSTATUS))));
            }

            // viewholder.toggle_hide.setChecked(Util.db.getHideStatus(checked_in_eventId,cursor.getString(cursor.getColumnIndex(DBFeilds.ADDED_ITEM_POOLID))));
            viewholder.toggle_hide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int i = ((Integer) buttonView.getTag()).intValue();
                    Cursor c = ticketAdapter.getCursor();
                    c.moveToPosition(i);
                    String itemPoolId = c.getString(c.getColumnIndex(DBFeilds.ADDED_ITEM_ID));
                    if (isChecked) {
                        Util.db.InsertandUpdateHideStatus(String.valueOf(!isChecked),
                                itemPoolId,checked_in_eventId);
                        //check whether its already selected or not
                        if (!selectedItemsPositions.contains(i))
                            selectedItemsPositions.add(i);
                    } else {
                        Util.db.InsertandUpdateHideStatus("true",
                                itemPoolId,checked_in_eventId);
                        //remove position if unchecked checked item
                        selectedItemsPositions.remove((Object) i);
                    }
                    //ticketAdapter.notifyDataSetChanged();
                   /* c_ticket= Util.db.getHideTicketCursor(whereClause);
                    itemView.setAdapter(ticketAdapter);2
                    ticketAdapter.changeCursor(c_ticket);
                    ticketAdapter.notifyDataSetChanged();*/
                }
            });

        }

        @Override
        public View newView(Context arg0, Cursor arg1, ViewGroup parent) {
            View v = inflater.inflate(R.layout.hideticketslist_item, parent, false);
            ViewHolder holder = new ViewHolder(v);
            holder.t_name = (TextView) v.findViewById(R.id.txtticketname);
            holder.t_line_items = (TextView) v.findViewById(R.id.packege_line_items);
            holder.t_price = (TextView) v.findViewById(R.id.txtticketprice);
            holder.txt_isPackage=(TextView) v.findViewById(R.id.txt_isPackage);
            holder.lay_isPackage=(LinearLayout) v.findViewById(R.id.lay_isPackage);
            holder.fram_soldout=(FrameLayout)v.findViewById(R.id.fram_soldout);
            holder.lay_buttons=(LinearLayout)v.findViewById(R.id.lay_buttons);
            holder.txt_closed = (TextView)v.findViewById(R.id.txt_closed);
            holder.toggle_hide =(ToggleButton)v.findViewById(R.id.toggle_hide);
            //holder.t_qty = (TextView) v.findViewById(R.id.txteqty);
            v.setTag(holder);

            return v;
        }
    }

    @Override
    public void doRequest () {
        String access_token = sfdcddetails.token_type + " "
                + sfdcddetails.access_token;
        try {
            if (requestType.equalsIgnoreCase("TICKET LIST")) {
                String url = sfdcddetails.instance_url
                        + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="
                        + checked_in_eventId;
                postMethod = new HttpPostData("Refreshing Tickets...", url, null,
                        access_token, HideandUnhideTicketsActivity.this);
                postMethod.execute();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void parseJsonResponse (String response){

    }

    @Override
    public void insertDB () {

    }
}

