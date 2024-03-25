package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleContent {
	public String contentId;
	public String contentName;
	public BundleContent(){
	}

}
