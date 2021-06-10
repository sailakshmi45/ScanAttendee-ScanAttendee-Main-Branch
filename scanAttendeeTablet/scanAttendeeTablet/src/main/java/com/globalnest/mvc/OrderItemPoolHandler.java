// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import com.google.gson.annotations.SerializedName;


public class OrderItemPoolHandler
{
	@SerializedName("Each_Ticket_Price__c")
    private Double OrderEachItemPrice;
    
	@SerializedName("Order__c")
	private String OrderId;
    
	@SerializedName("Item_Discount__c")
	private Double OrderItemDiscount;
    
	@SerializedName("Item__c")
	private String OrderItemId;
    
	@SerializedName("Id")
	private String OrderItemPoolId;
    
	@SerializedName("Item_Quantity__c")
	private String OrderItemQty;
    
	@SerializedName("Item_Total__c")
	private Double OrderItemTotalAmount;
    
	@SerializedName("Name")
	private String OrderPoolName;

	@SerializedName("Status__c")
	private String OrderItemStatus;
	
	@SerializedName("Currency__c")
	private String OrderItemCurrency;
	
	@SerializedName("Tax__c")
	private String OrderItemTax;
	
	@SerializedName("Item_Fee__c")
	private String OrderItemFee;
	
	@SerializedName("Item__r")
	public OrderItemTypeHandler itemType = new OrderItemTypeHandler();
	
    public OrderItemPoolHandler(){
    	itemType = new OrderItemTypeHandler();
    }

    public Double getOrderEachItemPrice()
    {
        return OrderEachItemPrice;
    }

    public String getOrderId()
    {
        return OrderId;
    }

    public Double getOrderItemDiscount()
    {
        return OrderItemDiscount;
    }

    public String getOrderItemId()
    {
        return OrderItemId;
    }

    public String getOrderItemPoolId()
    {
        return OrderItemPoolId;
    }

    public String getOrderItemQty()
    {
        return OrderItemQty;
    }

    public Double getOrderItemTotalAmount()
    {
        return OrderItemTotalAmount;
    }

    public String getOrderPoolName()
    {
        return OrderPoolName;
    }

    public void setOrderEachItemPrice(Double double1)
    {
        OrderEachItemPrice = double1;
    }

    public void setOrderId(String s)
    {
        OrderId = s;
    }

    public void setOrderItemDiscount(Double double1)
    {
        OrderItemDiscount = double1;
    }

    public void setOrderItemId(String s)
    {
        OrderItemId = s;
    }

    public void setOrderItemPoolId(String s)
    {
        OrderItemPoolId = s;
    }

    public void setOrderItemQty(String s)
    {
        OrderItemQty = s;
    }

    public void setOrderItemTotalAmount(Double double1)
    {
        OrderItemTotalAmount = double1;
    }

    public void setOrderPoolName(String s)
    {
        OrderPoolName = s;
    }

    public void setOrderItemStatus(String orderitemstatus){
    	this.OrderItemStatus = orderitemstatus;
    }
    public String getOrderItemStatus(){
    	return OrderItemStatus;
    }
    public void setOrderItemCurrency(String currency){
    	this.OrderItemCurrency = currency;
    }
    public String getOrderItemCurrency(){
    	return OrderItemCurrency;
    }
    public void setOrderItemFee(String orderitemfee){
    	this.OrderItemFee = orderitemfee;
    }
    public String getOrderItemFee(){
      return OrderItemFee;	
    }
    
    public void setOrderItemTax(String orderitemtax){
    	this.OrderItemTax = orderitemtax;
    }
    public String getOrderItemTax(){
      return OrderItemTax;	
    }
	@Override
	public String toString() {
		return "OrderItemPoolHandler [OrderEachItemPrice=" + OrderEachItemPrice
				+ ", OrderId=" + OrderId + ", OrderItemDiscount="
				+ OrderItemDiscount + ", OrderItemId=" + OrderItemId
				+ ", OrderItemPoolId=" + OrderItemPoolId + ", OrderItemQty="
				+ OrderItemQty + ", OrderItemTotalAmount="
				+ OrderItemTotalAmount + ", OrderPoolName=" + OrderPoolName + ", itemType="+itemType
				+ "]";
	}
    
    
    

    /*public String toString()
    {
        return (new StringBuilder("OrderItemPoolHandler [OrderItemPoolId=")).append(OrderItemPoolId).append(", OrderItemDiscount=").append(OrderItemDiscount).append(", OrderItemQty=").append(OrderItemQty).append(", OrderItemTotalAmount=").append(OrderItemTotalAmount).append(", OrderItemId=").append(OrderItemId).append(", OrderPoolName=").append(OrderPoolName).append(", OrderId=").append(OrderId).append("]").toString();
    }*/
}
