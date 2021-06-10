package com.globalnest.social;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

import com.globalnest.scanattendee.R;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Linkedin dialog
 * 
 * @author Babu
 */
public class LinkedinDialog extends Dialog {
	private ProgressDialog progressDialog = null;

	public static LinkedInApiClientFactory factory;
	public static LinkedInOAuthService oAuthService;
	public static LinkedInRequestToken liToken;
	public static String consumerKey,secretKey,callBackUrl;
	//private ProgressBar progressBar;
	/**
	 * Construct a new LinkedIn dialog
	 * 
	 * @param context
	 *            activity {@link Context}
	 * @param progressDialog
	 *            {@link ProgressDialog}
	 */
	public LinkedinDialog(Context context, ProgressDialog progressDialog,String consumerKey,String secretKey,String callBackUrl) {
		super(context,android.R.style.Theme_Light);
		this.progressDialog = progressDialog;
		this.consumerKey = consumerKey;
		this.secretKey = secretKey;
		this.callBackUrl = callBackUrl;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);// must call before super.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ln_dialog);
		//progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		new setWebViewAsync().execute("");
	}

	
	/**
	 * set webview.
	 */

	class setWebViewAsync extends AsyncTask<String, String, Boolean>{

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			WebView mWebView = (WebView) findViewById(R.id.webkitWebView1);
			mWebView.getSettings().setJavaScriptEnabled(true);

			//Log.i("LinkedinSample", LinkedinDialog.liToken.getAuthorizationUrl());
			mWebView.loadUrl(LinkedinDialog.liToken.getAuthorizationUrl());
			mWebView.setWebViewClient(new HelloWebViewClient());

			mWebView.setPictureListener(new PictureListener() {
				@Override
				public void onNewPicture(WebView view, Picture picture) {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}

				}
			});
		}
		@Override
		protected Boolean doInBackground(String... params) {
			try {
				LinkedinDialog.oAuthService = LinkedInOAuthServiceFactory.getInstance()
						.createLinkedInOAuthService(consumerKey,secretKey);
				LinkedinDialog.factory = LinkedInApiClientFactory.newInstance(consumerKey,secretKey);

				LinkedinDialog.liToken = LinkedinDialog.oAuthService
						.getOAuthRequestToken(callBackUrl);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
	}
	/**
	 * webview client for internal url loading
	 * callback url: "https://www.linkedin.com/uas/oauth/mukeshyadav4u.blogspot.in"
	 */
	class HelloWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
		}


		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		//	progressBar.setVisibility(View.GONE);
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.e("test", "-----------------------------5---------------------------");
			if (url.contains(callBackUrl)) {
				Uri uri = Uri.parse(url);
				String verifier = uri.getQueryParameter("oauth_verifier");

				cancel();

				for (OnVerifyListener d : listeners) {
					// call listener method
					d.onVerify(verifier);
				}
			} else if (url
					.contains("https://www.linkedin.com/uas/oauth/mukeshyadav4u.blogspot.in")) {
				cancel();
			} else {
				//Log.i("LinkedinSample", "url: " + url);
				view.loadUrl(url);
			}

			return true;
		}
	}

	/**
	 * List of listener.
	 */
	private List<OnVerifyListener> listeners = new ArrayList<OnVerifyListener>();

	/**
	 * Register a callback to be invoked when authentication have finished.
	 * 
	 * @param data
	 *            The callback that will run
	 */
	public void setVerifierListener(OnVerifyListener data) {
		listeners.add(data);
	}

	/**
	 * Listener for oauth_verifier.
	 */
	interface OnVerifyListener {
		/**
		 * invoked when authentication have finished.
		 * 
		 * @param verifier
		 *            oauth_verifier code.
		 */
		public void onVerify(String verifier);
	}
	@Override
	public void onBackPressed() {

		LinkedIn.activity.finish();
	}
}
