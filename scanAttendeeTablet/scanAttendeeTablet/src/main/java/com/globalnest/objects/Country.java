package com.globalnest.objects;

import java.io.Serializable;

public class Country implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public String Id="",Long_Name__c="",Currency_code__c="",Short_Name__c="",Name="";
  
  @Override
  public String toString(){
	return "Country [Id="+Id+",Long_Name__c="+Long_Name__c+"]";
  }
  
}
