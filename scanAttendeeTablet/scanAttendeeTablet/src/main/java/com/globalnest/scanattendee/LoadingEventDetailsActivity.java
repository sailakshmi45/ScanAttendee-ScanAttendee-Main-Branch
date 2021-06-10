package com.globalnest.scanattendee;


import java.lang.reflect.Type;
import java.util.List;

import com.globalnest.classes.CircleProgressView;
import com.globalnest.database.DBFeilds;
import com.globalnest.mvc.BadgeResponse;
import com.globalnest.mvc.BadgeResponseNew;
import com.globalnest.mvc.ItemsListResponse;
import com.globalnest.mvc.TicketListResponseHandler;
import com.globalnest.mvc.TotalOrderListHandler;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.EventObjects;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

public class LoadingEventDetailsActivity extends BaseActivity {



	CircleProgressView mCircleView;
	Cursor badgeCount,ticketCountt,attendeeCount;
	private static String BADGES="badges";
	private static String TICKETS="tickets";
	private static String ATTENDEES="attendees";
	int count=0;
	private String requestType="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading_event_details);
		mCircleView=(CircleProgressView) findViewById(R.id.circleView);
		badgeCount=Util.db.getBadgeTemplateCursor();
		attendeeCount=Util.db.getPaymentCursor(" where "+DBFeilds.ORDER_EVENT_ID+" = '"+checked_in_eventId+"'");
		ticketCountt=Util.db.getItems(" where "+DBFeilds.ADDED_ITEM_EVENTID+" = '"+checked_in_eventId+"'");


		mCircleView = (CircleProgressView) findViewById(R.id.circleView);
//      value setting
		mCircleView.setMaxValue(100);
		mCircleView.setValue(0);
		mCircleView.setValueAnimated(24);

//        show unit
		mCircleView.setUnit("%");
		mCircleView.setShowUnit(true);

//        text sizes
		mCircleView.setTextSize(20); // text size set, auto text size off
		mCircleView.setUnitSize(15); // if i set the text size i also have to set the unit size

		mCircleView.setAutoTextSize(true); // enable auto text size, previous values are overwritten

//        spinning
		mCircleView.spin(); // start spinning
		//mCircleView.stopSpinning(); // stops spinning. Spinner gets shorter until it disappears.
		//mCircleView.setValueAnimated(24); // stops spinning. Spinner spinns until on top. Then fills to set value.

		if(badgeCount.getCount()==0){
			requestType=BADGES;
			doRequest();
		}else if(ticketCountt.getCount()==0){
			requestType=TICKETS;
			doRequest();
		}else if(attendeeCount.getCount()==0){
			requestType=ATTENDEES;
			doRequest();
		}

	}
	protected void onResume() {
		super.onResume();
		if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		} else {
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}
	}


	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		mCircleView.setVisibility(View.VISIBLE);
		mCircleView.spin();
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		if(requestType.equals(BADGES)){
			mCircleView.setUnit("!");
			mCircleView.setShowUnit(true);
			mCircleView.setText("Loading Badge...");
			mCircleView.setAutoTextSize(true);
			String _url = sfdcddetails.instance_url + WebServiceUrls.SA_GET_BADGE_TEMPLATE + "Event_Id=" + checked_in_eventId;
			postMethod = new HttpPostData("Loading Badge...",_url, null, access_token, LoadingEventDetailsActivity.this);
			postMethod.execute();
		}else if(requestType.equals(TICKETS)){
			mCircleView.setUnit("!");
			mCircleView.setShowUnit(true);
			mCircleView.setText("Loading Tickets...");
			mCircleView.setAutoTextSize(true);
			String url = sfdcddetails.instance_url+ WebServiceUrls.SA_GET_TICKET_LIST + "Event_id="+ checked_in_eventId;
			postMethod = new HttpPostData("Loading Tickets...", url, null,access_token, LoadingEventDetailsActivity.this);
			postMethod.execute();
		}else if(requestType.equals(ATTENDEES)){
			mCircleView.setUnit("!");
			mCircleView.setShowUnit(true);
			mCircleView.setText("Loading Attendees...");
			mCircleView.setAutoTextSize(true);
			String url = sfdcddetails.instance_url+ WebServiceUrls.SA_ATTENDEE_LOAD_MORE + "Event_id="+ checked_in_eventId + "&User_id=" + sfdcddetails.user_id
					+ "&offset=" + 0 + "&limit="+ checkedin_event_record.Events.scan_attendee_limit__c;
			postMethod = new HttpPostData("Loading Attendes...",url, null, access_token,LoadingEventDetailsActivity.this);
			postMethod.execute();
		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub

		gson = new Gson();
		if(requestType.equals(BADGES)){
			//Log.i("---Badge Array Size--",":"+badge_handler.badges.size());
			Type listType = new TypeToken<List<BadgeResponseNew>>() {}.getType();
			List<BadgeResponseNew> badges =  new Gson().fromJson(response, listType);
			AppUtils.displayLog("---------------- parseJsonResponse Badge Size----------", ":"+checkedin_event_record.Events.Mobile_Default_Badge__c+" : " + response);
			Util.db.deleteBadges(checked_in_eventId);
			sharedPreferences.edit().clear().commit();
			for(BadgeResponseNew badge : badges){
				badge.badge.event_id = checked_in_eventId;
				Util.db.InsertAndUpdateBadgeTemplateNew(badge);
			}
			//Util.db.InsertAndUpdateBadgeTemplateNew(badge_handler.badges.);
			EventObjects event_data = Util.db.getSelectedEventRecord(checked_in_eventId);
			if(!NullChecker(event_data.Events.Mobile_Default_Badge__c).isEmpty())
				Util.db.updateBadgeStatus(event_data.Events.Mobile_Default_Badge__c," Where EventID = '" + checked_in_eventId +"'");
			requestType=TICKETS;
			doRequest();
		}else if(requestType.equals(TICKETS)){
			ItemsListResponse item_resposne = gson.fromJson(response, ItemsListResponse.class);
			Type listType = new TypeToken<TicketListResponseHandler[]>() {}.getType();
			//Log.i("--------------List Type---------",":" + listType.toString());
			TicketListResponseHandler[] responseHandler = gson.fromJson(response, listType);
			//Log.i("---TICKET ARRAY SIZE---", "" + responseHandler.length);
			Util.db.upadteItemListRecordInDB(item_resposne.Itemscls_infoList,checked_in_eventId);
			requestType=ATTENDEES;
			doRequest();
		}else{
			TotalOrderListHandler totalorderlisthandler = gson.fromJson(response, TotalOrderListHandler.class);
			Util.db.upadteOrderList(totalorderlisthandler.TotalLists,checked_in_eventId);
			finish();
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

	}
}
