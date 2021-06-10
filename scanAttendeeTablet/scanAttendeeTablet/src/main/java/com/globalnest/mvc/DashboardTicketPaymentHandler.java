package com.globalnest.mvc;

import java.io.Serializable;

public class DashboardTicketPaymentHandler implements Serializable {
	
	public double revenue=0;
	public String paygatey="";
	
	//public DashboardPaymentTypeHandler paygatey=new DashboardPaymentTypeHandler();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashboardTicketPaymentHandler [revenue=" + revenue
				+ ", paygatey=" + paygatey + "]";
	}
	
	
}
