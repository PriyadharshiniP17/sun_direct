package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataUserReviews implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1218627102199330165L;
	public int numUsersRated;
	public List<CardDataUserReviewsItem> values = new ArrayList<CardDataUserReviewsItem>();
	public float averageRating;
	public CardDataUserReviews(){
		
	}
}
