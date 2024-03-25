package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCommentsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3062094924676148622L;
	public String name;
	public int userId;
	public String comment;
	public String timestamp;
	public CardDataCommentsItem(){}
}
