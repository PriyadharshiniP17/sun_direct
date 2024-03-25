package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CarouselInfoResponseData extends BaseResponseData {

	/**
	 *
	 */
	private static final long serialVersionUID = -3114232287673074578L;
	public List<CarouselInfoData> results;
	public CarouselInfoResponseData(){
		
	}



}