//  ScanAttendee Android
//  Created by Ajay on 12-Feb-2015
//  This class is used to get the all the orders and tickets information from backend.
//  Copyright (c) 2014 Globalnest. All rights reserved

/**
 * 
 */
package com.globalnest.mvc;

import java.io.Serializable;

/**
 * @author Mayank Mishra
 *
 */
public class BlockTicketListController implements Serializable{
    
    private static final long serialVersionUID = 1L;

	
	public String Event__c="",Id="",Item_Pool__c="",Item_Type__c="",Item__c="",Name="",Ticket_Status__c="",Parent_ID__c="";
    public ItemPool_R Item_Pool__r = new ItemPool_R();
    public Item_Type_R Item_Type__r = new Item_Type_R();
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return "BlockTicketListController [Event__c=" + Event__c + ", Id="
		    + Id + ", Item_Pool__c=" + Item_Pool__c + ", Item_Type__c="
		    + Item_Type__c + ", Item__c=" + Item__c + ", Name=" + Name
		    + ", Ticket_Status__c=" + Ticket_Status__c +",Parent_ID__c="+Parent_ID__c+"]";
	}
	

	public class ItemPool_R implements Serializable{
		private static final long serialVersionUID = 1L;
		public Item_Type_R Item_Type__r = new Item_Type_R();
		public String Ticket_Settings__c="",Item_Pool_Name__c="",Package_Flag__c="",Id="",Badgable__c="";
		
		public String toString(){
			return "ItemPool_R [Ticket_Settings__c="+Ticket_Settings__c+",Item_Pool_Name__c="+Item_Pool_Name__c+",Package_Flag__c="+Package_Flag__c
					+",Id="+Id+",Badgable__c="+Badgable__c+"]";
		}
	}
	
	public class Item_Type_R implements Serializable{
		private static final long serialVersionUID = 1L;
		
		public String Id="",Name="";
		
		public String toString(){
			return "[Id="+Id+",Name="+Name+"]";
		}
		
	}

}
