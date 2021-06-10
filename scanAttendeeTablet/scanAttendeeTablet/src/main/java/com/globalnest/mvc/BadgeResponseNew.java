//  ScanAttendee Android
//  Created by Ajay on Oct 13, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;

public class BadgeResponseNew implements Serializable{

	private static final long serialVersionUID = 1L;
	public ArrayList<BadgeLayerNew> layers = new ArrayList<BadgeLayerNew>();
	  public BadgeNew badge = new BadgeNew();

}
