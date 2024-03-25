package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImagesItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9006518291552596096L;
	public String profile;
	public String link;
	public String type;
	public String resolution;
	public String siblingOrder;
	public CardDataImagesItem(){}

	@Override
	public String toString() {
		return "link- " + link;
	}
}
