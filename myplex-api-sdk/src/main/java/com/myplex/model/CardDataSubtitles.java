package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataSubtitles implements Serializable {
	/**
	 *
	 */
	public List<CardDataSubtitleItem> values;
	public CardDataSubtitles(){}
	@Override
	public String toString() {
		return "CardDataSubtitles:[" + "" +
				"values: " + values
				+ "]";
	}
}
