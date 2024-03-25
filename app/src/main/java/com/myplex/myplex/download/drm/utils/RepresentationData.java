package com.myplex.myplex.download.drm.utils;

import android.util.Log;

import java.io.File;

/**
 * Created by Srikanth on 18-Aug-17.
 */

public class RepresentationData {

    String url;
    public long bandwidth;
    public float dataPerHourInGbph;
    public int id;

    public String getUrl() {
        Log.d("Download getUrl-",MPD.getBaseUrl() + File.separator + url);
        return MPD.getBaseUrl() + File.separator + url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return url;
    }
}
