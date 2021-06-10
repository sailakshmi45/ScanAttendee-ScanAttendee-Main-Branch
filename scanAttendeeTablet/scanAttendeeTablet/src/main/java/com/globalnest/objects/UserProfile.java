package com.globalnest.objects;



public class UserProfile {

	public String Email__c = "", First_Name__c = "", Last_Name__c = "",
			Company_Name__c = "",Default_Company_ID__c="", Prefix__c = "",
			Address1__c = "", Address2__c = "", Zip_Code__c="", City__c = "", 
			Mobile__c = "",User_Pic__c="", Suffix__c = "", Title__c = "",
			State__c = "", Country__c = "",Id = "",paypal="",boothleadfee="";
	public String Userid = "", profilestate = "", profileimage = "",profilecountry = "",profileCity="";
	
	public UserCompany Default_Company_ID__r=new UserCompany();

	@Override
	public String toString() {
		return "Profile [First_Name__c=" + First_Name__c + ",Last_Name__c="
				+ Last_Name__c + ",Email__c=" + Email__c
				+ ",Company_Name__c=" + Company_Name__c +",Default_Company_ID__c="+Default_Company_ID__c
				+ ",Prefix__c="	+ Prefix__c + ",Address1__c=" + Address1__c
				+ ",Address2__c=" + Address2__c + ",City__c=" + City__c
				+ ",Zip_Code__c=" + Zip_Code__c + ",Mobile__c=" + Mobile__c
				+ ",Suffix__c=" + Suffix__c + ",Title__c=" + Title__c
				+ ",State__c=" + State__c + ",Country__c=" + Country__c
				+",User_Pic__c="+User_Pic__c+",Id=" +Id
				+",paypal="+paypal
				+",Default_Company_ID__r="+Default_Company_ID__r
				+",boothleadfee="+boothleadfee+ "]";
	}
}
