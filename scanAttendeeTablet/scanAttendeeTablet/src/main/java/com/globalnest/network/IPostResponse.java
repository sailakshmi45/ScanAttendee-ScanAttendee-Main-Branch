package com.globalnest.network;

public interface IPostResponse {

	public void doRequest();
	public void parseJsonResponse(String response);
	public void insertDB();
	
}
