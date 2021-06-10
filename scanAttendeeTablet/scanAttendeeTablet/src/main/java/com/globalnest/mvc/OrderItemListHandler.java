// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.globalnest.mvc;

import java.io.Serializable;

import com.globalnest.utils.Util;
import com.google.gson.annotations.SerializedName;

// Referenced classes of package com.globalnest.mvc:
//            BuyerInfoHandler

public class OrderItemListHandler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@SerializedName("BLN_GN_User__c")
	private String GNUserId;

	@SerializedName("Ticket_Status__c")
	public String TicketStatus;

	@SerializedName("RSVP__c")
	public String RSVPstatus;

	@SerializedName("Client_Company__c")
	public String company_id;

	@SerializedName("Client_GN_User__r")
	public BuyerInfoHandler attendeeInfo;

	@SerializedName("Badge_Label__c")
	private String badgeLabel;

	@SerializedName("Company__c")
	private String companyName;

	@SerializedName("Email__c")
	private String email;

	@SerializedName("Event__c")
	private String eventId;

	@SerializedName("First_Name__c")
	private String firstName;

	@SerializedName("User_Pic__c")
	private String UserImage;

	@SerializedName("Id")
	private String id;

	@SerializedName("Item__c")
	private String itemId;

	@SerializedName("Item_Pool__c")
	private String itemPoolId;

	@SerializedName("Item_Type__c")
	private String itemTypeId;

	@SerializedName("Last_Name__c")
	private String lastName;

	@SerializedName("Mobile__c")
	private String mobile;

	@SerializedName("Order__c")
	private String orderId;

	@SerializedName("Order_Item__c")
	private String orderItemId;

	@SerializedName("Name")
	private String ticketNumber;
	
	@SerializedName("Badge_ID__c")
	private String badgeId="";

	@SerializedName("LastModifiedDate")
	private String chickindate;
	
	//newly added tag
	
	@SerializedName("Note__c")
	private String note;
	
	@SerializedName("Tag_No__c")
	private String seatno;
	
	@SerializedName("TKT_Job_Title__c")
	private String designation;
	
	@SerializedName("badgeparentid__c")
	private String badgeparent_id;
	@SerializedName("Parent_ID__c")
	private String parent_id;
	@SerializedName("Custom_Barcode__c")
	private String custom_barcode_id;
	
	@SerializedName("Scan_Id__c")
	private String scan_id;
	
	public ItemPoor_R Item_Pool__r = new ItemPoor_R();
	public ItemType_R  Item_Type__r = new ItemType_R();
	public BadgeParentId_R badgeparentid__r = new BadgeParentId_R();
	public BadgeID_R Badge_ID__r = new BadgeID_R();
	public TStatus_R Tstatus__r = new TStatus_R();
    /*public String Id;*/
	
	@SerializedName("Tstatus_Id__r")
	public CheckinStatusHandler checkinhandler;
	
	public TicketProfileController tkt_profile__r = new TicketProfileController();
	
	public int totalSize=0;
	private String ticket_name="",item_type="";
	public OrderItemListHandler() {
		attendeeInfo = new BuyerInfoHandler();
		//checkinhandler = new CheckinStatusHandler();
	}
	
	
	//for Badge Lable Fields in Registration form
	
	public String tag="";
	

	public String getBadgeLabel() {
		return badgeLabel;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getEmail() {
		return email;
	}

	public String getEventId() {
		return eventId;
	}

	public String getFirstName() {
		return firstName;
	}
	public String getUserImage() {
		return UserImage;
	}
	public String getGNUserId() {
		return GNUserId;
	}

	public String getId() {
		return id;
	}

	public String getItemId() {
		return itemId;
	}

	public String getItemPoolId() {
		return itemPoolId;
	}

	public String getItemTypeId() {
		return itemTypeId;
	}

	public String getLastName() {
		return lastName;
	}

	public String getMobile() {
		return mobile;
	}

	public String getOrderId() {
		return orderId;
	}

	public String getOrderItemId() {
		return orderItemId;
	}

	public String getTicketNumber() {
		return ticketNumber;
	}

	public String getTicketStatus() {
		return TicketStatus;
	}

	public String getRSVPStatus() {
		return RSVPstatus;
	}
	public void setBadgeLabel(String s) {
		badgeLabel = s;
	}

	public void setCompanyName(String s) {
		companyName = s;
	}

	public void setEmail(String s) {
		email = s;
	}

	public void setEventId(String s) {
		eventId = s;
	}

	public void setFirstName(String s) {
		firstName = s;
	}
	public void setAttendeeImage(String s) {
		UserImage = s;
	}
	public void setGNUserId(String s) {
		GNUserId = s;
	}

	public void setId(String s) {
		id = s;
	}

	public void setItemId(String s) {
		itemId = s;
	}

	public void setItemPoolId(String s) {
		itemPoolId = s;
	}

	public void setItemTypeId(String s) {
		itemTypeId = s;
	}

	public void setLastName(String s) {
		lastName = s;
	}

	public void setMobile(String s) {
		mobile = s;
	}

	public void setOrderId(String s) {
		orderId = s;
	}

	public void setOrderItemId(String s) {
		orderItemId = s;
	}

	public void setTicketNumber(String s) {
		ticketNumber = s;
	}

	public void setTicketStatus(String s) {
		TicketStatus = s;
	}
	public void setRSVPStatus(String s) {
		RSVPstatus = s;
	}
	public String getChickindate() {
		return chickindate;
	}

	public void setChickindate(String chickindate) {
		this.chickindate = chickindate;
	}
	
	public String getBadgeId() {
		return badgeId;
	}

	public void setBadgeId(String badgeId) {
		this.badgeId = badgeId;
	}
	public void setPackageTicketName(String ticketname){
		this.ticket_name = ticketname;
	}
	public String getPackageTicketName(){
		return ticket_name;
	}
	public void setItemTypeName(String item_type){
		this.item_type = item_type;
	}
	public String getItemTypeName(){
		return item_type;
	}
	
	
	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * @return the designation
	 */
	public String getDesignation() {
		return designation;
	}

	/**
	 * @param designation the designation to set
	 */
	public void setDesignation(String designation) {
		this.designation = designation;
	}

	/**
	 * @return the seatno
	 */
	public String getSeatno() {
		return seatno;
	}

	/**
	 * @param seatno the seatno to set
	 */
	public void setSeatno(String seatno) {
		this.seatno = seatno;
	}
	
	

	/**
	 * @return the company_id
	 */
	public String getCompany_id() {
		return company_id;
	}

	/**
	 * @param company_id the company_id to set
	 */
	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}
	
	
	
	/**
	 * @return the company_id
	 */
	public String getnote() {
		return note;
	}

	public void setBadgeParentId(String badegparent_id){
		this.badgeparent_id = badegparent_id;
	}
	public String getBadgeParentId(){
		return badgeparent_id;
	}
	public void setTicketParentId(String parent_id){
		this.parent_id = parent_id;
	}
	public String getTicketParentId(){
		return parent_id;
	}
	/**
	 * @param //company_id the company_id to set
	 */
	public void setnote(String note) {
		this.note = note;
	}
	
	public void setCustomBarCode(String barcode){
		this.custom_barcode_id = Util.NullChecker(barcode);
	}
	public String getCustomBarCode(){
		return Util.NullChecker(custom_barcode_id);
	}
	public void setScanID(String scan_id){
		this.scan_id = scan_id;
	}
	public String getScanID(){
		return scan_id;
	}
	
	@Override
	public String toString() {
		return "OrderItemListHandler [GNUserId=" + GNUserId + ", TicketStatus="
				+ TicketStatus + ", RSVPstatus=" + RSVPstatus+ ", attendeeInfo=" + attendeeInfo
				+ ", badgeLabel=" + badgeLabel + ", companyName=" + companyName
				+ ", email=" + email + ", eventId=" + eventId + ", firstName="
				+ firstName + ", id=" + id + ", itemId=" + itemId
				+ ", itemPoolId=" + itemPoolId + ", itemTypeId=" + itemTypeId
				+ ", lastName=" + lastName + ", mobile=" + mobile
				+ ", orderId=" + orderId + ", orderItemId=" + orderItemId
				+ ", ticketNumber=" + ticketNumber + ", checkinhandler="
				+ checkinhandler + ", chickindate=" + chickindate +", totalSize="+totalSize
				+ ", badgeId=" + badgeId +", company_id=" + company_id 
				+", badgeparent_id="+badgeparent_id+", parent_id="+parent_id
				+", designation=" + designation +", seatno=" + seatno
				+", note=" + note+",Item_Pool__r="+Item_Pool__r
				+",Item_Type__r="+Item_Type__r+",badgeparentid__r"+badgeparentid__r+ "]";
		
		
	}

	
	public class ItemPoor_R implements Serializable {
		
		private static final long serialVersionUID = 1L;
		public String Id = "", Item_Pool_Name__c = "", Item_Type__c = "",Ticket_Settings__c="",Badgable__c="";
		public ItemType_R Item_Type__r = new ItemType_R(); 
	}
	
	public class ItemType_R implements Serializable{
		
		private static final long serialVersionUID = 1L;
		public String Id="",Name ="",Badgable__c="";
	}
	
	public class BadgeParentId_R implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String Badge_ID__c = "",Id="";
		
	}
	
	public class BadgeID_R implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public String Name="",Print_status__c="";
	}
	
	
}
