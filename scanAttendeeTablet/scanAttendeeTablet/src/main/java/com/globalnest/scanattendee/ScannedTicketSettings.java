//  ScanAttendee Android
//  Created by Ajay on Feb 18, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.ScannedItems;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.ITransaction;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.paypal.android.b.g;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @author laxmanamurthy
 *
 */
public class ScannedTicketSettings extends BaseActivity{

	ExpandableListView list_view;
	List<SessionGroup> group_list = new ArrayList<SessionGroup>();
	ScannedGroupExpanAdapter adapter;
	ToggleButton toggel_show_attendees;
	TextView txt_no_sessions;
	private String request_type =ITransaction.EMPTY_STRING;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.scanned_tickets_layout);
		group_list = Util.db.getGroupList(checked_in_eventId);
		adapter = new ScannedGroupExpanAdapter();
		list_view.setAdapter(adapter);
		for(int i=0;i<group_list.size();i++){
			list_view.expandGroup(i, false);
		}
		if(group_list.size()==0){
			txt_no_sessions.setVisibility(View.VISIBLE);
		}else{
			txt_no_sessions.setVisibility(View.GONE);
		}
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		toggel_show_attendees.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(Util.db.getSwitchedOnScanItem(checked_in_eventId).isEmpty()){
					toggel_show_attendees.setChecked(false);
					showScannedTicketsAlert("Please TurnON at least one scanattendee ticket to scan tickets.",false);
				}else{
					Util.selected_session_attedee_pref.edit().putBoolean(Util.SELECTED_SESSION_ATTENDEE_KEY, isChecked).commit();
				}

			}
		});

		img_addticket.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				if (!isOnline()) {
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				} else if(Util.isMyServiceRunning(DownloadService.class, ScannedTicketSettings.this)){
					showServiceRunningAlert(checkedin_event_record.Events.Name);
				}else{
					request_type = WebServiceUrls.SA_GET_TICKET_LIST;
					//request_type = WebServiceUrls.SA_SCANNEDITEMS_REFRESH;
					doRequest();
				}

			}
		});
		list_view.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
				// TODO Auto-generated method stub
				//Log.i("-----------------expamd group---------------index",":"+groupPosition+" : "+parent.isGroupExpanded(groupPosition));
				if(parent.isGroupExpanded(groupPosition)){
					parent.collapseGroup(groupPosition);
				}else{
					parent.expandGroup(groupPosition);
				}
				return true;
			}
		});


	}
	@Override
	protected void onResume() {
		super.onResume();

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		String access_token = sfdcddetails.token_type + " " + sfdcddetails.access_token;
		if(request_type.equalsIgnoreCase(WebServiceUrls.SA_GET_TICKET_LIST)){
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_TICKET_LIST + "Event_id=" + checked_in_eventId;
			postMethod = new HttpPostData("Loading Scanned Tickets...", url, null, access_token, ScannedTicketSettings.this);
		}else if(request_type.equalsIgnoreCase(WebServiceUrls.SA_SCANNEDITEMS_REFRESH)){
			String url = sfdcddetails.instance_url + WebServiceUrls.SA_SCANNEDITEMS_REFRESH+getValues();
			postMethod = new HttpPostData("Loading Scanned Tickets...", url, null, access_token, ScannedTicketSettings.this);
		}

		postMethod.execute();
	}

	private String getValues(){
		String scan_item_ids = ITransaction.EMPTY_STRING;
		int i = 0;
		List<ScannedItems> scanned_item_list = Util.db.getScannedItems(checked_in_eventId);
		for(ScannedItems scanned_item: scanned_item_list){
			scan_item_ids = scan_item_ids+scanned_item.Id;
			if(i != (scanned_item_list.size() - 1)){
				scan_item_ids = scan_item_ids+",";
			}
			i++;
		}

		List<NameValuePair> value = new ArrayList<NameValuePair>();
		value.add(new BasicNameValuePair("Event_Id", checked_in_eventId));
		value.add(new BasicNameValuePair("Scanitem_Id", scan_item_ids));
		value.add(new BasicNameValuePair("user_Id", sfdcddetails.user_id));
		return AppUtils.getQuery(value);
	}

	public String getGroupJson(){
		JSONObject obj = new JSONObject();
		try {
			List<SessionGroup> group_list = Util.db.getGroupList(checked_in_eventId);


		} catch (Exception e) {
			// TODO: handle exception
		}
		return obj.toString();
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			Gson gson = new Gson();
			/*Util.db.deleteTable(DBFeilds.TABLE_ADDED_TICKETS);
			Util.db.deleteTable(DBFeilds.TABLE_ITEM_POOL);
			Util.db.deleteTable(DBFeilds.TABLE_ITEM_REG_SETTINGS);*/
			if(request_type.equalsIgnoreCase(WebServiceUrls.SA_GET_TICKET_LIST)){
				ItemsListResponse item_response = gson.fromJson(response, ItemsListResponse.class);
				Util.db.upadteItemListRecordInDB(item_response.Itemscls_infoList, checked_in_eventId);
				Util.db.InsertAndUpdateSEMINAR_AGENDA(item_response.agndaInfo);

				Util.db.deleteEventScannedTicketsGroup(checked_in_eventId);
				Util.db.deleteEventScannedTickets(checked_in_eventId);

				if(!group_list.isEmpty()){
					for(SessionGroup group : group_list){
						for(SessionGroup server_group : item_response.userSessions){
							if(group.Id.equalsIgnoreCase(server_group.Id)){
								if(server_group.BLN_Session_users__r.records.size() > 0){
									server_group.BLN_Session_users__r.records.get(0).DefaultValue__c = group.Scan_Switch;
								}

							}
						}
					}
				}
				Util.db.InsertAndUpdateSESSION_GROUP(item_response.userSessions);
				group_list = Util.db.getGroupList(checked_in_eventId);
				adapter = new ScannedGroupExpanAdapter();
				list_view.setAdapter(adapter);
				for(int i=0;i<group_list.size();i++){
					list_view.expandGroup(i, true);
				}

			}else{
				//Log.i("----------------------Scanned Item REfresh---------",":"+response);
				Type listType = new TypeToken<ArrayList<ScannedItemsRefreshRes>>(){}.getType();
				List<ScannedItemsRefreshRes> scanned_items_ref = new Gson().fromJson(response, listType);
				List<ScannedItems> items_new_added = new ArrayList<ScannedItems>();
				for(int i=0;i<scanned_items_ref.size();i++){
					ScannedItemsRefreshRes scanitem_ref = scanned_items_ref.get(i);
					if(scanitem_ref.scanItem.Status__c.equalsIgnoreCase("Cancel")){
						Util.db.deleteEventScannedTickets(scanitem_ref.scanItem.BLN_Event__c, scanitem_ref.scanItem.Id);
						//scanned_items_ref.remove(i);
					}else if(scanitem_ref.isNew){
						items_new_added.add(scanitem_ref.scanItem);
					}
				}
				Util.db.InsertAndUpdateScannedItems(items_new_added,checked_in_eventId);
				scanned_items_ref.clear();
				items_new_added.clear();
				//scanned_item_list.clear();
				//scanned_item_list = Util.db.getScannedItems(checked_in_eventId);
				adapter = new ScannedGroupExpanAdapter();
				list_view.setAdapter(adapter);
			}


		} catch (Exception e) {
			// TODO: handle exception
			startErrorAnimation(getResources().getString(R.string.connection_error),
					txt_error_msg);
		}

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
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Scan Ticket Settings");
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		img_addticket.setImageResource(R.drawable.dashboardrefresh);
		img_addticket.setVisibility(View.VISIBLE);
		list_view = (ExpandableListView)linearview.findViewById(R.id.list_scanned_items);
		txt_no_sessions =(TextView)linearview.findViewById(R.id.txt_no_sessions);
		toggel_show_attendees = (ToggleButton)linearview.findViewById(R.id.toggle_selected_attendees_checkin);
		toggel_show_attendees.setChecked(Util.selected_session_attedee_pref.getBoolean(Util.SELECTED_SESSION_ATTENDEE_KEY, false));
	}




	public class ScannedItemsRefreshRes implements Serializable{

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		public ScannedItems scanItem = new ScannedItems();
		public boolean isNew=false;

	}

	public class ScannedGroupExpanAdapter extends BaseExpandableListAdapter{

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			return group_list.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			return group_list.get(groupPosition).BLN_Session_Items__r.records.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		@Override
		public SessionGroup getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return group_list.get(groupPosition);
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getChild(int, int)
		 */
		@Override
		public ScannedItems getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return group_list.get(groupPosition).BLN_Session_Items__r.records.get(childPosition);
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
		 */
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = inflater.inflate(R.layout.scanned_item_list_layout, null);
			TextView txt_item_poolname = (TextView)v.findViewById(R.id.item_pool_name);
			TextView txt_on_off = (TextView)v.findViewById(R.id.txt_on_off);
			ToggleButton toggle = (ToggleButton) v.findViewById(R.id.toggle_checkin);

			//String item_pool_name = Util.db.getItem_Pool_Name(getItem(position).BLN_Item_Pool__c, getItem(position).BLN_Event__c);
			txt_item_poolname.setText(getGroup(groupPosition).Name);
			
			/*String parent_id = Util.db.getItemPoolParentId(getItem(position).BLN_Item_Pool__c, getItem(position).BLN_Event__c);
			if(!NullChecker(parent_id).isEmpty()){
				String package_name = Util.db.getItem_Pool_Name(parent_id, getItem(position).BLN_Event__c);
				txt_item_poolname.setText(item_pool_name+" ( "+package_name+" )");
			}*/

			toggle.setChecked(getGroup(groupPosition).Scan_Switch);
			if(getGroup(groupPosition).Scan_Switch){
				txt_on_off.setText("Turned ON");
				txt_on_off.setTextColor(getResources().getColor(R.color.com_facebook_blue));
				for(SessionGroup group : group_list){
					if(!group.Id.equalsIgnoreCase(getGroup(groupPosition).Id)){
						group.Scan_Switch = false;
					}
				}
				adapter.notifyDataSetChanged();
			}else{
				txt_on_off.setText("Turned OFF");
				txt_on_off.setTextColor(getResources().getColor(R.color.gray_color));
			}
			toggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
					// TODO Auto-generated method stub

					List<SessionGroup> group_list_1 = Util.db.getGroupList(checked_in_eventId);
					for(SessionGroup group : group_list_1){
						//group = Util.db.getSwitchedONGroup(checked_in_eventId);
						group.Scan_Switch = false;
						/*List<ScannedItems> group_items = group.BLN_Session_Items__r.records;
						group.BLN_Session_Items__r.records = group_items;*/
						Util.db.UpdateSESSION_GROUP(group);
					}

					AppUtils.displayLog("----------Group Posistion, Switched On Group Id, Check box value------------",":"+ groupPosition+ " : "+Util.db.getSwitchedONGroupId(checked_in_eventId)+" : "+isChecked);

					SessionGroup group = getGroup(groupPosition);
					group.Scan_Switch = isChecked;
					/*group_items = Util.db.getScannedItemsGroup(checked_in_eventId, group.Id);
					group.BLN_Session_Items__r.records = group_items;*/
					Util.db.UpdateSESSION_GROUP(group);

					AppUtils.displayLog("----------Switched On Group Id------------",":"+ groupPosition+ " : "+Util.db.getSwitchedONGroupId(checked_in_eventId)+" : "+isChecked);
					group_list = Util.db.getGroupList(checked_in_eventId);
					adapter.notifyDataSetChanged();
				}
			});
			return v;
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
								 ViewGroup parent) {
			// TODO Auto-generated method stub
			View v = inflater.inflate(R.layout.scanned_item_list_child_layout, null);
			TextView txt_item_poolname = (TextView)v.findViewById(R.id.item_pool_name);
			TextView txt_on_off = (TextView)v.findViewById(R.id.txt_on_off);

			ToggleButton toggle = (ToggleButton) v.findViewById(R.id.toggle_checkin);
			toggle.setVisibility(View.GONE);
			String item_pool_name = Util.db.getItem_Pool_Name(getChild(groupPosition, childPosition).BLN_Item_Pool__c, getChild(groupPosition, childPosition).BLN_Event__c);
			txt_item_poolname.setText(item_pool_name);

			String parent_id = Util.db.getItemPoolParentId(getChild(groupPosition, childPosition).BLN_Item_Pool__c, getChild(groupPosition, childPosition).BLN_Event__c);
			if(!NullChecker(parent_id).isEmpty()){
				String package_name = Util.db.getItem_Pool_Name(parent_id, getChild(groupPosition, childPosition).BLN_Event__c);
				txt_item_poolname.setText(item_pool_name+" ( "+package_name+" )");
			}

			if(getGroup(groupPosition).Scan_Switch){
				txt_on_off.setText("Turned ON");
				txt_on_off.setTextColor(getResources().getColor(R.color.com_facebook_blue));
			}else{
				txt_on_off.setText("Turned OFF");
				txt_on_off.setTextColor(getResources().getColor(R.color.gray_color));
			}

			return v;
		}

		/* (non-Javadoc)
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return true;
		}

	}

}
