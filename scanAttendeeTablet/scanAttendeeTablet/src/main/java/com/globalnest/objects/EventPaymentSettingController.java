//  ScanAttendee Android
//  Created by Ajay on 23-Feb-2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Mayank Mishra
 *
 */
public class EventPaymentSettingController implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ArrayList<EventPaymentTypes> records=new ArrayList<EventPaymentTypes>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EventPaymentSettingController [records=" + records + "]";
	}
	
	
}
