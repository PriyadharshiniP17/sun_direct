package com.myplex.model;


public class FilterMenudata {
	public static final int ITEM = 0;
	public static final int SECTION = 1;
	public int menuId;
	public String label;
	public final int type;
	public int defaultResId;
	public String ImageUrl;
	
	public FilterMenudata(int type, String text,int Id) {
		this.type = type;
		this.label = text;
		this.menuId = Id;
	}
//	public List<FilterMenudata> submenuItem = new ArrayList<FilterMenudata>();
}
