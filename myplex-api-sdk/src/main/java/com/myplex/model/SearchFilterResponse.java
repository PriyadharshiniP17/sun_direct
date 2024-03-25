package com.myplex.model;

import java.io.Serializable;

public class SearchFilterResponse implements Serializable {
    public String displayName;
    public String publishingHouseId;
    public String key;
    public String searchFields;
    public boolean isChecked;
}