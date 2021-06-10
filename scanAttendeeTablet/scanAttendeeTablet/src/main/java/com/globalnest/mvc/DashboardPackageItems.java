//  ScanAttendee Android
//  Created by Ajay on Aug 17, 2015
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
public class DashboardPackageItems implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int checloutCnt=0,chekinCont=0;

	public DashboardPackageItemName itempool=new DashboardPackageItemName();
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashboardPackageItems [checloutCnt=" + checloutCnt
				+ ", chekinCont=" + chekinCont + ", itempool=" + itempool + "]";
	}
	
	
	

}
