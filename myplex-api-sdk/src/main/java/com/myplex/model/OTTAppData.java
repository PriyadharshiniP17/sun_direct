package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OTTAppData extends BaseResponseData implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3114232287673074578L;
    public List<OTTApp> results;
	public OTTAppData(){
		
	}



}