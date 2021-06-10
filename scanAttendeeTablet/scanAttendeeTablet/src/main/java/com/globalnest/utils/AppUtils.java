package com.globalnest.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.globalnest.network.WebServiceUrls;

import org.apache.http.NameValuePair;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.CHANGE_NETWORK_STATE;
import static android.Manifest.permission.CHANGE_WIFI_STATE;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class AppUtils {
	public static final String URL = "URL";
	public static final String VALUES = "VALUES";
	public static final String IMAGE = "IMAGE";
	public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
	public static SharedPreferences eventPrefer, logn_prefer;
	public final static String APP_ACCESS_TOKEN_TYPE = "TOKEN_TYPE";
	public final static String APP_ACCESS_TOKEN = "OAUTH TOKEN";
	// till herei
	public static final String INTENT_KEY = "intent_key";
	public static final String REVOKE_KEY="revoke_key";
	public static final String IS_DEV_PREF="is_dev_pref";
	public static final String IS_QA_PREF = "is_qa_pref";
	public static final String USER_CRED_PRF = "user_cred_pref";
	public static final String USER_EMAIL = "user_email";
	public static SharedPreferences isDevPref=null;
	public static SharedPreferences user_credentials = null;
	public static final boolean isLogEnabled = true;
	public static boolean isCrashReportEnable=true;//ON when moving to store
	public static void showError(Context ctxt, String message) {
		if (ctxt != null) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					ctxt);
			alertDialogBuilder.setTitle("Error");
			alertDialogBuilder
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									dialog.cancel();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
	public static void showError(Context ctxt, String message,Exception e) {
		String errorMessage="";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		errorMessage=sw.toString();
		if(errorMessage.trim().isEmpty()){
			errorMessage=getErrorFromException(e);
		}
		message=message+" "+errorMessage;
		if (ctxt != null) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					ctxt);
			alertDialogBuilder.setTitle("Error");
			alertDialogBuilder
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int id) {
									dialog.cancel();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}
	public static String getErrorFromException(Exception e){
		return e.getMessage()+String.valueOf(e.getStackTrace()[0].getLineNumber());
	}
	public static Boolean isOnline(Context context) {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		if (ni != null && ni.isConnected())
			return true;
		return false;
	}
	/*public static void givePhoneStatepermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{READ_PHONE_STATE}, 1);
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
				String.valueOf(new String[]{READ_PHONE_STATE}));
	}*/
	/*public static boolean isPhoneStatePermissionGranted(Context context) {
		int microphone = ContextCompat.checkSelfPermission(context.getApplicationContext(), READ_PHONE_STATE);
		return microphone ==PackageManager.PERMISSION_GRANTED;
	}*/
	public static void  givePhoneStateandStoragepermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{INTERNET,ACCESS_NETWORK_STATE
				,CHANGE_NETWORK_STATE,ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,BLUETOOTH_ADMIN,
				BLUETOOTH,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE}, 1);//READ_PHONE_STATE,//Added separatly ,RECORD_AUDIO
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, String.valueOf(new String[]{
				INTERNET,
				ACCESS_NETWORK_STATE,
				CAMERA,
				CHANGE_NETWORK_STATE,
				ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,
				BLUETOOTH_ADMIN,
				BLUETOOTH,
				WRITE_EXTERNAL_STORAGE,
				READ_EXTERNAL_STORAGE}));//READ_PHONE_STATE//Added separatly ,RECORD_AUDIO
		//String.valueOf(UriPermission.PARCELABLE_WRITE_RETURN_VALUE)
	}

	public static void  givepermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{INTERNET, READ_EXTERNAL_STORAGE,ACCESS_NETWORK_STATE
				,CAMERA,WRITE_EXTERNAL_STORAGE,CHANGE_NETWORK_STATE,ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,BLUETOOTH_ADMIN,
				BLUETOOTH}, 1);//READ_PHONE_STATE//Added separatly ,RECORD_AUDIO
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, String.valueOf(new String[]{
				INTERNET,
				ACCESS_NETWORK_STATE,
				CAMERA,
				CHANGE_NETWORK_STATE,
				ACCESS_WIFI_STATE,CHANGE_WIFI_STATE,
				BLUETOOTH_ADMIN,
				BLUETOOTH,
				READ_EXTERNAL_STORAGE,
				WRITE_EXTERNAL_STORAGE}));//READ_PHONE_STATE//Added separatly ,RECORD_AUDIO
		//String.valueOf(UriPermission.PARCELABLE_WRITE_RETURN_VALUE)
	}
	public static void giveMicrophonepermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{RECORD_AUDIO}, 1);
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
				String.valueOf(new String[]{RECORD_AUDIO}));
	}
	public static boolean isMicrophonePermissionGranted(Context context) {
		int microphone = ContextCompat.checkSelfPermission(context.getApplicationContext(), RECORD_AUDIO);
		return microphone ==PackageManager.PERMISSION_GRANTED;
	}
	public static void giveStoragermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE}, 1);
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
				String.valueOf(new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE}));
	}
	public static boolean isStoragePermissionGranted(Context context) {
		int writestorage = ContextCompat.checkSelfPermission(context.getApplicationContext(), WRITE_EXTERNAL_STORAGE);
		int readstorage = ContextCompat.checkSelfPermission(context.getApplicationContext(), READ_EXTERNAL_STORAGE);
		return writestorage == PackageManager.PERMISSION_GRANTED &&
				readstorage == PackageManager.PERMISSION_GRANTED ;
	}
	public static void giveCampermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{CAMERA}, 1);
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
				String.valueOf(new String[]{CAMERA}));
	}

	public static boolean isCamPermissionGranted(Context context) {
		int microphone = ContextCompat.checkSelfPermission(context.getApplicationContext(), CAMERA);
		return microphone ==PackageManager.PERMISSION_GRANTED;
	}
	public static boolean isLocationPermissionGranted(Context context) {
		int location = ContextCompat.checkSelfPermission(context.getApplicationContext(),Manifest.permission.ACCESS_FINE_LOCATION);
		return location ==PackageManager.PERMISSION_GRANTED;
	}
	public static void giveLocationpermission(Context context){
		ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
		ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,
				String.valueOf(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}));
	}
	public static boolean isPermissionGranted(Context context) {
		int writestorage = ContextCompat.checkSelfPermission(context.getApplicationContext(), WRITE_EXTERNAL_STORAGE);
		int readstorage = ContextCompat.checkSelfPermission(context.getApplicationContext(), READ_EXTERNAL_STORAGE);
		int camera = ContextCompat.checkSelfPermission(context.getApplicationContext(), CAMERA);
		return writestorage == PackageManager.PERMISSION_GRANTED &&
				readstorage == PackageManager.PERMISSION_GRANTED &&
				camera ==PackageManager.PERMISSION_GRANTED;
	}
	public static boolean isAllPermissionGranted(Context context) {
		return isStoragePermissionGranted(context) &&
				isCamPermissionGranted(context) &&
				isMicrophonePermissionGranted(context);//isPhoneStatePermissionGranted(context)&&
	}
	public static String getQuery(List<NameValuePair> params) {
		StringBuilder result = new StringBuilder();
		try {
			boolean first = true;
			for (NameValuePair pair : params) {
				if (first)
					first = false;
				else
					result.append("&");

				result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
				result.append("=");
				result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return result.toString();
	}

	public static String _getPreference(SharedPreferences perf, String key) {

		String value = perf.getString(key, "");

		return value;

	}

	public static HashMap<String, String> splitQuery(URL url) {
		HashMap<String, String> query_pairs = new LinkedHashMap<String, String>();

		try {
			String query = url.getQuery();
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),	URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return query_pairs;
	}

	public static String NullChecker(String var) {

		if (var == null || var.equals("null")) {
			return "";
		} else {
			return var;
		}

	}

	public static String prepareRequest(HashMap<String, String> params){
		String request = "";
		try {
			for (String key : params.keySet()) {
				if(!key.equalsIgnoreCase(WebServiceUrls.WEBSERVICE_CODE)){
					////Log.i("----------------Key And Values----------",":"+key+" : "+params.get(key));
					request = request+key+"="+(URLEncoder.encode(params.get(key),"UTF-8"))+"&";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	public static void _saveOrgPreferences(SharedPreferences pref,String key,String value){
		pref.edit().putString(key, value).commit();
	}

	public static String _getOrgPreferences(SharedPreferences pref,String key){
		try {
			String value = pref.getString(key, "DEV");

			return value;
		}catch (Exception e){
			return "";
		}
	}
	
	/*public static String prepareRequest(HashMap<String, String> params){
		String request = "";
		try {
			for (String key : params.keySet()) {
				if(!key.equalsIgnoreCase(WebServiceUrls.WEBSERVICE_CODE)){
					//Log.i("----------------Key And Values----------",":"+key+" : "+params.get(key));
				   request = request+key+"="+(URLEncoder.encode(params.get(key),"UTF-8"))+"&";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}*/

	public static void displayLog(String Tag,String message){
		try {
			if(isLogEnabled){
				Log.i(Tag,message);
			}
		}catch (Exception e){

		}
	}
}
