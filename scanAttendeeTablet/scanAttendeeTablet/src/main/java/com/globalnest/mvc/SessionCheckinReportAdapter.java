//  ScanAttendee Android
//  Created by Ajay on Nov 4, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.globalnest.Barchart.BarAttr;
import com.globalnest.Barchart.ProgressBarAnimation;
import com.globalnest.scanattendee.ExpandablePanel;
import com.globalnest.scanattendee.ExpandablePanel.OnExpandListener;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.globalnest.scanattendee.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author laxmanamurthy
 *
 */
public class SessionCheckinReportAdapter extends BaseAdapter{
	private Context context;
	private List<BarAttr> groupAttr =  new ArrayList<>();
	private HashMap<Integer,ArrayList<BarAttr>> mChildAttr = new HashMap<>();
	public SessionCheckinReportAdapter(Context context, List<BarAttr> goupAttr,HashMap<Integer,ArrayList<BarAttr>> mChildAttr) {
		// TODO Auto-generated constructor stub
		this.groupAttr = goupAttr;
		this.context = context;
		this.mChildAttr = mChildAttr;
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return groupAttr.size();
	}

	/* (non-Javadoc)
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public BarAttr getItem(int position) {
		// TODO Auto-generated method stub
		return groupAttr.get(position);
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
		View v = LayoutInflater.from(context).inflate(R.layout.dashboard_checkin_out_layout, null);
		
		ExpandablePanel panel = (ExpandablePanel)v.findViewById(R.id.expand_group);
		TextView txt_group = (TextView)v.findViewById(R.id.title);
		final ImageView img_expand = (ImageView)v.findViewById(R.id.img_expand);
		ListView list_items = (ListView)v.findViewById(R.id.list_session_item);
		TextView txt_ticketCount=(TextView)v.findViewById(R.id.txt_ticketCount);
		FrameLayout handle = (FrameLayout)v.findViewById(R.id.layout_checkedin_out);
		txt_group.setText(getItem(position).barBottomLabels);
		txt_ticketCount.setText(getItem(position).barTopLabels);
		ItemsAdapter adapter = new ItemsAdapter(context, mChildAttr.get(position));
		list_items.setAdapter(adapter);
		Util.setListViewHeightBasedOnChildren(list_items);
		panel.setOnExpandListener(new OnExpandListener() {
			
			@Override
			public void onExpand(View handle, View content) {
				// TODO Auto-generated method stub
				img_expand.setImageResource(R.drawable.minus_green);
			}
			
			@Override
			public void onCollapse(View handle, View content) {
				// TODO Auto-generated method stub
				img_expand.setImageResource(R.drawable.plus_green);
			}
		});
		
		return v;
	}
	
	
	public class ItemsAdapter extends BaseAdapter{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		private Context context;
		private List<BarAttr> childAttr = new ArrayList<>();
		public ItemsAdapter(Context context, List<BarAttr> childAttr) {
			// TODO Auto-generated constructor stub
			this.childAttr = childAttr;
			this.context = context;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(childAttr == null){
				return 0;
			}
			return childAttr.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public BarAttr getItem(int position) {
			// TODO Auto-generated method stub
			return childAttr.get(position);
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view =LayoutInflater.from(context).inflate(R.layout.progress_child_ticitem, null); 
	        DecimalFormat df = new DecimalFormat("#.##");
	         TextView title    =   (TextView)view.findViewById(R.id.title);
	         ProgressBar tvcol1 =   (ProgressBar)view.findViewById(R.id.progress_color);
	         //  RelativeLayout bar_layout=(RelativeLayout) row.findViewById(R.id.bar_layout);
	           //TextView tvcol2= (TextView)row.findViewById(R.id.colortext02);
	           TextView gt     =   (TextView)view.findViewById(R.id.txt_barPercent);
	           TextView txt_ticketCount=(TextView)view.findViewById(R.id.txt_ticketCount);
	           tvcol1.setProgressDrawable(context.getResources().getDrawable(R.drawable.blue_horizontal_progress));
	          
	           if(!getItem(position).barBottomLabels.isEmpty()){
	        	   title.setText(getItem(position).barBottomLabels);  
	        	   gt.setText(String.valueOf(getItem(position).frontBarPercentage)+"%");
	        	   //tvcol1.setProgress((int)mChildAttr.get(groupPosition).get(childPosition).frontBarPercentage);
	        	   if((int)getItem(position).frontBarPercentage==0){

	       			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, 5);
	       			anim.setDuration(1000);
	       			tvcol1.startAnimation(anim);
	        	   }else{
	       			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, (int)getItem(position).frontBarPercentage);
	       			anim.setDuration(1000);
	       			tvcol1.startAnimation(anim);
	       		
	        	   }
				   AppUtils.displayLog("---------------Child top bar----------------",":"+getItem(position).barBottomLabels+" : "+getItem(position).barTopLabels);
	        	   txt_ticketCount.setText(getItem(position).barTopLabels);
	        	   //Animation animation = AnimationUtils.loadAnimation(context, (position > position) ? R.anim.scale : R.anim.scale);
	        	   // convertView.startAnimation(animation);
	        	   //lastPosition = groupPosition;
	           }
			
			return view;
		}
		
	}

}
