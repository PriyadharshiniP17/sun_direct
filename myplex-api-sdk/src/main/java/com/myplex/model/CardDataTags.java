package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataTags implements Serializable {

	public List<CardDataTagsItem> values = new ArrayList<>();
	public CardDataTags(){}
}
