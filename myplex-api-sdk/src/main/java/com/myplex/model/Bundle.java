package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bundle extends BaseResponseData
{

	public BundleResult results = new BundleResult();
	
}
