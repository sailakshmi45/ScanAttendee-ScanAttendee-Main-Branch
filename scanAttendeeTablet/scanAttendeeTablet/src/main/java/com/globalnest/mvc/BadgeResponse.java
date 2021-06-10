/** 
 *GlobalNest 
 * Ajay Goyal
 */
package com.globalnest.mvc;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;


public class BadgeResponse {
	
	@SerializedName("badges")
	public ArrayList<BadgeController> badges=new ArrayList<BadgeController>();

	@Override
	public String toString() {
		return "BadgeResponse [badges=" + badges + "]";
	} 
	
	
	
}
