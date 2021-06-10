package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class DashboradHandler implements Serializable{
	
	
	public int checloutCnt=0,chekinCont=0,notCheckdin=0,totalAttendee=0,totalcanOrders=0,totalcanTiccnt=0,totalcompOrders=0,
			totalNoncompOrds=0,totalOrders=0,totalSoldTickets=0,totalTickets=0,OnsiteAttendeereg=0,OnsiteOrderCount=0;
	public double mobileRevenue=0,onsiteRevenue=0,totalrevenue=0,onlineRevenue=0,OnsitePaymentRevenue=0;
	public boolean availableScanAttendeeTicket=true;
	public List<DashboardSessionReport> sessionsReport = new ArrayList<DashboardSessionReport>();
	
	@SerializedName("ItemRevList")
	public ArrayList<DashboardTicketHandler> dashboardticketsList = new ArrayList<DashboardTicketHandler>();
	
	@SerializedName("innRevList")
	public ArrayList<DashboardTicketPaymentHandler> dashboardTicketsPaymentList = new ArrayList<DashboardTicketPaymentHandler>();
	
	@Override
	public String toString() {
		return "DashboradHandler [checloutCnt=" + checloutCnt + ", chekinCont="
				+ chekinCont + ", mobileRevenue=" + mobileRevenue
				+ ", notCheckdin=" + notCheckdin + ", onlineRevenue="
				+ onlineRevenue + ", onsiteRevenue=" + onsiteRevenue
				+ ", totalAttendee=" + totalAttendee + ", totalcanOrders="
				+ totalcanOrders + ", totalcanTiccnt=" + totalcanTiccnt
				+ ", totalcompOrders=" + totalcompOrders
				+ ", totalNoncompOrds=" + totalNoncompOrds + ", totalOrders="
				+ totalOrders + ", totalrevenue=" + totalrevenue
				+ ", totalSoldTickets=" + totalSoldTickets + ", totalTickets="
				+ totalTickets + ", dashboardticketsList=" + dashboardticketsList
				+ ", dashboardTicketsPaymentList=" + dashboardTicketsPaymentList
				+ "]";
	}
	
}
