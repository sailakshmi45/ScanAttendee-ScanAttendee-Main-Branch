package com.globalnest.scanattendee;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.globalnest.classes.RoundedImageView;
import com.globalnest.network.HttpClientClass;
import com.globalnest.objects.EventObjects;
import com.globalnest.social.ConfigLinkedIn;
import com.globalnest.social.GlobalFacebookActivity;
import com.globalnest.social.LinkedInActivity;
import com.globalnest.social.ShareLinkedInStatusOAuth2;
import com.globalnest.social.ShareTwitterStatus;
import com.globalnest.social.SocialConstats;
import com.globalnest.social.TwitterActivity;
import com.globalnest.utils.AlertDialogCustom;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SocialMedia extends BaseActivity implements OnClickListener {

	// private String event_id="";
	private Button twitter_btn, linkedIn, loginFb,logoutFB,logoutLinkedIn,logoutTwitter,btn_close;
	// private LoginButton loginFB;
	private RoundedImageView img_logo;
	private TextView txtHeader, txtTwitter, txtLinkedIn, txtFb, eventUrl,
			txt_eventname, txt_startdat, txt_location, txt_country, txt_desc,txt_enddate;
	// private LinearLayout messageLayout;
	private String type = "";
	// private EditText shareTxt;
	private ProgressDialog pDialog;
	AppUtils objAppUtill;
	String link = "", imageLink = "", eventname = "", desc = "";
	String message = "", hyper_link = "";
	// Twitter
	private String consumerKey = null;
	private String consumerSecret = null;
	private String callbackUrl = null;
	// private String oAuthVerifier = null;
	private EventObjects checkedin_event_record = new EventObjects();

	LinkedInActivity objLinkedIn = new LinkedInActivity();
	/* Any number for uniquely distinguish your request */
	public static final int WEBVIEW_REQUEST_CODE = 100;
	private static SharedPreferences mSharedPreferences;

	/* Shared preference keys */

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * try {
		 * 
		 * PackageInfo info =
		 * getPackageManager().getPackageInfo(getPackageName(),
		 * PackageManager.GET_SIGNATURES);
		 * 
		 * for (Signature signature : info.signatures) { MessageDigest md =
		 * MessageDigest.getInstance("SHA"); md.update(signature.toByteArray());
		 * Log.d("KeyHash:", Base64.encodeToString(md.digest(),
		 * Base64.DEFAULT)); }
		 * 
		 * } catch (NameNotFoundException e) { Log.e("name not found",
		 * e.toString()); } catch (NoSuchAlgorithmException e) {
		 * Log.e("no such an algorithm", e.toString()); }
		 */

		/* Enabling strict mode */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		setCustomContentView(R.layout.activity_social_media);

		objAppUtill = new AppUtils();
		if(getIntent().getStringExtra(Util.ADDED_EVENT_ID)!=null){
			checkedin_event_record = Util.db.getSelectedEventRecord(getIntent().getStringExtra(Util.ADDED_EVENT_ID));
			//checkedin_event_record = selected_event;
		}
		txt_eventname.setText(checkedin_event_record.Events.Name);
		txt_desc.setText(checkedin_event_record.Events.Description__c);
		txt_location.setText(checkedin_event_record.Events.Venue_Name__c);
		txt_country.setText(checkedin_event_record.state + ", "
				+ checkedin_event_record.country);
		String event_start_date = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.Start_Date__c,
				checkedin_event_record.Events.Time_Zone__c);
		String event_end_date = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c,
				checkedin_event_record.Events.Time_Zone__c);
		txt_startdat.setText(event_start_date);
		txt_enddate.setText(event_end_date);

		if (checkedin_event_record.image != null && checkedin_event_record.image.length() > 0)
			Picasso.with(SocialMedia.this).load(checkedin_event_record.image)
					.placeholder(R.drawable.default_image)
					.error(R.drawable.default_image).into(img_logo);
		if (!checkedin_event_record.RegistrationLink.isEmpty()
				|| checkedin_event_record.RegistrationLink != null) {
			Spanned hyper_link1 = Html
					.fromHtml("<a href=http://www.google.com>Google</a>");
			//Log.i("-----Spanded Text-----", hyper_link + " " + hyper_link1);

			eventUrl.setText(checkedin_event_record.RegistrationLink);
			eventUrl.setTextColor(getResources().getColor(
					R.color.blue_text_color));
		}
		link = eventUrl.getText().toString();
		hyper_link = link;
		// link =selected_event.RegistrationLink;
		if (!checkedin_event_record.image.contains("id=null"))
			imageLink = checkedin_event_record.image;
		else
			imageLink = "http://www.socketmobile.com/images/default-source/software-partners/logo_scanattendee.png?sfvrsn=0";
		message = "Eventdex.com/" + checkedin_event_record.Events.Name.replace(" ", "");
		eventname = checkedin_event_record.Events.Name;
		String event_start_date_string = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.Start_Date__c, checkedin_event_record.Events.Time_Zone__c);
		String event_end_date_string = Util.change_US_ONLY_DateFormat(checkedin_event_record.Events.End_Date__c, checkedin_event_record.Events.Time_Zone__c);


		desc = "From " + event_start_date_string +" \n to "
				+ event_end_date_string + " \n"
				+ checkedin_event_record.Events.Venue_Name__c+" \n"
				+ Util.db.getStateName(checkedin_event_record.Events.BLN_State__c)  + " "
				+ checkedin_event_record.Events.ZipCode__c;

		loginFb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isFBLogin()) {
					Intent i = new Intent(SocialMedia.this,
							GlobalFacebookActivity.class);
					// i.setType("text/html");
					i.putExtra("isLogin", isFBLogin());
					i.putExtra("share", "share");
					i.putExtra("message", hyper_link);
					i.putExtra("imageUrl", imageLink);
					i.putExtra("linkUrl", link);
					i.putExtra("name", txt_eventname.getText().toString());
					i.putExtra("caption", "Powerd By Eventdex");
					i.putExtra("desc", desc);

					startActivityForResult(i, 503);
				} else {
					Intent i = new Intent(SocialMedia.this,
							GlobalFacebookActivity.class);
					i.putExtra("isLogin", isFBLogin());
					startActivityForResult(i, 503);
				}


			}
		});

		if (isTwitterLogin()) {
			// twitter_btn_info.setVisibility(View.VISIBLE);
			txtTwitter.setText("LogOut");
			//twitter_btn.setAlpha(3.0f);
		}
		if (isLinkedInLogin()) {
			// linkedIn_info.setVisibility(View.VISIBLE);
			txtLinkedIn.setText("LogOut");
			//txtLinkedIn.setAlpha(3.0f);
		}
		if (isFBLogin()) {
			// fb_info.setVisibility(View.VISIBLE);
			txtFb.setText("LogOut");
			//txtFb.setAlpha(3.0f);
		}

		back_layout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (NullChecker(getIntent().getStringExtra("IS_FROM")).equals(
						"AddTicketActivity")) {
					Intent i = new Intent(SocialMedia.this,
							ManageTicketActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);

				} else {
					finish();
				}
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (NullChecker(getIntent().getStringExtra("IS_FROM")).equals("AddTicketActivity")) {
				Intent i = new Intent(SocialMedia.this,ManageTicketActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			} else {
				finish();
			}

		}
		return false;
	}

	@SuppressLint("NewApi")
	private void initViews() {}

	@SuppressLint("NewApi")
	private void enableShare() {
		// messageLayout.setAlpha(1.0f);
		// share.setEnabled(true);
		// shareTxt.setEnabled(true);
	}

	@SuppressLint("NewApi")
	private void disableShare() {
		// if(!isTwitterLogin()&& !isFBLogin() && !isLinkedInLogin()){
		// messageLayout.setAlpha(0.5f);
		// shareTxt.setText("");
		// share.setEnabled(false);
		// shareTxt.setEnabled(false);
		// }

	}

	private boolean isTwitterLogin() {
		return mSharedPreferences.getBoolean(
				SocialConstats.PREF_KEY_TWITTER_LOGIN, false);

	}

	private boolean isFBLogin() {
		return mSharedPreferences.getBoolean(
				SocialConstats.PREF_KEY_FACEBOOK_LOGIN, false);

	}

	private boolean isLinkedInLogin() {
		return mSharedPreferences.getBoolean(
				SocialConstats.PREF_KEY_LINKEDIN_LOGIN, false);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// uiHelper.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			// String verifier = data.getExtras().getString(oAuthVerifier);
			String userName = data.getExtras().getString("userName");
			String accessToken = data.getExtras().getString("accessToken");
			String secretToken = data.getExtras().getString("secretToken");
			try {

				// twitter_btn_info.setVisibility(View.VISIBLE);
				saveTwitterInfo(userName, accessToken, secretToken);
				new ShareTwitterStatus(SocialMedia.this).execute(message,
						consumerKey, consumerSecret, mSharedPreferences
								.getString(SocialConstats.PREF_KEY_OAUTH_TOKEN,
										""), mSharedPreferences.getString(
								SocialConstats.PREF_KEY_OAUTH_SECRET, ""),
						hyper_link, imageLink);

			} catch (Exception e) {
				// Log.e("Twitter Login Failed", e.getMessage());
			}
		} else if (resultCode == 502) {// linkedin
			if(data!=null){
				String userName = data.getStringExtra("userName");
				String accessToken = data.getStringExtra("accesstoken");
				//String secretToken = data.getStringExtra("secrettoken");
				saveLinkedInfo(userName, accessToken, "");
				message = eventname + " \n" + desc;
				new ShareLinkedInStatusOAuth2(SocialMedia.this).execute(message,ConfigLinkedIn.LINKEDIN_CONSUMER_KEY,
						ConfigLinkedIn.LINKEDIN_CONSUMER_SECRET,
						mSharedPreferences.getString(SocialConstats.PREF_KEY_OAUTH_TOKEN_LINKEDIN, ""),
						mSharedPreferences.getString(""/*SocialConstats.PREF_KEY_OAUTH_SECRET_LINKEDIN*/, ""),link, imageLink);
			}else{
				AlertDialogCustom dialog=new AlertDialogCustom(SocialMedia.this);
				dialog.setParamenters("Error !", "LinkedIn login failed .", null, null, 1, false);
				dialog.setAlertImage(R.drawable.error, "");
				dialog.show();
			}

		} else if (resultCode == 503) {// facebook
			String type = data.getExtras().getString("type");
			if (type.equals("Login")) {
				String userName = data.getExtras().getString("userName");
				Editor e = mSharedPreferences.edit();
				e.putString(SocialConstats.PREF_USER_NAME_FACEBOOK, userName);
				e.putBoolean(SocialConstats.PREF_KEY_FACEBOOK_LOGIN, true);
				e.commit();

				Intent i = new Intent(SocialMedia.this,
						GlobalFacebookActivity.class);
				i.putExtra("isLogin", isFBLogin());
				i.putExtra("share", "share");
				i.putExtra("message", link);
				i.putExtra("imageUrl", imageLink);
				i.putExtra("linkUrl", link);
				i.putExtra("name", txt_eventname.getText().toString());
				i.putExtra("caption", "Powerd By Globalnest");
				i.putExtra("desc", desc);
				startActivityForResult(i, 503);

			} else {

			}

		}
		setLogout();
		super.onActivityResult(requestCode, resultCode, data);
	}

	/* Reading twitter essential configuration parameters from strings.xml */
	private void initTwitterConfigs() {
		consumerKey = getString(R.string.twitter_consumer_key);
		consumerSecret = getString(R.string.twitter_consumer_secret);
		callbackUrl = getString(R.string.twitter_callback);
		// oAuthVerifier = getString(R.string.twitter_oauth_verifier);
	}

	/**
	 * Saving user information, after user is authenticated for the first time.
	 * You don't need to show user to login, until user has a valid access toen
	 */
	private void saveTwitterInfo(String userName, String accessToken,
								 String secretToken) {

		try {

			/* Storing oAuth tokens to shared preferences */
			Editor e = mSharedPreferences.edit();
			e.putString(SocialConstats.PREF_KEY_OAUTH_TOKEN, accessToken);
			e.putString(SocialConstats.PREF_KEY_OAUTH_SECRET, secretToken);
			e.putBoolean(SocialConstats.PREF_KEY_TWITTER_LOGIN, true);
			e.putString(SocialConstats.PREF_USER_NAME_TWITTER, userName);
			e.commit();
			txtTwitter.setText("LogOut");

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void saveLinkedInfo(String userName, String accessToken,
								String secretToken) {

		try {

			/* Storing oAuth tokens to shared preferences */
			Editor e = mSharedPreferences.edit();
			e.putString(SocialConstats.PREF_KEY_OAUTH_TOKEN_LINKEDIN,accessToken);
			e.putString(SocialConstats.PREF_KEY_OAUTH_SECRET_LINKEDIN,secretToken);
			e.putString(SocialConstats.PREF_USER_NAME_LINKEDIN, userName);
			e.putBoolean(SocialConstats.PREF_KEY_LINKEDIN_LOGIN, true);
			e.commit();

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/*class LogoutLinkedInAsync extends AsyncTask<String, String, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			pDialog = new ProgressDialog(SocialMedia.this);
			pDialog.setMessage("Loading...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected Boolean doInBackground(String... args) {

			try {

				return objLinkedIn.linkedInLogout(
						ConfigLinkedIn.LINKEDIN_CONSUMER_KEY,
						ConfigLinkedIn.LINKEDIN_CONSUMER_SECRET,
						mSharedPreferences.getString(
								SocialConstats.PREF_KEY_OAUTH_TOKEN_LINKEDIN,
								""), mSharedPreferences.getString(
								SocialConstats.PREF_KEY_OAUTH_SECRET_LINKEDIN,
								""));
				// return linkedInLogout();
			} catch (Exception e) {
				Log.d("Failed to logout", e.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			 Dismiss the progress dialog after sharing 
			pDialog.dismiss();
			if (result) {
				// shareTxt.setText("");
				// linkedIn_info.setVisibility(View.INVISIBLE);
				// name.setText("");
				txtHeader.setText("");
				txtHeader.setText("Login with socials");
				txtHeader.setTextColor(Color.parseColor("#000000"));
				// .setVisibility(View.INVISIBLE);
				txtLinkedIn.setText("LinkedIn");
				Editor e = mSharedPreferences.edit();
				e.putBoolean(SocialConstats.PREF_KEY_LINKEDIN_LOGIN, false);
				e.commit();
				disableShare();
				Toast.makeText(SocialMedia.this,
						"LinkedIn logout successfully", Toast.LENGTH_SHORT)
						.show();
			} else {

				Toast.makeText(SocialMedia.this, "unable to logout",
						Toast.LENGTH_SHORT).show();
			}

			// Clearing EditText field
			// mShareEditText.setText("");
		}

	}
*/


	@SuppressLint("NewApi")
	@Override
	public void onClick(View v) {
		Editor e;
		switch (v.getId()) {

			case R.id.btn_twitter:

				if (objAppUtill.isOnline(SocialMedia.this)) {
					type = "twitter";
					//twitter_btn.setAlpha(1.0f);
					//linkedIn.setAlpha(0.5f);
					initTwitterConfigs();
				/* Check if required twitter keys are set */
					if (TextUtils.isEmpty(consumerKey)
							|| TextUtils.isEmpty(consumerSecret)) {
						Toast.makeText(SocialMedia.this,
								"Twitter key and secret not configured",
								Toast.LENGTH_SHORT).show();
						return;
					}

				/*
				 * if already logged in, then hide login layout and show share
				 * layout
				 */
					if (isTwitterLogin()) {
						new ShareTwitterStatus(SocialMedia.this).execute(message,
								consumerKey, consumerSecret,
								mSharedPreferences.getString(
										SocialConstats.PREF_KEY_OAUTH_TOKEN, ""),
								mSharedPreferences.getString(
										SocialConstats.PREF_KEY_OAUTH_SECRET, ""),
								hyper_link, imageLink);

						// displayAlert();
					} else {

						Uri uri = getIntent().getData();

						if (uri != null && uri.toString().startsWith(callbackUrl)) {


						}

						final Intent intent = new Intent(this,
								TwitterActivity.class);
						intent.putExtra(getString(R.string.consumerKey),
								consumerKey);
						intent.putExtra(getString(R.string.consumersecret),
								consumerSecret);
						intent.putExtra(getString(R.string.callback), callbackUrl);
						startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
						// loginToTwitter();
					}
					setLogout();
				} else {
					Toast.makeText(SocialMedia.this, "please connect to internet",
							Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btn_linkedin:
				if (objAppUtill.isOnline(SocialMedia.this)) {
					type = "linkedin";
					//linkedIn.setAlpha(1.0f);
					//twitter_btn.setAlpha(0.5f);
					if (isLinkedInLogin()) {

						message = eventname + " \n" + desc;
						new ShareLinkedInStatusOAuth2(SocialMedia.this).execute(message,ConfigLinkedIn.LINKEDIN_CONSUMER_KEY,
								ConfigLinkedIn.LINKEDIN_CONSUMER_SECRET,
								mSharedPreferences.getString(SocialConstats.PREF_KEY_OAUTH_TOKEN_LINKEDIN, ""),
								mSharedPreferences.getString(""/*SocialConstats.PREF_KEY_OAUTH_SECRET_LINKEDIN*/, ""),link, imageLink);
					} else {
						final Intent intent = new Intent(this, LinkedInActivity.class);
						intent.putExtra(getString(R.string.consumerKey),
								ConfigLinkedIn.LINKEDIN_CONSUMER_KEY);
						intent.putExtra(getString(R.string.consumersecret),
								ConfigLinkedIn.LINKEDIN_CONSUMER_SECRET);
						intent.putExtra(getString(R.string.callback),
								ConfigLinkedIn.OAUTH_CALLBACK_URL);
						intent.putExtra(getString(R.string.scope),
								ConfigLinkedIn.SCOPE_PARAMS);
						startActivityForResult(intent, 502);
					}
					setLogout();

				} else {
					Toast.makeText(SocialMedia.this, "please connect to internet",
							Toast.LENGTH_SHORT).show();
				}

				break;

			case R.id.btn_fb_logou :
				e = mSharedPreferences.edit();
				e.putBoolean(SocialConstats.PREF_KEY_FACEBOOK_LOGIN, false);
				e.commit();
				setLogout();
				break;
			case R.id.btn_linkedin_logout :
				e = mSharedPreferences.edit();
				e.putBoolean(SocialConstats.PREF_KEY_LINKEDIN_LOGIN, false);
				e.commit();
				setLogout();
				break;
			case R.id.btn_twitter_logout :
				TwitterActivity objTwitter = new TwitterActivity();
				//objTwitter.logOut();
				e = mSharedPreferences.edit();
				e.putBoolean(SocialConstats.PREF_KEY_TWITTER_LOGIN, false);
				e.commit();
				setLogout();
				break;
			default:
				break;
		}

	}

	public String getShortURL(String longUrl) {
		// Making HTTP request
		String short_url = "";
		try {
			// DefaultHttpClient
			HttpClient httpClient = HttpClientClass.getHttpClient(30000);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#doRequest()
	 */
	@Override
	public void doRequest() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.globalnest.network.IPostResponse#parseJsonResponse(java.lang.String)
	 */
	@Override
	public void parseJsonResponse(String response) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.globalnest.scanattendee.BaseActivity#setCustomContentView(int)
	 */
	@Override
	public void setCustomContentView(int layout) {
		setContentView(R.layout.activity_social_media);
		mSharedPreferences = getSharedPreferences(SocialConstats.PREF_NAME, 0);
		txt_title.setText("Publish the event");
		img_menu.setImageResource(R.drawable.back_button);
		twitter_btn = (Button) findViewById(R.id.btn_twitter);
		linkedIn = (Button) findViewById(R.id.btn_linkedin);
		txtHeader = (TextView) findViewById(R.id.txt_header);
		txtTwitter = (TextView) findViewById(R.id.txt_twitter);
		txtLinkedIn = (TextView) findViewById(R.id.txt_linkedin);
		txtFb = (TextView) findViewById(R.id.txt_fb);
		img_logo = (RoundedImageView) findViewById(R.id.img_event_logo);
		eventUrl = (TextView) findViewById(R.id.event_url);
		loginFb = (Button) findViewById(R.id.login);
		btn_close = (Button) findViewById(R.id.close_button);
		logoutFB = (Button) findViewById(R.id.btn_fb_logou);
		logoutLinkedIn = (Button) findViewById(R.id.btn_linkedin_logout);
		logoutTwitter = (Button) findViewById(R.id.btn_twitter_logout);

		// initialization of event view

		txt_eventname = (TextView) findViewById(R.id.txt_eventname);
		txt_startdat = (TextView) findViewById(R.id.txt_startdate);
		txt_enddate=(TextView) findViewById(R.id.txt_enddate);
		txt_location = (TextView) findViewById(R.id.txt_eventloc);
		txt_country = (TextView) findViewById(R.id.txt_eventcitystate);
		txt_desc = (TextView) findViewById(R.id.txt_eventdesc);

		linkedIn.setOnClickListener(this);
		twitter_btn.setOnClickListener(this);

		loginFb.setOnClickListener(this);
		logoutLinkedIn.setOnClickListener(this);
		logoutTwitter.setOnClickListener(this);

		logoutFB.setEnabled(false);
		logoutLinkedIn.setEnabled(false);
		logoutTwitter.setEnabled(false);

		setLogout();
		btn_close.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void setLogout(){
		if(isFBLogin()){
			logoutFB.setEnabled(true);
			//loginFb.setAlpha(3.0f);
			logoutFB.setTextColor(getResources().getColor(R.color.fb_color));
		}else{
			logoutFB.setEnabled(false);
			//loginFb.setAlpha(0.0f);
			logoutFB.setTextColor(getResources().getColor(R.color.light_gray));
		}if(isLinkedInLogin()){
			logoutLinkedIn.setEnabled(true);
			//linkedIn.setAlpha(3.0f);
			logoutLinkedIn.setTextColor(getResources().getColor(R.color.linkedin_color));
		}else{
			logoutLinkedIn.setEnabled(false);
			//linkedIn.setAlpha(0.0f);
			logoutLinkedIn.setTextColor(getResources().getColor(R.color.light_gray));
		}if(isTwitterLogin()){
			logoutTwitter.setEnabled(true);
			//twitter_btn.setAlpha(3.0f);
			logoutTwitter.setTextColor(getResources().getColor(R.color.twitter_color));
		}else{
			logoutTwitter.setEnabled(false);
			//twitter_btn.setAlpha(0.0f);
			logoutTwitter.setTextColor(getResources().getColor(R.color.light_gray));
		}
	}

}

