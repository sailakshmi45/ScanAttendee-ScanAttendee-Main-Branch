package com.globalnest.objects;

import java.io.Serializable;

public class States implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String Long_Name__c="",Country__c="",Id="",Short_Name__c="",Name="";
	
	@Override
	public String toString(){
		return "States [Id="+Id+",Country__c="+Country__c+",Short_Name__c="+Short_Name__c
				 +",Long_Name__c="+Long_Name__c+"]";
	}
  
 
}
