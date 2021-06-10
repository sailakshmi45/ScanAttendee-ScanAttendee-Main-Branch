package com.globalnest.objects;

import java.io.Serializable;

public class EventPriceObject implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String Id="",Event__c="",Item_type__c="",Name="";
	public double BL_Fee_Percentage__c=0,BL_Fee_Amount__c=0,Max_bl_fee__c=0;
	//public boolean Eventdex_Product__c=false;
	public Item_type__r Item_type__r = new Item_type__r();
	
	@Override
	public String toString(){
		return "[Id="+Id+",Event__c="+Event__c+",Item_type__c="+Item_type__c+",Name="+Name
				+",BL_Fee_Percentage__c="+BL_Fee_Percentage__c+",BL_Fee_Amount__c="+BL_Fee_Amount__c
				+",Max_bl_fee__c="+Max_bl_fee__c+"]";
	}

	public class Item_type__r implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public String Id="";
		public boolean Eventdex_Product__c=false;
		
	}
}
