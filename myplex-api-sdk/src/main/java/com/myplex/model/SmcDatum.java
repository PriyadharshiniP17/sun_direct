
package com.myplex.model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class SmcDatum {

    @SerializedName("smc")
    @Expose
    private String smc;
    @SerializedName("name")
    @Expose
    private String name;

    public String getSmc() {
        return smc;
    }

    public void setSmc(String smc) {
        this.smc = smc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
