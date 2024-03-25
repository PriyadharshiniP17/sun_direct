package com.myplex.model;

import java.io.Serializable;

/**
 * Created by Srikanth on 19-Aug-17.
 */

public class DownloadMediadata implements Serializable{
    public String url;
    public int videoTrackId,audioTrackId;
    public String videoUrl,audioUrl,varientType;

    public DownloadMediadata(String url, int videoTrackId, int audioTrackId, String videoFileName, String audioFileName, String variantType) {
        this.url = url;
        this.videoTrackId = videoTrackId;
        this.audioTrackId = audioTrackId;
        this.videoUrl = videoFileName;
        this.audioUrl = audioFileName;
        this.varientType = varientType;
    }
}
