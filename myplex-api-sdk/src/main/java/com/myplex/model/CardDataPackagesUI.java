package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPackagesUI implements Serializable {

	/**
	 *
	 */
	public String terms;
	public String promoImage;
	public String actionButtonText;
	public String action;
	public String redirect;
	public CardDataPackagesUI(){
		
	}

	@Override
	public String toString() {
		return "CardDataPackagesUI: terms- " + terms
				+" promoImage- " + promoImage + " actionButtonText- " + actionButtonText + " action- " + action + " redirect- " + redirect;
	}
}
