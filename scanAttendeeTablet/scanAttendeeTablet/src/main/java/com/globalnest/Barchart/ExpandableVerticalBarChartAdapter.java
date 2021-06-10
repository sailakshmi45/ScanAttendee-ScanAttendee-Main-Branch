//  ScanAttendee Android
//  Created by Ajay on Jul 11, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.Barchart;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.globalnest.scanattendee.R;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * @author mayank
 *
 */
public class ExpandableVerticalBarChartAdapter extends BaseExpandableListAdapter{

	
	 private Activity mActivity;
	    private ArrayList<BarAttr> mGroupAttr;
	    private HashMap<Integer,ArrayList<BarAttr>> mChildAttr;
	    private int lastPosition;
	    private ExpandableListView listview;

	public ExpandableVerticalBarChartAdapter(Activity context,ArrayList<BarAttr> barGroupAttr,HashMap<Integer,ArrayList<BarAttr>> barChildAttr,ExpandableListView listview) { 
		mActivity=context;
		mGroupAttr = barGroupAttr;
		mChildAttr=barChildAttr;
		this.listview = listview;
 }


	@Override
	public BarAttr getChild( int groupPosition, int childPosititon ) {
		return mChildAttr.get(groupPosition).get(childPosititon); 
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent ) {

		LayoutInflater inflater=mActivity.getLayoutInflater();
		convertView=inflater.inflate(R.layout.progress_child_ticitem, null); 
        DecimalFormat df = new DecimalFormat("#.##");
         TextView title    =   (TextView)convertView.findViewById(R.id.title);
         ProgressBar tvcol1 =   (ProgressBar)convertView.findViewById(R.id.progress_color);
         //  RelativeLayout bar_layout=(RelativeLayout) row.findViewById(R.id.bar_layout);
           //TextView tvcol2= (TextView)row.findViewById(R.id.colortext02);
           TextView gt     =   (TextView)convertView.findViewById(R.id.txt_barPercent);
           TextView txt_ticketCount=(TextView)convertView.findViewById(R.id.txt_ticketCount);
           tvcol1.setProgressDrawable(mActivity.getResources().getDrawable(R.drawable.blue_horizontal_progress));
          
           if(!getChild(groupPosition, childPosition).barBottomLabels.isEmpty()){
        	   title.setText(mChildAttr.get(groupPosition).get(childPosition).barBottomLabels);  
        	   gt.setText(String.valueOf(mChildAttr.get(groupPosition).get(childPosition).frontBarPercentage)+"%");
        	   //tvcol1.setProgress((int)mChildAttr.get(groupPosition).get(childPosition).frontBarPercentage);
        	   if((int)mChildAttr.get(groupPosition).get(childPosition).frontBarPercentage==0){

       			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, 5);
       			anim.setDuration(1000);
       			tvcol1.startAnimation(anim);
        	   }else{
       			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, (int)mChildAttr.get(groupPosition).get(childPosition).frontBarPercentage);
       			anim.setDuration(1000);
       			tvcol1.startAnimation(anim);
       		
        	   }
        	   //Log.i("---------------Child top bar----------------",":"+mChildAttr.get(groupPosition).get(childPosition).barBottomLabels+" : "+mChildAttr.get(groupPosition).get(childPosition).barTopLabels);
        	   txt_ticketCount.setText(mChildAttr.get(groupPosition).get(childPosition).barTopLabels);
        	   Animation animation = AnimationUtils.loadAnimation(mActivity, (groupPosition > lastPosition) ? R.anim.scale : R.anim.scale);
        	   // convertView.startAnimation(animation);
        	   lastPosition = groupPosition;
           }
		
		return convertView;
	}

	@Override
	public int getChildrenCount( int groupPosition ) {
		//Log.i("---------------Child Array Count----------------",":"+mChildAttr.get(groupPosition)+ groupPosition);
		if(mChildAttr.get(groupPosition) == null){
			return 0;
		}
		return mChildAttr.get(groupPosition).size();
	}

	@Override
	public BarAttr getGroup( int groupPosition ) {
		return mGroupAttr.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupAttr.size();
	}

	@Override
	public long getGroupId( int groupPosition ) {
		return getGroup( groupPosition ).hashCode();
	}

	@Override
	public long getChildId( int groupPosition, int childPosition ) {
		return ( getGroup( groupPosition ) + "_" + getChild( groupPosition, childPosition ) ).hashCode();
	}

	@Override
	public View getGroupView(
			final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent ) {
		View row=null;
		LayoutInflater inflater=mActivity.getLayoutInflater();
		row=inflater.inflate(R.layout.progress_group_ticitem, null);
		TextView title    =   (TextView)row.findViewById(R.id.title);
		ProgressBar tvcol1 =   (ProgressBar)row.findViewById(R.id.progress_color);
		TextView txt_ticketCount=(TextView)row.findViewById(R.id.txt_ticketCount);
		TextView gt     =   (TextView)row.findViewById(R.id.txt_barPercent);
		ImageView img_ticketcount=(ImageView)row.findViewById(R.id.img_ticketcount);
		final ImageView img_expand = (ImageView)row.findViewById(R.id.img_expand);
		LinearLayout layout_expand = (LinearLayout)row.findViewById(R.id.layout_expand);
		title.setText(mGroupAttr.get(groupPosition).barBottomLabels);  
		gt.setText(String.valueOf(mGroupAttr.get(groupPosition).frontBarPercentage)+"%");
		if((int)mGroupAttr.get(groupPosition).frontBarPercentage==0){
			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, 5);
			anim.setDuration(1000);
			tvcol1.startAnimation(anim);
		}else{
			//tvcol1.setProgress((int)mGroupAttr.get(groupPosition).frontBarPercentage);
			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, (int)mGroupAttr.get(groupPosition).frontBarPercentage);
			anim.setDuration(1000);
			tvcol1.startAnimation(anim);
		}
		tvcol1.setProgressDrawable(mActivity.getResources().getDrawable(R.drawable.green_horizontal_progress));
		txt_ticketCount.setBackground(mActivity.getResources().getDrawable(R.drawable.green_border_circle));
		
		//Log.i("---------------group top bar----------------",":"+mGroupAttr.get(groupPosition).barBottomLabels+" : "+mGroupAttr.get(groupPosition).barTopLabels);
		txt_ticketCount.setText(mGroupAttr.get(groupPosition).barTopLabels);
		img_ticketcount.setVisibility(View.GONE);
		txt_ticketCount.setVisibility(View.VISIBLE);
		
		/*if(mGroupAttr.get(groupPosition).isPackage){
			img_ticketcount.setVisibility(View.VISIBLE);
			txt_ticketCount.setVisibility(View.GONE);
		}else{
			txt_ticketCount.setText(mGroupAttr.get(groupPosition).barTopLabels);
			img_ticketcount.setVisibility(View.GONE);
			txt_ticketCount.setVisibility(View.VISIBLE);
		}*/
		if(isExpanded){
			img_expand.setImageResource(R.drawable.minus_green);
		}else{
			img_expand.setImageResource(R.drawable.plus_green);
		}
		
		
		lastPosition = groupPosition;
		layout_expand.setTag((Integer)groupPosition);
		layout_expand.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int position = (Integer)v.getTag();
				if(listview.isGroupExpanded(position)){
					listview.collapseGroup(position);
				}else{
					listview.expandGroup(position);
				}
			}
		});
		
		
		return row;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getCombinedChildId( final long groupId, final long childId ) {
		return super.getCombinedChildId( groupId, childId );
	}

	@Override
	public long getCombinedGroupId( final long groupId ) {
		return super.getCombinedGroupId( groupId );
	}

	@Override
	public boolean isChildSelectable( int groupPosition, int childPosition ) {
		return true;
	}

	public BarAttr getGroupAttr(int position){
		return mGroupAttr.get(position);
	}
	
	public BarAttr getChildAttr(int groupPosition,int childPosition){
		return mChildAttr.get(groupPosition).get(childPosition);
	}
	
	public List<BarAttr> getChildAttrList(int groupPosition){
		return mChildAttr.get(groupPosition);
	}
}
