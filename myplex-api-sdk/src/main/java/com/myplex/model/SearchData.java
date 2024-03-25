package com.myplex.model;

import java.util.List;


public class SearchData {
	
	public static class ButtonData{

		String mButtonId;
		String mButtonName;
		String mButtonQualifer;
		String mButtonCategory;
		boolean isCLicked = false;
		
		
		public String getButtonId()
		{
			return this.mButtonId;
		}
		
		public String getButtonName() {
			return mButtonName;
		}
		
		public boolean isCLicked() {
			return isCLicked;
		}
		public void setCLicked(boolean isCLicked) {
			this.isCLicked = isCLicked;
		}
		
		public String getTagCategory()
		{
			return this.mButtonCategory;
		}
		
		public ButtonData(String id,String name,String category, String qualifier, boolean clickStatus) {
			this.mButtonId = id;
			this.mButtonName = name;
			this.mButtonCategory = category;
			this.mButtonQualifer = qualifier;
			this.isCLicked = clickStatus;
		}
	}
	
	String categoryName;
	boolean isSection = false;
	List<ButtonData> searchTags;
	boolean isExpanded=false;
    public static final int SECTION = 1;
    
    public SearchData()
    {
    }
    public SearchData(SearchData data)
    {
    	this.categoryName = data.categoryName;
    	this.isSection = data.isSection;
    	this.searchTags = data.searchTags;
    }

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

	public boolean isSection() {
		return isSection;
	}

	public void setSection(boolean isSection) {
		this.isSection = isSection;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
		this.isSection = true;
	}

    public List<ButtonData> getSearchTags() {
		return searchTags;
	}

	public void setSearchTags(List<ButtonData> searchTags) {
		this.searchTags = searchTags;
		this.isSection = false;
	}

}
