package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseResponseData {
	public String status;
	public String message;
	public String request_id;
	public int code;
    public String mobile;
    public boolean display;
	public String userid;
	public String serviceName;
	public String email;
	public String response;
	public String web_url;
	public boolean isGDPREnabled;
	public String show_privacy_popup;


	public BaseResponseData() {

	}

	@Override
	public String toString() {
		return "BaseResponseData: status- " + status + " code- " + code + " message- " + message + " userid- " + userid + " email- " + email + " response- " + response;
	}
}
