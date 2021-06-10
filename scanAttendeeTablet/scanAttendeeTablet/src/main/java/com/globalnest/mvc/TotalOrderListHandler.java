// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import java.util.ArrayList;

public class TotalOrderListHandler {

	public ArrayList<OrderDetailsHandler> TotalLists = new ArrayList<OrderDetailsHandler>();
	public ArrayList<TicketTagHandler> ticketTags=new ArrayList<TicketTagHandler>();
    public String errorMsg = "";
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "TotalOrderListHandler [TotalLists=" + TotalLists + "]";
	}

}
