//  ScanAttendee Android
//  Created by Ajay on Oct 29, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

/**
 * @author laxmanamurthy
 *
 */
public class AddressCotroller implements Serializable {
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    public String Id="",Address1__c="",Address2__c="",City__c="",State__c="",Country__c="",ZipCode__c="";
    public CountryAndStateObj Country__r= new CountryAndStateObj();
    public CountryAndStateObj State__r= new CountryAndStateObj();
    
}
