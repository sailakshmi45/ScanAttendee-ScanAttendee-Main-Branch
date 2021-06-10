package com.globalnest.mvc;

import java.io.Serializable;
import java.util.ArrayList;

public class DashboardTicketHandler implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public double itrevenue=0;
	public int ItemQuantity=0,checloutCnt=0,chekinCont=0;	
	public DashboardTicketNameObject item=new DashboardTicketNameObject();
	public ArrayList<DashboardPackageItems> pitems = new ArrayList<DashboardPackageItems>();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DashboardTicketHandler [itrevenue=" + itrevenue
				+ ", ItemQuantity=" + ItemQuantity + ", checloutCnt="
				+ checloutCnt + ", chekinCont=" + chekinCont + ", item=" + item
				+ ", pitems=" + pitems + "]";
	}
	
	
	
	
}
