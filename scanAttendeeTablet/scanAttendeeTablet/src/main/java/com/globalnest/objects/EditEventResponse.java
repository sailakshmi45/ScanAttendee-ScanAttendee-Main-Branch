/** 
 *GlobalNest
 * Ajay Goyal
 */
package com.globalnest.objects;

/**
 * @author Mayank Mishra
 *
 */
public class EditEventResponse {
	
	public String company="",image="",roles="",state="",country="",RegistrationLink="",sessiontime="",lastRefreshDate="";
	
	public Event Events=new Event();

	@Override
	public String toString() {
		return "EditEventObject [company=" + company + ", image=" + image
				+ ", roles=" + roles + ", state=" + state 
				+ ", Events="+ Events 
				+ ", RegistrationLink="+ RegistrationLink 
				+", country="+ country +",sessiontime="+sessiontime+ "]";
	}

	
	
}
