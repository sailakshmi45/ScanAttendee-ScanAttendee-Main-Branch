package com.globalnest.social;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.globalnest.scanattendee.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

public class LinkedInActivity extends Activity  {

	private ProgressDialog progressDialog = null;
	public static String clientID,clientSecret,callBackUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);// must call before super.
		super.onCreate(savedInstanceState);
		progressDialog = new ProgressDialog(LinkedInActivity.this);
		progressDialog.setMessage("loading");
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); StrictMode.setThreadPolicy(policy);
		}
		setContentView(R.layout.ln_dialog);
		clientID = getIntent().getExtras().getString(getString(R.string.consumerKey));
		clientSecret = getIntent().getExtras().getString(getString(R.string.consumersecret));
		callBackUrl = getIntent().getExtras().getString(getString(R.string.callback));
		setWebView();
	}

	/**
	 * set webview.
	 */
	@SuppressWarnings("deprecation")
	private void setWebView() {
		

		WebView mWebView = (WebView) findViewById(R.id.webkitWebView1);
		//mWebView.getSettings().setJavaScriptEnabled(true);
		CookieManager.getInstance().setAcceptCookie(true);
		mWebView.loadUrl("https://www.linkedin.com/uas/oauth2/authorization?response_type=code&client_id="+clientID+"&redirect_uri="+callBackUrl+"&state=987654321&scope=r_basicprofile+w_share");
		progressDialog.show();
		mWebView.setWebViewClient(new HelloWebViewClient());


	}
	
	class HelloWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains(callBackUrl)) {
				Uri uri = Uri.parse(url);
				String verifier = uri.getQueryParameter("code");
				progressDialog.dismiss();
				String accessUrl = "https://www.linkedin.com/uas/oauth2/accessToken?grant_type=authorization_code&code="+verifier+"&redirect_uri="+callBackUrl+"&client_id="+clientID+"&client_secret="+clientSecret;
				new AccessToken().execute(accessUrl,"https://api.linkedin.com/v1/people/~?format=json");
				
			} else if (url.contains(callBackUrl)) {
				progressDialog.dismiss();
			} else {
				//Log.i("LinkedinSample", "url: " + url);
				view.loadUrl(url);
			}

			return true;
		}
		@Override
		public void onPageFinished(WebView view, String url) {
			progressDialog.dismiss();
		}
	}
	
	public class AccessToken extends AsyncTask<String, Void, String>{
		private ProgressDialog progressDialog = null;
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			progressDialog = new ProgressDialog(LinkedInActivity.this);
			progressDialog.setMessage("loading");
			progressDialog.show();
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			if(result.equals(",")){
				Intent intent = null;
				setResult(502, intent);
			}else{
				String[] results = result.split(",");
				Intent intent = new Intent();
				intent.putExtra("accesstoken", results[0]);
				intent.putExtra("userName", results[1]);
				setResult(502, intent);
			}
			finish();
		}
		@Override
		protected String doInBackground(String... params) {
			String result = executeRequest(params[0]);
			String access_token = "";
			try {
				JSONObject objJson = new JSONObject(result);
				if(!objJson.isNull("access_token")){
					access_token = objJson.getString("access_token");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			String profileInfo = executeGetRequest(params[1],access_token);
			String name = "";
			
			try {
				JSONObject objJson = new JSONObject(profileInfo);
				if(!objJson.isNull("firstName")){
					name = objJson.getString("firstName");
				}
				if(!objJson.isNull("lastName")){
					name = name +" "+objJson.getString("lastName");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return access_token+","+name;
		}
		
	}
	
	private final int HTTP_OK = 200;
	private String executeRequest(String _url) {
		String responce = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			/*((AbstractHttpClient) httpClient).getCookieSpecs().register("lenient", new CookieSpecFactory() {
		        public CookieSpec newInstance(HttpParams params) {
		            return new LenientCookieSpec();
		        }
		    });
		HttpClientParams.setCookiePolicy(httpClient.getParams(), "lenient");*/
			
			HttpPost httpPost = new HttpPost(_url);
			httpPost.addHeader("Accept", "application/json");
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
			
			HttpResponse httpResponse = httpClient.execute(httpPost);

			if(httpResponse.getStatusLine().getStatusCode() == HTTP_OK){
				responce = EntityUtils.toString(httpResponse.getEntity());
			}else {
				responce = httpResponse.getStatusLine().getStatusCode()+"server error";
			}



		} catch (MalformedURLException e) {
			e.printStackTrace();
			responce = "MalformedURLException";
			//Log.i("--------------MalformedURLException----",":"+e.getMessage());
		} catch (ProtocolException e) {
			e.printStackTrace();
			responce = "ProtocolException";
			//Log.i("--------------ProtocolException----",":"+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			responce = "IOException";
			//Log.i("--------------IOException----",":"+e.getMessage());
		}  catch(Exception e){
			responce = "Server error";
			e.printStackTrace();
		}
		return responce;
	}
	private String executeGetRequest(String _url,String access_token) {
		String responce = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(_url);
			httpget.addHeader("Accept", "application/json");
			httpget.addHeader("Content-Type", "application/json");
			httpget.addHeader("Connection", "Keep-Alive");
			httpget.addHeader("Authorization", "Bearer "+access_token);
			
			HttpResponse httpResponse = httpClient.execute(httpget);

			if(httpResponse.getStatusLine().getStatusCode() == HTTP_OK){
				responce = EntityUtils.toString(httpResponse.getEntity());
			}else {
				responce = httpResponse.getStatusLine().getStatusCode()+"server error";
			}
			
			


		} catch (MalformedURLException e) {
			e.printStackTrace();
			responce = "MalformedURLException";
			//Log.i("--------------MalformedURLException----",":"+e.getMessage());
		} catch (ProtocolException e) {
			e.printStackTrace();
			responce = "ProtocolException";
			//Log.i("--------------ProtocolException----",":"+e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			responce = "IOException";
			//Log.i("--------------IOException----",":"+e.getMessage());
		}  catch(Exception e){
			responce = "Server error";
			e.printStackTrace();
		}
		return responce;
	}
	
	class LenientCookieSpec extends BrowserCompatSpec {
	    public LenientCookieSpec() {
	        super();
	        registerAttribHandler(ClientCookie.EXPIRES_ATTR, new BasicExpiresHandler(DATE_PATTERNS) {
	            @Override public void parse(SetCookie cookie, String value) throws MalformedCookieException {
	                if (TextUtils.isEmpty(value)) {
	                    // You should set whatever you want in cookie
	                    cookie.setExpiryDate(null);
	                } else {
	                    super.parse(cookie, value);
	                }
	            }
	        });
	    }
	}
}
