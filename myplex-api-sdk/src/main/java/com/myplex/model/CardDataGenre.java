package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataGenre implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6905498495722059016L;
	public String id;
	public String name;
	public CardDataGenre(){}
}
