package com.globalnest.objects;

import java.util.ArrayList;
import java.util.List;

public class CountriesObj {
    public Country Country= new Country();
    public List<States> States = new ArrayList<States>();
    public Currency currencym = new Currency();
    
    @Override
    public String toString(){
    	return "CountriesObj [Country="+Country+",States="+States+",currencym="+currencym+"]";
    }
}
