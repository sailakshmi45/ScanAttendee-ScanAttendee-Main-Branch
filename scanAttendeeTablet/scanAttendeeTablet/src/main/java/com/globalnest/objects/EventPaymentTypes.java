//  ScanAttendee Android
//  Created by Ajay on 23-Feb-2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.objects;

import java.io.Serializable;

/**
 * @author Mayank Mishra
 *
 */
public class EventPaymentTypes implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String Id="",Events__c="",Registration_Type__c="",Name="",Pay_Gateway__c="";
	public PaymentType Pay_Gateway__r=new PaymentType();
	public EventOrganaizer Events__r = new EventOrganaizer();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "EventPaymentTypes [Id=" + Id + ", Events__c=" + Events__c  + ", Registration_Type__c="
		+ Registration_Type__c +  ", Name=" + Name +  ", Pay_Gateway__c=" + Pay_Gateway__c +", Pay_Gateway__r=" + Pay_Gateway__r 
		+ ",Events__r="+Events__r+"]";
	}

	public class EventOrganaizer implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		Event event = new Event();
		public Event.Organaizer organizer_id__r = event.new Organaizer();
	}
}
