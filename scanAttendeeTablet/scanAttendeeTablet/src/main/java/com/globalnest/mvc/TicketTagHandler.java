//  ScanAttendee Android
//  Created by Ajay on May 27, 2015
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
public class TicketTagHandler implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String Event__c="",Id="",RowId__c="",Table_Name__c="",Tag_Text__c="",IsDefault__c="";

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TicketTagHandler [Event__c=" + Event__c + ", Id=" + Id
				+ ", RowId__c=" + RowId__c + ", Table_Name__c=" + Table_Name__c
				+ ", Tag_Text__c=" + Tag_Text__c + ", IsDefault__c="
				+ IsDefault__c + "]";
	}
	
	

}
