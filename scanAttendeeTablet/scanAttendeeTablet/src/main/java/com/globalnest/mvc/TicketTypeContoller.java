package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;

public class TicketTypeContoller implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String total_ticket = "", ticketName = "", isTaxable = "",
			ticketNumber = "", seatNumber = "", ticketsSelected = "",
			eventId = "", server_ticket_id = "", ticket_type = "",
			ticketCategory = "", image_url = "", feesetting = "",bl_fee="",item_fee="",//added at Item level
			ticketPaytype="",ticket_type_id="",ticket_item_poolid="";

	private ArrayList<String> ticketcount = new ArrayList<String>();
	int ticketQty = 0, soldTickets = 0;
	double ticketPrice = -1, total_amount;
	private double salexTax;

	/**
	 * @return the total_amount
	 */
	public double getTotal_amount() {
		return total_amount;
	}

	/**
	 * @param total_amount
	 *            the total_amount to set
	 */
	public void setTotal_amount(double total_amount) {
		this.total_amount = total_amount;
	}

	public void setTicketId(String id) {
		this.server_ticket_id = id;
	}

	public String getTicketsId() {
		return server_ticket_id;
	}

	public void setTicketsEventId(String id) {
		this.eventId = id;
	}

	public String getTicketsEventId() {
		return eventId;
	}

	public void setSoldTickets(int sold) {
		this.soldTickets = sold;
	}

	public int getSoldTickets() {
		return soldTickets;
	}

	public void setSelectedTickets(String purchasetickets) {
		this.ticketsSelected = purchasetickets;
	}

	public String getSelectedTickets() {
		return ticketsSelected;
	}

	public void setNoofTickets(String number) {
		this.total_ticket = number;
	}

	public String getNoofTickets() {
		return total_ticket;
	}

	public void setTicketTypeName(String name) {
		this.ticketName = name;
	}

	public String getTicketTypeName() {
		return ticketName;
	}

	public void setTicketCategory(String category) {
		this.ticketCategory = category;
	}

	public String getTicketCategory() {
		return ticketCategory;
	}

	public void setTicketQuantity(int qty) {
		this.ticketQty = qty;
	}

	public int getTicketQuantity() {
		return ticketQty;
	}

	public void setTicketPrice(double price) {
		this.ticketPrice = price;
	}

	public double getTicketPrice() {
		return ticketPrice;
	}

	public void setticketNumber(String number) {
		this.ticketNumber = number;
	}

	public String getticketNumber() {
		return ticketNumber;
	}

	public void setticketSeatNumber(String seat_number) {
		this.seatNumber = seat_number;
	}

	public String getticketSeatNumber() {
		return seatNumber;
	}

	public void setTicketType(String type) {
		this.ticket_type = type;
	}

	public String getTicketType() {
		return ticket_type;
	}

	public void setTicketCountList(ArrayList<String> ticketcountlist) {
		this.ticketcount = ticketcountlist;

	}

	public ArrayList<String> getTicketCountList() {
		return ticketcount;
	}

	public void setTicketImageUrl(String url) {
		this.image_url = url;
	}

	public String getTicketImageUrl() {
		return image_url;
	}

	public void setIsTicketTaxable(String tax) {
		this.isTaxable = tax;
	}

	public String getIsTicketTaxable() {
		return isTaxable;
	}

	public void setTicketSalexTax(double tax) {
		this.salexTax = tax;
	}

	public double getTicketSalexTax() {
		return salexTax;
	}

	public void setTicketFeeSetting(String feesetting) {
		this.feesetting = feesetting;
	}

	public String getTicketFeeSetting() {
		return feesetting;
	}
	public void setBLFeeAmount(String bl_fee){
		this.bl_fee = bl_fee;
	}
	public String getBLFeeAmount(){
		return bl_fee;
	}
	public void setItemFeeAmount(String item_fee){
		this.item_fee = item_fee;
	}
	public String getItemFeeAmount(){
		return item_fee;
	}
	public void setTicketPaymentType(String paymentType){
		this.ticketPaytype = paymentType;
	}
	public String getTicketPaymentType(){
		return ticketPaytype;
	}
	
	public void setTicketTypeId(String ticket_type_id){
		this.ticket_type_id = ticket_type_id;
	}
	public String getTicketTypeId(){
		return ticket_type_id;
	}
	public void setTicketPoolId(String poolid){
		this.ticket_item_poolid = poolid;
	}
	public String getTicketPoolId(){
		return ticket_item_poolid;
	}
}
