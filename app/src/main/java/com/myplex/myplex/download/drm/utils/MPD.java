package com.myplex.myplex.download.drm.utils;

import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Apalya on 11/18/2016.
 */

public class MPD implements Serializable {
    private String authToken;


    private static String baseUrl;
    public List<AdaptionSet> adaptionSetList;
    String mpdName;
    String streamURL;
    String[] prefixURL;
    String audioFileName;
    String videoFileName;
    private String audioURL;
    private String videoURL;


    public String getAudioURL() {
        Log.e("Download Url", baseUrl + File.separator + audioURL + "?" + authToken);
        return baseUrl + File.separator + audioURL + "?" + authToken;
    }

    public void setAudioURL(String audioURL) {
        this.audioURL = audioURL;
    }

    public String getVideoURL() {
        Log.e("Download Url", baseUrl + File.separator + videoURL + "?" + authToken);
        return baseUrl + File.separator + videoURL + "?" + authToken;
    }



    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }
    public String getAudioFileName() {
        return audioURL;
    }

    public String getVideoFileName() {
        return videoURL;
    }
    public MPD() {
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public String getStreamURL() {
        return streamURL;
    }

    public void setStreamURL(String streamURL) {
        this.streamURL = streamURL;
        try {
            //this.prefixURL = streamURL.substring(0, streamURL.lastIndexOf("/"));
//            if(partnerType == CardDetails.Partners.ALTBALAJI || !streamURL.contains("\\?")){
//                https://preprod-cdn.cloud.altbalaji.com/content/2016-06/472-57604d3c12582/SHOOTOUT_AT_WADAL_a1f8f0a3_video_track_0.mp4
//                https://sunnxt-md.akamaized.net/movies/7766/7766_est_sd_high.mpd?
// hdnea=st=1496755168~exp=1496798368~acl=/*~hmac=f4d53efeaf30bc378a4f6223d0238822a1037a1c4e47c10966cbb97ae7828628&cid=7766&country=IN&userid=4860&nid=0&osv=5.0.2&q=4&bw=2&model=Lenovo+A6000&os=Android&op=SUNNXT
            this.prefixURL = streamURL.split("\\?");
            this.authToken = prefixURL[1];
            this.baseUrl = streamURL.substring(0, streamURL.lastIndexOf(".mpd")+4);
                this.mpdName = baseUrl.substring(baseUrl.lastIndexOf("/")+1);
//                return;
//            }
//            this.prefixURL = streamURL.split("\\?");
//            this.authToken = prefixURL[1];
//            this.baseUrl = prefixURL[0].substring(0,prefixURL[0].lastIndexOf("/"));
//            this.mpdName = prefixURL[0].substring(prefixURL[0].lastIndexOf("/"));
        } catch (Exception e) {
            Log.d("MDP", "setStreamURL: " + e);
            this.prefixURL[0] = "";
        }
    }

    public String getMpdName() {
        Log.d("Download getMpdName- ",mpdName);
        return mpdName;
    }

}
