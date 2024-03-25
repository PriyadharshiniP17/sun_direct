package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoStatusUI implements Serializable {
    public String title;
    public String action;
    public String message;
    public boolean close_enabled;

    public VideoStatusUI(){

    }

    @Override
    public String toString() {
        return "VideoStatusUI: title- " + title
              + " action- " + action + " message- " + message;
    }
}
