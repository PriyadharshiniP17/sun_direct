package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataPromotionDetailsItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8536060706610835844L;
	public String amount;
	public String promotionId;
	public String promotionName;
	public String promotionType;
	public String promotionalPrice;
	public String percentage;
	public CardDataPromotionDetailsItem(){
		
	}
}
