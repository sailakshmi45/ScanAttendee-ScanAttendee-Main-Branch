package com.globalnest.mvc;

import java.io.Serializable;

public class AttendeeDetailsController implements Serializable {

	private String attendeeName = "", attendeeEmail = "", attendeeAddress = "", attendeeCategory="",
			attendeeCity = "", attendeeZipCode = "", attendeeCompany="", attendeeDes="", attendeeMobile="", ticketPrice="", ticketTypes="",
			paymentStatus="", attendeeId="", seatNumber="", paymentType="", eventId="", prefix="", attendeelName="", attendeeState="",attendee_country="",
			ticketRegistered="", totalPrice="", barcode="", itemTypes="", itemPrice="", checkedDate="", chequeNum="", ticketNumber="",
			mobile_att_id="", payment_date="", qrcode_id="";

	public void setAttendeeId(String id) {
		this.attendeeId = id;
	}

	public String getAttendeeId() {
		return attendeeId;
	}
	public void setAttendeeMobileId(String id) {
		this.mobile_att_id = id;
	}

	public String getAttendeeMobileId() {
		return mobile_att_id;
	}
	
	public void setAttendeeQrCodeId(String id) {
		this.qrcode_id = id;
	}

	public String getAttendeeQrCodeId() {
		return qrcode_id;
	}
	
	public void setAttendeeName(String name) {
		this.attendeeName = name;
	}

	public String getAttendeeName() {
		return attendeeName;
	}
	
	public void setAttendeeLName(String lName) {
		this.attendeelName = lName;
	}

	public String getAttendeeLName() {
		return attendeelName;
	}

	public void setAttendeeEmail(String email) {
		this.attendeeEmail = email;
	}

	public String getAttendeeEmail() {
		return attendeeEmail;
	}
	public void setAttendeeCompany(String comp) {
		this.attendeeCompany = comp;
	}

	public String getAttendeeCompany() {
		return attendeeCompany;
	}

	
	public void setAttendeeDesignation(String des) {
		this.attendeeDes = des;
	}

	public String getAttendeeDesignation() {
		return attendeeDes;
	}
	

	public void setAttendeeCategory(String category_type) {
		this.attendeeCategory = category_type;
	}

	public String getAttendeeCategory() {
		return attendeeCategory;
	}
	
	public void setAttendeeMobile(String mobile) {
		this.attendeeMobile = mobile;
	}

	public String getAttendeeMobile() {
		return attendeeMobile;
	}

	public void setAttendeeAddress(String address) {
		this.attendeeAddress = address;
	}

	public String getAttendeeAddress() {
		return attendeeAddress;
	}

	public void setAttendeeCity(String city) {
		this.attendeeCity = city;
	}

	public String getAttendeeCity() {
		return attendeeCity;
	}

	public void setAttendeeZipCode(String zipcode) {
		this.attendeeZipCode = zipcode;
	}

	public String getAttendeeZipCode() {
		return attendeeZipCode;
	}
	
	public void setAttendeeTotalPrice(String price) {
		this.totalPrice = price;
	}

	public String getAttendeeTotalPrice() {
		return totalPrice;
	}
	
	public void setAttendeeTicketPrice(String price) {
		this.ticketPrice = price;
	}

	public String getAttendeeTicketPrice() {
		return ticketPrice;
	}
	
	public void setAttendeeItemPrice(String price) {
		this.itemPrice = price;
	}

	public String getAttendeeItemPrice() {
		return itemPrice;
	}
	
	public void setAttendeeTicketTypes(String types) {
		this.ticketTypes = types;
	}

	public String getAttendeeTicketTypes() {
		return ticketTypes;
	}
	
	public void setAttendeeItemTypes(String types) {
		this.itemTypes = types;
	}

	public String getAttendeeItemTypes() {
		return itemTypes;
	}
	
	public void setAttendeePaymentStatus(String status) {
		this.paymentStatus = status;
	}

	public String getAttendeePaymentStatus() {
		return paymentStatus;
	}
	
	public void setAttendeeSeatNumner(String number) {
		this.seatNumber = number;
	}

	public String getAttendeeSeatNumber() {
		return seatNumber;
	}
	public void setAttendeeTicketNumber(String number) {
		this.ticketNumber = number;
	}

	public String getAttendeeTicketNumber() {
		return ticketNumber;
	}
	
	public void setAttendeePaymentType(String pType) {
		this.paymentType = pType;
	}

	public String getAttendeePaymentType() {
		return paymentType;
	}
	
	public void setAttendeeEventId(String eventid) {
		this.eventId = eventid;
	}

	public String getAttendeeEventId() {
		return eventId;
	}
	
	public void setAttendeePrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getAttendeePrefix() {
		return prefix;
	}
	
	public void setAttendeeState(String state) {
		this.attendeeState = state;
	}

	public String getAttendeeState() {
		return attendeeState;
	}
	public void setAttendeeCountry(String country) {
		this.attendee_country = country;
	}

	public String getAttendeeCountry() {
		return attendee_country;
	}
	public void setTicketRegistered(String ticket) {
		this.ticketRegistered = ticket;
	}

	public String getTicketRegistered() {
		return ticketRegistered;
	}
	
	public void setAttendeeBarCode(String att_barCode) {
		this.barcode = att_barCode;
	}

	public String getAttendeeBarCode() {
		return barcode;
	}
	
	public void setAttendeeCheckedDate(String date) {
		this.checkedDate = date;
	}

	public String getAttendeeCheckedDate() {
		
		return checkedDate;
	}
	public void setAttendeePaymentDate(String date) {
		this.payment_date = date;
	}

	public String getAttendeePaymentDate() {
		return payment_date;
	}
	public void setAttendeeChequeNum(String chequenum) {
		this.chequeNum = chequenum;
	}

	public String getAttendeeChequeNum() {
		return chequeNum;
	}
	

}

