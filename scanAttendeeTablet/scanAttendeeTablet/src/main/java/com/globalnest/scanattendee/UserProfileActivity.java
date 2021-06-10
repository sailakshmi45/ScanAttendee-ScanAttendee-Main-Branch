//  ScanAttendee Android
//  Created by Ajay
//  This class is used to edit user profile and saved on the backend, followed by dashboard screen
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.scanattendee;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.globalnest.BackgroundReciver.DownloadService;
import com.globalnest.classes.RoundedImageView;
import com.globalnest.cropimage.CropImage;
import com.globalnest.cropimage.CropUtil;
import com.globalnest.network.HttpPostData;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.objects.UserObjects;
import com.globalnest.utils.Util;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class UserProfileActivity extends BaseActivity implements OnClickListener{
	
	EditText edit_fname, edit_lname, edit_mobile, edit_comp,
	edit_city;
	TextView txt_email;
	Spinner edit_state, edit_country;
	RoundedImageView profile_img;
	Dialog ask_dialog;
	Bitmap profile_photo;
	Button btn_edit_company;
	boolean isOnCreate;
	String email = "", fname = "", lname = "", city = "", state = "",state_id="", country_id="",
			mobile = "", company = "", country="";
	//SpinnerArrayAdapter adaptercountry,indianstates, americanstates;;
	ArrayAdapter<String> adaptercountry,indianstates;
	private final int CREATE_LOGOUT_DIALOG=1;
//	private String company_name="";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCustomContentView(R.layout.user_profile_layout);
		
		edit_country.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// 
           String country = (String)edit_country.getSelectedItem();
				setStateAdapter(country);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// 

			}
		});
		
		back_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//
				if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(EventListActivity.class.getName())){
					Intent i = new Intent(UserProfileActivity.this,EventListActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra(Util.EVENT_LIST, Util.EVENT_LIST);
					startActivity(i);
					finish();
				}else{
					Intent i = new Intent(UserProfileActivity.this,DashboardActivity.class);
					i.putExtra("CheckIn Event", checkedin_event_record);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					i.putExtra(Util.HOME, "home_layout");
					startActivity(i);
					finish();
				}
			}
		});
		
		btn_edit_company.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				openCompanyEditDialog();
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/*if (Util.socket_device_pref.getBoolean(Util.SOCKET_DEVICE_CONNECTED, false)) {
			img_scanner_base.setBackgroundResource(R.drawable.green_circle_1);
		} else {
			img_scanner_base.setBackgroundResource(R.drawable.red_circle_1);
		}*/
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
	   View view = getCurrentFocus();
	   boolean ret = super.dispatchTouchEvent(event);

	   if (view instanceof EditText) {
	       View w = getCurrentFocus();
	       int scrcoords[] = new int[2];
	       w.getLocationOnScreen(scrcoords);
	       float x = event.getRawX() + w.getLeft() - scrcoords[0];
	       float y = event.getRawY() + w.getTop() - scrcoords[1];

	       if (event.getAction() == MotionEvent.ACTION_UP
	&& (x < w.getLeft() || x >= w.getRight()
	|| y < w.getTop() || y > w.getBottom()) ) {
	           InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	           imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
	       }
	   }
	return ret;
	}
	
	@Override
	public void doRequest() {
		
		String access_token = sfdcddetails.token_type+" "+sfdcddetails.access_token;
		profile_img.setDrawingCacheEnabled(true);
	    profile_img.buildDrawingCache();
		profile_photo=profile_img.getDrawingCache();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(profile_photo!=null)
		profile_photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		String image_string = Base64.encodeToString(data, Base64.NO_WRAP);
		postMethod = new HttpPostData("Updating Profile Info...",setProfileUrl(),image_string, access_token, UserProfileActivity.this);
		postMethod.execute();
	}
	private String setProfileUrl() {
		if(state_id==null){
			state_id="";
		}
		/*company_name = edit_comp.getText().toString().trim();
		String company_id="";
		if(!user_profile.Profile.Company_Name__c.equalsIgnoreCase(company_name)){
			user_profile.Profile.Company_Name__c = company_name;
		}else{
			company_id = user_profile.Profile.Default_Company_ID__c; 
		}*/
		//Log.i("--------------Existing Company Name----------------", ":"+user_profile.Profile.Company_Name__c+":"+user_profile.Profile.Default_Company_ID__c);
		String values = "Firstname="
				+ user_profile.Profile.First_Name__c.replaceAll(" ", "%20")
				+ "&Lastname="+ user_profile.Profile.Last_Name__c.replaceAll(" ", "%20") 
				+ "&Email="+ user_profile.Profile.Email__c 
				+ "&Mobile="+ user_profile.Profile.Mobile__c.replaceAll(" ", "%20")
				+ "&Companyname="+ user_profile.Profile.Company_Name__c.replaceAll(" ", "%20")
				+ "&DefaultcompanyId="+ user_profile.Profile.Default_Company_ID__c
				+ "&City="+ user_profile.Profile.City__c.replaceAll(" ", "%20") 
				+ "&State="+ state_id 
				+ "&Country="+ country_id
				+ "&Userid="+ sfdcddetails.user_id;
		String url = sfdcddetails.instance_url+WebServiceUrls.SA_PROFILE_UPDATE_URL + values;

		return url;

	}
	@Override
	public void parseJsonResponse(String response) {
		// 
		
		try{
			if(!isValidResponse(response)){
				openSessionExpireAlert(errorMessage(response));
			}
			/*UserProfile user_profile_contoller = Util.user_profile_parser
					.ParseUser(new JSONObject(response));*/
			Gson usergson=new Gson();
			/*UserProfile user_profile_contoller=usergson.fromJson(response, UserProfile.class);
			//Log.i("--Profile Respose----",":"+user_profile_contoller.Email__c);
			
			Util.db.InsertAndUpdateUser(user_profile_contoller);*/


			UserObjects user_profile_contoller=usergson.fromJson(response, UserObjects.class);
			//Log.i("--Profile Respose----",":"+user_profile_contoller.Profile.Email__c);
			user_profile_contoller.Profile.Userid=sfdcddetails.user_id;
			user_profile_contoller.Profile.profileimage=user_profile_contoller.profileimage;
			user_profile_contoller.Profile.profilecountry = user_profile_contoller.profilecountry;
			user_profile_contoller.Profile.profilestate = user_profile_contoller.profilestate;
			user_profile_contoller.Profile.profileCity = user_profile_contoller.profileCity;
			Util.db.InsertAndUpdateUserProfile(user_profile_contoller);
			
			_showCustomToast("Profile updated successfully.",Gravity.CENTER);
			if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(EventListActivity.class.getName())){
				Intent i = new Intent(UserProfileActivity.this,EventListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.EVENT_LIST, Util.EVENT_LIST);
				startActivity(i);
				finish();
			}else{
				Intent i = new Intent(UserProfileActivity.this,DashboardActivity.class);
				i.putExtra("CheckIn Event", checkedin_event_record);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.HOME, "home_layout");
				startActivity(i);
				finish();
			}
			
		}catch(Exception e){
			e.printStackTrace();
			startErrorAnimation(getResources().getString(R.string.connection_error), txt_error_msg);
			
		}
		
	}

	@Override
	public void setCustomContentView(int layout) {
		activity = this;
		View v = inflater.inflate(layout, null);
		linearview.addView(v);
		txt_title.setText("Profile");
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		txt_title.setTextColor(getResources().getColor(R.color.white));
		img_setting.setVisibility(View.GONE);
		img_menu.setImageResource(R.drawable.back_white);
		addevent_layout.setVisibility(View.GONE);
		button_layout.setVisibility(View.VISIBLE);
		event_layout.setVisibility(View.GONE);
		lay_top_line.setVisibility(View.GONE);
		img_socket_scanner.setVisibility(View.GONE);
		img_scanner_base.setVisibility(View.GONE);
		txt_save.setText("LOGOUT");
		txt_save.setVisibility(View.VISIBLE);
		txt_save.setOnClickListener(this);
		top_layout.setBackgroundResource(R.color.green_button_color);
		back_layout.setBackgroundResource(R.color.green_button_color);
		
	
		profile_img = (RoundedImageView) linearview.findViewById(R.id.imageprofile);
		txt_email = (TextView) linearview.findViewById(R.id.txtprofileemail);
		edit_fname = (EditText) linearview.findViewById(R.id.profile_fname);
		edit_lname = (EditText) linearview.findViewById(R.id.profile_lname);
		edit_mobile = (EditText) linearview.findViewById(R.id.profile_mob);
		edit_comp = (EditText) linearview.findViewById(R.id.profile_comp);
		edit_city = (EditText) linearview.findViewById(R.id.profile_city);
		edit_state = (Spinner) linearview.findViewById(R.id.profile_state);
		edit_country = (Spinner) linearview.findViewById(R.id.profile_country);
		btn_edit_company = (Button)linearview.findViewById(R.id.btn_edit_company);
		
		txt_email.setTypeface(Util.roboto_regular);
		edit_fname.setTypeface(Util.roboto_regular);
		edit_lname.setTypeface(Util.roboto_regular);
		
		edit_mobile.setTypeface(Util.roboto_regular);
		edit_city.setTypeface(Util.roboto_regular);
		edit_comp.setTypeface(Util.roboto_regular);
		email = user_profile.Profile.Email__c;
		txt_email.setText(user_profile.Profile.Email__c);
	    edit_fname.setText(NullChecker(user_profile.Profile.First_Name__c));
		edit_lname.setText(NullChecker(user_profile.Profile.Last_Name__c));
		//Log.i("Profile Mobile Number--",Util.NullChecker(user_profile.Profile.Mobile__c));
		if(!Util.NullChecker(user_profile.Profile.Mobile__c).isEmpty())
		edit_mobile.setText(user_profile.Profile.Mobile__c);
		
		if (NullChecker(user_profile.Profile.Company_Name__c)
				.equalsIgnoreCase(NullChecker(user_profile.Profile.First_Name__c) + NullChecker(user_profile.Profile.Last_Name__c))) {
			edit_comp.setEnabled(true);
			edit_comp.setText("");
			btn_edit_company.setVisibility(View.GONE);
		} else {
			edit_comp.setEnabled(false);
			edit_comp.setText(NullChecker(user_profile.Profile.Company_Name__c));
			btn_edit_company.setVisibility(View.VISIBLE);
		}
		edit_city.setText(NullChecker(user_profile.Profile.City__c));
		profile_img.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		btn_save.setOnClickListener(this);
		
		//Log.i("----Profile COuntry----",":"+user_profile.profilecountry);
		//Log.i("----Profile State----",":"+user_profile.profilestate);
		//edit_country.setAdapter(adaptercountry);
		
		 

	        
		if (!user_profile.profileimage.isEmpty()) {
			
			String url[] =sfdcddetails.instance_url.split("/");	
			Picasso.with(UserProfileActivity.this).load(user_profile.profileimage)
			.placeholder(R.drawable.default_image)
			.error(R.drawable.default_image).into(profile_img);
			
		}	
		country_list = Util.db.getCountryList("");
		/*Arrays.sort(country_list, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				// TODO Auto-generated method stub
				return lhs.compareToIgnoreCase(rhs);
			}
		});*/
		adaptercountry = new ArrayAdapter<String>(this,
				R.layout.spinner_item, country_list){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                
                ((TextView) v).setTypeface(Util.roboto_regular);
                return v;
			}
		};
		edit_country.setAdapter(adaptercountry);
		edit_country.setSelection(0);
		for(int i=0; i<country_list.size(); i++){
			if(country_list.get(i).trim().equalsIgnoreCase(NullChecker(Util.db.getCountryName(user_profile.profilecountry)))){
				edit_country.setSelection(i);
				setStateAdapter(country_list.get(i));
			}
		}
		//}
	}
	
	private void setStateAdapter(String country){
		state_list = Util.db.getStateList(Util.db.getCountryId(country));
		//state_list[0]="--Select--";
		////Log.i("UserProfile", "States"+state_list[0]);
		
		indianstates = new ArrayAdapter<String>(this,R.layout.spinner_item, state_list){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(getResources().getColor(R.color.black));
                ((TextView) v).setTypeface(Util.roboto_regular);
                return v;
			}
		};
		edit_state.setAdapter(indianstates);
		if (!user_profile.profilestate.isEmpty()) {
			for (int i = 0; i < state_list.size(); i++) {
				if (state_list.get(i).equalsIgnoreCase(NullChecker(Util.db.getStateLongName(user_profile.profilestate))))
					edit_state.setSelection(i);
			}
		} else {
			edit_state.setSelection(0);
		}
		
	}
	
	private void openCompanyEditDialog(){
		Util.setCustomAlertDialog(UserProfileActivity.this);
		Util.txt_dismiss.setVisibility(View.VISIBLE);
		Util.txt_okey.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				edit_comp.setEnabled(true);
				edit_comp.requestFocus();
			}
		});
		Util.txt_dismiss.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ticket_dialog.dismiss();
				Util.alert_dialog.dismiss();
				
			}
		});
		Util.openCustomDialog("Warning!","By Updating your Company all your Existing Payment Gateways are attached with your Company will be Cleared, Do you want to Update the Company.");
	}

	private void _showToast(String message){
		Toast toast = Toast.makeText(UserProfileActivity.this,message, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		// 
		if(v == btn_cancel){
			
			if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(EventListActivity.class.getName())){
				Intent i = new Intent(UserProfileActivity.this,EventListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.EVENT_LIST, Util.EVENT_LIST);
				startActivity(i);
				finish();
			}else{
				Intent i = new Intent(UserProfileActivity.this,DashboardActivity.class);
				i.putExtra("CheckIn Event", checkedin_event_record);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.HOME, "home_layout");
				startActivity(i);
				finish();
			}
			
			//overridePendingTransition(R.anim.rotate_in, R.anim.rotate_out);
		}else if(v == profile_img){
			
			
			openTakeFromDialg(UserProfileActivity.this);
			  /*if (photo_slider.isOpened()) 
				photo_slider.animateClose();
			else 
				photo_slider.animateOpen();*/
		}else if(v == btn_save){
			
			fname = edit_fname.getText().toString().trim();
			lname = edit_lname.getText().toString().trim();
			city = edit_city.getText().toString().trim();
			state = Util.NullChecker(edit_state.getSelectedItem().toString());
			mobile = edit_mobile.getText().toString().trim();
			company = edit_comp.getText().toString().trim();
			country = edit_country.getSelectedItem().toString();
			country_id = Util.db.getCountryId(edit_country.getSelectedItem().toString());
			state = edit_state.getSelectedItem().toString();
			if (state.equalsIgnoreCase("--Select--")) {
				state = "";
				state_id = "";
			} else {
				state_id = Util.db.getStateId(state);
			}

			if (fname.equals("")) {
				
				edit_fname.setError("Please enter first name.");
				//_showToast("First name is missing");
			} else if (lname.equals("")) {
				edit_lname.setError("Please enter last name.");
				//_showToast("Last name is missing");
				
			}/*else if (mobile.length() > 0 && mobile.length() <14) {
				edit_mobile.setError("Please enter valid mobile number.");
				//_showToast("Mobile no is not valid");
				
			}*/ else if(btn_edit_company.isShown() && company.isEmpty()){
				edit_comp.setError("Please enter valid company name.");
			}else {
				if (isOnline()) {
					user_profile.Profile.First_Name__c=fname;
					user_profile.Profile.Last_Name__c=lname;
					user_profile.Profile.City__c=city;
					user_profile.profilestate=state;
					user_profile.profilecountry=country;
					/*if(!company.isEmpty()){
						user_profile.Profile.Company_Na me__c = company;
					}*/
					if(user_profile.Profile.Company_Name__c.equalsIgnoreCase(user_profile.Profile.First_Name__c+user_profile.Profile.Last_Name__c) && !company.isEmpty()){
						user_profile.Profile.Company_Name__c=company;
						user_profile.Profile.Default_Company_ID__c = "";
					}else if(!user_profile.Profile.Company_Name__c.equalsIgnoreCase(company) && !company.isEmpty()){
						user_profile.Profile.Company_Name__c=company;
						user_profile.Profile.Default_Company_ID__c = "";
					}
					
					user_profile.Profile.Mobile__c=mobile;
					doRequest();
				}else{
					startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
				}
			}
		}/*else if(v == txt_camera){
			photo_slider.animateClose();
			takeImage(Util.PIC_FROM_CAMERA,UserProfileActivity.this);
		}else if(v == txt_gallery){
			photo_slider.animateClose();
			takeImage(Util.PIC_FROM_GALLERY,UserProfileActivity.this);
		}else if(v == txt_cancel){
			photo_slider.animateClose();
		}*/else if(v == txt_save){
			if(!isOnline()){
				startErrorAnimation(getResources().getString(R.string.network_error), txt_error_msg);
			}else if(!Util.isMyServiceRunning(DownloadService.class, UserProfileActivity.this)){
			     openLogoutDialog(UserProfileActivity.this);
			}else{
				showServiceRunningAlert(checkedin_event_record.Events.Name);
			}
			
			//showDialog(CREATE_LOGOUT_DIALOG);
		}
	}
	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
	
	    switch (id) {
	        case CREATE_LOGOUT_DIALOG:
	            return ask_dialog;
	        
	        default:
	            return null;
	    }
	}
	@SuppressWarnings("deprecation")
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		//
		super.onPrepareDialog(id, dialog, bundle);
		switch (id) {
		case CREATE_LOGOUT_DIALOG:
			break;

		}
	}
    
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {


		if((requestCode == REQUEST_CODE_CROP_IMAGE)&& (data!=null)){
		 
             String path = data.getStringExtra(CropImage.IMAGE_PATH);
           
             if ((path == null)||(TextUtils.isEmpty(path))) {
                 return;
             }
           Bitmap  bitmap = BitmapFactory.decodeFile(path);
           profile_photo = bitmap;
           profile_img.setImageBitmap(bitmap);

            if(mFileTemp!=null){
            	mFileTemp.delete();
            }
		}else if ((requestCode == PICK_FROM_CAMERA) && (resultCode == RESULT_OK)) {
			if (data != null) {

				//doCrop();
			} else {
				File mediaStorageDir = new File(
						Environment.getExternalStorageDirectory(),
						"ScanAttendee");
				if(!mediaStorageDir.exists()){
					mediaStorageDir.mkdir();
				}
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ "IMG_1.jpg");
				mImageCaptureUri = Uri.fromFile(mediaFile);
			

				if (mImageCaptureUri != null) {
					try {
						startCropImage(UserProfileActivity.this);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

		} else if (requestCode == PICK_FROM_FILE && data != null
				&& data.getData() != null) {

			mImageCaptureUri = data.getData();
			//mediaFile = new File(getRealPathFromURI(mImageCaptureUri));
			try {
				File mediaStorageDir = new File(
						Environment.getExternalStorageDirectory(),
						"ScanAttendee");
				if(!mediaStorageDir.exists()){
					mediaStorageDir.mkdir();
				}
				mFileTemp = new File(mediaStorageDir.getPath() + File.separator
						+ "IMG_1.jpg");
				InputStream inputStream = getContentResolver().openInputStream(data.getData());
	            FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
	            CropUtil.copyStream(inputStream, fileOutputStream);
	            mediaFile = mFileTemp;
				startCropImage(UserProfileActivity.this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (requestCode == CROP_FROM_CAMERA) {

			Bundle extras = data.getExtras();

			if (extras != null) {
				profile_photo = extras.getParcelable("data");
				profile_img.setImageBitmap(profile_photo);

			}
		}else if (requestCode == FINISH_RESULT) {
			startActivity(new Intent(UserProfileActivity.this, SplashActivity.class));
			finish();
		}

	
 }
	
	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app.",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
      
			intent.setData(mImageCaptureUri);
			intent.putExtra("crop", "true");
			intent.putExtra("outputX", 90);
			intent.putExtra("outputY", 90);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			// intent.putExra("circleCrop", "true");
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(cropOptions.get(item).appIntent,CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {

							int id = getLastImageId();
							removeImage(id);

							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
	
	private int getLastImageId() {
		final String[] imageColumns = { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA };
		final String imageOrderBy = MediaStore.Images.Media._ID + " DESC";
		@SuppressWarnings("deprecation")
		Cursor imageCursor = managedQuery(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns,
				null, null, imageOrderBy);
		// this.startManagingCursor(imageCursor);
		if (imageCursor.moveToFirst()) {
			int id = imageCursor.getInt(imageCursor
					.getColumnIndex(MediaStore.Images.Media._ID));
			return id;
		} else {
			return 0;
		}
	}

	private void removeImage(int id) {
		ContentResolver cr = getContentResolver();
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.Images.Media._ID + "=?",
				new String[] { Long.toString(id) });
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			/*Intent result = new Intent();
			setResult(1987, result);*/
			if(NullChecker(getIntent().getStringExtra(Util.INTENT_KEY_1)).equalsIgnoreCase(EventListActivity.class.getName())){
				Intent i = new Intent(UserProfileActivity.this,EventListActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.EVENT_LIST, Util.EVENT_LIST);
				startActivity(i);
				finish();
			}else{
				Intent i = new Intent(UserProfileActivity.this,DashboardActivity.class);
				i.putExtra("CheckIn Event", checkedin_event_record);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Util.HOME, "home_layout");
				startActivity(i);
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	/* (non-Javadoc)
	 * @see com.globalnest.network.IPostResponse#insertDB()
	 */
	@Override
	public void insertDB() {
		// 
		
	}
}
