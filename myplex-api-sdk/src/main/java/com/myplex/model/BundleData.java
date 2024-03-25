package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleData 
{
	public String contentType;
	public boolean couponFlag;	
	public String packageName;
	public String bbDescription;
	public String packageId;
	public String duration;
	public String commercialModel;
	public boolean packageIndicator;
	public boolean renewalFlag;
	public List<BundleContent> contents = new ArrayList<BundleContent>();
	public BundleData(){		
	}

}
