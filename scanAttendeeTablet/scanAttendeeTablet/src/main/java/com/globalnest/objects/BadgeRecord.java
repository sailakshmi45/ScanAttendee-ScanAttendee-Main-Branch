package com.globalnest.objects;

import java.io.Serializable;

public class BadgeRecord  implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Event__c="",Id="",Name="",Description__c="";

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BadgeRecord [Event__c=" + Event__c + ", Id=" + Id + ", Name="
				+ Name + "]";
	}
	
	

}
