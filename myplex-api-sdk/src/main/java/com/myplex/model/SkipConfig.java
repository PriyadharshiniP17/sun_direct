package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class SkipConfig implements Serializable {
    @JsonProperty("skipIntro")
    private List<SkipIntro> skipIntro = null;
    @JsonProperty("scenes")
    private List<Object> scenes = null;
    @JsonProperty("skipEndCredit")
    private List<Object> skipEndCredit = null;
    @JsonProperty("skipIntro")
    public List<SkipIntro> getSkipIntro() {
        return skipIntro;
    }

    @JsonProperty("skipIntro")
    public void setSkipIntro(List<SkipIntro> skipIntro) {
        this.skipIntro = skipIntro;
    }

    @JsonProperty("scenes")
    public List<Object> getScenes() {
        return scenes;
    }

    @JsonProperty("scenes")
    public void setScenes(List<Object> scenes) {
        this.scenes = scenes;
    }

    @JsonProperty("skipEndCredit")
    public List<Object> getSkipEndCredit() {
        return skipEndCredit;
    }

    @JsonProperty("skipEndCredit")
    public void setSkipEndCredit(List<Object> skipEndCredit) {
        this.skipEndCredit = skipEndCredit;
    }
}
