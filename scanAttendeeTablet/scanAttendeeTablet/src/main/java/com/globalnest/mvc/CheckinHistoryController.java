//  ScanAttendee Android
//  Created by Ajay on Apr 13, 2015
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
public class CheckinHistoryController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String LastModifiedDate="",Ticket="",Tstatus_name="",type="";
	
	public TicketScanedByObject gnUser=new TicketScanedByObject();
	public TStatus tstatus = new TStatus();

	@Override
	public String toString() {
		return "CheckinHistoryController [LastModifiedDate=" + LastModifiedDate
				+ ", Ticket=" + Ticket + ", Tstatus_name=" + Tstatus_name 
				+ ", type=" + type +", gnUser"+gnUser+"]";
	}
	
	

}
