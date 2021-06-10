package com.globalnest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.globalnest.network.Connectivity;
import com.globalnest.network.HttpClientClass;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.SFDCDetails;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketException;


public class LoginActivity extends Activity {
	WebView web_view;
	ProgressBar progress_bar, progress_horizontal;
	ImageView img_splash,img_refresh;
	String className="",refresh_token="";
	boolean isRevoke = false;
	AlertDialogCustom alert_dialog;
	LinearLayout layout_hide;
	FrameLayout layout_header;
	Spinner spnr_orgs;
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(com.globalnest.scanattendee.R.layout.login_layout);
			AppUtils.isDevPref = getSharedPreferences(AppUtils.IS_DEV_PREF, MODE_PRIVATE);
			//AppUtils.user_credentials = getSharedPreferences(AppUtils.USER_CRED_PRF, MODE_PRIVATE);
			AppUtils.isDevPref.edit().putString(AppUtils.IS_DEV_PREF, "PRODUCTION").commit();
			alert_dialog = new AlertDialogCustom(this);
			className = this.getIntent().getStringExtra(AppUtils.INTENT_KEY);
			isRevoke = this.getIntent().getBooleanExtra(AppUtils.REVOKE_KEY, false);
			refresh_token = this.getIntent().getStringExtra(AppUtils.ACCESS_TOKEN);
			layout_header = (FrameLayout) findViewById(com.globalnest.scanattendee.R.id.layout_header);
			img_refresh = (ImageView) findViewById(com.globalnest.scanattendee.R.id.img_refresh);
			layout_hide = (LinearLayout) findViewById(com.globalnest.scanattendee.R.id.layout_hide);
			progress_bar = (ProgressBar) findViewById(com.globalnest.scanattendee.R.id.progressbar_circular);
			progress_bar.setVisibility(View.GONE);
			progress_horizontal = (ProgressBar) findViewById(com.globalnest.scanattendee.R.id.progressbar_horizontal);
			progress_horizontal.setVisibility(View.GONE);
			progress_horizontal.getIndeterminateDrawable().setColorFilter(getResources().getColor(com.globalnest.scanattendee.R.color.green_color), android.graphics.PorterDuff.Mode.MULTIPLY);
			web_view = (WebView) findViewById(com.globalnest.scanattendee.R.id.web_view);
			img_splash = (ImageView) findViewById(com.globalnest.scanattendee.R.id.img_splash);
			img_splash.setVisibility(View.GONE);
			spnr_orgs = (Spinner) findViewById(com.globalnest.scanattendee.R.id.orgs_spinner);
			if (AppUtils.isLogEnabled) {
				spnr_orgs.setSelection(1);
				spnr_orgs.setVisibility(View.VISIBLE);
			} else {
				spnr_orgs.setSelection(2);
				spnr_orgs.setVisibility(View.GONE);
			}
			web_view.getSettings().setJavaScriptEnabled(true);
			//web_view.getSettings().setUseWideViewPort(true);
			//web_view.getSettings().setLoadWithOverviewMode(true);
			//web_view.getSettings().setSaveFormData(false);
			web_view.getSettings().setSavePassword(false);
			web_view.getSettings().setAllowContentAccess(true);


			////Log.i("---------------Internet Connection------------",":"+Connectivity.isConnectedFast(this));
			if (Connectivity.isConnectedFast(this)) {

				if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")) {
					if (isRevoke) {
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
					} else {
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
					}
				} else if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")) {
					if (isRevoke) {
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
					} else {
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
					}
				} else if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")) {
					if (isRevoke) {
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.REVOKE_URL + refresh_token);
					} else {
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.AUTHORIZECODE_URL);
					}
				}


			} else {
				alert_dialog.setParamenters("Internet Connection", "Your Internet is very slow unable to load login page. Please check your internet connection. ", null, null, 1, false);
				alert_dialog.show();
			}

			web_view.setWebViewClient(new WebviewWebViewClient());

			web_view.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int newProgress) {
					super.onProgressChanged(view, newProgress);
					progress_horizontal.setProgress(newProgress);
					if (newProgress == 100) {
						progress_horizontal.setVisibility(View.GONE);
					} else {
						progress_horizontal.setVisibility(View.VISIBLE);
					}
				}
			});

			spnr_orgs.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					// TODO Auto-generated method stub
					String[] orgs = getResources().getStringArray(com.globalnest.scanattendee.R.array.orgs_array);
					if (orgs[position].equalsIgnoreCase("DEV")) {
						AppUtils._saveOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF, "DEV");
						if (isRevoke) {
							web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
						} else {
							web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
						}
					} else if (orgs[position].equalsIgnoreCase("QA")) {
						AppUtils._saveOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF, "QA");
						if (isRevoke) {
							web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
							//AppUtils.isCrashReportEnable=true;
						} else {
							//AppUtils.isCrashReportEnable=true;
							web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
						}
					} else if (orgs[position].equalsIgnoreCase("PRODUCTION")) {
						AppUtils._saveOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF, "PRODUCTION");
						if (isRevoke) {
							AppUtils.isCrashReportEnable = false;
							web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.REVOKE_URL + refresh_token);
						} else {
							AppUtils.isCrashReportEnable = false;
							web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.AUTHORIZECODE_URL);
						}
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					// TODO Auto-generated method stub

				}

			});

			img_refresh.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")) {
						if (isRevoke) {
							web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
						} else {
							web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
						}
					} else if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")) {
						if (isRevoke) {
							web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.REVOKE_URL + refresh_token);
						} else {
							web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN + WebServiceUrls.AUTHORIZECODE_URL);
						}
					} else if (AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")) {
						if (isRevoke) {
							web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.REVOKE_URL + refresh_token);
						} else {
							web_view.loadUrl(WebServiceUrls.SA_PRODUCTION + WebServiceUrls.AUTHORIZECODE_URL);
						}
					}
				}
			});
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	private class WebviewWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			//Log.i("---------------------Complete Url----------------", ":"+ url);
			if (url.contains("https://success")) {
				Uri uri = Uri.parse(url);
				if(AppUtils.NullChecker(uri.getQueryParameter("error")).equalsIgnoreCase("access_denied")){
					openDenienedAlert();
				}else{
					String verifier = uri.getQueryParameter("code");
					new getAccessToken().execute(verifier);
				}
			}else if(url.endsWith("ASC_CustomLanding") || url.endsWith("CustomLanding")){
				/*web_view.setVisibility(View.GONE);
				img_splash.setVisibility(View.VISIBLE);
				progress_bar.setVisibility(View.VISIBLE);*/
				if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("DEV")){
					view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
				}else if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("QA")){
					view.loadUrl(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
				}else if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("PRODUCTION")){
					view.loadUrl(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.AUTHORIZECODE_URL);
				}

			}else{
				view.loadUrl(url);
			}
			return true;
		}
		public void onPageFinished(WebView view, String url) {
			// do your stuff here
			super.onPageFinished(view,url);
			
			/*if(url.contains("https://developer-eventdex.cs7.force.com/ScanAttendee/apex/ASC_CustomLanding")){
				 view.loadUrl(WebServiceUrls.AUTHORIZECODE_URL);
			}*/
			
			/*if (!url.contains("ASC_CustomLanding")) {
				if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					view.evaluateJavascript("javascript:document.getElementById('" + WebServiceUrls.LOGIN_INPUTBOX_ID
							+ "').value='" +AppUtils.NullChecker(AppUtils.user_credentials.getString(AppUtils.USER_EMAIL, ""))+ "';", null);
				} else {
					view.loadUrl("javascript:document.getElementById('" + WebServiceUrls.LOGIN_INPUTBOX_ID + "').value='"
							+ AppUtils.NullChecker(AppUtils.user_credentials.getString(AppUtils.USER_EMAIL, "")) + "';");
				}
			}*/

		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
									String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			view.loadUrl("file:///android_asset/myerrorpage.html");
		}
	}

	private class getAccessToken extends AsyncTask<String, Void, String> {
		protected void onPreExecute() {
			super.onPreExecute();
			layout_hide.setVisibility(View.GONE);
			web_view.setVisibility(View.GONE);
			spnr_orgs.setVisibility(View.GONE);
			layout_header.setVisibility(View.GONE);
			img_splash.setVisibility(View.VISIBLE);
			progress_bar.setVisibility(View.VISIBLE);

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			return getAccessTokenString(params[0]);
		}

		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progress_bar.setVisibility(View.GONE);
			progress_horizontal.setVisibility(View.GONE);
			try {
				//Log.i("-------------Login Response-----------", ":"+result);

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
				sfdc.refresh_token = refresh_token;
				sfdc.access_token = access_token;
				sfdc.token_type=token_type;

				Intent i = new Intent();
				i.setClassName(LoginActivity.this, className);
				//Intent i = new Intent(LoginActivity.this, EventsListActivity.class);
				i.putExtra(AppUtils.INTENT_KEY, sfdc);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				finish();

			} catch (Exception e) {
				// TODO: handle exception
				if(TextUtils.isEmpty(result)){
					alert_dialog.setParamenters("Error", e.getMessage(), null, null, 1, true);
				}else{
					alert_dialog.setParamenters("Error", result, null, null, 1, true);
				}
			}
		}
	}


















	private String getAccessTokenString(String authorize_code) {
		String token = "";
		try {

			HttpClient client =  HttpClientClass.getHttpClient(60000);
			//Log.i("---------------------Authorization Url----------------", ":"	+ WebServiceUrls.TOKEN_URL + authorize_code);
			HttpPost post;
			if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("DEV")){
				//Log.i("---------------------Authorization Url----------------", ":"	+ WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.TOKEN_URL + authorize_code);
				post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.TOKEN_URL + authorize_code);
			}else if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("QA")){
				post = new HttpPost(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.TOKEN_URL + authorize_code);
			}else if(spnr_orgs.getSelectedItem().toString().equalsIgnoreCase("PRODUCTION")){
				post = new HttpPost(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.TOKEN_URL + authorize_code);
			}else{
				post = new HttpPost(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.TOKEN_URL + authorize_code);
			}
			HttpResponse response = client.execute(post);
			token = EntityUtils.toString(response.getEntity());
			//Log.i("-----------Response--------------", ":" + token);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return token;
	}

	private void openDenienedAlert(){

		alert_dialog.setParamenters("Alert", "Please click on Allow to access ScanAttendee.", null, null, 0, false);
		alert_dialog.btnOK.setText("OK");
		alert_dialog.btnCancel.setVisibility(View.VISIBLE);
		alert_dialog.btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert_dialog.dismiss();
				if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}
			}
		});
		alert_dialog.btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alert_dialog.dismiss();
				CookieManager.getInstance().removeAllCookie();
				if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_DEV_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_QA_DOMAIN+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
					if(isRevoke){
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.REVOKE_URL+refresh_token);
					}else{
						web_view.loadUrl(WebServiceUrls.SA_PRODUCTION+WebServiceUrls.AUTHORIZECODE_URL);
					}
				}
			}
		});
		alert_dialog.show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
