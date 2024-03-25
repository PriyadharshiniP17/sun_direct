package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class VersionUpdateData extends BaseResponseData {
	public VersionData app = new VersionData();
	public VersionUpdateData(){}
	

}
