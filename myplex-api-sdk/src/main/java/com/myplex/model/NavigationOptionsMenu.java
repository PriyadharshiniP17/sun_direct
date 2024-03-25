package com.myplex.model;

public class NavigationOptionsMenu {
	public String mLabel = new String();
	public int mDefaultResId;
	public String mIconUrl = new String();
	public int mResourceLayoutId;
	public int mScreenType;
	

	public NavigationOptionsMenu(String label, int defaultResId, String iconUrl,int screenType,int layout) {
		this.mLabel = label;
		this.mDefaultResId = defaultResId;
		this.mIconUrl = iconUrl;
		this.mScreenType = screenType;
		this.mResourceLayoutId = layout;
	}
}