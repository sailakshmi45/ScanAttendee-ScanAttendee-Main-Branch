package com.globalnest.payments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.globalnest.scanattendee.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


@SuppressWarnings("deprecation")
public class TrustCommerceWebView extends Activity implements OnClickListener{

	public static final int TRUST_COMMERCE_REQUEST_CODE = 1002;
	public static final String TRUST_COMMERCE_CUSTOMER_ID = "customerId";
	public static final String TRUST_COMMERCE_PASS_WORD = "passWord";
	public static final String TRUST_COMMERCE_RESULT = "result";
	public static final String TRUST_COMMERCE_AMOUNT = "amount";
	public static final String TRUST_COMMERCE_TICKET = "ticket";
	public static final String TRUST_COMMERCE_NAME = "name";
	WebView webView;
	private String custId,passWord,amount,ticket,name;
	//private String OAUTH_URL ="https://vault.trustcommerce.com/trustee/token.php?custid=1114000&password=vivJand9";
	private String OAUTH_URL ="https://vault.trustcommerce.com/trustee/token.php?";
	private String PAYMENT_URL = "https://vault.trustcommerce.com/trustee/payment.php?";
	private String REDIRECT_URL = "https://blnew-boothleads.cs14.force.com/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trust_commerce_webview);
		CookieManager manager = CookieManager.getInstance();
		manager.removeAllCookie();
		manager.removeExpiredCookie();
		manager.removeSessionCookie();
		webView = (WebView)findViewById(R.id.webv);
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			custId = extras.getString(TRUST_COMMERCE_CUSTOMER_ID);
			passWord = extras.getString(TRUST_COMMERCE_PASS_WORD);
			amount = extras.getString(TRUST_COMMERCE_AMOUNT);
			ticket = extras.getString(TRUST_COMMERCE_TICKET);
			name = extras.getString(TRUST_COMMERCE_NAME);
			OAUTH_URL = OAUTH_URL+"custid="+custId+"&password="+passWord+"&ticket="+ticket+"&name="+name;
			//Log.i("-----------------OAuth URL-------------",":"+OAUTH_URL);
		}
		new HttpRequestAsync().execute();

	}

	@Override
	public void onClick(View v) {

	}

	public class HttpRequestAsync extends AsyncTask<Void, Void, String>{

		ProgressDialog pDialog = new ProgressDialog(TrustCommerceWebView.this);
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.setMessage("Loading....");
			pDialog.setCancelable(false);
			pDialog.show();
		}
		@Override
		protected String doInBackground(Void... param) {
			DefaultHttpClient httpClient;
			HttpPost httpPost;
			
			String json=null;
			try {
				httpClient = new DefaultHttpClient();
				httpPost = new HttpPost(OAUTH_URL);
				httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
				HttpResponse httpResponse = httpClient.execute(httpPost);
				HttpEntity httpEntity = httpResponse.getEntity();
				json = EntityUtils.toString(httpEntity);
				//Log.i("------------------token-----------",":"+json);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return json;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pDialog.dismiss();
			//Log.i("-----------------Web Url------------",":"+result);
			loadWebView(result);
		}



	}

	/**
	 * 
	 * @param token
	 */
	private void loadWebView(String token){
		try {
			webView.getSettings().setJavaScriptEnabled(true);
		//	webView.loadUrl(PAYMENT_URL+"token="+ZmE0YmFmMjNhOWE0NDE5OGQ4YTJkMTMxNTRhZWI2YTY=+"&amount=10&name=babu&customfield2=hyd&ticket=movie");
			webView.loadUrl(PAYMENT_URL+"token="+token+"&amount="+amount);
			webView.setWebViewClient(new WebViewClient() {

				ProgressDialog progress = new ProgressDialog(TrustCommerceWebView.this);

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					
					progress.dismiss();
					if(url.contains(REDIRECT_URL)&& url.equals(REDIRECT_URL)){
						finish();
					}else if (url.contains("status=approved")) {
						//Log.i("------------- Response--------------", ":"+ url);
						//Uri uri = Uri.parse(url);
						sendResponse(url);
						//Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
						finish();
					}else if(url.contains("status=decline")){
						//Uri uri = Uri.parse(url);
						sendResponse(url);
						finish();
					}
					
					//True if the host application wants to leave the current WebView and handle the url itself, otherwise return false.
					return true;
				}

				@Override 
				public void onPageStarted(WebView view, String url, Bitmap favicon){
					super.onPageStarted(view, url, favicon);
					try {
						progress.setMessage("Loading... ");
						progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						progress.setCancelable(false);
						progress.show();
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				@Override
				public void onPageFinished(WebView view, String url) {
					super.onPageFinished(view, url);
					progress.dismiss();
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * @author Babu
	 * @param uri
	 */
	
	private void sendResponse(String uri){
		
		//String message = uri.getQueryParameter("responsecodedescriptor");
		Intent result = new Intent();
		result.putExtra(TRUST_COMMERCE_RESULT, uri);
		setResult(TRUST_COMMERCE_REQUEST_CODE, result);
	}
}
