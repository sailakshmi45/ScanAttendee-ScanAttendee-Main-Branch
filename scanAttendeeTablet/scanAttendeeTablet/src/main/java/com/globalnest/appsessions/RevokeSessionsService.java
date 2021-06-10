//  ScanAttendee Android
//  Created by Ajay on Feb 18, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.appsessions;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.globalnest.network.HttpClientClass;
import com.globalnest.network.WebServiceUrls;
import com.globalnest.utils.AppUtils;
import com.globalnest.utils.Util;

/**
 * @author laxmanamurthy
 *
 */
public class RevokeSessionsService extends Service {

	private static final String TEST_URL = "https://test.salesforce.com/services/oauth2/revoke";
	private static final String PROD_URL = "https://login.salesforce.com/services/oauth2/revoke";
	private SharedPreferences event_pref;
	private boolean isRunning;
	private Context context;
	private Thread backgroundThread;
	private DeviceSessionId session_response = new DeviceSessionId();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		this.context = this;
		this.isRunning = false;
		this.backgroundThread = new Thread(myTask);
	}

	private Runnable myTask = new Runnable() {
		public void run() {
			AppUtils.displayLog("----------------Killing Sessions------------",":"+LogOutFunction(session_response));
			stopSelf();
		}
	};

	@Override
	public void onDestroy() {
		this.isRunning = false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		session_response =  (DeviceSessionId) intent.getSerializableExtra(WebServiceUrls.SA_DEVICE_SESSION);
		if (!this.isRunning) {
			this.isRunning = true;
			this.backgroundThread.start();
		}
		return START_STICKY;
	}

	public int LogOutFunction(DeviceSessionId session_id) {

		try {
			String url = null;

			// TODO Auto-generated method stub
			if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("DEV")){
				url = TEST_URL;
			}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("QA")){
				url = TEST_URL;
			}else if(AppUtils._getOrgPreferences(AppUtils.isDevPref, AppUtils.IS_DEV_PREF).equalsIgnoreCase("PRODUCTION")){
				url = PROD_URL;
			}else{
				url = PROD_URL;
			}
			//Log.i("---------------String Url-----------", ":" + url);
			HttpPost postMethod = new HttpPost(url);
			HttpClient client = HttpClientClass.getHttpClient(30000);
			postMethod.setHeader(HTTP.CONTENT_TYPE, "application/x-www-form-urlencoded");
			List<NameValuePair> values = new ArrayList<NameValuePair>();
			values.add(new BasicNameValuePair("token", session_id.Token__c));
			postMethod.setEntity(new UrlEncodedFormEntity(values));
			HttpResponse response = client.execute(postMethod);
			return response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return 400;
	}
}
