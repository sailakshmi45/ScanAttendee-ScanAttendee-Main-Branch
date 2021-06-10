// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;


public class BuyerInfoHandler implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SerializedName("Email__c")
    private String email;
	
	@SerializedName("First_Name__c")
    private String firstName;
	//newly added User_Pic__c

	@SerializedName("User_Pic__c")
	private String UserPic;

	@SerializedName("Id")
    private String id;
	
	@SerializedName("Last_Name__c")
    private String lastName;
	
	@SerializedName("Mobile__c")
    private String mobile;
	
	@SerializedName("BLN_GN_User__c")
    private String buyer_userid;
	
	@SerializedName("City__C")
    private String buyer_city;
	
	@SerializedName("Zip_Code__c")
    private String buyer_zipcode;
	
	@SerializedName("Address1__c")
    private String buyer_address1;
	
	@SerializedName("Address2__c")
    private String buyer_address2;
	
	@SerializedName("TKT_Company__c")
	private String company;
	
	
	public String buyer_state;
	//Fields for Registration Form
	
	String tag="",designation="",seatno="",badge_lable="",note="",custom_barcode="";
	

    public String getBuyer_userid() {
		return buyer_userid;
	}

	public void setBuyer_userid(String buyer_userid) {
		this.buyer_userid = buyer_userid;
	}

	public String getBuyer_city() {
		return buyer_city;
	}

	public void setBuyer_city(String buyer_city) {
		this.buyer_city = buyer_city;
	}

	public String getBuyer_zipcode() {
		return buyer_zipcode;
	}

	public void setBuyer_zipcode(String buyer_zipcode) {
		this.buyer_zipcode = buyer_zipcode;
	}

	public String getBuyer_address1() {
		return buyer_address1;
	}

	public void setBuyer_address1(String buyer_address1) {
		this.buyer_address1 = buyer_address1;
	}

	public String getBuyer_address2() {
		return buyer_address2;
	}

	public void setBuyer_address2(String buyer_address2) {
		this.buyer_address2 = buyer_address2;
	}



    public String getEmail()
    {
        return email;
    }

    public String getFirstName()
    {
        return firstName;
    }

	public String getUserPic()
	{
		return UserPic;
	}


	public String getId()
    {
        return id;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getMobile()
    {
        return mobile;
    }

    public void setEmail(String s)
    {
        email = s;
    }

    public void setFirstName(String s)
    {
        firstName = s;
    }

	public void setUserPic(String s)
	{
		UserPic = s;
	}

	public void setId(String s)
    {
        id = s;
    }

    public void setLastName(String s)
    {
        lastName = s;
    }

    public void setMobile(String s)
    {
        mobile = s;
    }
    
    public void setCompany(String s){
    	this.company = s;
    }
    public String getCompany(){
    	return company;
    }
    
    
//for registration form

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the designation
	 */
	public String getDesignation() {
		return designation;
	}

	/**
	 * @param designation the designation to set
	 */
	public void setDesignation(String designation) {
		this.designation = designation;
	}

	/**
	 * @return the seatno
	 */
	public String getSeatno() {
		return seatno;
	}

	/**
	 * @param seatno the seatno to set
	 */
	public void setSeatno(String seatno) {
		this.seatno = seatno;
	}

	/**
	 * @return the badge_lable
	 */
	public String getBadge_lable() {
		return badge_lable;
	}

	/**
	 * @param badge_lable the badge_lable to set
	 */
	public void setBadge_lable(String badge_lable) {
		this.badge_lable = badge_lable;
	}
	
	

	

	/**
	 * @return the company_id
	 */
	public String getnote() {
		return note;
	}

	/**
	 * @param company_id the company_id to set
	 */
	public void setnote(String note) {
		this.note = note;
	}
	
	public void setCustomBarcode(String custom_barcode){
		this.custom_barcode = custom_barcode;
	}
	public String getCustomBarcode(){
		return custom_barcode;
	}
	

	@Override
	public String toString() {
		return "BuyerInfoHandler [email=" + email + ", firstName=" + firstName + ", id=" + id + ", lastName=" + lastName
				+ ", mobile=" + mobile + ", buyer_address1=" + buyer_address1 + ", buyer_address2=" + buyer_address2
				+ ", buyer_city=" + buyer_city + ", buyer_userid=" + buyer_userid + ", company=" + company
				+ ", buyer_zipcode=" + buyer_zipcode + "]";
	}

    /*public String toString()
    {
        return (new StringBuilder("BuyerInfoHandler [firstName=")).append(firstName).append(", lastName=").append(lastName).append(", email=").append(email).append(", mobile=").append(mobile).append(", id=").append(id).append("]").toString();
    }*/
    
    
    
}
