package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VstbLoginSessionResponse extends BaseResponseData
{

	public List<VstbSessionData> results;
	public VstbLoginSessionResponse(){}
	
}
