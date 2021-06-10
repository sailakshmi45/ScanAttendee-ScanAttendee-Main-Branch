//  ScanAttendee Android
//  Created by Ajay on May 10, 2016
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
public class SessionTicketObject implements Serializable {
	public String Id = "", tkt_profile__c = "", Badge_Label__c = "", Custom_Barcode__c = "";
	public SessionProfileObject tkt_profile__r = new SessionProfileObject();
	public String Badge_ID__c="",Scan_Id__c="";
	public Item_Type Item_Type__r = new Item_Type();

	public class Item_Type implements Serializable {
		private static final long serialVersionUID = 1L;
		public String Name = "", Id = "";
	}

}
