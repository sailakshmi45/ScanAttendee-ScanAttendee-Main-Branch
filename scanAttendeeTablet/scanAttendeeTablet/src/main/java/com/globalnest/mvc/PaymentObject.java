//  ScanAttendee Android
//  Created by Ajay on Aug 26, 2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

package com.globalnest.mvc;

/**
 * @author laxmanamurthy
 *
 */
public class PaymentObject {
  
	public String Id="",Payment_Mode__c="",Payment_Ref_Number__c="",Pay_Address__c="",Registration_Type__c="",Payment_Amount__c="",Order__c="",Currency_Code__c="",credit_card_type__c="",credit_card_last_4digits__c="",
			BLN_Pay_Gateway__c="";
	
	public String toString(){
	
		return "[Id="+Id+",Payment_Mode__c="+Payment_Mode__c+",Payment_Ref_Number__c="+Payment_Ref_Number__c+",Pay_Address__c="+Pay_Address__c+",Registration_Type__c="+Registration_Type__c
				+",Payment_Amount__c="+Payment_Amount__c+",Order__c="+Order__c+",Currency_Code__c="+Currency_Code__c+",credit_card_type__c="+credit_card_type__c+",credit_card_last_4digits__c="+credit_card_last_4digits__c
				+",BLN_Pay_Gateway__c="+BLN_Pay_Gateway__c+"]";
	}
	
}
