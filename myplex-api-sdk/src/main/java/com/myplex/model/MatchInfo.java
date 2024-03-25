package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchInfo {

	public String statusUrl;
	public String matchStartTime;
	public String matchMobileUrl;
	public String matchEndTime;
	public String matchDataProvider;
	public String matchDataUrl;
	public String matchUrl;

}
