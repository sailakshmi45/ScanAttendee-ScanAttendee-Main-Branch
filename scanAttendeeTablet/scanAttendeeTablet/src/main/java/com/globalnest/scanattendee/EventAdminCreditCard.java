//  ScanAttendee Android
//  Created by Ajay on Jun 16, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.scanattendee;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.globalnest.classes.EditCard;
import com.globalnest.database.EventPayamentSettings;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.Event;
import com.globalnest.objects.Event.Organaizer;
import com.globalnest.objects.PaymentType;
import com.globalnest.stripe.android.model.Card;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.card.payment.CardIOActivity;
import io.card.payment.CardType;
import io.card.payment.CreditCard;


/**
 * @author laxmanamurthy
 *
 */
public class EventAdminCreditCard extends BaseActivity{

	private String REQUEST_TYPE;
	public static final int ACTIVITY_CODE = 2018;
	public static final String CUSTOMER_KEY = "customer_key";
	private Event.Organaizer organaizer;
	private EditText edt_cvv;
	private EditCard edt_cardnumber;
	private Spinner spnr_exp_month,spnr_exp_year;
	private Button btn_credit_authorize;
	private Animation shake;
	private  String card_num="",cvv="",month="",year="",card_token="";
	private TextView txt_cardtype,txt_cardname;
	private Button btn_edit_card,btnscancard;
	private LinearLayout layout_payinfo,layout_creditcard;
	private int keyDel;
	private String a = "";
	private PaymentType pay_key;
	private Card card;
	private static final int REQUEST_SCAN = 1110;

	private String scanCardNumber="",scanCCV="";
	private int scanExpMonth=0,scanExpYear=0;
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.credit_card_layout);
		Util.setCustomAlertDialog(EventAdminCreditCard.this);
		Util.txt_alert_title.setText("CreditCard Update");
		Util.txt_alert_msg.setText("Your card has been Authorized before, would you like to Change your Card.");
		Util.txt_dismiss.setVisibility(View.VISIBLE);

		organaizer = (Organaizer) this.getIntent().getSerializableExtra(PaymentSetting.KEYS);
		txt_title.setText("Payment options for Eventdex Fee.");
		pay_key = Util.db.getPay_Gateway_Key(" where "+EventPayamentSettings.KEY_PAYGATEWAY_NAME+" = '"+getString(R.string.eventdex_stripe_keys)+"'").get(0);
		viewCreditCard(organaizer);
		shake = AnimationUtils.loadAnimation(EventAdminCreditCard.this,	R.anim.shake);
		btn_credit_authorize.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				card_num = edt_cardnumber.getCardNumber().trim();
				cvv = edt_cvv.getText().toString().trim();
				month = (String)spnr_exp_month.getSelectedItem();
				year = (String)spnr_exp_year.getSelectedItem();

				if(month.equals("MM"))
					month="0";
				if(year.equals("YYYY"))
					year="0";


				card = new Card(card_num, Integer.valueOf(month), Integer.valueOf(year), cvv, "");

				if (card_num.isEmpty() || !card.validateNumber()) {
					edt_cardnumber.setError(getResources().getString(R.string.cardnumalert));
					edt_cardnumber.requestFocus();
					edt_cardnumber.startAnimation(shake);
				} else if (month.equals("0") || !card.validateExpMonth()) {
					spnr_exp_month.startAnimation(shake);
				} else if (year.equals("0") || !card.validateExpYear()) {
					spnr_exp_year.startAnimation(shake);
				} else if (cvv.length() < 3 || !card.validateCVC()) {
					edt_cvv.setError(getResources().getString(R.string.cvvalert));
					edt_cvv.requestFocus();
					edt_cvv.startAnimation(shake);
				}else if(!card.validateCard()){
					startErrorAnimation("Please Enter Valid Card Details.", txt_error_msg);
				}else{
					if(isOnline()){
						REQUEST_TYPE = WebServiceUrls.STRIPE_TOKENT_URL;
						doRequest();
					}else{
						startErrorAnimation(getResources().getString(R.string.network_error),txt_error_msg);
					}
				}
			}
		});
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Util.alert_dialog.dismiss();
				layout_creditcard.setVisibility(View.VISIBLE);
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Util.alert_dialog.dismiss();
			}
		});

		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				finish();
			}
		});
		btn_edit_card.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!layout_creditcard.isShown()){
					Util.alert_dialog.show();
				}else{
					layout_creditcard.setVisibility(View.GONE);
				}
			}
		});

		btnscancard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onScan();
			}
		});
		/*edt_cardnumber.addTextChangedListener(new TextWatcher() {

			// private static final char space = ' ';
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				boolean flag = true;
				String eachBlock[] = edt_cardnumber.getText().toString()
						.split(" ");
				for (int i = 0; i < eachBlock.length; i++) {
					if (eachBlock[i].length() > 4) {
						flag = false;
					}
				}
				if (flag) {

					edt_cardnumber.setOnKeyListener(new OnKeyListener() {

						@Override
						public boolean onKey(View v, int keyCode, KeyEvent event) {

							if (keyCode == KeyEvent.KEYCODE_DEL)
								keyDel = 1;
							return false;
						}
					});

					if (keyDel == 0) {

						if (((edt_cardnumber.getText().length() + 1) % 5) == 0) {

							if (edt_cardnumber.getText().toString().split(" ").length <= 3) {
								edt_cardnumber.setText(edt_cardnumber.getText()
										+ " ");
								edt_cardnumber.setSelection(edt_cardnumber
										.getText().length());
							}
						}
						a = edt_cardnumber.getText().toString();
					} else {
						a = edt_cardnumber.getText().toString();
						keyDel = 0;
					}

				} else {
					edt_cardnumber.setText(a);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});*/
	}
	protected void onResume() {
		super.onResume();

	}
	/* (non-Javadoc)
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		// TODO Auto-generated method stub
		activity = this;
		v = inflater.inflate(layout, null);
		linearview.addView(v);
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);
		edt_cardnumber = (EditCard) linearview.findViewById(R.id.editcardnum);
		edt_cvv = (EditText)linearview.findViewById(R.id.editcvv);
		spnr_exp_month = (Spinner)linearview.findViewById(R.id.spnrexpmonth);
		spnr_exp_year = (Spinner)linearview.findViewById(R.id.spnrexpyear);
		btn_credit_authorize = (Button)linearview.findViewById(R.id.btncreditpay);
		btn_credit_authorize.setText("Authorize Card");
		btn_edit_card = (Button)linearview.findViewById(R.id.btn_edit_card);
		btnscancard=(Button) v.findViewById(R.id.btnscancard);
		layout_creditcard = (LinearLayout)linearview.findViewById(R.id.layout_credicard_view);
		layout_payinfo = (LinearLayout)linearview.findViewById(R.id.event_admin_payinfo);
		txt_cardname = (TextView)linearview.findViewById(R.id.txt_cardname);
		txt_cardtype = (TextView)linearview.findViewById(R.id.txt_card_type);
		btn_credit_authorize.setTypeface(Util.roboto_regular);
		edt_cardnumber.setTypeface(Util.roboto_regular);
		edt_cvv.setTypeface(Util.roboto_regular);

	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub
		Card c = new Card(card_num,Integer.parseInt(month), Integer.parseInt(year), cvv,"");
		//Log.i("-----------------Card Validation--------------",":"+c.validateCard());
		String access_token = sfdcddetails.token_type+" "+pay_key.PG_Pass_Secret__c;
		if(REQUEST_TYPE == WebServiceUrls.STRIPE_TOKENT_URL && c.validateCard()){
			postMethod = new HttpPostData("",WebServiceUrls.STRIPE_TOKENT_URL, AppUtils.getQuery(getCardValues()), access_token, this);
			postMethod.execute();
		}else if(REQUEST_TYPE == WebServiceUrls.STRIPE_TOKENT_URL && !c.validateCard()){
			//Util.openCustomDialog("Error", "Enter Valid Card Details.");
			AlertDialogCustom dialog=new AlertDialogCustom(EventAdminCreditCard.this);
			dialog.setParamenters("Error","Enter Valid Card Details.", null, null, 1, true);
			dialog.setAlertImage(R.drawable.alert_error, "error");
			dialog.show();
		}else if(REQUEST_TYPE == WebServiceUrls.STRIPE_CUSTOMER_TOKEN){
			postMethod = new HttpPostData("",WebServiceUrls.STRIPE_CUSTOMER_TOKEN,AppUtils.getQuery(getCustomerValues()), access_token, this);
			postMethod.execute();
		}

	}

	private List<NameValuePair> getCardValues(){
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("card[number]", card_num));
		values.add(new BasicNameValuePair("card[exp_month]", month));
		values.add(new BasicNameValuePair("card[exp_year]", year));
		values.add(new BasicNameValuePair("card[cvc]", cvv));
		return values;
	}

	private List<NameValuePair> getCustomerValues(){
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("source", card_token));
		values.add(new BasicNameValuePair("description", ""));
		return values;
	}
	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub
		//Log.i("-------------------CardToken Rsponse-----------",":"+response);

		try {
			/*if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}*/
			if(REQUEST_TYPE == WebServiceUrls.STRIPE_TOKENT_URL){
				JSONObject res_obj = new JSONObject(response);
				if(!res_obj.has("error")){
					card_token = res_obj.optString("id");
					REQUEST_TYPE = WebServiceUrls.STRIPE_CUSTOMER_TOKEN;
					doRequest();
				}else{
					AlertDialogCustom dialog=new AlertDialogCustom(EventAdminCreditCard.this);
					dialog.setParamenters("Alert", res_obj.optJSONObject("error").optString("message"), null	, null, 1, false);
					dialog.show();
				}

			}else if(REQUEST_TYPE == WebServiceUrls.STRIPE_CUSTOMER_TOKEN){
				JSONObject res_obj = new JSONObject(response);
				JSONObject error_obj=res_obj.optJSONObject("error");
				//Log.i("--------------Error String-------------",":"+error_obj.toString());
				if(error_obj==null){
					JSONObject card_obj = res_obj.optJSONObject("sources");
					//JSONObject card_obj = res_obj.optJSONObject("cards");
					JSONArray data_obj = card_obj.optJSONArray("data");
					//Log.i("---------Data Object-----------",":"+data_obj.length());
					if(data_obj.length() > 0){
						JSONObject obj = data_obj.optJSONObject(0);
						organaizer.PG_Authorization_CC_Last_four_Digit__c = obj.optString("last4");
						organaizer.PG_Authorization_Key__c = obj.optString("customer");
						organaizer.PG_Authorization_Card_Type__c = obj.optString("brand");
						sendResponse();
					}
				}else{
					AlertDialogCustom dialog=new AlertDialogCustom(EventAdminCreditCard.this);
					dialog.setParamenters("Alert", error_obj.optString("message"), null	, null, 1, false);
					dialog.show();
				}
			}


		} catch (Exception e) {
			AppUtils.displayLog("--------------Credit Card Exception-------------",":"+e.getMessage());
			startErrorAnimation(e.getMessage(), txt_error_msg);
		}


	}

	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub

	}
	private void sendResponse(){
		Intent i = new Intent();
		i.putExtra(PaymentSetting.KEYS, organaizer);
		setResult(ACTIVITY_CODE, i);
		finish();
	}

	private void viewCreditCard(Event.Organaizer organaizer){
		if(organaizer.PG_Authorization_CC_Last_four_Digit__c != null && !TextUtils.isEmpty(organaizer.PG_Authorization_CC_Last_four_Digit__c)){
			layout_payinfo.setVisibility(View.VISIBLE);
			layout_creditcard.setVisibility(View.GONE);
			txt_cardname.setText("XXXXXXXXXXXX"+organaizer.PG_Authorization_CC_Last_four_Digit__c);
			txt_cardtype.setText(organaizer.PG_Authorization_Card_Type__c);

		}else{
			layout_creditcard.setVisibility(View.VISIBLE);
			layout_payinfo.setVisibility(View.GONE);
		}
	}
	public void onScan() {
		try {
			Intent intent = new Intent(this, CardIOActivity.class)
					/*//.putExtra(CardIOActivity.EXTRA_APP_TOKEN, Util.CARDIO_APP_TOKEN)
					.putExtra(CardIOActivity.EXTRA_NO_CAMERA, false)
					.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
					.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, true)
					.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
					.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false)
					.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false)
					.putExtra(CardIOActivity.EXTRA_LANGUAGE_OR_LOCALE, "en")
					.putExtra(CardIOActivity.EXTRA_GUIDE_COLOR, Color.GREEN)
					.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, false)
					.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, false);*/
			.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true) // default: false
			.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false) // default: false
			.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false); // default: false

			// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
			//startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
			try {
				//int unblurDigits = Integer.parseInt(mUnblurEdit.getText().toString());
			} catch (NumberFormatException ignored) {
			}

			startActivityForResult(intent, REQUEST_SCAN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == EventAdminCreditCard.REQUEST_SCAN  && data != null
				&& data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
			String outStr = new String();
			Bitmap cardTypeImage = null;
			CreditCard result = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
			if (result != null) {
				outStr += "Card number: " + result.getRedactedCardNumber() + "\n";
				scanCardNumber=SellOrderActivity.formatCardNumber("-",result.getFormattedCardNumber().replace(" ",""),4);
				scanExpMonth=result.expiryMonth;
				scanExpYear=result.expiryYear;
				scanCCV=result.cvv;

				edt_cardnumber.setText(scanCardNumber.toString());
				edt_cvv.setText(scanCCV);
				spnr_exp_month.setSelection(scanExpMonth);
				spnr_exp_year.setSelection(SellOrderActivity.getSelectedPosition(getResources().getStringArray(R.array.years),String.valueOf(scanExpYear)));

				//adapter.notifyDataSetChanged();

				CardType cardType = result.getCardType();
				cardTypeImage = cardType.imageBitmap(this);
				outStr += "Card type: " + cardType.name() + " cardType.getDisplayName(null)=" + cardType.getDisplayName(null) + "\n";
				outStr += "Card number: " + result.getFormattedCardNumber() + "\n";
				outStr += "Expiry: " + result.expiryMonth + "/" + result.expiryYear + "\n";
				outStr += "CVV: " + result.cvv + "\n";
				AppUtils.displayLog("Scan Card Resiult=",""+outStr);
			}
		}
	}
	public static String formatCardNumber(String t, String s, int num) {
		StringBuilder retVal;

		if (null == s || 0 >= num) {
			throw new IllegalArgumentException("Don't be silly");
		}

		if (s.length() <= num) {
			return s;
		}

		retVal = new StringBuilder(s);

		for(int i = retVal.length(); i > 0; i -= num){
			retVal.insert(i, t);
		}
		return retVal.toString();
	}
}
