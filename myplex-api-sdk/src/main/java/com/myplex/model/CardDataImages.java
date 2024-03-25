package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataImages implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5513734722684210985L;
	public List<CardDataImagesItem> values = new ArrayList<CardDataImagesItem>();
	public CardDataImages(){}
}
