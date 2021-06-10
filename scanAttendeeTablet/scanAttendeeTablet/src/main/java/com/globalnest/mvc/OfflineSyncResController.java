//  ScanAttendee Android
//  Created by Ajay on May 16, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OfflineSyncResController implements Serializable{

	private static final long serialVersionUID = 1L;

	public String ErrorMsg="";
	public List<Ticketbadgeprintstatus> ticketbadgeprintstatus=new ArrayList<Ticketbadgeprintstatus>();
	public List<OfflineSyncFailuerObject> FailureTickets=new ArrayList<OfflineSyncFailuerObject>();
	public List<OfflineSyncSuccessObject> SuccessTickets=new ArrayList<OfflineSyncSuccessObject>();
	public class Ticketbadgeprintstatus{
		public String PrintStatus="",BadgeId="",Ticketid="";
	}
}
