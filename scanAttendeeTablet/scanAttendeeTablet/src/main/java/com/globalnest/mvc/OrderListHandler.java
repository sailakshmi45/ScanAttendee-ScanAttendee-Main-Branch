// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;


// Referenced classes of package com.globalnest.mvc:
//            BuyerInfoHandler, PaymentTypeHandler

public class OrderListHandler
{
	@SerializedName("Total_Paid__c")
    private Double OrderAmountPaid;

    @SerializedName("Amount_Due__c")
    private Double OrderAmountDue;

    @SerializedName("CreatedDate")
    private String OrderDate;
    
    @SerializedName("Order_Discount__c")
    private Double OrderDiscount;
    
    @SerializedName("Fee_Amount__c")
    private Double OrderFeeAmount;
    
    @SerializedName("Id")
    private String OrderId;
    
    @SerializedName("Name")
    private String OrderName;
    
    @SerializedName("Order_Status__c")
    private String OrderStatus;
    
    @SerializedName("Order_Sub_Total__c")
    private Double OrderSubTotalAmount;
    
    @SerializedName("Order_Taxes__c")
    private Double OrderTaxAmount;
    
    @SerializedName("Order_Total__c")
    private Double OrderTotalAmount;

    @SerializedName("Registration_Type__c")
    private String OrderRegistrationType;

    @SerializedName("BLN_TKT_profile__r")
    public BuyerInfoHandler buyerInfo;
    
    @SerializedName("Client_Company__r")
    public ClientCompanyHandler compHandler;
    
    @SerializedName("Events__c")
    private String eventId;
    
    @SerializedName("BLN_TKT_profile__c")
    private String ClientGNUserId;


    //public PaymentTypeHandler records=new PaymentTypeHandler();

   // public TicketProfileController BLN_TKT_profile__r = new TicketProfileController();
    
    public int totalSize=0;
    
    public String getClientGNUserId() {
		return ClientGNUserId;
	}

	public void setClientGNUserId(String clientGNUserId) {
		ClientGNUserId = clientGNUserId;
	}



	@SerializedName("")
    private ArrayList<PaymentTypeHandler> paymentData;
    private PaymentTypeHandler paymentObjData;

    public OrderListHandler()
    {
        buyerInfo = new BuyerInfoHandler();
        compHandler = new ClientCompanyHandler();
    }

    public String getEventId()
    {
        return eventId;
    }

    public Double getOrderAmountPaid()
    {
        return OrderAmountPaid;
    }

    public Double getOrderAmountDue()
    {
        return OrderAmountDue;
    }
    public String getOrderDate()
    {
        return OrderDate;
    }

    public Double getOrderDiscount()
    {
        return OrderDiscount;
    }

    public Double getOrderFeeAmount()
    {
        return OrderFeeAmount;
    }

    public String getOrderId()
    {
        return OrderId;
    }

    public String getOrderName()
    {
        return OrderName;
    }

    public String getOrderStatus()
    {
        return OrderStatus;
    }

    public Double getOrderSubTotalAmount()
    {
        return OrderSubTotalAmount;
    }

    public Double getOrderTaxAmount()
    {
        return OrderTaxAmount;
    }

    public Double getOrderTotalAmount()
    {
        return OrderTotalAmount;
    }

    public String getOrderRegistrationType()
    {
        return OrderRegistrationType;
    }

    public ArrayList<PaymentTypeHandler> getPaymentData()
    {
        return paymentData;
    }
    public void setPaymentData(ArrayList<PaymentTypeHandler> paymenttypehandler)
    {
        paymentData = paymenttypehandler;
    }
    public PaymentTypeHandler getPaymentObjData()
    {
        return paymentObjData;
    }
    public void setPaymentObjData(PaymentTypeHandler paymenttypehandler)
    {
        paymentObjData = paymenttypehandler;
    }
    public void setEventId(String s)
    {
        eventId = s;
    }

    public void setOrderAmountPaid(Double double1)
    {
        OrderAmountPaid = double1;
    }

    public void setOrderDate(String s)
    {
        OrderDate = s;
    }

    public void setOrderDiscount(Double double1)
    {
        OrderDiscount = double1;
    }

    public void setOrderFeeAmount(Double double1)
    {
        OrderFeeAmount = double1;
    }

    public void setOrderId(String s)
    {
        OrderId = s;
    }

    public void setOrderName(String s)
    {
        OrderName = s;
    }

    public void setOrderStatus(String s)
    {
        OrderStatus = s;
    }

    public void setOrderSubTotalAmount(Double double1)
    {
        OrderSubTotalAmount = double1;
    }

    public void setOrderTaxAmount(Double double1)
    {
        OrderTaxAmount = double1;
    }

    public void setOrderTotalAmount(Double double1)
    {
        OrderTotalAmount = double1;
    }

    public void setOrderRegistrationType(String regtype)
    {
        OrderRegistrationType = regtype;
    }


    
    
	@Override
	public String toString() {
		return "OrderListHandler [OrderAmountPaid=" + OrderAmountPaid
				+ ", OrderDate=" + OrderDate + ", OrderDiscount="
				+ OrderDiscount + ", OrderFeeAmount=" + OrderFeeAmount
				+ ", OrderId=" + OrderId + ", OrderName=" + OrderName
				+ ", OrderStatus=" + OrderStatus + ", OrderSubTotalAmount="
				+ OrderSubTotalAmount + ", OrderTaxAmount=" + OrderTaxAmount
				+ ", OrderTotalAmount=" + OrderTotalAmount
                + ", OrderRegistrationType=" + OrderRegistrationType
                + ", buyerInfo=" + buyerInfo + ", eventId=" + eventId + ", paymentData="
				+ paymentData + ", ClientGNUserId="+ClientGNUserId
				+ ", compHandler="+compHandler+", totalSize="+totalSize+"]";
	}
    
    
    
    

   /* public String toString()
    {
        return (new StringBuilder("OrderListHandler [eventId=")).append(eventId).append(", OrderName=").append(OrderName).append(", OrderDiscount=").append(OrderDiscount).append(", OrderSubTotalAmount=").append(OrderSubTotalAmount).append(", OrderTotalAmount=").append(OrderTotalAmount).append(", OrderTaxAmount=").append(OrderTaxAmount).append(", OrderAmountPaid=").append(OrderAmountPaid).append(", OrderStatus=").append(OrderStatus).append(", OrderId=").append(OrderId).append(", OrderFeeAmount=").append(OrderFeeAmount).append(", OrderDate=").append(OrderDate).append(", buyerInfo=").append(buyerInfo).append("]").toString();
    }*/
}
