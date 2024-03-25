package com.myplex.myplex.media.exoVideo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.exoplayer2.source.TrackGroupArray;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackData implements Serializable, Comparable<TrackData>{
    public String name;
    public float bitrate;
    public int position;
    public float minBitrate;
    public float maxBitrate;
    public int trackRenderGroupPosition;
    public TrackGroupArray trackGroups;

    public TrackData() {

    }

    @Override
    public int compareTo(TrackData trackData) {
        return this.position > trackData.position ? 1 : this.position == trackData.position ? 0 : -1 ;
    }
}
