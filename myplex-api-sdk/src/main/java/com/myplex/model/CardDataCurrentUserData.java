package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCurrentUserData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5959237256889098035L;
	public float rating;
	public boolean favorite;
	public boolean watchlist;
	public List<CardDataPurchaseItem> purchase;
	public CardDataCurrentUserData(){
		
	}
}
