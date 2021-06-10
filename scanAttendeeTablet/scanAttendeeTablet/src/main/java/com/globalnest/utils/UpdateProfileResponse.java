package com.globalnest.utils;

import com.globalnest.objects.UserProfile;

public class UpdateProfileResponse {
	
	
	//public String profileimage;
	public String Userid = "", profilestate = "", profileimage = "",
			profilecountry = "",profileCity="";
	public UserProfile Profile =new UserProfile();

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UpdateProfileResponse [profileimage=" + profileimage
				+ ", Profile=" + Profile + "]";
	}
	
	

}
