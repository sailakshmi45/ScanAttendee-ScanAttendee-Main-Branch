//  ScanAttendee Android
//  Created by Ajay on Jul 9, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.Barchart;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.globalnest.scanattendee.R;

/**
 * @author mayank
 *
 */
public class BarChartAdapter extends BaseAdapter{
    private Activity mActivity;
    private ArrayList<BarAttr> mAttr;
    private int lastPosition;
    
    public BarChartAdapter(Activity context,ArrayList<BarAttr> barAttr)
    {  
        mActivity=context;
        mAttr = barAttr;
       
    }

    public int getCount()
    {
        // TODO Auto-generated method stub
        return mAttr.size();
    }

    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return mAttr.get(position).barBottomLabels;
    }

    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return position;
    }
    
    public View getView(final int position, View convertView, ViewGroup parent){
    	View row=null;
    	LayoutInflater inflater=mActivity.getLayoutInflater();
    	row=inflater.inflate(R.layout.progress_group_ticket, null); 
    	DecimalFormat df = new DecimalFormat("#.##");
    	TextView title    =   (TextView)row.findViewById(R.id.title);
    	ProgressBar tvcol1 =   (ProgressBar)row.findViewById(R.id.progress_color);
    	TextView gt     =   (TextView)row.findViewById(R.id.txt_barPercent);
    	TextView txt_ticketCount=(TextView)row.findViewById(R.id.txt_ticketCount);
    	if((int)mAttr.get(position).frontBarPercentage>60){
			gt.setTextColor(mActivity.getResources().getColor(R.color.light_gray));
		}else{
			gt.setTextColor(mActivity.getResources().getColor(R.color.black_faded));
		}
    	title.setText(mAttr.get(position).barBottomLabels);
    	gt.setText(String.valueOf(mAttr.get(position).frontBarPercentage)+"%");
    	if((int)mAttr.get(position).frontBarPercentage==0){
    		ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, 5);
			anim.setDuration(1000);
			tvcol1.startAnimation(anim);
		}else{
			ProgressBarAnimation anim = new ProgressBarAnimation(tvcol1, 0, (int)mAttr.get(position).frontBarPercentage);
			anim.setDuration(1000);
			tvcol1.startAnimation(anim);
		}
    	
    	txt_ticketCount.setText(mAttr.get(position).barTopLabels);
    	// Animation animation = AnimationUtils.loadAnimation(mActivity, (position > lastPosition) ? R.anim.scale : R.anim.scale);
         //row.startAnimation(animation);
    	lastPosition = position;
    	return row;
    }
}
