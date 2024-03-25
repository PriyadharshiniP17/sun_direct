
package com.myplex.model;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class SMCLIstResponse {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("smcs")
    @Expose
    private List<String> smcs = null;
    @SerializedName("smcData")
    @Expose
    private List<SmcDatum> smcData = null;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSmcs() {
        return smcs;
    }

    public void setSmcs(List<String> smcs) {
        this.smcs = smcs;
    }

    public List<SmcDatum> getSmcData() {
        return smcData;
    }

    public void setSmcData(List<SmcDatum> smcData) {
        this.smcData = smcData;
    }

}
