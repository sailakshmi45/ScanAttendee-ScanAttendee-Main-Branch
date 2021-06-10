//  ScanAttendee Android
//  Created by Ajay on Jul 8, 2015
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
public class DashbordStatistics {
	
	public double TotalCheckInCount=0.0,RevenueGenerated=0.0,TotalAttendeesRegistered=0.0,TotalOrders=0.0,TotalTickets=0.0,TotalTicketsSold=0.0;
	public String sName="",value="";
	public int image=0;
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashbordStatistics [TotalCheckInCount=" + TotalCheckInCount
				+ ", RevenueGenerated=" + RevenueGenerated
				+ ", TotalAttendeesRegistered=" + TotalAttendeesRegistered
				+ ", TotalOrders=" + TotalOrders + ", TotalTickets="
				+ TotalTickets + ", TotalTicketsSold=" + TotalTicketsSold
				+ ", sName=" + sName + ", value=" + value + ", image=" + image
				+ "]";
	}
	
	
	
	

}
