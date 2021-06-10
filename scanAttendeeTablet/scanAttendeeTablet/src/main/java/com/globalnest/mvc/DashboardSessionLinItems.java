//  ScanAttendee Android
//  Created by Ajay on Oct 17, 2016
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

import com.globalnest.objects.ScannedItems;

/**
 * @author laxmanamurthy
 *
 */
public class DashboardSessionLinItems implements Serializable{

	public int chekinCont = 0,totalBuyQty= 0;
	public ScannedItems sesItem = new ScannedItems();
}
