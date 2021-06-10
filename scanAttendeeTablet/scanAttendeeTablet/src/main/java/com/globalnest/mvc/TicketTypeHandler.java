package com.globalnest.mvc;

import java.io.Serializable;

public class TicketTypeHandler implements Serializable{
	
	String type_id="", type_name="";
	
	public void setItemTypeId(String id){
		this.type_id = id;
	}

	public String getItemTypeId(){
		
		return type_id;
	}
	
	public void setItemTypeName(String name){
		this.type_name = name;
	}

	public String getItemTypeName(){
		
		return type_name;
	}
	

}
