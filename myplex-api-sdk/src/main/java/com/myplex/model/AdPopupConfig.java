
package com.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AdPopupConfig {

    @SerializedName("enableAdPopUp")
    @Expose
    private String enableAdPopUp;
    @SerializedName("imageURL")
    @Expose
    private String imageURL;
    @Expose
    private String language;
    @SerializedName("id")
    @Expose
    private String id;

    public String getEnableAdPopUp() {
        return enableAdPopUp;
    }

    public void setEnableAdPopUp(String enableAdPopUp) {
        this.enableAdPopUp = enableAdPopUp;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setLanguage(String language){
        this.language=language;
    }
    public String getLanguage() {
        return language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}