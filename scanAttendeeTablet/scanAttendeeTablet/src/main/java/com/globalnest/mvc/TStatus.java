//  ScanAttendee Android
//  Created by Ajay on Oct 14, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

/**
 * @author laxmanamurthy
 *
 */
public class TStatus implements Serializable{
	
	public String Ticket__c="",BLN_Session_user__c="",BLN_Session_Item__c="",scan_time__c="",Id="";
	public boolean Tstatus_name__c=false,Islatest__c=true;
	public SessionTicketObject Ticket__r=new SessionTicketObject();
	public BLN_Session_User BLN_Session_user__r = new BLN_Session_User(); 
	public BLN_Session_Item__R BLN_Session_Item__r = new BLN_Session_Item__R();
	
	public class BLN_Session_User implements Serializable{
		public String Id="",BLN_Group__c="";
		public BLN_Group BLN_Group__r = new BLN_Group();
		public BLN_GN_User__r BLN_GN_User__r = new BLN_GN_User__r();

	}
	
	public class BLN_Group implements Serializable{
		public String Id="",Name="";
	}
	public class BLN_Session_Item__R implements Serializable{
		
		public String BLN_Item_Pool__c = "";
	}
	public class BLN_GN_User__r implements Serializable{

		public String Id = "";
		public String User__c = "";
	}
}
