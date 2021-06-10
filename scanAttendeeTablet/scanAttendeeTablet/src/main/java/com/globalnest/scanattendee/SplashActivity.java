//  ScanAttendee Android
//  This class is used to do one time background task basically or refresh the access token, followed by login view
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.globalnest.network.Connectivity;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.SafeAsyncTask;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.stripe.android.compat.AsyncTask;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.SFDCDetails;
import com.globalnest.utils.Util;
import com.globalnest.LoginActivity;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;

import static com.globalnest.scanattendee.BaseActivity.DeleteFiles;
import static com.globalnest.scanattendee.BaseActivity.errorMessage;
import static com.globalnest.scanattendee.BaseActivity.isLogoutclicked;
import static java.net.HttpURLConnection.HTTP_OK;

@SuppressLint("NewApi")
public class SplashActivity extends Activity {
	CountDownTimer timer;
	ProgressBar loading;
	TextView txt_error;
	RelativeLayout splash_layout;
	Animation startAnimation;
	SFDCDetails sfdc_details;
	Gson gson = new Gson();
	String appversion = "";
	Boolean islogout=true;
	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
			islogout = isLogoutclicked;

		setContentView(R.layout.splash_layout);
		Util.setFontStype(this);
		if(AppUtils.isLogEnabled){
			try {
				BaseActivity.backupDatabase();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		BaseActivity.initializeDB(this);
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
//Log.i("Eventdex Version Code--------",":"+pInfo.versionCode);
		appversion = pInfo.versionName;

		AppUtils.isDevPref = getSharedPreferences(AppUtils.IS_DEV_PREF, MODE_PRIVATE);
		if (android.os.Build.VERSION.SDK_INT >= 11) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);

		}

		Util.eventPrefer = getSharedPreferences(Util.eventpref, MODE_PRIVATE);
		Util.login_prefer = getSharedPreferences(Util.loginpref, MODE_PRIVATE);
		sfdc_details = Util.db.getSFDCDDETAILS();

		txt_error = (TextView) findViewById(R.id.baseerrormsg);
		splash_layout = (RelativeLayout) findViewById(R.id.splashlayout);
		loading = (ProgressBar)findViewById(R.id.progress_bar);
		//splash_layout.addView(new MovingBackGround(this));

		//loading.getIndeterminateDrawable().setColorFilter( getResources().getColor(R.color.green_button_color), android.graphics.PorterDuff.Mode.MULTIPLY);
		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over

				//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
				if(!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()&&islogout){
					//if (!isOnline1()) {
					if(isOnline()){
						new GetAccessToken().execute();
						//	new GetAppVersion().execute();
					}else{
						startActivity(new Intent(SplashActivity.this,EventListActivity.class));
						finish();
					}

				} else {
					isLogoutclicked=true;
					Intent i = new Intent(SplashActivity.this,LoginActivity.class);
					i.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
					//i.putExtra(AppUtils.REVOKE_KEY, SplashActivity.this.getIntent().getBooleanExtra(AppUtils.REVOKE_KEY, false));
					i.putExtra(AppUtils.ACCESS_TOKEN, AppUtils.NullChecker(SplashActivity.this.getIntent().getStringExtra(AppUtils.ACCESS_TOKEN)));
					startActivity(i);
					//startActivityForResult(i,Util.DASHBORD_ONACTIVITY_REQ_CODE);
					finish();
				}
			}
		}, 3000);

		startAnimation = AnimationUtils.loadAnimation(this, R.anim.translate);
		startAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationEnd(Animation arg0) {

			}
		});

	}
	public boolean isOnline1() {
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()){
			try{
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpConnectionParams.setSoTimeout(httpParameters, 3000);
				HttpClient _httpclient = new DefaultHttpClient(httpParameters);
				HttpGet _httpget = new HttpGet("http://www.google.com");
				HttpResponse _response = _httpclient.execute(_httpget);
				int _responsecode =_response.getStatusLine().getStatusCode();

				if(_responsecode == 200){
					//visibleOffLineMode(false);
					return new Boolean(true);
				}else{
					//visibleOffLineMode(true);
				}
			}catch(MalformedURLException e1){
				e1.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		return false;
	}

	private boolean isOnline() {
	/*	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected())
			return true;

		return false;*/
		if(Connectivity.isConnectedFast(SplashActivity.this)){//&&BaseActivity.getOfflinemode()
			return true;
		}
		return false;
	}
	public void alertStoreDialog(String string) {
		try {
			//AlertDialog.Builder d1;
			/*if (Build.VERSION.SDK_INT >= 14) {
				d1 = new AlertDialog.Builder(SplashActivity.this,R.style.CustomForDialog);
			} else {*/
			Util.setCustomAlertDialog(SplashActivity.this);
			Util.openCustomDialog("Alert", string);
			Util.txt_okey.setText("Update");
			Util.alert_dialog.setCancelable(false);
			Util.txt_dismiss.setText("Cancel");
			Util.txt_okey.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					try {
						Util.alert_dialog.dismiss();
						Intent helpintent = new Intent(Intent.ACTION_VIEW);
						helpintent.setData(Uri.parse(WebServiceUrls.PLAY_STORE_URL));
						startActivity(helpintent);
						finish();
					} catch (Exception e) {
						Util.alert_dialog.dismiss();
						e.printStackTrace();
					}
				}
			});
			Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Util.alert_dialog.dismiss();
						//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
						if (!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()) {
							startActivity(new Intent(SplashActivity.this,EventListActivity.class));
							finish();
						}else {
							startActivity(new Intent(SplashActivity.this, LoginActivity.class));
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private class GetAccessToken extends SafeAsyncTask<String>{

		protected void onPreExecute() throws Exception{
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);
		}

		/* (non-Javadoc)
		 * @see java.util.concurrent.Callable#call()
		 */
		@Override
		public String call() throws Exception {
            if(!sfdc_details.refresh_token.trim().isEmpty())
			return getAccessTokenString(sfdc_details.refresh_token);
            else if(!Util._getPreference(Util.login_prefer,Util.REFRESH_TOKEN).isEmpty()) {
                return getAccessTokenString(Util._getPreference(Util.login_prefer,Util.REFRESH_TOKEN));
            }else {
				return getAccessTokenString(sfdc_details.refresh_token);
			}
		}

		protected void onSuccess(String result )throws Exception{
			super.onSuccess(result);
			loading.setVisibility(View.GONE);
			//Log.i("-------------Response------------",":"+result+" : "+BaseActivity.isValidResponse(result));
			try {

				if(!BaseActivity.NullChecker(result).isEmpty()&&BaseActivity.NullChecker(result).contains("error")){
                    openSessionExpireAlert(errorMessage(result));

                }else if(BaseActivity.NullChecker(result).isEmpty()){
					//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
					if (!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()) {
						new GetAppVersion().execute();
						/*startActivity(new Intent(SplashActivity.this,EventListActivity.class));
						finish();*/
					}else{
						txt_error.setVisibility(View.VISIBLE);
						txt_error.setText(getResources().getString(R.string.network_error));
						txt_error.startAnimation(startAnimation);
					}

				}else if(!BaseActivity.isValidResponse(result)){
					Util.db.isDBDeleted();
					DeleteFiles();
					//Util.clearSharedPreference(Util.login_prefer); For Update
					Util.clearSharedPreference(Util.eventPrefer);
					Util.clearSharedPreference(Util.first_login_pref);
					Util.clearSharedPreference(Util.selected_session_attedee_pref);
					Util.clearSharedPreference(Util.offset_pref);
					Util.clearSharedPreference(Util.dashboard_data_pref);
					Util.clearSharedPreference(Util.socket_device_pref);
					Util.clearSharedPreference(Util.external_setting_pref);
					//Util.mApi = null;
					CookieManager.getInstance().removeAllCookie();
					Intent login = new Intent(SplashActivity.this, LoginActivity.class);
					login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
					login.putExtra(AppUtils.REVOKE_KEY, false);
					login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
					startActivity(login);
					finish();
					return;
				}else{
					SFDCDetails current_sfdc= gson.fromJson(result, SFDCDetails.class);
					sfdc_details.access_token = current_sfdc.access_token;
                    JSONObject obj = new JSONObject(result);
                    String refresh_token = obj.optString("refresh_token");
                    String access_token = obj.optString("access_token");
                    String instance_url = obj.optString("instance_url");
                    String token_type = obj.optString("token_type");
                    String[] user_id_params = obj.optString("id").split("/");
                    String user_id = user_id_params[user_id_params.length - 1];

                    SFDCDetails sfdc = new SFDCDetails();
                    sfdc.user_id = user_id;
                    sfdc.instance_url = instance_url+"/services/apexrest/";
                    if(!refresh_token.isEmpty()) {
						sfdc.refresh_token = refresh_token;
						Util._savePreference(Util.login_prefer, Util.REFRESH_TOKEN, sfdc.refresh_token);
					}
                    else if(!Util._getPreference(Util.login_prefer,Util.REFRESH_TOKEN).isEmpty()) {
						sfdc.refresh_token = Util._getPreference(Util.login_prefer, Util.REFRESH_TOKEN);
					}
                    sfdc.access_token = access_token;
                    sfdc.token_type=token_type;
                    Util.db.InsertAndUpdateSFDCDDETAILS(sfdc);

					//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
					if (!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()) {
						new GetAppVersion().execute();
						/*startActivity(new Intent(SplashActivity.this,EventListActivity.class));
						finish();*/
					}else{
						/*txt_error.setVisibility(View.VISIBLE);
						txt_error.setText(getResources().getString(R.string.network_error));
						txt_error.startAnimation(startAnimation);*/

						Util.db.isDBDeleted();
						DeleteFiles();
						//Util.clearSharedPreference(Util.login_prefer);  For Update
						Util.clearSharedPreference(Util.eventPrefer);
						Util.clearSharedPreference(Util.first_login_pref);
						Util.clearSharedPreference(Util.selected_session_attedee_pref);
						Util.clearSharedPreference(Util.offset_pref);
						Util.clearSharedPreference(Util.dashboard_data_pref);
						Util.clearSharedPreference(Util.socket_device_pref);
						Util.clearSharedPreference(Util.external_setting_pref);
						//Util.mApi = null;
						CookieManager.getInstance().removeAllCookie();
                        isLogoutclicked=true;
						Intent login = new Intent(SplashActivity.this, LoginActivity.class);
						login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
						login.putExtra(AppUtils.REVOKE_KEY, false);
						login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
						startActivity(login);
						finish();
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				if (!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()) {
					//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
					startActivity(new Intent(SplashActivity.this,EventListActivity.class));
					finish();
				}else{
					/*txt_error.setVisibility(View.VISIBLE);
					txt_error.setText(getResources().getString(R.string.network_error));
					txt_error.startAnimation(startAnimation);*/

					Util.db.isDBDeleted();
					DeleteFiles();
					//Util.clearSharedPreference(Util.login_prefer);//For Update
					Util.clearSharedPreference(Util.eventPrefer);
					Util.clearSharedPreference(Util.first_login_pref);
					Util.clearSharedPreference(Util.selected_session_attedee_pref);
					Util.clearSharedPreference(Util.offset_pref);
					Util.clearSharedPreference(Util.dashboard_data_pref);
					Util.clearSharedPreference(Util.socket_device_pref);
					Util.clearSharedPreference(Util.external_setting_pref);
					//Util.mApi = null;
					CookieManager.getInstance().removeAllCookie();

					Intent login = new Intent(SplashActivity.this, LoginActivity.class);
					login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
					login.putExtra(AppUtils.REVOKE_KEY, false);
					login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
					startActivity(login);
					finish();
				}

			}

		}
/*
		protected void onPostExecute(String result){
			super.onPostExecute(result);
			try {

				if(BaseActivity.NullChecker(result).isEmpty()){
					if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
						startActivity(new Intent(SplashActivity.this,EventListActivity.class));
						finish();
					}else{
						txt_error.setVisibility(View.VISIBLE);
						txt_error.setText(getResources().getString(R.string.network_error));
						txt_error.startAnimation(startAnimation);
					}

				}else if(!BaseActivity.isValidResponse(result)){
					Util.db.isDBDeleted();
					BaseActivity.DeleteFiles();
					Util.clearSharedPreference(Util.login_prefer);
					Util.clearSharedPreference(Util.eventPrefer);
					Util.clearSharedPreference(Util.first_login_pref);
					Util.clearSharedPreference(Util.selected_session_attedee_pref);
					Util.clearSharedPreference(Util.offset_pref);
					Util.clearSharedPreference(Util.dashboard_data_pref);
					Util.clearSharedPreference(Util.socket_device_pref);
					Util.clearSharedPreference(Util.external_setting_pref);
					//Util.mApi = null;
					CookieManager.getInstance().removeAllCookie();
					Intent login = new Intent(SplashActivity.this, LoginActivity.class);
					login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
					login.putExtra(AppUtils.REVOKE_KEY, false);
					login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
					startActivity(login);
					finish();
					return;
				}else{
					SFDCDetails current_sfdc= gson.fromJson(result, SFDCDetails.class);
					sfdc_details.access_token = current_sfdc.access_token;
					Util.db.InsertAndUpdateSFDCDDETAILS(sfdc_details);

					if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
						startActivity(new Intent(SplashActivity.this,EventListActivity.class));
						finish();
					}else{
						*/
/*txt_error.setVisibility(View.VISIBLE);
						txt_error.setText(getResources().getString(R.string.network_error));
						txt_error.startAnimation(startAnimation);*//*


						Util.db.isDBDeleted();
						BaseActivity.DeleteFiles();
						Util.clearSharedPreference(Util.login_prefer);
						Util.clearSharedPreference(Util.eventPrefer);
						Util.clearSharedPreference(Util.first_login_pref);
						Util.clearSharedPreference(Util.selected_session_attedee_pref);
						Util.clearSharedPreference(Util.offset_pref);
						Util.clearSharedPreference(Util.dashboard_data_pref);
						Util.clearSharedPreference(Util.socket_device_pref);
						Util.clearSharedPreference(Util.external_setting_pref);
						//Util.mApi = null;
						CookieManager.getInstance().removeAllCookie();

						Intent login = new Intent(SplashActivity.this, LoginActivity.class);
						login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
						login.putExtra(AppUtils.REVOKE_KEY, false);
						login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
						startActivity(login);
						finish();
					}
				}
			} catch (Exception e) {
				// TODO: handle exception
				if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
					startActivity(new Intent(SplashActivity.this,EventListActivity.class));
					finish();
				}else{
					*/
/*txt_error.setVisibility(View.VISIBLE);
					txt_error.setText(getResources().getString(R.string.network_error));
					txt_error.startAnimation(startAnimation);*//*


					Util.db.isDBDeleted();
					BaseActivity.DeleteFiles();
					Util.clearSharedPreference(Util.login_prefer);
					Util.clearSharedPreference(Util.eventPrefer);
					Util.clearSharedPreference(Util.first_login_pref);
					Util.clearSharedPreference(Util.selected_session_attedee_pref);
					Util.clearSharedPreference(Util.offset_pref);
					Util.clearSharedPreference(Util.dashboard_data_pref);
					Util.clearSharedPreference(Util.socket_device_pref);
					Util.clearSharedPreference(Util.external_setting_pref);
					//Util.mApi = null;
					CookieManager.getInstance().removeAllCookie();

					Intent login = new Intent(SplashActivity.this, LoginActivity.class);
					login.putExtra(AppUtils.INTENT_KEY, EventListActivity.class.getName());
					login.putExtra(AppUtils.REVOKE_KEY, false);
					login.putExtra(AppUtils.ACCESS_TOKEN, sfdc_details.refresh_token);
					startActivity(login);
					finish();
				}

			}

		}
*/
	}
	private class GetAppVersion extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			loading.setVisibility(View.GONE);
			/*if (!result.isEmpty() && !result.equalsIgnoreCase(appversion)) {
				alertStoreDialog("Update latest ScanAttendee version from play store.");
			} else {*/
			if (!result.isEmpty()&&!result.equalsIgnoreCase(appversion)&(!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty())){
				alertStoreDialog("Update latest ScanAttendee version from play store.");
			}else {
				startActivity(new Intent(SplashActivity.this,EventListActivity.class));
				finish();
			}
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loading.setVisibility(View.VISIBLE);

		}
		@Override
		protected String doInBackground(Void... params) {

			return getPlayAppVersion();
		}

	}
	private String getPlayAppVersion() {
		String currentversion = "";
		try {
			Document doc = Jsoup.connect(WebServiceUrls.PLAY_STORE_URL).get();
			String version = "Current Version";
			int index = doc.text().indexOf(version);

			currentversion = doc.text().substring((index + version.length()),
					(index + version.length() + 6));
			currentversion = currentversion.trim();
			//Log.i("Google Play App Version-------------", ":"+ currentversion);
		} catch (Exception e) {

			//Log.i("Exception-------------", ":");
			return currentversion;
		}
		return currentversion;
	}
	private String getAccessTokenString(String refresh_token) {
		String token = "";
		//Log.i("-----------------ORG Name---------------",":"+AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF));
		HttpPost post;
		try {
			HttpClient client = HttpClientClass.getHttpClient(30000);
			//Log.i("---------------------Authorization Url----------------", ":"+ WebServiceUrls.REFRESH_TOKEN + refresh_token);
			if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")){
				post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
			}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")){
				post = new HttpPost(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
			}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
				post = new HttpPost(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REFRESH_TOKEN + refresh_token);
				// Log.i("------------Access Token Url----------------",":"+WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REFRESH_TOKEN + refresh_token);
			}else{
				post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REFRESH_TOKEN + refresh_token);
			}
			//Log.i("------------Access Token Url----------------",":"+WebServiceUrls.SA_QA_DOMAIN_2+WebServiceUrls.REFRESH_TOKEN + refresh_token);
			HttpResponse response = client.execute(post);
			//token = EntityUtils.toString(response.getEntity());
			int res_code=response.getStatusLine().getStatusCode();
			//if (res_code == HTTP_OK) {
                token = EntityUtils.toString(response.getEntity());
            //}
			//Log.i("-----------Response--------------", ":" + token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return token;
	}

	public void openSessionExpireAlert(String msg) {
		try {
			Util.setCustomAlertDialog(SplashActivity.this);
			final String[] title_msg = msg.split(";");
			//Log.i("------------String Alrt Message----------",":"+title_msg.length+" : "+msg);
			Util.setCustomDialogImage(R.drawable.error);
			if (title_msg[0].equalsIgnoreCase("APEX_ERROR")) {
				Util.txt_okey.setText("OK");
				Util.txt_dismiss.setVisibility(View.GONE);
			}/*else if(title_msg[0].equalsIgnoreCase("invalid_request_error")){
            Util.txt_okey.setText("OK");
            Util.txt_dismiss.setVisibility(View.GONE);
        }*/ else {
				Util.txt_okey.setText("LOGOUT");
				Util.txt_dismiss.setVisibility(View.VISIBLE);

			}

			Util.alert_dialog.setCancelable(false);

			Util.txt_okey.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// ticket_dialog.dismiss();
					try{
					Util.alert_dialog.dismiss();
					if (!title_msg[0].equalsIgnoreCase("APEX_ERROR")) {
						boolean isdeleted = Util.db.isDBDeleted();
						if (isdeleted) {
							DeleteFiles();
							Util.clearSharedPreference(Util.login_prefer);
							Util.clearSharedPreference(Util.eventPrefer);
							Util.clearSharedPreference(Util.first_login_pref);
							Util.clearSharedPreference(Util.selected_session_attedee_pref);
							Util.clearSharedPreference(Util.offset_pref);
							Util.clearSharedPreference(Util.dashboard_data_pref);
							Util.clearSharedPreference(Util.socket_device_pref);
							//Util.clearSharedPreference(Util.external_setting_pref);
							//Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
							Util.clearSharedPreference(Util.external_setting_pref);
							Util.clearSharedPreference(Util.tempselfcheckinpref);
							//Util.mApi = null;
							CookieManager.getInstance().removeAllCookie();
							Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
							finish();
						} else {
							DeleteFiles();
							Util.clearSharedPreference(Util.login_prefer);
							Util.clearSharedPreference(Util.eventPrefer);
							Util.clearSharedPreference(Util.first_login_pref);
							Util.clearSharedPreference(Util.selected_session_attedee_pref);
							Util.clearSharedPreference(Util.offset_pref);
							Util.clearSharedPreference(Util.dashboard_data_pref);
							Util.clearSharedPreference(Util.socket_device_pref);
							//Util.clearSharedPreference(Util.external_setting_pref);
							//Util.external_setting_pref = getSharedPreferences(Util.EXTERNAL_PREF, MODE_PRIVATE);
							Util.clearSharedPreference(Util.external_setting_pref);
							Util.clearSharedPreference(Util.tempselfcheckinpref);
							//Util.mApi = null;
							CookieManager.getInstance().removeAllCookie();
							Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
							startActivity(intent);
							finish();
						}
					}
				}catch (Exception e){
						e.printStackTrace();
						CookieManager.getInstance().removeAllCookie();
						Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(intent);
						finish();
					}}
			});
			Util.txt_dismiss.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// ticket_dialog.dismiss();
					try {
						Util.alert_dialog.dismiss();
						//if (Util.db.isRecordExists("USER_PROFILE"," where UserID='"	+ Util._getPreference(Util.login_prefer,Util.USER_ID) + "'")) {
						if (!Util._getPreference(Util.login_prefer,Util.USER_ID).isEmpty()) {
							startActivity(new Intent(SplashActivity.this,EventListActivity.class));
							finish();
						}else {
							startActivity(new Intent(SplashActivity.this, LoginActivity.class));
							finish();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			if (!title_msg[0].equalsIgnoreCase("APEX_ERROR")) {
				title_msg[1] = title_msg[1] + ". Please logout and login again.";
			}
			Util.openCustomDialog("ERROR", title_msg[1]);
		}catch (Exception e){
			e.printStackTrace();
		}
	}

}
