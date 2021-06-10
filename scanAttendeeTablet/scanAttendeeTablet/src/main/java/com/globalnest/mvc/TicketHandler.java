package com.globalnest.mvc;

import java.io.Serializable;

import com.globalnest.objects.RegSettings;

public class TicketHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	String sale_start__c = "", sale_end__c = "", item_name__c = "",
			Payment__c = "", max_per_order__c = "", min_per_order__c = "",
			image_url__c = "", Id = "", Item_Pool__c = "", Event__c = "",
			Ticket_Settings__c = "", service_fee__c = "", item_type__c = "",
			Visibility__c = "",Taxable__c= "",SA_Onsite_Visibility__c="",Hide_Ticket_SA__c="",Fee="",isBadgable="",Badgelabel__c;

	public Item_type_r item_type__r = new Item_type_r();
	double price__c = 0;
	int Available_Tickets__c = 0, item_count__c = 0;

	public String getImage_url() {
		return image_url__c;
	}

	public void setImage_url(String image_url) {
		this.image_url__c = image_url;
	}
	public void setIsBadgable(String isBadgable){
		this.isBadgable=isBadgable;
	}

	public String getBadgable(){
		return isBadgable;
	}
	public void setBadgeLabel(String isBadgelabel){
		this.Badgelabel__c=isBadgelabel;
	}
	public String getBadgelabel(){
		return Badgelabel__c;
	}
	public void setItemFee(String itemfee){

		this.Fee=itemfee;
	}
	public String getItemFee(){
		return Fee;
	}
	public String getSale_start__c() {
		return sale_start__c;
	}

	public void setSale_start__c(String sale_start__c) {
		this.sale_start__c = sale_start__c;
	}

	public String getSale_end__c() {
		return sale_end__c;
	}

	public void setSale_end__c(String sale_end__c) {
		this.sale_end__c = sale_end__c;
	}

	public String getItem_name__c() {
		return item_name__c;
	}

	public void setItem_name__c(String item_name__c) {
		this.item_name__c = item_name__c;
	}

	public String getPayment__c() {
		return Payment__c;
	}

	public void setPayment__c(String payment__c) {
		this.Payment__c = payment__c;
	}

	public String getMax_per_order__c() {
		return max_per_order__c;
	}

	public void setMax_per_order__c(String max_per_order__c) {
		this.max_per_order__c = max_per_order__c;
	}

	public String getMin_per_order__c() {
		return min_per_order__c;
	}

	public void setMin_per_order__c(String min_per_order__c) {
		this.min_per_order__c = min_per_order__c;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		this.Id = id;
	}

	public String getItem_Pool__c() {
		return Item_Pool__c;
	}

	public void setItem_Pool__c(String item_Pool__c) {
		this.Item_Pool__c = item_Pool__c;
	}

	public String getVisibility__c() {
		return Visibility__c;
	}
	public String get_SA_Visibility__c() {
		return SA_Onsite_Visibility__c;
	}
	public void setSAOnsiteVisibility__c(String SA_Onsite_Visibility__c) {
		this.SA_Onsite_Visibility__c = SA_Onsite_Visibility__c;
	}
	public String get_Hide_Visibility__c() {
		return Hide_Ticket_SA__c;
	}
	/*public void setHideVisibility__c(String Hide_Ticket_SA__c) {
		this.Hide_Ticket_SA__c = Hide_Ticket_SA__c;
	}*/

	public void setVisibility__c(String visibility__c) {

		this.Visibility__c = visibility__c;
	}

	public double getPrice__c() {
		return price__c;
	}

	public void setPrice__c(double price__c) {
		this.price__c = price__c;
	}

	public int getAvailable_Tickets__c() {
		return Available_Tickets__c;
	}

	public void setAvailable_Tickets__c(int available_Tickets__c) {
		this.Available_Tickets__c = available_Tickets__c;
	}

	public int getItem_count__c() {
		return item_count__c;
	}

	public void setItem_count__c(int item_count__c) {
		this.item_count__c = item_count__c;
	}

	/**
	 * @return the event__c
	 */
	public String getEvent__c() {
		return Event__c;
	}

	/**
	 * @param event__c
	 *            the event__c to set
	 */
	public void setEvent__c(String event__c) {
		this.Event__c = event__c;
	}

	/**
	 * @return the ticket_Settings__c
	 */
	public String getTicket_Settings__c() {
		return Ticket_Settings__c;
	}

	/**
	 * @param ticket_Settings__c
	 *            the ticket_Settings__c to set
	 */
	public void setTicket_Settings__c(String ticket_Settings__c) {
		this.Ticket_Settings__c = ticket_Settings__c;
	}

	/**
	 * @return the service_fee__c
	 */
	public String getService_fee__c() {
		return service_fee__c;
	}

	/**
	 * @param service_fee__c
	 *            the service_fee__c to set
	 */
	public void setService_fee__c(String service_fee__c) {
		this.service_fee__c = service_fee__c;
	}




	/**
	 * @return the taxable__c
	 */
	public String getTaxable__c() {
		return Taxable__c;
	}

	/**
	 * @param taxable__c the taxable__c to set
	 */
	public void setTaxable__c(String taxable__c) {
		this.Taxable__c = taxable__c;
	}

	public RegSettings Reg_Settings__r = new RegSettings();

	@Override
	public String toString() {
		return "TicketHandler[sale_end__c=" + sale_end__c + ", sale_start__c="
				+ sale_start__c
				+ ", Visibility__c=" + Visibility__c
				+ ", SA_Onsite_Visibility__c=" + SA_Onsite_Visibility__c
				+ ", price__c=" + price__c + ", min_per_order__c="
				+ min_per_order__c + ", max_per_order__c=" + max_per_order__c
				+ ", Payment__c=" + Payment__c + ", Available_Tickets__c="
				+ Available_Tickets__c + ", item_name__c=" + item_name__c
				+ ", item_count__c=" + item_count__c + ", Id=" + Id
				+ ", Item_Pool__c=" + Item_Pool__c + ", service_fee__c="
				+ service_fee__c + ", Ticket_Settings__c=" + Ticket_Settings__c
				+ ", Event__c=" + Event__c + ", item_type__c=" + item_type__c
				+ ", Taxable__c=" + Taxable__c
				+ "]";
	}

	/**
	 * @return the item_type__c
	 */
	public String getItem_type__c() {
		return item_type__c;
	}

	/**
	 * @param item_type__c
	 *            the item_type__c to set
	 */
	public void setItem_type__c(String item_type__c) {
		this.item_type__c = item_type__c;
	}



	public class Item_type_r implements Serializable{
		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		public String Id="",Name="",BL_Fee_Amt_per_res__c="",BL_Fee_Pct_per_res__c="";
	}




}
