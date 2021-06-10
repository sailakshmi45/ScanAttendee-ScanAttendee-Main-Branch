// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import com.google.gson.annotations.SerializedName;


public class OrderItemTypeHandler
{

	@SerializedName("Id")
    private String itemId;
	
	@SerializedName("item_name__c")
    private String itemName;
	
	@SerializedName("Item_Pool__c")
    private String itemPoolId;
	
	@SerializedName("item_type__c")
    private String itemTypeId;

    public OrderItemTypeHandler()
    {
    }

    public String getItemId()
    {
        return itemId;
    }

    public String getItemName()
    {
        return itemName;
    }

    public String getItemPoolId()
    {
        return itemPoolId;
    }

    public String getItemTypeId()
    {
        return itemTypeId;
    }

    public void setItemId(String s)
    {
        itemId = s;
    }

    public void setItemName(String s)
    {
        itemName = s;
    }

    public void setItemPoolId(String s)
    {
        itemPoolId = s;
    }

    public void setItemTypeId(String s)
    {
        itemTypeId = s;
    }

	
    @Override
	public String toString() {
		return "OrderItemTypeHandler [itemId=" + itemId + ", itemName="
				+ itemName + ", itemPoolId=" + itemPoolId + ", itemTypeId="
				+ itemTypeId + "]";
			}
    
    
    

    /*public String toString()
    {
        return (new StringBuilder("OrderItemTypeHandler [itemName=")).append(itemName).append(", itemId=").append(itemId).append(", itemPoolId=").append(itemPoolId).append(", itemTypeId=").append(itemTypeId).append("]").toString();
    }*/
}
