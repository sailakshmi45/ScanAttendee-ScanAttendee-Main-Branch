//  ScanAttendee Android
//  Created by Ajay
//  This class is used to manage stripe payment gateway profile either login and logout
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.globalnest.database.EventPayamentSettings;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.PaymentType;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;

public class StripeAccountActivity extends BaseActivity{

	public static final int STRIPE_CODE = 2016;
	public static final String STRIPE_RESPONSE = "Stripe_Response";

	Dialog progress_dialog;
	WebView webView;
	String client_id="",secret_key="", auth_code="";
	TextView txt_loading;
	PaymentType pay_keys;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Util.setCustomAlertDialog(this);
		setCustomContentView(R.layout.stripe_layout);

		progress_dialog = new Dialog(this);
		progress_dialog.setCanceledOnTouchOutside(false);
		progress_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progress_dialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		this.getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		progress_dialog.setContentView(R.layout.loading_layout);
		txt_loading = (TextView) progress_dialog.findViewById(R.id.txtloading);
		txt_loading.setTypeface(Util.roboto_regular);
		txt_loading.setText("Loading stripe...");
		progress_dialog.show();


		pay_keys =  Util.db.getPay_Gateway_Key(" where "+EventPayamentSettings.KEY_PAYGATEWAY_NAME+" = '"+getString(R.string.eventdex_stripe_keys)+"'").get(0);

		//secret_key = Util._getPreference(Util.login_prefer, Util.STRIPE_SECRET_KEY);
		//client_id = Util._getPreference(Util.login_prefer, Util.STRIPE_CLIENT_ID);
		//client_id="ca_38gideD49DrWpka18X0KiZH9G7BUcGnT";
		//Log.i("StripAccount", "Client id="+client_id);

		webView.setWebViewClient(new Callback());
		//Log.i("----------------Stripe Login--------------",":"+WebServiceUrls.STRIPE_LOGIN_URL+"&client_id="+pay_keys.PG_User_Key__c);
		webView.loadUrl(WebServiceUrls.STRIPE_LOGIN_URL+"&client_id="+pay_keys.PG_User_Key__c);

		Util.txt_okey.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//

				Util.alert_dialog.dismiss();
				finish();

			}
		});

		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 
				finish();
			}
		});

	}

	private class MyWebChrome extends WebChromeClient {
		Window window;
		public MyWebChrome(Window aWindow)
		{
			window = aWindow;
		}
		@Override
		public void onProgressChanged(WebView view, int newProgress)
		{

			//Log.i("----------------Inside WebChromeClient Class----------", ":");
			window.setFeatureInt(Window.FEATURE_PROGRESS, newProgress * 100);
		}

		@Override
		public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
			AlertDialog dialog = new AlertDialog.Builder(view.getContext()).setMessage(message).
					setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//do nothing                
						}
					}).create();
			dialog.show();
			result.confirm();
			return true;
		}
		@Override
		public void onCloseWindow(WebView window){
			window.clearView();
		}

	}

	private class Callback extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {

			//Log.i("----------------Stripe redirect UrL----------", ":" + url);
			if(url.contains("code=ac")){
				auth_code=url.substring(url.indexOf("code=")+5,url.length());
				//Log.i("-------Authorize COde Value----------",":"+auth_code);
				//webView.removeAllViews();
				/*CookieManager mCookieManager = CookieManager.getInstance();
				CookieSyncManager mCookieSyncManager = CookieSyncManager.createInstance(getApplicationContext());
				clearCookieByUrl(url, mCookieManager, mCookieSyncManager);*/
				//new GetStripeAccount().execute();
				doRequest();
			}else{
				view.loadUrl(url);

			}


			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			progress_dialog.dismiss();

			// do what you want to do
			//Log.i("---------onPageFinished method called---------",":");

		}
	}

	@Override
	public void doRequest() {
		postMethod = new HttpPostData("Stripe Logging...", WebServiceUrls.STRIPE_AUTHORIZATION_URL, getValues(), null, this);
		postMethod.execute();
	}

	private String getValues(){
		ArrayList<NameValuePair> values = new ArrayList<NameValuePair>();
		values.add(new BasicNameValuePair("grant_type", "authorization_code"));
		values.add(new BasicNameValuePair("client_secret", pay_keys.PG_Pass_Secret__c));
		values.add(new BasicNameValuePair("code", auth_code));
		return AppUtils.getQuery(values);
	}

	@Override
	public void parseJsonResponse(String response) {

		try {
			JSONObject stripe = new JSONObject(response);

			//Log.i(stripe.getString("stripe_publishable_key")+ "--------------ACCESS_TOEKN----------",":" + stripe.getString("access_token"));
			//Log.i(stripe.getString("stripe_publishable_key")+ "--------------REFRESH TOKEN----------",":" + stripe.getString("stripe_publishable_key"));
			//Log.i(stripe.getString("refresh_token")+ "--------------REFRESH TOKEN----------",":" + stripe.getString("refresh_token"));
			if(isValidResponse(response)) {
				Util._savePreference(Util.eventPrefer, Util.STRIPE_ACCESS_TOKEN, stripe.getString("access_token"));
				Util._savePreference(Util.eventPrefer, Util.STRIPE_PUB_KEY, stripe.getString("stripe_publishable_key"));
				Util._savePreference(Util.eventPrefer, Util.STRIPE_REFRESH_TOKEN, stripe.getString("refresh_token"));
				Util._savePreference(Util.login_prefer, Util.STRIPE_LOGIN, true);
				sendResponse(response);
			}else{

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
		txt_title.setText("Stripe");
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_button);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		event_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.GONE);
		event_layout.setVisibility(View.VISIBLE);

		webView = (WebView) linearview.findViewById(R.id.stripelogin);
		webView.getSettings().setJavaScriptEnabled(true);

	}


	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// 

	}

	private void sendResponse(String  message){
		Intent result = new Intent();
		result.putExtra(STRIPE_RESPONSE, message);
		setResult(STRIPE_CODE, result);
		finish();
	}
}
