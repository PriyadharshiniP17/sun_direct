package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferralData  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1456228481282130301L;
	public ReferralData(){
		
	}
	public ReferralResults results=null;
	public String status=null;
	public String message=null;
	public String code=null;
	
}
