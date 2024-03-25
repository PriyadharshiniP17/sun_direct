package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagePriceDetailsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5840267809143390511L;
	public float price;
	public float promotionalPrice;
	public String paymentChannel;
	public boolean doubleConfirmation;
	public boolean webBased;
	public String name;
	public CardDataPackagePriceDetailsItem(){}
}
