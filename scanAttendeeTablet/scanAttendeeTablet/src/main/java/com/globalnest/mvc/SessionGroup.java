//  ScanAttendee Android
//  Created by Ajay on Oct 3, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.globalnest.objects.ScannedItems;
import com.globalnest.objects.Event;
import com.globalnest.objects.Event.ScannedItemObject;

/**
 * @author laxmanamurthy
 *
 */
public class SessionGroup implements Serializable{
 
	public String Id="",Name="",Product_Type__c="",Event__c="";
	public boolean Scan_Switch = false;
	public Event.ScannedItemObject BLN_Session_Items__r =  new Event().new ScannedItemObject();
	public Event.ScannedItemObject BLN_Session_users__r =  new Event().new ScannedItemObject();
	public String ALL="ALL";
	 /*public class ScannedItemObject implements Serializable{

		*//**
		 * 
		 *//*
		private static final long serialVersionUID = 1L;
		public List<ScannedItems> records = new ArrayList<ScannedItems>();
    	
    }*/
}
