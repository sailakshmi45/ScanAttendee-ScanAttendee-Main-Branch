//  ScanAttendee Android
//  Created by Ajay on May 26, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

/**
 * @author mayank
 *
 */
public class AttendeeAddressHandler implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String Name="",Id="";
	
	public AttendeeAddressObject Company_Ext__r=new AttendeeAddressObject();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttendeeAddressHandler [Name=" + Name + ", Id=" + Id
				+ ", Company_Ext__r=" + Company_Ext__r + "]";
	}
	
	

}
