package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedCastList implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -4050770043406016366L;

	public String mLayoutType;
	public List<CardDataRelatedCastItem> values = new ArrayList<CardDataRelatedCastItem>();
	public RelatedCastList(){}
}
