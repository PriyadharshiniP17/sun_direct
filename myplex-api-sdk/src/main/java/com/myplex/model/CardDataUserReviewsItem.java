package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataUserReviewsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8937075285197127739L;
	public String username;
	public String userId;
	public float rating;
	public String review;
	public String timestamp;
	public String name;
	public CardDataUserReviewsItem(){}
}
