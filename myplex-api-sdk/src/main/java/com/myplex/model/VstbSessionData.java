package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VstbSessionData implements Serializable
{
	public String globalServiceId;
	public String hostName;
	public String _id;
	public String timeshiftDuration;
	public VstbSessionItem videos;

	public VstbSessionData(){

	}
}
