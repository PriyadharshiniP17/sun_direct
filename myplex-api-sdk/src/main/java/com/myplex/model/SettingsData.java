package com.myplex.model;

public class SettingsData {
	public static final int VIEWTYPE_TOGGLEBUTTON = 1;
	public static final int VIEWTYPE_NORMAL = 3;
	public static final int ITEM = 0;
	public static final int SECTION = 1;
	public String mSettingName;
	public final int type;
	public int viewtype = VIEWTYPE_NORMAL;
	public int imageId; 
	
	public SettingsData(int type, String text, int resourceId,int viewType) {
		this.type = type;
		this.mSettingName = text;
		this.imageId = resourceId;
		this.viewtype = viewType;
	}
}
