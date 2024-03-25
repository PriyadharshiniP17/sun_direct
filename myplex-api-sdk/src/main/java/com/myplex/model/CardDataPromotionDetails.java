package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPromotionDetails {
	public List<CardDataPromotionDetailsItem> valueList = new ArrayList<CardDataPromotionDetailsItem>();
	public CardDataPromotionDetails(){}
}