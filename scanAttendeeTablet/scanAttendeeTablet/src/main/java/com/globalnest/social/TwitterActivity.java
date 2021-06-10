package com.globalnest.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.widget.Toast;

import com.globalnest.scanattendee.R;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterActivity extends Activity{

	private static Twitter twitter;
	public static Activity activity;
	private static RequestToken requestToken;
	/* Any number for uniquely distinguish your request */
	public static final int WEBVIEW_REQUEST_CODE = 100;
	ProgressDialog pDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		activity = this;
		Bundle bundle = getIntent().getExtras();
		String consumerKey = bundle.getString(getString(R.string.consumerKey));
		String consumerSecret = bundle.getString(getString(R.string.consumersecret));
		String callbackUrl = bundle.getString(getString(R.string.callback));

		new TwitterLoginInAsync().execute(consumerKey, consumerSecret, callbackUrl);
		//loginToTwitter(consumerKey, consumerSecret, callbackUrl);

	}

	
	class TwitterLoginInAsync extends AsyncTask<String, String, Boolean>{

		

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(activity);
			pDialog.setMessage("Loading...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected Boolean doInBackground(String... args) {

			try {
				loginToTwitter(args[0], args[1], args[2]);
				return true;//objLinkedIn.linkedInLogout(ConfigLinkedIn.LINKEDIN_CONSUMER_KEY, ConfigLinkedIn.LINKEDIN_CONSUMER_SECRET, mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN_LINKEDIN, ""), mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET_LINKEDIN, ""));
				//return linkedInLogout();
			} catch (Exception e) {
				Log.d("Failed to logout", e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
		}


	}
	private void loginToTwitter(String consumerKey,String consumerSecret,String callbackUrl) {



		final ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(consumerKey);
		builder.setOAuthConsumerSecret(consumerSecret);
		final Configuration configuration = builder.build();
		final TwitterFactory factory = new TwitterFactory(configuration);
		twitter = factory.getInstance();
		try {
			requestToken = twitter.getOAuthRequestToken(callbackUrl);
			/**
			 *  Loading twitter login page on webview for authorization 
			 *  Once authorized, results are received at onActivityResult
			 *  */
			pDialog.dismiss();
			final Intent intent = new Intent(this, TwitterWebViewActivity.class);
			intent.putExtra(TwitterWebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
			startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

		} catch (TwitterException e) {
			pDialog.dismiss();
			Toast.makeText(activity, "unable to login", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			finish();
		}

	}
	
	public void logOut(){
			if(twitter!=null){
				twitter.setOAuthAccessToken(null);
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeAllCookie();
			}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			String verifier = data.getExtras().getString(getString(R.string.twitter_oauth_verifier));
			AccessToken accessToken;
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				long userID = accessToken.getUserId();
				final User user = twitter.showUser(userID);
				String username = user.getName();
				String accessTokens = accessToken.getToken();
				String secretToken = accessToken.getTokenSecret();
				/* Sending results back */
				
				Intent resultIntent = new Intent();
				resultIntent.putExtra("userName",username);
				resultIntent.putExtra("accessToken",accessTokens);
				resultIntent.putExtra("secretToken",secretToken);
				setResult(RESULT_OK, resultIntent);
				finish();
			} catch (TwitterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				finish();
			}

			
		}
	}
}
