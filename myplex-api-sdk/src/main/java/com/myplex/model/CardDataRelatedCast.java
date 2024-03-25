package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedCast implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2783712815633378776L;
	public List<CardDataRelatedCastItem> values = new ArrayList<CardDataRelatedCastItem>();
	public List<RelatedCastList> castLists=new ArrayList<RelatedCastList>();
}
