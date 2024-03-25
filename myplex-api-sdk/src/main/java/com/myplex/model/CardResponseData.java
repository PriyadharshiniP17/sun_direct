package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardResponseData extends BaseResponseData {
	
	/**
	 * 
	 */
	public List<CardData> results;
	public String trackingId;
	public Map<String, String> responseHeaders = Collections.emptyMap();
	public int mStartIndex = 1;
	public CustomUI ui;
	public CardResponseData(){
		
	}
}
