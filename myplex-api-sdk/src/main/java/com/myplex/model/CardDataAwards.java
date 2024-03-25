package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataAwards implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5088952090911802150L;
	public List<CardDataAwardsItem> values = new ArrayList<CardDataAwardsItem>();
	public CardDataAwards(){}
}
