package com.globalnest.objects;

import java.io.Serializable;

public class EventObjects implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  public String state="",roles="",image="",SADashboardimgUrl="",country="",company="",RegistrationLink="",sessiontime="",lastRefreshDate="",countrycodes="";
  public String selfcheckinbgcolor="",selfcheckinoption="",selfcheckineventlogo="";
  public Event Events = new Event();
  @Override
  public String toString(){
	  return "EventObjects [state="+state+",roles="+roles+",image="+image
			  +",country="+country
			  +",company"+company
			  +",RegistrationLink="+RegistrationLink
			  +",sessiontime="+sessiontime
			  +",Events="+Events+"]";
  }
}
