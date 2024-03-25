
package com.myplex.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Result {

    @SerializedName("contentid")
    @Expose
    private String contentid;
    @SerializedName("programs")
    @Expose
    private List<CardData> programs = null;

    public String getContentid() {
        return contentid;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public List<CardData> getPrograms() {
        return programs;
    }

    public void setPrograms(List<CardData> programs) {
        this.programs = programs;
    }

}
