//  ScanAttendee Android
//  Created by Ajay on May 17, 2016
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
public class OffSyncSuccessTicObject {

	public String BLN_Scanusr_item__c="",CreatedDate="",Id="",Islatest__c="",Scan_source__c="",scan_time__c="",Ticket__c="",Tstatus_name__c="";
	
	public OffSyncSuccessTicket_r Ticket__r=new OffSyncSuccessTicket_r();
	public OffSyncSuccessBLN_Scanusr_item__r BLN_Scanusr_item__r=new OffSyncSuccessBLN_Scanusr_item__r();
}
