package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataComments implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 564071273732829007L;
	public List<CardDataCommentsItem> values = new ArrayList<CardDataCommentsItem>();
	public int numComments;
	public CardDataComments(){
		
	}
}
