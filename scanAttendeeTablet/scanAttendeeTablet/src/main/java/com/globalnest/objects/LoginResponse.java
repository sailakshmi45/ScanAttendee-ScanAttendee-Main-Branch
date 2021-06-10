package com.globalnest.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LoginResponse implements Serializable{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public UserObjects Profile = new UserObjects();
  public List<EventObjects> Events = new ArrayList<EventObjects>();
  public List<EventObjects> UpcomingEvents = new ArrayList<EventObjects>();
  public List<Currency> CurrencyList = new ArrayList<Currency>();
  public List<ItemType> ItemTypes = new ArrayList<ItemType>();
  public List<PaymentGateWaysRes> PGatewaytypes = new ArrayList<PaymentGateWaysRes>();
  public List<PaymentType> PayGateways = new ArrayList<PaymentType>();
  public String StripeEventdexClientID ="",StripeEventdexSecretKey="";
  
  @Override
   public String toString(){
	   return "LoginResponse [Profile="+Profile+",CountriesList="+CurrencyList+",Events="+Events+",UpcomingEvents="+UpcomingEvents+",PGatewaytypes="+PGatewaytypes+",ItemTypes="+ItemTypes
			   +",PayGateways="+PayGateways+",StripeEventdexClientID="+StripeEventdexClientID+",StripeEventdexSecretKey="+StripeEventdexSecretKey+"]";
   }
  
}
