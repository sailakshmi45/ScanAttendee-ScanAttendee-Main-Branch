//  ScanAttendee Android
//  Created by Ajay
//  This class used to change eventdex fee mode
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.Util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

@SuppressLint("NewApi")
public class SAFeeActivity extends BaseActivity implements OnClickListener{

	TextView txt_fee, txt_option1, txt_option2, txt_note;
	Switch s_include;
	boolean isSelected;
	
	String server_start_date="", server_end_date="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
		setCustomContentView(R.layout.app_fee_layout);
		
		
		s_include.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 
			   @Override
			   public void onCheckedChanged(CompoundButton buttonView,
			     boolean isChecked) {
			       //Log.i("----Include Fee---",":"+isChecked);
				   isSelected = isChecked;
				   if(isSelected){
					   txt_option1.setTextColor(getResources().getColor(R.color.black));
						txt_option2.setTextColor(getResources().getColor(R.color.textcolor)); 
				   }else{
					   txt_option1.setTextColor(getResources().getColor(R.color.textcolor));
					txt_option2.setTextColor(getResources().getColor(R.color.black));
				   }
					   
			 
			   }
			  });
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		/*postMethod = new HttpPostData1(setEventUrl(), null, null, "Updating fee, please wait...", image, 
				SAFeeActivity.this, true);*/
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		postMethod = new HttpPostData("",setEventUrl(), null, access_token,SAFeeActivity.this);
		postMethod.execute();
	}

	private String setEventUrl(){
		try {
			Date varDate = Util.server_dateFormat.parse(checkedin_event_record.Events.Start_Date__c);
			Date e_varDate = Util.server_dateFormat.parse(checkedin_event_record.Events.End_Date__c);
			Util.server_dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
			server_start_date = Util.server_dateFormat.format(varDate);
			server_end_date = Util.server_dateFormat.format(e_varDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String url = sfdcddetails.instance_url+WebServiceUrls.SA_ADD_EVENT
				+ "Event_Name="+ checkedin_event_record.Events.Name.replaceAll(" ", "%20")
				+ "&Start_Date="+ server_start_date
				+ "&STime="+ checkedin_event_record.Events.Start_Time__c.replaceAll(" ", "%20")
				+ "&End_Date="+ server_end_date
				+ "&ETime="+ checkedin_event_record.Events.End_Time__c.replaceAll(" ", "%20")
				+ "&MobileEvent_Id="+ checkedin_event_record.Events.mobileevent_id
				+ "&Event_Id="+ checkedin_event_record.Events.Id
				+ "&Address1="+ checkedin_event_record.Events.Street1__c.replaceAll(" ", "%20")
				+ "&Address2="+ ""
				+ "&Location="+ checkedin_event_record.Events.Venue_Name__c.replaceAll(" ", "%20")
				+ "&City="+ checkedin_event_record.Events.City__c.replaceAll(" ", "%20")
				+ "&State="+ checkedin_event_record.state.replaceAll(" ", "%20")
				+ "&Country="+checkedin_event_record.country.replaceAll(" ", "%20")
				+ "&Zipcode="+ checkedin_event_record.Events.ZipCode__c.replaceAll(" ", "%20")
				+ "&Desc="+ checkedin_event_record.Events.Description__c.replaceAll("\\s+", "%20") + 
				"&Status=EDIT" +
				"&SalesTax="+checkedin_event_record.Events.Tax_Rate__c
				+ "&FeeApplicable="+ isSelected
				+ "&OwnerEmailID="+ user_profile.Profile.Email__c;
		
		
		return url;
	}
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		//Log.i("--Response---",":"+response);
		
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			JSONObject obj = new JSONObject(response);
			boolean feeApplicable = obj.optBoolean("FeeApplicable");
	 		Util.db.updateScanAttendeeFee(checked_in_eventId, feeApplicable);
			_showCustomToast("Scan Attendee fee setting has been updated successfully.",Gravity.CENTER);
			finish();
		}catch(Exception e){
			e.printStackTrace();
			startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
		}
	}

	@Override
	public void setCustomContentView(int layout) {
		// TODO Auto-generated method stub
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Scan Attendee Fee");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE); 
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		txt_save.setVisibility(View.VISIBLE);
		txt_save.setOnClickListener(this);
		back_layout.setOnClickListener(this);
		txt_fee = (TextView) linearview.findViewById(R.id.txtappfee);
		txt_option1 = (TextView) linearview.findViewById(R.id.txtoption1);
		txt_option2 = (TextView) linearview.findViewById(R.id.txtoption2);
		txt_note = (TextView) linearview.findViewById(R.id.txtfeenote);
		s_include = (Switch)  linearview.findViewById(R.id.switch_include);
		
		txt_fee.setTypeface(Util.roboto_regular);
		txt_option2.setTypeface(Util.roboto_regular);
		txt_option1.setTypeface(Util.roboto_regular);
		txt_note.setTypeface(Util.roboto_regular);
		s_include.setTypeface(Util.roboto_regular);
		//Log.i("-----Fee Status---",":"+checkedin_event_record.Events.eventfeeapplicable);
		if(checkedin_event_record.Events.eventfeeapplicable.equals("true"))
			isSelected = true;
		else
			isSelected = false;
			
	
		if(isSelected){
			
			txt_option1.setTextColor(getResources().getColor(R.color.black));
			txt_option2.setTextColor(getResources().getColor(R.color.textcolor));
	
		}else{
			
			txt_option1.setTextColor(getResources().getColor(R.color.textcolor));
			txt_option2.setTextColor(getResources().getColor(R.color.black));
			
		}
		s_include.setChecked(isSelected);
		//Log.i("SAFeeActivity", "Tax Rate="+checkedin_event_record.Events.Tax_Rate__c);
		if(!Util.NullChecker(checkedin_event_record.Events.Tax_Rate__c).isEmpty())
		txt_fee.setText("Scan Attendee fee : "+String.valueOf(Util.nf.format(Double.valueOf(checkedin_event_record.Events.Tax_Rate__c))));
		else
		txt_fee.setText("Scan Attendee fee : "+"$00.00");
		txt_note.setText(Html.fromHtml("<font color='red'>Note : </font>"+"Scan Attendee Fee is applicable only on Stripe & Paypal transaction."));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == back_layout){
			finish();
		}else if(v == txt_save){
			if(isOnline())
				doRequest();
			else
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub
		
	}

}
