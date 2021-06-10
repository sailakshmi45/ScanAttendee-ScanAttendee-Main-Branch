// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import java.util.ArrayList;



// Referenced classes of package com.globalnest.mvc:
//            OrderListHandler, PaymentTypeHandler

public class OrderDetailsHandler
{
	//public String siteURL="";
	public ArrayList<OrderItemListHandler> ticketsInn = new ArrayList<OrderItemListHandler>();
	public ArrayList<OrderItemPoolHandler> orderItemInn = new ArrayList<OrderItemPoolHandler>();
	public ArrayList<AttendeeAddressHandler> compListInner=new ArrayList<AttendeeAddressHandler>();
	public ArrayList<TicketTagHandler> ticketTags=new ArrayList<TicketTagHandler>();
	public ArrayList<OrderItemListHandler> cancelledTickets = new ArrayList<OrderItemListHandler>();
	public OrderListHandler orderInn = new OrderListHandler();
	//public PaymentTypeHandler paymentInn = new PaymentTypeHandler();
	public ArrayList<PaymentTypeHandler> paymentInnmultiple = new ArrayList<PaymentTypeHandler>();
	public ArrayList<BadgeIDs> ticklist = new ArrayList<BadgeIDs>();//for badge id
	//for naics,diversities list
	public ArrayList<MultiListValues> listValues = new ArrayList<MultiListValues>();
	public void setPaymentInn(ArrayList<PaymentTypeHandler> payinn){
		paymentInnmultiple=payinn;
	}
	/*@Override
	public String toString() {
		return "OrderDetailsHandler{" +
				"siteURL='" + siteURL + '\'' +
				", ticketsInn=" + ticketsInn +
				", orderItemInn=" + orderItemInn +
				", compListInner=" + compListInner +
				", ticketTags=" + ticketTags +
				", cancelledTickets=" + cancelledTickets +
				", orderInn=" + orderInn +
				", paymentInn=" + paymentInn +
				'}';
	}*/
	@Override
	public String toString() {
		return "OrderDetailsHandler [orderInn=" + orderInn + ", orderItemInn="
				+ orderItemInn + ", paymentInnmultiple=" + paymentInnmultiple + ", ticketsInn="+ ticketsInn
				+ ", ticketTags="+ ticketTags+ ", compListInner="+ compListInner+ "]";
	}

	public class BadgeIDs{
		public BadgeParent badgeparentid__r= new BadgeParent();

		public String Badge_ID__c="",Id="",badgeparentid__c="";
		public class BadgeParent{
			public String Badge_ID__c = "";
		}
	}
	public class Listvalues{


	}
    

   /* public String toString()
    {
        return (new StringBuilder("OrderDetailsHandler [ticketsInn=")).append(ticketsInn).append(", orderItemInn=").append(orderItemInn).append(", orderInn=").append(orderInn).append(", paymentInn=").append(paymentInn).append("]").toString();
    }*/
}
