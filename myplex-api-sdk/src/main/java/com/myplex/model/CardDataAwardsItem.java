package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataAwardsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1690959686648920216L;
	public String _id;
	public String name;
	public String description;
	public String awardDate;
	public String category;
	public CardDataAwardsItem(){}
}
