
package com.myplex.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class SelectQuality{
    private String quality;
    private String qualityToShow;
    private boolean isSelected;
    public boolean defaultSelected;


    public SelectQuality(String quality, String qualityToShow, boolean isSelected,boolean defaultSelected) {
        this.quality = quality;
        this.qualityToShow = qualityToShow;
        this.isSelected = isSelected;
        this.defaultSelected = defaultSelected;

    }
    public String getQualityToShow() {
        return qualityToShow;
    }
    public void setLanguageToShow(String qualityToShow) {
        this.qualityToShow = qualityToShow;
    }
    public String getQuality() {
        return quality;
    }
    public void setQuality(String quality) {
        this.quality = quality;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
