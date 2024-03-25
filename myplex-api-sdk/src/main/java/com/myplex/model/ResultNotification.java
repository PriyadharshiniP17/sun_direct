
package com.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultNotification {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("action")
    @Expose
    private String action;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("subject")
    @Expose
    private String subject;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("androidActionUrl")
    @Expose
    private String androidActionUrl;
    @SerializedName("iosActionUrl")
    @Expose
    private String iosActionUrl;
    @SerializedName("webActionUrl")
    @Expose
    private String webActionUrl;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("nonDrmVideoUrl")
    @Expose
    private String nonDrmVideoUrl;
    @SerializedName("actions")
    @Expose
    private Actions actions;
    @SerializedName("notificationCreatedTime")
    @Expose
    private String notificationCreatedTime;
    @SerializedName("lastModifiedTime")
    @Expose
    private String lastModifiedTime;
    private boolean expanded;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAndroidActionUrl() {
        return androidActionUrl;
    }

    public void setAndroidActionUrl(String androidActionUrl) {
        this.androidActionUrl = androidActionUrl;
    }

    public String getIosActionUrl() {
        return iosActionUrl;
    }

    public void setIosActionUrl(String iosActionUrl) {
        this.iosActionUrl = iosActionUrl;
    }

    public String getWebActionUrl() {
        return webActionUrl;
    }

    public void setWebActionUrl(String webActionUrl) {
        this.webActionUrl = webActionUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNonDrmVideoUrl() {
        return nonDrmVideoUrl;
    }

    public void setNonDrmVideoUrl(String nonDrmVideoUrl) {
        this.nonDrmVideoUrl = nonDrmVideoUrl;
    }

    public Actions getActions() {
        return actions;
    }

    public void setActions(Actions actions) {
        this.actions = actions;
    }

    public String getNotificationCreatedTime() {
        return notificationCreatedTime;
    }

    public void setNotificationCreatedTime(String notificationCreatedTime) {
        this.notificationCreatedTime = notificationCreatedTime;
    }

    public String getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(String lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
