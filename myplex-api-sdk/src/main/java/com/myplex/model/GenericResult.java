package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericResult<T> {

	public List<T> values = new ArrayList<T>();
	public int totalCount;
}
