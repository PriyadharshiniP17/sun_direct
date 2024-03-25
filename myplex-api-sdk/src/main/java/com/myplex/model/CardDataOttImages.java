package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataOttImages implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 5513734722684210985L;
	public List<CardDataOttImagesItem> values = new ArrayList<>();
	public CardDataOttImages(){}
}
