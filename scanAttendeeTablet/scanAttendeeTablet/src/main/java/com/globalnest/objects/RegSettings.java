//  ScanAttendee Android
//  Created by Ajay on Nov 23, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author laxmanamurthy
 *
 */
public class RegSettings implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<RegistrationSettingsController> records = new ArrayList<RegistrationSettingsController>();
}
