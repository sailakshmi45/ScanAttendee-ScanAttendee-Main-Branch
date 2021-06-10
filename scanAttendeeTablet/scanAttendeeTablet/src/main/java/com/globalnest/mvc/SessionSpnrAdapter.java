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

import com.globalnest.scanattendee.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @author laxmanamurth
 *
 */
public class SessionSpnrAdapter extends BaseAdapter{
	
	private List<SessionGroup> group_list = new ArrayList<>();
	private Context context;
	
	public SessionSpnrAdapter(Context context, List<SessionGroup> group_list) {
		// TODO Auto-generated constructor stub
		this.group_list = group_list;
		this.context = context;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return group_list.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public SessionGroup getItem(int position) {
		// TODO Auto-generated method stub
		return group_list.get(position);
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
		checkbox.setVisibility(View.GONE);
		TextView text_view = (TextView) v.findViewById(R.id.spinner_text);
		text_view.setText(getItem(position).Name);
		return v;
	}

}
