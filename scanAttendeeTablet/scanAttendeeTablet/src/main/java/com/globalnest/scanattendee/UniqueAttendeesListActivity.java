package com.globalnest.scanattendee;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.ExternalSettings;
import com.globalnest.mvc.OfflineSyncFailuerObject;
import com.globalnest.mvc.OfflineSyncResController;
import com.globalnest.mvc.OfflineSyncSuccessObject;
import com.globalnest.mvc.TStatus;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class UniqueAttendeesListActivity extends BaseActivity {

    ListView _attendee_list;
    String whereClause,checked_time,attendee_firstname,attendee_lastname,attendee_emailid;
    Cursor attendee_cursor;
    private TextView att_ticket, att_name,att_order,att_parentticket_name;
    ListCheckInAdapter _adapter;
    private HashMap<String, Boolean> tickets_register = new HashMap<String, Boolean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        attendee_firstname = getIntent().getStringExtra(DBFeilds.ATTENDEE_FIRST_NAME);
        attendee_lastname = getIntent().getStringExtra(DBFeilds.ATTENDEE_LAST_NAME);
        attendee_emailid = getIntent().getStringExtra(DBFeilds.ATTENDEE_EMAIL_ID);
        if(attendee_firstname.contains("'")){

        }

        setCustomContentView(R.layout.activity_buyer_level_attendee_list);
        if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
            whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
            attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
        }else{

            whereClause =" where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
                    attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
        }

        if(attendee_cursor.getCount()>0)
        {
            _adapter=new ListCheckInAdapter();
            _attendee_list.setAdapter(_adapter);
        }
        txt_title.setText(attendee_firstname+" "+attendee_lastname+" ("+attendee_cursor.getCount()+")");
        _attendee_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position,long arg3) {
                Cursor c1=_adapter.getItem(position);
                //Log.i("---------------Badgable String---------------",":"+c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)));
                //if (c1.getString(c1.getColumnIndex(DBFeilds.ATTENDEE_BADGABLE)).equalsIgnoreCase("B - Badge")) {
                Intent i = new Intent(UniqueAttendeesListActivity.this, AttendeeDetailActivity.class);
                i.putExtra(Util.EVENT_ID, c1.getString(c1.getColumnIndex("Event_Id")));
                i.putExtra(Util.ATTENDEE_ID, c1.getString(c1.getColumnIndex("Attendee_Id")));
                i.putExtra(Util.ORDER_ID, c1.getString(c1.getColumnIndex("Order_Id")));
                startActivity(i);
                //}// removed for checkin report ;for non badge attendees

            }
        });
        Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
        externalSettings = new ExternalSettings();
        if(!Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, "").isEmpty()){
            externalSettings = new Gson().fromJson(Util.external_setting_pref.getString(sfdcddetails.user_id+checked_in_eventId, ""), ExternalSettings.class);
        }

        txt_save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.i("Bar code button done clicked-----","Size  of ticket register="+ tickets_register.size());
                if(tickets_register.size()>0) {
                    if (isOnline()) {
                        doRequest();
                    } else {
                        //startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
                        for (String key : tickets_register.keySet()) {
                            generateCsvFile(key.trim());
                        }
                    }
                }else{
                    AlertDialogCustom d = new AlertDialogCustom(UniqueAttendeesListActivity.this);
                    d.setParamenters("Alert !", "No records selected, please select a record for Check-in/check-out", null, null, 1, false);
                    d.show();
                }
            }
        });
        back_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent i = new Intent();
                setResult(2017, i);
                finish();

            }
        });

    }

    /* (non-Javadoc)
     * @see com.globalnest.network.IPostResponse#doRequest()
     */
    @Override
    public void doRequest() {
        if(isOnline()){
            String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
            postMethod = new HttpPostData("",setTicketCheckinUrl(), makeCheckin().toString(), access_token, UniqueAttendeesListActivity.this);
            postMethod.execute();

        }else{
            startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);

        }
    }

    /* (non-Javadoc)
     * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
     */
    @Override
    public void parseJsonResponse(String response) {
        try{
            if(!isValidResponse(response)){
                openSessionExpireAlert(errorMessage(response));
            }
            OfflineSyncResController offlineResponse = new Gson().fromJson(response, OfflineSyncResController.class);
            List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
            boolean isFreeSession = false;
            if(scanticks.size() > 0){
                isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
            }
            //JSONObject obj = new JSONObject(response);
            ////Log.i("Attendee List Activity", "requestType=  " + requestType);
            if (NullChecker(offlineResponse.ErrorMsg).isEmpty()) {
                //Log.i("Attendee List Activity", "error is null");
				/*JSONArray success = obj.optJSONArray("SuccessTickets");
				JSONArray failure = obj.optJSONArray("FailureTickets");*/
                String dialogtime = "";
                if (offlineResponse.SuccessTickets.size() > 0) {
                    for (OfflineSyncSuccessObject success : offlineResponse.SuccessTickets) {
                        //Log.i("Attendee List Activity","success is not null");
                        boolean status =success.Status;// success.optJSONObject(i).optBoolean("Status");
                        String time = success.TimeStamp;//success.optJSONObject(i).optString("TimeStamp");
                        String attendee_Id = success.STicketId.Ticket__c;//success.optJSONObject(i).optString("STicketId");
                        //	dialogtime = Util.db_date_format1.format(Util.date_format_sec.parse(time));
                        //	time = Util.new_db_date_format.format(Util.date_format_sec.parse(time));
                        ////Log.i("Attendee List Activity", "Database date="+ time + "dialog time=" + dialogtime);

                        if(isFreeSession){
                            List<TStatus> session_attendee = new ArrayList<TStatus>();
                            session_attendee.add(success.STicketId);
                            Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                        }else{
                            Util.db.updateCheckedInStatus(success.STicketId,checked_in_eventId);
                        }
                    }

                    if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
                        whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
                        attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
                    }else{

                        whereClause =" where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
                                attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
                    }
                    _adapter=new ListCheckInAdapter();
                    _attendee_list.setAdapter(_adapter);
                    finish();
                    //AlertDialogCustom dialog =new AlertDialogCustom(UniqueAttendeesListActivity.this);
                    //dialog.setParamenters("Status","Checked In/Out Successfully", null, null, 1, true);
                    //dialog.show();
                } else {
                    String failed_attendees = ITransaction.EMPTY_STRING;
                    String attendee_name = ITransaction.EMPTY_STRING;
                    String statusdisplay=" Checked-Out ";
                    int i =0 ;
                    for (OfflineSyncFailuerObject failure : offlineResponse.FailureTickets) {
                        boolean status = Boolean.valueOf(failure.Status);//failure.optJSONObject(i).optBoolean("Status");
                        String time = failure.TimeStamp;//failure.optJSONObject(i).optString("TimeStamp");
                        String attendee_Id = failure.STicketId;
                        if(status){
                            statusdisplay =" Checked-In ";
                        }//failure.optJSONObject(i).optString("STicketId");
                        dialogtime = Util.change_US_ONLY_DateFormatWithSEC(time, checkedin_event_record.Events.Time_Zone__c);

                        if(isFreeSession){
                            List<TStatus> session_attendee = new ArrayList<TStatus>();
                            session_attendee.add(failure.tStaus);
                            Util.db.InsertAndUpdateSessionAttendees(session_attendee, checked_in_eventId);
                        }else{
                            Util.db.updateCheckedInStatus(failure.tStaus,checked_in_eventId);
                        }
                        failed_attendees =failed_attendees+ Util.db.getAttendeeNameWithId(failure.tStaus.Ticket__c);
                        if(i != (offlineResponse.FailureTickets.size()-1)){
                            failed_attendees = failed_attendees+" , ";
                        }
                        i++;

                    }

                    if(NullChecker(getIntent().getStringExtra(Util.ACCESS_KEY_NAME)).equalsIgnoreCase(Util.ORDERS)){
                        whereClause = " where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
                        attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
                    }else{
                        whereClause =" where Event_Id='" + checked_in_eventId+ "' AND "+DBFeilds.ATTENDEE_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim() + "' OR "+DBFeilds.ATTENDEE_BADGE_PARENT_ID+" = '" + getIntent().getStringExtra(Util.ATTENDEE_ID).trim()+"'";
                                attendee_cursor=Util.db.getAttendeeswithUnique(attendee_firstname,attendee_lastname,attendee_emailid,checked_in_eventId);
                    }
                    _adapter=new ListCheckInAdapter();
                    _attendee_list.setAdapter(_adapter);

                    Util.setCustomAlertDialog(UniqueAttendeesListActivity.this);
                    Util.txt_dismiss.setVisibility(View.GONE);
                    Util.txt_okey.setText("Ok");
                    Util.txt_okey.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            // ticket_dialog.dismiss();
                            Util.alert_dialog.dismiss();
                            //finish();
                        }
                    });
                    Util.openCustomDialog("Alert",attendee_name + " is Already" +statusdisplay+ "\n at Time:"+dialogtime);

                    //Util.openCustomDialog("Check-in/out Failed",failed_attendees+". These attendees are already checked-in/out some other place in the event, Please check their status and try again.");
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        // Util.openCustomDialog("Alert", ""+response);


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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent();
            setResult(2017, i);
            finish();

            return true;
        }
        return false;
    }
    @Override
    public void setCustomContentView(int layout) {
        activity = this;
        v=inflater.inflate(layout, null);
        linearview.addView(v);
        img_socket_scanner.setVisibility(View.GONE);
        img_scanner_base.setVisibility(View.GONE);
        txt_title.setText("Buyer Tickets");
        img_menu.setImageResource(R.drawable.back_button);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        _attendee_list = (ListView) linearview.findViewById(R.id.buyer_level_attendee_list);
        txt_save.setVisibility(View.VISIBLE);
        txt_save.setText("Checkin");
        txt_save.setTypeface(Util.roboto_bold);
        //txt_save.setOnClickListener(this);
        //img_checkin_done = (ImageView) linearview.findViewById(R.id.btncheckindone);
        //img_checkin_cancel = (ImageView) linearview.findViewById(R.id.btncheckincancel);

        //buyer_name=(TextView) linearview.findViewById(R.id.buyer_name);

    }


    private class ListCheckInAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return attendee_cursor.getCount();
        }

        @Override
        public Cursor getItem(int position) {
            attendee_cursor.moveToPosition(position);
            return attendee_cursor;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            try {
                final ViewHolder holder =new ViewHolder();
                attendee_cursor.moveToPosition(position);
                View v = inflater.inflate(R.layout.unique_level_attedee_list_item,null);
                //txtticketnum = (TextView) v.findViewById(R.id.ticketnum);
                att_name = (TextView) v.findViewById(R.id.attendee_buyer_name);
                att_ticket=(TextView) v.findViewById(R.id.attendee_ticket_name);
                att_parentticket_name=(TextView) v.findViewById(R.id.attendee_ticket_parentname);
                att_order=(TextView) v.findViewById(R.id.attendee_order_name);
                holder.attendee_checkin_checkbox = (CheckBox) v.findViewById(R.id.attendee_checkin_checkbox);
                holder.statusbar =(View) v.findViewById(R.id.statusbar);
                holder.attendee_checkin_checkbox.setFocusable(false);

                att_name.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_FIRST_NAME))+" "+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_LAST_NAME)));

                String parent_id = Util.db.getItemPoolParentId(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                if(!NullChecker(parent_id).isEmpty()){
                    String package_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
                    att_ticket.setText(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+" ( "+package_name+" ) "+//"-"+
                            Html.fromHtml("<font <small> ("+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+") </small> </font>"));
                }else{
                    att_ticket.setText(Html.fromHtml("<Html> <font>"+attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEMPOOL_NAME))+//"-"+
                            " <small> (" +attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_TIKCET_NUMBER))+") </small> </font> </Html>"));
                }
                if(!Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))).isEmpty()){
                    //att_parentticket_name.setVisibility(View.VISIBLE);
                    String mergeparentname=Util.db.getAttendeeBadgeParentTicketName(Util.NullChecker(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_BADGE_PARENT_ID))));
                   att_ticket.setText(att_ticket.getText().toString()+"Merged with ("+mergeparentname+")"); //att_parentticket_name.setText("Merged with "+mergeparentname);
                }
                att_order.setText("ORDER ID : "+ attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_NUMBER))+"" );
                boolean isFreeSession = false;
                List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
                if(scanticks.size() > 0){
                    isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
                }

                String status = ITransaction.EMPTY_STRING;
                if(isFreeSession){
                    status = String.valueOf(Util.db.SessionCheckInStatus(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)),Util.db.getSwitchedONGroupId(checked_in_eventId)));
                }else{
                    status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
                }
                if (!Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()) {
                    holder.statusbar.setVisibility(View.VISIBLE);
                    if (status.equals("true")) {
                        holder.statusbar.setBackgroundColor(getResources().getColor(R.color.green_connected));
                    } else if (status.equals("false")) {
                        holder.statusbar.setBackgroundColor(getResources().getColor(R.color.orange_bg));
                    } else {
                        holder.statusbar.setBackgroundColor(getResources().getColor(R.color.gray_color));
                    }
                }

				/*if (NullChecker(status).equalsIgnoreCase("true")) {
					holder.attendee_checkin_checkbox.setChecked(true);
					//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_h);
				} else {
					holder.attendee_checkin_checkbox.setChecked(false);
					//attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_n);
				}*/

                if(tickets_register.containsKey(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))){
                    holder.attendee_checkin_checkbox.setChecked(Boolean.valueOf(tickets_register.get(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)))));
                }
                //holder.attendee_checkin_checkbox.setTag(position);
                holder.attendee_checkin_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        boolean withoutalert=true;
                        attendee_cursor.moveToPosition(position);
                        if (isChecked) {
                            String tstatus = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId, Util.db.getSwitchedONGroupId(checked_in_eventId));
                            String orderstatus = Util.db.getOrderStatuswithAttendee(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ORDER_ID)), checked_in_eventId);
                            String item_pool_id = Util.db.getItemPoolID(attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id")));
                            if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
                                holder.attendee_checkin_checkbox.setChecked(false);
                                showScannedTicketsAlert("Please TurnON at least one session for scanning.",true);
                            }
                            else if (!Util.db.isItemPoolSwitchON(item_pool_id, checked_in_eventId)) {
                                withoutalert = false;
								/*String item_pool_name = "";
								String parent_id = Util.db.getItemPoolParentId(item_pool_id, checked_in_eventId);
								if (!NullChecker(parent_id).isEmpty()) {
									item_pool_name = Util.db.getItem_Pool_Name(parent_id, checked_in_eventId);
								} else {
									item_pool_name = Util.db.getItem_Pool_Name(item_pool_id, checked_in_eventId);
								}*/
                                holder.attendee_checkin_checkbox.setChecked(false);
                                openScanSettingsAlert(UniqueAttendeesListActivity.this,item_pool_id,
                                        UniqueAttendeesListActivity.class.getName());
                                Util.txt_dismiss.setVisibility(View.GONE);
                                //showCustomToast(UniqueAttendeesListActivity.this, "Sorry! You are not allowed to check-in for \"" + item_pool_name + "\".", R.drawable.img_like, R.drawable.toast_redrounded, false);

                            } else if(Boolean.valueOf(tstatus) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                                withoutalert = false;
                                holder.attendee_checkin_checkbox.setChecked(false);
                                showMessageAlert(getString(R.string.checkin_only_msg),false);
                            }else if (!externalSettings.quick_checkin && !orderstatus.equalsIgnoreCase("Fully Paid")) {
                                withoutalert = false;
                                final boolean checked = isChecked;
                                Util.setCustomAlertDialog(UniqueAttendeesListActivity.this);
                                Util.alert_dialog.setCancelable(false);
                                Util.txt_okey.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        startCheckclickupdate(holder.attendee_checkin_checkbox, true, attendee_cursor);
                                    }
                                });
                                Util.txt_dismiss.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Util.alert_dialog.dismiss();
                                        holder.attendee_checkin_checkbox.setChecked(false);									}
                                });
                                Util.openCustomDialog("Alert", "This Order Status is " + orderstatus + "! \n Do you still want to Continue?");

                            }
                            if(withoutalert){
                                startCheckclickupdate(holder.attendee_checkin_checkbox, true, attendee_cursor);
                            }
                        }
                    }
                });
                return v;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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
                    //obj.put("isCHeckIn", tickets_register.get(key));
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

                    if (tickets_register.get(key)){
                        obj.put("isCHeckIn", tickets_register.get(key));
                    }else{
                        obj.put("isCHeckIn", false);
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

    class ViewHolder
    {
        CheckBox attendee_checkin_checkbox;
        View statusbar;
    }

    protected void onResume() {
        super.onResume();

    }
    public void startCheckclickupdate(CheckBox attendee_checkin_checkbox, Boolean isChecked, Cursor attendee_cursor){
        boolean isFreeSession = false;
        List<ScannedItems> scanticks = Util.db.getSwitchedOnScanItem(checked_in_eventId);
        if(scanticks.size() > 0){
            isFreeSession = Util.db.isItemPoolFreeSession(scanticks.get(0).BLN_Item_Pool__c, checked_in_eventId);
        }if(isFreeSession){
            String ticket_id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
            String status = String.valueOf(Util.db.SessionCheckInStatus(ticket_id,Util.db.getSwitchedONGroupId(checked_in_eventId)));
            if(Boolean.valueOf(status) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                attendee_checkin_checkbox.setChecked(false);
                showMessageAlert(getString(R.string.checkin_only_msg),false);
            }else{
                if(tickets_register.containsKey(ticket_id)){
                    tickets_register.remove(ticket_id);
                }
                String check_box_value = String.valueOf(attendee_checkin_checkbox.isChecked());
                if(!NullChecker(status).equalsIgnoreCase(check_box_value)){
                    tickets_register.put(ticket_id, !Boolean.valueOf(status));
                }
            }
        }else if(!Util.db.isItemPoolSwitchON(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId)){
            String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);
            if (NullChecker(status).equalsIgnoreCase("true")) {
                attendee_checkin_checkbox.setChecked(true);
                //attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_h);
            } else {
                attendee_checkin_checkbox.setChecked(false);
                //attendee_checkin_checkbox.setBackgroundResource(R.drawable.checkmark_n);
            }
            //openScanSettingsAlert(UniqueAttendeesListActivity.this,attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)),UniqueAttendeesListActivity.class.getName());
        }else{
            String ticket_id = attendee_cursor.getString(attendee_cursor.getColumnIndex("Attendee_Id"));
            String status = Util.db.getTStatusBasedOnGroup(attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)), attendee_cursor.getString(attendee_cursor.getColumnIndex(DBFeilds.ATTENDEE_ITEM_POOL_ID)), checked_in_eventId);

            if(Boolean.valueOf(status) && Util.checkin_only_pref.getBoolean(sfdcddetails.user_id+checked_in_eventId, false)){
                attendee_checkin_checkbox.setChecked(false);
                showMessageAlert(getString(R.string.checkin_only_msg),false);
            }else{
                if(tickets_register.containsKey(ticket_id)){
                    tickets_register.remove(ticket_id);
                }
                String check_box_value = String.valueOf(attendee_checkin_checkbox.isChecked());
                tickets_register.put(ticket_id, !Boolean.valueOf(NullChecker(status)));

                //if(!NullChecker(status).isEmpty()){
                    //tickets_register.put(ticket_id, !Boolean.valueOf(status));
                //}
            }
        }
    }

}
