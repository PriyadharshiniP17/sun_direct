package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileData implements Serializable {

	/**
	 *
	 */
	public UserProfile profile;
	public UserProfileData(){
		
	}
}
