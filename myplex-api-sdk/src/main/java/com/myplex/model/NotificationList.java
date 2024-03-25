
package com.myplex.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationList {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("results")
    @Expose
    private List<ResultNotification> results = null;
    @SerializedName("pagination")
    @Expose
    private List<Pagination> pagination = null;

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

    public List<ResultNotification> getResults() {
        return results;
    }

    public void setResults(List<ResultNotification> results) {
        this.results = results;
    }

    public List<Pagination> getPagination() {
        return pagination;
    }

    public void setPagination(List<Pagination> pagination) {
        this.pagination = pagination;
    }

}
