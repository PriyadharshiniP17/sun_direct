package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCertifiedRatings implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7863532731979288052L;
	public List<CardDataCertifiedRatingsItem> values = new ArrayList<CardDataCertifiedRatingsItem>();
	public CardDataCertifiedRatings(){}
}
