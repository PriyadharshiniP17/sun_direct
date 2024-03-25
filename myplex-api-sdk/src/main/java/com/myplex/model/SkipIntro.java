package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class SkipIntro implements Serializable {
    @JsonProperty("description")
    private String description;
    @JsonProperty("skipStartPosition")
    private int skipStartPosition;
    @JsonProperty("image")
    private String image;
    @JsonProperty("jumpTo")
    private int jumpTo;
    @JsonProperty("skipEndPosition")
    private int skipEndPosition;
    @JsonProperty("action")
    private String action;
    @JsonProperty("skipType")
    private String skipType;
    @JsonProperty("skipDisplay")
    private String skipDisplay;

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("skipStartPosition")
    public int getSkipStartPosition() {
        return skipStartPosition;
    }

    @JsonProperty("skipStartPosition")
    public void setSkipStartPosition(int skipStartPosition) {
        this.skipStartPosition = skipStartPosition;
    }

    @JsonProperty("image")
    public String getImage() {
        return image;
    }

    @JsonProperty("image")
    public void setImage(String image) {
        this.image = image;
    }

    @JsonProperty("jumpTo")
    public int getJumpTo() {
        return jumpTo;
    }

    @JsonProperty("jumpTo")
    public void setJumpTo(int jumpTo) {
        this.jumpTo = jumpTo;
    }

    @JsonProperty("skipEndPosition")
    public int getSkipEndPosition() {
        return skipEndPosition;
    }

    @JsonProperty("skipEndPosition")
    public void setSkipEndPosition(int skipEndPosition) {
        this.skipEndPosition = skipEndPosition;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("skipType")
    public String getSkipType() {
        return skipType;
    }

    @JsonProperty("skipType")
    public void setSkipType(String skipType) {
        this.skipType = skipType;
    }

    @JsonProperty("skipDisplay")
    public String getSkipDisplay() {
        return skipDisplay;
    }

    @JsonProperty("skipDisplay")
    public void setSkipDisplay(String skipDisplay) {
        this.skipDisplay = skipDisplay;
    }

}
