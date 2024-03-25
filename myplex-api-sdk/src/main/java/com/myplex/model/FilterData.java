package com.myplex.model;

import java.util.List;

public class FilterData {
    public String title;
    public List<FilterItem> mFilterItemList;
	public FilterData(String title, List<FilterItem> filterList) {
        this.title =title;
        mFilterItemList = filterList;
	}
}
