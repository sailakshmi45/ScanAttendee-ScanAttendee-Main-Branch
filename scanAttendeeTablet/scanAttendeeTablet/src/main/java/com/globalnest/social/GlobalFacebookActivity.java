package com.globalnest.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.globalnest.scanattendee.R;

import org.json.JSONObject;

import java.net.URL;
import java.util.Arrays;
import java.util.List;


public class GlobalFacebookActivity extends Activity {

	/*private UiLifecycleHelper uiHelper;
	LoginButton loginFB;*/
	boolean isLogin;
	private boolean pendingPublishReauthorization = false,isUpdated = false;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private ProgressDialog pDialog;
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";


	private CallbackManager callbackManager;
	private AccessTokenTracker accessTokenTracker;
	private ProfileTracker profileTracker;
	private LoginButton loginFB;
	private URL profilePicture;
	private String userId;
	private String TAG = "LoginActivity";


	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(GlobalFacebookActivity.this);
		setContentView(R.layout.main);
		callbackManager = CallbackManager.Factory.create();
		loginFB = (LoginButton) findViewById(R.id.authButton);
		isLogin = getIntent().getExtras().getBoolean("isLogin");
		boolean isLogout = getIntent().getExtras().getBoolean("LogOut");
		String shareTxt = getIntent().getExtras().getString("share");
		/*pDialog = new ProgressDialog(GlobalFacebookActivity.this);
		pDialog.setMessage("Loading fb...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();*/
		pDialog = new ProgressDialog(GlobalFacebookActivity.this);
		if (savedInstanceState != null) {
			pendingPublishReauthorization =
					savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}
		if(shareTxt!=null && shareTxt.equals("share")){
			final String message = getIntent().getExtras().getString("message");
			final String imageUrl = getIntent().getExtras().getString("imageUrl");
			final String linkUrl = getIntent().getExtras().getString("linkUrl");
			final String name = getIntent().getExtras().getString("name");
			final String desc = getIntent().getExtras().getString("desc");
			if(hasPublishPermission()){
				shareIt(name,desc,message, linkUrl,imageUrl);
			}else{
				// We need to get new permissions, then complete the action when we get called back.
				LoginManager.getInstance().logInWithPublishPermissions(this,PERMISSIONS);


				LoginManager manager = LoginManager.getInstance();
				manager.logInWithPublishPermissions(
						GlobalFacebookActivity.this,
						PERMISSIONS);

				manager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
					@Override
					public void onSuccess(LoginResult loginResult) {
						shareIt(name,desc,message, linkUrl,imageUrl);
					}

					@Override
					public void onCancel() {
						finish();
					}

					@Override
					public void onError(FacebookException error) {
						finish();
					}
				});
			}
			//		shareToFB(name,desc,message, linkUrl,imageUrl);
			//shareToFacebook(name,desc,message, linkUrl,imageUrl, Session.getActiveSession(), new ProgressDialog(GlobalFacebookSDKActivity.this), GlobalFacebookSDKActivity.this);
		}else if(isLogout){
			LoginManager.getInstance().logOut();
			Intent resultIntent = new Intent();
			resultIntent.putExtra("type","Logout");
			setResult(503, resultIntent);
			finish();
		}else{
			//loginFB.performClick();

			FacebookCallback<LoginResult> callback = new FacebookCallback<LoginResult>() {
				@Override
				public void onSuccess(LoginResult loginResult) {
					GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
						@Override
						public void onCompleted(JSONObject object, GraphResponse response) {
							Log.e(TAG,object.toString());
							Log.e(TAG,response.toString());

							try {
								userId = object.getString("id");
								/*profilePicture = new URL("https://graph.facebook.com/" + userId + "/picture?width=500&height=500");
								if(object.has("first_name"))
									firstName = object.getString("first_name");
								if(object.has("last_name"))
									lastName = object.getString("last_name");
								if (object.has("email"))
									email = object.getString("email");
								if (object.has("birthday"))
									birthday = object.getString("birthday");
								if (object.has("gender"))
									gender = object.getString("gender");

								Intent main = new Intent(LoginActivity.this,MainActivity.class);
								main.putExtra("name",firstName);
								main.putExtra("surname",lastName);
								main.putExtra("imageUrl",profilePicture.toString());
								startActivity(main);
								finish();*/

								Intent resultIntent = new Intent();
								resultIntent.putExtra("userName",object.getString("first_name")+" "+object.getString("last_name"));
								resultIntent.putExtra("type","Login");
								setResult(503, resultIntent);
								finish();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					//Here we put the requested fields to be returned from the JSONObject
					Bundle parameters = new Bundle();
					parameters.putString("fields", "id, first_name, last_name, email, birthday, gender");
					request.setParameters(parameters);
					request.executeAsync();
				}

				@Override
				public void onCancel() {
					finish();
				}

				@Override
				public void onError(FacebookException e) {
					e.printStackTrace();
					finish();
				}
			};
			//loginFB.setReadPermissions("email", "user_birthday","user_posts");
			// We need to get new permissions, then complete the action when we get called back.

			loginFB.registerCallback(callbackManager, callback);
			//loginFB.setOnClickListener((View.OnClickListener) this);
			loginFB.performClick();
			//LoginManager.getInstance().logInWithReadPermissions( this, Arrays.asList("user_photos", "email", "user_birthday", "public_profile") );
			//getPublishPermissions();
		}

	}

	private boolean hasPublishPermission() {
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		return accessToken != null && accessToken.getPermissions().contains("publish_actions");
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();


	}


	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		super.onActivityResult(requestCode, responseCode, intent);
		callbackManager.onActivityResult(requestCode, responseCode, intent);
	}

	public void shareIt(String name,String desc,String message,String  linkUrl,String imageUrl){
		try {
			pDialog.setMessage("Sharing on Facebook...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			Bundle postParams = new Bundle();
			postParams.putString("message", name+"\n"+desc+"\n"+ linkUrl);
			postParams.putString("name", name);
			postParams.putString("description",desc);
			if(imageUrl !=null && imageUrl.length()>0)
				postParams.putString("link", imageUrl);
			new GraphRequest(
					AccessToken.getCurrentAccessToken(), "/me/feed", postParams, HttpMethod.POST,
					new GraphRequest.Callback() {
						public void onCompleted(GraphResponse response) {
							if(pDialog !=null && pDialog.isShowing()){
								pDialog.dismiss();
							}
							if(response.getError() !=null && response.getError().getErrorMessage()!=null){
								Toast.makeText(GlobalFacebookActivity.this,response.getError().getErrorMessage(),Toast.LENGTH_LONG).show();

							}else{
								Toast.makeText(GlobalFacebookActivity.this,"Posted Successfully",Toast.LENGTH_LONG).show();

							}
							finish();
						}

					}
			).executeAsync();

		}catch (Exception e){
			e.printStackTrace();
		}


	}
/*
	public void shareToFB(String name,String desc,String message,String  linkUrl,String imageUrl ){
		try {
			//ShareDialog shareDialog = new ShareDialog(this);

			*//*ShareLinkContent content = new ShareLinkContent.Builder()

					.setContentTitle(name)
					.setContentDescription(desc)
					.setContentUrl(Uri.parse(linkUrl))
					//.setImageUrl(Uri.parse(imageUrl))

					.build();*//*

			//shareDialog.show(content);

			pDialog.setMessage("Loading...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
			if(linkUrl !=null && linkUrl.length()>0 && imageUrl !=null && imageUrl.length()>0 ){
				ShareLinkContent linkContent = new ShareLinkContent.Builder()
						.setContentTitle(name)
						.setContentDescription(desc)
						.setContentUrl(Uri.parse(linkUrl))
						.setImageUrl(Uri.parse(imageUrl))
						.build();
				if(hasPublishPermission())
					ShareApi.share(linkContent, shareCallback);
			}else if(linkUrl !=null && linkUrl.length()>0  ){
				ShareLinkContent linkContent = new ShareLinkContent.Builder()
						.setContentTitle(name)
						.setContentDescription(desc)
						.setContentUrl(Uri.parse(linkUrl))
						.build();
				if(hasPublishPermission())
					ShareApi.share(linkContent, shareCallback);
			}else if(imageUrl !=null && imageUrl.length()>0  ){
				ShareLinkContent linkContent = new ShareLinkContent.Builder()
						.setContentTitle(name)
						.setContentDescription(desc)
						.setContentUrl(Uri.parse("https://www.eventdex.com/"))
						.build();
				if(hasPublishPermission())
					ShareApi.share(linkContent, shareCallback);
			}else {
				ShareLinkContent linkContent = new ShareLinkContent.Builder()
						.setContentTitle(name)
						.setContentDescription(desc)
						.build();
				if(hasPublishPermission())
					ShareApi.share(linkContent, shareCallback);
			}


		}catch (Exception e){
			e.printStackTrace();
		}
	}
	private boolean hasPublishPermission() {
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		return accessToken != null && accessToken.getPermissions().contains("publish_actions");
	}
	private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
		@Override
		public void onCancel() {
			if(pDialog !=null &&pDialog.isShowing())
				pDialog.dismiss();
			Log.d("HelloFacebook", "Canceled");
			finish();
		}

		@Override
		public void onError(FacebookException error) {
			if(pDialog !=null &&pDialog.isShowing())
				pDialog.dismiss();
			Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
			String title = "Error";
			String alertMessage = error.getMessage();
			showResult(title, alertMessage);
			finish();
		}

		@Override
		public void onSuccess(Sharer.Result result) {
			if(pDialog !=null &&pDialog.isShowing())
				pDialog.dismiss();
			Log.d("HelloFacebook", "Success!");
			if (result.getPostId() != null) {
				String title = "success";
				String id = result.getPostId();
				String alertMessage = "Successfully Posted";
				showResult(title, alertMessage);
			}
			finish();
		}

		private void showResult(String title, String alertMessage) {
			*//*new AlertDialog.Builder(HelloFacebookSampleActivity.this)
					.setTitle(title)
					.setMessage(alertMessage)
					.setPositiveButton(R.string.ok, null)
					.show();*//*
			Toast.makeText(GlobalFacebookActivity.this,alertMessage,Toast.LENGTH_LONG).show();
		}
	};*/
}
