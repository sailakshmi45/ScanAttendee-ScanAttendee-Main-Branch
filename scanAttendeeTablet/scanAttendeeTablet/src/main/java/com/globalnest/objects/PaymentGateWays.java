package com.globalnest.objects;

import java.io.Serializable;

public class PaymentGateWays implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String Id="",Display_Type__c="",Name="",Online_Flag__c="",Onsite_Flag__c="",ScanAttendee_Flag__c="",Adaptive_Type__c="",BLN_Currency__c="",BLN_PGateway_Type__c="";

	@Override
	public String toString() {
		return "PaymentGateWays [Id=" + Id + ", Display_Type__c="
				+ Display_Type__c + ", Name=" + Name + ", Online_Flag__c="
				+ Online_Flag__c + ", Onsite_Flag__c=" + Onsite_Flag__c
				+ ", ScanAttendee_Flag__c=" + ScanAttendee_Flag__c 
				+ ", Adaptive_Type__c=" + Adaptive_Type__c +"]";
	}

}
