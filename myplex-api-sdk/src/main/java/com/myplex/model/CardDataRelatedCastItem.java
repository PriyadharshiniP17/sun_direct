package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedCastItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4050770043406016366L;
	public String _id;
	public String name;
	public List<String> roles = new ArrayList<String>();
	public List<String> types = new ArrayList<String>();
	public CardDataImages images;
	public String mLayoutType;
	public CardDataRelatedCastItem(){}
}
