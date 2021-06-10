//  ScanAttendee Android
//  Created by Ajay on Apr 6, 2015
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
public class DashboardCheckinHandler implements Serializable{
	
	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	public String ItemId;
	public String ItemName;
	public String TotalCheckInCount="0";
	public String TotalTicketCount="0";
	public String TotalTicketSold="0";
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashboardCheckinHandler [ItemId=" + ItemId + ", ItemName="
				+ ItemName + ", TotalCheckInCount=" + TotalCheckInCount
				+ ", TotalTicketCount=" + TotalTicketCount
				+ ", TotalTicketSold=" + TotalTicketSold + "]";
	}
	
	
	
	
	

}
