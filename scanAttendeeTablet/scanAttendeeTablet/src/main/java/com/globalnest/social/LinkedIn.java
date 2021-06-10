package com.globalnest.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;

import com.globalnest.social.LinkedinDialog.OnVerifyListener;
import com.globalnest.scanattendee.R;
import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
import com.google.code.linkedinapi.schema.Person;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

public class LinkedIn extends Activity{


	private LinkedInAccessToken accessToken;
	private LinkedInApiClientFactory factory;
	private LinkedInOAuthService oAuthService ;
	public static Activity activity;

	private  LinkedInRequestToken liToken;
	private LinkedInApiClient client;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		String consumerKey = bundle.getString(getString(R.string.consumerKey));
		String consumerSecret = bundle.getString(getString(R.string.consumersecret));
		String callback = bundle.getString(getString(R.string.callback));
		String scope = bundle.getString(getString(R.string.scope));
		activity = this;
		linkedInLogin(LinkedIn.this, consumerKey, consumerSecret, callback, false, scope);
		
	
	}
	

	public boolean linkedInLogout(String consumerKey,String consumerSecret,String oAuthToken,String oAuthSecret){

		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		consumer.setTokenWithSecret(oAuthToken,oAuthSecret);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet post = new HttpGet("https://api.linkedin.com/uas/oauth/invalidateToken");
		try {
			consumer.sign(post);
		} catch (OAuthMessageSignerException e) {

			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {

			e.printStackTrace();
		} catch (OAuthCommunicationException e) {

			e.printStackTrace();
		}

		try {
			org.apache.http.HttpResponse response = httpclient.execute(post);
			//if(response.getStatusLine().getStatusCode()==200){

			return true;
			//}


		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (ClientProtocolException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return false;
	}
	public void linkedInLogin(final Context context,final String consumerKey,final String secretKey,final String callBackUrl,boolean isLogin,final String scope) {
       
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
			}
		});
		oAuthService = LinkedInOAuthServiceFactory
				.getInstance().createLinkedInOAuthService(consumerKey,secretKey,scope);
		factory = LinkedInApiClientFactory
				.newInstance(consumerKey,secretKey);
		if (!isLogin) {
			ProgressDialog progressDialog = new ProgressDialog(
					context);
			LinkedinDialog d = new LinkedinDialog(context,progressDialog,consumerKey, secretKey, callBackUrl);
			d.show();

			// set call back listener to get oauth_verifier value
			d.setVerifierListener(new OnVerifyListener() {
				@Override
				public void onVerify(String verifier) {
					try {
						//Log.i("LinkedinSample", "verifier: " + verifier);

						accessToken = LinkedinDialog.oAuthService
								.getOAuthAccessToken(LinkedinDialog.liToken,
										verifier);
						LinkedinDialog.factory.createLinkedInApiClient(accessToken);
						client = factory.createLinkedInApiClient(accessToken);
						
						Person p = client.getProfileForCurrentUser();
						
						
						Intent resultIntent = new Intent();
						resultIntent.putExtra("userName",p.getFirstName() + " "+ p.getLastName());
						resultIntent.putExtra("accesstoken",accessToken.getToken());
						resultIntent.putExtra("secrettoken",accessToken.getTokenSecret());
						setResult(502, resultIntent);
						finish();
						//TODO check for user name
						/*saveLinkedInfo(accessToken,p.getFirstName() + " "+ p.getLastName());
						name.setVisibility(0);
						linkedIn_info.setVisibility(View.VISIBLE);
						messageLayout.setVisibility(View.VISIBLE);
						name.setText(mSharedPreferences.getString(PREF_USER_NAME_LINKEDIN, "Unable to fetch user name"));
						name.setTextColor(Color.parseColor("#3997AC"));
						txtHeader.setText("LinkedIn");
						txtHeader.setTextColor(Color.parseColor("#3997AC"));
						socialImage.setVisibility(View.VISIBLE);
						socialImage.setBackgroundResource(R.drawable.linkedin_icon_small);*/
						/*share.setVisibility(0);
					et.setVisibility(0);*/

					} catch (Exception e) {
						//Log.i("LinkedinSample", "error to get verifier");
						finish();
						
					}
				}
			});

			// set progress dialog
			progressDialog.setMessage("Loading...");
			progressDialog.setCancelable(true);
			progressDialog.show();
		}else{
			//name.setText(mSharedPreferences.getString(PREF_USER_NAME_LINKEDIN, "Name::"));
			//displayAlert();
		}
	}
}
