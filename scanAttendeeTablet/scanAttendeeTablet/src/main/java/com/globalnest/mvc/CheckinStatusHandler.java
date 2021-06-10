//  ScanAttendee Android
//  Created by Ajay on 26-Dec-2014
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;
/**
 * @author Ajay Goyal
 *
 */


public class CheckinStatusHandler implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SerializedName("Id")
	String checkinId;
	
	@SerializedName("Tstatus_name__c")
	boolean checkinstatus;
	
	public String getCheckinId() {
		return checkinId;
	}
	public void setCheckinId(String checkinId) {
		this.checkinId = checkinId;
	}
	public boolean getCheckinstatus() {
		return checkinstatus;
	}
	public void setCheckinstatus(boolean checkinstatus) {
		this.checkinstatus = checkinstatus;
	}
	
	
	@Override
	public String toString() {
		return "CheckinStatusHandler [checkinId=" + checkinId
				+ ", checkinstatus=" + checkinstatus + "]";
	}
	
	

}
