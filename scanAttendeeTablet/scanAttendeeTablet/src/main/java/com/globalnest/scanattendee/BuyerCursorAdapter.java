//  ScanAttendee Android
//  Created by Ajay on May 5, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.globalnest.database.DBFeilds;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class BuyerCursorAdapter extends CursorAdapter {
  
	public class ViewList{
		View v;
		int id;

		@Override
		public String toString() {
			return "ViewList [v=" + v + ", id=" + id + "]";
		}
	}

	// EventObjects _event=new EventObjects();
	private Activity _activity;
	private SFDCDetails _sfdc;
	private Context _context;
	// Cursor att_cursor = null;
	private String checkin_type,checked_in_event_id;
	private Cursor att_cursor = null;
	private boolean _is_order_id;
	private Map<Integer,ViewList> viewArray=new HashMap<Integer,ViewList>();
	private ArrayList<ProgressBar> front_checkin_bar_array=new ArrayList<ProgressBar>();
	private Cursor c_new;
	private String group_id = ITransaction.EMPTY_STRING;
	public class ViewHolder {
		private TextView attName, attComp,txtbuyer_orderid,txtbuyer_email,txt_att_checkin, txt_delete,txt_image;
		private ProgressBar delete_progress, checkin_progress,checkinprogressbuyer_front;
		private Button count;
		private ImageView image_att;
		private RelativeLayout checkin_layout;
		public boolean needInflate;
		

	}

	public BuyerCursorAdapter(Context context, Cursor c,Activity activity,SFDCDetails sfdc,boolean is_order_id,String event_id,String group_id) {
		super(context, c);
		this._activity=activity;
		this._sfdc=sfdc;
		this._context=context;
		this._is_order_id=is_order_id;
		this.c_new = c;
		this.checked_in_event_id = event_id;
		this.group_id = group_id;
	}
	

	public void changeCursor(Cursor cursor){
		super.changeCursor(cursor);
		this.c_new = cursor;
	}
    @Override
    public int getViewTypeCount() {
        // menu type count
        return 2;
    }

   /* @Override
    public int getItemViewType(int position) {
    	c_new.moveToPosition(position);
        return getCheckInStatus(getCursor(),position);
    }*/


	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		@SuppressWarnings("static-access")
		LayoutInflater inf = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

		View v = inf.inflate(R.layout.buyer_list_item, parent,false);
		ViewHolder holder = new ViewHolder();

		holder.checkin_layout = (RelativeLayout) v.findViewById(R.id.checkin_layout);
		//holder.delete_layout = (RelativeLayout) v.findViewById(R.id.deletelayout);

		holder.txt_delete = (TextView) v.findViewById(R.id.txt_buyer_delete);
		holder.txt_att_checkin = (TextView) v.findViewById(R.id.txt_buyer_checkin);

		//holder.txt_att_checkin = (TextView) v.findViewById(R.id.txt_att_checkin);
		holder.attName = (TextView) v.findViewById(R.id.txtbuyername);
		holder.attComp = (TextView) v.findViewById(R.id.txtbuyercomp);
		holder.txtbuyer_email = (TextView) v.findViewById(R.id.txtbuyer_email);
		holder.txtbuyer_orderid = (TextView) v.findViewById(R.id.txtbuyer_orderid);

		holder.txt_image= (TextView) v.findViewById(R.id.txt_image);
		holder.image_att= (ImageView) v.findViewById(R.id.img_att);
		holder.count = (Button) v.findViewById(R.id.btnbuyercount);

		holder.delete_progress = (ProgressBar) v.findViewById(R.id.deleteprogress);
		holder.checkin_progress = (ProgressBar) v.findViewById(R.id.checkinprogress);
		holder.checkinprogressbuyer_front = (ProgressBar) v.findViewById(R.id.checkinprogressbuyer_front);

		holder.txt_delete.setTypeface(Util.roboto_regular);
		holder.txt_att_checkin.setTypeface(Util.roboto_regular);
		holder.attName.setTypeface(Util.roboto_regular);
		holder.attComp.setTypeface(Util.roboto_regular);
		//holder.att_checkedin.setTypeface(Util.roboto_regular);
		holder.count.setTypeface(Util.roboto_regular, Typeface.BOLD);
		v.setTag(holder);
		return v;

	}

	
	@Override
	public void bindView(View v, Context context, final Cursor cursor) {
		
		final View parent_view;
		if (v==null) {
			parent_view = newView(context, cursor, null);
			ViewList vl=new ViewList();
			vl.v=parent_view;
			vl.id=cursor.getPosition();
			viewArray.put(cursor.getPosition(),vl);

		}
		else if (((ViewHolder)v.getTag()).needInflate) {
			parent_view = newView(context, cursor, null);

		}else{
			parent_view = v;
			ViewList vl=new ViewList();
			vl.v=parent_view;
			vl.id=cursor.getPosition();
			viewArray.put(cursor.getPosition(),vl);
		}

		final ViewHolder holder = (ViewHolder) v.getTag();

		

		//checked_in_event_id=cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID));
		checked_in_event_id=cursor.getString(cursor.getColumnIndex(DBFeilds.USER_EVENT_ID));
		//Log.i("-----Order Cursor Size-----", ":"+cursor.getCount());
		
		if(!AppUtils.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_FIRST_NAME))).trim().isEmpty() && !AppUtils.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_LAST_NAME))).trim().isEmpty())
			holder.txt_image.setText(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_FIRST_NAME)).substring(0, 1).toUpperCase()+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_LAST_NAME)).substring(0, 1).toUpperCase());
		else
			holder.txt_image.setText("NO");
		
		holder.attName.setText(BaseActivity.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_FIRST_NAME)))+" "+BaseActivity.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_LAST_NAME))));
		holder.attComp.setText(BaseActivity.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_COMPANY))));
		holder.txtbuyer_email.setText(BaseActivity.NullChecker(cursor.getString(cursor.getColumnIndex(DBFeilds.USER_EMAIL_ID))));
		//holder.txtbuyer_orderid.setText("Order id: "+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
		// attendee_data_cursor=cursor;


		//for attendees depend on the Mail_id
		////Log.i("^^^^^^^^^^^^^^^^^^^  Attendee Cursor ^^^^^^^^^^^^^^^^^^^",":"+_is_order_id);
		int checkin_count=0,checkout_count = 0;
		if(!_is_order_id)
		{
			String id= cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID));
			//Log.i("^^^^^^^^^^^^^^^^^^^ Attendee Name And GNUSER ID ^^^^^^^^^^^^^^^^^^^",":"+holder.attName.getText().toString()+" :: "+id);
			if(att_cursor!=null){
				att_cursor.close();
			}
			
			att_cursor= Util.db.getAttendeeDataCursorForBuyers(" where Event_Id='"+ checked_in_event_id+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+id+ "'");
			//Cursor check_in_cursor = Util.db.getAttendeeDataCursorForBuyers(" where Event_Id='"+ checked_in_event_id+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+id+ "' AND "+DBFeilds.ATTENDEE_ISCHECKIN+" = 'true'");
			checkin_count = Util.db.getTStatusCountWithOrder(id, checked_in_event_id, String.valueOf(true),group_id);
			/*if(check_in_cursor != null){
				check_in_cursor.close();
			}*/

			//Log.i("^^^^^^^^^^^^^^^^^^^ Attendee Cursor Count ^^^^^^^^^^^^^^^^^^^",":"+att_cursor.getCount());
		}else{
			if(att_cursor!=null){
				att_cursor.close();
			}
			att_cursor= Util.db.getAttendeeDataCursorForBuyers(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "'");
			//Cursor check_in_cursor = Util.db.getAttendeeDataCursorForBuyers(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "' AND "+DBFeilds.ATTENDEE_ISCHECKIN+" = 'true'");
			//Cursor check_out_cursor = Util.db.getAttendeeDataCursorForBuyers(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "' AND "+DBFeilds.ATTENDEE_ISCHECKIN+" = 'false'");
			
			
			checkin_count =  Util.db.getTStatusCountWithOrder(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)), checked_in_event_id, String.valueOf(true),group_id);
			checkout_count = Util.db.getTStatusCountWithOrder(cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)), checked_in_event_id, String.valueOf(false),group_id);
			/*if(check_in_cursor != null){
				check_in_cursor.close();
			}
			if(check_out_cursor != null){
				check_out_cursor.close();
			}*/
		}

		//int a = att_cursor.getCount();
		////Log.i("^^^^^^^^^^^^^^^^^^^  Attendee Cursor ^^^^^^^^^^^^^^^^^^^",":"+a);
		if(att_cursor.getCount()>0)
		{
			//int count=0;
	
           // int b = count;
			// //Log.i("^^^^^^^^^^^^^^^^^^^ Attendee Cursor
			// ^^^^^^^^^^^^^^^^^^^",":"+b);
			if((checkin_count+checkout_count) == 0){
				holder.checkin_layout.setBackgroundResource(R.color.gray_color);
				holder.txt_att_checkin.setText("Check In");
				holder.count.setBackgroundResource(R.drawable.gray_price_bg);
			}else if (checkin_count == att_cursor.getCount()) {
				holder.count.setBackgroundResource(R.drawable.green_price_bg);
				holder.checkin_layout.setBackgroundResource(R.color.orange_bg);
				holder.txt_att_checkin.setText("Check Out");
			} else {
				holder.checkin_layout.setBackgroundResource(R.color.green_button_color);
				holder.txt_att_checkin.setText("Check In");
				holder.count.setBackgroundResource(R.drawable.red_price_bg);
			}

			holder.count.setText(Integer.toString(checkin_count)+"/"+att_cursor.getCount());
		}
		holder.checkin_layout.setTag(cursor.getPosition());
		holder.count.setId(cursor.getPosition());
		holder.count.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cursor.moveToPosition((Integer) holder.count.getId());
				if(cursor.getCount()>0)
				{

					Intent global_intent = new Intent(_activity, GlobalScanActivity.class);
					global_intent.putExtra(Util.SCANDATA, cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)).toCharArray());
					global_intent.putExtra(Util.CLASSNAME, true);
					_activity.startActivity(global_intent);
					/*Intent i=new Intent(_activity,BuyerLevelAttendeeList.class);
					i.putExtra(Util.ORDER_ID, cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)));
					i.putExtra(Util.ACCESS_KEY_NAME,Util.ORDERS);
					i.putExtra(Util.BUYER_NAME, cursor.getString(cursor.getColumnIndex(DBFeilds.USER_FIRST_NAME))+" "+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_LAST_NAME)));
					_activity.startActivity(i);*/
				}

			}

		});

	}


	/*private class DoTicketCheckInFromBase extends SafeAsyncTask<String> {

		private String _body="";
		private int _index=0;
		*//**
		 * 
		 *//*
		public DoTicketCheckInFromBase(String body,int index) {
			this._body=body;
			this._index=index;
		}

		protected void onPreExecute() throws Exception {
			super.onPreExecute();
			if (front_checkin_bar_array.get(_index) != null) {
				front_checkin_bar_array.get(_index).setVisibility(View.VISIBLE);
				//_dialog.show();
			}
		}
		@Override
		public String call() throws Exception {
		
			return postTicketCheckIn(_body);
		}
		protected void onSuccess(String result) throws Exception {
			super.onSuccess(result);
			if(result != null){
				parseJsonResponse(result);	
				notifyDataSetChanged();
			}
		}
	}




	private String postTicketCheckIn(String body) {
		String response = "";
		try {

			HttpParams params = new BasicHttpParams();
			int timeoutconnection = 30000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutconnection);
			int sockettimeout = 30000;
			HttpConnectionParams.setSoTimeout(params, sockettimeout);

			HttpClient _httpclient = new DefaultHttpClient();
			HttpPost _httppost = new HttpPost(_sfdc.instance_url+WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby=" + 
					_sfdc.user_id);
			//Log.i("----------------Post Url-------------",":"+_sfdc.instance_url+WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby=" + _sfdc.user_id);


			_httppost.addHeader("Authorization", _sfdc.token_type+" "
					+_sfdc.access_token);
		
			//Log.i("-----Checki in Body ", body);
			_httppost.setEntity(new StringEntity(body.toString()));

			HttpResponse _httpresponse = _httpclient.execute(_httppost);
			int _responsecode = _httpresponse.getStatusLine().getStatusCode();
			//Log.i("HTTP RESPONSE CODE",":"+_responsecode);

			response = EntityUtils.toString(_httpresponse.getEntity());
			//Log.i("--------------Post Method Response -----------",":"+response);


		} catch (Exception e) {
			e.printStackTrace();
			response = e.getLocalizedMessage();
		}
		return response;

	}

	@SuppressWarnings("unused")
	public JSONArray makeCheckin(Cursor cursor,int position) {
		JSONArray ticketarray = null;
		Cursor att_cursor=null;

		if(!_is_order_id)
		{
			if(att_cursor!=null)
			{
				att_cursor.close();
				// att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ _event.Events.Id+"'" + " AND "+DBFeilds.ATTENDEE_BUYER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_GNUSER_ID))+ "'");
				att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.USER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "'",ITransaction.EMPTY_STRING);
			}else{

				// att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ _event.Events.Id+"'" + " AND "+DBFeilds.ATTENDEE_BUYER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_GNUSER_ID))+ "'");
				att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.USER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "'",ITransaction.EMPTY_STRING);
			}
		}else{
			if(att_cursor!=null)
			{
				att_cursor.close();
				// att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ _event.Events.Id+"'" + " AND "+DBFeilds.ATTENDEE_BUYER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_GNUSER_ID))+ "'");
				att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "'",ITransaction.EMPTY_STRING);
			}else{

				// att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ _event.Events.Id+"'" + " AND "+DBFeilds.ATTENDEE_BUYER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.USER_GNUSER_ID))+ "'");
				att_cursor= Util.db.getAttendeeDataCursor(" where Event_Id='"+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID))+"'" + " AND "+DBFeilds.ATTENDEE_ORDER_ID+" = '"+cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID))+ "'",ITransaction.EMPTY_STRING);
			}
		}

		try {


			ticketarray= new JSONArray();
			
			//===========for closing progress bars depend on the attende id on parsejsonresponse checkin tag==============//
			
			if(front_checkin_bar_array.get(position)!=null)
			{
			att_cursor.moveToFirst();
			front_checkin_bar_array.get(position).setTag(att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)));	
			}
			for(int i=0;i<att_cursor.getCount();i++)
			{
				JSONObject obj = new JSONObject();
				att_cursor.moveToPosition(i);
				try {

					if (checkin_type.equalsIgnoreCase("Check In")) {
						if (att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN)).equals("false")) {
							obj.put("TicketId", att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)));
							obj.put("device", "Android");
							obj.put("isCHeckIn", true);
						}
						ticketarray.put(obj);
					} else {
						if (att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN)).equals("true")) {
							obj.put("TicketId", att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)));
							obj.put("device", "Android");
							obj.put("isCHeckIn", false);
						}

						ticketarray.put(obj);
					}

					//main code


					obj.put("TicketId",att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ID)));
					if (att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN)).equals("true"))
						obj.put("isCHeckIn", false);
					else
						obj.put("isCHeckIn", true);
					ticketarray.put(obj);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
		return ticketarray;
	}

	public String setTicketCheckinUrl() {

		return _sfdc.instance_url+ WebServiceUrls.SA_TICKETS_SCAN_URL + "scannedby="+ _sfdc.user_id;
	}

	public void parseJsonResponse(String response)
	{

		JSONObject obj;
		try{
			obj = new JSONObject(response);
			////Log.i("Attendee List Activity", "requestType=  " + requestType);
			if (obj.optString("ErrorMsg").equalsIgnoreCase("null")) {
				//Log.i("Attendee List Activity", "error is null");
				JSONArray success = obj.optJSONArray("SuccessTickets");
				JSONArray failure = obj.optJSONArray("FailureTickets");
				String dialogtime = "";
				if (success.length()>0) {
					for (int i = 0; i < success.length(); i++) {
						//Log.i("Attendee List Activity","success is not null");
						boolean status = success.optJSONObject(i).optBoolean("Status");
						String time = success.optJSONObject(i).optString("TimeStamp");
						String attendee_Id = success.optJSONObject(i).optString("STicketId");
						dialogtime = Util.db_date_format1.format(Util.db_server_ticket_format.parse(time));
						time = Util.new_db_date_format.format(Util.db_server_ticket_format.parse(time));
						for(int j=0;j<front_checkin_bar_array.size();j++)
						{
							if(front_checkin_bar_array.get(j).getTag().equals(attendee_Id))
							{
								front_checkin_bar_array.get(j).setVisibility(View.GONE);
							}
						}
						Util.db.updateCheckedInStatus(attendee_Id,checked_in_event_id, time, status);
					}
				} else {
					for (int i = 0; i < failure.length(); i++) {
						boolean status = failure.optJSONObject(i).optBoolean("Status");
						String time = failure.optJSONObject(i).optString("TimeStamp");
						String attendee_Id = failure.optJSONObject(i).optString("STicketId");
						dialogtime = Util.db_date_format1.format(Util.db_server_ticket_format.parse(time));
						time = Util.new_db_date_format.format(Util.db_server_ticket_format.parse(time));
						Util.db.updateCheckedInStatus(attendee_Id,checked_in_event_id, time, status);
						for(int j=0;j<front_checkin_bar_array.size();j++)
						{
							if(front_checkin_bar_array.get(j).getTag().equals(attendee_Id))
							{
								front_checkin_bar_array.get(j).setVisibility(View.GONE);
							}
						}
						
						Message m=new Message();
						Bundle b=new Bundle();
						b.putString(Util.ATTENDEE_ID, attendee_Id);
						m.setData(b);
						AlertDialogCustom dialog =new AlertDialogCustom(_activity);
						dialog.setParamenters("Status","<Font color:#A00404>"+""+"</Font>"+"Already Checkid In", null, null, 1, false);
						dialog.show();
						//messageHAndler.sendMessage(m);
					}
				}
				notifyDataSetChanged();
			}
			notifyDataSetChanged();
		}catch(Exception e){
			for (int j = 0; j < front_checkin_bar_array.size(); j++) {

				front_checkin_bar_array.get(j).setVisibility(View.GONE);

			}
			e.printStackTrace();
		}


	}
	
	public ViewList getViewArray(int position){
		return viewArray.get(position);
	}

	public void onClickCheckin(Cursor cursor,int _barCount,ArrayList<ProgressBar> frontProgressBars,String checkinType){	
		checkin_type=checkinType;
		front_checkin_bar_array=frontProgressBars;
		new DoTicketCheckInFromBase(makeCheckin(cursor,_barCount).toString(),_barCount).execute();
		
	}*/
	
	/*public int getCheckInStatus(Cursor cursor, int position) {
		Cursor att_cursor = null;
		int count = 1;
		cursor.moveToPosition(position);
		if (!_is_order_id) {
			att_cursor = Util.db.getAttendeeDataCursor(
					" where Event_Id='" + checked_in_event_id + "'" + " AND " + DBFeilds.ATTENDEE_BUYER_ID + " = '"
							+ cursor.getString(cursor.getColumnIndex(DBFeilds.USER_GNUSER_ID)) + "'");
		} else {
			att_cursor = Util.db.getAttendeeDataCursor(
					" where Event_Id='" + cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_EVENT_ID)) + "'"
							+ " AND " + DBFeilds.ATTENDEE_ORDER_ID + " = '"
							+ cursor.getString(cursor.getColumnIndex(DBFeilds.ORDER_ORDER_ID)) + "'");
		}

		if (att_cursor == null) {
			return count;
		} else if (att_cursor.getCount() == 0) {
			att_cursor.close();
			return count;
		} else {
			count = 0;
			for (int i = 0; i < att_cursor.getCount(); i++) {
				att_cursor.moveToPosition(i);
				if (att_cursor.getString(att_cursor.getColumnIndex(DBFeilds.ATTENDEE_ISCHECKIN)).equalsIgnoreCase("true")) {
					count++;
				}
			}
			int cursor_size = att_cursor.getCount();
			att_cursor.close();
			if (count == cursor_size) {
				return 0;
			} else {
				return 1;
			}
		}

		
	}*/
}

