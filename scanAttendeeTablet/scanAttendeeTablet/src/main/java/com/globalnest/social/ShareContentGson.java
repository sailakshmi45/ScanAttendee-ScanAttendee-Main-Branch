package com.globalnest.social;

import com.google.gson.annotations.SerializedName;

public class ShareContentGson {

	String title,description/*,submitted-url,submitted-image-url*/;
	@SerializedName("submitted-url")
	String submittedUrl;
	@SerializedName("submitted-image-url")
	String submittedImageUrl;
}
