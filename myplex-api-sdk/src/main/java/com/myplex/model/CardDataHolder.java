package com.myplex.model;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class CardDataHolder {
	public RelativeLayout mDeleteLayout;
	public TextView mDelete;
	public ImageView mLangNoteIcon;
	
	public RelativeLayout mFavLayout;
	public TextView mFavourite;
	public ProgressBar mFavProgressBar;
	
	public RelativeLayout mTitleLayout;
	public TextView mTitle;
	public TextView mSubTitle;
	
	public RelativeLayout mPreviewLayout;
	public ImageView mPreview;
	public ImageView mOverLayPlay;
	
	public RelativeLayout mRentLayout;
	public TextView mRentText;
	
	
	public TextView mComments;
	public TextView mReviews;
	public TextView mCommentsText;
	public TextView mReviewsText;
	
	
	public ProgressBar mESTDownloadBar;
	public TextView mESTDownloadStatus;
	public CardData mDataObject;
	public TextView mCardDescText;
	public TextView mVideoStatusText;
	public TextView mVideoDurationText;
}
