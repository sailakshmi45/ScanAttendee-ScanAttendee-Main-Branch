package com.globalnest.objects;

import java.io.Serializable;

public class PaymentGateWaysRes implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String BLN_Currency__c = "",BLN_PGateway_Type__c="";
	
	public PaymentGateWays  BLN_PGateway_Type__r = new PaymentGateWays();

}
