package com.globalnest.scanattendee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.globalnest.mvc.CheckinHistoryController;
import com.globalnest.mvc.SessionGroup;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.google.gson.Gson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class CheckinHistory extends BaseActivity {

	private CheckinHistoryAdapter adapter;
	private ListView checkin_history_list;
	private TextView txt_checkin_tname,txt_oops;
	
	private List<CheckinHistoryController> checkinList=new ArrayList<CheckinHistoryController>() ;
	//private Map<String ,List<CheckinHistoryController>> child_list;
	private List<String> groupIds = new ArrayList<>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.activity_ckeckin_history);
		if(!NullChecker(this.getIntent().getStringExtra(Util.INTENT_KEY_2)).isEmpty()){
			groupIds.add(this.getIntent().getStringExtra(Util.INTENT_KEY_2));
		}else{
          for (SessionGroup group: Util.db.getGroupList(checked_in_eventId)){
			  groupIds.add(group.Id);
		  }
		}

		if(isOnline()){
			doRequest();
		}
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});
	}

	protected void onResume() {
		super.onResume();
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		} else {
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		String access_token = sfdcddetails.token_type+" "+ sfdcddetails.access_token;
		postMethod = new HttpPostData("Loading Checkin History...",setCheckinHistoryUrl(), null, access_token, CheckinHistory.this);
		postMethod.execute();
	}

	private String setCheckinHistoryUrl()
	{
		return sfdcddetails.instance_url+WebServiceUrls.SA_ATTENDEE_CHECKIN_HISTORY+"Ticket_Id="+getIntent().getStringExtra(Util.TICKET_ID)+"&freeSemPoolId="+getIntent().getStringExtra(Util.INTENT_KEY_1);
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		
		gson=new Gson();
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			CheckinHistoryController[] checkin_history = gson.fromJson(response, CheckinHistoryController[].class);
			if (checkin_history.length > 0) {
				for (CheckinHistoryController data : checkin_history) {
					// ExpandClass listClass=new ExpandClass();
					if(!groupIds.contains(data.tstatus.BLN_Session_user__r.BLN_Group__r.Id)){
						continue;
					}
					checkinList.add(data);
				}

				Collections.sort(checkinList, new Comparator<CheckinHistoryController>() {

					@Override
					public int compare(CheckinHistoryController lhs, CheckinHistoryController rhs) {
						// TODO Auto-generated method stub
						try {
							Date d1 = Util.date_format_new_sec.parse(lhs.LastModifiedDate);
							Date d2 = Util.date_format_new_sec.parse(rhs.LastModifiedDate);
							return d2.compareTo(d1);
						} catch (Exception e) {
							AppUtils.displayLog("--------------Exception----------",":"+e.getMessage());
						}
						
						return 0;
					}
				});
				adapter = new CheckinHistoryAdapter();
				checkin_history_list.setAdapter(adapter);
			} else {
				txt_oops.setVisibility(View.VISIBLE);
			}
		}catch(Exception e){
			e.printStackTrace();
			startErrorAnimation(getResources().getString(R.string.connection_error),txt_error_msg);
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
		activity = this;
		v=inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Check-In History");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		txt_checkin_tname=(TextView) findViewById(R.id.txt_checkin_tname);
		txt_oops=(TextView) findViewById(R.id.txt_oops);
		txt_checkin_tname.setText(getIntent().getStringExtra(Util.ATTENDEE_NAME));
		checkin_history_list=(ListView) findViewById(R.id.lis_view_checkin_history);

	}


	private class CheckinHistoryAdapter extends BaseAdapter
	{
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return checkinList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return checkinList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View arg1, ViewGroup arg2) {
			View v = inflater.inflate(R.layout.checkin_history_list_item, null);

			TextView txt_checkin_history_time=(TextView) v.findViewById(R.id.txt_checkin_history_time);
			TextView txt_checkin_history_status=(TextView) v.findViewById(R.id.txt_checkin_history_status);
			TextView txt_checkin_history_scanby=(TextView) v.findViewById(R.id.txt_checkin_history_scanby);
			TextView txt_checkin_history_session = (TextView)v.findViewById(R.id.txt_checkin_history_session);
			////Log.i("----Checkin History---", "---Group Name---"+group_list.get(groupposition));
			
			if(checkinList.get(position).gnUser!=null){
				txt_checkin_history_scanby.setText(checkinList.get(position).gnUser.First_Name__c+" "+checkinList.get(position).gnUser.Last_Name__c);
			}
			if(checkinList.get(position).Tstatus_name.equalsIgnoreCase("true"))
			{
				//txt_checkin_history_time.setText(checkinList.get(position).LastModifiedDate);
				txt_checkin_history_status.setText("Check In");
				txt_checkin_history_status.setTextColor(getResources().getColor(R.color.green_button_color));
			}else{
				//txt_checkin_history_time.setText(checkinList.get(position).LastModifiedDate);
				txt_checkin_history_status.setText("Check Out");
				txt_checkin_history_status.setTextColor(getResources().getColor(R.color.orange_bg));
			}
			
			txt_checkin_history_session.setText(checkinList.get(position).tstatus.BLN_Session_user__r.BLN_Group__r.Name);
			
			
			try {
				String gmt_time = Util.new_db_date_format.format(Util.date_format_new_sec.parse(checkinList.get(position).LastModifiedDate));
				//Log.i("-----------------GMT Time---------------",":"+gmt_time);
				txt_checkin_history_time.setText(Util.change_US_ONLY_DateFormatWithSEC(gmt_time, checkedin_event_record.Events.Time_Zone__c));
				//Log.i("-----------------Original Time---------------",":"+Util.change_US_ONLY_DateFormatWithSEC(gmt_time, checkedin_event_record.Events.Time_Zone__c));
			} catch (Exception e) {
				// TODO: handle exception
			}
			return v;
		}

		

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==Util.DASHBORD_ONACTIVITY_REQ_CODE){
			Intent i=new Intent(CheckinHistory.this,SplashActivity.class);
			startActivity(i);
			finish();
		}
	}

}
