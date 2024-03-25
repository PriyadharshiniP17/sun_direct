package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideosItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5038410985071493754L;
	public String profile;
	public String format;
	public String type;
	public String bitrate;
	public String link;
	public String licenseUrl;
	public String license_url;
	public String resolution;
	public String drmToken;
	public int elapsedTime;
	public CardDataVideosItem(){
		
	}
}
