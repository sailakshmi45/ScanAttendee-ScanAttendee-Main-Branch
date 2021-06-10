package com.globalnest.mvc;

import java.util.ArrayList;

public class TicketResponseHandler {
	public String Enddate;
	public String Startdate;
	public String ImageUrl;
	public String siteurl;
	public String Fee;
	public ArrayList<TicketHandler> Items = new ArrayList<TicketHandler>();
	public TicketPoolHandler Itempool= new TicketPoolHandler();

	@Override
	public String toString(){
		return "TicketResponseHandler [Items="+Items+ ", Startdate="+Startdate+", Enddate="+Enddate+", ImageUrl="+ImageUrl+", Itempool="+Itempool+", siteurl="+siteurl+"]";
	}

}
