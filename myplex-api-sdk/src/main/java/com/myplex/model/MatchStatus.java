package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchStatus {
	
	// "status":"notstarted/delayed/live/finished",
	public static final String STATUS_NOTSTARTED="notstarted";
	public static final String STATUS_DELAYED="delayed";
	public static final String STATUS_LIVE="live";
	public static final String STATUS_FINISHED="finished";
	
	public static enum MATCH_TYPE {CRICKET,FIFA};
	
	public List<Team> teams = new ArrayList<Team>();
	public String matchTitle;
	public String status;
	public String statusDescription;
	public String result;
	public MATCH_TYPE matchType = MATCH_TYPE.FIFA;

}
