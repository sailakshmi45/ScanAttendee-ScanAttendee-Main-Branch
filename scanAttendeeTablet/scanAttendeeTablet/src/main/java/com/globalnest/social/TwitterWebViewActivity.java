package com.globalnest.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.globalnest.scanattendee.R;

public class TwitterWebViewActivity extends Activity {
	
	private WebView webView;

	public static String EXTRA_URL = "extra_url";

	ProgressDialog pDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		//setTitle("Login");

		final String url = this.getIntent().getStringExtra(EXTRA_URL);
		if (null == url) {
			Log.e("Twitter", "URL cannot be null");
			finish();
		}
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new MyWebViewClient());
		webView.clearCache(true);
		pDialog = new ProgressDialog(TwitterWebViewActivity.this);
		pDialog.setMessage("Please wait redirecting to Twitter...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
		webView.loadUrl(url);
	}


	class MyWebViewClient extends WebViewClient {
		
		
		  @Override
          public void onPageStarted(WebView view, String url, Bitmap favicon) {
			 
           super.onPageStarted(view, url, favicon);
           
          }

         @Override
         public void onPageFinished(WebView view, String url) {
        	 super.onPageFinished(view, url);
        	 pDialog.dismiss();
         }
		
         
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
		//	pDialog.dismiss();
			if (url.contains(getResources().getString(R.string.twitter_callback))) {
				Uri uri = Uri.parse(url);
			//	pDialog.dismiss();
				/* Sending results back */
				String verifier = uri.getQueryParameter(getString(R.string.twitter_oauth_verifier));
				Intent resultIntent = new Intent();
				resultIntent.putExtra(getString(R.string.twitter_oauth_verifier), verifier);
				setResult(RESULT_OK, resultIntent);
				
				/* closing webview */
				finish();
				return true;
			}
			return false;
		}
	}
	
	@Override
	public void onBackPressed() {
		finish();
		TwitterActivity.activity.finish();
	}

}
