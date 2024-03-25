
package com.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Actions {

    @SerializedName("viewed")
    @Expose
    private String viewed;
    @SerializedName("archive")
    @Expose
    private String archive;

    public String getViewed() {
        return viewed;
    }

    public void setViewed(String viewed) {
        this.viewed = viewed;
    }

    public String getArchive() {
        return archive;
    }

    public void setArchive(String archive) {
        this.archive = archive;
    }

}
