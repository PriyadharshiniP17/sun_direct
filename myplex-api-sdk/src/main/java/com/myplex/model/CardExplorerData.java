package com.myplex.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class CardExplorerData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7373512867616315000L;
	public static final int REQUEST_RECOMMENDATION = 1;
	public static final int REQUEST_SEARCH = 2;
	public static final int REQUEST_FAVOURITE = 3;
	public static final int REQUEST_SIMILARCONTENT = 4;
	public static final int REQUEST_DOWNLOADS = 5;
	public static final int REQUEST_PURCHASES = 6;
	public static final int REQUEST_BROWSE = 7;
	public static final int REQUEST_INLINESEARCH = 8;
	public static final int REQUEST_TV_SHOWS = 9;
	public static final int REQUEST_CAROUSEL = 10;	
	public static final int REQUEST_INDIAN_TV_SHOWS = 11;
	public static final int REQUEST_VIDEOS = 12;
	public String requestUrl = new String();
	public int currentSelectedCard = 0;
	public HashMap<String,CardData> mEntries = new HashMap<String,CardData>();
	public ArrayList<CardData> mMasterEntries = new ArrayList<CardData>();
	public int mStartIndex = 1;
	public String searchQuery = new String();
	public String searchScope = new String();
	public String language = new String();
	public int requestType = REQUEST_RECOMMENDATION;
	public boolean continueWithExisting = false;
	public CardData cardDataToSubscribe = null;
	public void reset(){
		cardDataToSubscribe = null;
		requestType = REQUEST_SEARCH;
		searchQuery = new String();
		searchScope = new String();
		language = new String();
		currentSelectedCard = 0;
		mStartIndex = 1;
		mEntries = new HashMap<String,CardData>();
		mMasterEntries = new ArrayList<CardData>();
		continueWithExisting = false;
	}
}
