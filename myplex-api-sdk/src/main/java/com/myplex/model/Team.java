package com.myplex.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Team {

	public String sname;
	public String name;
	public String score;

	public boolean validate(){
		if(TextUtils.isEmpty(sname) || TextUtils.isEmpty(score)){
			return false;
		}
		
		return true;
	}
}
