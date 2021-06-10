//  ScanAttendee Android
//  Created by Ajay on Jul 8, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.scanattendee;

/**
 * @author mayank
 *
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.github.mikephil.charting.utils.PercentFormatter;
import com.globalnest.Barchart.BarAttr;
import com.globalnest.Barchart.BarChartAdapter;
import com.globalnest.Barchart.ColorTemplates;
import com.globalnest.Barchart.ExpandableVerticalBarChartAdapter;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.DashboradHandler;
import com.globalnest.mvc.SeminaAgenda;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.objects.RegistrationSettingsController;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import RLSDK.ad;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by cnbuff410
 */
public class DashboardListAdapter extends BaseExpandableListAdapter implements OnChartValueSelectedListener{
	private Activity mActivity;
	private ArrayList<String> mGroupNames = new ArrayList<String>();
	private DashboradHandler mDahboardHandler;
	private Typeface mTf;
	private EventObjects checkedin_event_record = new EventObjects();
	private ExpandableListView exp_listview;

	public DashboardListAdapter(Activity activity,ArrayList<String> GroupNames,DashboradHandler dashboardHandler,EventObjects checkedineventId,ExpandableListView exp_Listview) {
		this.mActivity = activity;
		this.mGroupNames=GroupNames;
		this.mDahboardHandler=dashboardHandler;
		this.exp_listview=exp_Listview;
		checkedin_event_record = checkedineventId;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
		
		//ChildViewHolder viewHolder = null;
		// General ListView optimization code.
		
		
		if(groupPosition==0){
			
			if(mGroupNames.size() == 1){
				final ArrayList<BarAttr> groupBarAttr=new ArrayList<BarAttr>();
				HashMap<Integer, ArrayList<BarAttr>> childBarAttr=new HashMap<Integer, ArrayList<BarAttr>>();
				view = mActivity.getLayoutInflater().inflate(R.layout.expanded_bar_chart, null);
				LinearLayout noTicketLay = (LinearLayout) view.findViewById(R.id.noTicketLay);
				final ExpandableListView listview = (ExpandableListView) view.findViewById(R.id.expanded_barchart);
				//RelativeLayout checkinLayout=(RelativeLayout)view.findViewById(R.id.checkinLayout); 
				//BarAttr attr=new BarAttr();
				double frontPercentage=0.0;
				int pTotalCheckin=0,pTotalCheckout=0;
				
				for(int i=0; i< mDahboardHandler.sessionsReport.size(); i++){

					pTotalCheckin=0;pTotalCheckout=0;
					if(mDahboardHandler.sessionsReport.get(i).lineItems.size()>0){
				
						boolean isFreeSession = false;
						BarAttr groupAttr=new BarAttr();
						groupAttr.barBottomLabels=mDahboardHandler.sessionsReport.get(i).sess.Name;
						pTotalCheckin = mDahboardHandler.sessionsReport.get(i).totalItemschkQty;
						pTotalCheckout = mDahboardHandler.sessionsReport.get(i).totalItemsbuyQty;
						for (int j = 0; j < mDahboardHandler.sessionsReport.get(i).lineItems.size(); j++) {
							String item_pool_id =  mDahboardHandler.sessionsReport.get(i).lineItems	.get(j).sesItem.BLN_Item_Pool__r.Id;
							if(Util.db.isItemPoolFreeSession(item_pool_id, checkedin_event_record.Events.Id)){
								isFreeSession = true;
								break;
							}
						}
						if(isFreeSession){
							frontPercentage = (pTotalCheckin * 100)/100;
						}else if (pTotalCheckout != 0){
							frontPercentage = (pTotalCheckin * 100) / (pTotalCheckout);
						}else{
							frontPercentage = 0.0;
						}
						if(isFreeSession){
							groupAttr.barTopLabels = pTotalCheckin+"";
						}else{
							groupAttr.barTopLabels = pTotalCheckin + "/" + (pTotalCheckout);
						}
						
						groupAttr.frontBarPercentage = frontPercentage;
						groupAttr.isType = true;
						groupAttr.isPackage = true;
						groupAttr.item_pool_c = mDahboardHandler.sessionsReport.get(i).sess.Id;
						groupBarAttr.add(i, groupAttr);
						ArrayList<BarAttr> childAttrList = new ArrayList<BarAttr>();
						//Log.i("----------------Session Name--------------",":"+mDahboardHandler.sessionsReport.get(i).sess.Name+ " : "+mDahboardHandler.sessionsReport.get(i).lineItems.size());
						
						for (int j = 0; j < mDahboardHandler.sessionsReport.get(i).lineItems.size(); j++) {
							//Log.i("-----------------pool Name--------------",":"+mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c+ " : "+mDahboardHandler.sessionsReport.get(i).lineItems.size());
							BarAttr childAttr = new BarAttr();
							String parent_id = Util.db.getItemPoolParentId(mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id,BaseActivity.checkedin_event_record.Events.Id);
							if(!Util.NullChecker(parent_id).isEmpty()){
								String package_name = Util.db.getItem_Pool_Name(parent_id, BaseActivity.checkedin_event_record.Events.Id);
								childAttr.barBottomLabels = mDahboardHandler.sessionsReport.get(i).lineItems
										.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c +" ( "+package_name+" )";
							}else{
								childAttr.barBottomLabels = mDahboardHandler.sessionsReport.get(i).lineItems
										.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c;
							}
							
							
							if(isFreeSession){
								childAttr.barTopLabels = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont
										+ "" ;
							}else{
								childAttr.barTopLabels = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont
										+ "/" + ( mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty);
							}
							
							
							childAttr.item_pool_c = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id;
							
							if(isFreeSession){
								childAttr.frontBarPercentage = (mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont * 100)/100;
							}else if ( mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty != 0) {
								
								childAttr.frontBarPercentage = (mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont * 100)
										                       /  mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty;
								
							} else {
								childAttr.frontBarPercentage = 0.0;
							}
							childAttrList.add(childAttr);
						}
						childBarAttr.put(i,childAttrList);
					}else{
						
						BarAttr groupAttr=new BarAttr();
						groupAttr.isPackage=false;
						
						String name = mDahboardHandler.dashboardticketsList.get(i).item.item_name__c;
						SeminaAgenda agenda = Util.db.getSeminarAgenda("where "+DBFeilds.SEMINAR_ITEM_POOL_ID+"='"+mDahboardHandler.dashboardticketsList.get(i).item.Item_Pool__c+"'");
						if(agenda != null){
							String start_date = Util.change_US_ONLY_DateFormat(agenda.startTime, checkedin_event_record.Events.Time_Zone__c);
							String end_date = Util.change_US_ONLY_DateFormat(agenda.endtime, checkedin_event_record.Events.Time_Zone__c);
							name = name+"\n("+start_date+" to "+ end_date +" , Room: "+agenda.room+" , Room No.: "+agenda.roomNo+")";
						}
						groupAttr.barBottomLabels=name;
						
						groupAttr.item_pool_c = mDahboardHandler.dashboardticketsList.get(i).item.Item_Pool__c;
							if((mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt)!=0){
								frontPercentage=(mDahboardHandler.dashboardticketsList.get(i).chekinCont*100)/(mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt);
							}else{
								frontPercentage=0.0;
							}
						groupAttr.barTopLabels=mDahboardHandler.dashboardticketsList.get(i).chekinCont+"/"+(mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt);
						groupAttr.frontBarPercentage=frontPercentage;
						groupAttr.isType=false;
						groupBarAttr.add(i,groupAttr);
						ArrayList<BarAttr>	cAttrList=new ArrayList<BarAttr>();
						childBarAttr.put(i,cAttrList);
					}

				}
				
				final ExpandableVerticalBarChartAdapter adapter=new ExpandableVerticalBarChartAdapter(mActivity, groupBarAttr,childBarAttr,listview);
				listview.setAdapter(adapter);
				//Util.setListViewHeightBasedOnChildren(listview);
				for(int i =0;i<groupBarAttr.size();i++){
	            	   Util.setListViewHeight(listview, i);
	              }

			
				listview.setOnGroupClickListener(new OnGroupClickListener() {
					
					@Override
					public boolean onGroupClick(ExpandableListView parent, View v,
							int groupPosition, long id) {
						
						BarAttr groupAttr=(BarAttr)adapter.getGroupAttr(groupPosition);
						//Log.i("-------------------Group Click Index-------------", " = "+groupPosition+"\n Is Package ="+groupAttr.isPackage+"\n itempool id= "+groupAttr.item_pool_c);
						if(groupAttr.isPackage){
							
							boolean isFreeSession  = false;
							if(adapter.getChildrenCount(groupPosition) > 0){
								BarAttr childAttr = adapter.getChildAttr(groupPosition, 0);
								isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
							}
							//boolean isFreeSession = Util.db.isItemPoolFreeSession(groupAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
							if(isFreeSession){
								Intent sessionIntent=new Intent(mActivity,SessionListActivity.class);
								sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
								sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.barBottomLabels);
								mActivity.startActivity(sessionIntent);
							}else{
								if(!isBuyersAndAttendeesHide()){
									Intent sessionIntent=new Intent(mActivity,AttendeeListActivity.class);
									sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
									sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
									sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.barBottomLabels);
									mActivity.startActivity(sessionIntent);
								}
							}
							
						}
						return true;
					}
				});
				
				listview.setOnChildClickListener(new OnChildClickListener() {
					
					@Override
					public boolean onChildClick(ExpandableListView parent, View v,
							int groupPosition, int childPosition, long id) {
						BarAttr childAttr=(BarAttr)adapter.getChildAttr(groupPosition, childPosition);
						BarAttr groupAttr=(BarAttr)adapter.getGroupAttr(groupPosition);
						//Log.i("-------------------Child Click Index-------------", " = "+groupPosition+":"+childPosition+"\n Is Package ="+childAttr.isPackage+"\n itempool id= "+childAttr.item_pool_c);
						
							boolean isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
							if(isFreeSession){
								Intent sessionIntent=new Intent(mActivity,SessionListActivity.class);
								sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
								sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
								sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.barBottomLabels+" ( "+groupAttr.barBottomLabels+" ) ");
								mActivity.startActivity(sessionIntent);
							}else {
								if(!isBuyersAndAttendeesHide()){
									Intent sessionIntent=new Intent(mActivity,AttendeeListActivity.class);
									sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
									sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
									sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
									sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.barBottomLabels+" ( "+groupAttr.barBottomLabels+" ) ");
									mActivity.startActivity(sessionIntent);
								}
							}
						
						return false;
					}
				});
				
			}else{
				view = mActivity.getLayoutInflater().inflate(R.layout.total_stastics_view, null);
				int totalCheckins=0;
				TextView totalOrders = (TextView) view.findViewById(R.id.txt_ttlOrders);
				TextView totalAttendees = (TextView) view.findViewById(R.id.txt_ttlAttendees);
				TextView totalRevenue = (TextView) view.findViewById(R.id.total_revGenerated);
				TextView onsiteOrders = (TextView) view.findViewById(R.id.onsite_totalorders);
				TextView onsiteAttendees = (TextView) view.findViewById(R.id.onsite_totalAttendees);
				TextView onsiterevenue = (TextView) view.findViewById(R.id.onsite_revenue);

		/*for(int i=0; i< mDahboardHandler.Checkins.size(); i++){

			if(Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount)!=0)
			{

				totalCheckins=totalCheckins+Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount);
			}

		}*/
				totalOrders.setText(String.valueOf((int) mDahboardHandler.totalOrders));
				totalAttendees.setText(String.valueOf((int) mDahboardHandler.totalAttendee));
				totalRevenue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(mDahboardHandler.totalrevenue));
				onsiteOrders.setText(String.valueOf((int) mDahboardHandler.OnsiteAttendeereg));
				onsiteAttendees.setText(String.valueOf((int) mDahboardHandler.OnsiteAttendeereg));
				onsiterevenue.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c + Util.RoundTo2Decimals(mDahboardHandler.OnsitePaymentRevenue));

				/*TextView ttlTicket=(TextView) view.findViewById(R.id.txt_ttlTicket);
				TextView ttlAttendee=(TextView) view.findViewById(R.id.txt_ttlAttendee);
				TextView ttlCheckin=(TextView) view.findViewById(R.id.txt_ttlCheckin);
				TextView ttlOrders=(TextView) view.findViewById(R.id.txt_ttlOrders);
				TextView ttlSold=(TextView) view.findViewById(R.id.txt_ttlSold);
				TextView ttlRevGenerated=(TextView) view.findViewById(R.id.txt_revGenerated);

				*//*for(int i=0; i< mDahboardHandler.Checkins.size(); i++){

					if(Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount)!=0)
					{

						totalCheckins=totalCheckins+Integer.parseInt(mDahboardHandler.Checkins.get(i).TotalCheckInCount);
					}

				}*//*

				ttlTicket.setText(String.valueOf((int)mDahboardHandler.totalTickets));
				ttlAttendee.setText(String.valueOf((int)mDahboardHandler.totalAttendee));
				ttlCheckin.setText(String.valueOf((int)mDahboardHandler.chekinCont+"/"+(mDahboardHandler.checloutCnt+mDahboardHandler.notCheckdin)));
				ttlOrders.setText(String.valueOf((int)mDahboardHandler.totalcompOrders));
				ttlSold.setText(String.valueOf((int)mDahboardHandler.totalSoldTickets));
				ttlRevGenerated.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(mDahboardHandler.totalrevenue));
			*/}
			


		}else if(groupPosition==1){

			view = mActivity.getLayoutInflater().inflate(R.layout.barchart_layout, null);
			TextView txt_ticmoreinfo=(TextView) view.findViewById(R.id.txt_ticmoreinfo);
			LinearLayout noTicketLay = (LinearLayout) view.findViewById(R.id.noTicketLay);
			ListView listview = (ListView) view.findViewById(R.id.list_expand);
			txt_ticmoreinfo.setVisibility(View.GONE);
			double frontPercentage = 0.0;
			ArrayList<BarAttr> barAttr=new ArrayList<BarAttr>();
			if(mDahboardHandler.dashboardticketsList.size()>0){
				for (int i = 0; i < mDahboardHandler.dashboardticketsList.size(); i++) {

					int total_qty = mDahboardHandler.dashboardticketsList.get(i).item.item_count__c;
							//+ mDahboardHandler.dashboardticketsList.get(i).gettAvailable();
					if (total_qty != 0) {
						frontPercentage = (mDahboardHandler.dashboardticketsList
								.get(i).ItemQuantity * 100) / total_qty;
						
					} else {
						frontPercentage = 0.0;

					}
					String revenue=Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(mDahboardHandler.dashboardticketsList.get(i).itrevenue);

					BarAttr attr=new BarAttr();
					attr.barBottomLabels= mDahboardHandler.dashboardticketsList.get(i).item.item_name__c+" "+revenue;
					attr.barTopLabels=String.valueOf(mDahboardHandler.dashboardticketsList.get(i).ItemQuantity + "/" + total_qty);
					attr.frontBarPercentage= frontPercentage;
					attr.backBarPercentage=0.0;
					// attr.barColor=ColorTemplates.VORDIPLOM_COLORS;
					barAttr.add(attr);
				}


				BarChartAdapter adapter = new BarChartAdapter(mActivity, barAttr);
				listview.setAdapter(adapter);
				//Util.setListViewHeightBasedOnChildren(listview);
				
				listview.setOnTouchListener(new View.OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {

			// Disallow the touch request for parent scroll on touch of child view
			            exp_listview.requestDisallowInterceptTouchEvent(true);

			            int action = event.getActionMasked();
			            switch (action) {
			                case MotionEvent.ACTION_UP:
			                	exp_listview.requestDisallowInterceptTouchEvent(false);
			                    break;
			            }
			            return false;
			        }
			    });
			}else{
				//noTicketLay.setVisibility(View.VISIBLE);
			}

		}else if(groupPosition==2){

			view = mActivity.getLayoutInflater().inflate(R.layout.pichart_layout, null);
			TextView totalSales=(TextView) view.findViewById(R.id.txttotalsales);
			FrameLayout paymentLayout=(FrameLayout) view.findViewById(R.id.paymentLayout);
			FrameLayout nopaymentLayout=(FrameLayout) view.findViewById(R.id.nopaymentLayout);
			PieChart pieChart=(PieChart) view.findViewById(R.id.pieChart);
			ListView paymentList=(ListView) view.findViewById(R.id.paymentview);
			LinearLayout paymentpielayout_notfound=(LinearLayout) view.findViewById(R.id.paymentpielayout_notfound);
			int total_sales=0;
		
				int[] colors = new int[mDahboardHandler.dashboardTicketsPaymentList.size()];

				String[] slicesName=new String[mDahboardHandler.dashboardTicketsPaymentList.size()];
				float[] slicePercent=new float[mDahboardHandler.dashboardTicketsPaymentList.size()];

				if(mDahboardHandler.dashboardTicketsPaymentList.size()>0)
				{
					for(int i=0;i<mDahboardHandler.dashboardTicketsPaymentList.size();i++)
					{
						total_sales += mDahboardHandler.dashboardTicketsPaymentList.get(i).revenue;

					}
				}

				if(mDahboardHandler.dashboardTicketsPaymentList.size()>0)
				{
					for(int i=0;i<mDahboardHandler.dashboardTicketsPaymentList.size();i++)
					{
						//Log.i("--------------Revenue-------------",":"+mDahboardHandler.dashboardTicketsPaymentList.get(i).revenue+pieChart);
						if(mDahboardHandler.dashboardTicketsPaymentList.get(i).revenue>0)
						{
							colors[i]=ColorTemplates.PAI_COLORS[i];
							
							slicePercent[i]=(float) ((mDahboardHandler.dashboardTicketsPaymentList.get(i).revenue*100)/total_sales);
							slicesName[i]="";
						}
					}
					setPieChart(slicesName,slicePercent,pieChart);
				}else{
					total_sales=0;
				}
				paymentList.setAdapter(new PaymentAdapter(mActivity,mDahboardHandler,checkedin_event_record));
				totalSales.setText("Total Sales: "+Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(total_sales));
			
				paymentList.setOnTouchListener(new ListView.OnTouchListener() {
			        @Override
			        public boolean onTouch(View v, MotionEvent event) {
			            int action = event.getAction();
			            switch (action) {
			            case MotionEvent.ACTION_DOWN:
			                // Disallow ScrollView to intercept touch events.
			                v.getParent().requestDisallowInterceptTouchEvent(true);
			                break;

			            case MotionEvent.ACTION_UP:
			                // Allow ScrollView to intercept touch events.
			                v.getParent().requestDisallowInterceptTouchEvent(false);
			                break;
			            }

			            // Handle ListView touch events.
			            v.onTouchEvent(event);
			            return true;
			        }
			    });
			



		}else if(groupPosition==3){
			final ArrayList<BarAttr> groupBarAttr=new ArrayList<BarAttr>();
			HashMap<Integer, ArrayList<BarAttr>> childBarAttr=new HashMap<Integer, ArrayList<BarAttr>>();
			view = mActivity.getLayoutInflater().inflate(R.layout.expanded_bar_chart, null);
			LinearLayout noTicketLay = (LinearLayout) view.findViewById(R.id.noTicketLay);
			final ExpandableListView listview = (ExpandableListView) view.findViewById(R.id.expanded_barchart);
			//RelativeLayout checkinLayout=(RelativeLayout)view.findViewById(R.id.checkinLayout); 
			//BarAttr attr=new BarAttr();
			double frontPercentage=0.0;
			int pTotalCheckin=0,pTotalCheckout=0;
			
			for(int i=0; i< mDahboardHandler.sessionsReport.size(); i++){

				pTotalCheckin=0;pTotalCheckout=0;
				if(mDahboardHandler.sessionsReport.get(i).lineItems.size()>0){
			
					boolean isFreeSession = false;
					BarAttr groupAttr=new BarAttr();
					groupAttr.barBottomLabels=mDahboardHandler.sessionsReport.get(i).sess.Name;
					pTotalCheckin = mDahboardHandler.sessionsReport.get(i).totalItemschkQty;
					pTotalCheckout = mDahboardHandler.sessionsReport.get(i).totalItemsbuyQty;
					for (int j = 0; j < mDahboardHandler.sessionsReport.get(i).lineItems.size(); j++) {
						String item_pool_id =  mDahboardHandler.sessionsReport.get(i).lineItems	.get(j).sesItem.BLN_Item_Pool__r.Id;
						if(Util.db.isItemPoolFreeSession(item_pool_id, checkedin_event_record.Events.Id)){
							isFreeSession = true;
							break;
						}
					}
					if(isFreeSession){
						frontPercentage = (pTotalCheckin * 100)/100;
					}else if (pTotalCheckout != 0){
						frontPercentage = (pTotalCheckin * 100) / (pTotalCheckout);
					}else{
						frontPercentage = 0.0;
					}
					if(isFreeSession){
						groupAttr.barTopLabels = pTotalCheckin+"";
					}else{
						groupAttr.barTopLabels = pTotalCheckin + "/" + (pTotalCheckout);
					}
					
					groupAttr.frontBarPercentage = frontPercentage;
					groupAttr.isType = true;
					groupAttr.isPackage = true;
					groupAttr.item_pool_c = mDahboardHandler.sessionsReport.get(i).sess.Id;
					groupBarAttr.add(i, groupAttr);
					ArrayList<BarAttr> childAttrList = new ArrayList<BarAttr>();
					//Log.i("----------------Session Name--------------",":"+mDahboardHandler.sessionsReport.get(i).sess.Name+ " : "+mDahboardHandler.sessionsReport.get(i).lineItems.size());
					
					for (int j = 0; j < mDahboardHandler.sessionsReport.get(i).lineItems.size(); j++) {
						//Log.i("-----------------pool Name--------------",":"+mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c+ " : "+mDahboardHandler.sessionsReport.get(i).lineItems.size());
						BarAttr childAttr = new BarAttr();
						/*childAttr.barBottomLabels = mDahboardHandler.sessionsReport.get(i).lineItems
								.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c;*/
						String parent_id = Util.db.getItemPoolParentId(mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id,BaseActivity.checkedin_event_record.Events.Id);
						if(!Util.NullChecker(parent_id).isEmpty()){
							String package_name = Util.db.getItem_Pool_Name(parent_id, BaseActivity.checkedin_event_record.Events.Id);
							childAttr.barBottomLabels = mDahboardHandler.sessionsReport.get(i).lineItems
									.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c +" ( "+package_name+" )";
						}else{
							childAttr.barBottomLabels = mDahboardHandler.sessionsReport.get(i).lineItems
									.get(j).sesItem.BLN_Item_Pool__r.Item_Pool_Name__c;
						}
						if(isFreeSession){
							childAttr.barTopLabels = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont
									+ "" ;
						}else{
							childAttr.barTopLabels = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont
									+ "/" + ( mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty);
						}
						
						
						childAttr.item_pool_c = mDahboardHandler.sessionsReport.get(i).lineItems.get(j).sesItem.BLN_Item_Pool__r.Id;
						
						if(isFreeSession){
							childAttr.frontBarPercentage = (mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont * 100)/100;
						}else if ( mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty != 0) {
							
							childAttr.frontBarPercentage = (mDahboardHandler.sessionsReport.get(i).lineItems.get(j).chekinCont * 100)
									                       /  mDahboardHandler.sessionsReport.get(i).lineItems.get(j).totalBuyQty;
							
						} else {
							childAttr.frontBarPercentage = 0.0;
						}
						childAttrList.add(childAttr);
					}
					childBarAttr.put(i,childAttrList);
				}else{
					
					BarAttr groupAttr=new BarAttr();
					groupAttr.isPackage=false;
					
					String name = mDahboardHandler.dashboardticketsList.get(i).item.item_name__c;
					SeminaAgenda agenda = Util.db.getSeminarAgenda("where "+DBFeilds.SEMINAR_ITEM_POOL_ID+"='"+mDahboardHandler.dashboardticketsList.get(i).item.Item_Pool__c+"'");
					if(agenda != null){
						String start_date = Util.change_US_ONLY_DateFormat(agenda.startTime, checkedin_event_record.Events.Time_Zone__c);
						String end_date = Util.change_US_ONLY_DateFormat(agenda.endtime, checkedin_event_record.Events.Time_Zone__c);
						name = name+"\n("+start_date+" to "+ end_date +" , Room: "+agenda.room+" , Room No.: "+agenda.roomNo+")";
					}
					groupAttr.barBottomLabels=name;
					
					groupAttr.item_pool_c = mDahboardHandler.dashboardticketsList.get(i).item.Item_Pool__c;
						if((mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt)!=0){
							frontPercentage=(mDahboardHandler.dashboardticketsList.get(i).chekinCont*100)/(mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt);
						}else{
							frontPercentage=0.0;
						}
					groupAttr.barTopLabels=mDahboardHandler.dashboardticketsList.get(i).chekinCont+"/"+(mDahboardHandler.dashboardticketsList.get(i).chekinCont+mDahboardHandler.dashboardticketsList.get(i).checloutCnt);
					groupAttr.frontBarPercentage=frontPercentage;
					groupAttr.isType=false;
					groupBarAttr.add(i,groupAttr);
					ArrayList<BarAttr>	cAttrList=new ArrayList<BarAttr>();
					childBarAttr.put(i,cAttrList);
				}

			}
			
			final ExpandableVerticalBarChartAdapter adapter=new ExpandableVerticalBarChartAdapter(mActivity, groupBarAttr,childBarAttr,listview);
			listview.setAdapter(adapter);
			//Util.setListViewHeightBasedOnChildren(listview);
			for(int i =0;i<groupBarAttr.size();i++){
         	   Util.setListViewHeight(listview, i);
           }
			listview.setOnGroupClickListener(new OnGroupClickListener() {
				
				@Override
				public boolean onGroupClick(ExpandableListView parent, View v,
						int groupPosition, long id) {
					
					
					BarAttr groupAttr=(BarAttr)adapter.getGroupAttr(groupPosition);
					//Log.i("-------------------Group Click Index-------------", " = "+groupPosition+"\n Is Package ="+groupAttr.isPackage+"\n itempool id= "+groupAttr.item_pool_c);
					if(groupAttr.isPackage){
						boolean isFreeSession  = false;
						if(adapter.getChildrenCount(groupPosition) > 0){
							BarAttr childAttr = adapter.getChildAttr(groupPosition, 0);
							isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
						}
						 //isFreeSession = Util.db.isItemPoolFreeSession(groupAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
						if(isFreeSession){
							Intent sessionIntent=new Intent(mActivity,SessionListActivity.class);
							sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.barBottomLabels);
							mActivity.startActivity(sessionIntent);
						}else{
							Intent sessionIntent=new Intent(mActivity,AttendeeListActivity.class);
							sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
							sessionIntent.putExtra(Util.INTENT_KEY_3, groupAttr.barBottomLabels);
							mActivity.startActivity(sessionIntent);
						}
					}
					return true;
				}
			});
			
			listview.setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					BarAttr childAttr=(BarAttr)adapter.getChildAttr(groupPosition, childPosition);
					BarAttr groupAttr=(BarAttr)adapter.getGroupAttr(groupPosition);
					//Log.i("-------------------Child Click Index-------------", " = "+groupPosition+":"+childPosition+"\n Is Package ="+childAttr.isPackage+"\n itempool id= "+childAttr.item_pool_c);
					
						boolean isFreeSession = Util.db.isItemPoolFreeSession(childAttr.item_pool_c,BaseActivity.checkedin_event_record.Events.Id);
						if(isFreeSession){
							Intent sessionIntent=new Intent(mActivity,SessionListActivity.class);
							sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
							sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.barBottomLabels+" ( "+groupAttr.barBottomLabels+" ) ");
							mActivity.startActivity(sessionIntent);
						}else {
							Intent sessionIntent=new Intent(mActivity,AttendeeListActivity.class);
							sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS, childAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_1, groupAttr.item_pool_c);
							sessionIntent.putExtra(Util.INTENT_KEY_2, DashboardActivity.class.getName());
							sessionIntent.putExtra(Util.INTENT_KEY_3, childAttr.barBottomLabels+" ( "+groupAttr.barBottomLabels+" ) ");
							mActivity.startActivity(sessionIntent);
						}
						/*Intent sessionIntent=new Intent(mActivity,SessionListActivity.class);
						sessionIntent.putExtra(WebServiceUrls.SA_GETSESSIONCHECKINS,childAttr.item_pool_c);
						mActivity.startActivity(sessionIntent);*/
					
					return false;
				}
			});
			
		
		}
		return view;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupNames.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupNames.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View view,
			ViewGroup parent) {

		GroupViewHolder viewHolder;
		if (view == null) {
			view = mActivity.getLayoutInflater().inflate(R.layout
					.dashbord_group_view, null);
			viewHolder = new GroupViewHolder();
			viewHolder.name = (TextView) view.findViewById(R.id.txt_groupText);
			view.setTag(viewHolder);
		} else {
			viewHolder = (GroupViewHolder) view.getTag();
		}

		viewHolder.name.setText(mGroupNames.get(groupPosition));

		return view;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	static class GroupViewHolder {
		TextView name;
	}

	static class ChildViewHolder {
		TextView device;
	}

	public void setPieChart(String[] sliceName,float[] slicePercent ,PieChart paymentChart_layout){
		// paymentChart_layout = (PieChart) findViewById(R.id.chart1);
		paymentChart_layout.setUsePercentValues(true);
		paymentChart_layout.setDescription("");
		paymentChart_layout.setDragDecelerationFrictionCoef(0.95f);
		mTf = Typeface.createFromAsset(mActivity.getAssets(),"OpenSans-Regular.ttf");
		paymentChart_layout.setCenterTextTypeface(Typeface.createFromAsset(mActivity.getAssets(), "OpenSans-Regular.ttf"));
		paymentChart_layout.setDrawHoleEnabled(true);
		paymentChart_layout.setHoleColorTransparent(true);
		paymentChart_layout.setTransparentCircleColor(Color.WHITE);
		paymentChart_layout.setHoleRadius(30f);
		paymentChart_layout.setTransparentCircleRadius(35f);
		paymentChart_layout.setDrawCenterText(true);
		paymentChart_layout.setRotationAngle(0);
		//enable rotation of the chart by touch
		paymentChart_layout.setRotationEnabled(true);
		paymentChart_layout.setOnChartValueSelectedListener(this);
		paymentChart_layout.setCenterText("");
		setPieData(sliceName, slicePercent, paymentChart_layout);
		paymentChart_layout.animateY(1500, Easing.EasingOption.EaseInOutQuad);
		//mChart.spin(2000, 0, 360);
		Legend l = paymentChart_layout.getLegend();
		l.setPosition(LegendPosition.RIGHT_OF_CHART);
		l.setXEntrySpace(7f);
		l.setYEntrySpace(0f);
		l.setYOffset(0f);
		l.setEnabled(false);
	}


	private void setPieData(String[] names, float[] percent,PieChart paymentChart_layout) {
		// float mult = range;
		ArrayList<Entry> yVals1 = new ArrayList<Entry>();
		for (int i = 0; i < percent.length; i++) {
			//Log.i("------------Percent Values-------------",":"+percent[i]);
			yVals1.add(new Entry(percent[i], i));
		}
		ArrayList<String> xVals = new ArrayList<String>();
		for (int i = 0; i < names.length; i++)
		{
			if (!AppUtils.NullChecker(names[i]).isEmpty()) {
				xVals.add(names[i]);
			} else {
				xVals.add("");
			}
		}

		PieDataSet dataSet = new PieDataSet(yVals1, "");
		dataSet.setSliceSpace(3f);
		dataSet.setSelectionShift(5f);
		// add a lot of colors
		ArrayList<Integer> colors = new ArrayList<Integer>();

		for (int c : ColorTemplates.PAI_COLORS){
			colors.add(c);
		}
		dataSet.setColors(colors);

		PieData data = new PieData(xVals, dataSet);
		data.setValueFormatter(new PercentFormatter());
		data.setValueTextSize(11f);
		data.setValueTextColor(Color.BLACK);
		data.setValueTypeface(mTf);
		paymentChart_layout.setData(data);

		// undo all highlights
		paymentChart_layout.highlightValues(null);

		paymentChart_layout.invalidate();
	}

	/* (non-Javadoc)
	 * @see com.github.mikephil.charting.listener.OnChartValueSelectedListener#onValueSelected(com.github.mikephil.charting.data.Entry, int, com.github.mikephil.charting.utils.Highlight)
	 */
	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {


	}

	/* (non-Javadoc)
	 * @see com.github.mikephil.charting.listener.OnChartValueSelectedListener#onNothingSelected()
	 */
	@Override
	public void onNothingSelected() {


	}
	
	
	
	public void OpenTicketMoreInfoDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		

		LayoutInflater li = LayoutInflater.from(mActivity);
		View promptsView = li.inflate(R.layout.dashboard_more_ticinfo, null);
		builder.setView(promptsView);
		
		ListView listView_eachTicRevnue =(ListView) promptsView.findViewById(R.id.listView_eachTicRevnue);
		TextView txt_close=(TextView) promptsView.findViewById(R.id.txt_close);
		EachTicketPopupAdapter adapter=new EachTicketPopupAdapter();
		listView_eachTicRevnue.setAdapter(adapter);
		final AlertDialog alertDialog = builder.create();
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		alertDialog.show();
		
		txt_close.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
			}
		});
	}
	
	class EachTicketPopupAdapter extends BaseAdapter{

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mDahboardHandler.dashboardticketsList.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mDahboardHandler.dashboardticketsList.get(position);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v =LayoutInflater.from(mActivity).inflate(R.layout.checkin_history_list_item, null);
			TextView txt_checkin_history_time=(TextView) v.findViewById(R.id.txt_checkin_history_time);
			TextView txt_checkin_history_status=(TextView) v.findViewById(R.id.txt_checkin_history_status);
			TextView txt_checkin_history_scanby=(TextView) v.findViewById(R.id.txt_checkin_history_scanby);
			
			txt_checkin_history_scanby.setVisibility(View.GONE);
			txt_checkin_history_status.setGravity(Gravity.RIGHT);
			txt_checkin_history_time.setText(mDahboardHandler.dashboardticketsList.get(position).item.item_name__c);
			txt_checkin_history_status.setText(Util.db.getCurrency(checkedin_event_record.Events.BLN_Currency__c).Currency_Symbol__c+Util.RoundTo2Decimals(mDahboardHandler.dashboardticketsList.get(position).itrevenue));
			
			return v;
		}
		
	}
	
	public boolean isBuyersAndAttendeesHide(){
		boolean buyers=false, attendee=false;
		List<RegistrationSettingsController> regsettings = Util.db.getRegSettingsList("where "+DBFeilds.REG_EVENT_ID+"='"+BaseActivity.checkedin_event_record.Events.Id+"' AND "+DBFeilds.REG_SETTING_TYPE+"='ScanAttendeeapp'");
		for(RegistrationSettingsController setting:regsettings){
			if(setting.Label_Name__c.equalsIgnoreCase("Display Attendees")){
				attendee = Boolean.valueOf(setting.Included__c);
			}else if(setting.Label_Name__c.equalsIgnoreCase("Display Buyers")){
				buyers = Boolean.valueOf(setting.Included__c);
			}
		}
		if(!buyers && !attendee){
			return true;
		}
		return false;
	}
}