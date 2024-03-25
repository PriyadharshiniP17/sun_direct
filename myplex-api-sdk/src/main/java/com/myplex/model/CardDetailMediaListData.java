package com.myplex.model;

import java.util.ArrayList;
import java.util.List;

public class CardDetailMediaListData extends CardDetailBaseData{
	public List<CardDetailMediaData> mList = new ArrayList<CardDetailMediaData>();
	public int mLastShownIndex = 0;
	public int mNumberofBlockAdded = 1;
}
