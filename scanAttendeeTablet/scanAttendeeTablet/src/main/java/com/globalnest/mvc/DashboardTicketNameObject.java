//  ScanAttendee Android
//  Created by Ajay on Aug 11, 2015
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
public class DashboardTicketNameObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Id="",item_name__c="",Item_Pool__c="";
	public int  item_count__c=0;
	public DashboardItemTypeObject item_type__r=new DashboardItemTypeObject();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashboardTicketNameObject [Id=" + Id + ", item_name__c="
				+ item_name__c + ", item_count__c=" + item_count__c
				+ ", item_type__r=" + item_type__r + "]";
	}
	
	

}
