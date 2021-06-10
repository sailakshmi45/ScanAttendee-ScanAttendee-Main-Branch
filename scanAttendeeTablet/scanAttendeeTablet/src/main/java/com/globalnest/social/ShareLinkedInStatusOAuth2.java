package com.globalnest.social;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.globalnest.utils.AppUtils;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;

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
		
		you need to create a app in https://www.linkedin.com/developer/apps and pass clientId and clientSecret
		for url shortening you need to create a app in https://console.developers.google.com/project and create a api key and copy that key in below key parameter
		https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyCrYPmdv_14i_UitxrCFOlnDVVmXB0lEIk
 *
 */


public class ShareLinkedInStatusOAuth2 extends AsyncTask<String, String, String>{

	ProgressDialog pDialog;
	Context context;
	public ShareLinkedInStatusOAuth2(Context context) {
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

	protected String doInBackground(String... args) {

		String access_token = args[3];
		String result="";
			try {
				
				ShareGson objShare = new ShareGson();
				ShareContentGson objShareContentGson = new ShareContentGson();
				ShareVisibility visibility = new ShareVisibility();
				objShareContentGson.description = "";
				objShareContentGson.title = "";
				objShareContentGson.submittedImageUrl=getShortURL(args[6]);
				if(args[5].length()==0){
					objShareContentGson.submittedUrl ="http://www.eventdex.com/";
				}else{
					objShareContentGson.submittedUrl =args[5];
				}
				
				objShare.comment = args[0];
				
				visibility.code ="anyone";
				objShare.visibility = visibility;
				objShare.content = objShareContentGson;
				Gson test = new Gson();
				String stringEntity = test.toJson(objShare);
				 result = executeRequestPost("https://api.linkedin.com/v1/people/~/shares?format=json", access_token,stringEntity);
				
				//Log.i("----------------Linked In Response---------",":"+result);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}

			return result;

		
	}

	@Override
	protected void onPostExecute(String result) {
         super.onPostExecute(result);
		/* Dismiss the progress dialog after sharing */
		pDialog.dismiss();
		//shareTxt.setText("");
		if(!AppUtils.NullChecker(result).isEmpty() && result.contains("success")){
			Toast.makeText(context, "Posted to LinkedIn!", Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
		}
		
	}

	public String getShortURL(String longUrl) {
		// Making HTTP request
		String short_url = "";
		try {
			// DefaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(
					"https://www.googleapis.com/urlshortener/v1/url?key=AIzaSyCrYPmdv_14i_UitxrCFOlnDVVmXB0lEIk");
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
	
	
	private final int HTTP_CREATED = 201;
	private String executeRequestPost(String _url,String access_token,String stringEntity) {
		String responce = null;
		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(_url);
			httpPost.addHeader("Accept", "application/json");
			httpPost.addHeader("Content-Type", "application/json");
			httpPost.addHeader("Connection", "Keep-Alive");
			httpPost.addHeader("Authorization", "Bearer "+access_token);
			
			if(stringEntity!=null && stringEntity.length() >0){
				StringEntity entity = new StringEntity(stringEntity);
				httpPost.setEntity(entity);
			}
			HttpResponse httpResponse = httpClient.execute(httpPost);

			////Log.i("-------------------Http Response----------------",":"+EntityUtils.toString(httpResponse.getEntity()));
			
			responce = EntityUtils.toString(httpResponse.getEntity());
			//Log.i("-------------------Http Response----------------",":"+responce);
			JSONObject obj = new JSONObject(responce);
		
			if(httpResponse.getStatusLine().getStatusCode() == HTTP_CREATED){
				responce = "success";
			}else {
				responce = obj.optString("message");
				//responce = httpResponse.getStatusLine().getStatusCode()+"server error";
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
}


