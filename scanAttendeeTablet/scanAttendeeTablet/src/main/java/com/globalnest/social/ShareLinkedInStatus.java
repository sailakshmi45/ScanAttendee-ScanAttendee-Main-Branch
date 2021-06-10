package com.globalnest.social;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

/**
 * 
 * @author Babu
 * you have to pass
  		String status = args[0];
		String consumerKey = args[1];
		String consumerSecret = args[2];
		String oAuthTocken = args[3];
		String oAuthSecret = args[4];
		String link = args[5];
		String imageLink = args[6];
 *
 */

public class ShareLinkedInStatus extends AsyncTask<String, String, Boolean>{

	ProgressDialog pDialog;
	Context context;
	public ShareLinkedInStatus(Context context) {
		this.context = context;

	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Posting to linkedin...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	protected Boolean doInBackground(String... args) {

		String status = args[0];
		String consumerKey = args[1];
		String consumerSecret = args[2];
		String oAuthTocken = args[3];
		String oAuthSecret = args[4];
		String linkOfEvent = args[5];
		String imageLink = args[6];
		String title=args[7];
		String desc=args[8];
		try {

			try {

				OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
				consumer.setTokenWithSecret(oAuthTocken, oAuthSecret);
				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost post = new HttpPost("https://api.linkedin.com/v1/people/~/shares");
				try {
					consumer.sign(post);
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				} // here need the consumer for sign in for post the share
				post.setHeader("content-type", "text/XML");
				String image ="",link="";
				if(!imageLink.equals("")){

					//image = getShortURL(imageLink);
				}
				if(!linkOfEvent.equals("")){
					link =linkOfEvent;
				}else{
					link = "https://www.globalnest.com";
				}
				//link = "https://www.globalnest.com";
				//String Link="<a href=\""+ link + "\">" + title + "</a>";
				//status="";
				//="<a href=""\"+link+"\"">"+"eventsex."+"</a>";
				//image="http://www.chatamakovica.sk/img/icon/sun.png";
				image="http://www.socketmobile.com/images/default-source/software-partners/logo_scanattendee.png?sfvrsn=0";
				String myEntity = "<share>"
						+"<comment>"+status+"</comment>"
						+"<content>"
						+"<title>"+title+"</title>"
						+"<description>"+desc+"</description>"
						+"<submitted-url>"+link+"</submitted-url>"
						+"<submitted-image-url>"+image+"</submitted-image-url>" 
						+"</content>"
						+"<visibility> "
						+" <code>anyone</code>" 
						+"</visibility>"
						+"</share>";
				
				//Log.i("---share string----", ":"+myEntity);
				try {
					post.setEntity(new StringEntity(myEntity));
					HttpResponse response = httpclient.execute(post);
					//Log.i("---response string----", ":"+EntityUtils.toString(response.getEntity())+" request code "+response.getStatusLine().getStatusCode());
					if(response.getStatusLine().getStatusCode()==201){
						return true;
					}else{
						return false;
					}

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			return false;

		} catch (Exception e) {

			Log.d("Failed to post!", e.getMessage());
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {

		/* Dismiss the progress dialog after sharing */
		pDialog.dismiss();
		//shareTxt.setText("");
		if(result)
			Toast.makeText(context, "Posted to LinkedIn!", Toast.LENGTH_SHORT).show();
		else
			Toast.makeText(context, "Unable to Post", Toast.LENGTH_SHORT).show();
		// Clearing EditText field
		//mShareEditText.setText("");
	}

	public String getShortURL(String longUrl) {
		// Making HTTP request
		String short_url = "";
		try {
			// DefaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(
					"https://www.googleapis.com/urlshortener/v1/url");
			JSONObject obj = new JSONObject();
			obj.put("longUrl", longUrl);
			;
			httpPost.setEntity(new StringEntity(obj.toString()));
			httpPost.setHeader("Content-Type", "application/json");
			HttpResponse httpResponse = httpClient.execute(httpPost);
			String json = EntityUtils.toString(httpResponse.getEntity());
			JSONObject jObj = new JSONObject(json);
			short_url = jObj.getString("id");

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Return JSON String
		return short_url;
	}
}


