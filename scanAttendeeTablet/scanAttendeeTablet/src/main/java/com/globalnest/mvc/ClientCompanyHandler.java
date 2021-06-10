 package com.globalnest.mvc;

import com.google.gson.annotations.SerializedName;

public class ClientCompanyHandler {
	
	@SerializedName("Name")
    private String compName;
	
	@SerializedName("Id")
    private String compId;

	public String getCompName() {
		return compName;
	}

	public void setCompName(String compName) {
		this.compName = compName;
	}

	public String getCompId() {
		return compId;
	}

	public void setCompId(String compId) {
		this.compId = compId;
	}
	
	@Override
	public String toString() {
		return "ClientCompanyHandler [compName=" + compName
				+ ", compId=" + compId +"]";
	}

}
