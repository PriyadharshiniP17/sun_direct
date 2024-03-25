package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdPopUpNotificationListResponse {

    @SerializedName("adPopUpNotification")
    @Expose
    private List<AdPopupConfig> adPopUpNotification = null;

    public List<AdPopupConfig> getAdFullScreenConfig() {
        return adPopUpNotification;
    }

    public void setAdFullScreenConfig(List<AdPopupConfig> adFullScreenConfig) {
        this.adPopUpNotification = adFullScreenConfig;
    }

}
