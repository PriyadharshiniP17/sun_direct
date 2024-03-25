
package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class AltBriefDescription {

    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("language")
    @Expose
    private String language;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
