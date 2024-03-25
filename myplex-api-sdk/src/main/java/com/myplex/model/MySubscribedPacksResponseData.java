package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MySubscribedPacksResponseData extends BaseResponseData {

	/**
	 *
	 */
	public List<CardDataPackages> results = new ArrayList<>();
	public MySubscribedPacksResponseData(){
		
	}
}
