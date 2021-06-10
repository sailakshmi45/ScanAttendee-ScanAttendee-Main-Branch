//  ScanAttendee Android
//  Created by Ajay on May 16, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.List;

/**
 * @author mayank
 *
 */
public class OfflineSyncFailuerObject implements Serializable{
	public String msg="",STicketId="",TimeStamp="";
	public boolean Status=false;
	public OfflineSyncFailureTicObject tbup=new OfflineSyncFailureTicObject();
    public TStatus tStaus = new TStatus();
}
