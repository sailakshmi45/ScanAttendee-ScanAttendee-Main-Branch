//  ScanAttendee Android
//  Created by Ajay on Oct 17, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.globalnest.objects.ScannedItems;

/**
 * @author laxmanamurthy
 *
 */
public class DashboardSessionReport implements Serializable{
	
	public int totalItemsbuyQty = 0,totalItemschkQty=0;
	public SessionGroup sess = new SessionGroup();
    public List<DashboardSessionLinItems> lineItems = new ArrayList<DashboardSessionLinItems>(); 
	 

}
