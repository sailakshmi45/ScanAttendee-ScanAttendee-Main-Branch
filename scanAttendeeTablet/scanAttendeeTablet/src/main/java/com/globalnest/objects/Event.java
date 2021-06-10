package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public String Accept_Refund_Policy__c="",Accept_Tax_Rate__c="",Accept_Terms_Conditions__c="",BLN_Country__c="",BLN_State__c="",City__c="",
		        Description__c="",End_Date__c="",End_Time__c="",Event_Status__c="",Event_Type__c="",Id="",IsDeleted="",Logo_URL__c="",Name="",
		        Organizer_Email__c="",organizer_id__c="",Phone_Number__c="",Refund_Policy__c="",Show_Terms_Checkbox__c="",Start_Date__c="",
		        Start_Time__c="",Street1__c="",Street2__c="",Tax_Rate__c="",Time_Zone__c="",User_Company__c="",Venue_Name__c="",Website_Url__c="",
		        ZipCode__c="",User_register__c="",checkin="",mobileevent_id="",eventfeeapplicable="",last_modified_date="",countrycodes="",
		        Mobile_Default_Badge__c="",BLN_Currency__c="",Revenue_visibility__c="",ZebraDpi__c="";
public int scan_attendee_limit__c=0;
public Orders Orders__r = new Orders();
public Tickets tickets__r = new Tickets();
public Organaizer organizer_id__r = new Organaizer();
public EventPaymentSettingController Event_Pay_Gateway__r=new EventPaymentSettingController();
public Event_Price_R Event_Price__r = new Event_Price_R();
public ScannedItemObject BLN_Scanusr_items__r = new ScannedItemObject();
public BadgeStyleObject BLN_Badge_Styles__r =new BadgeStyleObject(); 
public String state="",roles="",image="",country="",company="";
public boolean AllowNoticketSessions__c = false;
public Country BLN_Country__r = new Country();
public States BLN_State__r = new States();
public RegSettings Reg_Settings__r = new RegSettings();
  
  @Override
  public String toString(){
	  return "[Accept_Refund_Policy__c="+Accept_Refund_Policy__c+",Accept_Tax_Rate__c="+Accept_Tax_Rate__c+",Accept_Terms_Conditions__c="+Accept_Terms_Conditions__c
			  +",BLN_Country__c="+BLN_Country__c+",BLN_State__c="+BLN_State__c+",City__c="+City__c+",Description__c="+Description__c+",End_Date__c="+End_Date__c
			  +",End_Time__c="+End_Time__c+",Event_Status__c="+Event_Status__c+",Event_Type__c="+Event_Type__c+",Id="+Id+",IsDeleted="+IsDeleted+",Logo_URL__c="
			  +Logo_URL__c+",Name="+Name+",Organizer_Email__c="+Organizer_Email__c+",organizer_id__c="+organizer_id__c+",Phone_Number__c="+Phone_Number__c
			  +",Refund_Policy__c="+Refund_Policy__c +",Show_Terms_Checkbox__c="+Show_Terms_Checkbox__c+",Start_Date__c="+Start_Date__c+",Start_Time__c="+Start_Time__c
			  +",Street1__c="+Street1__c+",Street2__c="+Street2__c+",Tax_Rate__c="+Tax_Rate__c+",Time_Zone__c="+Time_Zone__c+",User_Company__c="+User_Company__c
			  +",Venue_Name__c="+Venue_Name__c+",Website_Url__c="+Website_Url__c+",ZipCode__c="+ZipCode__c+",checkin="+checkin+",mobileevent_id="+mobileevent_id
			  +",eventfeeapplicable="+eventfeeapplicable +",BadgeTemplate_Styles__r="+BLN_Badge_Styles__r +",Event_Pay_Gateway__r="+Event_Pay_Gateway__r
			  +",scan_attendee_limit__c="+scan_attendee_limit__c+"Orders__r="+Orders__r+",tickets__r="+tickets__r+"organizer_id__r="+organizer_id__r 
			  +",last_modified_date="+last_modified_date+",AllowNoticketSessions__c="+AllowNoticketSessions__c+"]";
  }
		        
  
    public class Orders implements Serializable{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int totalSize = 0;
    			
    	public String toString(){
    		return "Orders [totalSize="+totalSize+"]";
    	}
    }

    public class Tickets implements Serializable{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public int totalSize = 0;
    			
    	public String toString(){
    		return "Tickets [totalSize="+totalSize+"]";
    	}
    }
    public class Organaizer implements Serializable{
    	/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String Name="",Id="",PG_Authorization_CC_Last_four_Digit__c="",PG_Authorization_Key__c="",
				PG_Authorization_Card_Type__c="";
		public boolean PG_Credit_Card_Overwrite__c=false;
    	
    	public String toString(){
    		return "Organaizer [Name="+Name+",Id="+Id+",PG_Authorization_CC_Last_four_Digit__c="+PG_Authorization_CC_Last_four_Digit__c+",PG_Authorization_Key__c="+PG_Authorization_Key__c
    				+",PG_Credit_Card_Overwrite__c="+PG_Credit_Card_Overwrite__c +",PG_Authorization_Card_Type__c="+PG_Authorization_Card_Type__c+"]";
    		
    	}
    }
    public class Event_Price_R implements Serializable{

		private static final long serialVersionUID = 1L;
		public boolean done=false;
		public int totalSize = 0;
		public List<EventPriceObject> records = new ArrayList<EventPriceObject>();
    	
		@Override
		public String toString(){
			return "[done="+done+",totalSize="+totalSize+",records="+records+"]";
		}
    }
    
    public class ScannedItemObject implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public List<ScannedItems> records = new ArrayList<ScannedItems>();
    	
    }
    
}
