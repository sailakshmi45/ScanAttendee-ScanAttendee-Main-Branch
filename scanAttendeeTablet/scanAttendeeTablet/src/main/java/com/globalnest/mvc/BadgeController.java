/** 
 *GlobalNest
 * Ajay Goyal
 */
package com.globalnest.mvc;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * @author Mayank Mishra
 *
 */
public class BadgeController {
	
	@SerializedName("width")
	String badgewith;

	@SerializedName("height")
	String badgeheight;

	@SerializedName("EventId")
	String eventid;

	@SerializedName("name")
	String badgename;

	@SerializedName("backgroundColor")
	String badgebackgroundcolor;

	@SerializedName("templateType")
	String badgetemplatetype;

	String isSelected="";
	
	@SerializedName("elemsbg")
	public ArrayList<BadgeElements> badge_elm;
	
	public  BadgeController()
	{
		badge_elm=new ArrayList<BadgeElements>();
	}

	public String getBadgewith() {
		return badgewith;
	}

	public void setBadgewith(String badgewith) {
		this.badgewith = badgewith;
	}

	public String getBadgeheight() {
		return badgeheight;
	}

	public void setBadgeheight(String badgeheight) {
		this.badgeheight = badgeheight;
	}

	public String getEventid() {
		return eventid;
	}

	public void setEventid(String eventid) {
		this.eventid = eventid;
	}

	public String getBadgename() {
		return badgename;
	}

	public void setBadgename(String badgename) {
		this.badgename = badgename;
	}

	public String getBadgebackgroundcolor() {
		return badgebackgroundcolor;
	}

	public void setBadgebackgroundcolor(String badgebackgroundcolor) {
		this.badgebackgroundcolor = badgebackgroundcolor;
	}

	public String getBadgetemplatetype() {
		return badgetemplatetype;
	}

	public void setBadgetemplatetype(String badgetemplatetype) {
		this.badgetemplatetype = badgetemplatetype;
	}

	public void setSelectedTemplate(String value) {
		this.isSelected = value;
	}

	public String getSelectedTemplate() {
		return isSelected;
      }
	
	@Override
	public String toString() {
		return "BadgeController [badgewith=" + badgewith + ", badgeheight="
				+ badgeheight + ", eventid=" + eventid + ", badgename="
				+ badgename + ", badgebackgroundcolor=" + badgebackgroundcolor
				+ ", badgetemplatetype=" + badgetemplatetype + ", badge_elm="
				+ badge_elm + "]";
	}
	
	
	
	

}
