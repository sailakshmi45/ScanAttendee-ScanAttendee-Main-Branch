package com.globalnest.mvc;

import java.util.ArrayList;
import java.util.List;

public class RefreshResponse {

	public EventsRefreshObject BLN_ASC_EventsOUTPUT=new EventsRefreshObject();
	public List<TotalOrderListHandler> BLN_ASC_CheckinOUTPUT=new ArrayList<TotalOrderListHandler>();
	public List<TicketListResponseHandler> BLN_ASC_ItemsListOUTPUT=new ArrayList<TicketListResponseHandler>();
	public String LastRefreshedDate = "";
	public ArrayList<OrderDetailsHandler> TotalLists = new ArrayList<OrderDetailsHandler>();
	public ArrayList<TicketTagHandler> ticketTags=new ArrayList<TicketTagHandler>();
	public ArrayList<Itemlist> itemlist=new ArrayList<Itemlist>();
	public String noofBatch="";
	public boolean isLoadBadgeId=true;
	//public TotalOrderListHandler 
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RefreshResponse [BLN_ASC_EventsOUTPUT=" + BLN_ASC_EventsOUTPUT
				+ ", BLN_ASC_CheckinOUTPUT=" + BLN_ASC_CheckinOUTPUT
				+ ", BLN_ASC_ItemsListOUTPUT=" + BLN_ASC_ItemsListOUTPUT + "]";
	}
	
	
	public class Itemlist{
		public String Id="",Hide_Ticket_SA__c="";
	}

}
