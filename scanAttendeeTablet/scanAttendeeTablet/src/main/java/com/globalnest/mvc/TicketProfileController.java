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
public class TicketProfileController implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public AddressCotroller Home_Address__r = new AddressCotroller();
	public AddressCotroller Work_Address__r = new AddressCotroller();
	public AddressCotroller Billing_Address__r = new AddressCotroller();

	/*public String Id="",First_Name__c="",Last_Name__c="",Email__c="",Age__c="",Home_Address__c="",
			Home_Phone__c="",Keywords__c="",Gender__c="",TKT_Company__c="",TKT_Job_Title__c="",
			Tax_Id__c="",Work_Address__c="",Work_Phone__c="",Video__c="",User_Pic__c="",TwitterId__c="",
			Suffix__c="",Shipping_Address__c="",Secondary_Business_Category__c="",Revenue__c="",Primary_Business_Category__c="",
			Prefix__c="",Number_Of_Employees__c="",Mobile__c="",LinkedInId__c="",FourSquareId__c="",FaceBookId__c="",
			Established_Date__c="",Duns_Number__c="",DOB__c="",Company_Website_URL__c="",Company_Logo__c="",Company_Description__c="",
			Blog_URL__c="",Blogger__c="",BLN_GN_User__c="",BLN_Company__c="",Biography__c="",Billing_Address__c="";*/


	public String Id="",Age__c="",BBB_Number__c="",Billing_Address__c="",Biography__c="",BLN_Company__c="",
	Bln_Custom_Fields__c="",BLN_GN_User__c="",BLN_MM_Accepted_Appointments__c="",BLN_MM_Blocked_Appointments__c="",
	BLN_MM_Cancelled_Appointments__c="",BLN_MM_EnableSchedules__c="",BLN_MM_Lead_Score__c="",BLN_MM_Pending_Appointments__c="",
	BLN_MM_ProfileStatus__c="",BLN_MM_Report_TKT_Table__c="",BLN_MM_Total_Appointments__c="",Blog_URL__c="",Blogger__c="",
	Business_Structure__c="",CageCode__c="",Company_Description__c="",Company_Logo__c="",Company_Website_URL__c="",DBA__c="",
	distribution_Country__c="",DOB__c="",Duns_Number__c="",Email__c="",Established_Date__c="",Ethnicity__c="",
	Exceptional_Keywords__c="",FaceBookId__c="",FaxNumber__c="",First_Name__c="",FourSquareId__c="",Full_Name__c="",Gender__c="",
	Geographical_Region__c="",GSA_Schedule__c="",Home_Address__c="",Home_Phone__c="",Instagram__c="",IP_Address__c="",Keywords__c="",
	Last_Name__c="",LinkedInId__c="",Manufactures_Country__c="",Mobile__c="",Number_Of_Employees__c="",Outside_Facilities__c="",
	Prefix__c="",Primary_Business_Category__c="",References1__c="",References2__c="",Revenue__c="",RSVP__c="",ScopeOfWork1__c="",
	ScopeOfWork2__c="",Secondary_Business_Category__c="",Secondary_email__c="",Shipping_Address__c="",Skype__c="",Snapchat__c="",
	Suffix__c="",Tax_Id__c="",TKT_Company__c="",TKT_Job_Title__c="",TwitterId__c="",User_Pic__c="",Video__c="",Wechat__c="",WhatsApp__c="",
	Work_Address__c="",Work_Phone__c="",Year_in_business__c="",Phone__c="";
	public String SpeakerLinkedInId="",Location_Room__c="",SpeakerTwitterId="",SpeakerBlogger="",SpeakerVideo="";

}
