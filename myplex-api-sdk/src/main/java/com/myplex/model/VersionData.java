package com.myplex.model;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)

public class VersionData implements Serializable{
	public int version;
	public String link;
	public String message;
	public String type;
	public String menu_default;
	public String menu_index;
	public String negativeButtonText;
	public String positiveButtonText;
	public String alertTitle;

	public boolean validate(){
		
		if(TextUtils.isEmpty(link) || TextUtils.isEmpty(message) || TextUtils.isEmpty(type) || version == 0){
			return false;
		}
		
		return true;
	}
}
