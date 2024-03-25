package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionResponseData {
	public String status;
	public String message;
	public int code;

	public ConnectionResponseData() {

	}

	@Override
	public String toString() {
		return "BaseResponseData: status- " + status + " code- " + code + " message- " + message ;
	}
}
