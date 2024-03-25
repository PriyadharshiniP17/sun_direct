package com.myplex.model;

import java.util.ArrayList;
import java.util.List;

public class CardDetailBaseData {
	public String contentID;
	public String contentName;
	public String briefDescription;
	public String fullDescription;
	public String studioDescription;
	public String myplexDescription;
	public float rating;
	public String parentalRating;
	public String releaseDate;
	public boolean isExpanded = false;
	public List<CardDetailCastCrew> mCastCrewList = new ArrayList<CardDetailCastCrew>();
	public List<CardDetailMediaData> mPlayinPlaceList = new ArrayList<CardDetailMediaData>();
	public List<CardDetailMultiMediaGroup> mMultiMediaGroup = new ArrayList<CardDetailMultiMediaGroup>();
	public List<CardDetailCommentData> mCommentsList = new ArrayList<CardDetailCommentData>();
	public List<CardDetailCommentData> mReviewsList = new ArrayList<CardDetailCommentData>();
}
