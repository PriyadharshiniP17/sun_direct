package com.myplex.myplex.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationModel {


    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("sub_title")
    @Expose
    private String subTitle;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("event_time")
    @Expose
    private String eventTime;
    @SerializedName("remainder_id")
    @Expose
    private String remainderId;
    @SerializedName("remainder_time")
    @Expose
    private String remainderTime;
    @SerializedName("is_already_remainded")
    @Expose
    private int isAlreadyRemainded;
    @SerializedName("is_video")
    @Expose
    private int isVideo;
    @SerializedName("video_type")
    @Expose
    private String videoType;
    @SerializedName("video_id")
    @Expose
    private String videoId;

    boolean isExpanded;

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getRemainderId() {
        return remainderId;
    }

    public void setRemainderId(String remainderId) {
        this.remainderId = remainderId;
    }

    public String getRemainderTime() {
        return remainderTime;
    }

    public void setRemainderTime(String remainderTime) {
        this.remainderTime = remainderTime;
    }

    public int getIsAlreadyRemainded() {
        return isAlreadyRemainded;
    }

    public void setIsAlreadyRemainded(int isAlreadyRemainded) {
        this.isAlreadyRemainded = isAlreadyRemainded;
    }

    public int getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(Integer isVideo) {
        this.isVideo = isVideo;
    }

    public String getVideoType() {
        return videoType;
    }

    public void setVideoType(String videoType) {
        this.videoType = videoType;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    @Override
    public String toString() {
        return "NotificationModel{" +
                "id=" + id +
                ", icon='" + icon + '\'' +
                ", title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                ", description='" + description + '\'' +
                ", eventTime='" + eventTime + '\'' +
                ", remainderId='" + remainderId + '\'' +
                ", remainderTime='" + remainderTime + '\'' +
                ", isAlreadyRemainded=" + isAlreadyRemainded +
                ", isVideo=" + isVideo +
                ", videoType='" + videoType + '\'' +
                ", videoId='" + videoId + '\'' +
                ", isExpanded=" + isExpanded +
                '}';
    }
}