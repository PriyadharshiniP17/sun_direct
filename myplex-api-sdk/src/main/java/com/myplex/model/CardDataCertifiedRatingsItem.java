package com.myplex.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataCertifiedRatingsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8404342724773295227L;
	public String name;
	public String rating;
	public CardDataCertifiedRatingsItem(){}
}
