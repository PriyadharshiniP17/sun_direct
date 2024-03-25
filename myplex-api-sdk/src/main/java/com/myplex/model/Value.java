
package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Value {

    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("profile")
    @Expose
    private String profile;
    @SerializedName("resolution")
    @Expose
    private String resolution;
    @SerializedName("link")
    @Expose
    private String link;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

}
