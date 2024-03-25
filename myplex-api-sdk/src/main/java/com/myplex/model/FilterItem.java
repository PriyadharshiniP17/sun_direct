package com.myplex.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by phani on 1/4/2016.
 */
public class FilterItem implements Serializable{
    private String title;
    private boolean isChecked;

    public FilterItem(String title, boolean isChecked) {
        this.title = title;
        this.isChecked = isChecked;
    }
    public FilterItem(){

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }



}
