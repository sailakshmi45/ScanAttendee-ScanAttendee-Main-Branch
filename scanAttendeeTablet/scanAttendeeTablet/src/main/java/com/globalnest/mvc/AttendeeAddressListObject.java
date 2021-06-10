//  ScanAttendee Android
//  Created by Ajay on May 26, 2015
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
public class AttendeeAddressListObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public String BLN_Business_Country__c="",BLN_Business_State__c="",Business_Address1__c="",Business_Address2__c="",Business_City__c="",Business_Zipcode__c="",Company__c="",
			Duns_Number__c="",Established_Date__c="",Id="",name="";


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AttendeeAddressListObject [BLN_Business_Country__c="
				+ BLN_Business_Country__c + ", BLN_Business_State__c="
				+ BLN_Business_State__c + ", Business_Address1__c="
				+ Business_Address1__c + ", Business_Address2__c="
				+ Business_Address2__c + ", Business_City__c="
				+ Business_City__c + ", Business_Zipcode__c="
				+ Business_Zipcode__c + ", Company__c=" + Company__c
				+ ", Duns_Number__c=" + Duns_Number__c
				+ ", Established_Date__c=" + Established_Date__c + ", Id=" + Id
				+ ", name=" + name+"]";
	}
	
	
	
	

}
