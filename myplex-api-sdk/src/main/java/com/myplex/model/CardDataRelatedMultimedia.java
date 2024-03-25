package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimedia implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8855822486678329965L;
	public List<CardDataRelatedMultimediaItem> values = new ArrayList<CardDataRelatedMultimediaItem>();
	public CardDataRelatedMultimedia(){}
}
