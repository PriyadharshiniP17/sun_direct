package com.myplex.model;


import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdFullScreenListResponse {

    @SerializedName("adFullScreenConfig")
    @Expose
    private List<AdFullScreenConfig> adFullScreenConfig = null;

    public List<AdFullScreenConfig> getAdFullScreenConfig() {
        return adFullScreenConfig;
    }

    public void setAdFullScreenConfig(List<AdFullScreenConfig> adFullScreenConfig) {
        this.adFullScreenConfig = adFullScreenConfig;
    }

}
