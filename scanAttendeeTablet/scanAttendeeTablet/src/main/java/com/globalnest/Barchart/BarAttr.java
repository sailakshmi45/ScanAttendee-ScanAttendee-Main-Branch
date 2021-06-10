package com.globalnest.Barchart;

import java.io.Serializable;

public class BarAttr implements Serializable {

	public String barBottomLabels = "", barTopLabels = "", barOthers = "",item_pool_c="",session_name="";
	public double frontBarPercentage = 0, backBarPercentage = 0;
	public boolean isType = false,isPackage=false;
	public int barColor = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BarAttr [barBottomLabels=" + barBottomLabels + ", barTopLabels=" + barTopLabels + ", barOthers="
				+ barOthers + ", frontBarPercentage=" + frontBarPercentage + ", backBarPercentage=" + backBarPercentage
				+ ", barColor=" + barColor + "]";
	}

}
