package com.globalnest.objects;

import java.io.Serializable;

public class ScannedItems implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String Id="",BLN_Event__c="",BLN_GN_User__c="",BLN_Item_Pool__c="",Status__c="",BLN_Group__c="";
	public boolean DefaultValue__c = false;
	public BLN_Item_Pool__R BLN_Item_Pool__r = new BLN_Item_Pool__R();
	//This object used for Dashboard only
	public class BLN_Item_Pool__R implements Serializable{
		public String Id ="",Item_Pool_Name__c ="";
	}
	
}
