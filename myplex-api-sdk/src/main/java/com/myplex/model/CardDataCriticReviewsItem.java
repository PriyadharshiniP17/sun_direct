package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCriticReviewsItem implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2112648370157888250L;
	public String name;
    public float rating;
    public String review;
    public CardDataCriticReviewsItem(){}
}
