package com.globalnest.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * 
 * @author babu
 * you have to pass
  		String status = args[0];
		String consumerKey = args[1];
		String consumerSecret = args[2];
		String oAuthTocken = args[3];
		String oAuthSecret = args[4];
		String link = args[5];
		String imageLink = args[6];
 */
public class ShareTwitterStatus extends AsyncTask<String, String, String> {

	Activity context;
	ProgressDialog pDialog;
	public ShareTwitterStatus(Activity context) {
		this.context =context;
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Posting to twitter...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	protected String doInBackground(String... args) {

		String status = args[0];
		String consumerKey = args[1];
		String consumerSecret = args[2];
		String oAuthTocken = args[3];
		String oAuthSecret = args[4];
		String link = args[5];
		String imageLink = args[6];
		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(consumerKey);
			builder.setOAuthConsumerSecret(consumerSecret);

			// Access Token
			String access_token = oAuthTocken;
			// Access Token Secret
			String access_token_secret = oAuthSecret;

			AccessToken accessToken = new AccessToken(access_token, access_token_secret);
			Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

			if(!link.equals("")){
				status = status+" \n "+link;
			}else{
				status = status+" \n http://www.google.com";
			}
			// Update status
			StatusUpdate statusUpdate = new StatusUpdate(status);
			if(imageLink!=null && !imageLink.equals("")){
				InputStream is = null;
				try {
					URL url = new URL(imageLink);
					is = url.openStream();
				} catch (Exception e) {
					e.printStackTrace();
				}
				statusUpdate.setMedia("test.jpg", is);
			}


			twitter4j.Status response = twitter.updateStatus(statusUpdate);
			//twitter.

			Log.d("Status", response.getText());

		} catch (TwitterException e) {
			
			Log.d("Failed to post!", e.getMessage());
			return "failed";
		}
		return "success";
	}

	@Override
	protected void onPostExecute(String result) {

		/* Dismiss the progress dialog after sharing */
		pDialog.dismiss();
		if(result!=null && result.contains("success")){
			Toast.makeText(context, "Posted to Twitter!", Toast.LENGTH_SHORT).show();
			//context.finish();
			
		}else{
			Toast.makeText(context, "Failed to post!", Toast.LENGTH_SHORT).show();
		}
		//shareTxt.setText("");
		

		// Clearing EditText field
		//mShareEditText.setText("");
	}

}
