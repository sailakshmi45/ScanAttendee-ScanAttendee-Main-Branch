//  ScanAttendee Android
//  Created by Ajay on Jul 9, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.scanattendee;

import com.globalnest.Barchart.ColorTemplates;
import com.globalnest.mvc.DashboradHandler;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.Util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author mayank
 *
 */
public class PaymentAdapter extends BaseAdapter{

	Activity mActivity;
	DashboradHandler dashboard_data;
	private EventObjects checkedin_event_record;
	
	public PaymentAdapter(Activity activity,DashboradHandler handler,EventObjects checkedinEvent) {
		mActivity =activity;
		dashboard_data=handler;
		checkedin_event_record=checkedinEvent;
	}
	
	@Override
	public int getCount() {
		
		return dashboard_data.dashboardTicketsPaymentList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return dashboard_data.dashboardTicketsPaymentList.get(position);
	}

	@Override
	public long getItemId(int position) {
		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		try {
			LayoutInflater inflater=mActivity.getLayoutInflater();
			View v = inflater.inflate(R.layout.dashboard_item_layout, null);
			Button btn = (Button) v.findViewById(R.id.btntype);
			TextView txt_name = (TextView) v.findViewById(R.id.txtname);
			txt_name.setTypeface(Util.roboto_regular);
			/*if(!dashboard_data.dashboardTicketsPaymentList.get(position).paygatey.equalsIgnoreCase("Check") && !dashboard_data.dashboardTicketsPaymentList.get(position).paygatey.equalsIgnoreCase("Cash")
					 && !dashboard_data.dashboardTicketsPaymentList.get(position).paygatey.equalsIgnoreCase("External Pay Gateway")){
			txt_name.setText(dashboard_data.dashboardTicketsPaymentList.get(position).paygatey+" "+dashboard_data.dashboardTicketsPaymentList.get(position).paygatey.PGateway_Type__r.Adaptive_Type__c
					+" "+Util.db.getCurrency(checkedin_event_record.Events.BLN_Country__c).Currency_Symbol__c+Util.RoundTo2Decimals(dashboard_data.dashboardTicketsPaymentList.get(position).revenue));
			}else{
				txt_name.setText(dashboard_data.dashboardTicketsPaymentList.get(position).paygatey.PGateway_Type__r.Name
						+" "+Util.db.getCurrency(checkedin_event_record.Events.BLN_Country__c).Currency_Symbol__c+Util.RoundTo2Decimals(dashboard_data.dashboardTicketsPaymentList.get(position).revenue));

			}*/
			
			txt_name.setText(dashboard_data.dashboardTicketsPaymentList.get(position).paygatey+" "+Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(dashboard_data.dashboardTicketsPaymentList.get(position).revenue));
			btn.setBackgroundColor(ColorTemplates.PAI_COLORS[position]);
			
			
			return v;
		} catch (Exception e) {
			
			e.printStackTrace();
		}return null;
	}
	
}
