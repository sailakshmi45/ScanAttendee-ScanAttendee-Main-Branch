
package com.globalnest.mvc;

import com.google.gson.annotations.SerializedName;


public class PaymentTypeHandler
{
    public String CreatedDate="",Settlement_Date__c="",credit_card_last_4digits__c="",credit_card_type__c="",Currency_Code__c="",Id="",
            LastModifiedDate="",Name="",Order__c="",Payment_Amount__c="",Payment_Mode__c="",Payment_Ref_Number__c="",
            Payment_Status__c="",Payment_Type__c="",Pay_reg_type__c="",Note__c="";
	 /* *//*@SerializedName("Id")
    private String Id="";*//*
    
	  @SerializedName("Name")
    private String PayKey="";
   
	  @SerializedName("Payment_Mode__c")
    private String PaymentMode="";
   
	  @SerializedName("Payment_Type__c")
    private String PaymentType="";
	  
	  @SerializedName("Payment_Ref_Number__c")
	  private String CheckNumber="";
	  
	  @SerializedName("credit_card_type__c")
	  private String CreditCardType="";
	  
	  //------------------------
	  
	  @SerializedName("credit_card_last_4digits__c")
	  private String last4degits="";
	  
	  @SerializedName("Registration_Type__c")
	  private String RegistrationType="";*/
	
	  
      
	  public BLNPayGateWayType BLN_Pay_Gateway__r = new BLNPayGateWayType();
    /**
	 * @return the checkNumber
	 */
	/*public String getCheckNumber() {
		return CheckNumber;
	}

	*//**
	 * @param checkNumber the checkNumber to set
	 *//*
	public void setCheckNumber(String checkNumber) {
		CheckNumber = checkNumber;
	}

	*//**
	 * @return the creditCardType
	 *//*
	public String getCreditCardType() {
		return CreditCardType;
	}

	*/


    @Override
    public String toString() {
        return "PaymentTypeHandler{" +
                "CreatedDate='" + CreatedDate +
                ", credit_card_last_4digits__c='" + credit_card_last_4digits__c +
                ", credit_card_type__c='" + credit_card_type__c +
                ", Currency_Code__c='" + Currency_Code__c +
                ", Id='" + Id +
                ", LastModifiedDate='" + LastModifiedDate +
                ", Name='" + Name +
                ", Order__c='" + Order__c +
                ", Payment_Amount__c='" + Payment_Amount__c +
                ", Payment_Mode__c='" + Payment_Mode__c +
                ", Payment_Ref_Number__c='" + Payment_Ref_Number__c +
                ", Payment_Status__c='" + Payment_Status__c +
                ", Payment_Type__c='" + Payment_Type__c +
                ", Pay_reg_type__c='" + Pay_reg_type__c +
                ", BLN_Pay_Gateway__r=" + BLN_Pay_Gateway__r +
                '}';
    }

    public class BLNPayGateWayType{
    	public PayGateWayType PGateway_Type__r = new PayGateWayType();
    }
    
    public class PayGateWayType{
    	public String Id="",Name="";
    }
    
    

  /*  public String toString()
    {
        return (new StringBuilder("PaymentTypeHandler [PayKey=")).append(PayKey).append(", PaymentMode=").append(PaymentMode).append(", PaymentType=").append(PaymentType).append(", mobile=").append(OrderId).append(", id=").append(OrderId).append("]").toString();
    }*/
}
