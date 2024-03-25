package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCriticReviews implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8436168085963617386L;
	public List<CardDataCriticReviewsItem> values = new ArrayList<CardDataCriticReviewsItem>();
	public CardDataCriticReviews(){}
}
