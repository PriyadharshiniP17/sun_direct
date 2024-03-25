package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataSubtitleItem implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -6905498495722059016L;
	public String language;
	public String link_sub;
	public CardDataSubtitleItem(){}

	@Override
	public String toString() {
		return "["+
				" language: " + language +
				" link_sub: " + link_sub +
				"]";
	}
}
