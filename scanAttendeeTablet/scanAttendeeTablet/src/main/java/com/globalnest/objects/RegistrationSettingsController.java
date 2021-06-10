//  ScanAttendee Android
//  Created by Ajay on Nov 18, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.objects;

import java.io.Serializable;

public class RegistrationSettingsController implements Serializable{

	private static final long serialVersionUID = 1L;

	public String Item__c="",Id="",Column_Name__c="",Defaullt_Label__c="",Label_Name__c="",Setting_Type__c="",Event__c="";
	public boolean Included__c = false,Required__c=false,Read_Access__c=true,Update_Access__c=false;
	public String Group_Name__c="",Group_Label__c="",Custom_Message__c="";
}

