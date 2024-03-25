
package com.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdFullScreenConfig {

    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("position")
    @Expose
    private String position;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("durationInSeconds")
    @Expose
    private Integer durationInSeconds;
    @SerializedName("skippable")
    @Expose
    private String skippable;
    @SerializedName("skipText")
    @Expose
    private String skipText;
    @SerializedName("actionUrl")
    @Expose
    private String actionUrl;

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public String getSkippable() {
        return skippable;
    }

    public void setSkippable(String skippable) {
        this.skippable = skippable;
    }

    public String getSkipText() {
        return skipText;
    }

    public void setSkipText(String skipText) {
        this.skipText = skipText;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

}