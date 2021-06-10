package com.globalnest.mvc;

import java.io.Serializable;

import com.globalnest.objects.RegSettings;

import RLSDK.bo;

public class TicketPoolHandler implements Serializable{
	
	private static final long serialVersionUID = 1L;
	int Item_Count__c=0,Addon_Count__c=0;
	private  String Item_Pool_Name__c="", Name="",Item_Type__c="", Id="", Package_Flag__c="",
			        Addon_Parent__c="",Badgable__c="",Ticket_Settings__c="",Badge_Label__c="";
	private boolean Ticketed_Sessions__c = false;

	public String getPoolItemName() {
		return Item_Pool_Name__c;
	}
	public void setPoolItemName(String poolItemName) {
		this.Item_Pool_Name__c = poolItemName;
	}
	public String getPoolName() {
		return Name;
	}
	public void setPoolName(String poolName) {
		this.Name = poolName;
	}
	public String getPoolItemType() {
		return Item_Type__c;
	}
	public void setPoolItemType(String poolItemType) {
		this.Item_Type__c = poolItemType;
	}
	public String getPoolId() {
		return Id;
	}
	public void setPoolId(String poolId) {
		this.Id = poolId;
	}
	public String getPoolPackage() {
		return Package_Flag__c;
	}
	public void setPoolPackage(String poolPackage) {
		this.Package_Flag__c = poolPackage;
	}
	public int getPoolItemCount() {
		return Item_Count__c;
	}
	public void setPoolItemCount(int poolItemCount) {
		this.Item_Count__c = poolItemCount;
	}
	public void setAddonCount(int addoncount){
		this.Addon_Count__c = addoncount;
	}
	public int getAddonCount(){
		return Addon_Count__c;
	}
	public void setAddonParantId(String addonid){
		this.Addon_Parent__c = addonid;
	}
	public String getAddonParentId(){
		return Addon_Parent__c;
	}
	public String getBadgeLabel(){
		return Badge_Label__c;
	}
	public void setBadgeAble(String badgelable) {
		this.Badgable__c = badgelable;
	}

	public String getBadgable() {
		return Badgable__c;
	}
	public void setItemSettings(String Ticket_Settings__c){
		this.Ticket_Settings__c = Ticket_Settings__c;
	}

	public String getItemSettings(){
		return Ticket_Settings__c;
	}
	
	public void setTicketed_Sessions__c(boolean session){
		this.Ticketed_Sessions__c = session;
	}
	public boolean getTicketed_Sessions__c(){
		return Ticketed_Sessions__c;
	}
	
	public RegSettings Reg_Settings__r = new RegSettings();
	@Override
	public String toString() {
		return "TicketPoolHandler[Name=" + Name + ", Item_Pool_Name__c="
				+ Item_Pool_Name__c + ", Item_Type__c=" + Item_Type__c
				+ ", Package_Flag__c=" + Package_Flag__c + ", Item_Count__c="
				+ Item_Count__c + ", Id=" + Id +", Addon_Count__c="+Addon_Count__c
				+", Addon_Parent__c="+Addon_Parent__c+", Badgable__c="+Badgable__c
				+", Badge_Label__c="+Badge_Label__c
				+",Ticket_Settings__c="+Ticket_Settings__c+"]";
	}

}
