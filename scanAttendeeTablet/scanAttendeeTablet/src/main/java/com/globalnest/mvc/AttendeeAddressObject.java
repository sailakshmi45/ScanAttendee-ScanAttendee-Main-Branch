//  ScanAttendee Android
//  Created by Ajay on May 26, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mayank
 *
 */
public class AttendeeAddressObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public List<AttendeeAddressListObject> records=new ArrayList<AttendeeAddressListObject>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttendeeAddressObject [records=" + records + "]";
	}
	
	

}
