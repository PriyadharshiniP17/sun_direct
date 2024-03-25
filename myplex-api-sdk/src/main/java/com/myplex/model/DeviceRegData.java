package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class DeviceRegData extends BaseResponseData {
	public String expiresAt;
    public String deviceId;
    public String clientKey;
    public AppLoginConfigData appLoginConfig ;
    public DeviceRegData(){

    }
}
