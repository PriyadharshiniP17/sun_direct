package com.myplex.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardVideoResponseResult {
    public CardDataVideos videos;
   // public CardResponseSubtitleData subtitles;
    public int elapsed_time;
    public CardDataGeneralInfo generalInfo;
    public VideoInfo videoInfo;
}
