package com.globalnest.mvc;

import java.io.Serializable;

public class CollectionController implements Serializable {

	private String eventId = "", ticket_id = "", item_id = "",
			paymentTypes = "", pName = "", pDate = "", attendeeId = "",
			stripe_payment_id = "", payment_type = "", payment_status = "",
			check_no = "", ticket_num = "", item_num = "", isCheckedIn = "",
			ticket_name = "", checked_ticketnumber = "", item_name = "",
			 offline_checIn_tickets, payment_date = "",
			seatNum = "", payment_id = "", order_type = "", ticket_checkin_time="", ticket_checkout_time="";
	double t_amount = 0, totalprice;
	int qty;

	public void setEventId(String id) {
		this.eventId = id;
	}

	public String getEventId() {
		return eventId;
	}

	public void setTicketId(String id) {
		this.ticket_id = id;
	}

	public String getTicketId() {
		return ticket_id;
	}

	public void setTicketName(String name) {
		this.ticket_name = name;
	}

	public String getTicketName() {
		return ticket_name;
	}

	public void setItemId(String id) {
		this.item_id = id;
	}

	public String getItemId() {
		return item_id;
	}

	public void setitemName(String name) {
		this.item_name = name;
	}

	public String getItemName() {
		return item_name;
	}

	public void setAttendeeId(String att_id) {
		this.attendeeId = att_id;
	}

	public String getAttendeeId() {
		return attendeeId;
	}

	

	public void setPaymentType(String type) {
		this.paymentTypes = type;
	}

	public String getPaymentType() {
		return paymentTypes;
	}

	public void setAmount(double price) {
		this.totalprice = price;
	}

	public double getAmount() {
		return totalprice;
	}

	public void setPurchaseName(String name) {
		this.pName = name;
	}

	public String getPurchaseName() {
		return pName;
	}

	public void setPurchasedate(String date) {
		this.pDate = date;
	}

	public String getPurchaseDate() {
		return pDate;
	}

	public void setAttendeePaymentStatus(String status) {
		this.payment_status = status;
	}

	public String getAttendeePaymentStatus() {
		return payment_status;
	}

	public void setAttendeePaymentType(String pType) {
		this.payment_type = pType;
	}

	public String getAttendeePaymentType() {
		return payment_type;
	}

	public void setAttendeeCheckNum(String pType) {
		this.check_no = pType;
	}

	public String getAttendeeCheckNum() {
		return check_no;
	}

	public void setAttendeeOfflineCheckNum(String offline_ticket) {
		this.offline_checIn_tickets = offline_ticket;
	}

	public String getAttendeeOfflineCheckNum() {
		return offline_checIn_tickets;
	}

	public void setTicketQty(int qty) {
		this.qty = qty;
	}

	public int getTicketQty() {
		return qty;
	}

	public void setAttendeeTicketNum(String pType) {
		this.ticket_num = pType;
	}

	public String getAttendeeTicketNum() {
		return ticket_num;
	}

	public void setAttendeeItemNum(String item) {
		this.item_num = item;
	}

	public String getAttendeeItemNum() {
		return item_num;
	}

	public void setAttendeeSeatNum(String seat) {
		this.seatNum = seat;
	}

	public String getAttendeeSeatNum() {
		return seatNum;
	}

	public void setAttendeeCheckedTicketNum(String pType) {
		this.checked_ticketnumber = pType;
	}

	public String getAttendeeCheckedTicketNum() {
		return checked_ticketnumber;
	}

	public void setTotalAmount(double amt) {
		this.t_amount = amt;
	}

	public double getTotalAmount() {
		return t_amount;
	}

	public void setCheckInStatus(String check_status) {
		this.isCheckedIn = check_status;
	}

	public String getCheckInStatus() {
		return isCheckedIn;
	}

	public void setPaymentTime(String date) {
		this.payment_date = date;
	}

	public String getPaymentTime() {
		return payment_date;
	}

	public void setTicketCheckinTime(String date) {
		this.ticket_checkin_time = date;
	}

	public String getTicketCheckinTime() {
		return ticket_checkin_time;
	}
	public void setTicketCheckoutTime(String date) {
		this.ticket_checkout_time = date;
	}

	public String getTicketCheckoutTime() {
		return ticket_checkout_time;
	}
	
	public void setOrderId(String id) {
		this.payment_id = id;
	}

	public String getOrderId() {
		return payment_id;
	}

	public void setStripePaymentId(String id) {
		this.stripe_payment_id = id;
	}

	public String getStripePaymentId() {
		return stripe_payment_id;
	}

	public void setOrderType(String type) {
		this.order_type = type;
	}

	public String getOrderType() {
		return order_type;
	}

}
