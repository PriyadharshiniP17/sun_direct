package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponResponseData {

	public String status;
	public String message;
	public String packageId;
	public double priceTobeCharged;
	public double couponAmount;
	public ArrayList<String> errors = new ArrayList<String>();
	public CouponResponseData() 
	{
	}
	
}
