package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataContent implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -6870595256947750470L;
	public String categoryName;
	public String drmType;
	public String releaseDate;
	public List<CardDataGenre> genre;
	public String duration;
	public String contentRating;
	public boolean is3d;
	public CardDataCertifiedRatings certifiedRatings;
	public List<String> language;
	public boolean drmEnabled;
	public String parentId;
	public String categoryType;
	public String serialNo;
	public String siblingOrder;
	public String startDate;
	public String adProvider;
	public String videoQuality;
	public String adType;
	public boolean adEnabled;
	public boolean isChromeCastEnabled;
	public ContentAdConfig adConfig;

	public String actionType;
	public String channelNumber;
	public String actionURL;
	public String nextChannel;
	public String previousChannel;
	public String isSupportCatchup;
	public List<String> subGenres;
	public List<String> audioLanguage;
	public CardDataContent(){}
}
