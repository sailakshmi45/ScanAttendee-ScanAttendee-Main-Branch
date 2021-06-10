//  ScanAttendee Android
//  Created by Ajay on Dec 28, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 *
 */
package com.globalnest.mvc;

import java.io.Serializable;

public class ExternalSettings implements Serializable{

	private static final long serialVersionUID = 1L;
	public boolean quick_checkin=true,quick_print=false,custom_barcode=false,offline_scan=false,allow_promocode=false,
			doubleSide_badge=false,isValidateBadge=false,identical_doubleSide_badge=false,mirror_doubleSide_badge=false,
			identical_doubleSide_badge_two_in_one=false,mirror_doubleSide_badge_two_in_one=false,
			online_mode=true,zebra_settings=false,checkin_checkout=true;


	@Override
	public String toString(){
		return "[quick_checkin="+quick_checkin+",quick_print="+quick_print+",custom_barcode="+custom_barcode+",offline_scan="+offline_scan+"]";
	}
}

