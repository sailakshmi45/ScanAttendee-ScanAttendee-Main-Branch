//  ScanAttendee Android
//  Created by Ajay on Aug 3, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

/**
 * @author mayank
 *
 */
public class TicketScanedByObject {

	public String Email__c="",First_Name__c="",Id="",Last_Name__c="",User__c="";

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TicketScanedByObject [Email__c=" + Email__c
				+ ", First_Name__c=" + First_Name__c + ", Id=" + Id
				+ ", Last_Name__c=" + Last_Name__c + ", User__c=" + User__c
				+ "]";
	}
	
	
}
