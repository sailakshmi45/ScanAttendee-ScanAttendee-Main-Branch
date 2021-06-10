//  ScanAttendee Android
//  Created by Ajay
//  This class is used to change sales tax value which will applied on all items and package
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.classes.MultiDirectionSlidingDrawer;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.Util;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SalesTaxActivity extends BaseActivity implements OnClickListener{
	TextView txt_salestax, txt_value, txt_note;
	Switch s_ticket, s_item;
    ImageView img_edit;
    MultiDirectionSlidingDrawer slider;
    NumberPicker n1, n2, n3, n4;
    Button btn_cancel, btn_done;
    String value1="0", value2="0", value3="0", value4="0";
    boolean ticketChecked, itemChecked;
    
	String server_start_date="", server_end_date="", salex_tax="00.00";
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCustomContentView(R.layout.sales_tax_layout);

		
		
		s_ticket.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 
			   @Override
			   public void onCheckedChanged(CompoundButton buttonView,
			     boolean isChecked) {
			 
				   ticketChecked = isChecked;
			 
			   }
			  });
		s_item.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 
			   @Override
			   public void onCheckedChanged(CompoundButton buttonView,
			     boolean isChecked) {
			 
				   itemChecked = isChecked;
			 
			   }
			  });

	}
	@Override
	protected void onResume() {
		super.onResume();
		
	}
	@Override 
	public void doRequest() {
		
	/*	postMethod = new HttpPostData1(setEventUrl(), null, null, "Updating sales tax, please wait...", image, 
				SalesTaxActivity.this, true);*/
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		postMethod = new HttpPostData("", setEventUrl(),null,access_token, SalesTaxActivity.this);
		postMethod.execute();
	}
	private String setEventUrl(){
		String url="";
		try {
			Date varDate = Util.db_server_format.parse(checkedin_event_record.Events.Start_Date__c);
			Date e_varDate = Util.db_server_format.parse(checkedin_event_record.Events.End_Date__c);
			Util.server_dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
			server_start_date = Util.server_dateFormat.format(varDate);
			server_end_date = Util.server_dateFormat.format(e_varDate);
		}catch (ParseException e) {
			e.printStackTrace();
		}
		url = sfdcddetails.instance_url+WebServiceUrls.SA_SET_EVENT_SALESTAX+"SalesTax="+salex_tax
				+"&isTaxApplicable="+ticketChecked
				+"&EventId="+checkedin_event_record.Events.Id;
			
		return url;
	}
	@Override
	public void parseJsonResponse(String response) {
		String msg="";
		try {
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			JSONObject res_object = new JSONObject(response);
			final String success = res_object.optString("success");
			final String error = res_object.optString("msg");
			if(error.equals("null")){
				Util.db.updateTicketSalesTax(checked_in_eventId, salex_tax,String.valueOf(ticketChecked));
				Toast.makeText(SalesTaxActivity.this, success, Toast.LENGTH_LONG);
				finish();
			}else {
				AlertDialogCustom errormsg=new AlertDialogCustom(SalesTaxActivity.this);
				if(res_object.optString("errror").isEmpty())
					msg="Server is not responding, please try again later.";
				else
					msg=res_object.optString("errror");
				errormsg.setParamenters("Alert", msg, null, null, 1, false);
				errormsg.show();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Sales Tax");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		event_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		txt_save.setVisibility(View.VISIBLE);
		txt_salestax = (TextView) linearview.findViewById(R.id.txt_salestax);
		txt_value = (TextView) linearview.findViewById(R.id.txt_sel_value);
		txt_note = (TextView) linearview.findViewById(R.id.txtnote);
		s_ticket = (Switch) linearview.findViewById(R.id.switch_tickets);
		s_item = (Switch) linearview.findViewById(R.id.switch_items);
		img_edit  = (ImageView) linearview.findViewById(R.id.img_edit);
		s_ticket.setTypeface(Util.roboto_regular);
		s_item.setTypeface(Util.roboto_regular);
		txt_salestax.setTypeface(Util.roboto_regular);
		txt_value.setTypeface(Util.roboto_regular);
		txt_note.setTypeface(Util.roboto_regular);
		slider = (MultiDirectionSlidingDrawer) linearview.findViewById(R.id.salestaxwheel);
		n1 = (NumberPicker)slider.findViewById(R.id.numberPicker1);
		n2 = (NumberPicker)slider.findViewById(R.id.numberPicker2);
		n3 = (NumberPicker)slider.findViewById(R.id.numberPicker3);
		n4 = (NumberPicker)slider.findViewById(R.id.numberPicker4);
		btn_cancel = (Button)slider.findViewById(R.id.taxcancel);
		btn_done = (Button)slider.findViewById(R.id.taxdone);
		btn_done.setTypeface(Util.roboto_regular);
		btn_cancel.setTypeface(Util.roboto_regular);
		btn_done.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		img_edit.setOnClickListener(this);
		txt_save.setOnClickListener(this);
		back_layout.setOnClickListener(this);
		n1.setMaxValue(9);
		n1.setMinValue(0);
        n1.setWrapSelectorWheel(false);
		n2.setMaxValue(9);
		n2.setMinValue(0);
		n2.setWrapSelectorWheel(false);
		n3.setMaxValue(9);
		n3.setMinValue(0);
		n3.setWrapSelectorWheel(false);
		n4.setMaxValue(9);
		n4.setMinValue(0);
		n4.setWrapSelectorWheel(false);
		if(!salex_tax.isEmpty())
		salex_tax = checkedin_event_record.Events.Tax_Rate__c;
		txt_value.setText("Your Selected Sales Tax is: "+salex_tax+"%");
		ticketChecked=Boolean.valueOf(checkedin_event_record.Events.Accept_Tax_Rate__c);
		s_ticket.setChecked(ticketChecked);
		n1.setOnValueChangedListener(new OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				
				value1 = String.valueOf(newVal);
			}
		});
		n2.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				
				value2 = String.valueOf(newVal);
			}
		});
		n3.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				
				value3 = String.valueOf(newVal);
			}
		});
		n4.setOnValueChangedListener(new OnValueChangeListener() {

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				
				value4 = String.valueOf(newVal);
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v == img_edit){
			 if (slider.isOpened()) 
	          	 slider.animateClose();
			 else 
				 slider.animateOpen();
					
		}else if(v == btn_cancel){
			 slider.animateClose();
		}else if(v == btn_done){
			 slider.animateClose();
			 salex_tax = value1+value2+"."+value3+value4;
			 txt_value.setText("Your Selected Sales Tax is: "+salex_tax+"%");
		}else if(v == txt_save){
			if(salex_tax.isEmpty())
				salex_tax="0";
			if(Double.parseDouble(salex_tax)>0){
			if(isOnline())
				doRequest();
			else
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);;
			}else{
				AlertDialogCustom dialog=new AlertDialogCustom(SalesTaxActivity.this);
				dialog.setParamenters("Alert", "Sales tax should be greater then zero.", null, null, 1, false);
				dialog.show();
			}
		}else if(v == back_layout){
			finish();
		}
	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		
	}

}
