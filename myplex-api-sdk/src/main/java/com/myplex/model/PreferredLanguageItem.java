package com.myplex.model;

import java.io.Serializable;

/**
 * Created by Apparao on 26/02/2019.
 */
public class PreferredLanguageItem implements Serializable{
    private String humanReadable;
    private boolean is_default;
    private String image;
    private String term;
    public boolean isChecked;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getHumanReadable() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }

    public boolean isDefault() {
        return is_default;
    }

    public void setIs_default(boolean isDefault) {
        this.is_default = isDefault;
    }

    @Override
    public String toString() {
        return "term = "+term+" , image = "+getImage()+" , humanreadable = "+getHumanReadable()+" , is_default = "+isDefault();
    }
}
