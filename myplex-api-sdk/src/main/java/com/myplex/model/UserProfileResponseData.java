package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileResponseData extends BaseResponseData {

	/**
	 *
	 */
	public UserProfileData result = new UserProfileData();;
	public UserProfileResponseData(){
		
	}
}
