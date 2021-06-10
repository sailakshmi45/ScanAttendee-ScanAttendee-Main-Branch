//  ScanAttendee Android
//  Created by Ajay on Oct 21, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.util.ArrayList;
import java.util.List;

import com.globalnest.objects.ScannedItems;
import com.globalnest.scanattendee.BaseActivity;
import com.globalnest.scanattendee.R;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @author laxmanamurthy
 *
 */
public class SessionItemSpnrAdapter extends BaseAdapter{
	
	private Context context;
	private List<ScannedItems> scanned_items_list = new ArrayList<>();
	
	/**
	 * 
	 */
	public SessionItemSpnrAdapter(Context context, List<ScannedItems> scanned_items_list) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.scanned_items_list = scanned_items_list;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return scanned_items_list.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public ScannedItems getItem(int position) {
		// TODO Auto-generated method stub
		return scanned_items_list.get(position);
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = LayoutInflater.from(context).inflate(R.layout.spinner_item_with_checkbox,null);
		CheckBox checkbox = (CheckBox)v.findViewById(R.id.checkBox);
		checkbox.setEnabled(false);
		TextView text_view = (TextView) v.findViewById(R.id.spinner_text);
		String item_pool_name = Util.db.getItem_Pool_Name(getItem(position).BLN_Item_Pool__c,BaseActivity.checkedin_event_record.Events.Id);
		text_view.setText(item_pool_name);
		AppUtils.displayLog("---------------Item Name is checked-------",":"+item_pool_name+" : "+getItem(position).DefaultValue__c);
		checkbox.setChecked(getItem(position).DefaultValue__c);
		String parent_id = Util.db.getItemPoolParentId(getItem(position).BLN_Item_Pool__c,BaseActivity.checkedin_event_record.Events.Id);
		if(!Util.NullChecker(parent_id).isEmpty()){
			String package_name = Util.db.getItem_Pool_Name(parent_id, BaseActivity.checkedin_event_record.Events.Id);
			text_view.setText(item_pool_name+" ( "+package_name+" )");
		}
		//text_view.setText(getItem(position).BLN_Item_Pool__r.Item_Pool_Name__c);
		return v;
	}

}
