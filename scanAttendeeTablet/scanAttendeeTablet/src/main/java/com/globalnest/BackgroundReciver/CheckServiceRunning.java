//  ScanAttendee Android
//  Created by Ajay on Sep 18, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.BackgroundReciver;

import com.globalnest.utils.Util;
import com.globalnest.MainActivity;

import android.os.AsyncTask;

/**
 * @author mayank
 *
 */
public class CheckServiceRunning extends AsyncTask<String,String,String>{
	
	private MainActivity mActivity;
	
	public CheckServiceRunning(MainActivity activity){
		mActivity=activity;
		
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String isRunning= String.valueOf(Util.isMyServiceRunning(DownloadService.class, mActivity));
		return isRunning;
	}
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		mActivity.parseJsonResponse(result);
	}

}
