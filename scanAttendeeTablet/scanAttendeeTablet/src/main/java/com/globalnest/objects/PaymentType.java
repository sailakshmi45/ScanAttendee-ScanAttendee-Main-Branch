//  ScanAttendee Android
//  Created by Ajay on 23-Feb-2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.objects;

import java.io.Serializable;

/**
 * @author Mayank Mishra
 *
 */
public class PaymentType implements Serializable {
	
	/**
	 * 
	 */
private static final long serialVersionUID = 1L;
	
	public String Company__c="",Id="",Name="",PGateway_Type__c="",PG_Signature__c="",PG_User_Key__c="",PG_Email_Id__c="",PP_Fee_Payer__c="",PP_Payment_Type__c="",
			PG_Pass_Secret__c="",Service_Fee__c ="" ,Paygateway_name__c="",BLN_GN_User__c="",citrus_param__c="",Paygateway_Label__c="";
	
	
	@Override
	public String toString() {
		return "PaymentType [Company__c=" + Company__c + ", Id=" + Id
				+ ", Name=" + Name + ", PGateway_Type__c=" + PGateway_Type__c
				+ ", PG_Signature__c=" + PG_Signature__c + ", PG_User_Key__c="
				+ PG_User_Key__c + ", PG_Email_Id__c=" + PG_Email_Id__c
				+ ", PP_Fee_Payer__c=" + PP_Fee_Payer__c
				+ ", PP_Payment_Type__c=" + PP_Payment_Type__c
				+ ", PG_Pass_Secret__c=" + PG_Pass_Secret__c
				+ ", Service_Fee__c=" + Service_Fee__c 
				+ ", Paygateway_name__c=" + Paygateway_name__c
				+ ", citrus_param__c"+ citrus_param__c
				+ ", BLN_GN_User__c=" + BLN_GN_User__c+"]";
	}

	
}
