package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagePriceDetails {
	public List<CardDataPackagePriceDetailsItem> values = new ArrayList<CardDataPackagePriceDetailsItem>();
	public CardDataPackagePriceDetails(){}
}
