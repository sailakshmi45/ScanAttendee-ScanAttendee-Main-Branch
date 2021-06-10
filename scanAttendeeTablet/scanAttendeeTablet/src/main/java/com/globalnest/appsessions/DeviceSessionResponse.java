//  Eventdex Android
//  Created by Ajay on Feb 18, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.appsessions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author laxmanamurthy
 *
 */
public class DeviceSessionResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
     
	public List<DeviceSessionId> sessionsTocancel = new ArrayList<DeviceSessionId>();
	public int SessionsCount=0;
	public String msg="",tempSessionsCount="",currentSessionId="";
}
