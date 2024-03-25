package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataRelatedMultimediaItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7658987893462658720L;
//	public CardData content;
	public String _id;
	public CardDataGeneralInfo generalInfo;
	public CardDataImages images;
	public CardDataRelatedMultimediaItem(){}
}
