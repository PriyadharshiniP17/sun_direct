package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedContent implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 474778913019096728L;
	public String duration;
	public CardDataCertifiedRatings certifiedRatings;
	public CardDataRelatedContent(){}
}
