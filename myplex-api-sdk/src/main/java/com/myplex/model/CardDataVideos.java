package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDataVideos implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4394934829056419470L;
    public List<CardDataVideosItem> values = new ArrayList<CardDataVideosItem>();
    //TODO: Add class for vast ads
    public ContentAdConfig adConfig;
    public String status;
    public String message;
    public int elapsedTime;
    public VideoStatusUI ui;
    public String appAction;
    public String actionUrl;

    public CardDataVideos() {

    }
}
