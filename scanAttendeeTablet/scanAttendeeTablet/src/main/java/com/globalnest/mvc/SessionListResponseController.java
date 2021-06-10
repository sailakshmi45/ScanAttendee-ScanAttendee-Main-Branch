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
public class SessionListResponseController implements Serializable{

	private static final long serialVersionUID = 1L;
	public String BLN_Scanusr_item__c="",Id="",scan_time__c="",Ticket__c="",Tstatus_name__c="",Islatest__c;
	public BLN_Scanusr_item__r BLN_Session_Item__r= new BLN_Scanusr_item__r();
	public SessionTicketObject Ticket__r=new SessionTicketObject();
	
	public class BLN_Scanusr_item__r implements Serializable{
		private static final long serialVersionUID = 1L;
		public String BLN_Item_Pool__c="",Id="";
	}
}

