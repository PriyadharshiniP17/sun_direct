package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)

public class VideoInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5444134620786836582L;

	@JsonProperty("cdnTypes")
	public List<String> cdnTypes = new ArrayList<String>();
	@JsonProperty("languages")
	public List<String> languages = new ArrayList<String>();
	@JsonProperty("quality")
	public List<String> quality = new ArrayList<String>();
	@JsonProperty("profiles")
	public List<String> profiles = new ArrayList<String>();
	@JsonProperty("cameraAngles")
	public List<String> cameraAngles = new ArrayList<String>();

}
