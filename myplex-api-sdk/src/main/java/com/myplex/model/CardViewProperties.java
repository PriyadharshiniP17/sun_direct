package com.myplex.model;

public class CardViewProperties {
	public float mLastPosition;
	public float mStartPosition;
	public double mRelativeUnitUp = 1;
	public double mRelativeUnitDown = 1;
	public float mPreviousItemPosition;
	public float mNextItemPosition;
	public boolean mDecidingFactor = false;
	public CardViewProperties(float lastposition,float startposition,double relativeunitup,double relativeunitdown,float mpreviousposition,float mnextposition,boolean decidingfactor){
		this.mLastPosition = lastposition;
		this.mStartPosition = startposition;
		this.mRelativeUnitUp = relativeunitup;
		this.mRelativeUnitDown = relativeunitdown;
		this.mPreviousItemPosition = mpreviousposition;
		this.mNextItemPosition = mnextposition;
		this.mDecidingFactor = decidingfactor;
	}
	public CardViewProperties(){
		
	}
}
