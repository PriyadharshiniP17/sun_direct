package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultsData extends BaseResponseData {
	
	public Result results = new Result(); 
	public ResultsData() {
		
	}
}
