package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FavouriteResponse extends BaseResponseData
{

	public boolean favorite;
	public boolean watchlist;
	public boolean like;
}
