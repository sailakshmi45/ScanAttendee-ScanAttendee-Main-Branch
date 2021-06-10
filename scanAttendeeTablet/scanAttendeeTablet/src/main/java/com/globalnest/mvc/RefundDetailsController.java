package com.globalnest.mvc;

import android.os.Parcel;
import android.os.Parcelable;

public class RefundDetailsController implements Parcelable{
	private String eventId = "", ticket_id = "", item_id = "",payment_id="",
			  pDate = "", attendeeId = "",stripe_payment_id = "", payment_type = "",
			 ticket_name ="", item_name="", attendee_name="", ticket_number="", item_number="";
	double totalprice, actual_price;
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

	public void setAttendeeName(String name) {
		this.attendee_name = name;
	}

	public String getAttendeeName() {
		return attendee_name;
	}
	
	public void setPaymentType(String type) {
		this.payment_type = type;
	}

	public String getPaymentType() {
		return payment_type;
	}

	public void setAmount(double price) {
		this.totalprice = price;
	}

	public double getAmount() {
		return totalprice;
	}
	public void setActualAmount(double price) {
		this.actual_price = price;
	}

	public double getActualAmount() {
		return actual_price;
	}
	public void setRefunddate(String date) {
		this.pDate = date;
	}

	public String getRefunddate() {
		return pDate;
	}
	public void setAttendeeTicketNum(String pType) {
		this.ticket_number = pType;
	}

	public String getAttendeeTicketNum() {
		return ticket_number;
	}

	public void setAttendeeItemNum(String item) {
		this.item_number = item;
	}

	public String getAttendeeItemNum() {
		return item_number;
	}
	public void setRefundOrderId(String id) {
		this.payment_id = id;
	}

	public String getRefundOrderId() {
		return payment_id;
	}

	public void setStripePaymentId(String id) {
		this.stripe_payment_id = id;
	}

	public String getStripePaymentId() {
		return stripe_payment_id;
	}
	
	 public RefundDetailsController() {
		// TODO Auto-generated constructor stub
	}

	private RefundDetailsController(Parcel in) {
		eventId = in.readString();
		ticket_number = in.readString();
		item_number = in.readString();
		totalprice = in.readDouble();
		actual_price = in.readDouble();
		attendee_name = in.readString();
		attendeeId = in.readString();
		pDate = in.readString();
		attendeeId = in.readString();
		
		payment_type = in.readString();
		
		payment_id = in.readString();
		
		ticket_name = in.readString();
		item_name = in.readString();
		item_id = in.readString();
		ticket_id = in.readString();
		stripe_payment_id = in.readString();
		
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<RefundDetailsController> CREATOR = new Parcelable.Creator<RefundDetailsController>() {

		public RefundDetailsController createFromParcel(Parcel source) {
			return new RefundDetailsController(source);
		}

		public RefundDetailsController[] newArray(int size) {
			return new RefundDetailsController[size];
		}

	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
		
		dest.writeString(eventId);
		dest.writeString(attendee_name);
		dest.writeDouble(totalprice);
		dest.writeDouble(actual_price);
		dest.writeString(pDate);
		dest.writeString(attendeeId);
		dest.writeString(payment_type);
		dest.writeString(payment_id);
		dest.writeString(ticket_number);
		dest.writeString(ticket_name);
		dest.writeString(item_name);
		dest.writeString(item_id);
		dest.writeString(ticket_id);
		dest.writeString(item_number);
		dest.writeString(stripe_payment_id);

		

	}

}
