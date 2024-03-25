package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPurchase {
	public List<CardDataPurchaseItem> values = new ArrayList<CardDataPurchaseItem>();
	public CardDataPurchase(){}
}
