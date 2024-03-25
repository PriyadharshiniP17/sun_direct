package com.myplex.model;

import java.io.Serializable;

public class DownloadContentData implements Serializable {

    public String aUrl;
    public CardData aMovieData;
    public String fileName;
    public String mVideoUrl;
    public String mAudioUrl;
    public String videoFileName;
    public String audioFileName;
    public String subtitleLink;
    public String size;
    public String format;
    public int contentType;

    public String drmLicenseUrl;
    public String drmToken;
    public String userAgent;
    public boolean shouldDownloadOnWifi;

}
